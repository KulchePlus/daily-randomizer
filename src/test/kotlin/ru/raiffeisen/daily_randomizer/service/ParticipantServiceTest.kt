package ru.raiffeisen.daily_randomizer.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParticipantServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private fun createService(): ParticipantService =
        ParticipantService(ParticipantStorage(tempDir.resolve("names.json")))

    @Test
    fun `empty storage returns empty list`() {
        assertTrue(createService().getParticipants().isEmpty())
    }

    @Test
    fun `addParticipant adds trimmed name in pool by default`() {
        val service = createService()
        service.addParticipant("  Алиса  ")
        assertEquals(listOf(Participant("Алиса", inPool = true)), service.getParticipants())
    }

    @Test
    fun `addParticipant rejects blank name`() {
        val service = createService()
        assertThrows<IllegalArgumentException> { service.addParticipant("   ") }
    }

    @Test
    fun `addParticipant rejects duplicate ignoring case`() {
        val service = createService()
        service.addParticipant("Алиса")
        assertThrows<IllegalArgumentException> { service.addParticipant("алиса") }
        assertEquals(1, service.getParticipants().size)
    }

    @Test
    fun `removeParticipant removes name ignoring case`() {
        val service = createService()
        service.addParticipant("Алиса")
        service.addParticipant("Боб")
        service.removeParticipant("алиса")
        assertEquals(listOf("Боб"), service.getParticipants().map { it.name })
    }

    @Test
    fun `setInPool toggles pool membership`() {
        val service = createService()
        service.addParticipant("Алиса")
        service.setInPool("Алиса", false)
        assertFalse(service.getParticipants().first().inPool)
        assertTrue(service.getPool().isEmpty())
        service.setInPool("алиса", true)
        assertEquals(1, service.getPool().size)
    }

    @Test
    fun `addAllToPool and removeAllFromPool affect everyone`() {
        val service = createService()
        service.addParticipant("Алиса")
        service.addParticipant("Боб")
        service.removeAllFromPool()
        assertTrue(service.getPool().isEmpty())
        service.addAllToPool()
        assertEquals(2, service.getPool().size)
    }

    @Test
    fun `removeFromPool removes only the winner`() {
        val service = createService()
        service.addParticipant("Алиса")
        service.addParticipant("Боб")
        service.removeFromPool("Алиса")
        assertEquals(listOf("Боб"), service.getPool().map { it.name })
        assertEquals(2, service.getParticipants().size)
    }

    @Test
    fun `pickRandom returns null for empty pool`() {
        assertNull(createService().pickRandom())
    }

    @Test
    fun `pickRandom returns only pool members`() {
        val service = createService()
        service.addParticipant("Алиса")
        service.addParticipant("Боб")
        service.setInPool("Боб", false)
        repeat(10) {
            assertEquals("Алиса", service.pickRandom()?.name)
        }
    }
}
