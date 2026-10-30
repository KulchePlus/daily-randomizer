package ru.raiffeisen.daily_randomizer.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.nio.file.Files
import java.nio.file.Path

/**
 * Хранит профили команд в JSON-файле. По умолчанию — ~/.daily-randomizer/names.json.
 */
class ParticipantStorage(private val filePath: Path = defaultPath()) {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun load(): List<Participant> = loadProfiles().activeProfile().participants

    fun save(participants: List<Participant>) {
        val state = loadProfiles()
        saveProfiles(state.copy(profiles = state.profiles.map {
            if (it.id == state.activeProfileId) it.copy(participants = participants) else it
        }))
    }

    fun loadProfiles(): ParticipantProfiles {
        if (!Files.exists(filePath)) return ParticipantProfiles()
        return try {
            val content = Files.readString(filePath)
            // Старый формат — массив участников; сохраняем его как первую команду.
            val state = if (objectMapper.readTree(content).isArray) {
                ParticipantProfiles(profiles = listOf(TeamProfile(
                    id = "default", name = "Основная команда",
                    participants = objectMapper.readValue<List<Participant>>(content),
                )))
            } else {
                objectMapper.readValue<ParticipantProfiles>(content)
            }
            require(state.profiles.isNotEmpty()) { "Нет профилей команд" }
            require(state.profiles.map { it.id }.distinct().size == state.profiles.size) { "Повторяющиеся профили" }
            require(state.profiles.any { it.id == state.activeProfileId }) { "Не найден активный профиль" }
            state
        } catch (e: Exception) {
            throw IllegalStateException("Не удалось прочитать файл участников: $filePath", e)
        }
    }

    fun saveProfiles(state: ParticipantProfiles) {
        try {
            Files.createDirectories(filePath.toAbsolutePath().parent)
            Files.writeString(filePath, objectMapper.writeValueAsString(state))
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
