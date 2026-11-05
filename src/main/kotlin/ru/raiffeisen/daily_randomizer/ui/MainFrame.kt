package ru.raiffeisen.daily_randomizer.ui

import ru.raiffeisen.daily_randomizer.service.ParticipantService
import ru.raiffeisen.daily_randomizer.service.UpdateController
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

    private val updates = UpdateController()
    private val slotReel = SlotReel()
    private val spinButton = JButton("КРУТИТЬ")
    private val conductButton = JButton("Провести")
    private var lastWinner: String? = null
    private val spinSequenceGenerator = SpinSequenceGenerator()
    private val spinAnimator = SpinAnimator()
    private val fireworksOverlay = FireworksOverlay()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        // pack() вместо фиксированного setSize: на Windows при масштабировании DPI
        // шрифты занимают больше пикселей, и при фиксированном размере контент
        // не влезал — GridBagLayout сплющивал барабан в полоску
        pack()
        setSize(maxOf(width, 560), maxOf(height, 400))
        setLocationRelativeTo(null)

        // Листенеры регистрируются ОДИН раз: buildContent() вызывается при каждой
        // смене темы, а кнопки — поля. Повторный addActionListener накапливал бы
        // листенеры (N смен темы → N диалогов предупреждения).
        spinButton.addActionListener { onSpinClicked() }
        conductButton.addActionListener { onConductClicked() }

        buildContent()

        // Салют поверх всего окна: glass pane не перехватывает клики
        rootPane.glassPane = fireworksOverlay
        if (updates.autoUpdates) updates.checkForUpdates()
    }

    /**
     * Строит всё содержимое окна. Вызывается из init и при смене темы
     * (onThemeChanged) — пересоздаёт компоненты с новой палитрой.
     */
    private fun buildContent() {
        contentPane.removeAll()
        contentPane.layout = BorderLayout()
        val root = FeltBackgroundPanel(BorderLayout())
        contentPane.add(root, BorderLayout.CENTER)
        root.add(createSettingsCorner(), BorderLayout.NORTH)
        root.add(createCenterPanel(), BorderLayout.CENTER)
        if (CasinoTheme.current.id == AppTheme.FIELD_OF_WONDERS.id) {
            root.add(JPanel(FlowLayout(FlowLayout.RIGHT, 12, 6)).apply {
                isOpaque = false
                add(YakubovichPortrait())
            }, BorderLayout.SOUTH)
        }

        val winner = lastWinner
        if (winner == null) {
            slotReel.setPlaceholder(startPrompt())
            conductButton.isVisible = false
        } else {
            slotReel.showWinner(winner)
            showPostSpinButtons()
        }
        contentPane.revalidate()
        // Reserve the controls' height even before the post-spin button is shown.
        val required = preferredSize
        minimumSize = java.awt.Dimension(maxOf(560, required.width), maxOf(400, required.height + 60))
        if (width < minimumSize.width || height < minimumSize.height) {
            setSize(maxOf(width, minimumSize.width), maxOf(height, minimumSize.height))
        }
        contentPane.repaint()
    }

    private fun createSettingsCorner(): JPanel {
        val settingsButton = JButton("⚙ Настройки").apply {
            CasinoTheme.styleSmallButton(this)
            addActionListener { openSettings() }
        }
        val corner = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = BorderFactory.createEmptyBorder(10, 10, 0, 10)
            add(settingsButton, BorderLayout.EAST)
        }
        // Логотип слева в углу — только в темах, где он включён
        if (CasinoTheme.current.showLogo) {
            val logo = RaifLogoComponent()
            val logoWrap = JPanel(BorderLayout()).apply {
                isOpaque = false
                add(logo, BorderLayout.WEST)
            }
            corner.add(logoWrap, BorderLayout.WEST)
        }
        return corner
    }

    private fun createCenterPanel(): JPanel {
        spinButton.text = CasinoTheme.current.spinText
        CasinoTheme.styleSpinButton(spinButton)
        CasinoTheme.styleSmallButton(conductButton)

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
        spinButton.text = CasinoTheme.current.spinningText
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
        spinButton.text = CasinoTheme.current.respinText
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
        spinButton.text = CasinoTheme.current.spinText
        slotReel.setPlaceholder("«$winner» провёл дейли. ${startPrompt()}")
    }

    private fun startPrompt(): String = "Нажми «${CasinoTheme.current.spinText}»"

    private fun openSettings() {
        spinAnimator.stop()
        spinButton.isEnabled = true
        SettingsDialog(
            this, participantService,
            updates = updates,
            onThemeChanged = { buildContent() },
            onProfileChanged = {
                lastWinner = null
                spinButton.text = CasinoTheme.current.spinText
                buildContent()
            },
        ).isVisible = true
    }
}
