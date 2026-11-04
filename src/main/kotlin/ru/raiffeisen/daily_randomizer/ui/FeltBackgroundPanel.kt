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
            BackgroundPattern.NONE -> Unit
            else -> paintThemePattern(g2, w, h)
        }
    }

    private fun paintThemePattern(g2: Graphics2D, w: Int, h: Int) {
        val layer = g2.create() as Graphics2D
        try {
            layer.color = CasinoTheme.gold
            layer.composite = java.awt.AlphaComposite.SrcOver.derive(0.08f)
            when (CasinoTheme.current.backgroundPattern) {
                BackgroundPattern.LOTTO -> {
                    layer.font = java.awt.Font(java.awt.Font.SERIF, java.awt.Font.BOLD, 18)
                    for (y in 24..h step 76) for (x in 24..w step 76) {
                        layer.drawOval(x - 17, y - 17, 34, 34)
                        val number = ((x / 76 + y / 76 * 7) % 90 + 1).toString()
                        layer.drawString(number, x - layer.fontMetrics.stringWidth(number) / 2, y + 6)
                    }
                }
                BackgroundPattern.SCANLINES -> {
                    for (y in 0..h step 5) layer.drawLine(0, y, w, y)
                    layer.composite = java.awt.AlphaComposite.SrcOver.derive(0.18f)
                    layer.font = java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12)
                    layer.drawString("// daily_randomizer — ready", 18, 24)
                }
                BackgroundPattern.NEON_GRID -> {
                    val horizon = h / 3
                    layer.composite = java.awt.AlphaComposite.SrcOver.derive(0.14f)
                    layer.color = CasinoTheme.current.redBright
                    for (y in horizon..h step 32) layer.drawLine(0, y, w, y)
                    layer.color = CasinoTheme.gold
                    for (x in -w..w * 2 step 80) layer.drawLine(w / 2, horizon, x, h)
                }
                BackgroundPattern.COFFEE -> {
                    layer.stroke = BasicStroke(1.5f)
                    for (y in 24..h step 90) for (x in 24..w step 90) {
                        val bean = layer.create() as Graphics2D
                        try {
                            bean.rotate(-0.5, x.toDouble(), y.toDouble())
                            bean.drawOval(x - 6, y - 10, 12, 20)
                            bean.drawArc(x - 4, y - 10, 8, 20, -90, 180)
                        } finally { bean.dispose() }
                    }
                }
                else -> Unit
            }
        } finally { layer.dispose() }
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
