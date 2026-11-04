package ru.raiffeisen.daily_randomizer.ui

import java.awt.BasicStroke
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.RenderingHints
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.Timer

/** Шуточная подписка: имя используется только для подписи QR, выбор участников не затрагивается. */
class SubscriptionPanel : JPanel(GridBagLayout()) {
    private val heading = JLabel("Подписка «Антидейли»")
    private val description = JTextArea(
        "Купив подписку, вы будете гораздо реже выпадать при выборе ведущего дейли. " +
            "Больше спокойных утр, меньше внезапных выступлений!",
    )
    private val nameLabel = JLabel("Введите имя")
    private val nameField = JTextField(24)
    private val generateButton = JButton("Сгенерировать QR для оплаты")
    private val status = JLabel(" ", SwingConstants.CENTER)
    private val placeholder = JLabel("Ваш QR-код появится здесь", SwingConstants.CENTER)
    private val spinner = LoadingSpinner()
    private val qrLabel = JLabel(ImageIcon(requireNotNull(javaClass.getResource(QR_RESOURCE))), SwingConstants.CENTER)
    private val qrCards = JPanel(CardLayout())
    private var startedAt = 0L
    private val generationTimer = Timer(40) {
        spinner.advance()
        if (System.nanoTime() - startedAt >= GENERATION_DELAY_MS * 1_000_000L) finishGeneration()
    }

    init {
        isOpaque = false
        border = BorderFactory.createEmptyBorder(16, 24, 16, 24)
        description.isEditable = false
        description.isFocusable = false
        description.isOpaque = false
        description.lineWrap = true
        description.wrapStyleWord = true
        description.rows = 3
        nameLabel.labelFor = nameField
        nameField.name = "subscriptionName"
        qrLabel.accessibleContext.accessibleDescription = "QR-код для оплаты подписки"
        qrCards.isOpaque = false
        qrCards.preferredSize = Dimension(246, 246)
        qrCards.add(placeholder, "empty")
        val loadingCard = JPanel(GridBagLayout()).apply {
            isOpaque = false
            add(spinner)
        }
        qrCards.add(loadingCard, "loading")
        qrCards.add(qrLabel, "qr")

        val constraints = GridBagConstraints().apply {
            gridx = 0
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
        }
        fun row(component: JComponent, index: Int, gap: Int) {
            constraints.gridy = index
            constraints.insets = Insets(gap, 0, 0, 0)
            add(component, constraints)
        }
        row(heading, 0, 0)
        row(description, 1, 8)
        row(nameLabel, 2, 8)
        row(nameField, 3, 6)
        row(generateButton, 4, 10)
        row(status, 5, 8)
        constraints.weighty = 1.0
        constraints.fill = GridBagConstraints.BOTH
        row(qrCards, 6, 8)

        generateButton.addActionListener { generate() }
        nameField.addActionListener { if (generateButton.isEnabled) generate() }
        refreshTheme()
    }

    fun refreshTheme() {
        heading.font = CasinoTheme.titleFont
        heading.foreground = CasinoTheme.textLight
        description.font = CasinoTheme.smallButtonFont.deriveFont(java.awt.Font.PLAIN, 13f)
        description.foreground = CasinoTheme.textLight
        nameLabel.font = CasinoTheme.smallButtonFont
        nameLabel.foreground = CasinoTheme.textLight
        nameField.font = CasinoTheme.smallButtonFont.deriveFont(15f)
        nameField.background = CasinoTheme.slotWindowBg
        nameField.foreground = CasinoTheme.goldBright
        nameField.caretColor = CasinoTheme.gold
        nameField.selectionColor = CasinoTheme.felt
        nameField.selectedTextColor = CasinoTheme.textLight
        nameField.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CasinoTheme.goldDark),
            BorderFactory.createEmptyBorder(7, 10, 7, 10),
        )
        CasinoTheme.styleSmallButton(generateButton)
        generateButton.preferredSize = Dimension(320, 34)
        status.font = CasinoTheme.smallButtonFont
        status.foreground = CasinoTheme.gold
        placeholder.font = CasinoTheme.smallButtonFont
        placeholder.foreground = CasinoTheme.goldDark
        repaint()
    }

    private fun generate() {
        val name = nameField.text.trim()
        if (name.isEmpty()) {
            status.text = "Сначала введите имя"
            nameField.requestFocusInWindow()
            return
        }
        nameField.isEditable = false
        generateButton.isEnabled = false
        status.text = "Генерируем QR для оплаты…"
        (qrCards.layout as CardLayout).show(qrCards, "loading")
        startedAt = System.nanoTime()
        generationTimer.restart()
    }

    private fun finishGeneration() {
        generationTimer.stop()
        nameField.isEditable = true
        generateButton.isEnabled = true
        status.text = "QR для ${nameField.text.trim()} готов"
        (qrCards.layout as CardLayout).show(qrCards, "qr")
    }

    fun stopGeneration() {
        generationTimer.stop()
        nameField.isEditable = true
        generateButton.isEnabled = true
        status.text = " "
        (qrCards.layout as CardLayout).show(qrCards, "empty")
    }

    override fun removeNotify() {
        // Закрытое окно не должно оставлять активные Swing-таймеры.
        stopGeneration()
        super.removeNotify()
    }

    companion object {
        const val GENERATION_DELAY_MS = 1500L
        // Статический QR: https://media.giphy.com/media/Vuw9m5wXviFIQ/giphy.gif
        // ZXing 3.5.3, correction M, quiet zone 4 модуля; сетевых запросов из UI нет.
        const val QR_RESOURCE = "/subscription/payment-qr.png"
    }
}

private class LoadingSpinner : JComponent() {
    private var angle = 0

    init {
        preferredSize = Dimension(48, 48)
        accessibleContext?.accessibleName = "Генерация QR-кода"
    }

    fun advance() {
        angle = (angle + 18) % 360
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.stroke = BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.color = CasinoTheme.goldDark
            g2.drawOval(6, 6, width - 13, height - 13)
            g2.color = CasinoTheme.gold
            g2.drawArc(6, 6, width - 13, height - 13, -angle, 100)
        } finally { g2.dispose() }
    }
}
