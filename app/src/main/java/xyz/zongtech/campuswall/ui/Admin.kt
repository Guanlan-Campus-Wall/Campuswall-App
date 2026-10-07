@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package xyz.zongtech.campuswall.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import xyz.zongtech.campuswall.R
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.displayError
import xyz.zongtech.campuswall.displayState
import xyz.zongtech.campuswall.objects
import xyz.zongtech.campuswall.pathSegment
import xyz.zongtech.campuswall.s
import xyz.zongtech.campuswall.strings

private data class AdminLink(val id: String, val icon: ImageVector, val label: String, val caps: List<String>)

private val adminGroups =
    listOf(
        "内容" to listOf(
            AdminLink("dashboard", Icons.Rounded.Dashboard, "仪表盘", listOf("dashboard.read")),
            AdminLink("wall", Icons.AutoMirrored.Rounded.Chat, "帖子审核", listOf("content.queue.read")),
            AdminLink("confessions", Icons.Rounded.Favorite, "表白墙审核", listOf("content.queue.read")),
            AdminLink("comments", Icons.Rounded.Description, "评论管理", listOf("content.comment.read")),
            AdminLink("trash", Icons.Rounded.Delete, "内容回收站", listOf("content.trash.read")),
        ),
        "社区" to listOf(
            AdminLink("users", Icons.Rounded.Group, "用户与权限", listOf("users.read")),
            AdminLink("notice", Icons.Rounded.Campaign, "公告管理", listOf("notice.read")),
            AdminLink("feedback", Icons.Rounded.SupportAgent, "反馈工单", listOf("feedback.read")),
            AdminLink("report", Icons.Rounded.Flag, "举报管理", listOf("report.read")),
        ),
        "系统" to listOf(
            AdminLink("notifications", Icons.Rounded.Notifications, "消息提醒", listOf("settings.notifications.read")),
            AdminLink("settings", Icons.Rounded.Lock, "平台与验证", listOf("settings.read")),
            AdminLink("audit", Icons.Rounded.History, "操作审计", listOf("audit.read")),
            AdminLink("log", Icons.Rounded.Description, "管理员日志", listOf("logs.legacy_admin.read")),
            AdminLink("error_log", Icons.Rounded.ErrorOutline, "错误日志", listOf("logs.error.read")),
        ),
    )

/** 网页 AdminShell：左侧（这里是抽屉）分组导航 + 顶栏标题。 */
@Composable
fun AdminScreen(model: WallModel, section: String, go: (String) -> Unit, back: () -> Unit) {
    val c = W
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var themeOpen by remember { mutableStateOf(false) }
    val visible = adminGroups.map { (g, links) -> g to links.filter { l -> l.caps.any { model.can(it) } } }.filter { it.second.isNotEmpty() }
    val all = visible.flatMap { it.second }
    val current = all.firstOrNull { it.id == section } ?: all.firstOrNull { section == "dashboard" && it.id == "dashboard" }
    val active = current?.id ?: all.firstOrNull()?.id
    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = c.surface, drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)) {
                Column(Modifier.fillMaxHeight().statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Image(painterResource(R.drawable.school_badge), null, Modifier.size(32.dp).clip(CircleShape).background(Color.White))
                        Column {
                            Text("观澜校园墙", style = MaterialTheme.typography.titleSmall)
                            Text("管理后台", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                        }
                    }
                    Gap(16.dp)
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        visible.forEach { (group, links) ->
                            Text(group, Modifier.padding(start = 12.dp, top = 12.dp, bottom = 6.dp), style = MaterialTheme.typography.labelSmall, color = c.ink3)
                            links.forEach { link ->
                                val on = link.id == active
                                Row(
                                    Modifier.fillMaxWidth()
                                        .clip(RSm)
                                        .background(if (on) c.accentSoft else Color.Transparent)
                                        .clickable {
                                            scope.launch { drawer.close() }
                                            if (!on) go("admin/${link.id}")
                                        }
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(link.icon, null, Modifier.size(18.dp), tint = if (on) c.accentStrong else c.ink2)
                                    Text(link.label, color = if (on) c.accentStrong else c.ink, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                    Hairline()
                    Gap(12.dp)
                    model.user?.let { u ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Initial(u.s("username"), 30.dp)
                            Text(u.s("username"), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Gap(10.dp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WButton("前台", { go("wall") }, Modifier.weight(1f), small = true, icon = Icons.AutoMirrored.Rounded.OpenInNew)
                        WButton("退出", {
                            model.perform({ go("wall") }) {
                                try {
                                    model.api.request("/api/admin/logout", "POST")
                                } finally {
                                    model.refreshSession()
                                }
                            }
                        }, Modifier.weight(1f), small = true, icon = Icons.AutoMirrored.Rounded.Logout)
                    }
                }
            }
        },
    ) {
        Column(Modifier.fillMaxSize().background(c.bg)) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBtn(Icons.Rounded.Menu, "展开管理菜单", { scope.launch { drawer.open() } })
                Text(current?.label ?: "管理后台", Modifier.weight(1f).padding(start = 6.dp), style = MaterialTheme.typography.headlineSmall)
                IconBtn(if (c.dark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode, "外观与主题", { themeOpen = true })
            }
            Hairline()
            Box(Modifier.weight(1f)) {
                if (model.user == null || all.isEmpty())
                    Column(Modifier.padding(PageGutter)) {
                        EmptyCard(
                            if (model.user == null) "请先登录管理账号" else "当前账号没有后台权限",
                            "管理员、审核员请从登录页的「管理员入口」登录。",
                            Icons.Rounded.Lock,
                            if (model.user == null) "去登录" else "返回前台",
                            { go(if (model.user == null) "login" else "wall") },
                        )
                    }
                else
                    when (active) {
                        "dashboard" -> Dashboard(model, go)
                        "wall" -> ReviewQueue(model, "posts", go)
                        "confessions" -> ReviewQueue(model, "confessions", go)
                        "comments" -> CommentsAdmin(model, go)
                        "trash" -> TrashAdmin(model)
                        "users" -> UsersAdmin(model)
                        "notice" -> NoticeAdmin(model)
                        "feedback" -> FeedbackAdmin(model)
                        "report" -> ReportAdmin(model)
                        "notifications" -> NotificationSettingsAdmin(model)
                        "settings" -> SettingsAdmin(model)
                        "audit" -> AuditAdmin(model)
                        "log" -> LogAdmin(model, error = false)
                        "error_log" -> LogAdmin(model, error = true)
                    }
            }
        }
    }
    if (themeOpen) ThemeSheet(model) { themeOpen = false }
}

/** 后台页面的通用滚动容器。 */
@Composable
private fun AdminList(content: LazyListScope.() -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/** 统一的「加载 → 结果」状态。 */
private class Loader<T>(initial: T) {
    var data by mutableStateOf(initial)
    var loading by mutableStateOf(true)
    var reload by mutableIntStateOf(0)
}

@Composable
private fun <T> rememberLoader(model: WallModel, initial: T, vararg keys: Any?, fetch: suspend () -> T): Loader<T> {
    val loader = remember { Loader(initial) }
    LaunchedEffect(*keys, loader.reload, model.revision) {
        loader.loading = true
        try {
            loader.data = fetch()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        } finally {
            loader.loading = false
        }
    }
    return loader
}

@Composable
private fun CheckBox(checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    val c = W
    Box(
        Modifier.size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) c.accent else c.surface)
            .border(1.5.dp, if (checked) c.accent else c.lineStrong, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, role = Role.Checkbox) { onChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(Icons.Rounded.Check, null, Modifier.size(16.dp), tint = c.accentInk)
    }
}

/** 顶部的筛选按钮组（btn-primary / btn-outline + 计数）。 */
@Composable
private fun FilterButtons(options: List<Pair<String, String>>, selected: String, counts: JSONObject?, onSelect: (String) -> Unit, countKey: (String) -> String = { it }) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            val on = value == selected
            val count = counts?.optInt(countKey(value))
            Row(
                Modifier.clip(RSm)
                    .background(if (on) W.accent else Color.Transparent)
                    .border(1.dp, if (on) Color.Transparent else W.lineStrong, RSm)
                    .clickable { onSelect(value) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(label, color = if (on) W.accentInk else W.ink, style = MaterialTheme.typography.labelMedium)
                if (count != null)
                    Text(
                        "$count",
                        Modifier.clip(Pill).background(if (on) Color.White.copy(0.25f) else W.surface3).padding(horizontal = 7.dp),
                        color = if (on) W.accentInk else W.ink2,
                        fontSize = 12.sp,
                    )
            }
        }
    }
}

@Composable
private fun Toolbar(query: String, onQuery: (String) -> Unit, placeholder: String, onSearch: () -> Unit, onRefresh: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SearchBar(query, onQuery, placeholder, onSearch, Modifier.weight(1f))
        IconBtn(Icons.Rounded.Refresh, "刷新", onRefresh, kind = Btn.Outline, size = 46.dp)
    }
}

@Composable
private fun SelectionBar(allSelected: Boolean, onToggleAll: () -> Unit, summary: String, actions: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RMd).background(W.surface2).border(1.dp, W.line, RMd).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CheckBox(allSelected, { onToggleAll() })
            Text("本页全选", style = MaterialTheme.typography.labelLarge)
            Box(Modifier.weight(1f))
            Text(summary, style = MaterialTheme.typography.labelSmall, color = W.ink3)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { actions() }
    }
}

@Composable
private fun StatTile(value: String, label: String, detail: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RMd).background(W.surface2).border(1.dp, W.line, RMd).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold))
        Text(label, style = MaterialTheme.typography.bodySmall, color = W.ink3)
        detail?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = W.ink3) }
    }
}

@Composable
private fun StatGrid(tiles: List<Triple<String, String, String?>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (v, l, d) -> StatTile(v, l, d, Modifier.weight(1f)) }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

private fun formatTime(value: String): String {
    if (value.isBlank()) return "-"
    return runCatching { OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss")) }
        .recoverCatching { Instant.parse(value).atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss")) }
        .getOrDefault(value)
}

// ---------------------------------------------------------------- 仪表盘

@Composable
private fun Dashboard(model: WallModel, go: (String) -> Unit) {
    val c = W
    val loader = rememberLoader(model, JSONObject()) { model.api.request("/api/admin/dashboard/stats") }
    val r = loader.data
    val stats = r.optJSONObject("stats") ?: JSONObject()
    val m = stats.optJSONObject("messages") ?: JSONObject()
    val reports = stats.optJSONObject("reports") ?: JSONObject()
    val managers = stats.optJSONObject("managers") ?: JSONObject()
    val trash = stats.optJSONObject("trash") ?: JSONObject()
    val feedback = stats.optJSONObject("feedback") ?: JSONObject()
    val community = stats.optJSONObject("community") ?: JSONObject()
    val audit = stats.optJSONObject("audit") ?: JSONObject()
    val canReview = model.can("content.queue.read")
    val reviewOnly = canReview && !listOf("content.trash.read", "content.message.hide", "content.comment.read").all { model.can(it) }
    AdminList {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("平台运行概览", style = MaterialTheme.typography.headlineSmall)
                    Text(if (loader.loading) "正在读取最新数据" else "数据生成于 ${formatTime(r.s("generated_at"))}", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                }
                WButton("刷新", { loader.reload++ }, small = true, icon = Icons.Rounded.Refresh, enabled = !loader.loading)
            }
        }
        item {
            val tiles = mutableListOf<@Composable (Modifier) -> Unit>()
            fun metric(icon: ImageVector, label: String, value: Int, detail: String, tone: Int) =
                tiles.add { mod -> Metric(icon, label, value, detail, tone, mod) }
            if (canReview && reviewOnly) {
                metric(Icons.AutoMirrored.Rounded.Chat, "待审核帖子", m.optInt("pending_posts"), "校园动态与其他内容", if (m.optInt("pending_posts") > 0) 3 else 2)
                metric(Icons.Rounded.Favorite, "待审核表白", m.optInt("pending_confessions"), "表白墙便签独立队列", if (m.optInt("pending_confessions") > 0) 3 else 2)
            }
            if (canReview && !reviewOnly) metric(Icons.AutoMirrored.Rounded.Chat, "公开内容", m.optInt("visible"), "今日新增 ${m.optInt("last_24_hours")}，累计 ${m.optInt("total")}", 1)
            if (model.can("report.read")) metric(Icons.Rounded.Flag, "待处理举报", reports.optInt("total"), "近 7 天处理 ${reports.optInt("processed_last_7_days")} 条，累计 ${reports.optInt("processed_total")} 条", if (reports.optInt("total") > 0) 3 else 2)
            if (model.can("users.read")) metric(Icons.Rounded.Lock, "管理员账号", managers.optInt("active"), "${managers.optInt("disabled")} 个停用，${managers.optInt("super_admins", managers.optInt("super_admin"))} 个账号管理者", 2)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { tiles.forEach { it(Modifier.fillMaxWidth()) } }
        }
        if (canReview && !reviewOnly) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("近 7 天发布趋势", style = MaterialTheme.typography.titleMedium)
                            Text("公开留言共新增 ${m.optInt("last_7_days")} 条", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                        }
                        Badge("7 天")
                    }
                    val daily = m.objects("daily")
                    val max = (daily.maxOfOrNull { it.optInt("count") } ?: 0).coerceAtLeast(1)
                    Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                        daily.forEach { d ->
                            val count = d.optInt("count")
                            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                Text("$count", style = MaterialTheme.typography.labelSmall, color = c.ink2)
                                Box(Modifier.weight(1f).width(22.dp).clip(RoundedCornerShape(6.dp)).background(c.surface3), contentAlignment = Alignment.BottomCenter) {
                                    Box(Modifier.fillMaxWidth().fillMaxHeight((count / max.toFloat()).coerceAtLeast(if (count > 0) 0.1f else 0.03f)).background(c.accent))
                                }
                                Text(d.s("label"), style = MaterialTheme.typography.labelSmall, color = c.ink3, maxLines = 1)
                            }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), spacing = 4.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("内容治理", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        WButton("帖子审核", { go("admin/wall") }, small = true)
                        HGap(6.dp)
                        WButton("表白审核", { go("admin/confessions") }, small = true)
                    }
                    Gap(6.dp)
                    Fact("待审核帖子", "${m.optInt("pending_posts")}")
                    Fact("待审核表白", "${m.optInt("pending_confessions")}")
                    Fact("待审核后公开", "${m.optInt("awaiting_publication")}")
                    Fact("已下架", "${m.optInt("hidden")}")
                    Fact("已下架评论", "${m.optInt("comments_hidden")}")
                    Fact("回收站", "${trash.optInt("all")}")
                    if (model.can("feedback.read")) Fact("待跟进反馈", "${feedback.optInt("pending") + feedback.optInt("in_progress")}")
                    if (model.can("settings.read")) {
                        Fact("发帖 / 评论", "${if (community.optBoolean("posting_enabled", true)) "开" else "关"} / ${if (community.optBoolean("commenting_enabled", true)) "开" else "关"}")
                        Fact("发帖预审", if (community.optBoolean("require_post_approval")) "开启" else "关闭")
                    }
                    Fact("置顶 / 精华", "${m.optInt("pinned")} / ${m.optInt("featured")}")
                    Fact("评论与表态", "${m.optInt("likes") + m.optInt("dislikes") + m.optInt("comments")}")
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text("热门分区", style = MaterialTheme.typography.titleMedium)
                    Text("按当前公开留言数量统计", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    val tags = m.objects("top_tags")
                    if (tags.isEmpty()) Text("暂时没有分区数据", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.forEach { t -> Tag("#${t.s("tag")}  ${t.optInt("count")}") { go(tagRoute(t.s("tag"))) } }
                    }
                }
            }
        }
        item {
            val quick =
                buildList {
                    if (canReview) {
                        add(Triple(Icons.AutoMirrored.Rounded.Chat, "帖子审核", "${m.optInt("pending_posts")}") to "wall")
                        add(Triple(Icons.Rounded.Favorite, "表白墙审核", "${m.optInt("pending_confessions")}") to "confessions")
                    }
                    if (model.can("notice.read")) add(Triple(Icons.Rounded.Campaign, "公告管理", "发布") to "notice")
                    if (model.can("content.comment.read")) add(Triple(Icons.Rounded.Description, "评论管理", "${m.optInt("comments_hidden")}") to "comments")
                    if (model.can("content.trash.read")) add(Triple(Icons.Rounded.Delete, "内容回收站", "${trash.optInt("all")}") to "trash")
                    if (model.can("users.read")) add(Triple(Icons.Rounded.Lock, "用户与权限", "${managers.optInt("total")}") to "users")
                    if (model.can("report.read")) add(Triple(Icons.Rounded.Flag, "举报管理", "${reports.optInt("total")}") to "report")
                    if (model.can("feedback.read")) add(Triple(Icons.Rounded.SupportAgent, "反馈工单", "${feedback.optInt("pending") + feedback.optInt("in_progress")}") to "feedback")
                    if (model.can("settings.read")) add(Triple(Icons.Rounded.Tune, "平台设置", if (community.optBoolean("posting_enabled", true) && community.optBoolean("commenting_enabled", true)) "开放" else "受限") to "settings")
                    if (model.can("audit.read")) add(Triple(Icons.Rounded.History, "操作审计", "${audit.optInt("last_7_days")}") to "audit")
                }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quick.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (info, id) ->
                            Row(
                                Modifier.weight(1f).clip(RMd).background(c.surface).border(1.dp, c.line, RMd).clickable { go("admin/$id") }.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(info.first, null, Modifier.size(18.dp), tint = c.accent)
                                Text(info.second, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                Text(info.third, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (row.size == 1) Box(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(icon: ImageVector, label: String, value: Int, detail: String, tone: Int, modifier: Modifier) {
    val (fg, bg) = W.tones[tone]
    Card(modifier, padding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TileIcon(icon, fg, bg)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = W.ink3)
                Text("$value", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold))
                Text(detail, style = MaterialTheme.typography.labelSmall, color = W.ink3)
            }
        }
    }
}

// ---------------------------------------------------------------- 帖子 / 表白审核

private val reviewStatuses =
    listOf("pending" to "待审核", "approved" to "已通过", "awaiting_publication" to "待公开", "visible" to "公开中", "hidden" to "已下架", "all" to "全部")

@Composable
private fun ReviewBadge(m: JSONObject) {
    when {
        m.s("review_status") == "approved" -> Badge("已审核", Tone.Success, Icons.Rounded.CheckCircle)
        m.s("moderation_status") == "pending" ->
            Badge(if (m.s("review_source") in listOf("lexicon", "openai", "ai_unavailable")) "AI 未通过 · 待人工" else "待审核", Tone.Warning, Icons.Rounded.HourglassTop)
        m.s("moderation_status") == "hidden" -> Badge("待复核", Tone.Warning, Icons.Rounded.History)
        else -> Badge("待复核 · 已公开", Tone.Warning, Icons.Rounded.History)
    }
}

@Composable
private fun BoundUser(m: JSONObject) {
    val c = W
    when {
        m.optBoolean("identity_redacted") || m.optBoolean("review_identity_redacted") || m.optBoolean("author_redacted") ->
            Text("发布者身份已隐藏，审核时只需判断内容是否符合社区规范", style = MaterialTheme.typography.bodySmall, color = c.ink3)
        m.optBoolean("official") || m.s("author_type") == "admin" -> Badge("官方账号发布" + m.s("admin_username").let { if (it.isBlank()) "" else " · $it" })
        m.s("user_id").isBlank() -> Text("匿名或未登录发布（审核页不展示可识别身份）", style = MaterialTheme.typography.bodySmall, color = c.ink3)
        else -> {
            val u = m.optJSONObject("user") ?: JSONObject()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Badge(if (m.optBoolean("anonymous", true)) "匿名" else "非匿名")
                Badge("用户名：${u.s("username", m.s("username")).ifBlank { "-" }}")
                Badge("昵称：${u.s("nickname", m.s("display_name_snapshot")).ifBlank { "-" }}")
                Badge("状态：${u.s("status").ifBlank { "-" }}")
            }
        }
    }
}

@Composable
private fun ReviewQueue(model: WallModel, scope: String, go: (String) -> Unit) {
    val c = W
    val item = if (scope == "confessions") "便签" else "帖子"
    val unit = if (scope == "confessions") "张" else "条"
    val canReview = model.can("content.review")
    val canPin = model.can("content.message.pin")
    val canFeature = model.can("content.message.feature")
    val canHide = model.can("content.message.hide")
    val canDelete = model.can("content.message.delete")
    val canRepair = model.can("content.media.repair")
    val canDeleteComment = model.can("content.comment.delete")
    val canSelect = canReview || canHide
    var status by remember(scope) { mutableStateOf("pending") }
    var query by remember(scope) { mutableStateOf("") }
    var applied by remember(scope) { mutableStateOf("") }
    var page by remember(scope) { mutableIntStateOf(1) }
    var selected by remember(scope, status, page) { mutableStateOf(emptySet<Long>()) }
    var detail by remember { mutableStateOf<JSONObject?>(null) }
    var deleteTarget by remember { mutableStateOf<JSONObject?>(null) }
    var hideTarget by remember { mutableStateOf<JSONObject?>(null) }
    var hideReason by remember { mutableStateOf("违反社区规范") }
    val visibleStatuses = if (canHide) reviewStatuses else reviewStatuses.filter { it.first in listOf("pending", "approved", "awaiting_publication") }
    val loader =
        rememberLoader(model, JSONObject(), scope, status, applied, page) {
            model.api.request("/api/admin/api/messages?q=${applied.pathSegment()}&page=$page&status=$status&scope=$scope")
        }
    val data = loader.data
    val rows = data.objects("messages")
    val scopeCounts = data.optJSONObject("scope_counts")
    fun reload() = loader.reload++
    fun act(message: String, call: suspend () -> Unit) = model.perform({ reload() }) {
        call()
        model.error = message
    }
    fun openDetail(id: String) {
        model.perform {
            val r = model.api.request("/api/admin/api/get_message/$id")
            detail = r.optJSONObject("message") ?: r.optJSONObject("data") ?: r
        }
    }
    AdminList {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WButton("帖子审核 ${scopeCounts?.optJSONObject("posts")?.optInt("pending") ?: 0}", { if (scope != "posts") go("admin/wall") }, Modifier.weight(1f), kind = if (scope == "posts") Btn.Primary else Btn.Outline, icon = Icons.AutoMirrored.Rounded.Chat)
                WButton("表白墙审核 ${scopeCounts?.optJSONObject("confessions")?.optInt("pending") ?: 0}", { if (scope != "confessions") go("admin/confessions") }, Modifier.weight(1f), kind = if (scope == "confessions") Btn.Primary else Btn.Outline, icon = Icons.Rounded.Favorite)
            }
        }
        item { FilterButtons(visibleStatuses, status, data.optJSONObject("counts"), { status = it; page = 1 }) }
        item { Toolbar(query, { query = it }, "搜索${item}内容", { applied = query.trim(); page = 1 }, { reload() }) }
        if (canSelect)
            item {
                val ids = rows.map { it.optLong("id") }
                SelectionBar(
                    ids.isNotEmpty() && selected.containsAll(ids),
                    { selected = if (selected.containsAll(ids)) emptySet() else ids.toSet() },
                    "当前筛选 ${data.optInt("total")} $unit，已选 ${selected.size} $unit",
                ) {
                    fun bulk(action: String, extra: JSONObject.() -> Unit = {}) = model.perform({ reload() }) {
                        val r = model.api.request("/api/admin/messages/bulk-moderation", "POST", json = JSONObject().put("message_ids", JSONArray(selected.toList())).put("action", action).apply(extra))
                        model.error = "成功处理 ${r.optInt("succeeded")} $unit$item" + if (r.optInt("failed") > 0) "，${r.optInt("failed")} ${unit}未完成" else ""
                        selected = emptySet()
                    }
                    if (canReview) WButton("批量通过", { bulk("approve") }, kind = Btn.Success, small = true, enabled = selected.isNotEmpty() && !model.busy, icon = Icons.Rounded.CheckCircle)
                    if (canReview) WButton("批量退回", { bulk("return") }, small = true, enabled = selected.isNotEmpty() && !model.busy, icon = Icons.AutoMirrored.Rounded.Undo)
                    if (canHide)
                        WButton("批量下架", {
                            hideReason = "违反社区规范"
                            hideTarget = JSONObject().put("bulk", true)
                        }, kind = Btn.Danger, small = true, enabled = selected.isNotEmpty() && !model.busy, icon = Icons.Rounded.VisibilityOff)
                }
            }
        if (loader.loading) item { Spinner() }
        if (!loader.loading && rows.isEmpty()) item { EmptyCard("当前队列为空", "没有符合当前筛选条件的$item。", Icons.Rounded.Inbox) }
        items(rows, key = { it.s("id") }) { m ->
            val id = m.optLong("id")
            val hidden = m.s("moderation_status") == "hidden"
            Card(Modifier.fillMaxWidth(), color = if (hidden) c.surface2 else c.surface) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (canSelect) CheckBox(id in selected, { v -> selected = if (v) selected + id else selected - id })
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Badge("#$id")
                            Badge(if (scope == "confessions") "表白便签" else "帖子")
                            ReviewBadge(m)
                            if (m.optBoolean("pinned")) Badge("置顶", Tone.Warning, Icons.Rounded.PushPin)
                            if (m.optBoolean("featured")) Badge("精华", Tone.Success, Icons.Rounded.Star)
                            if (hidden) Badge("已下架", Tone.Danger, Icons.Rounded.VisibilityOff)
                            m.optJSONObject("poll")?.let { Badge("投票 ${it.optInt("total_votes")} 票") }
                        }
                        Text(m.s("timestamp", m.s("time")), style = MaterialTheme.typography.labelSmall, color = c.ink3)
                        Text(m.s("text").ifBlank { m.optJSONObject("poll")?.s("question") ?: "附件$item" }, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        if (hidden) Text("下架原因：${m.s("hidden_reason", "违反社区规范")}", style = MaterialTheme.typography.bodySmall, color = c.danger)
                        BoundUser(m)
                        Text("♥ ${m.optInt("likes")}   💬 ${m.optJSONArray("comments")?.length() ?: 0}", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (canReview) {
                            val approved = m.s("review_status") == "approved"
                            WButton(if (approved) "退回待审" else "通过审核", {
                                act(if (approved) "${item}已退回待审" else "${item}已通过审核") {
                                    model.api.request("/api/admin/messages/$id/review", "POST", json = JSONObject().put("action", if (approved) "return" else "approve"))
                                }
                            }, Modifier.weight(1f), kind = if (approved) Btn.Outline else Btn.Success, small = true, enabled = !model.busy)
                        }
                        WButton("详情", { openDetail("$id") }, Modifier.weight(1f), small = true, icon = Icons.Rounded.Info)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (canPin)
                            WButton(if (m.optBoolean("pinned")) "取消置顶" else "置顶", {
                                act("${item}管理状态已更新") { model.api.request("/api/admin/messages/$id/moderation", "POST", json = JSONObject().put("pinned", !m.optBoolean("pinned"))) }
                            }, Modifier.weight(1f), small = true, icon = Icons.Rounded.PushPin, enabled = !model.busy)
                        if (canFeature)
                            WButton(if (m.optBoolean("featured")) "取消精华" else "设为精华", {
                                act("${item}管理状态已更新") { model.api.request("/api/admin/messages/$id/moderation", "POST", json = JSONObject().put("featured", !m.optBoolean("featured"))) }
                            }, Modifier.weight(1f), small = true, icon = Icons.Rounded.Star, enabled = !model.busy)
                    }
                    if (canHide)
                        WButton(if (hidden) "恢复" else "下架$item", {
                            if (hidden) act("${item}已恢复") { model.api.request("/api/admin/messages/$id/moderation", "POST", json = JSONObject().put("hidden", false)) }
                            else {
                                hideReason = m.s("hidden_reason").ifBlank { "违反社区规范" }
                                hideTarget = m
                            }
                        }, Modifier.fillMaxWidth(), small = true, kind = if (hidden) Btn.Success else Btn.Outline, icon = if (hidden) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff, enabled = !model.busy)
                    if (canRepair && m.strings("files").isNotEmpty())
                        WButton("修复媒体缩略图", { act("媒体缩略文件已重新生成") { model.api.request("/api/admin/repair_message/$id", "POST") } }, Modifier.fillMaxWidth(), small = true, icon = Icons.Rounded.Build, enabled = !model.busy)
                    if (canDelete) WButton("移入回收站", { deleteTarget = m }, Modifier.fillMaxWidth(), kind = Btn.Danger, small = true, icon = Icons.Rounded.Delete, enabled = !model.busy)
                }
            }
        }
        item { Pager(page, data.optInt("total_pages"), { page = it }) }
    }
    detail?.let { m ->
        val id = m.s("id")
        Sheet("${if (scope == "confessions") "表白便签" else "帖子"}详情 #$id", { detail = null }) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ReviewBadge(m)
                if (m.optBoolean("official")) Badge("官方帖子")
                if (m.s("moderation_status") == "hidden") Badge("已下架", Tone.Danger)
                if (m.optBoolean("pinned")) Badge("已置顶", Tone.Warning)
                if (m.optBoolean("featured")) Badge("精华", Tone.Success)
            }
            StatGrid(
                listOf(
                    Triple(formatTime(m.s("timestamp", m.s("time"))), "提交时间", null),
                    Triple(
                        when (m.s("moderation_status")) {
                            "pending" -> "等待审核"
                            "hidden" -> "已下架"
                            else -> "公开中"
                        },
                        "公开状态",
                        null,
                    ),
                    Triple("${m.optInt("likes")} 赞 · ${m.optInt("dislikes")} 踩", "互动", null),
                    Triple("${m.optJSONArray("comments")?.length() ?: 0} 条", "评论", null),
                )
            )
            Callout("发布者", icon = Icons.Rounded.Info)
            BoundUser(m)
            FlatCard(Modifier.fillMaxWidth()) {
                Text("${item}内容", style = MaterialTheme.typography.titleSmall)
                Text(m.s("text").ifBlank { m.optJSONObject("poll")?.s("question") ?: "此${item}没有文字内容" }, style = MaterialTheme.typography.bodyMedium)
                val tags = m.strings("tags")
                if (tags.isNotEmpty()) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { tags.forEach { Badge("#$it") } }
            }
            m.optJSONObject("poll")?.let { poll ->
                FlatCard(Modifier.fillMaxWidth()) {
                    Text("投票 · ${poll.optInt("total_votes")} 票", style = MaterialTheme.typography.titleSmall)
                    Text(poll.s("question"), style = MaterialTheme.typography.bodyMedium)
                    poll.objects("options").forEach { o -> Fact(o.s("text", o.s("label")), "${o.optInt("votes")} 票") }
                }
            }
            val files = m.strings("files").ifEmpty { m.strings("filenames") }
            if (files.isNotEmpty()) {
                Text("附件（${files.size}）", style = MaterialTheme.typography.titleSmall)
                MediaGrid(files, model.api, limit = files.size, compact = true)
            }
            if (m.s("moderation_status") == "hidden") Callout("下架原因：${m.s("hidden_reason", "违反社区规范")}", Tone.Danger, Icons.Rounded.VisibilityOff)
            if (canReview && m.s("review_status") != "approved")
                WButton("通过这条$item", {
                    model.perform({
                        detail = null
                        reload()
                    }) { model.api.request("/api/admin/messages/$id/review", "POST", json = JSONObject().put("action", "approve")) }
                }, Modifier.fillMaxWidth(), kind = Btn.Success, icon = Icons.Rounded.CheckCircle)
            Text("评论（${m.optJSONArray("comments")?.length() ?: 0}）", style = MaterialTheme.typography.titleSmall)
            val comments = m.objects("comments")
            if (comments.isEmpty()) Text("暂无评论", Modifier.fillMaxWidth().clip(RMd).background(c.surface2).padding(14.dp), color = c.ink3)
            comments.forEach { cm ->
                FlatCard(Modifier.fillMaxWidth()) {
                    Text(cm.s("text"), style = MaterialTheme.typography.bodyMedium)
                    val u = cm.optJSONObject("user")
                    Text(
                        (if (u != null) "评论用户：${u.s("nickname").ifBlank { u.s("username").ifBlank { "已登录用户" } }}" else "匿名评论") + " · ${formatTime(cm.s("timestamp", cm.s("time")))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.ink3,
                    )
                    if (canDeleteComment)
                        WButton("移入回收站", {
                            model.perform({ openDetail(id) }) { model.api.request("/api/admin/api/delete_comment/$id/${cm.s("id").pathSegment()}", "POST") }
                        }, kind = Btn.Danger, small = true)
                }
            }
        }
    }
    deleteTarget?.let { m ->
        Confirm("将${item}移入回收站", "$item #${m.s("id")} 将立即从公开页面和管理队列移除，可在「内容回收站」恢复或彻底删除。", "移入回收站", { deleteTarget = null }, {
            model.perform({
                deleteTarget = null
                reload()
            }) {
                model.api.request("/api/admin/delete_message/${m.s("id")}", "POST")
                model.error = "${item}已移入回收站"
            }
        }, enabled = !model.busy)
    }
    hideTarget?.let { t ->
        val bulk = t.optBoolean("bulk")
        Sheet(if (bulk) "批量下架 ${selected.size} $unit$item" else "下架$item #${t.s("id")}", { hideTarget = null }, footer = {
            WButton("取消", { hideTarget = null })
            WButton("确认下架", {
                model.perform({
                    hideTarget = null
                    reload()
                }) {
                    if (bulk) {
                        model.api.request("/api/admin/messages/bulk-moderation", "POST", json = JSONObject().put("message_ids", JSONArray(selected.toList())).put("action", "hide").put("hidden_reason", hideReason.trim()))
                        selected = emptySet()
                    } else model.api.request("/api/admin/messages/${t.s("id")}/moderation", "POST", json = JSONObject().put("hidden", true).put("hidden_reason", hideReason.trim()))
                    model.error = "${item}已下架"
                }
            }, kind = Btn.Danger, enabled = hideReason.isNotBlank() && !model.busy)
        }) {
            Text("下架后公开页面不可见，登录作者仍可在「我的发布」中查看原因。", style = MaterialTheme.typography.bodySmall, color = c.ink3)
            Field(hideReason, { hideReason = it }, label = "下架原因", placeholder = "请输入给作者看的下架原因", singleLine = false, minLines = 3, maxLength = 200)
        }
    }
}

// ---------------------------------------------------------------- 评论管理

@Composable
private fun CommentsAdmin(model: WallModel, go: (String) -> Unit) {
    val c = W
    val canHide = model.can("content.comment.hide")
    val canDelete = model.can("content.comment.delete")
    var status by remember { mutableStateOf("all") }
    var query by remember { mutableStateOf("") }
    var applied by remember { mutableStateOf("") }
    var page by remember { mutableIntStateOf(1) }
    var selected by remember(status, page) { mutableStateOf(emptySet<String>()) }
    var hideTarget by remember { mutableStateOf<JSONObject?>(null) }
    var hideReason by remember { mutableStateOf("违反社区规范") }
    var deleteTarget by remember { mutableStateOf<JSONObject?>(null) }
    val loader = rememberLoader(model, JSONObject(), status, applied, page) {
        model.api.request("/api/admin/comments?status=$status&q=${applied.pathSegment()}&page=$page&page_size=20")
    }
    val data = loader.data
    val rows = data.objects("comments")
    fun key(cm: JSONObject) = "${cm.s("message_id")}:${cm.s("id")}"
    fun targets() = JSONArray(rows.filter { key(it) in selected }.map { JSONObject().put("message_id", it.opt("message_id")).put("comment_id", it.s("id")) })
    AdminList {
        item { FilterButtons(listOf("all" to "全部", "visible" to "公开中", "hidden" to "已下架"), status, data.optJSONObject("counts"), { status = it; page = 1 }) }
        item { Toolbar(query, { query = it }, "搜索评论或原帖内容...", { applied = query.trim(); page = 1 }, { loader.reload++ }) }
        if (canHide)
            item {
                val keys = rows.map { key(it) }
                SelectionBar(keys.isNotEmpty() && selected.containsAll(keys), { selected = if (selected.containsAll(keys)) emptySet() else keys.toSet() }, "当前筛选 ${data.optInt("total")} 条，已选 ${selected.size} 条") {
                    WButton("批量下架", { hideReason = "违反社区规范"; hideTarget = JSONObject().put("bulk", true) }, kind = Btn.Danger, small = true, enabled = selected.isNotEmpty() && !model.busy, icon = Icons.Rounded.VisibilityOff)
                    WButton("批量恢复", {
                        model.perform({ loader.reload++ }) {
                            val r = model.api.request("/api/admin/comments/bulk-moderation", "POST", json = JSONObject().put("action", "restore").put("targets", targets()))
                            model.error = "成功处理 ${r.optInt("succeeded")} 条评论"
                            selected = emptySet()
                        }
                    }, kind = Btn.Success, small = true, enabled = selected.isNotEmpty() && !model.busy, icon = Icons.Rounded.Visibility)
                }
            }
        if (loader.loading) item { Spinner() }
        if (!loader.loading && rows.isEmpty()) item { EmptyCard("当前没有评论", "没有符合当前条件的评论记录。", Icons.Rounded.Inbox) }
        items(rows, key = { key(it) }) { cm ->
            val hidden = cm.s("moderation_status") == "hidden"
            Card(Modifier.fillMaxWidth(), color = if (hidden) c.surface2 else c.surface) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (canHide) CheckBox(key(cm) in selected, { v -> selected = if (v) selected + key(cm) else selected - key(cm) })
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Badge(if (hidden) "已下架" else "公开中", if (hidden) Tone.Danger else Tone.Success, if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility)
                            Badge("留言 #${cm.s("message_id")} · ${cm.s("floor")} 楼")
                            if (cm.s("refer_id").isNotBlank()) Badge("回复", icon = Icons.AutoMirrored.Rounded.Reply)
                        }
                        Text(cm.s("timestamp").ifBlank { "未知时间" }, style = MaterialTheme.typography.labelSmall, color = c.ink3)
                        Text(cm.s("text").ifBlank { if (cm.strings("files").isNotEmpty()) "附件评论" else "空评论" }, style = MaterialTheme.typography.bodyMedium)
                        if (cm.s("refer_id").isNotBlank()) Text("引用：${cm.s("refer").ifBlank { "原评论不可见" }}", Modifier.clip(RSm).background(c.surface3).padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = c.ink2)
                        if (hidden) Text("下架原因：${cm.s("hidden_reason", "违反社区规范")}", style = MaterialTheme.typography.bodySmall, color = c.danger)
                        val u = cm.optJSONObject("user") ?: JSONObject()
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Badge("用户名：${u.s("username", cm.s("username")).ifBlank { "-" }}")
                            Badge("姓名：${u.s("real_name").ifBlank { "-" }}")
                            Badge("昵称：${u.s("nickname").ifBlank { "-" }}")
                            Badge("账号：${u.s("status").ifBlank { "-" }}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("原帖：${cm.s("message_preview").ifBlank { "附件留言" }}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = c.ink3, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (cm.s("message_moderation_status") == "visible") TextLink("打开原帖", Icons.AutoMirrored.Rounded.OpenInNew) { go("message/${cm.s("message_id")}") }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canHide)
                        WButton(if (hidden) "恢复公开" else "下架评论", {
                            if (hidden)
                                model.perform({ loader.reload++ }) {
                                    model.api.request("/api/admin/comments/${cm.s("message_id")}/${cm.s("id").pathSegment()}/moderation", "POST", json = JSONObject().put("hidden", false).put("hidden_reason", ""))
                                    model.error = "评论已恢复公开"
                                }
                            else {
                                hideReason = cm.s("hidden_reason").ifBlank { "违反社区规范" }
                                hideTarget = cm
                            }
                        }, Modifier.weight(1f), kind = if (hidden) Btn.Success else Btn.Outline, small = true, enabled = !model.busy)
                    if (canDelete) WButton("移入回收站", { deleteTarget = cm }, Modifier.weight(1f), kind = Btn.Danger, small = true, icon = Icons.Rounded.Delete, enabled = !model.busy)
                }
            }
        }
        item { Pager(page, data.optInt("total_pages"), { page = it }) }
    }
    hideTarget?.let { t ->
        val bulk = t.optBoolean("bulk")
        Sheet(if (bulk) "批量下架 ${selected.size} 条评论" else "下架评论 · 留言 #${t.s("message_id")}", { hideTarget = null }, footer = {
            WButton("取消", { hideTarget = null })
            WButton("确认下架", {
                model.perform({ hideTarget = null; loader.reload++ }) {
                    if (bulk) {
                        model.api.request("/api/admin/comments/bulk-moderation", "POST", json = JSONObject().put("action", "hide").put("hidden_reason", hideReason.trim()).put("targets", targets()))
                        selected = emptySet()
                    } else model.api.request("/api/admin/comments/${t.s("message_id")}/${t.s("id").pathSegment()}/moderation", "POST", json = JSONObject().put("hidden", true).put("hidden_reason", hideReason.trim()))
                    model.error = "评论已下架"
                }
            }, kind = Btn.Danger, enabled = hideReason.isNotBlank() && !model.busy)
        }) {
            Field(hideReason, { hideReason = it }, label = "下架原因", singleLine = false, minLines = 3, maxLength = 200)
        }
    }
    deleteTarget?.let { cm ->
        Confirm("将评论移入回收站", "评论将从公开页面和评论管理队列移除，可在「内容回收站」恢复或彻底删除。", "移入回收站", { deleteTarget = null }, {
            model.perform({ deleteTarget = null; loader.reload++ }) {
                model.api.request("/api/admin/api/delete_comment/${cm.s("message_id")}/${cm.s("id").pathSegment()}", "POST")
                model.error = "评论已移入回收站"
            }
        })
    }
}

// ---------------------------------------------------------------- 回收站

@Composable
private fun TrashAdmin(model: WallModel) {
    val c = W
    val canRestoreMessage = model.can("content.message.restore")
    val canPurgeMessage = model.can("content.message.purge")
    val canRestoreComment = model.can("content.comment.restore")
    val canPurgeComment = model.can("content.comment.purge")
    fun canRestore(i: JSONObject) = if (i.s("type") == "message") canRestoreMessage else canRestoreComment
    fun canPurge(i: JSONObject) = if (i.s("type") == "message") canPurgeMessage else canPurgeComment
    var type by remember { mutableStateOf("all") }
    var query by remember { mutableStateOf("") }
    var applied by remember { mutableStateOf("") }
    var page by remember { mutableIntStateOf(1) }
    var selected by remember(type, page) { mutableStateOf(emptySet<String>()) }
    var purgeTarget by remember { mutableStateOf<JSONObject?>(null) }
    val loader = rememberLoader(model, JSONObject(), type, applied, page) {
        model.api.request("/api/admin/trash?type=$type&q=${applied.pathSegment()}&page=$page&page_size=20")
    }
    val data = loader.data
    val rows = data.objects("items")
    fun key(i: JSONObject) = "${i.s("type")}:${i.s("message_id")}:${i.s("comment_id")}"
    fun chosen() = rows.filter { key(it) in selected }
    fun target(i: JSONObject) = JSONObject().put("type", i.s("type")).put("message_id", i.opt("message_id")).put("comment_id", i.s("comment_id"))
    fun path(i: JSONObject) = if (i.s("type") == "message") "/api/admin/trash/messages/${i.s("message_id")}" else "/api/admin/trash/comments/${i.s("message_id")}/${i.s("comment_id").pathSegment()}"
    val counts = data.optJSONObject("counts") ?: JSONObject()
    AdminList {
        item {
            FilterButtons(listOf("all" to "全部", "message" to "留言", "comment" to "评论"), type, counts, { type = it; page = 1 }) { if (it == "all") "all" else "${it}s" }
        }
        item { Toolbar(query, { query = it }, "搜索内容、账号、操作者", { applied = query.trim(); page = 1 }, { loader.reload++ }) }
        if (canRestoreMessage || canRestoreComment || canPurgeMessage || canPurgeComment)
            item {
                val keys = rows.filter { canRestore(it) || canPurge(it) }.map { key(it) }
                SelectionBar(keys.isNotEmpty() && selected.containsAll(keys), { selected = if (selected.containsAll(keys)) emptySet() else keys.toSet() }, "当前筛选 ${data.optInt("total")} 项，已选 ${selected.size} 项") {
                    if (canRestoreMessage || canRestoreComment)
                        WButton("批量恢复", {
                            model.perform({ loader.reload++ }) {
                                val r = model.api.request("/api/admin/trash/bulk", "POST", json = JSONObject().put("action", "restore").put("targets", JSONArray(chosen().map { target(it) })))
                                model.error = "成功处理 ${r.optInt("succeeded")} 项"
                                selected = emptySet()
                            }
                        }, kind = Btn.Success, small = true, enabled = chosen().isNotEmpty() && chosen().all { canRestore(it) } && !model.busy, icon = Icons.AutoMirrored.Rounded.Undo)
                    if (canPurgeMessage || canPurgeComment)
                        WButton("彻底删除", { purgeTarget = JSONObject().put("bulk", true) }, kind = Btn.Danger, small = true, enabled = chosen().isNotEmpty() && chosen().all { canPurge(it) } && !model.busy, icon = Icons.Rounded.Delete)
                }
            }
        if (loader.loading) item { Spinner() }
        if (!loader.loading && rows.isEmpty()) item { EmptyCard("回收站为空", icon = Icons.Rounded.Delete) }
        items(rows, key = { key(it) }) { i ->
            Card(Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (canRestore(i) || canPurge(i)) CheckBox(key(i) in selected, { v -> selected = if (v) selected + key(i) else selected - key(i) })
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Badge(if (i.s("type") == "message") "留言" else "评论", Tone.Danger)
                            Badge("留言 #${i.s("message_id")}" + i.s("floor").let { if (it.isBlank()) "" else " · $it 楼" })
                            Badge(when (i.s("deletion_origin")) {
                                "user" -> "用户自行删除"
                                "admin" -> "后台操作"
                                "report" -> "举报处置"
                                else -> "系统迁移"
                            })
                        }
                        Text(formatTime(i.s("deleted_at")), style = MaterialTheme.typography.labelSmall, color = c.ink3)
                        Text(i.s("text").ifBlank { "附件内容" }, style = MaterialTheme.typography.bodyMedium)
                        if (i.s("type") == "comment") Text("原帖：${i.s("message_preview").ifBlank { "附件留言" }}", style = MaterialTheme.typography.bodySmall, color = c.ink3, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "操作者：${i.s("deleted_by").ifBlank { "-" }}　原因：${i.s("deletion_reason").ifBlank { "-" }}　" +
                                if (i.s("user_id").isNotBlank()) "绑定账号：${i.optJSONObject("user")?.s("username")?.ifBlank { null } ?: i.s("username").ifBlank { i.s("user_id") }}" else "游客内容",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.ink3,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canRestore(i))
                        WButton("恢复", {
                            model.perform({ loader.reload++ }) {
                                model.api.request("${path(i)}/restore", "POST", json = JSONObject())
                                model.error = "内容已恢复"
                            }
                        }, Modifier.weight(1f), kind = Btn.Success, small = true, enabled = !model.busy)
                    if (canPurge(i)) WButton("彻底删除", { purgeTarget = i }, Modifier.weight(1f), kind = Btn.Danger, small = true, enabled = !model.busy)
                    if (!canRestore(i) && !canPurge(i)) Text("只读", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                }
            }
        }
        item { Pager(page, data.optInt("total_pages"), { page = it }) }
    }
    purgeTarget?.let { t ->
        val bulk = t.optBoolean("bulk")
        Sheet(if (bulk) "彻底删除 ${selected.size} 项内容" else "彻底删除内容", { purgeTarget = null }, footer = {
            WButton("取消", { purgeTarget = null })
            WButton("确认彻底删除", {
                model.perform({ purgeTarget = null; loader.reload++ }) {
                    if (bulk) {
                        model.api.request("/api/admin/trash/bulk", "POST", json = JSONObject().put("action", "purge").put("confirm", "PURGE").put("targets", JSONArray(chosen().map { target(it) })))
                        selected = emptySet()
                    } else model.api.request(path(t), "DELETE", json = JSONObject().put("confirm", "PURGE"))
                    model.error = "内容已彻底删除"
                }
            }, kind = Btn.Danger, enabled = !model.busy)
        }) {
            Callout("数据库记录、互动关系和无其他引用的附件将被永久清理，操作无法撤销。", Tone.Danger)
        }
    }
}

// ---------------------------------------------------------------- 用户与权限

private val roleOptions =
    listOf(
        Triple("user", "普通用户", "默认只能使用前台。超级管理员可按开关授予后台单项能力。"),
        Triple("reviewer", "审核员", "默认可审核帖子、表白便签和学号注册，并管理公告。超级管理员可再按开关增删其权限。"),
        Triple("admin", "管理员", "默认可管理内容与平台日常事务。超级管理员可再按开关增删其权限，包括角色与个人权限分配。"),
        Triple("super_admin", "超级管理员", "拥有全部权限，始终锁定为全开。可以把任意开关授予其他非超管账号。AI 审核密钥仅超管可改。"),
    )

private val permissionGroupLabels =
    mapOf(
        "dashboard" to "后台入口", "publishing" to "内容发布", "review" to "审核队列", "content" to "帖子管理", "comments" to "评论管理",
        "notice" to "公告管理", "feedback" to "反馈工单", "reports" to "举报管理", "users" to "用户管理", "security" to "角色与安全",
        "settings" to "平台设置", "logs" to "日志与审计",
    )

private fun roleLabel(role: String) = roleOptions.firstOrNull { it.first == role }?.second ?: displayState(role)

@Composable
private fun UserStatus(u: JSONObject) {
    when {
        u.s("status") == "pending" -> Badge("待审核", Tone.Warning, Icons.Rounded.HourglassTop)
        u.s("status") == "disabled" -> Badge("已停用", Tone.Danger, Icons.Rounded.Block)
        u.optBoolean("is_muted") -> Badge("禁言中", Tone.Warning)
        else -> Badge("正常", Tone.Success)
    }
}

@Composable
private fun UsersAdmin(model: WallModel) {
    val c = W
    val canRoles = model.can("users.role.assign")
    val canPermissions = model.can("users.permissions.assign")
    val selfId = model.user?.s("id")
    var query by remember { mutableStateOf("") }
    var applied by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("created_at:desc") }
    var page by remember { mutableIntStateOf(1) }
    var pageSize by remember { mutableIntStateOf(25) }
    var roleTarget by remember { mutableStateOf<JSONObject?>(null) }
    var permTarget by remember { mutableStateOf<JSONObject?>(null) }
    var editTarget by remember { mutableStateOf<JSONObject?>(null) }
    var muteTarget by remember { mutableStateOf<JSONObject?>(null) }
    var resetTarget by remember { mutableStateOf<JSONObject?>(null) }
    var creating by remember { mutableStateOf(false) }
    val loader = rememberLoader(model, JSONObject(), applied, role, state, sort, page, pageSize) {
        val (by, order) = sort.split(":")
        val status = if (state in listOf("active", "disabled", "pending")) state else ""
        model.api.request(
            "/api/admin/users?page=$page&page_size=$pageSize&q=${applied.pathSegment()}&status=$status&muted=${if (state == "muted") "true" else ""}&role=$role&sort_by=$by&sort_order=$order"
        )
    }
    val data = loader.data
    val users = data.objects("users")
    val stats = data.optJSONObject("stats") ?: JSONObject()
    val byRole = stats.optJSONObject("by_role") ?: JSONObject()
    fun run(msg: String, call: suspend () -> Unit, done: () -> Unit = {}) = model.perform({ done(); loader.reload++ }) {
        call()
        model.error = msg
    }
    AdminList {
        item {
            Callout(
                (if (canPermissions) "你可以逐项设置用户权限。" else if (canRoles) "你可以修改账号角色。" else "你只能执行已授予的用户管理操作。") +
                    "个人「拒绝」优先于角色默认和个人「允许」。",
                icon = Icons.Rounded.Lock,
            )
        }
        if (stats.optInt("pending") > 0)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Callout("有 ${stats.optInt("pending")} 个用户名密码注册待审核。通过后对方才能登录；拒绝后该用户名不能再注册。", Tone.Warning, Icons.Rounded.HourglassTop)
                    if (state != "pending") WButton("查看待审注册", { state = "pending"; page = 1 }, kind = Btn.Primary, small = true)
                }
            }
        item {
            StatGrid(
                listOf(
                    Triple("${stats.optInt("total")}", "注册用户", "普通用户 ${byRole.optInt("user")}"),
                    Triple("${stats.optInt("pending")}", "待审核", "用户名密码注册"),
                    Triple("${stats.optInt("active")}", "正常账号", "管理员 ${byRole.optInt("admin")}"),
                    Triple("${stats.optInt("muted")}", "禁言中", "审核员 ${byRole.optInt("reviewer")}"),
                    Triple("${stats.optInt("disabled")}", "已停用", "超级管理员 ${byRole.optInt("super_admin")}"),
                )
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchBar(query, { query = it }, "用户名、昵称、姓名或 ID", { applied = query.trim(); page = 1 })
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Select(listOf("" to "全部角色") + roleOptions.map { it.first to it.second }, role, { role = it; page = 1 })
                    Select(listOf("" to "全部状态", "active" to "正常", "pending" to "待审核", "muted" to "禁言中", "disabled" to "已停用"), state, { state = it; page = 1 })
                    Select(
                        listOf("created_at:desc" to "最新注册", "created_at:asc" to "最早注册", "last_login_at:desc" to "最近登录", "username:asc" to "用户名 A-Z", "role:asc" to "按角色排序", "status:asc" to "按状态排序"),
                        sort,
                        { sort = it; page = 1 },
                    )
                    WButton("清除", {
                        query = ""; applied = ""; role = ""; state = ""; sort = "created_at:desc"; page = 1
                    })
                    IconBtn(Icons.Rounded.Refresh, "刷新", { loader.reload++ }, kind = Btn.Outline)
                    if (canRoles) WButton("创建管理员", { creating = true }, kind = Btn.Primary, icon = Icons.Rounded.PersonAdd)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val first = if (data.optInt("total") == 0) 0 else (page - 1) * pageSize + 1
                    Text(if (loader.loading) "正在加载…" else "显示 $first–${first + users.size - if (first == 0) 0 else 1}，共 ${data.optInt("total")} 位匹配用户", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    Text("每页 ", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    Select(listOf("25" to "25", "50" to "50", "100" to "100"), "$pageSize", { pageSize = it.toInt(); page = 1 })
                }
            }
        }
        if (loader.loading) item { Spinner() }
        if (!loader.loading && users.isEmpty()) item { EmptyCard("没有匹配的注册用户", icon = Icons.Rounded.Group) }
        items(users, key = { it.s("id") }) { u ->
            val isSelf = u.s("id") == selfId
            val protectedTarget = !canRoles && (u.s("role") != "user" || u.strings("capabilities").isNotEmpty())
            val enabled = !protectedTarget && !isSelf
            val canEdit = (model.can("users.profile.update") || (model.can(if (u.s("status") == "disabled") "users.status.enable" else "users.status.disable") && u.s("status") != "pending")) && enabled
            val canMute = model.can("users.mute") && enabled
            val canReset = model.can("users.password.reset") && enabled
            val canApprove = u.s("status") == "pending" && model.can("users.status.enable") && enabled
            val canReject = u.s("status") == "pending" && model.can("users.status.disable") && enabled
            val canDisable = model.can("users.status.disable") && enabled && u.s("role") != "super_admin" && u.s("status") != "pending"
            val canRole = canRoles && !isSelf && u.s("status") != "pending"
            val canPerm = canPermissions && u.s("status") != "pending"
            var menu by remember { mutableStateOf(false) }
            Card(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(u.s("username"), style = MaterialTheme.typography.titleMedium)
                        Text(u.s("nickname").ifBlank { u.s("real_name").ifBlank { "未设置显示名称" } }, style = MaterialTheme.typography.bodySmall, color = c.ink2)
                        Text("用户 ID · ${u.s("id")}", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                    }
                    Box {
                        if (canEdit || canMute || canReset || canApprove || canReject || canDisable || canRole || canPerm)
                            WButton("管理", { menu = true }, small = true, icon = Icons.Rounded.MoreHoriz, enabled = !model.busy)
                        else Text("无可用操作", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                        DropdownMenu(menu, { menu = false }, containerColor = c.surface) {
                            if (canRole) MenuItem(Icons.Rounded.Shield, "设置角色") { menu = false; roleTarget = u }
                            if (canPerm) MenuItem(Icons.Rounded.Tune, "细分权限") { menu = false; permTarget = u }
                            if (canEdit) MenuItem(Icons.Rounded.Edit, "编辑资料") { menu = false; editTarget = u }
                            if (canMute)
                                if (u.optBoolean("is_muted")) MenuItem(Icons.Rounded.CheckCircle, "解除禁言") {
                                    menu = false
                                    run("${u.s("username")} 已解除禁言", { model.api.request("/api/admin/users/${u.s("id")}/unmute", "POST") })
                                } else MenuItem(Icons.Rounded.Block, "设置禁言") { menu = false; muteTarget = u }
                            if (canReset) MenuItem(Icons.Rounded.Key, "重置密码") { menu = false; resetTarget = u }
                            if (canApprove) MenuItem(Icons.Rounded.CheckCircle, "通过注册") {
                                menu = false
                                run("${u.s("username")} 已通过审核", { model.api.request("/api/admin/users/${u.s("id")}/registration/approve", "POST") })
                            }
                            if (canReject) MenuItem(Icons.Rounded.Close, "拒绝注册", danger = true) {
                                menu = false
                                run("${u.s("username")} 的注册已拒绝", { model.api.request("/api/admin/users/${u.s("id")}/registration/reject", "POST") })
                            }
                            if (canDisable) MenuItem(Icons.Rounded.Block, "停用账号", danger = true, enabled = u.s("status") != "disabled") {
                                menu = false
                                run("${u.s("username")} 已停用", { model.api.request("/api/admin/users/${u.s("id")}/disable", "POST") })
                            }
                        }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Badge(roleLabel(u.s("role", "user")), if (u.s("role") in listOf("admin", "super_admin")) Tone.Accent else Tone.Neutral)
                    UserStatus(u)
                    if (u.optBoolean("permission_customized")) Badge("个人权限", Tone.Warning, Icons.Rounded.Tune)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("最后登录 ${formatTime(u.s("last_login_at"))}", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                    Text(if (u.optBoolean("is_muted")) "禁言到 ${formatTime(u.s("muted_until"))}" else "注册于 ${formatTime(u.s("created_at"))}", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                    if (u.s("mute_reason").isNotBlank()) Text("原因：${u.s("mute_reason")}", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                }
            }
        }
        item { Pager(page, data.optInt("total_pages"), { page = it }) }
    }
    roleTarget?.let { u ->
        var next by remember(u) { mutableStateOf(u.s("role", "user")) }
        Sheet("设置 ${u.s("username")} 的角色", { roleTarget = null }, footer = {
            WButton("取消", { roleTarget = null })
            WButton("保存角色", {
                run("${u.s("username")} 已设为${roleLabel(next)}", { model.api.request("/api/admin/users/${u.s("id")}/role", "PUT", json = JSONObject().put("role", next)) }) { roleTarget = null }
            }, kind = Btn.Primary, enabled = next != u.s("role") && !model.busy)
        }) {
            roleOptions.forEach { (value, label, desc) ->
                val on = next == value
                Column(
                    Modifier.fillMaxWidth().clip(RMd).background(c.surface2).border(if (on) 1.5.dp else 1.dp, if (on) c.accent else c.line, RMd).clickable { next = value }.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(label, style = MaterialTheme.typography.titleSmall, color = if (on) c.accentStrong else c.ink)
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = c.ink3)
                }
            }
            if (u.optBoolean("permission_customized") && next != u.s("role")) Callout("修改角色会清空该用户现有的全部个人权限覆盖。", Tone.Warning)
            if (next == "super_admin") Callout("超级管理员可以修改所有用户权限。只授予你完全信任的人。", Tone.Warning)
        }
    }
    permTarget?.let { u -> PermissionsSheet(model, u, isSelf = u.s("id") == selfId) { changed -> permTarget = null; if (changed) loader.reload++ } }
    editTarget?.let { u ->
        var realName by remember(u) { mutableStateOf(u.s("real_name")) }
        var nickname by remember(u) { mutableStateOf(u.s("nickname")) }
        var gender by remember(u) { mutableStateOf(u.s("gender", "0").ifBlank { "0" }) }
        var bio by remember(u) { mutableStateOf(u.s("bio")) }
        var status by remember(u) { mutableStateOf(u.s("status", "active").ifBlank { "active" }) }
        Sheet("编辑 ${u.s("username")} 的资料", { editTarget = null }, footer = {
            WButton("取消", { editTarget = null })
            WButton("保存", {
                run("用户资料已更新", {
                    model.api.request("/api/admin/users/${u.s("id")}", "PUT", fields = mapOf("real_name" to realName, "nickname" to nickname, "gender" to gender, "bio" to bio, "status" to status))
                }) { editTarget = null }
            }, kind = Btn.Primary, enabled = !model.busy)
        }) {
            Field(realName, { realName = it }, label = "真实姓名")
            Field(nickname, { nickname = it }, label = "昵称")
            Text("性别", style = MaterialTheme.typography.labelMedium, color = c.ink2)
            Seg(listOf("0" to "未设置", "1" to "男", "2" to "女"), gender, { gender = it }, Modifier.fillMaxWidth(), fill = true)
            Field(bio, { bio = it }, label = "简介", singleLine = false, minLines = 3)
            if (u.s("status") != "pending") {
                Text("账号状态", style = MaterialTheme.typography.labelMedium, color = c.ink2)
                Seg(listOf("active" to "正常", "disabled" to "停用"), status, { status = it }, Modifier.fillMaxWidth(), fill = true)
            }
        }
    }
    muteTarget?.let { u ->
        var until by remember(u) { mutableStateOf(OffsetDateTime.now(ZoneId.of("Asia/Shanghai")).plusDays(7).withSecond(0).withNano(0).toString()) }
        var reason by remember(u) { mutableStateOf("") }
        Sheet("禁言 ${u.s("username")}", { muteTarget = null }, footer = {
            WButton("取消", { muteTarget = null })
            WButton("确认禁言", {
                run("${u.s("username")} 已禁言", { model.api.request("/api/admin/users/${u.s("id")}/mute", "POST", fields = mapOf("muted_until" to until, "reason" to reason)) }) { muteTarget = null }
            }, kind = Btn.Danger, enabled = until.isNotBlank() && !model.busy)
        }) {
            DateTimeField("禁言截止时间", until) { until = it }
            Field(reason, { reason = it }, label = "原因", singleLine = false, minLines = 3, maxLength = 200)
        }
    }
    resetTarget?.let { u ->
        var pw by remember(u) { mutableStateOf("") }
        var confirm by remember(u) { mutableStateOf("") }
        Sheet("重置 ${u.s("username")} 的密码", { resetTarget = null }, footer = {
            WButton("取消", { resetTarget = null })
            WButton("确认重置", {
                run("${u.s("username")} 的密码已重置", { model.api.request("/api/admin/users/${u.s("id")}/reset_password", "POST", fields = mapOf("password" to pw)) }) { resetTarget = null }
            }, kind = Btn.Danger, enabled = pw.length >= 8 && pw == confirm && !model.busy)
        }) {
            Field(pw, { pw = it }, label = "新密码", placeholder = "至少 8 个字符", secret = true)
            Field(confirm, { confirm = it }, label = "确认新密码", secret = true, hint = if (confirm.isNotEmpty() && confirm != pw) "两次输入不一致" else null)
        }
    }
    if (creating) {
        var username by remember { mutableStateOf("") }
        var nickname by remember { mutableStateOf("") }
        var pw by remember { mutableStateOf("") }
        var confirm by remember { mutableStateOf("") }
        var newRole by remember { mutableStateOf("reviewer") }
        Sheet("创建管理员", { creating = false }, footer = {
            WButton("取消", { creating = false })
            WButton("创建", {
                run("管理账号 $username 已创建", {
                    model.api.request("/api/admin/users", "POST", json = JSONObject().put("username", username).put("password", pw).put("role", newRole).put("nickname", nickname))
                }) { creating = false }
            }, kind = Btn.Primary, enabled = username.isNotBlank() && pw.length >= 8 && pw == confirm && !model.busy)
        }) {
            Field(username, { username = it }, label = "用户名")
            Field(nickname, { nickname = it }, label = "显示昵称")
            Field(pw, { pw = it }, label = "初始密码", placeholder = "至少 8 个字符", secret = true)
            Field(confirm, { confirm = it }, label = "确认密码", secret = true)
            Text("角色", style = MaterialTheme.typography.labelMedium, color = c.ink2)
            Choices(roleOptions.filter { it.first != "user" }.map { it.first to it.second }, newRole, { newRole = it })
        }
    }
}

@Composable
private fun PermissionsSheet(model: WallModel, user: JSONObject, isSelf: Boolean, close: (Boolean) -> Unit) {
    val c = W
    var catalog by remember { mutableStateOf(emptyList<JSONObject>()) }
    var state by remember { mutableStateOf<JSONObject?>(null) }
    var draft by remember { mutableStateOf(emptyMap<String, String>()) }
    var original by remember { mutableStateOf(emptyMap<String, String>()) }
    var reason by remember { mutableStateOf("") }
    var reload by remember { mutableIntStateOf(0) }
    LaunchedEffect(user.s("id"), reload) {
        try {
            catalog = model.api.request("/api/admin/permissions").objects("permissions")
            val r = model.api.request("/api/admin/users/${user.s("id")}/permissions")
            val st = r.optJSONObject("permissions") ?: r
            state = st
            val o = st.optJSONObject("overrides")
            val allow = o?.strings("allow").orEmpty()
            val deny = o?.strings("deny").orEmpty()
            original = catalog.associate { p -> p.s("key") to (if (p.s("key") in allow) "allow" else if (p.s("key") in deny) "deny" else "inherit") }
            draft = original
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        }
    }
    val st = state
    val readOnly = st == null || st.optBoolean("overrides_locked") || isSelf
    val defaults = st?.strings("defaults").orEmpty().toSet()
    Sheet("细分权限 · ${user.s("username")}", { close(false) }, footer = {
        if (st != null && !readOnly && st.optBoolean("customized"))
            WButton("恢复角色默认", {
                model.perform({ close(true) }) {
                    model.api.request(
                        "/api/admin/users/${user.s("id")}/permissions",
                        "DELETE",
                        json = JSONObject().put("permission_version", st.opt("permission_version")).put("reason", reason.trim()).put("confirm", "RESET_PERMISSION_OVERRIDES"),
                    )
                    model.error = "已恢复角色默认权限，旧会话已撤销"
                }
            }, small = true, enabled = reason.isNotBlank() && !model.busy)
        WButton(if (readOnly) "关闭" else "取消", { close(false) }, small = true)
        if (!readOnly)
            WButton("确认并保存", {
                model.perform({ close(true) }) {
                    try {
                        model.api.request(
                            "/api/admin/users/${user.s("id")}/permissions",
                            "PUT",
                            json =
                                JSONObject()
                                    .put("allow", JSONArray(draft.filterValues { it == "allow" }.keys.sorted()))
                                    .put("deny", JSONArray(draft.filterValues { it == "deny" }.keys.sorted()))
                                    .put("permission_version", st!!.opt("permission_version"))
                                    .put("reason", reason.trim())
                                    .put("confirm", "REPLACE_PERMISSION_OVERRIDES"),
                        )
                        model.error = "个人权限已更新，旧会话已撤销"
                    } catch (e: Exception) {
                        reload++
                        throw e
                    }
                }
            }, kind = Btn.Primary, small = true, enabled = draft != original && reason.isNotBlank() && !model.busy)
    }) {
        if (st == null) {
            Spinner()
            return@Sheet
        }
        Callout(
            "${roleLabel(st.s("role", user.s("role")))}默认 + 个人允许/拒绝。解析顺序：个人拒绝 → 个人允许 → 角色默认。拒绝始终优先，权限依赖会自动联动。" +
                (if (st.optBoolean("overrides_locked")) "\n超级管理员始终拥有全部权限，不能设置个人覆盖。" else "") +
                (if (isSelf) "\n为避免自我授权或锁定，不能修改当前账号的个人权限。" else ""),
            icon = Icons.Rounded.Tune,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Badge("角色默认 ${defaults.size}")
            Badge("个人允许 ${draft.count { it.value == "allow" }}", Tone.Success)
            Badge("个人拒绝 ${draft.count { it.value == "deny" }}", Tone.Danger)
            Badge("最终可用 ${catalog.count { p -> val v = draft[p.s("key")] ?: "inherit"; v == "allow" || (v == "inherit" && p.s("key") in defaults) }}", Tone.Warning)
        }
        catalog.groupBy { it.s("group", "other") }.forEach { (group, perms) ->
            Text("${permissionGroupLabels[group] ?: group} · ${perms.size} 项", style = MaterialTheme.typography.titleSmall)
            perms.forEach { p ->
                val k = p.s("key")
                val choice = draft[k] ?: "inherit"
                val effective = choice == "allow" || (choice == "inherit" && k in defaults)
                FlatCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(p.s("label", k), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        Badge(
                            when (p.s("risk")) {
                                "critical" -> "严格控制"
                                "high" -> "高风险"
                                "medium" -> "中风险"
                                else -> "低风险"
                            },
                            when (p.s("risk")) {
                                "critical", "high" -> Tone.Danger
                                "medium" -> Tone.Warning
                                else -> Tone.Neutral
                            },
                        )
                    }
                    if (p.s("description").isNotBlank()) Text(p.s("description"), style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    Text(k, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = c.ink3)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Seg(
                            listOf("inherit" to "继承（${if (k in defaults) "开" else "关"}）", "allow" to "允许", "deny" to "拒绝"),
                            choice,
                            { v -> if (!readOnly) draft = draft + (k to v) },
                        )
                        Box(Modifier.weight(1f))
                        Badge(if (effective) "可用" else "不可用", if (effective) Tone.Success else Tone.Neutral)
                    }
                }
            }
        }
        if (!readOnly) Field(reason, { reason = it }, label = "调整原因 *", placeholder = "例如：临时协助处理举报", maxLength = 200)
    }
}

// ---------------------------------------------------------------- 公告管理

private val priorityOptions =
    listOf(Triple("normal", "普通", "仅在首页展示"), Triple("important", "重要", "首次发布时自动提醒"), Triple("urgent", "紧急", "用于需要立即关注的信息"))

private fun isFuture(value: String) = runCatching { OffsetDateTime.parse(value).toInstant().isAfter(Instant.now()) }
    .recoverCatching { LocalDateTime.parse(value.replace(' ', 'T').take(19)).atZone(ZoneId.of("Asia/Shanghai")).toInstant().isAfter(Instant.now()) }
    .getOrDefault(false)

private fun bucket(n: JSONObject) = if (n.s("status") == "published" && isFuture(n.s("publish_at"))) "scheduled" else n.s("status", "published")

private val bucketLabels = mapOf("all" to "全部", "published" to "已发布", "scheduled" to "定时中", "draft" to "草稿", "archived" to "已归档")

@Composable
private fun NoticeAdmin(model: WallModel) {
    val c = W
    val canCreate = model.can("notice.create")
    val canUpdate = model.can("notice.update")
    val canDelete = model.can("notice.delete")
    var editing by remember { mutableStateOf<JSONObject?>(null) }
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("normal") }
    var scheduled by remember { mutableStateOf(false) }
    var publishAt by remember { mutableStateOf("") }
    var remind by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("all") }
    var priorityFilter by remember { mutableStateOf("all") }
    var archiveTarget by remember { mutableStateOf<JSONObject?>(null) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = rememberCoroutineScope()
    val loader = rememberLoader(model, emptyList<JSONObject>()) {
        val r = model.api.request("/api/admin/notice")
        r.objects("notices").ifEmpty { r.objects("content") }.ifEmpty { r.objects("data") }
    }
    val notices = loader.data
    fun reset() {
        editing = null; title = ""; summary = ""; content = ""; priority = "normal"; scheduled = false; publishAt = ""; remind = false
    }
    fun payload(n: JSONObject, status: String) =
        mapOf(
            "title" to n.s("title"), "summary" to n.s("summary"), "content" to n.s("content"),
            "priority" to n.s("priority", "normal"), "status" to status, "publish_at" to n.s("publish_at", n.s("timestamp")),
        )
    val visible = notices.filter { n ->
        (filter == "all" || bucket(n) == filter) && (priorityFilter == "all" || n.s("priority", "normal") == priorityFilter) &&
            (query.isBlank() || listOf("title", "summary", "content").any { n.s(it).contains(query, true) })
    }
    val canCompose = if (editing != null) canUpdate else canCreate
    LazyColumn(
        Modifier.fillMaxSize().imePadding(),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            StatGrid(
                listOf(
                    Triple("${notices.count { bucket(it) == "published" }}", "正在展示", null),
                    Triple("${notices.count { bucket(it) == "scheduled" }}", "定时发布", null),
                    Triple("${notices.count { bucket(it) == "draft" }}", "草稿", null),
                    Triple("${notices.count { bucket(it) == "archived" }}", "已归档", null),
                )
            )
        }
        if (canCompose)
            item {
                Card(Modifier.fillMaxWidth(), spacing = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("公告工作台", style = MaterialTheme.typography.labelSmall, color = c.accent)
                            Text(if (editing != null) "编辑公告" else "创建公告", style = MaterialTheme.typography.headlineSmall)
                        }
                        if (editing != null) WButton("取消编辑", { reset() }, small = true)
                    }
                    Text(if (editing != null) "修改内容不会默认再次弹窗；需要重新提醒时请手动勾选。" else "先完善信息，再保存草稿、立即发布或安排定时发布。", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    Field(title, { title = it }, label = "标题（必填）", placeholder = "例如：运动会期间校门开放安排", maxLength = 120)
                    Field(summary, { summary = it }, label = "摘要（选填）", placeholder = "用一两句话概括公告要点", singleLine = false, minLines = 2, maxLength = 300)
                    Field(content, { content = it }, label = "正文（必填）", placeholder = "输入公告正文", singleLine = false, minLines = 8, maxLength = 10000)
                    Text("优先级", style = MaterialTheme.typography.labelMedium, color = c.ink2)
                    priorityOptions.forEach { (v, l, d) ->
                        val on = priority == v
                        Row(
                            Modifier.fillMaxWidth().clip(RMd).border(if (on) 1.5.dp else 1.dp, if (on) c.accent else c.line, RMd).clickable { priority = v }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Badge(l, when (v) { "urgent" -> Tone.Danger; "important" -> Tone.Warning; else -> Tone.Neutral })
                            Text(d, style = MaterialTheme.typography.bodySmall, color = c.ink2)
                        }
                    }
                    Text("发布时间", style = MaterialTheme.typography.labelMedium, color = c.ink2)
                    Seg(listOf("now" to "立即发布", "scheduled" to "定时发布"), if (scheduled) "scheduled" else "now", {
                        scheduled = it == "scheduled"
                        if (scheduled && publishAt.isBlank()) publishAt = OffsetDateTime.now(ZoneId.of("Asia/Shanghai")).plusHours(1).withMinute(0).withSecond(0).withNano(0).toString()
                    }, Modifier.fillMaxWidth(), fill = true)
                    if (scheduled) DateTimeField("计划发布时间", publishAt) { publishAt = it }
                    if (editing != null && priority in listOf("important", "urgent"))
                        SwitchRow("将这次更新作为新提醒", remind, { remind = it }, supporting = "勾选后，未读用户会再次看到自动弹窗；默认不勾选。")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        fun submit(status: String) {
                            model.perform({ reset(); loader.reload++ }) {
                                if (status == "published" && scheduled) require(isFuture(publishAt)) { "计划发布时间需要晚于当前时间" }
                                val at =
                                    if (status == "published") (if (scheduled) OffsetDateTime.parse(publishAt).toInstant().toString() else Instant.now().toString())
                                    else if (scheduled && publishAt.isNotBlank()) OffsetDateTime.parse(publishAt).toInstant().toString()
                                    else editing?.s("publish_at").orEmpty()
                                val fields = mapOf("title" to title.trim(), "summary" to summary.trim(), "content" to content.trim(), "priority" to priority, "status" to status, "publish_at" to at, "remind_on_update" to remind.toString())
                                val e = editing
                                if (e != null) model.api.request("/api/admin/notice/${e.s("id")}", "PUT", fields = fields)
                                else model.api.request("/api/admin/notice", "POST", fields = fields)
                                model.error = if (status == "draft") "草稿已保存" else if (scheduled) "公告已安排定时发布" else "公告已发布"
                            }
                        }
                        WButton("保存草稿", { submit("draft") }, Modifier.weight(1f), enabled = title.isNotBlank() && content.isNotBlank() && !model.busy)
                        WButton(if (scheduled) "安排发布" else if (editing != null) "保存并发布" else "立即发布", { submit("published") }, Modifier.weight(1f), kind = Btn.Primary, icon = Icons.Rounded.Send, enabled = title.isNotBlank() && content.isNotBlank() && !model.busy)
                    }
                }
            }
        else item { Callout(if (canUpdate) "从历史记录选择一条公告开始编辑。你拥有编辑权限，但不能新建公告。" else "当前为只读模式：你可以查看和筛选公告历史，但没有创建或编辑公告的权限。", icon = Icons.Rounded.Visibility) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("历史公告", style = MaterialTheme.typography.headlineSmall)
                    Text("共 ${notices.size} 条，归档后不会公开展示但仍可恢复。", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                }
                IconBtn(Icons.Rounded.Refresh, "刷新", { loader.reload++ }, kind = Btn.Outline)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Field(query, { query = it }, placeholder = "搜索公告标题或内容", leading = Icons.Rounded.Search)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Select(bucketLabels.toList(), filter, { filter = it })
                    Select(listOf("all" to "全部优先级", "normal" to "普通", "important" to "重要", "urgent" to "紧急"), priorityFilter, { priorityFilter = it })
                }
            }
        }
        if (loader.loading) item { Spinner() }
        if (!loader.loading && visible.isEmpty()) item { EmptyCard("没有符合条件的公告", icon = Icons.Rounded.Inbox) }
        items(visible, key = { it.s("id") }) { n ->
            val b = bucket(n)
            Card(Modifier.fillMaxWidth()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Badge(bucketLabels[b] ?: b, when (b) { "published" -> Tone.Success; "scheduled" -> Tone.Warning; "archived" -> Tone.Neutral; else -> Tone.Accent })
                    Badge(priorityOptions.firstOrNull { it.first == n.s("priority") }?.second ?: "普通", when (n.s("priority")) { "urgent" -> Tone.Danger; "important" -> Tone.Warning; else -> Tone.Neutral })
                }
                Text(n.s("publish_at", n.s("timestamp")).ifBlank { "未设置发布时间" } + n.s("updated_at").let { if (it.isBlank()) "" else " · 编辑于 $it" }, style = MaterialTheme.typography.labelSmall, color = c.ink3)
                Text(n.s("title").ifBlank { "校园公告" }, style = MaterialTheme.typography.titleMedium)
                if (n.s("summary").isNotBlank()) Text(n.s("summary"), style = MaterialTheme.typography.bodySmall, color = c.ink2)
                Text(n.s("content"), style = MaterialTheme.typography.bodySmall, color = c.ink2, maxLines = 4, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canUpdate)
                        WButton("编辑", {
                            editing = n; title = n.s("title"); summary = n.s("summary"); content = n.s("content"); priority = n.s("priority", "normal")
                            scheduled = isFuture(n.s("publish_at")); publishAt = if (scheduled) n.s("publish_at") else ""; remind = false
                            scope.launch { listState.animateScrollToItem(1) }
                        }, small = true, icon = Icons.Rounded.Edit, enabled = !model.busy)
                    if (n.s("status") == "archived" && canUpdate)
                        WButton("恢复", {
                            model.perform({ loader.reload++ }) {
                                model.api.request("/api/admin/notice/${n.s("id")}", "PUT", fields = payload(n, "published"))
                                model.error = "公告已恢复"
                            }
                        }, kind = Btn.Primary, small = true, enabled = !model.busy)
                    if (n.s("status") != "archived" && canDelete) WButton("归档", { archiveTarget = n }, kind = Btn.Danger, small = true, icon = Icons.Rounded.Archive, enabled = !model.busy)
                }
            }
        }
    }
    archiveTarget?.let { n ->
        Confirm("归档公告", "归档后公告会立即从首页移除，但记录会保留在历史列表中，之后可以恢复。", "确认归档", { archiveTarget = null }, {
            model.perform({ archiveTarget = null; if (editing?.s("id") == n.s("id")) reset(); loader.reload++ }) {
                model.api.request("/api/admin/notice/${n.s("id")}", "DELETE")
                model.error = "公告已归档，可随时恢复"
            }
        })
    }
}

/** 日期 + 时间选择，值为带时区的 ISO 8601 字符串。 */
@Composable
fun DateTimeField(label: String, value: String, onChange: (String) -> Unit) {
    val c = W
    val zone = ZoneId.of("Asia/Shanghai")
    var calendar by remember { mutableStateOf(false) }
    var clock by remember { mutableStateOf(false) }
    val current = runCatching { OffsetDateTime.parse(value).atZoneSameInstant(zone) }.getOrNull()
    val base = current ?: java.time.ZonedDateTime.now(zone)
    val date = rememberDatePickerState(initialSelectedDateMillis = base.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
    val time = rememberTimePickerState(base.hour, base.minute, true)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.ink2)
        Row(
            Modifier.fillMaxWidth().clip(RMd).border(1.dp, c.lineStrong, RMd).clickable { calendar = true }.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(20.dp), tint = c.ink3)
            Text(current?.format(DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm")) ?: "未设置", style = MaterialTheme.typography.bodyMedium, color = if (current == null) c.ink3 else c.ink)
        }
    }
    if (calendar)
        DatePickerDialog(
            onDismissRequest = { calendar = false },
            confirmButton = { WButton("下一步", { calendar = false; clock = true }, kind = Btn.Primary, small = true, enabled = date.selectedDateMillis != null) },
            dismissButton = { WButton("取消", { calendar = false }, small = true) },
            colors = DatePickerDefaults.colors(containerColor = c.surface),
        ) { DatePicker(date, colors = DatePickerDefaults.colors(containerColor = c.surface)) }
    if (clock)
        Sheet("选择时间", { clock = false }, footer = {
            WButton("取消", { clock = false })
            WButton("确定", {
                val day = Instant.ofEpochMilli(date.selectedDateMillis!!).atZone(ZoneOffset.UTC).toLocalDate()
                onChange(day.atTime(time.hour, time.minute).atZone(zone).toOffsetDateTime().toString())
                clock = false
            }, kind = Btn.Primary)
        }) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(time, colors = TimePickerDefaults.colors(containerColor = c.surface2)) }
        }
}

// ---------------------------------------------------------------- 反馈工单

private val feedbackCategories = mapOf("bug" to "网站故障", "feature" to "功能建议", "account" to "账号问题", "content" to "内容与社区", "other" to "其他反馈")
private val feedbackStatuses = mapOf("pending" to "待处理", "in_progress" to "处理中", "resolved" to "已解决", "closed" to "已关闭")

private fun ticketId(id: String) = id.chunked(8).joinToString("-")

@Composable
private fun TicketStatus(status: String, labels: Map<String, String>) =
    Badge(labels[status] ?: status, when (status) { "pending" -> Tone.Warning; "resolved" -> Tone.Success; else -> Tone.Neutral })

@Composable
private fun FeedbackAdmin(model: WallModel) {
    val c = W
    val canUpdate = model.can("feedback.update")
    var q by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var applied by remember { mutableStateOf(Triple("", "", "")) }
    var page by remember { mutableIntStateOf(1) }
    var selected by remember { mutableStateOf<JSONObject?>(null) }
    val loader = rememberLoader(model, JSONObject(), applied, page) {
        model.api.request("/api/admin/feedback?page=$page&page_size=15&q=${applied.first.pathSegment()}&category=${applied.second}&status=${applied.third}")
    }
    val data = loader.data
    val tickets = data.objects("tickets")
    val stats = data.optJSONObject("stats") ?: JSONObject()
    val categories = data.optJSONObject("categories")?.let { o -> o.keys().asSequence().associateWith { o.s(it) } } ?: feedbackCategories
    val statuses = data.optJSONObject("statuses")?.let { o -> o.keys().asSequence().associateWith { o.s(it) } } ?: feedbackStatuses
    AdminList {
        item {
            StatGrid(listOf(Triple("${stats.optInt("pending")}", "待处理", null), Triple("${stats.optInt("in_progress")}", "处理中", null), Triple("${stats.optInt("resolved")}", "已解决", null), Triple("${stats.optInt("closed")}", "已关闭", null)))
        }
        item {
            Column(Modifier.fillMaxWidth().clip(RMd).background(c.surface2).border(1.dp, c.line, RMd).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Field(q, { q = it }, placeholder = "搜索工单编号、主题、邮箱或反馈内容", leading = Icons.Rounded.Search)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Select(listOf("" to "全部分类") + categories.toList(), category, { category = it })
                    Select(listOf("" to "全部状态") + statuses.toList(), status, { status = it })
                    WButton("查询", { applied = Triple(q.trim(), category, status); page = 1 }, kind = Btn.Primary, icon = Icons.Rounded.Search)
                    IconBtn(Icons.AutoMirrored.Rounded.Undo, "重置筛选", { q = ""; category = ""; status = ""; applied = Triple("", "", ""); page = 1 }, kind = Btn.Outline)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("工单队列", style = MaterialTheme.typography.headlineSmall)
                    Text("当前共有 ${stats.optInt("pending") + stats.optInt("in_progress")} 条待跟进工单，处理记录仅供后台团队协作查阅。", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                }
                WButton("刷新", { loader.reload++ }, small = true, icon = Icons.Rounded.Refresh)
            }
        }
        if (loader.loading) item { Spinner() }
        if (!loader.loading && tickets.isEmpty()) item { EmptyCard("没有匹配的反馈工单", icon = Icons.Rounded.Inbox) }
        items(tickets, key = { it.s("id") }) { t ->
            Card(Modifier.fillMaxWidth()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Badge(categories[t.s("category")] ?: "其他反馈")
                    TicketStatus(t.s("status"), statuses)
                    Text(ticketId(t.s("id")), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = c.ink3)
                }
                Text(t.s("title").ifBlank { "未填写反馈主题" }, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(t.s("text"), style = MaterialTheme.typography.bodySmall, color = c.ink2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${t.s("timestamp")}　${t.s("email").ifBlank { "未留邮箱" }}" + t.s("updated_at").let { if (it.isBlank()) "" else "　更新于 $it" }, style = MaterialTheme.typography.labelSmall, color = c.ink3)
                WButton(if (canUpdate) "处理" else "查看", { selected = t }, kind = if (canUpdate) Btn.Primary else Btn.Outline, small = true, icon = if (canUpdate) Icons.Rounded.Edit else Icons.Rounded.Visibility)
            }
        }
        item { Pager(page, data.optInt("total_pages"), { page = it }) }
    }
    selected?.let { t ->
        var st by remember(t) { mutableStateOf(t.s("status", "pending")) }
        var reply by remember(t) { mutableStateOf(t.s("public_reply")) }
        var note by remember(t) { mutableStateOf(t.s("internal_note")) }
        Sheet("${if (canUpdate) "处理" else "查看"}反馈 · ${ticketId(t.s("id"))}", { selected = null }, footer = {
            WButton("关闭", { selected = null })
            if (canUpdate)
                WButton("保存处理结果", {
                    model.perform({ selected = null; loader.reload++ }) {
                        model.api.request("/api/admin/feedback/${t.s("id").pathSegment()}", "PUT", json = JSONObject().put("status", st).put("public_reply", reply).put("internal_note", note))
                        model.error = "工单已更新"
                    }
                }, kind = Btn.Primary, enabled = !model.busy)
        }) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Badge(categories[t.s("category")] ?: "其他反馈"); TicketStatus(t.s("status"), statuses) }
            Text(t.s("title").ifBlank { "未填写反馈主题" }, style = MaterialTheme.typography.titleMedium)
            Text(t.s("text"), style = MaterialTheme.typography.bodyMedium)
            Text(t.s("email").ifBlank { "未留下联系邮箱" }, style = MaterialTheme.typography.labelSmall, color = c.ink3)
            Hairline()
            Text("工单状态", style = MaterialTheme.typography.labelMedium, color = c.ink2)
            Choices(statuses.toList(), st) { if (canUpdate) st = it }
            Field(reply, { reply = it }, label = "处理说明", singleLine = false, minLines = 4, maxLength = 10000, enabled = canUpdate)
            Field(note, { note = it }, label = "内部备注", singleLine = false, minLines = 4, maxLength = 10000, enabled = canUpdate)
        }
    }
}

// ---------------------------------------------------------------- 举报管理

private fun resolutionLabel(r: String) = when (r) {
    "delete_comment" -> Triple("评论已移入回收站", Tone.Warning, Icons.Rounded.Delete)
    "delete_message" -> Triple("留言已移入回收站", Tone.Danger, Icons.Rounded.Delete)
    else -> Triple("保留内容", Tone.Success, Icons.Rounded.CheckCircle)
}

@Composable
private fun ReportTarget(r: JSONObject) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Badge("${if (r.s("target_type") == "comment") "评论" else "留言"}举报", if (r.s("target_type") == "comment") Tone.Warning else Tone.Neutral)
            Badge(r.s("category").ifBlank { "其它违规情况" }.let { displayState(it) })
            if (r.s("comment_id").isNotBlank()) Text("评论 ID：${r.s("comment_id")}", style = MaterialTheme.typography.labelSmall, color = W.ink3)
        }
        Text(r.s("target_excerpt").ifBlank { "被举报内容暂无文字摘要" }, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ReportAdmin(model: WallModel) {
    val c = W
    val canHistory = model.can("report.history.read")
    val canResolve = model.can("report.resolve")
    fun canAction(a: String) = canResolve && (a == "dismiss" || (a == "delete_comment" && model.can("content.comment.delete")) || (a == "delete_message" && model.can("content.message.delete")))
    var view by remember { mutableStateOf("pending") }
    var context by remember { mutableStateOf<Pair<String, JSONObject>?>(null) }
    var confirm by remember { mutableStateOf<Triple<String, JSONObject, String>?>(null) }
    var hq by remember { mutableStateOf("") }
    var hType by remember { mutableStateOf("") }
    var hAction by remember { mutableStateOf("") }
    var hApplied by remember { mutableStateOf(Triple("", "", "")) }
    var hPage by remember { mutableIntStateOf(1) }
    val pending = rememberLoader(model, JSONObject()) { model.api.request("/api/admin/report") }
    val history = rememberLoader(model, JSONObject(), view, hApplied, hPage) {
        if (view != "history") JSONObject()
        else model.api.request("/api/admin/reports/history?page=$hPage&page_size=20&q=${hApplied.first.pathSegment()}&target_type=${hApplied.second}&action=${hApplied.third}")
    }
    val reports = pending.data.optJSONObject("reports") ?: JSONObject()
    val entries = reports.keys().asSequence().map { it to reports.objects(it) }.filter { it.second.isNotEmpty() }.toList()
    val total = entries.sumOf { it.second.size }
    val commentCount = entries.sumOf { e -> e.second.count { it.s("target_type") == "comment" } }
    val processed = pending.data.optInt("processed_total")
    AdminList {
        item { StatGrid(listOf(Triple("$total", "待处理举报", null), Triple("$commentCount", "评论举报", null), Triple("${entries.size}", "涉及留言", null), Triple("$processed", "累计处理记录", null))) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WButton("待处理 $total", { view = "pending" }, kind = if (view == "pending") Btn.Primary else Btn.Outline, small = true, icon = Icons.Rounded.Inbox)
                if (canHistory) WButton("处理记录 $processed", { view = "history" }, kind = if (view == "history") Btn.Primary else Btn.Outline, small = true, icon = Icons.Rounded.Archive)
            }
        }
        if (view == "pending") {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("待处理队列", style = MaterialTheme.typography.headlineSmall)
                        Text(if (canResolve) "查看上下文后，可保留内容，或按已授予的内容权限移入回收站。" else "当前为只读查看，不会显示举报处理按钮。", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    }
                    WButton("刷新", { pending.reload++ }, small = true, icon = Icons.Rounded.Refresh)
                }
            }
            if (pending.loading) item { Spinner() }
            if (!pending.loading && entries.isEmpty()) item { EmptyCard("暂无待处理举报", "新的留言或评论举报会集中显示在这里。", Icons.Rounded.Shield) }
            items(entries, key = { it.first }) { (mid, list) ->
                Card(Modifier.fillMaxWidth(), padding = PaddingValues(0.dp), spacing = 0.dp) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("留言 #$mid", style = MaterialTheme.typography.titleMedium)
                        Badge("${list.size} 条待处理")
                        Box(Modifier.weight(1f))
                        WButton("查看留言", { context = mid to list.first() }, small = true, icon = Icons.Rounded.Visibility)
                    }
                    list.forEach { r ->
                        Hairline()
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ReportTarget(r)
                            Text("举报说明", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                            Text(r.s("text").ifBlank { "无补充说明" }, style = MaterialTheme.typography.bodySmall)
                            Text(r.s("timestamp").ifBlank { "时间未知" } + r.s("email").let { if (it.isBlank()) "" else "　$it" }, style = MaterialTheme.typography.labelSmall, color = c.ink3)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                WButton("上下文", { context = mid to r }, small = true)
                                if (canAction("dismiss")) WButton("保留内容", { confirm = Triple(mid, r, "dismiss") }, small = true, icon = Icons.Rounded.CheckCircle)
                                if (r.s("target_type") == "comment" && canAction("delete_comment")) WButton("移走评论", { confirm = Triple(mid, r, "delete_comment") }, kind = Btn.Danger, small = true)
                                if (canAction("delete_message")) WButton("移走留言", { confirm = Triple(mid, r, "delete_message") }, kind = Btn.Danger, small = true)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Column(Modifier.fillMaxWidth().clip(RMd).background(c.surface2).border(1.dp, c.line, RMd).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field(hq, { hq = it }, placeholder = "搜索留言 ID、摘要、理由、邮箱或管理员", leading = Icons.Rounded.Search)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Select(listOf("" to "全部对象", "message" to "留言举报", "comment" to "评论举报"), hType, { hType = it })
                        Select(listOf("" to "全部处理方式", "dismiss" to "保留内容", "delete_comment" to "评论移入回收站", "delete_message" to "留言移入回收站"), hAction, { hAction = it })
                        WButton("查询", { hApplied = Triple(hq.trim(), hType, hAction); hPage = 1 }, kind = Btn.Primary, icon = Icons.Rounded.Search)
                    }
                }
            }
            item { Text("当前筛选共 ${history.data.optInt("total")} 条，记录处理方式、管理员和时间。", style = MaterialTheme.typography.bodySmall, color = c.ink3) }
            if (history.loading) item { Spinner() }
            val items = history.data.objects("items")
            if (!history.loading && items.isEmpty()) item { EmptyCard("没有匹配的处理记录", "处理待办举报后，审计记录会保存在这里。", Icons.Rounded.Archive) }
            items(items) { r ->
                val (label, tone, icon) = resolutionLabel(r.s("resolution"))
                Card(Modifier.fillMaxWidth()) {
                    ReportTarget(r)
                    Badge(label, tone, icon)
                    Hairline()
                    Text("举报说明", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                    Text(r.s("text").ifBlank { "无补充说明" }, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "留言 #${r.s("message_id")}" + r.s("timestamp").let { if (it.isBlank()) "" else "　举报于 $it" } + "　处理于 ${r.s("processed_at").ifBlank { "时间未知" }}　${r.s("processed_by").ifBlank { "历史记录" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.ink3,
                    )
                    if (r.s("public_reply").isNotBlank()) FlatCard(Modifier.fillMaxWidth()) {
                        Text("处理说明（后台记录）", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                        Text(r.s("public_reply"), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item { Pager(hPage, history.data.optInt("total_pages"), { hPage = it }) }
        }
    }
    context?.let { (mid, report) ->
        var message by remember(mid) { mutableStateOf<JSONObject?>(null) }
        LaunchedEffect(mid) {
            try {
                val r = model.api.request("/api/admin/api/get_message/$mid")
                message = r.optJSONObject("message") ?: r.optJSONObject("data") ?: r
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                model.error = e.displayError()
                context = null
            }
        }
        Sheet("举报上下文 · 留言 #$mid", { context = null }, footer = {
            WButton("关闭", { context = null })
            if (canAction("dismiss")) WButton("保留内容", { context = null; confirm = Triple(mid, report, "dismiss") })
            if (canAction("delete_message")) WButton("移走留言", { context = null; confirm = Triple(mid, report, "delete_message") }, kind = Btn.Danger)
        }) {
            ReportTarget(report)
            val m = message
            if (m == null) Spinner()
            else {
                FlatCard(Modifier.fillMaxWidth()) {
                    Text("留言内容", style = MaterialTheme.typography.titleSmall)
                    Text(m.s("text").ifBlank { "此留言只包含附件" }, style = MaterialTheme.typography.bodyMedium)
                    Text(m.s("timestamp"), style = MaterialTheme.typography.labelSmall, color = c.ink3)
                }
                val files = m.strings("files")
                if (files.isNotEmpty()) MediaGrid(files, model.api, limit = files.size, compact = true)
                val comments = m.objects("comments")
                if (comments.isNotEmpty()) {
                    Text("评论（${comments.size}）", style = MaterialTheme.typography.titleSmall)
                    comments.forEach { cm ->
                        val reported = cm.s("id") == report.s("comment_id")
                        Column(
                            Modifier.fillMaxWidth().clip(RMd).background(if (reported) c.warningSoft else c.surface2).border(1.dp, if (reported) c.warningLine else c.line, RMd).padding(12.dp)
                        ) {
                            if (reported) Badge("被举报的评论", Tone.Warning)
                            Text(cm.s("text"), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
    confirm?.let { (mid, report, action) ->
        var reply by remember(report) { mutableStateOf("") }
        val (label) = resolutionLabel(action)
        Sheet(
            when (action) {
                "delete_comment" -> "移走被举报的评论"
                "delete_message" -> "移走被举报的留言"
                else -> "保留内容并结束举报"
            },
            { confirm = null },
            footer = {
                WButton("取消", { confirm = null })
                WButton("确认处理", {
                    model.perform({ confirm = null; pending.reload++ }) {
                        val r = model.api.request("/api/admin/reports/$mid/${report.s("id")}/resolve", "POST", json = JSONObject().put("action", action).put("public_reply", reply.trim()))
                        model.error = "已处理 ${r.optInt("resolved", 1)} 条举报 · $label"
                    }
                }, kind = if (action == "dismiss") Btn.Primary else Btn.Danger, enabled = !model.busy)
            },
        ) {
            ReportTarget(report)
            if (action != "dismiss") Callout("内容会移入回收站，可在「内容回收站」恢复。", Tone.Warning)
            Field(reply, { reply = it }, label = "处理说明（选填，仅后台可见）", singleLine = false, minLines = 3, maxLength = 1000)
        }
    }
}

// ---------------------------------------------------------------- 消息提醒

@Composable
private fun NotificationSettingsAdmin(model: WallModel) {
    val c = W
    val canUpdate = model.can("settings.notifications.update")
    val canTest = model.can("settings.notifications.test")
    var clearTarget by remember { mutableStateOf<JSONObject?>(null) }
    val loader = rememberLoader(model, JSONObject()) { model.api.request("/api/admin/settings/notifications") }
    val settings = loader.data.optJSONObject("settings") ?: loader.data
    val providers = settings.objects("providers")
    AdminList {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("审核通知", style = MaterialTheme.typography.headlineSmall)
                    Text("有新的待审内容时，通过以下渠道提醒审核人员。", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                }
                IconBtn(Icons.Rounded.Refresh, "刷新", { loader.reload++ }, kind = Btn.Outline)
            }
        }
        item {
            StatGrid(
                listOf(
                    Triple("${providers.count { it.optBoolean("enabled") }}", "正在启用", null),
                    Triple("${providers.count { it.optBoolean("configured") }}", "已配置", null),
                    Triple("${providers.size}", "可用渠道", null),
                )
            )
        }
        if (loader.loading) item { Spinner() }
        items(providers, key = { it.s("id") }) { p ->
            val email = p.s("id") == "email"
            var enabled by remember(p.toString()) { mutableStateOf(p.optBoolean("enabled")) }
            var target by remember(p.toString()) { mutableStateOf("") }
            var secret by remember(p.toString()) { mutableStateOf("") }
            Card(Modifier.fillMaxWidth(), spacing = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.s("label"), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Badge(if (p.optBoolean("configured")) "已配置" else "尚未配置", if (p.optBoolean("configured")) Tone.Success else Tone.Neutral)
                }
                SwitchRow("启用审核提醒", enabled, { enabled = it }, supporting = if (email) "向收件邮箱发送待审提醒" else "向机器人群发送待审提醒", enabled = canUpdate)
                Field(
                    target,
                    { target = it },
                    label = if (email) "收件邮箱" else "机器人 Webhook",
                    placeholder = if (p.optBoolean("configured")) "已安全保存；留空表示不替换" else if (email) "多个地址用逗号分隔" else "粘贴完整 HTTPS Webhook",
                    secret = !email,
                    enabled = canUpdate,
                )
                if (p.optBoolean("supports_signing_secret"))
                    Field(secret, { secret = it }, label = "签名校验密钥（可选）", placeholder = "留空表示不替换", secret = true, enabled = canUpdate)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canUpdate)
                        WButton("保存", {
                            model.perform({ loader.reload++ }) {
                                require(!(enabled && !p.optBoolean("configured") && target.isBlank())) { if (email) "启用前请填写收件邮箱" else "启用前请粘贴机器人平台提供的完整 Webhook" }
                                model.api.request("/api/admin/settings/notifications/${p.s("id").pathSegment()}", "PUT", json = JSONObject().put("enabled", enabled).put("webhook", target.trim()).put("secret", secret.trim()))
                                model.error = "${p.s("label")}设置已立即生效"
                            }
                        }, kind = Btn.Primary, small = true, enabled = !model.busy)
                    if (canTest && p.optBoolean("configured"))
                        WButton("发送测试", {
                            model.perform {
                                model.api.request("/api/admin/settings/notifications/${p.s("id").pathSegment()}/test", "POST")
                                model.error = "测试消息已发送"
                            }
                        }, small = true, icon = Icons.Rounded.Send, enabled = !model.busy)
                    if (canUpdate && p.optBoolean("configured")) WButton("清除", { clearTarget = p }, kind = Btn.Danger, small = true, enabled = !model.busy)
                }
            }
        }
        item { Callout("测试按钮只发送服务器生成的固定内容，不会把帖子正文、用户身份、联系方式、Webhook 或密钥写入页面、日志与审计记录。", icon = Icons.Rounded.Info) }
    }
    clearTarget?.let { p ->
        Confirm("清除${p.s("label")}", "该渠道会立即停用，已保存的收件信息将被清除。之后如需恢复，必须重新填写。", "确认清除", { clearTarget = null }, {
            model.perform({ clearTarget = null; loader.reload++ }) { model.api.request("/api/admin/settings/notifications/${p.s("id").pathSegment()}", "DELETE") }
        })
    }
}

// ---------------------------------------------------------------- 平台与验证

@Composable
private fun SettingsAdmin(model: WallModel) {
    val c = W
    val canCommunity = model.can("settings.community.update")
    val canCaptcha = model.can("settings.captcha.update")
    val canReadAi = model.can("settings.ai.read")
    val canAi = model.can("settings.ai.update")
    val community = rememberLoader(model, JSONObject()) { model.api.request("/api/admin/settings/community").optJSONObject("settings") ?: JSONObject() }
    val captcha = rememberLoader(model, JSONObject()) { model.api.request("/api/admin/settings/captcha").optJSONObject("settings") ?: JSONObject() }
    val ai = rememberLoader(model, JSONObject()) { if (canReadAi) runCatching { model.api.request("/api/admin/settings/ai").optJSONObject("settings") }.getOrNull() ?: JSONObject() else JSONObject() }
    AdminList {
        item {
            val s = community.data
            var form by remember(s.toString()) { mutableStateOf(JSONObject(s.toString())) }
            var words by remember(s.toString()) { mutableStateOf(s.optJSONArray("sensitive_words")?.let { a -> (0 until a.length()).joinToString("\n") { a.optString(it) } } ?: s.s("sensitive_words")) }
            fun set(k: String, v: Any) { form = JSONObject(form.toString()).put(k, v) }
            FormCard("社区运营控制", "开关立即同步到前台，适用于网页和手机端。") {
                if (community.loading) Spinner()
                SwitchRow("允许发布留言", form.optBoolean("posting_enabled", true), { set("posting_enabled", it) }, "关闭后所有访客都不能发布留言", canCommunity)
                SwitchRow("允许发表评论", form.optBoolean("commenting_enabled", true), { set("commenting_enabled", it) }, "关闭后所有留言暂停新增评论和回复", canCommunity)
                SwitchRow("允许游客发帖", form.optBoolean("guest_posting_enabled"), { set("guest_posting_enabled", it) }, "关闭后未登录访客不能发布动态或表白；默认关闭，需登录后发帖", canCommunity)
                SwitchRow("允许游客评论", form.optBoolean("guest_commenting_enabled"), { set("guest_commenting_enabled", it) }, "关闭后未登录访客不能评论或回复；默认关闭", canCommunity)
                SwitchRow("强制全部人工审核", form.optBoolean("require_post_approval"), { set("require_post_approval", it) }, "打开后，即使 AI/词库未命中辱骂也会送人工。关闭时：命中辱骂必进人工复审，未命中直接公开。免审角色和失物招领仍立即公开。", canCommunity)
                Field(form.s("pause_reason"), { set("pause_reason", it) }, label = "暂停说明", placeholder = "例如：系统维护中，预计今晚 22:00 恢复", enabled = canCommunity)
                Field(form.s("community_rules"), { set("community_rules", it) }, label = "社区规则（每行一条）", singleLine = false, minLines = 5, enabled = canCommunity)
                val count = words.split(Regex("[\\n,，]+")).map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet().size
                Field(words, { words = it }, label = "敏感词（$count 个）", placeholder = "每行一个词，也支持使用逗号分隔", singleLine = false, minLines = 4, enabled = canCommunity)
                WButton("保存社区设置", {
                    model.perform({ community.reload++; model.refreshSession() }) {
                        val r = model.api.request(
                            "/api/admin/settings/community",
                            "PUT",
                            json = JSONObject()
                                .put("posting_enabled", form.optBoolean("posting_enabled", true))
                                .put("commenting_enabled", form.optBoolean("commenting_enabled", true))
                                .put("guest_posting_enabled", form.optBoolean("guest_posting_enabled"))
                                .put("guest_commenting_enabled", form.optBoolean("guest_commenting_enabled"))
                                .put("require_post_approval", form.optBoolean("require_post_approval"))
                                .put("pause_reason", form.s("pause_reason"))
                                .put("community_rules", form.s("community_rules"))
                                .put("sensitive_words", words),
                        )
                        val released = r.optInt("released_pending")
                        model.error = if (released > 0) "设置已生效，$released 条待审留言已公开" else "社区运营设置已同步到前台"
                    }
                }, kind = Btn.Primary, enabled = canCommunity && !model.busy)
            }
        }
        item {
            val s = captcha.data
            var enabled by remember(s.toString()) { mutableStateOf(s.optBoolean("enabled")) }
            var provider by remember(s.toString()) { mutableStateOf(s.s("provider").takeIf { it in listOf("turnstile", "recaptcha") } ?: "none") }
            var siteKey by remember(s.toString()) { mutableStateOf(s.s("site_key")) }
            var secret by remember(s.toString()) { mutableStateOf("") }
            var clearSecret by remember(s.toString()) { mutableStateOf(false) }
            var login by remember(s.toString()) { mutableStateOf(s.optBoolean("protect_login", true)) }
            var register by remember(s.toString()) { mutableStateOf(s.optBoolean("protect_register", true)) }
            var adminLogin by remember(s.toString()) { mutableStateOf(s.optBoolean("protect_admin_login", true)) }
            var hosts by remember(s.toString()) { mutableStateOf(s.optJSONArray("allowed_hostnames")?.let { a -> (0 until a.length()).joinToString("\n") { a.optString(it) } } ?: s.s("allowed_hostnames")) }
            val ready = !enabled || (provider != "none" && siteKey.isNotBlank() && !clearSecret && (s.optBoolean("has_secret") || secret.isNotBlank()) && (login || register || adminLogin) && (provider != "turnstile" || hosts.isNotBlank()))
            FormCard("Cloudflare Turnstile 人机验证", "保护登录、注册和后台入口。") {
                SwitchRow("启用人机验证", enabled, { enabled = it }, "总开关；关闭后保留密钥和范围设置，重新开启无需重复填写", canCaptcha)
                Text("服务商", style = MaterialTheme.typography.labelMedium, color = c.ink2)
                Seg(listOf("none" to "未选择", "turnstile" to "Turnstile", "recaptcha" to "reCAPTCHA"), provider, { if (canCaptcha) provider = it }, Modifier.fillMaxWidth(), fill = true)
                Field(siteKey, { siteKey = it }, label = "站点公钥", placeholder = "Site key", enabled = canCaptcha)
                Field(secret, { secret = it }, label = "验证密钥", placeholder = if (s.optBoolean("has_secret")) "已保存；留空表示不替换" else "Secret key", secret = true, enabled = canCaptcha)
                if (s.optBoolean("has_secret")) SwitchRow("清除已保存的密钥", clearSecret, { clearSecret = it }, enabled = canCaptcha)
                Text("保护范围", style = MaterialTheme.typography.labelMedium, color = c.ink2)
                SwitchRow("师生登录", login, { login = it }, "用户名密码登录", canCaptcha)
                SwitchRow("账号注册", register, { register = it }, "新用户提交注册", canCaptcha)
                SwitchRow("后台登录", adminLogin, { adminLogin = it }, "审核员与管理员入口", canCaptcha)
                Field(hosts, { hosts = it }, label = "允许的验证域名（每行一个）", placeholder = "wall.zongtech.xyz", singleLine = false, minLines = 2, enabled = canCaptcha)
                if (!ready) Callout("启用前需要选择服务商、填写公钥与密钥、至少保护一个入口，Turnstile 还需填写允许的域名。", Tone.Warning)
                WButton("保存人机验证", {
                    model.perform({ captcha.reload++ }) {
                        model.api.request(
                            "/api/admin/settings/captcha",
                            "PUT",
                            json = JSONObject().put("enabled", enabled).put("provider", provider).put("site_key", siteKey.trim()).put("secret_key", secret.trim()).put("clear_secret", clearSecret)
                                .put("protect_login", login).put("protect_register", register).put("protect_admin_login", adminLogin).put("allowed_hostnames", hosts),
                        )
                        model.error = "人机验证设置已生效"
                    }
                }, kind = Btn.Primary, enabled = canCaptcha && ready && !model.busy)
            }
        }
        if (canReadAi)
            item {
                val s = ai.data
                var enabled by remember(s.toString()) { mutableStateOf(s.optBoolean("enabled")) }
                var baseUrl by remember(s.toString()) { mutableStateOf(s.s("base_url")) }
                var key by remember(s.toString()) { mutableStateOf("") }
                var clearKey by remember(s.toString()) { mutableStateOf(false) }
                var aiModel by remember(s.toString()) { mutableStateOf(s.s("model", "gpt-4o-mini").ifBlank { "gpt-4o-mini" }) }
                FormCard("AI 帖子审核", "词库共 ${s.optInt("lexicon_size")} 个词条。") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge(if (s.optBoolean("configured")) "已配置" else "未配置", if (s.optBoolean("configured")) Tone.Success else Tone.Neutral)
                        if (s.optBoolean("has_api_key")) Badge("已保存 API Key")
                    }
                    SwitchRow("启用模型复检", enabled, { enabled = it }, "词库未命中时，再把正文发给 OpenAI 兼容接口。模型判定辱骂或接口失败时仍送人工。关闭则只走词库。", canAi)
                    Field(baseUrl, { baseUrl = it }, label = "服务地址", placeholder = "https://api.openai.com/v1", enabled = canAi)
                    Field(key, { key = it }, label = "API Key", placeholder = if (s.optBoolean("has_api_key")) "已保存；留空表示不替换" else "sk-…", secret = true, enabled = canAi)
                    if (s.optBoolean("has_api_key")) SwitchRow("清除已保存的 API Key", clearKey, { clearKey = it }, enabled = canAi)
                    Field(aiModel, { aiModel = it }, label = "模型", placeholder = "gpt-4o-mini", enabled = canAi)
                    WButton("保存 AI 审核", {
                        model.perform({ ai.reload++ }) {
                            model.api.request(
                                "/api/admin/settings/ai",
                                "PUT",
                                json = JSONObject().put("enabled", enabled).put("base_url", baseUrl.trim()).put("api_key", key.trim()).put("clear_api_key", clearKey).put("model", aiModel.trim().ifBlank { "gpt-4o-mini" }),
                            )
                            model.error = "AI 审核配置已保存，API Key 不会回显"
                        }
                    }, kind = Btn.Primary, enabled = canAi && !model.busy)
                }
            }
    }
}

// ---------------------------------------------------------------- 审计与日志

private val auditTargets =
    listOf("" to "全部对象", "admin" to "后台", "message" to "留言", "comment" to "评论", "user" to "用户", "app" to "应用", "notice" to "公告", "report" to "举报", "manager" to "管理员", "setting" to "设置", "legacy_log" to "历史日志")

@Composable
private fun AuditAdmin(model: WallModel) {
    val c = W
    var query by remember { mutableStateOf("") }
    var applied by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var page by remember { mutableIntStateOf(1) }
    val loader = rememberLoader(model, JSONObject(), applied, target, page) {
        model.api.request("/api/admin/audit?q=${applied.pathSegment()}&target_type=$target&page=$page&page_size=25")
    }
    val rows = loader.data.objects("items")
    AdminList {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Toolbar(query, { query = it }, "搜索操作者、摘要或动作", { applied = query.trim(); page = 1 }, { loader.reload++ })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Select(auditTargets, target, { target = it; page = 1 })
                    Box(Modifier.weight(1f))
                    Text("共 ${loader.data.optInt("total")} 条", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                }
            }
        }
        if (loader.loading) item { Spinner() }
        if (!loader.loading && rows.isEmpty()) item { EmptyCard("暂无审计记录", icon = Icons.Rounded.Description) }
        items(rows, key = { it.s("id") }) { a ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.width(78.dp)) {
                    val t = formatTime(a.s("created_at")).split(" ")
                    Text(t.getOrElse(0) { "" }, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Text(t.getOrElse(1) { "" }, style = MaterialTheme.typography.labelSmall, color = c.ink3)
                }
                Box(Modifier.padding(top = 4.dp).size(10.dp).clip(CircleShape).background(c.accent))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge(a.s("actor"), icon = Icons.Rounded.CheckCircle)
                        Badge(auditTargets.firstOrNull { it.first == a.s("target_type") }?.second ?: a.s("target_type").ifBlank { "后台" })
                        if (a.s("target_id").isNotBlank()) Badge("#${a.s("target_id")}")
                    }
                    Text(a.s("summary"), style = MaterialTheme.typography.titleSmall)
                    Text(a.s("action"), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = c.ink3)
                }
            }
            Hairline(Modifier.padding(top = 10.dp))
        }
        item { Pager(page, loader.data.optInt("total_pages"), { page = it }) }
    }
}

@Composable
private fun LogAdmin(model: WallModel, error: Boolean) {
    val c = W
    var query by remember(error) { mutableStateOf("") }
    var applied by remember(error) { mutableStateOf("") }
    val loader = rememberLoader(model, emptyList<String>(), error, applied) {
        model.api.request("${if (error) "/api/admin/log" else "/api/admin/admin_log"}?search=${applied.pathSegment()}").strings("log_content")
    }
    AdminList {
        item { Toolbar(query, { query = it }, "搜索日志内容", { applied = query.trim() }, { loader.reload++ }) }
        item { Text("共 ${loader.data.size} 行", style = MaterialTheme.typography.bodySmall, color = c.ink3) }
        if (loader.loading) item { Spinner() }
        if (!loader.loading && loader.data.isEmpty()) item { EmptyCard("暂无日志", icon = Icons.Rounded.Description) }
        if (loader.data.isNotEmpty())
            item {
                Column(Modifier.fillMaxWidth().clip(RMd).background(c.surface2).border(1.dp, c.line, RMd).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    loader.data.forEachIndexed { i, line ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${i + 1}", Modifier.width(32.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = c.ink3, textAlign = TextAlign.End)
                            Text(line, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, lineHeight = 18.sp), color = if (error) c.danger else c.ink2)
                        }
                    }
                }
            }
    }
}
