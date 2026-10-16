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
 */
object CasinoTheme {

    // Сукно игрового стола
    val feltDark = Color(0x0B, 0x3D, 0x24)
    val felt = Color(0x0E, 0x51, 0x2F)
    val feltLight = Color(0x14, 0x63, 0x3B)
    // Самый тёмный оттенок сукна — для кнопок (выделяются на фоне feltDark)
    val feltDeepest = Color(0x06, 0x2A, 0x18)
    val feltDeepestPressed = Color(0x04, 0x1F, 0x11)

    // Золото
    val gold = Color(0xD4, 0xAF, 0x37)
    val goldBright = Color(0xF5, 0xD7, 0x63)
    val goldDark = Color(0x9A, 0x7B, 0x1E)

    // Красное дерево для кнопки "Крутить"
    val red = Color(0xB3, 0x1B, 0x1B)
    val redBright = Color(0xD9, 0x2B, 0x2B)

    // Тёмные панели
    val slotWindowBg = Color(0x12, 0x12, 0x12)
    val textLight = Color(0xF5, 0xF0, 0xE1)

    val titleFont = Font(Font.SERIF, Font.BOLD, 22)
    val nameFont = Font(Font.SANS_SERIF, Font.BOLD, 40)
    val reelFont = Font(Font.SANS_SERIF, Font.BOLD, 30)
    val winnerFont = Font(Font.SANS_SERIF, Font.BOLD, 46)
    val buttonFont = Font(Font.SANS_SERIF, Font.BOLD, 18)
    val smallButtonFont = Font(Font.SANS_SERIF, Font.BOLD, 12)

    // Оттенок золота в HSB для перелива имени победителя
    const val goldHue = 0.13

    /**
     * Стилизует кнопку "Крутить" как красную фишку с золотой окантовкой.
     */
    fun styleSpinButton(button: JButton) {
        button.font = buttonFont
        button.foreground = goldBright
        button.background = red
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isOpaque = false
        button.border = null
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
    }

    /**
     * Стилизует окошко с именем как дисплей игрового автомата.
     */
    fun styleSlotWindow(label: JLabel) {
        label.font = nameFont
        label.foreground = goldBright
        label.background = slotWindowBg
        label.isOpaque = true
        label.horizontalAlignment = SwingConstants.CENTER
        label.border = javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(gold, 3),
            javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24),
        )
    }
}
