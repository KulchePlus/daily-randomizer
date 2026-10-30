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

    val titleFont = Font(Font.SERIF, Font.BOLD, 22)
    val nameFont = Font(Font.SANS_SERIF, Font.BOLD, 40)
    val reelFont = Font(Font.SANS_SERIF, Font.BOLD, 30)
    val winnerFont = Font(Font.SANS_SERIF, Font.BOLD, 46)
    val buttonFont = Font(Font.SANS_SERIF, Font.BOLD, 18)
    val smallButtonFont = Font(Font.SANS_SERIF, Font.BOLD, 12)

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
        }
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
                g2.color = if (model.isRollover) goldBright else gold
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
