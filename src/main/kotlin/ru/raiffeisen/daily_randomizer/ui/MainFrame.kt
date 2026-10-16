package ru.raiffeisen.daily_randomizer.ui

import ru.raiffeisen.daily_randomizer.service.ParticipantService
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel

/**
 * Главное окно в стиле казино: барабан слот-машины с прокруткой имён сверху вниз,
 * кнопка-фишка "Крутить" (после выбора — "Перекрутить"), кнопка "Провести",
 * кнопка настроек в углу, фон — зелёное сукно.
 */
class MainFrame(
    private val participantService: ParticipantService,
) : JFrame("Daily Randomizer") {

    private val slotReel = SlotReel()
    private val spinButton = JButton("КРУТИТЬ")
    private val conductButton = JButton("Провести")
    private var lastWinner: String? = null
    private val spinSequenceGenerator = SpinSequenceGenerator()
    private val spinAnimator = SpinAnimator()
    private val fireworksOverlay = FireworksOverlay()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(560, 400)
        setLocationRelativeTo(null)

        // Весь фон окна — сукно; кнопка настроек встроена в общий фрейм
        contentPane.layout = BorderLayout()
        val root = FeltBackgroundPanel(BorderLayout())
        contentPane.add(root, BorderLayout.CENTER)
        root.add(createSettingsCorner(), BorderLayout.NORTH)
        root.add(createCenterPanel(), BorderLayout.CENTER)

        // Салют поверх всего окна: glass pane не перехватывает клики
        rootPane.glassPane = fireworksOverlay

        slotReel.setPlaceholder("Нажми «Крутить»")
        conductButton.isVisible = false
    }

    private fun createSettingsCorner(): JPanel {
        val settingsButton = JButton("⚙ Настройки").apply {
            CasinoTheme.styleSmallButton(this)
            addActionListener { openSettings() }
        }
        return JPanel(BorderLayout()).apply {
            isOpaque = false
            border = BorderFactory.createEmptyBorder(10, 10, 0, 10)
            add(settingsButton, BorderLayout.EAST)
        }
    }

    private fun createCenterPanel(): JPanel {
        CasinoTheme.styleSpinButton(spinButton)
        spinButton.addActionListener { onSpinClicked() }

        CasinoTheme.styleSmallButton(conductButton)
        conductButton.addActionListener { onConductClicked() }

        val panel = FeltBackgroundPanel(GridBagLayout())
        val constraints = GridBagConstraints().apply {
            gridx = 0
            fill = GridBagConstraints.HORIZONTAL
            weightx = 0.7
            insets = Insets(10, 50, 10, 50)
        }
        panel.add(slotReel, constraints)
        constraints.gridy = 1
        constraints.fill = GridBagConstraints.NONE
        constraints.insets = Insets(24, 0, 10, 0)
        panel.add(spinButton, constraints)
        constraints.gridy = 2
        constraints.insets = Insets(10, 0, 24, 0)
        panel.add(conductButton, constraints)
        return panel
    }

    private fun onSpinClicked() {
        val pool = participantService.getPool()
        if (pool.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Пул пуст. Добавьте имена в настройках и отметьте галочкой, кто сегодня участвует.",
                "Нет участников",
                JOptionPane.WARNING_MESSAGE,
            )
            return
        }
        if (pool.size == 1) {
            lastWinner = pool.first().name
            slotReel.showWinner(lastWinner!!)
            fireworksOverlay.launch()
            showPostSpinButtons()
            return
        }
        spinButton.isEnabled = false
        conductButton.isVisible = false
        spinButton.text = "КРУТИТСЯ..."
        val names = pool.map { it.name }
        val sequence = spinSequenceGenerator.generate(names)
        slotReel.setStrip(sequence)
        slotReel.setOffset(0.0)
        spinAnimator.spin(
            sequence,
            onFrame = { offset -> slotReel.setOffset(offset) },
            onFinish = { name -> onSpinFinished(name) },
        )
    }

    private fun onSpinFinished(name: String) {
        lastWinner = name
        slotReel.showWinner(name)
        fireworksOverlay.launch()
        showPostSpinButtons()
    }

    private fun showPostSpinButtons() {
        spinButton.isEnabled = true
        spinButton.text = "ПЕРЕКРУТИТЬ"
        conductButton.isVisible = true
    }

    /**
     * «Провести»: убирает победителя из пула и сбрасывает состояние к новому выбору.
     */
    private fun onConductClicked() {
        val winner = lastWinner ?: return
        participantService.removeFromPool(winner)
        lastWinner = null
        conductButton.isVisible = false
        spinButton.text = "КРУТИТЬ"
        slotReel.setPlaceholder("«$winner» провёл дейли. Нажми «Крутить»")
    }

    private fun openSettings() {
        spinAnimator.stop()
        spinButton.isEnabled = true
        SettingsDialog(this, participantService).isVisible = true
    }
}
