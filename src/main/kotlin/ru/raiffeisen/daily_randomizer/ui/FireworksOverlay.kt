package ru.raiffeisen.daily_randomizer.ui

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent
import kotlin.math.cos
import kotlin.math.sin

/**
 * Полноэкранный оверлей салюта поверх всего окна (glass pane).
 * Не перехватывает клики. Запускается через [launch], сам скрывается,
 * когда все разрывы затухли.
 */
class FireworksOverlay : JComponent() {

    private var fireworks: List<Firework> = emptyList()

    init {
        isOpaque = false
        isVisible = false
    }

    /**
     * Запускает новый салют на весь размер окна.
     */
    fun launch() {
        fireworks = spawnFireworks()
        isVisible = true
        timer.start()
    }

    private val timer: javax.swing.Timer = javax.swing.Timer(50) {
        fireworks = fireworks.map { it.advance() }.filter { it.alive }
        if (fireworks.isEmpty()) {
            timer.stop()
            isVisible = false
        }
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        fireworks.forEach { it.paint(g2) }
    }

    private fun spawnFireworks(): List<Firework> {
        val random = java.util.Random()
        // Цвета частиц — по теме: у казино случайный HSB, у Райффайзена фирменные жёлто-белые
        val themeColors = CasinoTheme.current.fireworksColors
        return List(FIREWORK_COUNT) {
            Firework(
                x = random.nextDouble() * 0.8 + 0.1,
                y = random.nextDouble() * 0.6 + 0.1,
                color = if (themeColors.isNotEmpty()) {
                    themeColors[random.nextInt(themeColors.size)]
                } else {
                    Color.getHSBColor(random.nextFloat(), 0.85f, 1.0f)
                },
                particles = List(PARTICLE_COUNT) {
                    val angle = random.nextDouble() * 2 * Math.PI
                    val speed = random.nextDouble() * 0.6 + 0.4
                    Particle(angle, speed)
                },
            )
        }
    }

    private data class Particle(val angle: Double, val speed: Double)

    /**
     * Разрыв фейерверка: точка взрыва с разлетающимися частицами, затухающими со временем.
     */
    private class Firework(
        private val x: Double,
        private val y: Double,
        private val color: Color,
        private val particles: List<Particle>,
    ) {
        private var age = 0.0

        val alive: Boolean
            get() = age < LIFETIME

        fun advance(): Firework {
            age += STEP
            return this
        }

        fun paint(g2: Graphics2D) {
            val w = g2.clipBounds?.width ?: return
            val h = g2.clipBounds?.height ?: return
            val cx = (x * w).toInt()
            val cy = (y * h).toInt()
            val alpha = (1.0 - age / LIFETIME).coerceIn(0.0, 1.0)
            val radius = age * 90.0

            g2.composite = java.awt.AlphaComposite.SrcOver.derive(alpha.toFloat())
            particles.forEach { p ->
                val px = cx + radius * p.speed * cos(p.angle)
                val py = cy + radius * p.speed * sin(p.angle)
                g2.color = color
                g2.fillRect(px.toInt(), py.toInt(), 4, 4)
            }
            g2.composite = java.awt.AlphaComposite.SrcOver
        }

        companion object {
            private const val LIFETIME = 4.8
            private const val STEP = 0.05
        }
    }

    companion object {
        private const val FIREWORK_COUNT = 14
        private const val PARTICLE_COUNT = 48
    }
}
