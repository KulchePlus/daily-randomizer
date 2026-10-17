package ru.raiffeisen.daily_randomizer.ui

import java.awt.BasicStroke
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel

/**
 * Панель с фоном игрового стола: радиальный градиент + узор по теме
 * ([BackgroundPattern.VIGNETTE] — казино, [BackgroundPattern.DIAGONAL_STRIPES] — Райффайзен).
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
        g2.paint = null

        when (CasinoTheme.current.backgroundPattern) {
            BackgroundPattern.VIGNETTE -> paintVignette(g2, w, h)
            BackgroundPattern.DIAGONAL_STRIPES -> paintDiagonalStripes(g2, w, h)
        }
    }

    /** Виньетка по краям — стиль казино. */
    private fun paintVignette(g2: Graphics2D, w: Int, h: Int) {
        val composite = g2.composite
        g2.color = java.awt.Color(0, 0, 0, 60)
        for (i in 0 until 20) {
            g2.drawOval(i * 8 - w / 4, i * 8 - h / 4, w + w / 2 - i * 16, h + h / 2 - i * 16)
        }
        g2.composite = composite
    }

    /**
     * Диагональные линии под 45° — фирменная графика Райффайзена.
     * Тонкие полупрозрачные жёлтые штрихи, не мешают контенту.
     */
    private fun paintDiagonalStripes(g2: Graphics2D, w: Int, h: Int) {
        val composite = g2.composite
        g2.composite = java.awt.AlphaComposite.SrcOver.derive(0.07f)
        g2.color = CasinoTheme.gold
        g2.stroke = BasicStroke(2f)
        // Шаг 56 px, линии идут из левого нижнего угла в правый верхний
        val step = 56
        val max = w + h
        var d = -h
        while (d < max) {
            g2.drawLine(d, h, d + h, 0)
            d += step
        }
        g2.composite = composite
    }
}
