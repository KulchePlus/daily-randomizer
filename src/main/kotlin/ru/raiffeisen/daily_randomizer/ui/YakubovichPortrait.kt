package ru.raiffeisen.daily_randomizer.ui

import java.awt.Dimension
import java.awt.Graphics
import javax.imageio.ImageIO
import javax.swing.JComponent

/** Pixel portrait supplied by the user, displayed at its original resolution. */
class YakubovichPortrait : JComponent() {
    private val portrait = ImageIO.read(checkNotNull(javaClass.getResource("/themes/yakubovich.png")))

    init {
        preferredSize = Dimension(portrait.width, portrait.height)
        minimumSize = preferredSize
        toolTipText = "Леонид Якубович — вращайте барабан!"
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.drawImage(portrait, (width - portrait.width) / 2, (height - portrait.height) / 2, null)
    }
}
