package ru.raiffeisen.daily_randomizer.ui

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Path2D
import javax.swing.JComponent

/** Официальный знак Райффайзен Банка, масштабируемый без искажения пропорций. */
class RaifLogoComponent : JComponent() {

    override fun getPreferredSize(): Dimension = Dimension(72, 72)

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val size = minOf(width, height).toDouble()
        if (size <= 0.0) return

        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.translate((width - size) / 2.0, (height - size) / 2.0)
            g2.scale(size / 32.0, size / 32.0)
            g2.color = Color(0xFE, 0xE6, 0x00)
            g2.fill(BACKGROUND)
            g2.color = Color(0x2B, 0x2D, 0x33)
            g2.fill(EMBLEM)
        } finally {
            g2.dispose()
        }
    }

    companion object {

        // Исходные контуры и цвета: https://www.raiffeisen.ru/fav/icon.svg
        // Координаты SVG (viewBox 0 0 32 32) сохранены без изменений.
        private val BACKGROUND = Path2D.Double().apply {
            moveTo(32.0, 25.952)
            curveTo(32.0, 29.84, 29.84, 32.0, 25.952, 32.0)
            lineTo(6.048, 32.0)
            curveTo(2.16, 32.0, 0.0, 29.84, 0.0, 25.952)
            lineTo(0.0, 6.048)
            curveTo(0.0, 2.16, 2.16, 0.0, 6.048, 0.0)
            lineTo(25.96, 0.0)
            curveTo(29.84, 0.0, 32.0, 2.16, 32.0, 6.048)
            lineTo(32.0, 25.952)
            closePath()
        }

        private val EMBLEM = Path2D.Double().apply {
            moveTo(20.575, 14.4398)
            lineTo(20.575, 11.3838)
            lineTo(21.679, 10.2798)
            lineTo(21.679, 12.9278)
            lineTo(22.639, 13.8878)
            lineTo(26.503, 10.0238)
            lineTo(27.455, 10.9758)
            curveTo(27.447, 10.2558, 27.303, 8.16783, 25.535, 6.43183)
            curveTo(23.631, 4.59183, 21.567, 5.42383, 20.863, 6.12783)
            lineTo(17.007, 9.98383)
            lineTo(17.991, 10.9678)
            lineTo(15.999, 12.9758)
            lineTo(14.007, 10.9838)
            lineTo(14.991, 9.99983)
            lineTo(11.135, 6.14383)
            curveTo(10.431, 5.43983, 8.36697, 4.60783, 6.46297, 6.44783)
            curveTo(4.69497, 8.18383, 4.55097, 10.2718, 4.54297, 10.9918)
            lineTo(5.49497, 10.0398)
            lineTo(9.35897, 13.9038)
            lineTo(10.319, 12.9438)
            lineTo(10.319, 10.2958)
            lineTo(11.423, 11.3998)
            lineTo(11.423, 14.4558)
            lineTo(12.975, 15.9998)
            lineTo(5.32697, 23.6478)
            lineTo(8.35097, 26.6638)
            lineTo(15.999, 19.0238)
            lineTo(23.647, 26.6718)
            lineTo(26.671, 23.6558)
            lineTo(19.023, 15.9998)
            lineTo(20.575, 14.4398)
            closePath()
        }
    }
}
