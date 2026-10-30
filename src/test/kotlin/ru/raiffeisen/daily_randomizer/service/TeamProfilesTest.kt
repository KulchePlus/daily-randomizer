package ru.raiffeisen.daily_randomizer.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TeamProfilesTest {
    @TempDir
    lateinit var tempDir: Path

    private fun service() = ParticipantService(ParticipantStorage(tempDir.resolve("names.json")))

    @Test
    fun `teams keep independent participants and pool after restart`() {
        val service = service()
        val firstId = service.getActiveProfile().id
        service.addParticipant("Алиса")
        service.removeAllFromPool()
        service.createProfile("  Вторая команда  ")
        val secondId = service.getActiveProfile().id
        assertTrue(service.getParticipants().isEmpty())
        service.addParticipant("Алиса")
        service.addParticipant("Боб")
        service.removeFromPool("Боб")

        val restarted = service()
        assertEquals(secondId, restarted.getActiveProfile().id)
        assertEquals("Вторая команда", restarted.getActiveProfile().name)
        assertEquals(listOf("Алиса"), restarted.getPool().map { it.name })
        restarted.switchProfile(firstId)
        assertEquals(listOf(Participant("Алиса", false)), restarted.getParticipants())
        restarted.addAllToPool()
        restarted.removeParticipant("Алиса")
        restarted.switchProfile(secondId)
        assertEquals(listOf(Participant("Алиса"), Participant("Боб", false)), restarted.getParticipants())
    }

    @Test
    fun `legacy participants survive migration and subsequent changes`() {
        val file = tempDir.resolve("names.json")
        Files.writeString(file, """[{"name":"Алиса","inPool":false},{"name":"Боб","inPool":true}]""")
        val service = service()
        val originalId = service.getActiveProfile().id
        assertEquals(originalId, service.getActiveProfile().id)
        service.createProfile("Новая")
        service.switchProfile(originalId)
        assertEquals(listOf(Participant("Алиса", false), Participant("Боб")), service().getParticipants())
    }

    @Test
    fun `rename and delete preserve other teams and valid active profile`() {
        val service = service()
        val firstId = service.getActiveProfile().id
        service.addParticipant("Алиса")
        service.createProfile("Вторая")
        val secondId = service.getActiveProfile().id
        service.renameProfile(secondId, "  Новое имя  ")
        assertEquals(secondId, service.getActiveProfile().id)
        assertEquals("Новое имя", service().getActiveProfile().name)
        service.deleteProfile(secondId)
        assertEquals(firstId, service().getActiveProfile().id)
        assertEquals(listOf(Participant("Алиса")), service.getParticipants())
        assertThrows<IllegalArgumentException> { service.deleteProfile(firstId) }
    }

    @Test
    fun `invalid profile operations leave saved data unchanged`() {
        val service = service()
        service.createProfile("Команда")
        val before = service.getProfiles()
        val activeId = service.getActiveProfile().id
        assertThrows<IllegalArgumentException> { service.createProfile("   ") }
        assertThrows<IllegalArgumentException> { service.createProfile(" команда ") }
        assertThrows<IllegalArgumentException> { service.renameProfile(activeId, " ") }
        assertThrows<IllegalArgumentException> { service.renameProfile(activeId, "основная команда") }
        assertThrows<IllegalArgumentException> { service.switchProfile("missing") }
        assertThrows<IllegalArgumentException> { service.deleteProfile("missing") }
        assertEquals(before, service().getProfiles())
        assertEquals(activeId, service().getActiveProfile().id)
    }

    @Test
    fun `deleting inactive team keeps current selection`() {
        val service = service()
        val firstId = service.getActiveProfile().id
        service.createProfile("Вторая")
        val secondId = service.getActiveProfile().id
        service.deleteProfile(firstId)
        assertEquals(secondId, service().getActiveProfile().id)
    }
}
