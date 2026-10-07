/*
 * 改写自 Kyant0/AndroidLiquidGlass 的 LiquidBottomTabs 示例（Apache License 2.0）。
 */
package xyz.zongtech.campuswall.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
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
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

private val LocalTabScale = staticCompositionLocalOf { { 1f } }

/**
 * 液态玻璃标签栏：选中指示器是一块可拖动的玻璃透镜，透过它能看到主题色的选中内容。
 * 用作底部导航栏，也用作页面里的分段选择器。
 */
@Composable
fun GlassTabs(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    count: Int,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    backdrop: Backdrop = LocalBackdrop.current,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = Glass.colors
    val accent = colors.accent
    val container = colors.panel
    val tabsBackdrop = rememberLayerBackdrop()
    val inner = height - 8.dp
    BoxWithConstraints(modifier, contentAlignment = Alignment.CenterStart) {
        val density = LocalDensity.current
        val tabWidth by
            rememberUpdatedState(with(density) { (constraints.maxWidth.toFloat() - 8.dp.toPx()) / count })
        val maxWidth by rememberUpdatedState(constraints.maxWidth.toFloat())
        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by
            remember(density) {
                derivedStateOf {
                    val fraction = (offsetAnimation.value / maxWidth).fastCoerceIn(-1f, 1f)
                    with(density) { 4.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction)) }
                }
            }
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val scope = rememberCoroutineScope()
        val latestSelected by rememberUpdatedState(selectedIndex)
        val latestOnSelect by rememberUpdatedState(onSelect)
        var currentIndex by remember { mutableIntStateOf(selectedIndex) }
        val drag = remember(scope, count) {
            DampedDragAnimation(
                animationScope = scope,
                initialValue = currentIndex.toFloat().coerceIn(0f, (count - 1).toFloat()),
                valueRange = 0f..(count - 1).coerceAtLeast(1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = 78f / 56f,
                onDragStarted = {},
                onDragStopped = {
                    val target = targetValue.fastRoundToInt().fastCoerceIn(0, count - 1)
                    currentIndex = target
                    animateToValue(target.toFloat())
                    scope.launch { offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f)) }
                },
                onDrag = { _, amount ->
                    updateValue(
                        (targetValue + amount.x / tabWidth * if (isLtr) 1f else -1f)
                            .fastCoerceIn(0f, (count - 1).toFloat())
                    )
                    scope.launch { offsetAnimation.snapTo(offsetAnimation.value + amount.x) }
                },
            )
        }
        LaunchedEffect(selectedIndex) { currentIndex = selectedIndex }
        LaunchedEffect(drag) {
            snapshotFlow { currentIndex }
                .drop(1)
                .collectLatest { index ->
                    drag.animateToValue(index.toFloat())
                    if (index != latestSelected) latestOnSelect(index)
                }
        }
        val highlight = remember(scope) {
            InteractiveHighlight(
                animationScope = scope,
                position = { size, _ ->
                    Offset(
                        if (isLtr) (drag.value + 0.5f) * tabWidth + panelOffset
                        else size.width - (drag.value + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f,
                    )
                },
            )
        }

        Row(
            Modifier.graphicsLayer { translationX = panelOffset }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        vibrancy()
                        blur(8.dp.toPx())
                        lens(24.dp.toPx(), 24.dp.toPx())
                    },
                    shadow = { Shadow(radius = 20.dp, color = Color.Black.copy(alpha = 0.1f)) },
                    layerBlock = {
                        val s = lerp(1f, 1f + 16.dp.toPx() / size.width, drag.pressProgress)
                        scaleX = s
                        scaleY = s
                    },
                    onDrawSurface = { drawRect(container) },
                )
                .then(highlight.modifier)
                .height(height)
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )

        CompositionLocalProvider(LocalTabScale provides { lerp(1f, 1.2f, drag.pressProgress) }) {
            Row(
                Modifier.clearAndSetSemantics {}
                    .alpha(0f)
                    .layerBackdrop(tabsBackdrop)
                    .graphicsLayer { translationX = panelOffset }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { Capsule() },
                        effects = {
                            val progress = drag.pressProgress
                            vibrancy()
                            blur(8.dp.toPx())
                            lens(24.dp.toPx() * progress, 24.dp.toPx() * progress)
                        },
                        highlight = { Highlight.Default.copy(alpha = drag.pressProgress) },
                        onDrawSurface = { drawRect(container) },
                    )
                    .then(highlight.modifier)
                    .height(inner)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .graphicsLayer(colorFilter = ColorFilter.tint(accent)),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }

        Box(
            Modifier.padding(horizontal = 4.dp)
                .graphicsLayer {
                    translationX =
                        if (isLtr) drag.value * tabWidth + panelOffset
                        else size.width - (drag.value + 1f) * tabWidth + panelOffset
                }
                .then(highlight.gestureModifier)
                .then(drag.modifier)
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                    shape = { Capsule() },
                    effects = {
                        val progress = drag.pressProgress
                        lens(10.dp.toPx() * progress, 14.dp.toPx() * progress, chromaticAberration = true)
                    },
                    highlight = { Highlight.Default.copy(alpha = drag.pressProgress) },
                    shadow = { Shadow(alpha = drag.pressProgress) },
                    innerShadow = {
                        InnerShadow(radius = 8.dp * drag.pressProgress, alpha = drag.pressProgress)
                    },
                    layerBlock = {
                        scaleX = drag.scaleX
                        scaleY = drag.scaleY
                        val velocity = drag.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = drag.pressProgress
                        drawRect(
                            if (colors.dark) Color.White.copy(0.1f) else Color.Black.copy(0.08f),
                            alpha = 1f - progress,
                        )
                        drawRect(Color.Black.copy(alpha = 0.03f * progress))
                    },
                )
                .height(inner)
                .fillMaxWidth(1f / count)
        )
    }
}

/** [GlassTabs] 中的一个标签。 */
@Composable
fun RowScope.GlassTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scale = LocalTabScale.current
    Column(
        modifier
            .clip(Capsule())
            .semantics { this.selected = selected }
            .clickable(interactionSource = null, indication = null, role = Role.Tab, onClick = onClick)
            .fillMaxHeight()
            .weight(1f)
            .graphicsLayer {
                val s = scale()
                scaleX = s
                scaleY = s
            },
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

/** 液态玻璃分段选择器。 */
@Composable
fun GlassSegmented(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val index = options.indexOfFirst { it.first == selected }.coerceAtLeast(0)
    GlassTabs(
        selectedIndex = index,
        onSelect = { onSelect(options[it].first) },
        count = options.size,
        modifier = modifier.fillMaxWidth(),
        height = 48.dp,
    ) {
        options.forEachIndexed { i, (value, label) ->
            GlassTab(selected = i == index, onClick = { onSelect(value) }) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
