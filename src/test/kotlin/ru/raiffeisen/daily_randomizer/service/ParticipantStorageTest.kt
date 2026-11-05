package ru.raiffeisen.daily_randomizer.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

class ParticipantStorageTest {
    @TempDir lateinit var directory: Path

    @Test
    fun repeatedSaveReplacesFileAndCleansTemporaryFiles() {
        val storage = ParticipantStorage(directory.resolve("names.json"))
        storage.save(listOf(Participant("Alice")))
        storage.save(listOf(Participant("Bob")))
        assertEquals(listOf(Participant("Bob")), storage.load())
        Files.list(directory).use { assertEquals(1L, it.count()) }
    }
}
