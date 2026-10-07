package xyz.zongtech.campuswall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DoneAll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.displayError
import xyz.zongtech.campuswall.displayState
import xyz.zongtech.campuswall.glass.Glass
import xyz.zongtech.campuswall.glass.GlassButton
import xyz.zongtech.campuswall.glass.GlassButtonStyle
import xyz.zongtech.campuswall.glass.GlassChoiceRow
import xyz.zongtech.campuswall.glass.GlassConfirmDialog
import xyz.zongtech.campuswall.glass.GlassIconButton
import xyz.zongtech.campuswall.glass.GlassLoading
import xyz.zongtech.campuswall.glass.GlassPanel
import xyz.zongtech.campuswall.glass.GlassProgressBar
import xyz.zongtech.campuswall.glass.GlassSegmented
import xyz.zongtech.campuswall.glass.GlassSwitchRow
import xyz.zongtech.campuswall.glass.GlassTextField
import xyz.zongtech.campuswall.glass.SectionTitle
import xyz.zongtech.campuswall.objects
import xyz.zongtech.campuswall.pathSegment
import xyz.zongtech.campuswall.s

@Composable
fun TopicsScreen(model: WallModel, go: (String) -> Unit) {
    var topics by remember { mutableStateOf(emptyList<JSONObject>()) }
    var q by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var total by remember { mutableIntStateOf(0) }
    var sort by rememberSaveable { mutableStateOf("popular") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(q, page, sort, model.revision, refresh) {
        loading = true
        try {
            val response =
                model.api.request("/api/topics?q=${q.pathSegment()}&s=$sort&start=${(page - 1) * 20}&end=${page * 20}")
            topics = response.objects("data")
            total = response.optInt("total", topics.size)
            error = null
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.displayError()
        } finally {
            loading = false
        }
    }
    Column(Modifier.fillMaxSize()) {
        GlassTopBar("话题", subtitle = "发现大家正在聊什么")
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = LocalBottomSpace.current),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GlassTextField(
                    q,
                    {
                        q = it
                        page = 1
                    },
                    Modifier.fillMaxWidth(),
                    placeholder = "搜索话题",
                    singleLine = true,
                    leadingIcon = Icons.Rounded.Search,
                )
            }
            item {
                GlassSegmented(
                    listOf("popular" to "热门", "newest" to "最新", "name" to "名称"),
                    sort,
                    {
                        sort = it
                        page = 1
                    },
                )
            }
            if (loading) item { GlassProgressBar() }
            error?.let { item { EmptyState(it) { refresh++ } } }
            if (!loading && error == null && topics.isEmpty()) item { EmptyState("没有找到相关话题") }
            items(topics) { t ->
                val name = t.s("name", t.s("tag"))
                GlassPanel(
                    Modifier.fillMaxWidth(),
                    onClick = { go(tagRoute(name)) },
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).clip(Capsule()).background(Glass.colors.accent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("#", style = MaterialTheme.typography.titleMedium, color = Glass.colors.accent)
                        }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${t.optInt("count", t.optInt("message_count"))} 条动态",
                                style = MaterialTheme.typography.bodySmall,
                                color = Glass.colors.secondary,
                            )
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Glass.colors.secondary)
                    }
                }
            }
            item { Pager(page, total, { page = it }, enabled = !loading) }
        }
    }
}

@Composable
fun ProfileScreen(model: WallModel, id: String, go: (String) -> Unit, back: () -> Unit) {
    var user by remember { mutableStateOf(JSONObject()) }
    LaunchedEffect(id) {
        try {
            user = model.api.request("/api/user/$id/profile").optJSONObject("user") ?: JSONObject()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        }
    }
    FeedScreen(
        model,
        user.s("nickname", "同学的主页"),
        go,
        path = "/api/user/$id/messages",
        back = back,
        header = {
            GlassPanel(Modifier.fillMaxWidth(), tint = Glass.colors.accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(avatarUrl(id), 64.dp)
                    Column(Modifier.weight(1f).padding(start = 16.dp)) {
                        Text(user.s("nickname", "同学"), style = MaterialTheme.typography.titleLarge)
                        Text(
                            user.s("bio").ifBlank { "这位同学还没有填写简介" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Glass.colors.secondary,
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun LostFoundScreen(model: WallModel, go: (String) -> Unit, back: () -> Unit) {
    if (model.user == null) {
        Column(Modifier.fillMaxSize()) {
            GlassTopBar("失物招领", back)
            EmptyState(
                "启事含有联系方式，登录后可查看。",
                Modifier.padding(16.dp),
                actionText = "去登录",
            ) {
                go("login")
            }
        }
        return
    }
    FeedScreen(
        model,
        "失物招领",
        go,
        path = "/api/user/lost-found",
        back = back,
        subtitle = "丢了东西或捡到东西，都可以发在这里",
        extraActions = {
            GlassIconButton(Icons.Rounded.Add, "发布启事", { go("lost/new") }, style = GlassButtonStyle.Prominent)
        },
    )
}

@Composable
fun LostFoundComposeScreen(model: WallModel, back: () -> Unit) {
    var kind by rememberSaveable { mutableStateOf("lost") }
    var item by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var resolved by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().imePadding()) {
        GlassTopBar("发布失物启事", back)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GlassSegmented(listOf("lost" to "寻物", "found" to "招领"), kind, { kind = it })
            GlassPanel(Modifier.fillMaxWidth()) {
                GlassTextField(item, { item = it }, Modifier.fillMaxWidth(), label = "物品", singleLine = true)
                GlassTextField(location, { location = it }, Modifier.fillMaxWidth(), label = "地点", singleLine = true)
                GlassTextField(time, { time = it }, Modifier.fillMaxWidth(), label = "时间", placeholder = "例如：周三午休", singleLine = true)
                GlassTextField(details, { details = it }, Modifier.fillMaxWidth(), label = "特征与说明", minLines = 3)
                GlassTextField(contact, { contact = it }, Modifier.fillMaxWidth(), label = "联系方式", singleLine = true)
                GlassSwitchRow("已找回", resolved, { resolved = it })
            }
        }
        GlassButton(
            "发布",
            {
                model.perform(back) {
                    model.api.request(
                        "/api/user/lost-found",
                        "POST",
                        json =
                            JSONObject()
                                .put("kind", kind)
                                .put("item", item)
                                .put("location", location)
                                .put("time", time)
                                .put("details", details)
                                .put("contact", contact)
                                .put("resolved", resolved),
                    )
                }
            },
            Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            enabled = item.isNotBlank() && location.isNotBlank() && !model.busy,
            style = GlassButtonStyle.Prominent,
            icon = Icons.AutoMirrored.Rounded.Send,
            height = 54.dp,
        )
    }
}

@Composable
fun NotificationsScreen(model: WallModel, go: (String) -> Unit) {
    var rows by remember { mutableStateOf(emptyList<JSONObject>()) }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var total by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var clearAll by remember { mutableStateOf(false) }
    LaunchedEffect(page, model.revision, model.user) {
        if (model.user != null) {
            loading = true
            try {
                val r = model.api.request("/api/user/me/notifications?page=$page&page_size=20")
                rows = r.objects("notifications")
                total = r.optInt("total")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                model.error = e.displayError()
            } finally {
                loading = false
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        GlassTopBar("消息", subtitle = if (model.unread > 0) "${model.unread} 条未读" else null) {
            if (model.user != null) {
                GlassIconButton(
                    Icons.Rounded.DoneAll,
                    "全部已读",
                    { model.perform { model.api.request("/api/user/me/notifications/read-all", "POST") } },
                )
                GlassIconButton(Icons.Rounded.DeleteSweep, "清空消息", { clearAll = true }, enabled = rows.isNotEmpty())
            }
        }
        if (model.user == null) {
            EmptyState("登录后查看评论、回复和审核通知", Modifier.padding(16.dp), actionText = "去登录") { go("login") }
            return@Column
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = LocalBottomSpace.current),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (loading && rows.isEmpty())
                item { Box(Modifier.fillMaxWidth(), Alignment.Center) { GlassLoading() } }
            if (!loading && rows.isEmpty()) item { EmptyState("暂时没有新消息") }
            items(rows, key = { it.s("id") }) { n ->
                val unread = !n.optBoolean("is_read")
                GlassPanel(
                    Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    tint = if (unread) Glass.colors.accent else androidx.compose.ui.graphics.Color.Unspecified,
                    onClick = {
                        model.perform({ if (n.optLong("message_id") > 0) go("message/${n.s("message_id")}") }) {
                            model.api.request("/api/user/me/notifications/${n.s("id")}/read", "POST")
                        }
                    },
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (unread)
                            Box(Modifier.padding(end = 8.dp).size(8.dp).clip(Capsule()).background(Glass.colors.danger))
                        Text(
                            n.s("title", "校园墙新消息"),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        GlassIconButton(
                            Icons.Rounded.Close,
                            "删除",
                            {
                                model.perform {
                                    model.api.request("/api/user/me/notifications/${n.s("id")}", "DELETE")
                                }
                            },
                            size = 32.dp,
                        )
                    }
                    Text(n.s("content", n.s("text")), style = MaterialTheme.typography.bodyMedium, color = Glass.colors.secondary)
                    n.s("created_at", n.s("timestamp")).takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = Glass.colors.secondary)
                    }
                }
            }
            item { Pager(page, total, { page = it }, enabled = !loading) }
        }
    }
    if (clearAll)
        GlassConfirmDialog(
            title = "清空所有消息？",
            text = "清空后无法恢复通知记录，动态和评论不受影响。",
            confirmText = "清空",
            danger = true,
            onDismissRequest = { clearAll = false },
            onConfirm = {
                model.perform {
                    model.api.request("/api/user/me/notifications", "DELETE")
                    clearAll = false
                    page = 1
                }
            },
        )
}

@Composable
fun MyCommentsScreen(model: WallModel, go: (String) -> Unit, back: () -> Unit) {
    var rows by remember { mutableStateOf(emptyList<JSONObject>()) }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var total by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var deleting by remember { mutableStateOf<JSONObject?>(null) }
    LaunchedEffect(page, model.revision) {
        loading = true
        try {
            val r = model.api.request("/api/user/me/comments?page=$page&page_size=20")
            rows = r.objects("comments")
            total = r.optInt("total")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        } finally {
            loading = false
        }
    }
    Column(Modifier.fillMaxSize()) {
        GlassTopBar("我的评论", back)
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = LocalBottomSpace.current),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (loading && rows.isEmpty()) item { Box(Modifier.fillMaxWidth(), Alignment.Center) { GlassLoading() } }
            if (!loading && rows.isEmpty()) item { EmptyState("还没有发表过评论") }
            items(rows, key = { "${it.s("message_id")}/${it.s("id")}" }) { c ->
                GlassPanel(
                    Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    onClick = { go("message/${c.s("message_id")}") },
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(c.s("text"), style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            displayState(c.s("moderation_status")),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            color = Glass.colors.secondary,
                        )
                        GlassButton("删除", { deleting = c }, height = 34.dp)
                    }
                }
            }
            item { Pager(page, total, { page = it }, enabled = !loading) }
        }
    }
    deleting?.let { c ->
        GlassConfirmDialog(
            title = "删除这条评论？",
            text = c.s("text").take(60),
            confirmText = "删除",
            danger = true,
            onDismissRequest = { deleting = null },
            onConfirm = {
                model.perform {
                    model.api.request(
                        "/api/user/me/comments/${c.s("message_id")}/${c.s("id").pathSegment()}",
                        "DELETE",
                    )
                    deleting = null
                }
            },
        )
    }
}

@Composable
fun HelpScreen(model: WallModel, back: () -> Unit) {
    var notices by remember { mutableStateOf(emptyList<JSONObject>()) }
    var title by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    val category = "other"
    LaunchedEffect(Unit) {
        try {
            val r = model.api.request("/api/notice")
            notices = r.objects("notices").ifEmpty { r.objects("content") }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        }
    }
    Column(Modifier.fillMaxSize().imePadding()) {
        GlassTopBar("公告与帮助", back)
        Column(
            Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = LocalBottomSpace.current),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            notices.forEach {
                GlassPanel(Modifier.fillMaxWidth(), tint = Glass.colors.accent) {
                    Text(it.s("title"), style = MaterialTheme.typography.titleMedium)
                    Text(it.s("content", it.s("text")), style = MaterialTheme.typography.bodyMedium)
                }
            }
            GlassPanel(Modifier.fillMaxWidth()) {
                SectionTitle("社区规则")
                Text(
                    model.community.s("community_rules").ifBlank { "友善交流，尊重他人，不传播不实信息。" },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            GlassPanel(Modifier.fillMaxWidth()) {
                SectionTitle("意见反馈")
                GlassTextField(title, { title = it }, Modifier.fillMaxWidth(), label = "标题", singleLine = true)
                GlassTextField(text, { text = it }, Modifier.fillMaxWidth(), label = "说明", minLines = 4)
                GlassTextField(
                    email,
                    { email = it },
                    Modifier.fillMaxWidth(),
                    label = "联系邮箱",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                GlassButton(
                    "提交反馈",
                    {
                        model.perform {
                            val r =
                                model.api.request(
                                    "/api/help/form",
                                    "POST",
                                    fields = mapOf("title" to title, "text" to text, "email" to email, "category" to category),
                                )
                            text = ""
                            model.error = "反馈已提交，编号 ${r.s("ticket_id")}"
                        }
                    },
                    Modifier.fillMaxWidth(),
                    enabled = text.isNotBlank() && !model.busy,
                    style = GlassButtonStyle.Prominent,
                )
            }
        }
    }
}

@Composable
fun ReportScreen(model: WallModel, id: String, back: () -> Unit) {
    var category by rememberSaveable { mutableStateOf("other") }
    var text by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().imePadding()) {
        GlassTopBar("举报内容", back, "我们会尽快核实处理")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GlassPanel(Modifier.fillMaxWidth()) {
                SectionTitle("举报类型")
                GlassChoiceRow(
                    listOf(
                        "spam" to "垃圾信息",
                        "abuse" to "恶意行为",
                        "porn" to "低俗或违法信息",
                        "rumor" to "虚假信息",
                        "other" to "其他",
                    ),
                    category,
                    { category = it },
                )
            }
            GlassPanel(Modifier.fillMaxWidth()) {
                GlassTextField(text, { text = it }, Modifier.fillMaxWidth(), label = "举报理由", minLines = 3)
                GlassTextField(
                    email,
                    { email = it },
                    Modifier.fillMaxWidth(),
                    label = "联系邮箱（可选）",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
            }
        }
        GlassButton(
            "提交举报",
            {
                model.perform(back) {
                    require(id.matches(Regex("[0-9]+(?:/comment/[^/]+)?")))
                    model.api.request(
                        "/api/help/report/$id",
                        "POST",
                        fields = mapOf("category" to category, "text" to text, "email" to email),
                    )
                }
            },
            Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            enabled = text.isNotBlank() && !model.busy,
            style = GlassButtonStyle.Danger,
            height = 54.dp,
        )
    }
}
