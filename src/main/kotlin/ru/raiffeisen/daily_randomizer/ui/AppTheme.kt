package ru.raiffeisen.daily_randomizer.ui

import java.awt.Color

/** Узор фона главного окна. */
enum class BackgroundPattern {
    /** Виньетка по краям — казино. */
    VIGNETTE,

    /** Диагональные жёлтые линии — Райффайзен. */
    DIAGONAL_STRIPES,
    LOTTO,
    SCANLINES,
    NEON_GRID,
    COFFEE,
    DOS_BRICKS,
    NONE,
}

/** Стиль кнопки "Крутить". */
enum class ChipStyle {
    /** Круглая казино-фишка с декоративными полосками. */
    ROUND_CHIP,

    /** Плоский прямоугольник с фирменной рамкой. */
    FLAT_RECT,
    WOOD,
    NEON,
    SOFT,
}

enum class ReelStyle { CLASSIC, BARREL, TERMINAL, NEON, CARD, CLEAN }

/**
 * Тема приложения: палитра, типографика, узор фона, оформление барабана и кнопки.
 */
data class AppTheme(
    val id: String,
    val title: String,
    val feltDark: Color,
    val felt: Color,
    val feltLight: Color,
    val feltDeepest: Color,
    val feltDeepestPressed: Color,
    val gold: Color,
    val goldBright: Color,
    val goldDark: Color,
    val red: Color,
    val redBright: Color,
    val slotWindowBg: Color,
    val textLight: Color,
    val backgroundPattern: BackgroundPattern = BackgroundPattern.VIGNETTE,
    val slotBorderThickness: Int = 3,
    val chipStyle: ChipStyle = ChipStyle.ROUND_CHIP,
    val showLogo: Boolean = false,
    val fireworksColors: List<Color> = emptyList(),
    val fontFamily: String = java.awt.Font.SANS_SERIF,
    val reelStyle: ReelStyle = ReelStyle.CLASSIC,
    val spinText: String = "КРУТИТЬ",
    val respinText: String = "ПЕРЕКРУТИТЬ",
    val spinningText: String = "КРУТИТСЯ...",
    val shimmerWinner: Boolean = true,
) {
    companion object {
        /** Казино: зелёное сукно, золото, красная фишка, виньетка. */
        val CASINO = AppTheme(
            id = "casino",
            title = "Казино",
            feltDark = Color(0x0B, 0x3D, 0x24),
            felt = Color(0x0E, 0x51, 0x2F),
            feltLight = Color(0x14, 0x63, 0x3B),
            feltDeepest = Color(0x06, 0x2A, 0x18),
            feltDeepestPressed = Color(0x04, 0x1F, 0x11),
            gold = Color(0xD4, 0xAF, 0x37),
            goldBright = Color(0xF5, 0xD7, 0x63),
            goldDark = Color(0x9A, 0x7B, 0x1E),
            red = Color(0xB3, 0x1B, 0x1B),
            redBright = Color(0xD9, 0x2B, 0x2B),
            slotWindowBg = Color(0x12, 0x12, 0x12),
            textLight = Color(0xF5, 0xF0, 0xE1),
        )

        /** Райффайзен: фирменный жёлтый на тёмно-сером, диагональные линии, логотип. */
        val RAIFFEISEN = AppTheme(
            id = "raiffeisen",
            title = "Райффайзен",
            feltDark = Color(0x1A, 0x1A, 0x1A),
            felt = Color(0x33, 0x33, 0x33),
            feltLight = Color(0x3D, 0x3D, 0x3D),
            feltDeepest = Color(0x0D, 0x0D, 0x0D),
            feltDeepestPressed = Color(0x00, 0x00, 0x00),
            gold = Color(0xFF, 0xEC, 0x00),
            goldBright = Color(0xFF, 0xF9, 0x5A),
            goldDark = Color(0xB8, 0xA8, 0x00),
            red = Color(0x26, 0x26, 0x26),
            redBright = Color(0x3D, 0x3D, 0x3D),
            slotWindowBg = Color(0x00, 0x00, 0x00),
            textLight = Color(0xFA, 0xFA, 0xFA),
            backgroundPattern = BackgroundPattern.DIAGONAL_STRIPES,
            slotBorderThickness = 1,
            chipStyle = ChipStyle.FLAT_RECT,
            showLogo = true,
            fireworksColors = listOf(
                Color(0xFF, 0xEC, 0x00),
                Color(0xFF, 0xF9, 0x5A),
                Color(0xFA, 0xFA, 0xFA),
                Color(0xB8, 0xA8, 0x00),
            ),
        )

        val SOVIET_LOTTO = AppTheme(
            id = "soviet-lotto", title = "Советское лото",
            feltDark = Color(0x451E28), felt = Color(0x713341), feltLight = Color(0x63303C),
            feltDeepest = Color(0x351720), feltDeepestPressed = Color(0x291119),
            gold = Color(0xE4BC77), goldBright = Color(0x56361F), goldDark = Color(0xBE965E),
            red = Color(0xD8AE70), redBright = Color(0xEBCB94),
            slotWindowBg = Color(0xEDD3A2), textLight = Color(0xFFF0D5),
            backgroundPattern = BackgroundPattern.LOTTO,
            chipStyle = ChipStyle.WOOD, reelStyle = ReelStyle.BARREL,
            fontFamily = java.awt.Font.SERIF, spinText = "Достать бочонок",
            respinText = "Ещё бочонок", spinningText = "Перемешиваем...", shimmerWinner = false,
            fireworksColors = listOf(Color(0xE4BC77), Color(0xFFF0D5), Color(0xB95660)),
        )

        val TERMINAL = AppTheme(
            id = "terminal", title = "Терминал",
            feltDark = Color(0x080E0B), felt = Color(0x14271B), feltLight = Color(0x0D1912),
            feltDeepest = Color(0x050A07), feltDeepestPressed = Color(0x162D1D),
            gold = Color(0x6BE595), goldBright = Color(0xA5FFC3), goldDark = Color(0x448F5C),
            red = Color(0x0B1710), redBright = Color(0x173622),
            slotWindowBg = Color(0x050B07), textLight = Color(0xB8EAC8),
            backgroundPattern = BackgroundPattern.SCANLINES,
            chipStyle = ChipStyle.FLAT_RECT, reelStyle = ReelStyle.TERMINAL,
            fontFamily = java.awt.Font.MONOSPACED, spinText = "./choose",
            respinText = "./choose --again", spinningText = "running...", shimmerWinner = false,
            fireworksColors = listOf(Color(0x6BE595), Color(0xA5FFC3)),
        )

        val CYBERPUNK = AppTheme(
            id = "cyberpunk", title = "Киберпанк",
            feltDark = Color(0x100B21), felt = Color(0x29163F), feltLight = Color(0x211135),
            feltDeepest = Color(0x0B0718), feltDeepestPressed = Color(0x36164D),
            gold = Color(0x43E8F3), goldBright = Color(0xBAF9FF), goldDark = Color(0xA14AA4),
            red = Color(0x251135), redBright = Color(0xFF50CB),
            slotWindowBg = Color(0x0B0718), textLight = Color(0xF5DFFF),
            backgroundPattern = BackgroundPattern.NEON_GRID,
            chipStyle = ChipStyle.NEON, reelStyle = ReelStyle.NEON,
            fontFamily = java.awt.Font.MONOSPACED,
            fireworksColors = listOf(Color(0x43E8F3), Color(0xFF50CB), Color(0xBFA0FF)),
        )

        val COFFEE_HOUSE = AppTheme(
            id = "coffee-house", title = "Кофейня",
            feltDark = Color(0xE9D8C2), felt = Color(0xDEC7AA), feltLight = Color(0xF6EBDD),
            feltDeepest = Color(0xFFF5E8), feltDeepestPressed = Color(0xDDC6AB),
            gold = Color(0x70472F), goldBright = Color(0x593722), goldDark = Color(0x936E4F),
            red = Color(0x70472F), redBright = Color(0x8A5B3C),
            slotWindowBg = Color(0xFFF8ED), textLight = Color(0x49352A),
            backgroundPattern = BackgroundPattern.COFFEE,
            chipStyle = ChipStyle.SOFT, reelStyle = ReelStyle.CARD,
            fontFamily = java.awt.Font.SERIF, spinText = "Кто варит дейли?",
            respinText = "Другой заказ", spinningText = "Завариваем...", shimmerWinner = false,
            fireworksColors = listOf(Color(0x70472F), Color(0xBE8758), Color(0x936E4F)),
        )

        val MINIMALISM = AppTheme(
            id = "minimalism", title = "Минимализм",
            feltDark = Color(0xF4F5F7), felt = Color(0xE5EAF2), feltLight = Color(0xF4F5F7),
            feltDeepest = Color(0xFFFFFF), feltDeepestPressed = Color(0xE4EAF4),
            gold = Color(0x315DB5), goldBright = Color(0x23478F), goldDark = Color(0x54709F),
            red = Color(0x315DB5), redBright = Color(0x23478F),
            slotWindowBg = Color(0xFFFFFF), textLight = Color(0x202C40),
            backgroundPattern = BackgroundPattern.NONE,
            slotBorderThickness = 1, chipStyle = ChipStyle.SOFT, reelStyle = ReelStyle.CLEAN,
            spinText = "Выбрать ведущего", respinText = "Выбрать ещё раз",
            spinningText = "Выбираем...", shimmerWinner = false,
            fireworksColors = listOf(Color(0x315DB5), Color(0x7394CF), Color(0x54709F)),
        )

        val FIELD_OF_WONDERS = AppTheme(
            id = "field-of-wonders", title = "Поле чудес",
            feltDark = Color(0x24243C), felt = Color(0x343454), feltLight = Color(0x24243C),
            feltDeepest = Color(0x000080), feltDeepestPressed = Color(0x000050),
            gold = Color(0xFFFF55), goldBright = Color(0xFFFFFF), goldDark = Color(0xAAAAAA),
            red = Color(0x800000), redBright = Color(0xAA0000),
            slotWindowBg = Color(0x000080), textLight = Color(0xFFFFFF),
            backgroundPattern = BackgroundPattern.DOS_BRICKS,
            slotBorderThickness = 4, chipStyle = ChipStyle.FLAT_RECT, reelStyle = ReelStyle.CLASSIC,
            fontFamily = java.awt.Font.MONOSPACED,
            spinText = "Вращайте барабан!", respinText = "Ещё оборот!",
            spinningText = "Крутим!", shimmerWinner = false,
            fireworksColors = listOf(Color(0xFFFF55), Color(0x55FFFF), Color(0xFF55FF)),
        )

        val ALL = listOf(CASINO, RAIFFEISEN, SOVIET_LOTTO, TERMINAL, CYBERPUNK, COFFEE_HOUSE, MINIMALISM, FIELD_OF_WONDERS)
    }
}
