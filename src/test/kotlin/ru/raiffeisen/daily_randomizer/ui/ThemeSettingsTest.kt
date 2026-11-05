package ru.raiffeisen.daily_randomizer.ui

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

class ThemeSettingsTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `selection survives a new settings instance`() {
        val file = directory.resolve("settings/appearance.properties")
        ThemeSettings(file).save(AppTheme.TERMINAL)
        assertEquals(AppTheme.TERMINAL, ThemeSettings(file).load())
    }

    @Test
    fun `missing or unknown selection uses default`() {
        val file = directory.resolve("appearance.properties")
        assertEquals(AppTheme.CASINO, ThemeSettings(file).load())
        Files.writeString(file, "theme=unknown")
        assertEquals(AppTheme.CASINO, ThemeSettings(file).load())
    }
}
