package com.home

import com.home.plugins.configureDependencies
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDependencies()
    configureHTTP()
    configureSecurity()
    configureFrameworks()
    configureSerialization()
    configureRouting()
}
