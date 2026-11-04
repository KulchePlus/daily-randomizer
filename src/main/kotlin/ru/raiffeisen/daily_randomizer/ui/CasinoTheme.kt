package ru.raiffeisen.daily_randomizer.ui

import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.SwingConstants

/**
 * Тема в стиле казино: зелёное сукно, золото, красная кнопка.
 * Палитра делегирует текущей теме [AppTheme] — переключается на лету.
 */
object CasinoTheme {

    /** Текущая тема приложения. */
    var current: AppTheme = AppTheme.CASINO

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
    val nameFont get() = Font(current.fontFamily, Font.BOLD, 40)
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
        when (current.chipStyle) {
            ChipStyle.ROUND_CHIP -> styleRoundChipButton(button)
            ChipStyle.FLAT_RECT -> styleFlatRectButton(button)
            ChipStyle.WOOD, ChipStyle.NEON, ChipStyle.SOFT -> styleThemedButton(button)
        }
    }

    private fun styleThemedButton(button: JButton) {
        button.font = buttonFont
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isOpaque = false
        button.isBorderPainted = false
        button.preferredSize = java.awt.Dimension(240, 60)
        button.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
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
                        val shape = java.awt.Polygon(intArrayOf(12, w, w, w - 12, 0, 0), intArrayOf(0, 0, h - 12, h, h, 12), 6)
                        g2.fillPolygon(shape)
                        g2.color = gold
                        g2.stroke = java.awt.BasicStroke(2f)
                        g2.drawPolygon(shape)
                        g2.color = current.redBright
                        g2.drawLine(16, h - 5, w - 18, h - 5)
                    } else {
                        if (style == ChipStyle.WOOD) {
                            g2.paint = java.awt.GradientPaint(0f, 0f, redBright, 0f, h.toFloat(), red)
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
                    if (!button.isEnabled) g2.composite = java.awt.AlphaComposite.SrcOver.derive(0.55f)
                    val metrics = g2.fontMetrics
                    g2.drawString(button.text, (w - metrics.stringWidth(button.text)) / 2, (h + metrics.ascent - metrics.descent) / 2)
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
        button.preferredSize = java.awt.Dimension(220, 60)
        button.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
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
                    val rx = (b.width / 2.0 - 2) * Math.cos(angle)
                    val ry = (b.height / 2.0 - 2) * Math.sin(angle)
                    val ix = (b.width / 2.0 - 9) * Math.cos(angle)
                    val iy = (b.height / 2.0 - 9) * Math.sin(angle)
                    g2.stroke = java.awt.BasicStroke(3f)
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
        button.preferredSize = java.awt.Dimension(220, 60)
        button.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
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
                g2.stroke = java.awt.BasicStroke(3f)
                g2.drawRect(1, 1, b.width - 3, b.height - 3)
                // Текст: жёлтый на тёмном, тёмный на жёлтом (при нажатии)
                g2.font = button.font
                g2.color = if (pressed) current.feltDark else if ((c as JButton).model.isRollover) goldBright else gold
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
    fun styleSmallButton(button: JButton) {
        button.font = smallButtonFont
        button.foreground = gold
        button.background = feltDeepest
        button.isFocusPainted = false
        button.isBorderPainted = false
        button.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
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

    /**
     * Стилизует окошко с именем как дисплей игрового автомата.
     * Толщина рамки — по теме: 3px (казино) или 1px (Райффайзен).
     */
    fun styleSlotWindow(label: JLabel) {
        label.font = nameFont
        label.foreground = goldBright
        label.background = slotWindowBg
        label.isOpaque = true
        label.horizontalAlignment = SwingConstants.CENTER
        label.border = javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(gold, current.slotBorderThickness),
            javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24),
        )
    }
}
