package xyz.zongtech.campuswall.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.zongtech.campuswall.ui.W
import xyz.zongtech.campuswall.ui.WebColors

/*
 * 液态玻璃只作为点缀：底部输入条、悬浮按钮和开关。
 * 页面主体使用与网页一致的纸面卡片，避免大量实时模糊拖慢滚动。
 */

/** 悬浮玻璃元素折射的背景；页面内部默认没有可折射的背景。 */
val LocalBackdrop = staticCompositionLocalOf<Backdrop> { emptyBackdrop() }

private fun WebColors.track() = if (dark) Color(0xFF787880).copy(0.36f) else Color(0xFF787878).copy(0.2f)

/** 悬浮的液态玻璃胶囊，按压时放大并产生高光。 */
@Composable
fun GlassCapsule(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    contentDescription: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = W
    val scope = rememberCoroutineScope()
    val highlight = remember(scope) { InteractiveHighlight(scope) }
    Row(
        modifier
            .semantics { contentDescription?.let { this.contentDescription = it } }
            .drawBackdrop(
                backdrop = LocalBackdrop.current,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(10.dp.toPx())
                    lens(18.dp.toPx(), 32.dp.toPx())
                },
                shadow = { Shadow(radius = 20.dp, color = Color.Black.copy(alpha = 0.12f)) },
                layerBlock = {
                    val s = lerp(1f, 1f + 4.dp.toPx() / size.height, highlight.pressProgress)
                    scaleX = s
                    scaleY = s
                },
                onDrawSurface = { drawRect(colors.surface.copy(alpha = if (colors.dark) 0.55f else 0.6f)) },
            )
            .clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onClick)
            .then(highlight.modifier)
            .then(highlight.gestureModifier)
            .height(height),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

/** 圆形悬浮玻璃按钮（回到顶部等）。 */
@Composable
fun GlassCircle(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier, size: Dp = 44.dp, content: @Composable () -> Unit) {
    GlassCapsule(onClick, modifier.width(size), height = size, contentDescription = contentDescription) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { content() }
    }
}

/** 液态玻璃开关：可点按，也可拖动滑块，按下时滑块变成透明的玻璃透镜。 */
@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val colors = W
    val backdrop = LocalBackdrop.current
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val dragWidth = with(density) { 20.dp.toPx() }
    val scope = rememberCoroutineScope()
    val currentChecked by rememberUpdatedState(checked)
    val onChange by rememberUpdatedState(onCheckedChange)
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(if (checked) 1f else 0f) }
    val drag = remember(scope) {
        DampedDragAnimation(
            animationScope = scope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.5f,
            onDragStarted = {},
            onDragStopped = {
                if (didDrag) {
                    fraction = if (targetValue >= 0.5f) 1f else 0f
                    didDrag = false
                } else {
                    fraction = if (currentChecked) 0f else 1f
                }
                if ((fraction == 1f) != currentChecked) onChange(fraction == 1f)
                // 外部拒绝了这次切换（例如未授予通知权限）时，滑块回到真实状态。
                scope.launch {
                    delay(150)
                    val actual = if (currentChecked) 1f else 0f
                    if (fraction != actual) fraction = actual
                }
            },
            onDrag = { _, amount ->
                if (!didDrag) didDrag = amount.x != 0f
                val delta = amount.x / dragWidth
                fraction = (if (isLtr) fraction + delta else fraction - delta).fastCoerceIn(0f, 1f)
            },
        )
    }
    LaunchedEffect(drag) { snapshotFlow { fraction }.collectLatest { drag.updateValue(it) } }
    LaunchedEffect(checked) {
        val target = if (checked) 1f else 0f
        if (target != fraction) {
            fraction = target
            drag.animateToValue(target)
        }
    }
    val trackBackdrop = rememberLayerBackdrop()
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.4f)
            .semantics {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
                contentDescription?.let { this.contentDescription = it }
                if (enabled)
                    onClick {
                        onChange(!currentChecked)
                        true
                    }
            }
            .size(64.dp, 28.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier.layerBackdrop(trackBackdrop)
                .clip(Capsule())
                .drawBehind { drawRect(lerp(colors.track(), colors.accent, drag.value)) }
                .size(64.dp, 28.dp)
        )
        Box(
            Modifier.graphicsLayer {
                    val padding = 2.dp.toPx()
                    translationX =
                        if (isLtr) lerp(padding, padding + dragWidth, drag.value)
                        else lerp(-padding, -(padding + dragWidth), drag.value)
                }
                .then(if (enabled) drag.modifier else Modifier)
                .drawBackdrop(
                    backdrop =
                        rememberCombinedBackdrop(
                            backdrop,
                            rememberBackdrop(trackBackdrop) { drawTrack ->
                                val progress = drag.pressProgress
                                scale(lerp(2f / 3f, 0.75f, progress), lerp(0f, 0.75f, progress)) {
                                    drawTrack()
                                }
                            },
                        ),
                    shape = { Capsule() },
                    effects = {
                        val progress = drag.pressProgress
                        blur(8.dp.toPx() * (1f - progress))
                        lens(5.dp.toPx() * progress, 10.dp.toPx() * progress, chromaticAberration = true)
                    },
                    highlight = {
                        Highlight.Ambient.copy(
                            width = Highlight.Ambient.width / 1.5f,
                            blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                            alpha = drag.pressProgress,
                        )
                    },
                    shadow = { Shadow(radius = 4.dp, color = Color.Black.copy(alpha = 0.08f)) },
                    innerShadow = {
                        InnerShadow(radius = 4.dp * drag.pressProgress, alpha = drag.pressProgress)
                    },
                    layerBlock = {
                        scaleX = drag.scaleX
                        scaleY = drag.scaleY
                        val velocity = drag.velocity / 50f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 1f - drag.pressProgress)) },
                )
                .size(40.dp, 24.dp)
        )
    }
}

