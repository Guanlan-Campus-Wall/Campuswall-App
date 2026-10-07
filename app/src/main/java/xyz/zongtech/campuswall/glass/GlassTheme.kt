package xyz.zongtech.campuswall.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.isRenderEffectSupported

/** 液态玻璃界面的配色。玻璃面板的透明度会根据设备是否支持实时模糊自动调整。 */
@Immutable
data class GlassColors(
    val dark: Boolean,
    val accent: Color,
    val onAccent: Color,
    val content: Color,
    val secondary: Color,
    val panel: Color,
    val panelStrong: Color,
    val track: Color,
    val outline: Color,
    val danger: Color,
    val love: Color,
    val dim: Color,
    val wallpaperBase: List<Color>,
    val wallpaperBlobs: List<Color>,
)

fun glassColors(scheme: ColorScheme, dark: Boolean): GlassColors {
    val blur = isRenderEffectSupported()
    val panelBase = if (dark) Color(0xFF16181C) else Color(0xFFFBFCFD)
    return GlassColors(
        dark = dark,
        accent = scheme.primary,
        onAccent = scheme.onPrimary,
        content = if (dark) Color(0xFFF2F4F5) else Color(0xFF15191C),
        secondary = if (dark) Color(0xFFB4BBC0) else Color(0xFF5B656C),
        panel = panelBase.copy(alpha = if (blur) (if (dark) 0.42f else 0.5f) else 0.9f),
        panelStrong = panelBase.copy(alpha = if (blur) (if (dark) 0.62f else 0.72f) else 0.95f),
        track = if (dark) Color(0xFF787880).copy(0.36f) else Color(0xFF787878).copy(0.2f),
        outline = if (dark) Color.White.copy(0.12f) else Color.Black.copy(0.08f),
        danger = if (dark) Color(0xFFFF6961) else Color(0xFFD70015),
        love = Color(0xFFE0457B),
        dim = if (dark) Color.Black.copy(0.5f) else Color(0xFF29293A).copy(0.25f),
        wallpaperBase =
            if (dark)
                listOf(
                    Color(0xFF0B0E12),
                    scheme.primary.copy(0.18f).compositeOver(Color(0xFF0E1216)),
                )
            else
                listOf(
                    scheme.primaryContainer.copy(0.55f).compositeOver(Color(0xFFF4F6F7)),
                    Color(0xFFF7F4F1),
                ),
        wallpaperBlobs =
            listOf(
                scheme.primary.copy(if (dark) 0.55f else 0.45f),
                scheme.tertiary.copy(if (dark) 0.45f else 0.35f),
                scheme.secondary.copy(if (dark) 0.4f else 0.3f),
                Color(0xFFE0457B).copy(if (dark) 0.3f else 0.22f),
            ),
    )
}

val LocalGlassColors = staticCompositionLocalOf { glassColors(androidx.compose.material3.lightColorScheme(), false) }

/** 当前玻璃组件折射的背景。玻璃面板会把自身导出为新的背景，供内部组件继续折射。 */
val LocalBackdrop = staticCompositionLocalOf<Backdrop> { emptyBackdrop() }

object Glass {
    val colors: GlassColors
        @Composable get() = LocalGlassColors.current

    val backdrop: Backdrop
        @Composable get() = LocalBackdrop.current
}

private val GlassTypography =
    Typography().let { base ->
        base.copy(
            headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
            headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
            titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            bodyLarge = base.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
        )
    }

@Composable
fun GlassTheme(scheme: ColorScheme, dark: Boolean, content: @Composable () -> Unit) {
    val colors = glassColors(scheme, dark)
    MaterialTheme(colorScheme = scheme, typography = GlassTypography) {
        CompositionLocalProvider(
            LocalGlassColors provides colors,
            LocalContentColor provides colors.content,
            content = content,
        )
    }
}

/** 静态的柔光背景，给液态玻璃提供可折射的色彩。保持静止以节省电量。 */
@Composable
fun GlassWallpaper(modifier: Modifier = Modifier) {
    val colors = Glass.colors
    Canvas(modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(colors.wallpaperBase))
        val spots =
            listOf(
                Offset(0.05f, 0.08f) to 0.75f,
                Offset(1.0f, 0.32f) to 0.65f,
                Offset(0.1f, 0.72f) to 0.7f,
                Offset(0.9f, 0.95f) to 0.6f,
            )
        spots.forEachIndexed { index, (center, radius) ->
            val c = Offset(center.x * size.width, center.y * size.height)
            val r = radius * size.width
            drawCircle(
                Brush.radialGradient(
                    listOf(colors.wallpaperBlobs[index], Color.Transparent),
                    center = c,
                    radius = r,
                ),
                radius = r,
                center = c,
            )
        }
    }
}

val TextStyle.secondary: TextStyle
    @Composable get() = copy(color = Glass.colors.secondary)
