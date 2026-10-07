package xyz.zongtech.campuswall.glass

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.RoundedRectangle

@Stable
class OverlayEntry {
    var content: @Composable () -> Unit by mutableStateOf({})
    var onDismiss: () -> Unit by mutableStateOf({})
    var dismissible: Boolean by mutableStateOf(true)
}

/**
 * 弹窗统一渲染在页面内容图层之外，这样弹窗玻璃可以折射并模糊整个页面，
 * 而不需要另开一个系统窗口。
 */
@Stable
class OverlayHost {
    internal val entries = mutableStateListOf<OverlayEntry>()
}

val LocalOverlayHost = staticCompositionLocalOf { OverlayHost() }

@Composable
fun OverlayLayer(host: OverlayHost, backdrop: Backdrop) {
    host.entries.forEachIndexed { index, entry ->
        key(entry) { OverlayFrame(entry, backdrop, index == host.entries.lastIndex) }
    }
}

@Composable
private fun OverlayFrame(entry: OverlayEntry, backdrop: Backdrop, top: Boolean) {
    val colors = Glass.colors
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, spring(0.75f, 420f)) }
    BackHandler(enabled = top) { if (entry.dismissible) entry.onDismiss() }
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize()
                .graphicsLayer { alpha = appear.value.coerceIn(0f, 1f) }
                .background(colors.dim)
                .clickable(interactionSource = null, indication = null) {
                    if (entry.dismissible) entry.onDismiss()
                }
        )
        Box(
            Modifier.align(Alignment.Center)
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .graphicsLayer {
                    val p = appear.value
                    alpha = p.coerceIn(0f, 1f)
                    scaleX = lerp(0.88f, 1f, p)
                    scaleY = lerp(0.88f, 1f, p)
                }
        ) {
            CompositionLocalProvider(LocalBackdrop provides backdrop) { entry.content() }
        }
    }
}

/** 液态玻璃弹窗。弹窗内容默认可滚动。 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    dismissible: Boolean = true,
    scrollable: Boolean = true,
    buttons: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val host = LocalOverlayHost.current
    val entry = remember { OverlayEntry() }
    SideEffect {
        entry.onDismiss = onDismissRequest
        entry.dismissible = dismissible
        entry.content = { DialogCard(title, scrollable, buttons, content) }
    }
    DisposableEffect(host, entry) {
        host.entries.add(entry)
        onDispose { host.entries.remove(entry) }
    }
}

@Composable
private fun DialogCard(
    title: String?,
    scrollable: Boolean,
    buttons: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = Glass.colors
    val exported = rememberLayerBackdrop()
    BoxWithConstraints {
        Column(
            Modifier.widthIn(max = 480.dp)
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .semantics { dialog() }
                .clickable(interactionSource = null, indication = null) {}
                .drawBackdrop(
                    backdrop = LocalBackdrop.current,
                    shape = { RoundedRectangle(36.dp) },
                    effects = {
                        colorControls(brightness = if (colors.dark) 0f else 0.15f, saturation = 1.5f)
                        blur(if (colors.dark) 10.dp.toPx() else 18.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    exportedBackdrop = exported,
                    onDrawSurface = { drawRect(colors.panelStrong) },
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CompositionLocalProvider(LocalBackdrop provides exported) {
                title?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
                Column(
                    Modifier.weight(1f, fill = false)
                        .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content,
                )
                buttons?.invoke()
            }
        }
    }
}

/** 确认弹窗：取消 + 确认两个玻璃按钮。 */
@Composable
fun GlassConfirmDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    text: String? = null,
    confirmText: String = "确认",
    dismissText: String = "取消",
    danger: Boolean = false,
    confirmEnabled: Boolean = true,
) {
    GlassDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        buttons = {
            DialogButtons(
                confirmText = confirmText,
                onConfirm = onConfirm,
                dismissText = dismissText,
                onDismiss = onDismissRequest,
                danger = danger,
                confirmEnabled = confirmEnabled,
            )
        },
    ) {
        text?.let { Text(it, style = MaterialTheme.typography.bodyLarge, color = Glass.colors.secondary) }
    }
}

@Composable
fun DialogButtons(
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String = "取消",
    onDismiss: () -> Unit,
    danger: Boolean = false,
    confirmEnabled: Boolean = true,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassButton(dismissText, onDismiss, Modifier.weight(1f))
        GlassButton(
            confirmText,
            onConfirm,
            Modifier.weight(1f),
            enabled = confirmEnabled,
            style = if (danger) GlassButtonStyle.Danger else GlassButtonStyle.Prominent,
        )
    }
}
