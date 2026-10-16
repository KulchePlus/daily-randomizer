package ru.raiffeisen.daily_randomizer.ui

import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel

/**
 * Панель с фоном "зелёное сукно казино": радиальный градиент с виньеткой.
 */
class FeltBackgroundPanel(layout: java.awt.LayoutManager?) : JPanel(layout) {

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val w = width
        val h = height

        // Базовый градиент от центра к краям (эффект освещения над столом)
        val gradient = GradientPaint(
            0f, 0f, CasinoTheme.feltLight,
            w * 0.8f, h * 1.0f, CasinoTheme.feltDark,
        )
        g2.paint = gradient
        g2.fillRect(0, 0, w, h)

        // Виньетка по краям
        g2.paint = null
        val vignette = g2.composite
        g2.color = java.awt.Color(0, 0, 0, 60)
        for (i in 0 until 20) {
            g2.drawOval(i * 8 - w / 4, i * 8 - h / 4, w + w / 2 - i * 16, h + h / 2 - i * 16)
        }
        g2.composite = vignette
    }
}
