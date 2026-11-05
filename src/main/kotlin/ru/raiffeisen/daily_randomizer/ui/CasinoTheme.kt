package ru.raiffeisen.daily_randomizer.ui

import java.awt.*
import javax.swing.JButton
import kotlin.math.cos
import kotlin.math.sin

/**
 * Тема в стиле казино: зелёное сукно, золото, красная кнопка.
 * Палитра делегирует текущей теме [AppTheme] — переключается на лету.
 */
object CasinoTheme {

    /** Текущая тема приложения. */
    var current: AppTheme = ThemeSettings().load()

    // Сукно игрового стола
    val feltDark: Color get() = current.feltDark
    val felt: Color get() = current.felt
    val feltLight: Color get() = current.feltLight

    // Самый тёмный оттенок сукна — для кнопок (выделяются на фоне feltDark)
    val feltDeepest: Color get() = current.feltDeepest
    val feltDeepestPressed: Color get() = current.feltDeepestPressed

    // Золото
    val gold: Color get() = current.gold
    val goldBright: Color get() = current.goldBright
    val goldDark: Color get() = current.goldDark

    // Красное дерево для кнопки "Крутить"
    val red: Color get() = current.red
    val redBright: Color get() = current.redBright

    // Тёмные панели
    val slotWindowBg: Color get() = current.slotWindowBg
    val textLight: Color get() = current.textLight

    val titleFont get() = Font(current.fontFamily, Font.BOLD, 22)
    val reelFont get() = Font(current.fontFamily, Font.BOLD, 30)
    val winnerFont get() = Font(current.fontFamily, Font.BOLD, 46)
    val buttonFont get() = Font(current.fontFamily, Font.BOLD, 18)
    val smallButtonFont get() = Font(current.fontFamily, Font.BOLD, 12)

    // Оттенок золота в HSB для перелива имени победителя
    val goldHue: Double
        get() {
            val hsb = Color.RGBtoHSB(current.gold.red, current.gold.green, current.gold.blue, null)
            return hsb[0].toDouble()
        }

    /**
     * Стилизует кнопку "Крутить" по теме: круглая казино-фишка или
     * плоский прямоугольник с фирменной рамкой (Райффайзен).
     */
    fun styleSpinButton(button: JButton) {
        if (current.id == AppTheme.FIELD_OF_WONDERS.id) {
            styleDosButton(button, true)
            return
        }
        when (current.chipStyle) {
            ChipStyle.ROUND_CHIP -> styleRoundChipButton(button)
            ChipStyle.FLAT_RECT -> styleFlatRectButton(button)
            ChipStyle.WOOD, ChipStyle.NEON, ChipStyle.SOFT -> styleThemedButton(button)
        }
    }

    private fun styleDosButton(button: JButton, large: Boolean, blue: Boolean = large) {
        button.font = Font(Font.MONOSPACED, Font.BOLD, if (large) 16 else 12)
        button.isOpaque = false
        button.isContentAreaFilled = false
        button.isBorderPainted = false
        button.isFocusPainted = false
        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        button.setUI(object : javax.swing.plaf.basic.BasicButtonUI() {
            override fun paint(g: Graphics, c: javax.swing.JComponent) {
                val layer = g.create() as Graphics2D
                try {
                    val down = button.model.isPressed
                    val w = c.width - 1
                    val h = c.height - 1
                    layer.color = if (blue) {
                        if (down) Color(0x000060) else if (button.model.isRollover) Color(0x2020BB) else Color(0x000090)
                    } else {
                        if (down) Color(0x969696) else if (button.model.isRollover) Color(0xD0D0D0) else Color(0xB8B8B8)
                    }
                    layer.fillRect(0, 0, w, h)
                    layer.color = if (blue) {
                        if (down) Color(0x000040) else Color(0x7777DD)
                    } else if (down) Color(0x505050) else Color.WHITE
                    layer.drawLine(0, 0, w, 0)
                    layer.drawLine(0, 0, 0, h)
                    layer.color = if (blue) {
                        if (down) Color(0x7777DD) else Color(0x000040)
                    } else if (down) Color.WHITE else Color(0x505050)
                    layer.drawLine(0, h, w, h)
                    layer.drawLine(w, 0, w, h)
                    layer.font = button.font
                    layer.color = if (blue) {
                        if (button.isEnabled) Color.WHITE else Color(0xB0B0CC)
                    } else if (button.isEnabled) Color(0x202020) else Color(0x606060)
                    val metrics = layer.fontMetrics
                    val shift = if (down) 1 else 0
                    layer.drawString(
                        button.text, (w - metrics.stringWidth(button.text)) / 2 + shift,
                        (h + metrics.ascent - metrics.descent) / 2 + shift
                    )
                    if (button.hasFocus()) layer.drawRect(4, 4, w - 8, h - 8)
                } finally {
                    layer.dispose()
                }
            }
        })
        button.border = javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14)
        button.preferredSize = if (large) Dimension(240, 60) else null
        button.minimumSize = button.preferredSize
    }

    private fun styleThemedButton(button: JButton) {
        button.font = buttonFont
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isOpaque = false
        button.isBorderPainted = false
        button.preferredSize = Dimension(240, 60)
        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        button.setUI(object : javax.swing.plaf.basic.BasicButtonUI() {
            override fun paint(g: Graphics, c: javax.swing.JComponent) {
                val g2 = g.create() as Graphics2D
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    val model = button.model
                    val w = c.width - 1
                    val h = c.height - 1
                    val style = current.chipStyle
                    val active = model.isPressed || model.isRollover
                    g2.color = if (active) redBright else red
                    if (style == ChipStyle.NEON) {
                        val shape =
                            Polygon(intArrayOf(12, w, w, w - 12, 0, 0), intArrayOf(0, 0, h - 12, h, h, 12), 6)
                        g2.fillPolygon(shape)
                        g2.color = gold
                        g2.stroke = BasicStroke(2f)
                        g2.drawPolygon(shape)
                        g2.color = current.redBright
                        g2.drawLine(16, h - 5, w - 18, h - 5)
                    } else {
                        if (style == ChipStyle.WOOD) {
                            g2.paint = GradientPaint(0f, 0f, redBright, 0f, h.toFloat(), red)
                        }
                        g2.fillRoundRect(0, 0, w, h, 16, 16)
                        g2.paint = null
                        if (style == ChipStyle.WOOD) {
                            g2.color = goldDark
                            g2.drawRoundRect(4, 4, w - 8, h - 8, 12, 12)
                        }
                    }
                    if (button.hasFocus()) {
                        g2.color = textLight
                        g2.drawRoundRect(5, 5, w - 10, h - 10, 10, 10)
                    }
                    g2.font = button.font
                    g2.color = when (style) {
                        ChipStyle.WOOD -> goldBright
                        ChipStyle.NEON -> if (active) current.feltDeepest else gold
                        else -> current.feltDeepest
                    }
                    if (!button.isEnabled) g2.composite = AlphaComposite.SrcOver.derive(0.55f)
                    val metrics = g2.fontMetrics
                    g2.drawString(
                        button.text,
                        (w - metrics.stringWidth(button.text)) / 2,
                        (h + metrics.ascent - metrics.descent) / 2
                    )
                } finally {
                    g2.dispose()
                }
            }
        })
        button.border = null
    }

    /**
     * Круглая красная фишка с золотой окантовкой — стиль казино.
     */
    private fun styleRoundChipButton(button: JButton) {
        button.font = buttonFont
        button.foreground = goldBright
        button.background = red
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isOpaque = false
        // Border ставится ПОСЛЕ setUI: installDefaults у BasicButtonUI
        // устанавливает стандартный border, если текущий null — на Windows
        // из-за этого рисовалась квадратная рамка поверх фишки
        button.preferredSize = Dimension(220, 60)
        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        button.setUI(object : javax.swing.plaf.basic.BasicButtonUI() {
            override fun paint(g: Graphics, c: javax.swing.JComponent) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                val b = c.bounds
                val pressed = (c as JButton).model.isPressed
                // Внешнее золотое кольцо
                g2.color = gold
                g2.fillOval(0, 0, b.width - 1, b.height - 1)
                // Красная фишка
                g2.color = if (pressed) redBright else red
                g2.fillOval(4, 4, b.width - 9, b.height - 9)
                // Декоративные золотые полоски по краю фишки (как у казино-фишки)
                g2.color = gold
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val cx = b.width / 2.0
                    val cy = b.height / 2.0
                    val rx = (b.width / 2.0 - 2) * cos(angle)
                    val ry = (b.height / 2.0 - 2) * sin(angle)
                    val ix = (b.width / 2.0 - 9) * cos(angle)
                    val iy = (b.height / 2.0 - 9) * sin(angle)
                    g2.stroke = BasicStroke(3f)
                    g2.drawLine(
                        (cx + ix).toInt(), (cy + iy).toInt(),
                        (cx + rx).toInt(), (cy + ry).toInt(),
                    )
                }
                // Текст
                g2.font = button.font
                g2.color = goldBright
                val text = button.text
                val metrics = g2.fontMetrics
                val tx = (b.width - metrics.stringWidth(text)) / 2
                val ty = (b.height + metrics.ascent - metrics.descent) / 2
                g2.drawString(text, tx, ty)
            }
        })
        button.border = null
    }

    /**
     * Плоский прямоугольник с толстой жёлтой рамкой — фирменный стиль Райффайзена:
     * строгая графика, при нажатии заливка жёлтым с тёмным текстом.
     */
    private fun styleFlatRectButton(button: JButton) {
        button.font = buttonFont
        button.foreground = gold
        button.background = red
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isOpaque = false
        button.isBorderPainted = false
        button.preferredSize = Dimension(220, 60)
        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        button.setUI(object : javax.swing.plaf.basic.BasicButtonUI() {
            override fun paint(g: Graphics, c: javax.swing.JComponent) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                val b = c.bounds
                val pressed = (c as JButton).model.isPressed
                // Фон: тёмно-серый, при нажатии — фирменный жёлтый
                g2.color = if (pressed) gold else current.red
                g2.fillRect(0, 0, b.width - 1, b.height - 1)
                // Толстая жёлтая рамка
                g2.color = gold
                g2.stroke = BasicStroke(3f)
                g2.drawRect(1, 1, b.width - 3, b.height - 3)
                // Текст: жёлтый на тёмном, тёмный на жёлтом (при нажатии)
                g2.font = button.font
                g2.color = if (pressed) current.feltDark else if (c.model.isRollover) goldBright else gold
                val text = button.text
                val metrics = g2.fontMetrics
                val tx = (b.width - metrics.stringWidth(text)) / 2
                val ty = (b.height + metrics.ascent - metrics.descent) / 2
                g2.drawString(text, tx, ty)
            }
        })
        button.border = null
    }

    /**
     * Стилизует маленькую кнопку (настройки и т.п.): тёмно-зелёная с золотой рамкой,
     * темнее фона, чтобы выделяться. Свои paint — иначе Swing рисует свой фон
     * при нажатии/наведении и затирает цвет.
     */
    fun styleSmallButton(button: JButton, blue: Boolean = false) {
        if (current.id == AppTheme.FIELD_OF_WONDERS.id) {
            styleDosButton(button, false, blue)
            return
        }
        button.font = smallButtonFont
        button.foreground = gold
        button.background = feltDeepest
        button.isFocusPainted = false
        button.isBorderPainted = false
        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        button.isOpaque = false
        button.isContentAreaFilled = false
        button.border = javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(goldDark),
            javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10),
        )
        button.setUI(object : javax.swing.plaf.basic.BasicButtonUI() {
            override fun paint(g: Graphics, c: javax.swing.JComponent) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                val b = c.bounds
                val model = (c as JButton).model
                // Фон: самый тёмный оттенок сукна, при нажатии ещё темнее
                g2.color = if (model.isPressed) feltDeepestPressed else feltDeepest
                g2.fillRect(0, 0, b.width - 1, b.height - 1)
                // Золотая рамка
                g2.color = goldDark
                g2.drawRect(0, 0, b.width - 1, b.height - 1)
                // Текст
                g2.font = button.font
                g2.color = if (model.isRollover) textLight else gold
                val metrics = g2.fontMetrics
                val tx = (b.width - metrics.stringWidth(button.text)) / 2
                val ty = (b.height + metrics.ascent - metrics.descent) / 2
                g2.drawString(button.text, tx, ty)
            }
        })
        // Border ПОСЛЕ setUI: installDefaults устанавливает стандартный border,
        // если текущий null — на Windows из-за этого рисовалась квадратная рамка
        button.border = javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(goldDark),
            javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10),
        )
    }

}
