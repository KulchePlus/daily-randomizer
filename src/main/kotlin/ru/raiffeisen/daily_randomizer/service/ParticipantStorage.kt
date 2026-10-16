package ru.raiffeisen.daily_randomizer.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.nio.file.Files
import java.nio.file.Path

/**
 * Хранит список участников в JSON-файле. По умолчанию — ~/.daily-randomizer/names.json.
 */
class ParticipantStorage(private val filePath: Path = defaultPath()) {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun load(): List<Participant> {
        if (!Files.exists(filePath)) {
            return emptyList()
        }
        return try {
            val content = Files.readString(filePath)
            objectMapper.readValue<List<Participant>>(content)
        } catch (e: Exception) {
            throw IllegalStateException("Не удалось прочитать файл участников: $filePath", e)
        }
    }

    fun save(participants: List<Participant>) {
        try {
            Files.createDirectories(filePath.parent)
            Files.writeString(filePath, objectMapper.writeValueAsString(participants))
        } catch (e: Exception) {
            throw IllegalStateException("Не удалось сохранить файл участников: $filePath", e)
        }
    }

    companion object {
        fun defaultPath(): Path {
            val home = Path.of(System.getProperty("user.home"))
            return home.resolve(".daily-randomizer").resolve("names.json")
        }
    }
}
