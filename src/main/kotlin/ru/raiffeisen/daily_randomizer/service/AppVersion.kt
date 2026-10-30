package ru.raiffeisen.daily_randomizer.service

import java.util.Properties

object AppVersion {
    val current: String = Properties().apply {
        AppVersion.javaClass.getResourceAsStream("/app-version.properties")?.use { load(it) }
    }.getProperty("version", "неизвестна")
}
