package ru.raiffeisen.daily_randomizer.ui

import javax.swing.Timer
import kotlin.random.Random

/**
 * Непрерывная анимация барабана: лента имён плавно движется сверху вниз.
 * Скорость постепенно снижается (как остановка барабана в казино), в конце
 * лента мягко докручивается до целой ячейки с именем победителя.
 * Общая длительность прокрутки немного рандомизируется.
 *
 * @param onFrame вызывается на каждый кадр с текущим смещением (в ячейках, дробное)
 * @param onFinish вызывается один раз в конце с итоговым именем
 */
class SpinAnimator(private val random: Random = Random.Default) {

    fun spin(
        sequence: List<String>,
        onFrame: (Double) -> Unit,
        onFinish: (String) -> Unit,
    ) {
        stop()
        if (sequence.isEmpty()) {
            return
        }

        // Цель прокрутки: 1 полный круг + докрутка до последнего элемента ленты (победителя).
        // Лента зациклена по модулю size, поэтому остановка на (size - 1) показывает победителя в центре.
        val size = sequence.size
        val totalCells = (size + (size - 1)).toDouble()
        var offset = 0.0
        var lastTime = System.nanoTime()

        // Рандомизация длительности — через случайную стартовую скорость
        val maxSpeed = MAX_CELLS_PER_SEC + random.nextDouble() * RANDOM_SPEED_SPREAD

        val swingTimer = Timer(FRAME_MS, null)
        swingTimer.addActionListener {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1_000_000_000.0
            lastTime = now

            // Скорость падает со временем: быстрый старт, плавное торможение
            val progress = (offset / totalCells).coerceIn(0.0, 1.0)
            val speed = maxSpeed - (maxSpeed - MIN_CELLS_PER_SEC) * progress * progress

            offset += speed * dt
            if (offset >= totalCells) {
                stop()
                onFrame(totalCells)
                onFinish(sequence.last())
            } else {
                onFrame(offset)
            }
        }
        swingTimer.isRepeats = true
        timer = swingTimer
        lastTime = System.nanoTime()
        swingTimer.start()
    }

    private var timer: Timer? = null

    fun stop() {
        timer?.stop()
        timer = null
    }

    companion object {
        private const val FRAME_MS = 16
        private const val MAX_CELLS_PER_SEC = 18.0
        private const val MIN_CELLS_PER_SEC = 1.2

        /** Разброс стартовой скорости для рандомизации длительности. */
        private const val RANDOM_SPEED_SPREAD = 8.0
    }
}
