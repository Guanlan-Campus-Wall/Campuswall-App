package xyz.zongtech.campuswall.glass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
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
import com.kyant.shapes.RoundedRectangle
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 液态玻璃面板。面板会把自身导出为新的背景，面板内部的按钮、开关、输入框继续折射这块玻璃。
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    onClick: (() -> Unit)? = null,
    tint: Color = Color.Unspecified,
    strong: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val backdrop = LocalBackdrop.current
    val colors = Glass.colors
    val exported = rememberLayerBackdrop()
    val scope = rememberCoroutineScope()
    val highlight = remember(scope) { InteractiveHighlight(scope) }
    Column(
        modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(cornerRadius) },
                effects = {
                    vibrancy()
                    blur(14.dp.toPx())
                    lens(14.dp.toPx(), 28.dp.toPx())
                },
                shadow = { Shadow(radius = 18.dp, color = Color.Black.copy(alpha = 0.07f)) },
                layerBlock =
                    if (onClick != null) {
                        {
                            val s = lerp(1f, 1f + 6.dp.toPx() / size.width, highlight.pressProgress)
                            scaleX = s
                            scaleY = s
                        }
                    } else null,
                exportedBackdrop = exported,
                onDrawSurface = {
                    drawRect(if (strong) colors.panelStrong else colors.panel)
                    if (tint.isSpecified) drawRect(tint.copy(alpha = 0.16f))
                },
            )
            .then(
                if (onClick != null)
                    Modifier.clickable(interactionSource = null, indication = null, onClick = onClick)
                        .then(highlight.modifier)
                        .then(highlight.gestureModifier)
                else Modifier
            )
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
    ) {
        CompositionLocalProvider(LocalBackdrop provides exported) { content() }
    }
}

enum class GlassButtonStyle {
    Normal,
    Prominent,
    Danger,
}

/** 液态玻璃按钮：按压时放大，并跟随手指产生弹性位移与高光。 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: GlassButtonStyle = GlassButtonStyle.Normal,
    height: Dp = 48.dp,
    horizontalPadding: Dp = 18.dp,
    backdrop: Backdrop = LocalBackdrop.current,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = Glass.colors
    val scope = rememberCoroutineScope()
    val highlight = remember(scope) { InteractiveHighlight(scope) }
    val tint =
        when (style) {
            GlassButtonStyle.Normal -> Color.Unspecified
            GlassButtonStyle.Prominent -> colors.accent
            GlassButtonStyle.Danger -> colors.danger
        }
    val contentColor =
        when (style) {
            GlassButtonStyle.Normal -> colors.content
            GlassButtonStyle.Prominent -> colors.onAccent
            GlassButtonStyle.Danger -> Color.White
        }
    Row(
        modifier
            .alpha(if (enabled) 1f else 0.45f)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(2.dp.toPx())
                    lens(12.dp.toPx(), 24.dp.toPx())
                },
                shadow = { Shadow(radius = 10.dp, color = Color.Black.copy(alpha = 0.06f)) },
                layerBlock =
                    if (enabled) {
                        {
                            val width = size.width
                            val h = size.height
                            val progress = highlight.pressProgress
                            val scale = lerp(1f, 1f + 4.dp.toPx() / h, progress)
                            val maxOffset = size.minDimension
                            val offset = highlight.offset
                            translationX = maxOffset * tanh(0.05f * offset.x / maxOffset)
                            translationY = maxOffset * tanh(0.05f * offset.y / maxOffset)
                            val maxDragScale = 4.dp.toPx() / h
                            val angle = atan2(offset.y, offset.x)
                            scaleX =
                                scale +
                                    maxDragScale * abs(cos(angle) * offset.x / size.maxDimension) *
                                        (width / h).fastCoerceAtMost(1f)
                            scaleY =
                                scale +
                                    maxDragScale * abs(sin(angle) * offset.y / size.maxDimension) *
                                        (h / width).fastCoerceAtMost(1f)
                        }
                    } else null,
                onDrawSurface = {
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.78f))
                    } else {
                        drawRect(colors.panel.copy(alpha = colors.panel.alpha * 0.7f))
                    }
                },
            )
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .then(if (enabled) highlight.modifier.then(highlight.gestureModifier) else Modifier)
            .height(height)
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) { content() }
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: GlassButtonStyle = GlassButtonStyle.Normal,
    icon: ImageVector? = null,
    height: Dp = 48.dp,
) {
    GlassButton(onClick, modifier, enabled, style, height) {
        icon?.let { Icon(it, null, Modifier.size(20.dp)) }
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 圆形玻璃图标按钮。 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: GlassButtonStyle = GlassButtonStyle.Normal,
    size: Dp = 44.dp,
    iconTint: Color = Color.Unspecified,
    backdrop: Backdrop = LocalBackdrop.current,
) {
    GlassButton(
        onClick = onClick,
        modifier = modifier.width(size).semantics { contentDescription?.let { this.contentDescription = it } },
        enabled = enabled,
        style = style,
        height = size,
        horizontalPadding = 0.dp,
        backdrop = backdrop,
    ) {
        Icon(
            icon,
            null,
            Modifier.size(size * 0.48f),
            tint = if (iconTint.isSpecified) iconTint else LocalContentColor.current,
        )
    }
}

/** 胶囊形玻璃筛选标签，选中时以主题色染色。 */
@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = Glass.colors
    val backdrop = LocalBackdrop.current
    val scope = rememberCoroutineScope()
    val highlight = remember(scope) { InteractiveHighlight(scope) }
    Row(
        modifier
            .alpha(if (enabled) 1f else 0.45f)
            .semantics { stateDescription = if (selected) "已选择" else "未选择" }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(2.dp.toPx())
                    lens(8.dp.toPx(), 16.dp.toPx())
                },
                shadow = null,
                layerBlock = {
                    val s = lerp(1f, 1f + 4.dp.toPx() / size.height, highlight.pressProgress)
                    scaleX = s
                    scaleY = s
                },
                onDrawSurface = {
                    if (selected) {
                        drawRect(colors.accent, blendMode = BlendMode.Hue)
                        drawRect(colors.accent.copy(alpha = 0.8f))
                    } else drawRect(colors.panel)
                },
            )
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .then(highlight.modifier)
            .then(highlight.gestureModifier)
            .height(36.dp)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val color = if (selected) colors.onAccent else colors.content
        icon?.let { Icon(it, null, Modifier.size(16.dp), tint = color) }
        Text(label, style = MaterialTheme.typography.labelLarge, color = color, maxLines = 1)
    }
}

/** 一组互斥选项，以玻璃标签展示。 */
@Composable
fun GlassChoiceRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            GlassChip(label, selected == value, { onSelect(value) }, enabled = enabled)
        }
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
    val colors = Glass.colors
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
                .drawBehind { drawRect(lerp(colors.track, colors.accent, drag.value)) }
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

/** 标题 + 说明 + 玻璃开关的设置行，整行可点按。 */
@Composable
fun GlassSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    enabled: Boolean = true,
) {
    Row(modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        // 只有文字区域可点按；开关自身处理点按和拖动，避免一次点按触发两次切换。
        Column(
            Modifier.weight(1f)
                .clip(RoundedRectangle(12.dp))
                .clickable(enabled = enabled, role = Role.Switch) { onCheckedChange(!checked) }
                .padding(end = 12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            supporting?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = Glass.colors.secondary)
            }
        }
        GlassToggle(checked, onCheckedChange, enabled = enabled, contentDescription = title)
    }
}

/** 玻璃输入框。 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    secret: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = Glass.colors
    val backdrop = LocalBackdrop.current
    var focused by remember { mutableStateOf(false) }
    var reveal by remember { mutableStateOf(false) }
    val radius = if (singleLine) 26.dp else 22.dp
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        label?.let {
            Text(
                it,
                Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = colors.secondary,
            )
        }
        Row(
            Modifier.fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedRectangle(radius) },
                    effects = {
                        vibrancy()
                        blur(4.dp.toPx())
                        lens(8.dp.toPx(), 16.dp.toPx())
                    },
                    shadow = null,
                    innerShadow = { InnerShadow(radius = 6.dp, alpha = 0.35f) },
                    onDrawSurface = { drawRect(colors.panel) },
                )
                .then(
                    if (focused) Modifier.border(1.5.dp, colors.accent.copy(0.7f), RoundedRectangle(radius))
                    else Modifier
                )
                .heightIn(min = 52.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            leadingIcon?.let { Icon(it, null, Modifier.size(20.dp), tint = colors.secondary) }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier =
                    Modifier.weight(1f).onFocusChanged { focused = it.isFocused }.semantics {
                        if (label != null) contentDescription = label
                    },
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                minLines = if (singleLine) 1 else minLines,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.content),
                cursorBrush = SolidColor(colors.accent),
                visualTransformation =
                    if (secret && !reveal) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty() && placeholder != null)
                            Text(
                                placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.secondary.copy(alpha = 0.8f),
                            )
                        inner()
                    }
                },
            )
            if (secret)
                Icon(
                    if (reveal) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    if (reveal) "隐藏密码" else "显示密码",
                    Modifier.size(22.dp).clip(Capsule()).clickable { reveal = !reveal },
                    tint = colors.secondary,
                )
            trailing?.invoke()
        }
    }
}

/** 加载中的玻璃胶囊。 */
@Composable
fun GlassLoading(modifier: Modifier = Modifier, text: String = "正在加载…") {
    val colors = Glass.colors
    Row(
        modifier
            .drawBackdrop(
                backdrop = LocalBackdrop.current,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(8.dp.toPx())
                    lens(10.dp.toPx(), 20.dp.toPx())
                },
                onDrawSurface = { drawRect(colors.panelStrong) },
            )
            .height(44.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CircularProgressIndicator(Modifier.size(18.dp), color = colors.accent, strokeWidth = 2.5.dp)
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** 细长的玻璃进度条，不确定进度时来回流动。 */
@Composable
fun GlassProgressBar(modifier: Modifier = Modifier) {
    val colors = Glass.colors
    val transition = rememberInfiniteTransition(label = "progress")
    val position by
        transition.animateFloat(
            0f,
            1f,
            infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
            label = "position",
        )
    Box(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .drawBackdrop(
                backdrop = LocalBackdrop.current,
                shape = { Capsule() },
                effects = { blur(4.dp.toPx()) },
                shadow = null,
                onDrawSurface = { drawRect(colors.track) },
            )
            .drawBehind {
                val w = size.width * 0.35f
                val x = -w + (size.width + w) * position
                drawRoundRect(
                    colors.accent,
                    topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                    size = androidx.compose.ui.geometry.Size(w, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
                )
            }
    )
}

/** 区块标题。 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        action?.invoke()
    }
}

@Composable
fun VSpace(height: Dp) = Spacer(Modifier.height(height))
