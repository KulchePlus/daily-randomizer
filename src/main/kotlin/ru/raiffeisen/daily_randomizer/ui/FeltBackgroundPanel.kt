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
                BackgroundPattern.DOS_BRICKS -> {
                    layer.composite = java.awt.AlphaComposite.SrcOver
                    layer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
                    paintDosStoneWall(layer, w, h)
                }
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

    /** Flat VGA stone faces with stepped outlines, matching the DOS reference. */
    private fun paintDosStoneWall(g: Graphics2D, w: Int, h: Int) {
        val random = kotlin.random.Random(1993)
        val floorY = (h * 0.55).toInt()
        g.color = java.awt.Color(0x0000AA)
        g.fillRect(0, 0, w, floorY)
        var y = -8
        while (y < floorY) {
            val rowHeight = 36
            var x = if ((y + 8) / rowHeight % 2 == 0) -18 else -46
            while (x < w) {
                val stoneWidth = random.nextInt(42, 70)
                val left = x + 4
                val right = x + stoneWidth - 4
                val top = y + 4
                val bottom = minOf(y + rowHeight - 3, floorY - 1)
                val shape = java.awt.Polygon(
                    intArrayOf(left + 4, right - 5, right - 5, right, right - 2, right - 2, left + 5, left + 5, left, left + 2),
                    intArrayOf(top, top + 1, top + 5, top + 5, bottom - 5, bottom, bottom - 1, bottom - 4, bottom - 4, top + 5),
                    10,
                )
                g.color = java.awt.Color(0x000080)
                g.fillPolygon(shape)
                g.stroke = BasicStroke(2f)
                g.color = java.awt.Color(0x5555BB)
                g.drawPolygon(shape)
                // A few flat, dark joints rather than noise across the stone faces.
                g.color = java.awt.Color(0x000040)
                g.drawLine(left + 7, bottom + 1, right - 3, bottom + 1)
                x += stoneWidth
            }
            y += rowHeight
        }
        g.color = java.awt.Color(0xAAAAAA)
        g.fillRect(0, floorY, w, h - floorY)
        g.color = java.awt.Color(0x303030)
        g.fillRect(0, floorY, w, 2)
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
