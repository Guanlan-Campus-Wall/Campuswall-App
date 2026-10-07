@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package xyz.zongtech.campuswall.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.zongtech.campuswall.glass.GlassToggle

val RSm = RoundedCornerShape(10.dp)
val RMd = RoundedCornerShape(14.dp)
val RLg = RoundedCornerShape(18.dp)
val RXl = RoundedCornerShape(24.dp)
val Pill = RoundedCornerShape(999.dp)

/** `.card`：白色纸面 + 1px 暖灰描边。 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    color: Color = W.surface,
    border: Color = W.line,
    shape: RoundedCornerShape = RLg,
    spacing: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(shape)
            .background(color)
            .border(1.dp, border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

/** `.card-flat`：浅底描边小卡片。 */
@Composable
fun FlatCard(modifier: Modifier = Modifier, padding: PaddingValues = PaddingValues(12.dp), content: @Composable ColumnScope.() -> Unit) =
    Card(modifier, padding, color = W.surface2, shape = RMd, spacing = 6.dp, content = content)

enum class Btn { Primary, Outline, Ghost, Danger, Success, Ink, Soft }

/** `.btn` 及其变体。 */
@Composable
fun WButton(
    text: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: Btn = Btn.Outline,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    small: Boolean = false,
    large: Boolean = false,
    trailingIcon: ImageVector? = null,
) {
    val c = W
    val (bg, fg, bd) =
        when (kind) {
            Btn.Primary -> Triple(c.accent, c.accentInk, Color.Transparent)
            Btn.Outline -> Triple(Color.Transparent, c.ink, c.lineStrong)
            Btn.Ghost -> Triple(Color.Transparent, c.ink2, Color.Transparent)
            Btn.Danger -> Triple(c.danger, if (c.dark) Color(0xFF1A1918) else Color.White, Color.Transparent)
            Btn.Success -> Triple(c.success, if (c.dark) Color(0xFF1A1918) else Color.White, Color.Transparent)
            Btn.Ink -> Triple(c.ink, c.bg, Color.Transparent)
            Btn.Soft -> Triple(c.accentSoft, c.accentStrong, Color.Transparent)
        }
    val height = if (small) 36.dp else if (large) 50.dp else 44.dp
    Row(
        modifier
            .alpha(if (enabled) 1f else 0.5f)
            .heightIn(min = height)
            .clip(RSm)
            .background(bg)
            .border(1.dp, bd, RSm)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (text == null) 0.dp else if (small) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides fg) {
            icon?.let { Icon(it, null, Modifier.size(if (small) 16.dp else 18.dp)) }
            text?.let {
                Text(
                    it,
                    style = if (small) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            trailingIcon?.let { Icon(it, null, Modifier.size(16.dp)) }
        }
    }
}

/** `.btn-icon`：正方形图标按钮。 */
@Composable
fun IconBtn(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: Btn = Btn.Ghost,
    enabled: Boolean = true,
    size: Dp = 44.dp,
    tint: Color = Color.Unspecified,
) {
    val c = W
    val (bg, fg, bd) =
        when (kind) {
            Btn.Primary -> Triple(c.accent, c.accentInk, Color.Transparent)
            Btn.Outline -> Triple(Color.Transparent, c.ink, c.lineStrong)
            Btn.Danger -> Triple(c.dangerSoft, c.danger, c.dangerLine)
            else -> Triple(Color.Transparent, c.ink2, Color.Transparent)
        }
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.5f)
            .size(size)
            .clip(RSm)
            .background(bg)
            .border(1.dp, bd, RSm)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(size * 0.45f), tint = if (tint == Color.Unspecified) fg else tint)
    }
}

enum class Tone { Neutral, Success, Warning, Danger, Accent }

@Composable
private fun toneColors(tone: Tone): Triple<Color, Color, Color> {
    val c = W
    return when (tone) {
        Tone.Neutral -> Triple(c.ink2, c.surface3, Color.Transparent)
        Tone.Success -> Triple(c.success, c.successSoft, c.successLine)
        Tone.Warning -> Triple(c.warning, c.warningSoft, c.warningLine)
        Tone.Danger -> Triple(c.danger, c.dangerSoft, c.dangerLine)
        Tone.Accent -> Triple(c.accentStrong, c.accentSoft, c.accentLine)
    }
}

/** `.badge`。 */
@Composable
fun Badge(text: String, tone: Tone = Tone.Neutral, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    val (fg, bg, bd) = toneColors(tone)
    Row(
        modifier.clip(Pill).background(bg).border(1.dp, bd, Pill).padding(horizontal = 9.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon?.let { Icon(it, null, Modifier.size(13.dp), tint = fg) }
        Text(text, color = fg, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

/** `.tag`：话题标签。 */
@Composable
fun Tag(text: String, onClick: (() -> Unit)? = null) {
    Text(
        text,
        Modifier.clip(Pill)
            .background(W.surface3)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = W.ink2,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
    )
}

/** `.chip`：可选中的胶囊。 */
@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, icon: ImageVector? = null, count: Int? = null) {
    val c = W
    Row(
        Modifier.heightIn(min = 36.dp)
            .clip(Pill)
            .background(if (selected) c.accentSoft else Color.Transparent)
            .border(1.dp, if (selected) c.accentLine else c.lineStrong, Pill)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val fg = if (selected) c.accentStrong else c.ink2
        icon?.let { Icon(it, null, Modifier.size(15.dp), tint = fg) }
        Text(text, color = fg, style = MaterialTheme.typography.labelMedium)
        count?.let { Badge("$it") }
    }
}

/** `.seg`：分段选择器。 */
@Composable
fun Seg(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier, fill: Boolean = false) {
    val c = W
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface3)
            .then(if (fill) Modifier else Modifier.horizontalScroll(rememberScrollState()))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { (value, label) ->
            val on = value == selected
            Box(
                Modifier.then(if (fill) Modifier.weight(1f) else Modifier)
                    .heightIn(min = 34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (on) c.surface else Color.Transparent)
                    .then(if (on) Modifier.border(1.dp, c.line, RoundedCornerShape(9.dp)) else Modifier)
                    .clickable(role = Role.Tab) { onSelect(value) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = if (on) c.ink else c.ink2, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
        }
    }
}

/** 下拉选择（对应网页的 select）。 */
@Composable
fun Select(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        WButton(
            options.firstOrNull { it.first == selected }?.second ?: selected,
            { open = true },
            trailingIcon = Icons.Rounded.KeyboardArrowDown,
        )
        DropdownMenu(open, { open = false }, containerColor = W.surface) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label, color = W.ink) },
                    onClick = {
                        open = false
                        onSelect(value)
                    },
                    trailingIcon = { if (value == selected) Icon(Icons.Rounded.Check, null, tint = W.accent) },
                )
            }
        }
    }
}

/** `.field`：带标签的输入框。 */
@Composable
fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLength: Int? = null,
    secret: Boolean = false,
    enabled: Boolean = true,
    hint: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leading: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = W
    var focused by remember { mutableStateOf(false) }
    var reveal by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        label?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = c.ink2) }
        Row(
            Modifier.fillMaxWidth()
                .alpha(if (enabled) 1f else 0.6f)
                .clip(RMd)
                .background(c.surface)
                .border(if (focused) 1.5.dp else 1.dp, if (focused) c.accent else c.lineStrong, RMd)
                .heightIn(min = 48.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            leading?.let { Icon(it, null, Modifier.size(20.dp), tint = c.ink3) }
            BasicTextField(
                value = value,
                onValueChange = { onValueChange(if (maxLength != null) it.take(maxLength) else it) },
                modifier =
                    Modifier.weight(1f).onFocusChanged { focused = it.isFocused }.semantics {
                        (label ?: placeholder)?.let { contentDescription = it }
                    },
                enabled = enabled,
                singleLine = singleLine,
                minLines = if (singleLine) 1 else minLines,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.ink),
                cursorBrush = SolidColor(c.accent),
                visualTransformation = if (secret && !reveal) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty() && placeholder != null)
                            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = c.ink3)
                        inner()
                    }
                },
            )
            if (secret)
                Icon(
                    if (reveal) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    if (reveal) "隐藏密码" else "显示密码",
                    Modifier.size(22.dp).clip(CircleShape).clickable { reveal = !reveal },
                    tint = c.ink3,
                )
            trailing?.invoke()
        }
        if (hint != null || maxLength != null)
            Row {
                Text(hint.orEmpty(), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = c.ink3)
                if (maxLength != null && !singleLine)
                    Text("${value.length} / $maxLength", style = MaterialTheme.typography.labelSmall, color = c.ink3)
            }
    }
}

/** 网页顶部的搜索框：放大镜 + 输入 + 内嵌「搜索」按钮。 */
@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit, placeholder: String, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    val c = W
    Row(
        modifier.fillMaxWidth().clip(RMd).background(c.surface).border(1.dp, c.lineStrong, RMd).padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Search, null, Modifier.size(20.dp), tint = c.ink3)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).semantics { contentDescription = placeholder },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.ink),
            cursorBrush = SolidColor(c.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = c.ink3)
                    inner()
                }
            },
        )
        WButton("搜索", onSearch, kind = Btn.Primary, small = true)
    }
}

/** 开关行：标题 + 说明 + 液态玻璃开关。 */
@Composable
fun SwitchRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit, supporting: String? = null, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(
            Modifier.weight(1f).clip(RSm).clickable(enabled = enabled, role = Role.Switch) { onChange(!checked) }.padding(end = 12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = W.ink)
            supporting?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = W.ink3) }
        }
        GlassToggle(checked, onChange, enabled = enabled, contentDescription = title)
    }
}

/** 单选（网页里的 radio / select 选项）。 */
@Composable
fun Choices(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (v, l) -> Chip(l, v == selected, { onSelect(v) }) }
    }
}

@Composable
fun Spinner(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(28.dp), color = W.accent, strokeWidth = 2.5.dp, trackColor = W.surface3)
    }
}

/** `.tile-icon`：柔和底色的方形图标。 */
@Composable
fun TileIcon(icon: ImageVector, fg: Color = W.accent, bg: Color = W.accentSoft, size: Dp = 44.dp) {
    Box(Modifier.size(size).clip(RMd).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size(size * 0.5f), tint = fg)
    }
}

/** `.empty-state-card`。 */
@Composable
fun EmptyCard(
    title: String,
    text: String? = null,
    icon: ImageVector? = null,
    actionText: String? = null,
    action: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth(), padding = PaddingValues(horizontal = 24.dp, vertical = 36.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            icon?.let { TileIcon(it, size = 56.dp) }
            Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            text?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = W.ink2, textAlign = TextAlign.Center) }
            if (actionText != null && action != null) {
                Spacer(Modifier.height(4.dp))
                WButton(actionText, action, kind = Btn.Primary)
            }
        }
    }
}

/** `.info-callout`。 */
@Composable
fun Callout(text: String, tone: Tone = Tone.Neutral, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    val (fg, bg, bd) =
        if (tone == Tone.Neutral) Triple(W.accentStrong, W.surface2, W.line) else toneColors(tone)
    Row(
        modifier.fillMaxWidth().clip(RMd).background(bg).border(1.dp, bd, RMd).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon?.let { Icon(it, null, Modifier.padding(top = 2.dp).size(18.dp), tint = fg) }
        Text(text, style = MaterialTheme.typography.bodySmall, color = W.ink)
    }
}

/** `.page-head`：返回链接 + 衬线大标题 + 说明。 */
@Composable
fun PageHead(title: String, subtitle: String? = null, back: Pair<String, () -> Unit>? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        back?.let { (label, go) ->
            Row(
                Modifier.clip(RSm).clickable(onClick = go).padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, Modifier.size(18.dp), tint = W.ink2)
                Text(label, style = MaterialTheme.typography.bodySmall, color = W.ink2)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.displaySmall, color = W.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), content = actions)
        }
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = W.ink2) }
    }
}

@Composable
fun SectionHead(title: String, modifier: Modifier = Modifier, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

/** 网页的 hairline 分隔线。 */
@Composable
fun Hairline(modifier: Modifier = Modifier) = Box(modifier.fillMaxWidth().height(1.dp).background(W.line))

@Composable
fun Pager(page: Int, totalPages: Int, onPage: (Int) -> Unit) {
    if (totalPages <= 1) return
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        WButton("上一页", { onPage(page - 1) }, small = true, enabled = page > 1)
        Text("第 $page / $totalPages 页", Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodySmall, color = W.ink3)
        WButton("下一页", { onPage(page + 1) }, small = true, enabled = page < totalPages)
    }
}

/** 网页手机端的弹窗：底部抽屉，圆角 28。 */
@Composable
fun Sheet(
    title: String,
    onDismiss: () -> Unit,
    footer: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = W.surface,
        scrimColor = W.overlay,
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding()) {
            Text(title, Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 10.dp), style = MaterialTheme.typography.headlineSmall)
            Hairline()
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content,
            )
            footer?.let {
                Hairline()
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    content = it,
                )
            }
        }
    }
}

@Composable
fun Confirm(
    title: String,
    text: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    danger: Boolean = true,
    enabled: Boolean = true,
) {
    Sheet(
        title,
        onDismiss,
        footer = {
            WButton("取消", onDismiss)
            WButton(confirmText, onConfirm, kind = if (danger) Btn.Danger else Btn.Primary, enabled = enabled)
        },
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = W.ink2)
    }
}

@Composable
fun Gap(h: Dp) = Spacer(Modifier.height(h))

@Composable
fun HGap(w: Dp) = Spacer(Modifier.width(w))

/** `dl.facts` 的一行。 */
@Composable
fun Fact(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = W.ink3)
        Text(value, style = MaterialTheme.typography.bodySmall, color = W.ink, fontWeight = FontWeight.SemiBold)
    }
}

/** 头像的占位：首字母圆形。 */
@Composable
fun Initial(text: String, size: Dp = 32.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(W.accentSoft), contentAlignment = Alignment.Center) {
        Text(text.take(1).uppercase(), color = W.accentStrong, fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.42f).sp)
    }
}

/** 带描边的分组列表（菜单）。 */
@Composable
fun ListCard(content: @Composable ColumnScope.() -> Unit) =
    Card(Modifier.fillMaxWidth(), padding = PaddingValues(vertical = 4.dp), spacing = 0.dp, content = content)

@Composable
fun ListRow(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    supporting: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    tint: Color = W.ink2,
) {
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        icon?.let { Icon(it, null, Modifier.size(20.dp), tint = tint) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = W.ink)
            supporting?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = W.ink3) }
        }
        trailing?.invoke()
    }
}

@Composable
fun Bordered(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.clip(RMd).border(BorderStroke(1.dp, W.line), RMd)) { content() }
}
