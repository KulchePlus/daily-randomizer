package ru.raiffeisen.daily_randomizer.ui

import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JButton

/** Миниатюра из тех же компонентов, что и главное окно. Вызывается на EDT. */
object ThemePreview {
    fun create(theme: AppTheme): ImageIcon {
        val previous = CasinoTheme.current
        try {
            CasinoTheme.current = theme
            val image = BufferedImage(192, 136, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                // Рендерим полный размер и уменьшаем вместе со шрифтами и узорами.
                g.scale(1.0 / 3.0, 1.0 / 3.0)
                val background = FeltBackgroundPanel(null)
                background.setSize(576, 408)
                background.paint(g)
                val reel = SlotReel()
                reel.setSize(456, 132)
                reel.setStrip(listOf("Иван", "Алиса", "Мария"))
                reel.setOffset(1.0)
                paintAt(g, 60, 114, 456, 132) { reel.paint(it) }
                val button = JButton(theme.spinText)
                CasinoTheme.styleSpinButton(button)
                button.setSize(300, 60)
                paintAt(g, 138, 282, 300, 60) { button.paint(it) }
                val settings = JButton("Настройки")
                CasinoTheme.styleSmallButton(settings)
                settings.setSize(100, 30)
                paintAt(g, 452, 18, 100, 30) { settings.paint(it) }
                if (theme.showLogo) {
                    val logo = RaifLogoComponent()
                    logo.setSize(72, 72)
                    paintAt(g, 18, 18, 72, 72) { logo.paint(it) }
                }
            } finally { g.dispose() }
            return ImageIcon(image)
        } finally {
            CasinoTheme.current = previous
        }
    }

    private fun paintAt(g: Graphics2D, x: Int, y: Int, w: Int, h: Int, paint: (Graphics2D) -> Unit) {
        val layer = g.create(x, y, w, h) as Graphics2D
        try { paint(layer) } finally { layer.dispose() }
    }
}
