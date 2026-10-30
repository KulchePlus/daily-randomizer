package ru.raiffeisen.daily_randomizer.service

import java.util.UUID

data class TeamProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val participants: List<Participant> = emptyList(),
) {
    override fun toString(): String = name
}

data class ParticipantProfiles(
    val profiles: List<TeamProfile> = listOf(TeamProfile(id = "default", name = "Основная команда")),
    val activeProfileId: String = profiles.first().id,
) {
    fun activeProfile(): TeamProfile = profiles.first { it.id == activeProfileId }
}
