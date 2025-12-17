package com.home

import ai.koog.ktor.Koog
import ai.koog.ktor.aiAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import com.home.model.MesureMessage
import com.home.service.FcmService
import com.home.service.MesureService
import db.TypeMesureEnum
import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
import io.github.damir.denis.tudor.ktor.server.rabbitmq.rabbitMQ
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock

fun Application.configureFrameworks() {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable -> log.error("ExceptionHandler got $throwable") }
    val rabbitMQScope = CoroutineScope(SupervisorJob() + exceptionHandler)
    val json = Json { ignoreUnknownKeys = true }
    
    val mesureService: MesureService by dependencies
    val fcmService: FcmService by dependencies

    install(RabbitMQ) {
        uri = "amqp://guest:guest@localhost:5672"
        defaultConnectionName = "default-connection"
        dispatcherThreadPollSize = 4
        tlsEnabled = false
        scope = rabbitMQScope
    }

    rabbitmq {
        basicConsume {
            autoAck = true
            queue = "temperature"
            dispatcher = Dispatchers.rabbitMQ
            coroutinePollSize = 100

            deliverCallback<String> { msg ->
                processMessage("temperature", MesureMessage(msg.body.replace("\"","").toDouble(), TypeMesureEnum.TEMPERATURE, Clock.System.now()), mesureService, fcmService)
            }

            deliverFailureCallback { msg ->
                log.info("Received undeliverable message (deserialization failed): ${msg.body.toString(Charsets.UTF_8)}")
            }
        }
        basicConsume {
            autoAck = true
            queue = "humidity"
            dispatcher = Dispatchers.rabbitMQ
            coroutinePollSize = 100

            deliverCallback<String> { msg ->
                processMessage("humidity", MesureMessage(msg.body.replace("\"","").toDouble(), TypeMesureEnum.HUMIDITE, Clock.System.now()), mesureService, fcmService)
            }

            deliverFailureCallback { msg ->
                log.info("Received undeliverable message (deserialization failed): ${msg.body.toString(Charsets.UTF_8)}")
            }
        }
        basicConsume {
            autoAck = true
            queue = "gaz"
            dispatcher = Dispatchers.rabbitMQ
            coroutinePollSize = 100

            deliverCallback<String> { msg ->
                processMessage("gaz", MesureMessage(msg.body.replace("\"","").toDouble(), TypeMesureEnum.GAZ, Clock.System.now()), mesureService, fcmService)
            }
    
            deliverFailureCallback { msg ->
                log.info("Received undeliverable message (deserialization failed): ${msg.body.toString(Charsets.UTF_8)}")
            }
        }
    }

    rabbitmq {
        queueBind {
            queue = "test-queue"
            exchange = "test-exchange"
            routingKey = "test-routing-key"
            exchangeDeclare {
                exchange = "test-exchange"
                type = "direct"
            }
            queueDeclare {
                queue = "test-queue"
                arguments = mapOf(
                    "x-dead-letter-exchange" to "dlx",
                    "x-dead-letter-routing-key" to "dlq-dlx"
                )
            }
        }
    }

    routing {
        rabbitmq {
            get("/rabbitmq") {
                basicPublish {
                    exchange = "test-exchange"
                    routingKey = "test-routing-key"
                    properties = basicProperties {
                        correlationId = "jetbrains"
                        type = "plugin"
                        headers = mapOf("ktor" to "rabbitmq")
                    }
                    message { "Hello Ktor!" }
                }

                call.respondText("Hello RabbitMQ!")
            }
        }

        rabbitmq {
            basicConsume {
                autoAck = true
                queue = "test-queue"
                dispatcher = Dispatchers.rabbitMQ
                coroutinePollSize = 100

                deliverCallback<String> { msg ->
                    log.info("Received message: ${msg.body}")
                    error("Error during message processing: ${msg.body}")
                }

                deliverFailureCallback { msg ->
                    log.info("Received undeliverable message (deserialization failed): ${msg.body.toString(Charsets.UTF_8)}")
                }
            }
        }
    }
    
    install(Koog) {
        llm {
            openAI(apiKey = "your-openai-api-key")
            anthropic(apiKey = "your-anthropic-api-key")
            ollama { baseUrl = "http://localhost:11434" }
            google(apiKey = "your-google-api-key")
            openRouter(apiKey = "your-openrouter-api-key")
            deepSeek(apiKey = "your-deepseek-api-key")
        }
    }

    routing {
        route("/ai") {
            post("/chat") {
                val userInput = call.receive<String>()
                val output = aiAgent(userInput, model = OpenAIModels.Chat.GPT4_1)
                call.respondText(output)
            }
        }
    }
}

private suspend fun Application.processMessage(
    queueSource: String,
    rawMessage: MesureMessage,
    mesureService: MesureService,
    fcmService: FcmService
) {
    try {
        log.info("Processing message from '$queueSource': $rawMessage")
        
        val message = rawMessage
        
        // 1. Sauvegarde en DB
        val savedMesure = mesureService.saveMesure(message)
        log.info("Mesure saved with ID: ${savedMesure.id.value}")
        
        // 2. Envoi vers Firebase Cloud Messaging (topic basé sur la queue)
        fcmService.sendToTopic(queueSource, message)
        log.info("FCM notification sent to topic: $queueSource")
        
    } catch (e: Exception) {
        log.error("Error processing message from '$queueSource': ${e.message}", e)
    }
}
