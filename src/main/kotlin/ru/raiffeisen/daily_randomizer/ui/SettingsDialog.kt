package ru.raiffeisen.daily_randomizer.ui

import ru.raiffeisen.daily_randomizer.service.Participant
import ru.raiffeisen.daily_randomizer.service.ParticipantService
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Window
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import javax.swing.UIManager

/**
 * Диалог настроек с вкладками: «Участники» (список и пул) и «Тема» (выбор стиля).
 * Тёмный фон, золотые акценты в стиле казино.
 */
class SettingsDialog(
    owner: Window,
    private val participantService: ParticipantService,
    private val onThemeChanged: () -> Unit = {},
) : JDialog(owner, "Настройки", ModalityType.APPLICATION_MODAL) {

    private companion object {
        // Ширина зоны клика по чекбоксу в строке списка (px от левого края ячейки)
        const val CHECKBOX_ZONE_WIDTH = 40

        // Имена карточек (вкладок)
        const val TAB_PARTICIPANTS = "participants"
        const val TAB_THEME = "theme"
    }

    private val listModel = DefaultListModel<Participant>()
    private val list = JList(listModel)
    private val nameField = JTextField(20)

    init {
        layout = BorderLayout()

        // Листенеры регистрируются ОДИН раз: refreshTheme() пересоздаёт содержимое
        // при каждой смене темы, а поля — одни и те же. Повторная регистрация
        // накапливала бы листенеры (N смен темы → N обработчиков клика).
        nameField.addActionListener { onAdd() }
        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                val index = list.locationToIndex(event.point)
                if (index < 0) return
                val participant = listModel.get(index) ?: return
                val cellBounds = list.getCellBounds(index, index) ?: return
                val clickX = event.point.x - cellBounds.x
                if (clickX <= CHECKBOX_ZONE_WIDTH) {
                    participantService.setInPool(participant.name, !participant.inPool)
                    reloadParticipants()
                }
            }
        })

        buildContent()
    }

    /**
     * Строит всё содержимое диалога. Вызывается из init и при смене темы
     * (refreshTheme) — пересоздаёт компоненты с новой палитрой.
     */
    private fun buildContent() {
        contentPane.background = CasinoTheme.feltDark

        // Свои вкладки на кнопках + CardLayout: Aqua (macOS) рисует JTabbedPane
        // по-своему (белый фон, серый текст) и игнорирует часть настроек —
        // полностью управляемая отрисовка надёжнее.
        val contentCards = JPanel(java.awt.CardLayout())
        contentCards.isOpaque = false
        contentCards.add(createParticipantsTab(), TAB_PARTICIPANTS)
        contentCards.add(createThemeTab(), TAB_THEME)

        val participantsButtons = createButtonsPanel()
        val closeButtonOnly = createCloseOnlyPanel()

        // Нижняя панель тоже на CardLayout: BorderLayout хранит только один
        // компонент в CENTER — вторая панель вытеснялась и кнопки пропадали
        val southPanel = JPanel(java.awt.CardLayout())
        southPanel.isOpaque = false
        southPanel.add(participantsButtons, TAB_PARTICIPANTS)
        southPanel.add(closeButtonOnly, TAB_THEME)

        val participantsTabButton = createTabButton("Участники")
        val themeTabButton = createTabButton("Тема")

        fun selectTab(tab: String) {
            (contentCards.layout as java.awt.CardLayout).show(contentCards, tab)
            (southPanel.layout as java.awt.CardLayout).show(southPanel, tab)
            val onParticipants = tab == TAB_PARTICIPANTS
            participantsTabButton.isSelected = onParticipants
            themeTabButton.isSelected = !onParticipants
        }

        participantsTabButton.addActionListener { selectTab(TAB_PARTICIPANTS) }
        themeTabButton.addActionListener { selectTab(TAB_THEME) }

        val tabHeader = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 4)).apply {
            isOpaque = false
            border = BorderFactory.createEmptyBorder(8, 12, 0, 12)
            add(participantsTabButton)
            add(themeTabButton)
        }

        add(tabHeader, BorderLayout.NORTH)
        add(contentCards, BorderLayout.CENTER)
        add(southPanel, BorderLayout.SOUTH)

        selectTab(TAB_PARTICIPANTS)

        // Ширина окна — по фактической ширине панели кнопок + поля,
        // чтобы все кнопки помещались в один ряд при любом шрифте системы
        pack()
        val buttonsWidth = participantsButtons.preferredSize.width
        setSize(maxOf(buttonsWidth + 32, 440), maxOf(height, 460))
        setLocationRelativeTo(owner)

        reloadParticipants()
    }

    /**
     * Кнопка-вкладка: выбранная — сукно с золотой рамкой, невыбранная — темнее.
     */
    private fun createTabButton(title: String): JButton {
        val button = JButton(title)
        CasinoTheme.styleSmallButton(button)
        button.isFocusable = false
        button.addActionListener {
            // Состояние selected управляется в selectTab — здесь ничего не делаем
        }
        button.setUI(object : javax.swing.plaf.basic.BasicButtonUI() {
            override fun paint(g: Graphics, c: javax.swing.JComponent) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
                val b = c.bounds
                val selected = (c as JButton).isSelected
                g2.color = if (selected) CasinoTheme.felt else CasinoTheme.feltDeepest
                g2.fillRect(0, 0, b.width - 1, b.height - 1)
                g2.color = if (selected) CasinoTheme.gold else CasinoTheme.goldDark
                g2.drawRect(0, 0, b.width - 1, b.height - 1)
                g2.font = CasinoTheme.smallButtonFont
                g2.color = if (selected) CasinoTheme.textLight else CasinoTheme.gold
                val metrics = g2.fontMetrics
                val tx = (b.width - metrics.stringWidth(button.text)) / 2
                val ty = (b.height + metrics.ascent - metrics.descent) / 2
                g2.drawString(button.text, tx, ty)
            }
        })
        return button
    }

    /**
     * Вкладка «Участники»: поле добавления + список с чекбоксами пула.
     */
    private fun createParticipantsTab(): JPanel {
        val tab = JPanel(BorderLayout())
        tab.isOpaque = false
        tab.add(createAddPanel(), BorderLayout.NORTH)
        tab.add(createListPanel(), BorderLayout.CENTER)
        return tab
    }

    /**
     * Вкладка «Тема»: плитки тем — картинка-превью + название снизу.
     * Клик по плитке переключает тему приложения на лету.
     */
    private fun createThemeTab(): JPanel {
        val tab = JPanel(BorderLayout())
        tab.isOpaque = false

        val tilesPanel = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 16, 16)).apply {
            isOpaque = false
            border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
        }

        AppTheme.ALL.forEach { theme ->
            tilesPanel.add(createThemeTile(theme) {
                CasinoTheme.current = theme
                refreshTheme()
                onThemeChanged()
            })
        }

        val scrollPane = JScrollPane(tilesPanel)
        scrollPane.border = null
        scrollPane.isOpaque = false
        scrollPane.viewport.isOpaque = false
        scrollPane.verticalScrollBarPolicy = javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED

        tab.add(scrollPane, BorderLayout.CENTER)
        return tab
    }

    /**
     * Плитка темы: превью-картинка + название снизу, золотая рамка у выбранной.
     * Клик — [onSelect].
     */
    private fun createThemeTile(theme: AppTheme, onSelect: () -> Unit): JPanel {
        val selected = CasinoTheme.current.id == theme.id
        val icon = loadImageIcon(theme.previewPath)
        val previewLabel = JLabel(icon)
        previewLabel.border = BorderFactory.createLineBorder(
            if (selected) CasinoTheme.gold else CasinoTheme.goldDark,
            if (selected) 3 else 1,
        )

        val titleLabel = JLabel(theme.title, SwingConstants.CENTER)
        titleLabel.font = CasinoTheme.buttonFont
        titleLabel.foreground = if (selected) CasinoTheme.goldBright else CasinoTheme.gold

        val tile = JPanel(BorderLayout(0, 6)).apply {
            isOpaque = false
            add(previewLabel, BorderLayout.CENTER)
            add(titleLabel, BorderLayout.SOUTH)
            cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
        }
        tile.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                onSelect()
            }
        })
        return tile
    }

    /**
     * Перекрашивает окно настроек под новую тему: пересоздаём всё содержимое.
     */
    private fun refreshTheme() {
        contentPane.removeAll()
        buildContent()
        contentPane.revalidate()
        contentPane.repaint()
    }

    /**
     * Загружает картинку из ресурсов jar или из файла (для запуска из IDE).
     */
    private fun loadImageIcon(path: String): javax.swing.Icon {
        val resource = javaClass.getResourceAsStream(path)
        if (resource != null) {
            return javax.swing.ImageIcon(resource.readBytes())
        }
        val file = java.io.File("src/main/resources$path")
        return javax.swing.ImageIcon(file.absolutePath)
    }

    /**
     * Нижняя панель для вкладки «Тема»: только «Закрыть».
     */
    private fun createCloseOnlyPanel(): JPanel {
        val closeButton = JButton("Закрыть").apply {
            CasinoTheme.styleSmallButton(this)
            addActionListener { dispose() }
        }
        return JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            isOpaque = false
            border = BorderFactory.createEmptyBorder(4, 12, 12, 12)
            add(closeButton)
        }
    }

    private fun createListPanel(): JPanel {
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.background = CasinoTheme.slotWindowBg
        list.foreground = CasinoTheme.textLight
        list.selectionForeground = CasinoTheme.goldBright
        list.selectionBackground = CasinoTheme.felt
        list.font = Font(Font.SANS_SERIF, Font.PLAIN, 16)
        list.fixedCellHeight = 30
        list.cellRenderer = ParticipantRenderer()

        val scrollPane = JScrollPane(list)
        scrollPane.border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CasinoTheme.goldDark),
                "Участники (галочка — в пуле)",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                CasinoTheme.smallButtonFont,
                CasinoTheme.gold,
            ),
            BorderFactory.createEmptyBorder(4, 8, 8, 8),
        )
        scrollPane.background = CasinoTheme.feltDark
        scrollPane.viewport.background = CasinoTheme.slotWindowBg
        scrollPane.verticalScrollBarPolicy = javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED

        return JPanel(BorderLayout()).apply {
            isOpaque = false
            border = BorderFactory.createEmptyBorder(8, 12, 8, 12)
            add(scrollPane, BorderLayout.CENTER)
        }
    }

    private fun createAddPanel(): JPanel {
        nameField.font = Font(Font.SANS_SERIF, Font.PLAIN, 16)
        nameField.background = CasinoTheme.slotWindowBg
        nameField.foreground = CasinoTheme.textLight
        nameField.caretColor = CasinoTheme.gold
        nameField.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CasinoTheme.goldDark),
            BorderFactory.createEmptyBorder(6, 8, 6, 8),
        )

        val addButton = JButton("Добавить").apply {
            CasinoTheme.styleSmallButton(this)
            addActionListener { onAdd() }
        }

        return JPanel(BorderLayout(8, 0)).apply {
            isOpaque = false
            border = BorderFactory.createEmptyBorder(12, 12, 4, 12)
            add(nameField, BorderLayout.CENTER)
            add(addButton, BorderLayout.EAST)
        }
    }

    private fun createButtonsPanel(): JPanel {
        val addAllButton = JButton("Добавить всех").apply {
            CasinoTheme.styleSmallButton(this)
            addActionListener {
                participantService.addAllToPool()
                reloadParticipants()
            }
        }
        val removeAllButton = JButton("Убрать всех").apply {
            CasinoTheme.styleSmallButton(this)
            addActionListener {
                participantService.removeAllFromPool()
                reloadParticipants()
            }
        }
        val deleteButton = JButton("Удалить выбранного").apply {
            CasinoTheme.styleSmallButton(this)
            addActionListener { onDelete() }
        }
        val closeButton = JButton("Закрыть").apply {
            CasinoTheme.styleSmallButton(this)
            addActionListener { dispose() }
        }
        return JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            isOpaque = false
            border = BorderFactory.createEmptyBorder(4, 12, 12, 12)
            add(addAllButton)
            add(removeAllButton)
            add(deleteButton)
            add(closeButton)
        }
    }

    private fun onAdd() {
        val name = nameField.text
        try {
            participantService.addParticipant(name)
            nameField.text = ""
            reloadParticipants()
        } catch (e: IllegalArgumentException) {
            showError(e.message ?: "Не удалось добавить участника")
        }
    }

    private fun onDelete() {
        val selected = list.selectedValue ?: return
        participantService.removeParticipant(selected.name)
        reloadParticipants()
    }

    private fun reloadParticipants() {
        listModel.clear()
        participantService.getParticipants().forEach { listModel.addElement(it) }
    }

    private fun showError(message: String) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE)
    }

    /**
     * Рендер строки участника: чекбокс слева (в пуле или нет) + имя.
     * Только отрисовка — переключение пула обрабатывает MouseListener на списке.
     */
    private inner class ParticipantRenderer : ListCellRenderer<Participant> {

        private val checkBox = JCheckBox().apply {
            isOpaque = false
            isFocusPainted = false
        }
        private val nameLabel = JLabel().apply {
            font = Font(Font.SANS_SERIF, Font.PLAIN, 16)
        }
        private val panel = JPanel(BorderLayout(8, 0)).apply {
            isOpaque = false
            border = BorderFactory.createEmptyBorder(2, 8, 2, 8)
            add(checkBox, BorderLayout.WEST)
            add(nameLabel, BorderLayout.CENTER)
        }

        override fun getListCellRendererComponent(
            list: JList<out Participant>,
            value: Participant,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            checkBox.isSelected = value.inPool
            nameLabel.text = value.name
            nameLabel.foreground = if (value.inPool) CasinoTheme.textLight else Color(0x88, 0x88, 0x80)
            panel.background = if (isSelected) CasinoTheme.felt else CasinoTheme.slotWindowBg
            panel.isOpaque = isSelected
            return panel
        }
    }
}
