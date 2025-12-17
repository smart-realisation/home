package db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// Custom column type pour mapper varchar vers enum Kotlin
inline fun <reified T : Enum<T>> Table.varcharEnum(name: String, length: Int = 255): Column<T> =
    registerColumn(name, object : org.jetbrains.exposed.sql.ColumnType<T>() {
        override fun sqlType(): String = "VARCHAR($length)"
        override fun valueFromDB(value: Any): T = when (value) {
            is String -> enumValueOf<T>(value)
            is Enum<*> -> value as T
            else -> error("Unexpected value: $value")
        }
        override fun notNullValueToDB(value: T): Any = value.name
    })

enum class TypeMesureEnum {
    TEMPERATURE, HUMIDITE, GAZ
}

enum class UniteEnum {
    CELSIUS, PERCENT, PPM
}

object TypesMesures : IntIdTable("types_mesure") {
    val code = varcharEnum<TypeMesureEnum>("code")
    val unite = varcharEnum<UniteEnum>("unite")
    val description = varchar("description", 255)
}

class TypesMesureEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TypesMesureEntity>(TypesMesures)

    var code: TypeMesureEnum by TypesMesures.code
    var unite: UniteEnum by TypesMesures.unite
    var description by TypesMesures.description
}

object Mesures : IntIdTable("mesures") {
    val valeur = double("valeur")
    val type_mesure_id = reference("type_mesure_id", TypesMesures)
    val mesureAt = timestamp("mesure_at")
}

class MesureEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MesureEntity>(Mesures)
    var valeur by Mesures.valeur
    var type_mesure by TypesMesureEntity referencedOn Mesures.type_mesure_id
    var mesureAt by Mesures.mesureAt
}
