package com.home.plugins

import com.home.service.FcmService
import com.home.service.MesureService
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import org.jetbrains.exposed.sql.Database

private const val POSTGRES_HOST = "localhost"
private const val POSTGRES_PORT = 5432
private const val POSTGRES_DB = "home"
private const val POSTGRES_USER = "postgres"
private const val POSTGRES_PASSWORD = "postgres"
private const val POSTGRES_SSLMODE = "disable"
private const val POSTGRES_DRIVER = "org.postgresql.Driver"

private fun buildJdbcUrl(): String {
    return "jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/$POSTGRES_DB?user=$POSTGRES_USER&password=$POSTGRES_PASSWORD&sslmode=$POSTGRES_SSLMODE"
}

fun Application.configureDependencies() {
    dependencies {
        provide<Database> {
            Database.connect(
                url = buildJdbcUrl(),
                user = POSTGRES_USER,
                driver = POSTGRES_DRIVER,
                password = POSTGRES_PASSWORD,
            )
        }
        
        provide<MesureService> {
            MesureService(resolve())
        }
        
        provide<FcmService> {
            FcmService("firebase-service-account.json")
        }
    }
}
