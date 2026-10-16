package ru.raiffeisen.daily_randomizer

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import ru.raiffeisen.daily_randomizer.service.ParticipantService
import ru.raiffeisen.daily_randomizer.service.ParticipantStorage
import ru.raiffeisen.daily_randomizer.ui.MainFrame
import javax.swing.SwingUtilities

@SpringBootApplication
class DailyRandomizerApplication

fun main(args: Array<String>) {
    // Spring Boot по умолчанию выставляет java.awt.headless=true (configureHeadlessProperty),
    // что ломает создание Swing-окон. Явно отключаем.
    val app = SpringApplication(DailyRandomizerApplication::class.java)
    app.setHeadless(false)
    app.run(*args)

    SwingUtilities.invokeLater {
        MainFrame(ParticipantService(ParticipantStorage())).isVisible = true
    }
}
