package xyz.zongtech.campuswall.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 与网页版一致的设计令牌（frontend/src/styles/tokens.css）：
 * 象牙白纸面、近黑墨色、强调色可选，几乎不用阴影，靠 1px 暖灰描边分层。
 */
@Immutable
data class WebColors(
    val dark: Boolean,
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val surface4: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val line: Color,
    val lineStrong: Color,
    val success: Color,
    val successSoft: Color,
    val successLine: Color,
    val warning: Color,
    val warningSoft: Color,
    val warningLine: Color,
    val danger: Color,
    val dangerSoft: Color,
    val dangerLine: Color,
    val accent: Color,
    val accentInk: Color,
    val accentStrong: Color,
    val accentSoft: Color,
    val accentSoft2: Color,
    val accentLine: Color,
    val overlay: Color,
    val toastBg: Color,
    val toastFg: Color,
    val notes: List<Color>,
    val noteInk: Color,
    val tones: List<Pair<Color, Color>>,
)

data class Palette(val id: String, val label: String, val swatch: Color)

val palettes =
    listOf(
        Palette("clay", "赤陶", Color(0xFFB85636)),
        Palette("sky", "天青", Color(0xFF2F6A99)),
        Palette("olive", "橄榄", Color(0xFF566A3C)),
        Palette("fig", "无花果", Color(0xFFA8476B)),
        Palette("slate", "石墨", Color(0xFF2B2B28)),
    )

private fun accent(palette: String, dark: Boolean): List<Long> =
    if (!dark)
        when (palette) {
            "sky" -> listOf(0xFF2F6A99, 0xFFFFFFFF, 0xFF245A85, 0xFFE8F0F7, 0xFFD5E5F2, 0xFFB2CCE2)
            "olive" -> listOf(0xFF566A3C, 0xFFFFFFFF, 0xFF455732, 0xFFEDF1E3, 0xFFDEE6CC, 0xFFBFCEA3)
            "fig" -> listOf(0xFFA8476B, 0xFFFFFFFF, 0xFF8A3556, 0xFFF8E8EE, 0xFFF0D3DE, 0xFFE2B0C3)
            "slate" -> listOf(0xFF2B2B28, 0xFFFAF9F5, 0xFF141413, 0xFFEFEDE5, 0xFFE8E6DC, 0xFFD3CFC2)
            else -> listOf(0xFFB85636, 0xFFFFFFFF, 0xFF9C4526, 0xFFF8EBE4, 0xFFF2DACE, 0xFFE6BBA6)
        }
    else
        when (palette) {
            "sky" -> listOf(0xFF7FB0DE, 0xFF14202B, 0xFFA6C9E8, 0xFF26384A, 0xFF2E445A, 0xFF43607E)
            "olive" -> listOf(0xFF9BB07A, 0xFF1B2112, 0xFFB6C898, 0xFF303A24, 0xFF3A472B, 0xFF55683F)
            "fig" -> listOf(0xFFDC86A4, 0xFF2A121B, 0xFFEAA7BE, 0xFF44292F, 0xFF53333B, 0xFF7D4A5B)
            "slate" -> listOf(0xFFE8E6DC, 0xFF141413, 0xFFFAF9F5, 0xFF3A3935, 0xFF43423D, 0xFF5A5851)
            else -> listOf(0xFFE08A6A, 0xFF1A1918, 0xFFEBA88F, 0xFF43302A, 0xFF523A31, 0xFF7A5242)
        }

fun webColors(dark: Boolean, palette: String): WebColors {
    val a = accent(palette, dark).map { Color(it) }
    return if (!dark)
        WebColors(
            dark = false,
            bg = Color(0xFFFAF9F5),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFF5F4ED),
            surface3 = Color(0xFFF0EEE6),
            surface4 = Color(0xFFE8E6DC),
            ink = Color(0xFF141413),
            ink2 = Color(0xFF3D3D3A),
            ink3 = Color(0xFF66655E),
            line = Color(0xFFE8E6DC),
            lineStrong = Color(0xFFD3CFC2),
            success = Color(0xFF4F6B2E),
            successSoft = Color(0xFFEEF2E4),
            successLine = Color(0xFFCDD9B3),
            warning = Color(0xFF9A5B0C),
            warningSoft = Color(0xFFFBF0DD),
            warningLine = Color(0xFFECD2A2),
            danger = Color(0xFFB3261E),
            dangerSoft = Color(0xFFFBE9E7),
            dangerLine = Color(0xFFF0C4BF),
            accent = a[0],
            accentInk = a[1],
            accentStrong = a[2],
            accentSoft = a[3],
            accentSoft2 = a[4],
            accentLine = a[5],
            overlay = Color(0x75141413),
            toastBg = Color(0xFF141413),
            toastFg = Color(0xFFFAF9F5),
            notes = listOf(0xFFF2E3BF, 0xFFF3D6CD, 0xFFD6E4EF, 0xFFDFE6CF, 0xFFE2DFF0).map { Color(it) },
            noteInk = Color(0xFF2A2620),
            tones =
                listOf(
                    Color(0xFFB85636) to Color(0xFFF8E9E1),
                    Color(0xFF2F6A99) to Color(0xFFE5EEF6),
                    Color(0xFF566A3C) to Color(0xFFEBF0DF),
                    Color(0xFFA8476B) to Color(0xFFF7E6EC),
                    Color(0xFF8F5E1E) to Color(0xFFF6EAD3),
                    Color(0xFF5E5A8C) to Color(0xFFEAE9F3),
                ),
        )
    else
        WebColors(
            dark = true,
            bg = Color(0xFF262624),
            surface = Color(0xFF30302E),
            surface2 = Color(0xFF3A3935),
            surface3 = Color(0xFF43423D),
            surface4 = Color(0xFF4D4C46),
            ink = Color(0xFFFAF9F5),
            ink2 = Color(0xFFC2C0B6),
            ink3 = Color(0xFFA8A69C),
            line = Color(0xFF3F3E3A),
            lineStrong = Color(0xFF55534D),
            success = Color(0xFFA3BE7E),
            successSoft = Color(0xFF2C3322),
            successLine = Color(0xFF48553A),
            warning = Color(0xFFE0A553),
            warningSoft = Color(0xFF3A2F1B),
            warningLine = Color(0xFF5E4A28),
            danger = Color(0xFFF28B82),
            dangerSoft = Color(0xFF3F2623),
            dangerLine = Color(0xFF6C3A35),
            accent = a[0],
            accentInk = a[1],
            accentStrong = a[2],
            accentSoft = a[3],
            accentSoft2 = a[4],
            accentLine = a[5],
            overlay = Color(0x9E0A0A09),
            toastBg = Color(0xFFFAF9F5),
            toastFg = Color(0xFF141413),
            notes = listOf(0xFF4D4328, 0xFF4F3029, 0xFF2A3F52, 0xFF36432A, 0xFF3F3A58).map { Color(it) },
            noteInk = Color(0xFFF6F0E2),
            tones =
                listOf(
                    Color(0xFFE6957A) to Color(0xFF43302A),
                    Color(0xFF86B6E0) to Color(0xFF26384A),
                    Color(0xFFA9C17F) to Color(0xFF303A24),
                    Color(0xFFE393B0) to Color(0xFF44292F),
                    Color(0xFFE3B36A) to Color(0xFF403421),
                    Color(0xFFB6B2E0) to Color(0xFF34324E),
                ),
        )
}

val LocalWeb = staticCompositionLocalOf { webColors(false, "clay") }

/** 当前主题色板。 */
val W: WebColors
    @Composable get() = LocalWeb.current

/** 标题使用衬线体，与网页的 --font-serif 对应。 */
val Serif: FontFamily = FontFamily.Serif

private val baseTypography = Typography()

private val webTypography =
    baseTypography.copy(
        displaySmall = TextStyle(fontFamily = Serif, fontSize = 34.sp, lineHeight = 42.sp, fontWeight = FontWeight.Normal),
        headlineLarge = TextStyle(fontFamily = Serif, fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Normal),
        headlineMedium = TextStyle(fontFamily = Serif, fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Normal),
        headlineSmall = TextStyle(fontFamily = Serif, fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
        titleLarge = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
        titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 28.sp),
        bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
        bodySmall = TextStyle(fontSize = 13.5.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        labelMedium = TextStyle(fontSize = 13.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        labelSmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    )

@Composable
fun WebTheme(dark: Boolean, palette: String, content: @Composable () -> Unit) {
    val c = webColors(dark, palette)
    val scheme =
        if (dark)
            darkColorScheme(
                primary = c.accent,
                onPrimary = c.accentInk,
                background = c.bg,
                surface = c.surface,
                surfaceContainerHigh = c.surface,
                onSurface = c.ink,
                onSurfaceVariant = c.ink2,
                outline = c.lineStrong,
                outlineVariant = c.line,
                error = c.danger,
            )
        else
            lightColorScheme(
                primary = c.accent,
                onPrimary = c.accentInk,
                background = c.bg,
                surface = c.surface,
                surfaceContainerHigh = c.surface,
                onSurface = c.ink,
                onSurfaceVariant = c.ink2,
                outline = c.lineStrong,
                outlineVariant = c.line,
                error = c.danger,
            )
    MaterialTheme(colorScheme = scheme, typography = webTypography) {
        CompositionLocalProvider(LocalWeb provides c, LocalContentColor provides c.ink, content = content)
    }
}
