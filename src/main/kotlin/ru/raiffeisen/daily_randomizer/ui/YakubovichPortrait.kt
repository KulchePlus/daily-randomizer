package ru.raiffeisen.daily_randomizer.ui

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.imageio.ImageIO
import javax.swing.JComponent

/** Compact pixel portrait beside the controls. */
class YakubovichPortrait : JComponent() {
    private val portrait = ImageIO.read(checkNotNull(javaClass.getResource("/themes/yakubovich.png")))

    init {
        preferredSize = Dimension(80, 80)
        minimumSize = preferredSize
        toolTipText = "Леонид Якубович — вращайте барабан!"
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val scale = minOf(width.toDouble() / portrait.width, height.toDouble() / portrait.height)
        val w = (portrait.width * scale).toInt()
        val h = (portrait.height * scale).toInt()
        val layer = g.create() as Graphics2D
        try {
            layer.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
            layer.drawImage(portrait, (width - w) / 2, (height - h) / 2, w, h, null)
        } finally { layer.dispose() }
    }
}
