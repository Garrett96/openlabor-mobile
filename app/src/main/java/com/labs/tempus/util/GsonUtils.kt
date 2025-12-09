package com.labs.tempus.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object GsonUtils {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            .registerTypeAdapter(object : TypeToken<LocalDateTime?>() {}.type, LocalDateTimeNullableDeserializer())
            .create()
    }

    private class LocalDateTimeSerializer : JsonSerializer<LocalDateTime> {
        override fun serialize(
            src: LocalDateTime,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            return JsonPrimitive(formatter.format(src))
        }
    }

    private class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): LocalDateTime {
            return LocalDateTime.parse(json.asString, formatter)
        }
    }
    
    private class LocalDateTimeNullableDeserializer : JsonDeserializer<LocalDateTime?> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): LocalDateTime? {
            return if (json == null || json.isJsonNull) {
                null
            } else {
                LocalDateTime.parse(json.asString, formatter)
            }
        }
    }
}
