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
        require(trimmed.isNotEmpty()) { "Имя не может быть пустым" }
        storage.updateActiveParticipants { participants ->
            require(participants.none { it.name.equals(trimmed, ignoreCase = true) }) {
                "Участник уже есть в списке: $trimmed"
            }
            participants + Participant(trimmed)
        }
    }

    fun removeParticipant(name: String) {
        storage.updateActiveParticipants { participants ->
            participants.filterNot { it.name.equals(name, ignoreCase = true) }
        }
    }

    fun setInPool(name: String, inPool: Boolean) {
        storage.updateActiveParticipants { participants ->
            participants.map {
                if (it.name.equals(name, ignoreCase = true)) it.copy(inPool = inPool) else it
            }
        }
    }

    /**
     * Добавляет всех участников в пул.
     */
    fun addAllToPool() {
        setAllInPool(true)
    }

    /**
     * Убирает всех участников из пула.
     */
    fun removeAllFromPool() {
        setAllInPool(false)
    }

    private fun setAllInPool(inPool: Boolean) {
        storage.updateActiveParticipants { participants ->
            participants.map { it.copy(inPool = inPool) }
        }
    }

    /** Убирает участника из пула после проведения дейли. */
    fun removeFromPool(name: String) {
        setInPool(name, false)
    }

    /**
     * Возвращает случайного участника из пула или null, если пул пуст.
     */
    fun pickRandom(pool: List<Participant> = getPool()): Participant? = pool.randomOrNull()
}
