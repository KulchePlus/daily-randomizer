package ru.raiffeisen.daily_randomizer.ui

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpinSequenceGeneratorTest {

    @Test
    fun selectedWinnerEndsSequence() {
        val result = SpinSequenceGenerator().generate(listOf("Alice", "Bob"), winner = "Bob")
        assertEquals("Bob", result.last())
    }

    private val participants = listOf("Алиса", "Боб", "Вера")
    private val generator = SpinSequenceGenerator()

    @Test
    fun `sequence has requested length`() {
        val sequence = generator.generate(participants, steps = 10)
        assertEquals(10, sequence.size)
    }

    @Test
    fun `last element is one of participants`() {
        val sequence = generator.generate(participants, steps = 10)
        assertTrue(participants.contains(sequence.last()))
    }

    @Test
    fun `all intermediate elements are participants`() {
        val sequence = generator.generate(participants, steps = 30)
        assertTrue(sequence.all { participants.contains(it) })
    }

    @Test
    fun `rejects empty participants`() {
        assertThrows<IllegalArgumentException> { generator.generate(emptyList()) }
    }

    @Test
    fun `rejects non-positive steps`() {
        assertThrows<IllegalArgumentException> { generator.generate(participants, steps = 0) }
    }

    @Test
    fun `default steps value is used when not specified`() {
        val sequence = generator.generate(participants)
        assertEquals(SpinSequenceGenerator.DEFAULT_STEPS, sequence.size)
    }
}
