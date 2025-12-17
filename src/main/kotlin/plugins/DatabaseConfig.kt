package plugins

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import org.jetbrains.exposed.sql.Database

// Centralized, self-documenting configuration values
private const val POSTGRES_HOST = "localhost"
private const val POSTGRES_PORT = 5432
private const val POSTGRES_DB = "home"
private const val POSTGRES_USER = "postgres"
private const val POSTGRES_PASSWORD = "postgres"
private const val POSTGRES_SSLMODE = "disable"

// Correct PostgreSQL driver class
private const val POSTGRES_DRIVER = "org.postgresql.Driver"

// Small helper to build a clear JDBC URL
private fun buildJdbcUrl(
    host: String = POSTGRES_HOST,
    port: Int = POSTGRES_PORT,
    database: String = POSTGRES_DB,
    user: String = POSTGRES_USER,
    password: String = POSTGRES_PASSWORD,
    sslMode: String = POSTGRES_SSLMODE,
): String {
    return "jdbc:postgresql://$host:$port/$database?user=$user&password=$password&sslmode=$sslMode"
}

fun Application.configureDatabase() {
    dependencies {
        provide {
            Database.connect(
                url = buildJdbcUrl(),
                user = POSTGRES_USER,
                driver = POSTGRES_DRIVER,
                password = POSTGRES_PASSWORD,
            )
        }
    }
}