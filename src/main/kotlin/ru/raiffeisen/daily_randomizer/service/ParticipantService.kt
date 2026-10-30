package ru.raiffeisen.daily_randomizer.service

/**
 * Бизнес-логика работы со списком участников: добавление, удаление,
 * управление пулом (кто участвует в выборе сегодня), случайный выбор.
 */
class ParticipantService(private val storage: ParticipantStorage) {

    fun getProfiles(): List<TeamProfile> = storage.loadProfiles().profiles

    fun getActiveProfile(): TeamProfile = storage.loadProfiles().activeProfile()

    fun createProfile(name: String) {
        val state = storage.loadProfiles()
        val profile = TeamProfile(name = validateProfileName(name, state))
        storage.saveProfiles(state.copy(profiles = state.profiles + profile, activeProfileId = profile.id))
    }

    fun switchProfile(id: String) {
        val state = storage.loadProfiles()
        require(state.profiles.any { it.id == id }) { "Команда не найдена" }
        storage.saveProfiles(state.copy(activeProfileId = id))
    }

    fun renameProfile(id: String, name: String) {
        val state = storage.loadProfiles()
        require(state.profiles.any { it.id == id }) { "Команда не найдена" }
        val trimmed = validateProfileName(name, state, id)
        storage.saveProfiles(state.copy(profiles = state.profiles.map {
            if (it.id == id) it.copy(name = trimmed) else it
        }))
    }

    fun deleteProfile(id: String) {
        val state = storage.loadProfiles()
        require(state.profiles.any { it.id == id }) { "Команда не найдена" }
        require(state.profiles.size > 1) { "Нельзя удалить единственную команду" }
        val remaining = state.profiles.filterNot { it.id == id }
        storage.saveProfiles(state.copy(
            profiles = remaining,
            activeProfileId = if (state.activeProfileId == id) remaining.first().id else state.activeProfileId,
        ))
    }

    private fun validateProfileName(name: String, state: ParticipantProfiles, excludedId: String? = null): String {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Название команды не может быть пустым" }
        require(state.profiles.none { it.id != excludedId && it.name.equals(trimmed, ignoreCase = true) }) {
            "Команда \"$trimmed\" уже существует"
        }
        return trimmed
    }

    fun getParticipants(): List<Participant> = storage.load()

    fun getPool(): List<Participant> = storage.load().filter { it.inPool }

    /**
     * Добавляет участника. Бросает [IllegalArgumentException] при пустом имени или дубликате.
     */
    fun addParticipant(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("Имя не может быть пустым")
        }
        val participants = storage.load()
        if (participants.any { it.name.equals(trimmed, ignoreCase = true) }) {
            throw IllegalArgumentException("Участник \"$trimmed\" уже есть в списке")
        }
        storage.save(participants + Participant(trimmed))
    }

    fun removeParticipant(name: String) {
        val updated = storage.load().filterNot { it.name.equals(name, ignoreCase = true) }
        storage.save(updated)
    }

    /**
     * Обновляет флаг участия в пуле для участника с именем [name].
     */
    fun setInPool(name: String, inPool: Boolean) {
        val updated = storage.load().map {
            if (it.name.equals(name, ignoreCase = true)) it.copy(inPool = inPool) else it
        }
        storage.save(updated)
    }

    /**
     * Добавляет всех участников в пул.
     */
    fun addAllToPool() {
        storage.save(storage.load().map { it.copy(inPool = true) })
    }

    /**
     * Убирает всех участников из пула.
     */
    fun removeAllFromPool() {
        storage.save(storage.load().map { it.copy(inPool = false) })
    }

    /**
     * Убирает участника из пула (после проведения дейли или перекрутки).
     */
    fun removeFromPool(name: String) {
        setInPool(name, false)
    }

    /**
     * Возвращает случайного участника из пула или null, если пул пуст.
     */
    fun pickRandom(): Participant? = getPool().randomOrNull()
}
