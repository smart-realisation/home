package com.home.model

import db.TypeMesureEnum
import db.TypesMesures
import kotlinx.serialization.Serializable

@Serializable
data class MesureMessage(
    val valeur: Double,
    val typeMesureCode: TypeMesureEnum,
    @Serializable(with = InstantSerializer::class)
    val mesureAt: kotlinx.datetime.Instant
)

object InstantSerializer : kotlinx.serialization.KSerializer<kotlinx.datetime.Instant> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("Instant", kotlinx.serialization.descriptors.PrimitiveKind.STRING)
    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: kotlinx.datetime.Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): kotlinx.datetime.Instant = kotlinx.datetime.Instant.parse(decoder.decodeString())
}

@Serializable
data class FmsNotification(
    val source: String,
    val mesureId: Int,
    val valeur: Double,
    val typeMesure: TypeMesureEnum,
    @Serializable(with = InstantSerializer::class)
    val timestamp: kotlinx.datetime.Instant,
    val alertLevel: AlertLevel = AlertLevel.INFO
)

@Serializable
enum class AlertLevel {
    INFO, WARNING, CRITICAL
}
