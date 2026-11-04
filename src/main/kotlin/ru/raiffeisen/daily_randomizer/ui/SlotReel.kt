package ru.raiffeisen.daily_randomizer.ui

import java.awt.Color
import java.awt.Dimension
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent

/**
 * Барабан игрового автомата: вертикальная лента имён, прокручивающаяся сверху вниз.
 * Во время прокрутки лента зациклена (после последнего имени снова идут первые) —
 * интрига сохраняется до полной остановки. При победе показывает имя с переливом
 * и запускает фейерверк.
 */
class SlotReel : JComponent() {

    private var strip: List<String> = emptyList()
    private var offset = 0.0
    private var placeholder = ""
    private var winnerMode = false
    private var hueOffset = 0.0

    private val shimmerTimer = javax.swing.Timer(50) {
        if (winnerMode) {
            hueOffset = (hueOffset + 0.08) % 1.0
            repaint()
        }
    }

    fun setPlaceholder(text: String) {
        placeholder = text
        strip = emptyList()
        offset = 0.0
        winnerMode = false
        shimmerTimer.stop()
        repaint()
    }

    fun setStrip(names: List<String>) {
        require(names.isNotEmpty()) { "Лента имён не может быть пустой" }
        strip = names
        offset = 0.0
        winnerMode = false
        shimmerTimer.stop()
        repaint()
    }

    fun setOffset(value: Double) {
        // Во время прокрутки лента зациклена — смещение не ограничиваем
        offset = value
        repaint()
    }

    /**
     * Показывает победителя: увеличенное переливающееся имя.
     * Салют рисует отдельный полноэкранный оверлей (FireworksOverlay).
     */
    fun showWinner(name: String) {
        strip = listOf(name)
        offset = 0.0
        winnerMode = true
        hueOffset = 0.0
        if (CasinoTheme.current.shimmerWinner) shimmerTimer.start() else shimmerTimer.stop()
        repaint()
    }

    override fun getPreferredSize(): Dimension = Dimension(320, 110)

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

            val w = width
            val h = height

            // Тёмный фон дисплея
            g2.color = CasinoTheme.slotWindowBg
            val style = CasinoTheme.current.reelStyle
            val round = style == ReelStyle.BARREL || style == ReelStyle.CARD || style == ReelStyle.CLEAN
            if (round) {
                val shape = java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, w.toDouble(), h.toDouble(), 24.0, 24.0)
                g2.clip(shape)
            }
            g2.fillRect(0, 0, w, h)
            if (style == ReelStyle.BARREL) {
                g2.color = Color(0xCFAC76)
                for (y in 8 until h step 12) g2.drawLine(0, y, w, y)
            }

            if (winnerMode) {
                paintWinner(g2, w, h)
            } else {
                paintReel(g2, w, h)
            }

            // Золотые линии-маркеры по центру, как в слот-машине
            val cellHeight = winnerFontMetrics(g2).height + 24
            val centerY = h / 2.0
            g2.color = CasinoTheme.gold
            val lineY1 = (centerY - cellHeight / 2.0).toInt()
            val lineY2 = (centerY + cellHeight / 2.0).toInt()
            if (style == ReelStyle.CLASSIC) {
                g2.fillRect(0, lineY1, w, CasinoTheme.current.slotBorderThickness)
                g2.fillRect(0, lineY2 - 2, w, CasinoTheme.current.slotBorderThickness)
            } else {
                g2.color = if (style == ReelStyle.BARREL) CasinoTheme.goldDark else CasinoTheme.gold
                if (round) g2.drawRoundRect(1, 1, w - 3, h - 3, 24, 24)
                else g2.drawRect(1, 1, w - 3, h - 3)
                if (style == ReelStyle.TERMINAL) {
                    g2.font = CasinoTheme.smallButtonFont
                    g2.drawString(if (winnerMode) "> selected" else "> candidates", 12, 19)
                }
                if (style == ReelStyle.NEON) {
                    g2.color = CasinoTheme.current.redBright
                    g2.fillRect(0, h - 4, w / 3, 3)
                    g2.fillRect(w - 30, 0, 30, 3)
                }
            }
        } finally { g2.dispose() }
    }

    private fun paintReel(g2: Graphics2D, w: Int, h: Int) {
        // Затемнение сверху и снизу — эффект цилиндра барабана
        val shade = if (CasinoTheme.current.reelStyle in listOf(ReelStyle.BARREL, ReelStyle.CARD, ReelStyle.CLEAN)) 12 else 170
        g2.paint = GradientPaint(0f, 0f, Color(0, 0, 0, shade), 0f, h * 0.32f, Color(0, 0, 0, 0))
        g2.fillRect(0, 0, w, h)
        g2.paint = GradientPaint(0f, h * 0.68f, Color(0, 0, 0, 0), 0f, h.toFloat(), Color(0, 0, 0, shade))
        g2.fillRect(0, 0, w, h)
        g2.paint = null

        g2.clipRect(0, 0, w, h)

        if (strip.isEmpty()) {
            drawCentered(
                g2, placeholder, w, h / 2, CasinoTheme.goldBright,
                fitFont(g2, placeholder, CasinoTheme.reelFont, w - 2 * TEXT_PADDING),
            )
            return
        }

        g2.font = CasinoTheme.reelFont
        val metrics = g2.fontMetrics
        val cellHeight = metrics.height + 18
        val centerY = h / 2.0
        val size = strip.size

        // Лента зациклена: индекс берётся по модулю размера, интрига сохраняется
        for (i in -2..2) {
            val index = Math.floorMod(offset.toInt() + i, size)
            val cellCenterY = centerY + (offset.toInt() + i - offset) * cellHeight
            val distance = Math.abs(centerY - cellCenterY) / (h / 2.0)
            val alpha = (1.0 - distance).coerceIn(0.0, 1.0)
            g2.composite = java.awt.AlphaComposite.SrcOver.derive(alpha.toFloat())
            drawCentered(g2, strip[index], w, cellCenterY.toInt(), CasinoTheme.goldBright, CasinoTheme.reelFont)
        }
        g2.composite = java.awt.AlphaComposite.SrcOver
    }

    private fun paintWinner(g2: Graphics2D, w: Int, h: Int) {
        // Переливающееся имя: оттенок золота циклически меняется
        val hue = (CasinoTheme.goldHue + hueOffset) % 1.0
        val shimmerColor = if (CasinoTheme.current.shimmerWinner) Color.getHSBColor(hue.toFloat(), 0.65f, 1.0f) else CasinoTheme.goldBright

        val text = strip.firstOrNull() ?: return
        g2.font = fitFont(g2, text, CasinoTheme.winnerFont, w - 2 * TEXT_PADDING)
        val metrics = g2.fontMetrics
        val x = (w - metrics.stringWidth(text)) / 2
        val y = h / 2 + (metrics.ascent - metrics.descent) / 2

        // Лёгкое свечение вокруг имени
        g2.color = Color(shimmerColor.red, shimmerColor.green, shimmerColor.blue, if (CasinoTheme.current.shimmerWinner) 60 else 0)
        g2.drawString(text, x - 2, y)
        g2.drawString(text, x + 2, y)
        g2.drawString(text, x, y - 2)
        g2.drawString(text, x, y + 2)

        g2.color = shimmerColor
        g2.drawString(text, x, y)
    }

    private fun winnerFontMetrics(g2: Graphics2D): java.awt.FontMetrics {
        g2.font = CasinoTheme.winnerFont
        return g2.fontMetrics
    }

    /**
     * Подбирает размер шрифта под ширину [maxWidth]: уменьшает, пока текст не влезет.
     * Минимум — 12pt, чтобы текст не съёживался до нечитаемого.
     */
    private fun fitFont(g2: Graphics2D, text: String, base: java.awt.Font, maxWidth: Int): java.awt.Font {
        if (text.isEmpty()) return base
        var size = base.size
        while (size > MIN_FONT_SIZE && g2.getFontMetrics(base.deriveFont(size.toFloat())).stringWidth(text) > maxWidth) {
            size -= 1
        }
        return base.deriveFont(size.toFloat())
    }

    private fun drawCentered(g2: Graphics2D, text: String, w: Int, centerY: Int, color: Color, font: java.awt.Font) {
        if (text.isEmpty()) return
        g2.font = font
        g2.color = color
        val metrics = g2.fontMetrics
        val x = (w - metrics.stringWidth(text)) / 2
        val y = centerY + (metrics.ascent - metrics.descent) / 2
        g2.drawString(text, x, y)
    }

    companion object {
        private const val MIN_FONT_SIZE = 12
        private const val TEXT_PADDING = 16
    }
}
