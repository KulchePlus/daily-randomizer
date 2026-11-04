package ru.raiffeisen.daily_randomizer.ui

import java.awt.Color
import javax.swing.SwingUtilities
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppThemeTest {
    @Test
    fun `theme previews leave the active theme unchanged`() {
        SwingUtilities.invokeAndWait {
            val previous = CasinoTheme.current
            try {
                CasinoTheme.current = AppTheme.COFFEE_HOUSE
                AppTheme.ALL.forEach { theme ->
                    val preview = ThemePreview.create(theme)
                    assertEquals(192, preview.iconWidth, theme.id)
                    assertEquals(136, preview.iconHeight, theme.id)
                    assertEquals(AppTheme.COFFEE_HOUSE, CasinoTheme.current)
                }
            } finally { CasinoTheme.current = previous }
        }
    }

    @Test
    fun `all theme text remains readable on its surface`() {
        AppTheme.ALL.forEach { theme ->
            assertTrue(contrast(theme.goldBright, theme.slotWindowBg) >= 4.5, "${theme.id}: reel")
            assertTrue(contrast(theme.textLight, theme.feltDark) >= 4.5, "${theme.id}: settings")
            assertTrue(contrast(theme.textLight, theme.felt) >= 4.5, "${theme.id}: selected row")
            assertTrue(contrast(theme.gold, theme.feltDeepest) >= 4.5, "${theme.id}: small button")
        }
    }

    private fun contrast(a: Color, b: Color): Double {
        fun luminance(color: Color): Double {
            fun channel(value: Int): Double {
                val normalized = value / 255.0
                return if (normalized <= 0.04045) normalized / 12.92 else ((normalized + 0.055) / 1.055).pow(2.4)
            }
            return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
        }
        val x = luminance(a)
        val y = luminance(b)
        return (maxOf(x, y) + 0.05) / (minOf(x, y) + 0.05)
    }
}
