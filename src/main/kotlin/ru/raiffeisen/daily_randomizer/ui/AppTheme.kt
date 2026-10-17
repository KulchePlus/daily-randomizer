package ru.raiffeisen.daily_randomizer.ui

import java.awt.Color

/** Узор фона главного окна. */
enum class BackgroundPattern {
    /** Виньетка по краям — казино. */
    VIGNETTE,

    /** Диагональные жёлтые линии — Райффайзен. */
    DIAGONAL_STRIPES,
}

/** Стиль кнопки "Крутить". */
enum class ChipStyle {
    /** Круглая казино-фишка с декоративными полосками. */
    ROUND_CHIP,

    /** Плоский прямоугольник с фирменной рамкой. */
    FLAT_RECT,
}

/**
 * Тема приложения: палитра цветов, узор фона, стиль кнопки и путь к превью.
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
    val previewPath: String,
    val backgroundPattern: BackgroundPattern = BackgroundPattern.VIGNETTE,
    val slotBorderThickness: Int = 3,
    val chipStyle: ChipStyle = ChipStyle.ROUND_CHIP,
    val showLogo: Boolean = false,
    val fireworksColors: List<Color> = emptyList(),
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
            previewPath = "/themes/casino.png",
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
            previewPath = "/themes/raiffeisen.png",
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

        val ALL = listOf(CASINO, RAIFFEISEN)
    }
}
