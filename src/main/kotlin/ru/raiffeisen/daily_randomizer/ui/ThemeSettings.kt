package ru.raiffeisen.daily_randomizer.ui

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** Stores the user's selection separately from temporary preview themes. */
internal class ThemeSettings(
    private val file: Path = Path.of(System.getProperty("user.home"), ".daily-randomizer", "appearance.properties"),
) {
    fun load(): AppTheme = runCatching {
        val properties = Properties()
        if (Files.exists(file)) Files.newInputStream(file).use { properties.load(it) }
        AppTheme.ALL.firstOrNull { it.id == properties.getProperty("theme") } ?: AppTheme.CASINO
    }.getOrDefault(AppTheme.CASINO)

    fun save(theme: AppTheme) {
        Files.createDirectories(file.parent)
        val properties = Properties().apply { setProperty("theme", theme.id) }
        Files.newOutputStream(file).use { properties.store(it, null) }
    }
}
