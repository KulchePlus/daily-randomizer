package ru.raiffeisen.daily_randomizer.service

/**
 * Участник: имя и флаг, находится ли он в пуле для выбора ведущего сегодня.
 */
data class Participant(
    val name: String,
    val inPool: Boolean = true,
)
