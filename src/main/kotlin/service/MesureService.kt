package com.home.service

import com.home.model.AlertLevel
import com.home.model.FmsNotification
import com.home.model.MesureMessage
import db.MesureEntity
import db.Mesures
import db.TypeMesureEnum
import db.TypesMesureEntity
import db.TypesMesures
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class MesureService(private val database: Database) {

    fun saveMesure(message: MesureMessage): MesureEntity {
        return transaction(database) {
            val typeMesureEnum = message.typeMesureCode
            val typeMesure = TypesMesureEntity.find { TypesMesures.code eq typeMesureEnum }
                .firstOrNull() ?: throw IllegalArgumentException("Type mesure not found: ${message.typeMesureCode}")

            MesureEntity.new {
                valeur = message.valeur
                type_mesure = typeMesure
                mesureAt = message.mesureAt
            }
        }
    }

    fun createFmsNotification(
        queueSource: String,
        mesure: MesureEntity,
        message: MesureMessage
    ): FmsNotification {
        val alertLevel = determineAlertLevel(queueSource, message.valeur)
        
        return FmsNotification(
            source = queueSource,
            mesureId = mesure.id.value,
            valeur = message.valeur,
            typeMesure = message.typeMesureCode,
            timestamp = message.mesureAt,
            alertLevel = alertLevel
        )
    }

    private fun determineAlertLevel(queueSource: String, valeur: Double): AlertLevel {
        return when (queueSource) {
            "temperature" -> when {
                valeur > 40 || valeur < 0 -> AlertLevel.CRITICAL
                valeur > 35 || valeur < 5 -> AlertLevel.WARNING
                else -> AlertLevel.INFO
            }
            "humidity" -> when {
                valeur > 90 || valeur < 20 -> AlertLevel.CRITICAL
                valeur > 80 || valeur < 30 -> AlertLevel.WARNING
                else -> AlertLevel.INFO
            }
            "gaz" -> when {
                valeur > 1000 -> AlertLevel.CRITICAL
                valeur > 500 -> AlertLevel.WARNING
                else -> AlertLevel.INFO
            }
            else -> AlertLevel.INFO
        }
    }
}
