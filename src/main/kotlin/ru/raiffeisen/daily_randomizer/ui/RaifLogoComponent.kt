package ru.raiffeisen.daily_randomizer.ui

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent

/**
 * Логотип в стиле Raiffeisen: упрощённый Giebelkreuz — два скрещённых
 * фронтона (крыши) с конскими головами на концах, фирменный жёлтый.
 * Рисуется программно: чистые линии, без растровых файлов.
 */
class RaifLogoComponent : JComponent() {

    override fun getPreferredSize(): Dimension = Dimension(72, 72)

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

        val w = width
        val h = height
        val stroke = BasicStroke(w * 0.14f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)

        g2.color = CasinoTheme.gold
        g2.stroke = stroke

        // Два скрещённых фронтона, как в Giebelkreuz: линии из нижних углов
        // сходятся наверху и слегка пересекаются — концы торчат над точкой скрещения
        val crossX = w / 2.0
        val crossY = h * 0.42
        val overshoot = w * 0.14
        // Левый фронтон: из левого нижнего угла вверх-вправо, конец чуть за точку скрещения
        g2.drawLine(
            (w * 0.12).toInt(), (h * 0.88).toInt(),
            (crossX + overshoot).toInt(), (crossY - overshoot * 0.9).toInt(),
        )
        // Правый фронтон: из правого нижнего угла вверх-влево
        g2.drawLine(
            (w * 0.88).toInt(), (h * 0.88).toInt(),
            (crossX - overshoot).toInt(), (crossY - overshoot * 0.9).toInt(),
        )

        // Конские головы на верхних концах, смотрят друг на друга
        paintHorseHead(g2, (crossX + overshoot).toInt(), (crossY - overshoot * 0.9).toInt(), -1)
        paintHorseHead(g2, (crossX - overshoot).toInt(), (crossY - overshoot * 0.9).toInt(), 1)
    }

    /**
     * Конская голова на конце фронтона: треугольное ухо,
     * направленное внутрь ([side] = -1 влево, 1 вправо) — головы смотрят друг на друга.
     */
    private fun paintHorseHead(g2: Graphics2D, x: Int, y: Int, side: Int) {
        val ear = width / 5
        g2.fillPolygon(
            intArrayOf(x, x + side * ear, x + side * ear / 4),
            intArrayOf(y, y, y - ear),
            3,
        )
    }

    companion object {
        /** Общий экземпляр для переиспользования. */
        val INSTANCE = RaifLogoComponent()
    }
}
