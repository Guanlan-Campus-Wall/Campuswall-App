@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package xyz.zongtech.campuswall.ui

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import okhttp3.MultipartBody
import org.json.JSONObject
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.displayError
import xyz.zongtech.campuswall.glass.DialogButtons
import xyz.zongtech.campuswall.glass.Glass
import xyz.zongtech.campuswall.glass.GlassButton
import xyz.zongtech.campuswall.glass.GlassButtonStyle
import xyz.zongtech.campuswall.glass.GlassChip
import xyz.zongtech.campuswall.glass.GlassConfirmDialog
import xyz.zongtech.campuswall.glass.GlassDialog
import xyz.zongtech.campuswall.glass.GlassIconButton
import xyz.zongtech.campuswall.glass.GlassLoading
import xyz.zongtech.campuswall.glass.GlassPanel
import xyz.zongtech.campuswall.glass.GlassSwitchRow
import xyz.zongtech.campuswall.glass.GlassTextField
import xyz.zongtech.campuswall.glass.SectionTitle
import xyz.zongtech.campuswall.objects
import xyz.zongtech.campuswall.pathSegment
import xyz.zongtech.campuswall.s
import xyz.zongtech.campuswall.strings

@Composable
fun DetailScreen(model: WallModel, id: String, go: (String) -> Unit, back: () -> Unit) {
    var post by remember { mutableStateOf<JSONObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var comment by rememberSaveable { mutableStateOf("") }
    var refer by remember { mutableStateOf<JSONObject?>(null) }
    var saved by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    var deleting by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    var editTags by remember { mutableStateOf("") }
    var editAnonymous by remember { mutableStateOf(true) }
    var deleteComment by remember { mutableStateOf<JSONObject?>(null) }
    LaunchedEffect(id, model.revision, refresh) {
        try {
            post = model.api.request("/api/get_message_details/$id", "POST").optJSONObject("message")
            error = null
            if (model.user != null)
                saved = model.api.request("/api/user/me/favorites/ids").strings("ids").contains(id)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.displayError()
        }
    }
    val p = post
    val owner =
        p != null && (p.optBoolean("owned") || id in model.ownedPosts || p.s("user_id") == model.user?.s("id"))
    Column(Modifier.fillMaxSize().imePadding()) {
        GlassTopBar("动态详情", back) {
            if (p != null) {
                if (model.user != null)
                    GlassIconButton(
                        if (saved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        if (saved) "取消收藏" else "收藏",
                        {
                            model.perform {
                                model.api.request("/api/user/me/favorites/$id", if (saved) "DELETE" else "POST")
                            }
                        },
                        style = if (saved) GlassButtonStyle.Prominent else GlassButtonStyle.Normal,
                    )
                GlassIconButton(Icons.Rounded.Flag, "举报", { go("report/$id") })
                if (owner) {
                    GlassIconButton(
                        Icons.Rounded.Edit,
                        "编辑",
                        {
                            editText = p.s("text")
                            editTags = p.strings("tags").joinToString(",")
                            editAnonymous = p.optBoolean("anonymous", true)
                            editing = true
                        },
                    )
                    GlassIconButton(Icons.Rounded.Delete, "删除", { deleting = true }, iconTint = Glass.colors.danger)
                }
            }
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            error?.let { item { EmptyState(it) { refresh++ } } }
            if (p == null && error == null)
                item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { GlassLoading() } }
            if (p != null) {
                item { PostCard(p, model, go, detail = true) }
                val comments = p.objects("comments")
                item {
                    SectionTitle("评论 · ${comments.size}", Modifier.padding(top = 8.dp, start = 4.dp))
                }
                if (comments.isEmpty())
                    item {
                        Text(
                            "还没有评论，说点什么吧。",
                            Modifier.padding(start = 4.dp),
                            color = Glass.colors.secondary,
                        )
                    }
                items(comments, key = { it.s("id") }) { c ->
                    CommentCard(
                        c,
                        onReply = { refer = c },
                        onReport = { go("report/${("$id/comment/${c.s("id")}").pathSegment()}") },
                        onDelete = if (c.optBoolean("owned")) ({ deleteComment = c }) else null,
                    )
                }
            }
        }
        if (p != null) CommentComposer(model, id, comment, { comment = it }, refer, { refer = null }, go)
    }
    if (deleting)
        GlassConfirmDialog(
            title = "删除这条动态？",
            text = "动态将从校园墙移除。",
            confirmText = "删除",
            danger = true,
            onDismissRequest = { deleting = false },
            onConfirm = {
                model.perform(back) {
                    model.api.request("/api/user/me/messages/$id", "DELETE")
                    deleting = false
                }
            },
        )
    deleteComment?.let { c ->
        GlassConfirmDialog(
            title = "删除这条评论？",
            text = c.s("text").take(60),
            confirmText = "删除",
            danger = true,
            onDismissRequest = { deleteComment = null },
            onConfirm = {
                model.perform {
                    model.api.request("/api/user/me/comments/$id/${c.s("id").pathSegment()}", "DELETE")
                    deleteComment = null
                }
            },
        )
    }
    if (editing)
        GlassDialog(
            onDismissRequest = { editing = false },
            title = "编辑动态",
            buttons = {
                DialogButtons(
                    confirmText = "保存",
                    onDismiss = { editing = false },
                    confirmEnabled = !model.busy,
                    onConfirm = {
                        model.perform {
                            model.api.request(
                                "/api/user/me/messages/$id",
                                "PUT",
                                fields =
                                    mapOf(
                                        "text" to editText,
                                        "tags" to editTags.replace('，', ','),
                                        "anonymous" to editAnonymous.toString(),
                                    ),
                            )
                            editing = false
                        }
                    },
                )
            },
        ) {
            GlassTextField(editText, { editText = it }, Modifier.fillMaxWidth(), label = "正文", minLines = 4)
            GlassTextField(editTags, { editTags = it }, Modifier.fillMaxWidth(), label = "话题，用逗号分隔", singleLine = true)
            if (post?.optJSONObject("lost_found") == null)
                GlassSwitchRow("匿名发布", editAnonymous, { editAnonymous = it })
        }
}

@Composable
private fun CommentCard(
    c: JSONObject,
    onReply: () -> Unit,
    onReport: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val colors = Glass.colors
    GlassPanel(Modifier.fillMaxWidth(), cornerRadius = 24.dp, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                c.s("display_name_snapshot", c.s("nickname", "同学")),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(c.s("timestamp"), style = MaterialTheme.typography.labelSmall, color = colors.secondary)
        }
        if (c.s("refer").isNotBlank())
            Text(
                "回复：" + c.s("refer"),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        Text(c.s("text"), style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassButton("回复", onReply, height = 34.dp)
            GlassButton("举报", onReport, height = 34.dp)
            onDelete?.let { GlassButton("删除", it, height = 34.dp) }
        }
    }
}

@Composable
private fun CommentComposer(
    model: WallModel,
    id: String,
    comment: String,
    onComment: (String) -> Unit,
    refer: JSONObject?,
    clearRefer: () -> Unit,
    go: (String) -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 10.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (model.user == null && !model.community.optBoolean("guest_commenting_enabled")) {
            GlassButton("登录后评论", { go("login") }, Modifier.fillMaxWidth(), style = GlassButtonStyle.Prominent)
            return
        }
        if (!model.community.optBoolean("commenting_enabled", true)) {
            Text("评论功能暂时关闭", color = Glass.colors.secondary)
            return
        }
        refer?.let {
            GlassChip("回复：${it.s("text").take(24)}", true, clearRefer, icon = Icons.Rounded.Close)
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassTextField(
                comment,
                onComment,
                Modifier.weight(1f),
                placeholder = "友善交流，认真回应",
            )
            GlassIconButton(
                Icons.AutoMirrored.Rounded.Send,
                "发送评论",
                {
                    model.perform {
                        val fields = mutableMapOf("text" to comment)
                        refer?.let {
                            fields["refer_id"] = it.s("id")
                            fields["refer"] = it.s("text")
                        }
                        model.api.request("/api/wall/comment/$id", "POST", fields = fields)
                        onComment("")
                        clearRefer()
                    }
                },
                enabled = comment.isNotBlank() && !model.busy,
                style = GlassButtonStyle.Prominent,
                size = 52.dp,
            )
        }
    }
}

@Composable
fun ComposeScreen(model: WallModel, initialTag: String = "", back: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("draft.${model.user?.s("id")}", 0) }
    var text by rememberSaveable { mutableStateOf(prefs.getString("text", "").orEmpty()) }
    var tags by rememberSaveable { mutableStateOf(initialTag) }
    var anonymous by rememberSaveable { mutableStateOf(true) }
    var official by rememberSaveable { mutableStateOf(false) }
    var question by rememberSaveable { mutableStateOf("") }
    var options by rememberSaveable { mutableStateOf("") }
    var deadline by rememberSaveable { mutableStateOf("") }
    var files by remember { mutableStateOf(emptyList<Uri>()) }
    var cameraUri by rememberSaveable { mutableStateOf("") }
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
            files = (files + it).distinct().take(20)
        }
    val camera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) files = (files + Uri.parse(cameraUri)).take(20)
        }
    LaunchedEffect(text) { prefs.edit().putString("text", text).apply() }
    val canPost =
        (model.user != null || model.community.optBoolean("guest_posting_enabled")) &&
            model.community.optBoolean("posting_enabled", true)
    Column(Modifier.fillMaxSize().imePadding()) {
        GlassTopBar("发布校园动态", back, "分享日常、提问，或认真说一句心里话")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GlassPanel(Modifier.fillMaxWidth()) {
                GlassTextField(text, { text = it }, Modifier.fillMaxWidth(), placeholder = "此刻想说点什么？", minLines = 6)
                GlassTextField(
                    tags,
                    { tags = it },
                    Modifier.fillMaxWidth(),
                    label = "话题",
                    placeholder = "多个话题用逗号分隔",
                    singleLine = true,
                )
                GlassSwitchRow("匿名发布", anonymous, { anonymous = it }, supporting = "其他同学看不到你的昵称和头像")
                if (model.can("content.publish.official"))
                    GlassSwitchRow("以官方身份发布", official, { official = it })
            }
            GlassPanel(Modifier.fillMaxWidth()) {
                SectionTitle("附件")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassButton(
                        "添加附件",
                        { picker.launch(arrayOf("image/*", "video/*", "audio/*", "application/pdf")) },
                        icon = Icons.Rounded.AddPhotoAlternate,
                    )
                    GlassButton(
                        "拍照",
                        {
                            val dir = File(context.cacheDir, "photos").apply { mkdirs() }
                            val uri =
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.files",
                                    File.createTempFile("photo", ".jpg", dir),
                                )
                            cameraUri = uri.toString()
                            camera.launch(uri)
                        },
                        icon = Icons.Rounded.PhotoCamera,
                    )
                }
                if (files.isNotEmpty())
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        files.forEach { uri ->
                            GlassChip(displayName(context, uri), false, { files = files - uri }, icon = Icons.Rounded.Close)
                        }
                    }
                else Text("可添加图片、视频、音频或 PDF，最多 20 个。", style = MaterialTheme.typography.bodySmall, color = Glass.colors.secondary)
            }
            GlassPanel(Modifier.fillMaxWidth()) {
                SectionTitle("投票（可选）")
                GlassTextField(question, { question = it }, Modifier.fillMaxWidth(), placeholder = "投票问题", singleLine = true)
                if (question.isNotBlank()) {
                    GlassTextField(options, { options = it }, Modifier.fillMaxWidth(), label = "选项", placeholder = "每行一个选项", minLines = 3)
                    DateTimeField("投票截止时间", deadline) { deadline = it }
                }
            }
            if (!model.community.optBoolean("posting_enabled", true))
                Text(model.community.s("pause_reason", "发帖暂时关闭"), color = Glass.colors.danger)
        }
        GlassButton(
            text = if (model.busy) "正在上传与提交…" else "发布",
            onClick = {
                model.perform(back) {
                    require(text.isNotBlank() || files.isNotEmpty() || question.isNotBlank()) {
                        "请填写正文或添加附件、投票"
                    }
                    val uploads = files.map { model.api.upload(it) }
                    val body =
                        MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("text", text)
                            .addFormDataPart("tags", tags.replace('，', ','))
                            .addFormDataPart("anonymous", anonymous.toString())
                            .addFormDataPart("post_as_admin", official.toString())
                    if (question.isNotBlank()) {
                        body.addFormDataPart("poll_question", question)
                        options.lines().filter { it.isNotBlank() }.forEach { body.addFormDataPart("poll_options", it) }
                        if (deadline.isNotBlank()) body.addFormDataPart("poll_closes_at", deadline)
                    }
                    uploads.forEach { body.addFormDataPart("filenames", it) }
                    val result = model.api.request("/api/wall/submit", "POST", body = body.build())
                    prefs.edit().clear().apply()
                    model.error = if (result.s("moderation_status") == "pending") "已提交，请等待审核" else "发布成功"
                }
            },
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            enabled = !model.busy && canPost,
            style = GlassButtonStyle.Prominent,
            icon = Icons.AutoMirrored.Rounded.Send,
            height = 54.dp,
        )
    }
}

private fun displayName(context: android.content.Context, uri: Uri): String =
    runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0) else null
            }
        }
        .getOrNull()
        ?.take(24) ?: uri.lastPathSegment?.takeLast(24).orEmpty()

private val shanghai: ZoneId = ZoneId.of("Asia/Shanghai")

/** 日期时间选择：玻璃输入框 + 玻璃弹窗中的日期、时间选择器。值为带时区的 ISO 8601 字符串。 */
@Composable
fun DateTimeField(label: String, value: String, onChange: (String) -> Unit) {
    var calendar by remember { mutableStateOf(false) }
    var clock by remember { mutableStateOf(false) }
    val current =
        runCatching { OffsetDateTime.parse(value).atZoneSameInstant(shanghai) }
            .getOrElse { ZonedDateTime.now(shanghai) }
    val date =
        rememberDatePickerState(
            initialSelectedDateMillis = current.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
    val time = rememberTimePickerState(current.hour, current.minute, true)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassButton(onClick = { calendar = true }, modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(20.dp))
            Text(
                "$label：" +
                    if (value.isBlank()) "未设置"
                    else current.format(DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm")),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        if (value.isNotBlank()) GlassChip("清除时间", false, { onChange("") }, icon = Icons.Rounded.Close)
    }
    val pickerColors = Color.Transparent
    if (calendar)
        GlassDialog(
            onDismissRequest = { calendar = false },
            title = "选择日期",
            scrollable = false,
            buttons = {
                DialogButtons(
                    confirmText = "下一步",
                    onDismiss = { calendar = false },
                    confirmEnabled = date.selectedDateMillis != null,
                    onConfirm = {
                        calendar = false
                        clock = true
                    },
                )
            },
        ) {
            DatePicker(
                date,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(containerColor = pickerColors),
            )
        }
    if (clock)
        GlassDialog(
            onDismissRequest = { clock = false },
            title = "选择时间",
            buttons = {
                DialogButtons(
                    confirmText = "确定",
                    onDismiss = { clock = false },
                    onConfirm = {
                        val day = Instant.ofEpochMilli(date.selectedDateMillis!!).atZone(ZoneOffset.UTC).toLocalDate()
                        onChange(day.atTime(time.hour, time.minute).atZone(shanghai).toOffsetDateTime().toString())
                        clock = false
                    },
                )
            },
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TimePicker(time, colors = TimePickerDefaults.colors(containerColor = pickerColors))
            }
        }
}
