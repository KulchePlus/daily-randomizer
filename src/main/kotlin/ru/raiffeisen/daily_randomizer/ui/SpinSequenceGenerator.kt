package ru.raiffeisen.daily_randomizer.ui

import kotlin.random.Random

/**
 * Генерирует последовательность "прокрутки" имён: сначала быстро, потом замедление как в казино.
 * Последний элемент последовательности — выпавший участник.
 */
class SpinSequenceGenerator(private val random: Random = Random.Default) {

    /**
     * @param participants непустой список участников
     * @param steps количество смен имён во время прокрутки
     * @return последовательность показанных имён; последний элемент — результат
     */
    fun generate(participants: List<String>, steps: Int = DEFAULT_STEPS): List<String> {
        require(participants.isNotEmpty()) { "Список участников не может быть пустым" }
        require(steps > 0) { "Количество шагов должно быть положительным" }

        val winnerIndex = random.nextInt(participants.size)
        val sequence = List(steps - 1) { participants[random.nextInt(participants.size)] }
        return sequence + participants[winnerIndex]
    }

    companion object {
        const val DEFAULT_STEPS = 30
    }
}
