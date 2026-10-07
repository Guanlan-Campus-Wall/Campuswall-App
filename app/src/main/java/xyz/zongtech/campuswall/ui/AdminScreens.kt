package xyz.zongtech.campuswall.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.displayError
import xyz.zongtech.campuswall.displayState
import xyz.zongtech.campuswall.glass.DialogButtons
import xyz.zongtech.campuswall.glass.Glass
import xyz.zongtech.campuswall.glass.GlassButton
import xyz.zongtech.campuswall.glass.GlassButtonStyle
import xyz.zongtech.campuswall.glass.GlassChip
import xyz.zongtech.campuswall.glass.GlassChoiceRow
import xyz.zongtech.campuswall.glass.GlassConfirmDialog
import xyz.zongtech.campuswall.glass.GlassDialog
import xyz.zongtech.campuswall.glass.GlassIconButton
import xyz.zongtech.campuswall.glass.GlassPanel
import xyz.zongtech.campuswall.glass.GlassProgressBar
import xyz.zongtech.campuswall.glass.GlassSwitchRow
import xyz.zongtech.campuswall.glass.GlassTextField
import xyz.zongtech.campuswall.glass.SectionTitle
import xyz.zongtech.campuswall.objects
import xyz.zongtech.campuswall.pathSegment
import xyz.zongtech.campuswall.s
import xyz.zongtech.campuswall.strings

private data class AdminSection(val title: String, val endpoint: String, val capability: String, val key: String)

private val sections =
    listOf(
        AdminSection("概览", "/dashboard/stats", "dashboard.read", "stats"),
        AdminSection("动态审核", "/api/messages", "content.queue.read", "messages"),
        AdminSection("评论管理", "/comments", "content.comment.read", "comments"),
        AdminSection("用户管理", "/users", "users.read", "users"),
        AdminSection("公告", "/notice", "notice.read", "notices"),
        AdminSection("举报", "/report", "report.read", "reports"),
        AdminSection("举报处理记录", "/reports/history", "report.history.read", "items"),
        AdminSection("反馈", "/feedback", "feedback.read", "tickets"),
        AdminSection("回收站", "/trash", "content.trash.read", "messages"),
        AdminSection("审计记录", "/audit", "audit.read", "events"),
        AdminSection("社区设置", "/settings/community", "settings.read", "community"),
        AdminSection("人机验证", "/settings/captcha", "settings.read", "captcha"),
        AdminSection("内容审核设置", "/settings/ai", "settings.ai.read", "ai"),
        AdminSection("通知集成", "/settings/notifications", "settings.notifications.read", "providers"),
        AdminSection("管理日志", "/admin_log", "logs.legacy_admin.read", "logs"),
        AdminSection("错误日志", "/log", "logs.error.read", "logs"),
    )

private val selectable = listOf("/api/messages", "/comments", "/trash")

@Composable
fun AdminScreen(model: WallModel, back: () -> Unit) {
    val allowed = sections.filter { model.can(it.capability) }
    var selected by remember { mutableStateOf<AdminSection?>(null) }
    var response by remember { mutableStateOf(JSONObject()) }
    var error by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableIntStateOf(1) }
    var search by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf<Pair<String, suspend () -> Unit>?>(null) }
    var form by remember { mutableStateOf<AdminForm?>(null) }
    var permissionUser by remember { mutableStateOf<String?>(null) }
    var status by remember(selected) { mutableStateOf(if (selected?.endpoint == "/api/messages") "pending" else "all") }
    var scope by remember(selected) { mutableStateOf("all") }
    var checked by remember(selected, page, status, scope) { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(selected, page, search, status, scope, model.revision, refresh) {
        selected?.let { s ->
            loading = true
            try {
                response =
                    model.api.request(
                        "/api/admin${s.endpoint}?page=$page&page_size=20&search=${search.pathSegment()}&q=${search.pathSegment()}&status=$status&scope=$scope"
                    )
                error = null
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                error = e.displayError()
            } finally {
                loading = false
            }
        }
    }
    val leave = {
        if (selected == null) back()
        else {
            selected = null
            page = 1
            search = ""
            response = JSONObject()
        }
    }
    androidx.activity.compose.BackHandler(enabled = selected != null) { leave() }
    Column(Modifier.fillMaxSize()) {
        GlassTopBar(selected?.title ?: "管理中心", leave, if (selected == null) "按账号权限开放" else null)
        val s = selected
        if (s == null) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = LocalBottomSpace.current),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (allowed.isEmpty()) item { EmptyState("当前账号没有管理权限") }
                items(allowed) { section ->
                    GlassPanel(
                        Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        onClick = { selected = section },
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(section.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Glass.colors.secondary)
                        }
                    }
                }
            }
            return@Column
        }
        val statuses =
            when (s.endpoint) {
                "/api/messages" ->
                    listOf("pending" to "待审核", "approved" to "已审核", "awaiting_publication" to "待发布") +
                        if (model.can("content.message.hide"))
                            listOf("all" to "全部", "visible" to "公开", "hidden" to "下架")
                        else emptyList()
                "/comments" -> listOf("all" to "全部", "visible" to "公开", "hidden" to "下架")
                "/users" -> listOf("all" to "全部", "active" to "正常", "pending" to "待注册审核", "disabled" to "停用")
                "/feedback" ->
                    listOf(
                        "all" to "全部",
                        "pending" to "待处理",
                        "in_progress" to "处理中",
                        "resolved" to "已解决",
                        "closed" to "已关闭",
                    )
                else -> emptyList()
            }
        val rows =
            if (s.endpoint == "/report")
                response
                    .optJSONObject("reports")
                    ?.let { reports ->
                        reports
                            .keys()
                            .asSequence()
                            .flatMap { id -> reports.objects(id).map { JSONObject(it.toString()).put("message_id", id) } }
                            .toList()
                    }
                    .orEmpty()
            else
                response
                    .objects(s.key)
                    .ifEmpty { response.objects("data") }
                    .ifEmpty { response.objects("items") }
                    .ifEmpty { response.objects("content") }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = LocalBottomSpace.current),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (statuses.isNotEmpty())
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        statuses.forEach { (v, t) ->
                            GlassChip(t, status == v, {
                                status = v
                                page = 1
                            })
                        }
                    }
                }
            if (s.endpoint == "/api/messages")
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("all" to "所有内容", "posts" to "校园动态", "confessions" to "表白墙").forEach { (v, t) ->
                            GlassChip(t, scope == v, {
                                scope = v
                                page = 1
                            })
                        }
                    }
                }
            if (!s.endpoint.startsWith("/settings/") && s.endpoint != "/dashboard/stats")
                item {
                    GlassTextField(
                        search,
                        {
                            search = it
                            page = 1
                        },
                        Modifier.fillMaxWidth(),
                        placeholder = "搜索",
                        singleLine = true,
                        leadingIcon = Icons.Rounded.Search,
                    )
                }
            if (loading) item { GlassProgressBar() }
            error?.let { item { EmptyState(it) { refresh++ } } }
            if (s.endpoint in selectable)
                item {
                    BulkBar(model, s.endpoint, rows, checked, model.busy, { checked = it }) { label, run -> action = label to run }
                }
            if (s.endpoint == "/notice" && model.can("notice.create"))
                item { GlassButton("创建公告", { form = noticeForm() }, style = GlassButtonStyle.Prominent) }
            if (s.endpoint == "/users" && model.can("users.role.assign"))
                item {
                    GlassButton(
                        "新建管理账号",
                        {
                            form =
                                AdminForm(
                                    "新建管理账号",
                                    "/api/admin/users",
                                    listOf(
                                        FormField("username", "账号"),
                                        FormField("nickname", "昵称"),
                                        FormField("password", "初始密码", secret = true),
                                        FormField("role", "角色", "reviewer", roles.filter { it.first != "user" }),
                                    ),
                                    json = true,
                                )
                        },
                        style = GlassButtonStyle.Prominent,
                    )
                }
            if (s.endpoint == "/settings/notifications")
                item { NotificationSettings(model, response.optJSONObject("settings") ?: response) }
            else if (s.endpoint.startsWith("/settings/"))
                item {
                    SettingsEditor(
                        model,
                        s.endpoint,
                        response.optJSONObject(s.key) ?: response.optJSONObject("settings") ?: response,
                    )
                }
            if (s.endpoint == "/dashboard/stats")
                item {
                    GlassPanel(Modifier.fillMaxWidth()) { ReadableObject(response.optJSONObject("stats") ?: response) }
                }
            val logLines = response.strings("log_content")
            if (logLines.isNotEmpty())
                item {
                    GlassPanel(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        logLines.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            items(rows) { row ->
                AdminRow(
                    model,
                    s.endpoint,
                    row,
                    checked = if (s.endpoint in selectable) row.toString() in checked else null,
                    onCheck = { v -> checked = if (v) checked + row.toString() else checked - row.toString() },
                    confirm = { label, run -> action = label to run },
                    showForm = { form = it },
                    permissions = { permissionUser = row.s("id") },
                )
            }
            if (rows.isEmpty() && !loading && !s.endpoint.startsWith("/settings/") && s.endpoint != "/dashboard/stats")
                item { EmptyState("暂无记录") }
            item { Pager(page, response.optInt("total", rows.size), { page = it }, enabled = !loading) }
        }
    }
    action?.let { (title, run) ->
        GlassConfirmDialog(
            title = "确认$title？",
            onDismissRequest = { action = null },
            danger = title.contains("删除"),
            confirmEnabled = !model.busy,
            onConfirm = {
                model.perform {
                    run()
                    action = null
                }
            },
        )
    }
    form?.let { AdminFormDialog(model, it) { form = null } }
    permissionUser?.let { PermissionsDialog(model, it) { permissionUser = null } }
}

@Composable
private fun BulkBar(
    model: WallModel,
    endpoint: String,
    rows: List<JSONObject>,
    checked: Set<String>,
    busy: Boolean,
    setChecked: (Set<String>) -> Unit,
    confirm: (String, suspend () -> Unit) -> Unit,
) {
    val operations =
        when (endpoint) {
            "/api/messages" ->
                listOf("approve" to "批量通过", "return" to "退回待审", "hide" to "批量下架", "restore" to "批量上架")
            "/comments" -> listOf("hide" to "批量下架", "restore" to "批量上架")
            else -> listOf("restore" to "批量恢复", "purge" to "永久删除")
        }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassChip(
            if (checked.size == rows.size && rows.isNotEmpty()) "取消选择" else "选择本页",
            checked.isNotEmpty() && checked.size == rows.size,
            { setChecked(if (checked.size == rows.size) emptySet() else rows.map { it.toString() }.toSet()) },
            icon = Icons.Rounded.Check,
        )
        operations.forEach { (operation, label) ->
            GlassButton(
                label,
                {
                    val chosen = rows.filter { it.toString() in checked }
                    confirm("$label ${chosen.size} 项") {
                        val payload = JSONObject().put("action", operation)
                        val path =
                            when (endpoint) {
                                "/api/messages" -> {
                                    payload.put("message_ids", JSONArray(chosen.map { it.optLong("id") }))
                                    "/messages/bulk-moderation"
                                }
                                "/comments" -> {
                                    payload.put(
                                        "targets",
                                        JSONArray(
                                            chosen.map {
                                                JSONObject().put("message_id", it.opt("message_id")).put("comment_id", it.s("id"))
                                            }
                                        ),
                                    )
                                    "/comments/bulk-moderation"
                                }
                                else -> {
                                    payload
                                        .put("confirm", if (operation == "purge") "PURGE" else "")
                                        .put(
                                            "targets",
                                            JSONArray(
                                                chosen.map {
                                                    JSONObject()
                                                        .put("type", it.s("type", "message"))
                                                        .put("message_id", it.opt("message_id") ?: it.opt("id"))
                                                        .put("comment_id", it.s("comment_id", it.s("id")))
                                                }
                                            ),
                                        )
                                    "/trash/bulk"
                                }
                            }
                        val result = model.api.request("/api/admin$path", "POST", json = payload)
                        val failures = result.objects("results").count { !it.optBoolean("success") }
                        setChecked(emptySet())
                        if (failures > 0) model.error = "$failures 项未完成，请检查权限或内容状态"
                    }
                },
                enabled = checked.isNotEmpty() && !busy,
                height = 36.dp,
                style = if (operation == "purge") GlassButtonStyle.Danger else GlassButtonStyle.Normal,
            )
        }
    }
}

@Composable
private fun AdminRow(
    model: WallModel,
    endpoint: String,
    row: JSONObject,
    checked: Boolean?,
    onCheck: (Boolean) -> Unit,
    confirm: (String, suspend () -> Unit) -> Unit,
    showForm: (AdminForm) -> Unit,
    permissions: () -> Unit,
) {
    fun request(path: String, method: String, json: JSONObject?): suspend () -> Unit = {
        model.api.request(path, method, json = json)
    }
    GlassPanel(Modifier.fillMaxWidth(), cornerRadius = 24.dp, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val title =
                    row.s("text", row.s("title", row.s("nickname", row.s("summary", row.s("action", "记录 ${row.s("id")}")))))
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 6, overflow = TextOverflow.Ellipsis)
                Text(
                    displayState(row.s("status", row.s("moderation_status", row.s("timestamp")))),
                    style = MaterialTheme.typography.labelMedium,
                    color = Glass.colors.secondary,
                )
                if (endpoint == "/api/messages" && row.s("review_status").isNotBlank())
                    Text(displayState(row.s("review_status")), style = MaterialTheme.typography.labelMedium, color = Glass.colors.secondary)
                if (endpoint == "/users")
                    Text(displayState(row.s("role")), style = MaterialTheme.typography.labelMedium, color = Glass.colors.accent)
                if (endpoint == "/feedback" && row.s("email").isNotBlank())
                    Text(row.s("email"), style = MaterialTheme.typography.labelMedium, color = Glass.colors.secondary)
                if (endpoint == "/report")
                    Text(displayState(row.s("category")), style = MaterialTheme.typography.labelMedium, color = Glass.colors.danger)
            }
            if (checked != null)
                GlassIconButton(
                    Icons.Rounded.Check,
                    if (checked) "取消选择" else "选择",
                    { onCheck(!checked) },
                    size = 36.dp,
                    style = if (checked) GlassButtonStyle.Prominent else GlassButtonStyle.Normal,
                    iconTint = if (checked) Color.Unspecified else Glass.colors.secondary.copy(alpha = 0.4f),
                )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val id = row.s("id")
            when (endpoint) {
                "/api/messages" -> {
                    listOf("approve" to "通过", "return" to "退回待审").forEach { (v, t) ->
                        SmallAction(t) {
                            confirm(t, request("/api/admin/messages/$id/review", "POST", JSONObject().put("action", v)))
                        }
                    }
                    SmallAction("删除", danger = true) { confirm("删除动态", request("/api/admin/delete_message/$id", "POST", null)) }
                    SmallAction("置顶") {
                        confirm(
                            "切换置顶",
                            request("/api/admin/messages/$id/moderation", "POST", JSONObject().put("pinned", !row.optBoolean("pinned"))),
                        )
                    }
                    SmallAction("精华") {
                        confirm(
                            "切换精华",
                            request("/api/admin/messages/$id/moderation", "POST", JSONObject().put("featured", !row.optBoolean("featured"))),
                        )
                    }
                    SmallAction("上架 / 下架") {
                        confirm(
                            "切换下架状态",
                            request(
                                "/api/admin/messages/$id/moderation",
                                "POST",
                                JSONObject().put("hidden", row.s("moderation_status") != "hidden"),
                            ),
                        )
                    }
                    SmallAction("修复") { confirm("修复内容索引", request("/api/admin/repair_message/$id", "POST", null)) }
                }
                "/comments" ->
                    listOf("visible" to "公开", "hidden" to "隐藏").forEach { (v, t) ->
                        SmallAction(t) {
                            confirm(
                                t,
                                request(
                                    "/api/admin/comments/${row.s("message_id")}/${id.pathSegment()}/moderation",
                                    "POST",
                                    JSONObject().put("hidden", v == "hidden"),
                                ),
                            )
                        }
                    }
                "/users" -> {
                    UserAdminActions(model, row, showForm, permissions)
                    listOf(
                            "registration/approve" to "通过注册",
                            "registration/reject" to "驳回注册",
                            "unmute" to "解除禁言",
                            "disable" to "停用账号",
                        )
                        .forEach { (v, t) ->
                            SmallAction(t) { confirm(t, request("/api/admin/users/$id/$v", "POST", null)) }
                        }
                }
                "/notice" -> {
                    SmallAction("编辑") { showForm(noticeForm(row)) }
                    SmallAction("删除", danger = true) { confirm("删除公告", request("/api/admin/notice/$id", "DELETE", null)) }
                }
                "/feedback" ->
                    SmallAction("处理工单") {
                        showForm(
                            AdminForm(
                                "处理反馈",
                                "/api/admin/feedback/$id",
                                listOf(
                                    FormField(
                                        "status",
                                        "状态",
                                        row.s("status"),
                                        listOf(
                                            "pending" to "待处理",
                                            "in_progress" to "处理中",
                                            "resolved" to "已解决",
                                            "closed" to "已关闭",
                                        ),
                                    ),
                                    FormField("public_reply", "公开回复", row.s("public_reply"), multiline = true),
                                    FormField("internal_note", "内部备注", row.s("internal_note"), multiline = true),
                                ),
                                "PUT",
                                true,
                            )
                        )
                    }
                "/report" ->
                    SmallAction("处理") {
                        showForm(
                            AdminForm(
                                "处理举报",
                                "/api/admin/reports/${row.s("message_id")}/$id/resolve",
                                listOf(
                                    FormField(
                                        "action",
                                        "处理方式",
                                        "dismiss",
                                        listOf("dismiss" to "不予处理", "delete_message" to "删除动态") +
                                            if (row.s("target_type") == "comment") listOf("delete_comment" to "删除评论")
                                            else emptyList(),
                                    ),
                                    FormField("public_reply", "处理说明", multiline = true),
                                ),
                                json = true,
                            )
                        )
                    }
                "/trash" -> {
                    val target =
                        if (row.s("type") == "comment")
                            "comments/${row.s("message_id")}/${row.s("comment_id", id).pathSegment()}"
                        else "messages/${row.s("message_id", id)}"
                    SmallAction("恢复") { confirm("恢复内容", request("/api/admin/trash/$target/restore", "POST", null)) }
                    SmallAction("永久删除", danger = true) {
                        confirm(
                            "永久删除（无法恢复）",
                            request("/api/admin/trash/$target", "DELETE", JSONObject().put("confirm", "PURGE")),
                        )
                    }
                }
                else -> {}
            }
        }
        if (endpoint !in listOf("/api/messages", "/comments", "/users", "/notice", "/feedback", "/report", "/trash"))
            ReadableObject(row)
    }
}

@Composable
private fun SmallAction(text: String, danger: Boolean = false, onClick: () -> Unit) {
    GlassButton(
        onClick,
        height = 34.dp,
        horizontalPadding = 14.dp,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (danger) Glass.colors.danger else Glass.colors.content,
        )
    }
}

data class FormField(
    val key: String,
    val label: String,
    val initial: String = "",
    val choices: List<Pair<String, String>> = emptyList(),
    val multiline: Boolean = false,
    val secret: Boolean = false,
)

data class AdminForm(
    val title: String,
    val path: String,
    val fields: List<FormField>,
    val method: String = "POST",
    val json: Boolean = false,
)

@Composable
private fun AdminFormDialog(model: WallModel, form: AdminForm, close: () -> Unit) {
    var values by remember(form) { mutableStateOf(form.fields.associate { it.key to it.initial }) }
    GlassDialog(
        onDismissRequest = { if (!model.busy) close() },
        title = form.title,
        buttons = {
            DialogButtons(
                confirmText = "确认保存",
                onDismiss = close,
                confirmEnabled = !model.busy,
                onConfirm = {
                    model.perform(close) {
                        if (form.json) model.api.request(form.path, form.method, json = JSONObject(values))
                        else model.api.request(form.path, form.method, fields = values)
                    }
                },
            )
        },
    ) {
        form.fields.forEach { field ->
            if (field.key in listOf("muted_until", "publish_at"))
                DateTimeField(field.label, values[field.key].orEmpty()) { values = values + (field.key to it) }
            else if (field.choices.isNotEmpty()) {
                Text(field.label, style = MaterialTheme.typography.labelLarge, color = Glass.colors.secondary)
                GlassChoiceRow(field.choices, values[field.key].orEmpty(), { values = values + (field.key to it) })
            } else
                GlassTextField(
                    values[field.key].orEmpty(),
                    { values = values + (field.key to it) },
                    Modifier.fillMaxWidth(),
                    label = field.label,
                    singleLine = !field.multiline,
                    minLines = if (field.multiline) 3 else 1,
                    secret = field.secret,
                )
        }
    }
}

private val roles = listOf("user" to "普通用户", "reviewer" to "审核员", "admin" to "管理员", "super_admin" to "超级管理员")

private fun noticeForm(row: JSONObject? = null) =
    AdminForm(
        if (row == null) "发布公告" else "编辑公告",
        "/api/admin/notice" + (row?.let { "/${it.s("id")}" } ?: ""),
        listOf(
            FormField("title", "标题", row?.s("title").orEmpty()),
            FormField("summary", "摘要", row?.s("summary").orEmpty()),
            FormField("content", "正文", row?.s("content").orEmpty(), multiline = true),
            FormField(
                "priority",
                "优先级",
                row?.s("priority") ?: "normal",
                listOf("normal" to "普通", "important" to "重要", "urgent" to "紧急"),
            ),
            FormField(
                "status",
                "发布状态",
                row?.s("status") ?: "draft",
                listOf("draft" to "草稿", "published" to "公开", "archived" to "归档"),
            ),
            FormField("publish_at", "发布时间（可留空）", row?.s("publish_at").orEmpty()),
        ),
        if (row == null) "POST" else "PUT",
    )

@Composable
private fun UserAdminActions(model: WallModel, row: JSONObject, show: (AdminForm) -> Unit, permissions: () -> Unit) {
    val base = "/api/admin/users/${row.s("id")}"
    if (model.can("users.profile.update"))
        SmallAction("资料") {
            show(
                AdminForm(
                    "编辑资料",
                    base,
                    listOf(
                        FormField("real_name", "真实姓名", row.s("real_name")),
                        FormField("nickname", "昵称", row.s("nickname")),
                        FormField("gender", "性别", row.s("gender", "0"), listOf("0" to "未设置", "1" to "男", "2" to "女")),
                        FormField("bio", "简介", row.s("bio"), multiline = true),
                        FormField("status", "账号状态", row.s("status", "active"), listOf("active" to "正常", "disabled" to "停用")),
                    ),
                    "PUT",
                )
            )
        }
    if (model.can("users.role.assign"))
        SmallAction("角色") {
            show(AdminForm("调整角色", "$base/role", listOf(FormField("role", "角色", row.s("role"), roles)), "PUT", true))
        }
    if (model.can("users.permissions.assign")) SmallAction("权限", onClick = permissions)
    if (model.can("users.mute"))
        SmallAction("禁言") {
            show(
                AdminForm(
                    "禁言账号",
                    "$base/mute",
                    listOf(FormField("muted_until", "禁言截止时间"), FormField("reason", "原因", multiline = true)),
                )
            )
        }
    if (model.can("users.password.reset"))
        SmallAction("重置密码") {
            show(AdminForm("重置密码", "$base/reset_password", listOf(FormField("password", "新密码", secret = true))))
        }
}

@Composable
private fun PermissionsDialog(model: WallModel, id: String, close: () -> Unit) {
    var state by remember { mutableStateOf<JSONObject?>(null) }
    var catalog by remember { mutableStateOf(emptyList<JSONObject>()) }
    var values by remember { mutableStateOf(emptyMap<String, String>()) }
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(id) {
        try {
            catalog = model.api.request("/api/admin/permissions").objects("permissions")
            state = model.api.request("/api/admin/users/$id/permissions")
            val overrides = state?.optJSONObject("overrides")
            values =
                catalog.associate {
                    val k = it.s("key")
                    k to
                        when (k) {
                            in overrides?.strings("allow").orEmpty() -> "allow"
                            in overrides?.strings("deny").orEmpty() -> "deny"
                            else -> "inherit"
                        }
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.displayError()
        }
    }
    GlassDialog(
        onDismissRequest = close,
        title = "个人权限",
        buttons = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(reason, { reason = it }, Modifier.fillMaxWidth(), placeholder = "变更原因（必填）", singleLine = true)
                DialogButtons(
                    confirmText = "保存权限",
                    onDismiss = close,
                    confirmEnabled = state != null && reason.isNotBlank() && !model.busy,
                    onConfirm = {
                        model.perform(close) {
                            model.api.request(
                                "/api/admin/users/$id/permissions",
                                "PUT",
                                json =
                                    JSONObject()
                                        .put("allow", JSONArray(values.filterValues { it == "allow" }.keys.toList()))
                                        .put("deny", JSONArray(values.filterValues { it == "deny" }.keys.toList()))
                                        .put("permission_version", state!!.opt("permission_version"))
                                        .put("reason", reason)
                                        .put("confirm", "REPLACE_PERMISSION_OVERRIDES"),
                            )
                        }
                    },
                )
            }
        },
    ) {
        error?.let { Text(it, color = Glass.colors.danger) }
        if (state == null && error == null) GlassProgressBar()
        catalog.forEach { p ->
            val k = p.s("key")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(p.s("label", p.s("name", k)), style = MaterialTheme.typography.titleSmall)
                if (p.s("description").isNotBlank())
                    Text(p.s("description"), style = MaterialTheme.typography.bodySmall, color = Glass.colors.secondary)
                GlassChoiceRow(
                    listOf("inherit" to "继承", "allow" to "允许", "deny" to "禁止"),
                    values[k] ?: "inherit",
                    { values = values + (k to it) },
                )
            }
        }
    }
}

private val labels =
    mapOf(
        "posting_enabled" to "允许发帖",
        "commenting_enabled" to "允许评论",
        "guest_posting_enabled" to "允许访客发帖",
        "guest_commenting_enabled" to "允许访客评论",
        "require_post_approval" to "发帖需要审核",
        "pause_reason" to "暂停说明",
        "community_rules" to "社区规则",
        "enabled" to "启用",
        "provider" to "服务商",
        "site_key" to "站点公钥",
        "secret_key" to "验证密钥",
        "protect_login" to "保护登录",
        "protect_register" to "保护注册",
        "protect_admin_login" to "保护管理登录",
        "model" to "模型",
        "base_url" to "服务地址",
        "api_key" to "接口密钥",
        "text" to "内容",
        "title" to "标题",
        "status" to "状态",
        "created_at" to "创建时间",
        "total" to "总计",
        "users" to "用户",
        "messages" to "动态",
        "comments" to "评论",
        "id" to "编号",
        "message_id" to "动态编号",
        "user_id" to "用户编号",
        "action" to "操作",
        "reason" to "原因",
        "role" to "身份",
        "nickname" to "昵称",
        "username" to "账号",
        "student_id" to "学号",
        "email" to "邮箱",
        "moderation_status" to "审核状态",
        "updated_at" to "更新时间",
        "timestamp" to "时间",
        "community" to "社区",
        "reports" to "举报",
        "feedback" to "反馈",
        "managers" to "管理人员",
        "audit" to "审计",
        "pending" to "待处理",
        "approved" to "已通过",
        "hidden" to "已隐藏",
        "visible" to "已公开",
        "deleted" to "已删除",
        "rejected" to "已退回",
        "pending_posts" to "待审动态",
        "pending_confessions" to "待审表白",
        "affected_messages" to "涉及动态",
        "comment_reports" to "评论举报",
        "processed_total" to "已处理总数",
        "processed_last_7_days" to "近七天已处理",
        "in_progress" to "处理中",
        "resolved" to "已解决",
        "closed" to "已关闭",
        "active" to "正常",
        "disabled" to "已停用",
        "count" to "数量",
        "category" to "分类",
        "public_reply" to "公开回复",
        "internal_note" to "内部备注",
    )

@Composable
private fun ReadableObject(value: JSONObject) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        value.keys().forEach { k ->
            if (k != "success" && !k.contains("secret") && !k.contains("token")) {
                val v = value.opt(k)
                if (v is JSONObject) {
                    Text(labels[k] ?: "其他信息", style = MaterialTheme.typography.titleSmall)
                    Box(Modifier.padding(start = 12.dp)) { ReadableObject(v) }
                } else if (v !is JSONArray)
                    Text(
                        "${labels[k] ?: "信息"}：${if (v == JSONObject.NULL) "—" else displayState(v?.toString().orEmpty())}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
            }
        }
    }
}

@Composable
private fun SettingsEditor(model: WallModel, path: String, original: JSONObject) {
    var values by remember(original.toString()) { mutableStateOf(JSONObject(original.toString())) }
    var secret by remember { mutableStateOf("") }
    var clearSecret by remember { mutableStateOf(false) }
    val editable =
        when (path) {
            "/settings/community" -> model.can("settings.community.update")
            "/settings/captcha" -> model.can("settings.captcha.update")
            "/settings/ai" -> model.can("settings.ai.update")
            else -> false
        }
    GlassPanel(Modifier.fillMaxWidth()) {
        original.keys().forEach { k ->
            val v = original.opt(k)
            if (k in labels && k != "success") {
                if (v is Boolean)
                    GlassSwitchRow(
                        labels[k] ?: k,
                        values.optBoolean(k),
                        { values = JSONObject(values.toString()).put(k, it) },
                        enabled = editable,
                    )
                else if (v !is JSONObject && v !is JSONArray)
                    GlassTextField(
                        values.s(k),
                        { values = JSONObject(values.toString()).put(k, it) },
                        Modifier.fillMaxWidth(),
                        label = labels[k] ?: k,
                        enabled = editable,
                    )
            }
        }
        if (path == "/settings/community" || path == "/settings/captcha") {
            val key = if (path == "/settings/community") "sensitive_words" else "allowed_hostnames"
            var lines by remember(original.toString()) { mutableStateOf(original.strings(key).joinToString("\n")) }
            GlassTextField(
                lines,
                {
                    lines = it
                    values =
                        JSONObject(values.toString())
                            .put(key, JSONArray(it.lines().map { line -> line.trim() }.filter { line -> line.isNotEmpty() }))
                },
                Modifier.fillMaxWidth(),
                label = if (key == "sensitive_words") "敏感词，每行一个" else "允许的验证域名，每行一个",
                minLines = 3,
                enabled = editable,
            )
        }
        if (path == "/settings/captcha" || path == "/settings/ai") {
            GlassTextField(
                secret,
                { secret = it },
                Modifier.fillMaxWidth(),
                label = "替换密钥（留空保留原值）",
                singleLine = true,
                secret = true,
                enabled = editable,
            )
            GlassSwitchRow("清除已保存的密钥", clearSecret, { clearSecret = it }, enabled = editable)
        }
        GlassButton(
            "保存设置",
            {
                model.perform {
                    val payload = JSONObject(values.toString())
                    if (path == "/settings/ai") payload.put("api_key", secret).put("clear_api_key", clearSecret)
                    if (path == "/settings/captcha") payload.put("secret_key", secret).put("clear_secret", clearSecret)
                    model.api.request("/api/admin$path", "PUT", json = payload)
                    secret = ""
                    clearSecret = false
                    model.error = "设置已保存"
                }
            },
            Modifier.fillMaxWidth(),
            enabled = !model.busy && editable,
            style = GlassButtonStyle.Prominent,
        )
    }
}

@Composable
private fun NotificationSettings(model: WallModel, response: JSONObject) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        response.objects("providers").forEach { provider ->
            var enabled by remember(provider.toString()) { mutableStateOf(provider.optBoolean("enabled")) }
            var webhook by remember { mutableStateOf("") }
            var secret by remember { mutableStateOf("") }
            var confirmClear by remember { mutableStateOf(false) }
            val path = "/api/admin/settings/notifications/${provider.s("id")}"
            val canUpdate = model.can("settings.notifications.update")
            GlassPanel(Modifier.fillMaxWidth()) {
                SectionTitle(provider.s("label")) {
                    Text(
                        if (provider.optBoolean("configured")) "已配置" else "尚未配置",
                        style = MaterialTheme.typography.labelMedium,
                        color = Glass.colors.secondary,
                    )
                }
                GlassSwitchRow("启用", enabled, { enabled = it }, enabled = canUpdate)
                GlassTextField(
                    webhook,
                    { webhook = it },
                    Modifier.fillMaxWidth(),
                    label = "新的机器人地址（留空保留）",
                    singleLine = true,
                    secret = true,
                    enabled = canUpdate,
                )
                if (provider.optBoolean("supports_signing_secret"))
                    GlassTextField(
                        secret,
                        { secret = it },
                        Modifier.fillMaxWidth(),
                        label = "新的签名密钥（留空保留）",
                        singleLine = true,
                        secret = true,
                        enabled = canUpdate,
                    )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canUpdate)
                        GlassButton(
                            "保存",
                            {
                                model.perform {
                                    model.api.request(
                                        path,
                                        "PUT",
                                        json = JSONObject().put("enabled", enabled).put("webhook", webhook).put("secret", secret),
                                    )
                                    webhook = ""
                                    secret = ""
                                }
                            },
                            style = GlassButtonStyle.Prominent,
                        )
                    if (model.can("settings.notifications.test"))
                        GlassButton(
                            "发送测试",
                            {
                                model.perform {
                                    model.api.request("$path/test", "POST")
                                    model.error = "测试消息已发送"
                                }
                            },
                        )
                    if (canUpdate) GlassButton("清除", { confirmClear = true }, style = GlassButtonStyle.Danger)
                }
            }
            if (confirmClear)
                GlassConfirmDialog(
                    title = "清除该通知渠道？",
                    confirmText = "清除",
                    danger = true,
                    onDismissRequest = { confirmClear = false },
                    onConfirm = {
                        model.perform {
                            model.api.request(path, "DELETE")
                            confirmClear = false
                        }
                    },
                )
        }
    }
}
