package ru.raiffeisen.daily_randomizer.ui

import java.awt.Component
import java.awt.Container
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.Timer
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SubscriptionPanelTest {
    @Test
    fun `blank name does not start generation`() = SwingUtilities.invokeAndWait {
        val panel = SubscriptionPanel()
        val components = descendants(panel)
        components.filterIsInstance<JButton>().single().doClick(0)
        assertTrue(components.filterIsInstance<JButton>().single().isEnabled)
        assertTrue(components.filterIsInstance<JTextField>().single().isEditable)
        assertTrue(components.filterIsInstance<JLabel>().any { it.text == "Сначала введите имя" })
        assertFalse(components.filterIsInstance<JLabel>().single { it.icon != null }.isVisible)
        panel.stopGeneration()
    }

    @Test
    fun `QR appears after simulated generation and can be generated again`() {
        lateinit var panel: SubscriptionPanel
        lateinit var button: JButton
        lateinit var qr: JLabel
        var startedAt = 0L
        val completed = CountDownLatch(1)
        lateinit var observer: Timer
        SwingUtilities.invokeAndWait {
            panel = SubscriptionPanel()
            val components = descendants(panel)
            button = components.filterIsInstance<JButton>().single()
            qr = components.filterIsInstance<JLabel>().single { it.icon != null }
            components.filterIsInstance<JTextField>().single().text = "Тестовый участник"
            startedAt = System.nanoTime()
            button.doClick(0)
            assertFalse(button.isEnabled)
            assertFalse(qr.isVisible)
            observer = Timer(20) {
                if (button.isEnabled && qr.isVisible) completed.countDown()
            }
            observer.start()
        }
        try {
            assertTrue(completed.await(5, TimeUnit.SECONDS), "QR did not appear")
            assertTrue(System.nanoTime() - startedAt >= SubscriptionPanel.GENERATION_DELAY_MS * 1_000_000L)
            SwingUtilities.invokeAndWait {
                assertTrue(qr.isVisible)
                button.doClick(0)
                assertFalse(qr.isVisible)
                assertFalse(button.isEnabled)
                panel.stopGeneration()
                assertTrue(button.isEnabled)
                assertFalse(qr.isVisible)
            }
        } finally {
            SwingUtilities.invokeAndWait {
                observer.stop()
                panel.stopGeneration()
            }
        }
    }

    private fun descendants(container: Container): List<Component> = container.components.flatMap {
        listOf(it) + if (it is Container) descendants(it) else emptyList()
    }
}
