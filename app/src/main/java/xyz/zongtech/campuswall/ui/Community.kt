package xyz.zongtech.campuswall.ui

import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import org.json.JSONArray
import org.json.JSONObject
import xyz.zongtech.campuswall.R
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.displayError
import xyz.zongtech.campuswall.objects
import xyz.zongtech.campuswall.pathSegment
import xyz.zongtech.campuswall.s

/** 未登录时的首页（网页 Home 的手机端首屏）。 */
@Composable
fun HomeScreen(model: WallModel, go: (String) -> Unit) {
    val c = W
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PageGutter, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(
            Modifier.clip(com.kyant.shapes.Capsule()).border(1.dp, c.lineStrong, com.kyant.shapes.Capsule()).padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(painterResource(R.drawable.school_badge), null, Modifier.size(24.dp).clip(CircleShape).background(Color.White))
            Text("龙华区观澜中学 · 始建于 1914", style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        Text("观澜校园墙", style = MaterialTheme.typography.displaySmall.copy(fontSize = 44.sp, lineHeight = 52.sp))
        Text("记录日常，分享心声，也是寻物、提问与互助的地方。", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 30.sp), color = c.ink2)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WButton("逛逛动态", { go("wall") }, Modifier.weight(1f), kind = Btn.Primary, large = true, icon = Icons.AutoMirrored.Rounded.Chat)
            WButton(if (model.user == null) "登录 / 注册" else "个人中心", { go(if (model.user == null) "login" else "me") }, Modifier.weight(1f), large = true)
        }
        Gap(8.dp)
        listOf(
            Triple(Icons.Rounded.Favorite, "表白墙", "把没说出口的话，写成一张便签。") to "confessions",
            Triple(Icons.Rounded.Inbox, "失物招领", "丢了东西？捡到东西？让它回到主人身边。") to "lost",
            Triple(Icons.Rounded.Sell, "话题广场", "按标签找到感兴趣的讨论。") to "topics",
        ).forEachIndexed { i, (info, route) ->
            val (icon, title, text) = info
            val (fg, bg) = c.tones[i % c.tones.size]
            Card(Modifier.fillMaxWidth(), onClick = { go(route) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    TileIcon(icon, fg, bg)
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.headlineSmall)
                        Text(text, style = MaterialTheme.typography.bodySmall, color = c.ink2)
                    }
                }
            }
        }
    }
}

private const val CONFESSION_TAG = "表白"

/** 网页表白墙：深色粒子爱心卡片 + 公开便签。 */
@Composable
fun ConfessionScreen(model: WallModel, go: (String) -> Unit) {
    val c = W
    var notes by remember { mutableStateOf(emptyList<JSONObject>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var heartLive by rememberSaveable { mutableStateOf(true) }
    var composing by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<JSONObject?>(null) }
    val handled = rememberSaveable { mutableIntStateOf(model.dockTick) }
    LaunchedEffect(model.dockTick) {
        if (model.dockTick != handled.intValue) {
            handled.intValue = model.dockTick
            composing = true
        }
    }
    LaunchedEffect(reload, model.revision) {
        loading = true
        error = null
        try {
            notes =
                model.api
                    .request("/api/get_messages?start=0&end=72&s=newest&tag=${CONFESSION_TAG.pathSegment()}")
                    .objects("data")
                    .filter {
                        it.s("moderation_status") == "visible" && it.s("review_status") == "approved" &&
                            it.optJSONObject("lost_found") == null && it.s("text").isNotBlank()
                    }
                    .sortedByDescending { it.s("timestamp") }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.displayError().ifBlank { "表白便签加载失败，请稍后重试" }
        } finally {
            loading = false
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 20.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                Modifier.fillMaxWidth().clip(RXl).background(Brush.verticalGradient(listOf(Color(0xFF221D1A), Color(0xFF181514)))).border(1.dp, Color(0xFF3A322D), RXl).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("表白墙", style = MaterialTheme.typography.displaySmall, color = Color(0xFFFAF3EC))
                Text("把没说出口的话，写成一张便签。", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCDBFB4))
                ParticleHeart(live = heartLive)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Favorite, null, Modifier.size(18.dp), tint = Color(0xFFE59B7E))
                    Text("${notes.size} 张便签已经公开", Modifier.weight(1f), color = Color(0xFFE9DED6), style = MaterialTheme.typography.bodySmall)
                    DarkButton(if (heartLive) "暂停动画" else "播放动画") { heartLive = !heartLive }
                    DarkButton("刷新", Icons.Rounded.Refresh) { reload++ }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("公开便签", style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp))
                Badge("${notes.size} 张")
            }
        }
        if (loading) item { Spinner() }
        error?.let { e -> item { EmptyCard("暂时无法加载便签", e, actionText = "重试", action = { reload++ }) } }
        if (!loading && error == null && notes.isEmpty())
            item { EmptyCard("还没有公开的便签", "写下第一张便签，审核通过后会出现在这里。", icon = Icons.Rounded.Favorite) }
        items(notes.chunked(2)) { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                pair.forEach { note ->
                    val index = notes.indexOf(note)
                    NoteCard(note, index, Modifier.weight(1f)) { selected = note }
                }
                if (pair.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
    if (composing) ConfessionCompose(model) { composing = false }
    selected?.let { note ->
        Sheet("便签 #${note.s("id")}", { selected = null }, footer = {
            WButton("查看留言", {
                selected = null
                go("message/${note.s("id")}")
            })
            WButton("关闭", { selected = null }, kind = Btn.Primary)
        }) {
            Text(note.s("text"), style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Serif, fontSize = 19.sp, lineHeight = 32.sp))
            Text(note.s("timestamp"), style = MaterialTheme.typography.bodySmall, color = c.ink3)
        }
    }
}

private val tilts = listOf(-1.6f, 1.2f, -0.8f, 1.8f, -1.2f, 0.8f)

@Composable
private fun NoteCard(note: JSONObject, index: Int, modifier: Modifier, onClick: () -> Unit) {
    val c = W
    Box(modifier.padding(top = 10.dp)) {
        Column(
            Modifier.fillMaxWidth()
                .heightIn(min = 150.dp)
                .rotate(tilts[index % tilts.size])
                .clip(RoundedCornerShape(6.dp))
                .background(c.notes[index % c.notes.size])
                .clickable(onClick = onClick)
                .padding(start = 14.dp, end = 14.dp, top = 22.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                note.s("text"),
                Modifier.weight(1f, fill = false),
                color = c.noteInk,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Serif, lineHeight = 24.sp),
                maxLines = 7,
                overflow = TextOverflow.Ellipsis,
            )
            Text(shortTime(note.s("timestamp")), color = c.noteInk.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        }
        Box(
            Modifier.align(Alignment.TopCenter).size(64.dp, 18.dp).rotate(-2f).background(Color(0x66D9A68A), RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun DarkButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    Row(
        Modifier.clip(RSm).background(Color(0x14FFFFFF)).border(1.dp, Color(0x33FFFFFF), RSm).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon?.let { Icon(it, null, Modifier.size(16.dp), tint = Color(0xFFF2E9E2)) }
        Text(text, color = Color(0xFFF2E9E2), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ConfessionCompose(model: WallModel, close: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var anonymous by rememberSaveable { mutableStateOf(true) }
    val user = model.user
    val canPost = model.community.optBoolean("posting_enabled", true) && (user != null || model.community.optBoolean("guest_posting_enabled"))
    Sheet(
        "写一张便签",
        close,
        footer = {
            WButton("取消", close)
            WButton(
                if (model.busy) "正在提交…" else "贴上便签",
                {
                    model.perform(close) {
                        require(text.isNotBlank()) { "请先写下想说的话" }
                        val r =
                            model.api.request(
                                "/api/wall/submit",
                                "POST",
                                fields = mapOf("text" to text.trim(), "tags" to CONFESSION_TAG, "anonymous" to (user == null || anonymous).toString()),
                            )
                        model.error = if (r.s("moderation_status") == "pending") "便签已提交，审核通过后公开" else "便签已贴上表白墙"
                    }
                },
                kind = Btn.Primary,
                enabled = canPost && text.isNotBlank() && !model.busy,
            )
        },
    ) {
        if (!canPost)
            Callout(
                if (!model.community.optBoolean("posting_enabled", true)) model.community.s("pause_reason").ifBlank { "管理员暂时关闭了发帖功能" } else "登录后才能发布",
                Tone.Warning,
            )
        Field(text, { text = it }, placeholder = "写给那个人的话…", singleLine = false, minLines = 6, maxLength = 280)
        if (user != null) SwitchRow("匿名发布", anonymous, { anonymous = it }, supporting = "关闭后便签会展示你的昵称")
        Text("便签需经过审核才会公开，请友善表达。", style = MaterialTheme.typography.bodySmall, color = W.ink3)
    }
}

/** 原生绘制的粒子爱心（网页 HeartParticles 的手机版）。 */
@Composable
fun ParticleHeart(live: Boolean = true) {
    val context = LocalContext.current
    val motion = remember { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f }
    val points = remember {
        val random = Random(41)
        List(1400) {
            val t = random.nextFloat() * 2 * PI
            val r = sqrt(random.nextFloat().toDouble()).pow(0.6)
            val x = 16 * sin(t).pow(3) * r
            val y = (13 * cos(t) - 5 * cos(2 * t) - 2 * cos(3 * t) - cos(4 * t)) * r
            val z = (random.nextFloat() - .5) * 10 * sqrt(1 - r * r + 0.05)
            floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat(), random.nextFloat())
        }
    }
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(motion, live) {
        if (motion && live) {
            val start = withFrameNanos { it } - (time * 1e9f).toLong()
            while (isActive) withFrameNanos { time = (it - start) / 1e9f }
        }
    }
    Canvas(Modifier.fillMaxWidth().height(260.dp)) {
        val angle = sin(time * .35f) * .5f
        val pulse = 1 + sin(time * 2.2f) * .03f
        val scale = size.height / 36f * pulse
        val ca = cos(angle)
        val sa = sin(angle)
        for (p in points) {
            val rx = p[0] * ca + p[2] * sa
            val depth = -p[0] * sa + p[2] * ca
            val persp = 60f / (60f - depth)
            val twinkle = 0.55f + 0.45f * sin(time * 3f + p[3] * 20f)
            drawCircle(
                Color(0xFFE39A6B).copy(alpha = (((depth + 10) / 22f) * twinkle).coerceIn(0.15f, 0.95f)),
                radius = (0.8f + persp) * 1.1f,
                center = Offset(size.width / 2 + rx * scale * persp, size.height / 2 - p[1] * scale * persp),
            )
        }
    }
}

private val lostFilters =
    listOf(
        Triple("all", "全部", "失物招领"),
        Triple("lost", "寻物启事", "寻物启事"),
        Triple("found", "招领启事", "招领启事"),
        Triple("resolved", "已找回", "已找回"),
    )

@Composable
fun LostFoundScreen(model: WallModel, go: (String) -> Unit) {
    var filter by rememberSaveable { mutableStateOf("all") }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var rows by remember { mutableStateOf(emptyList<JSONObject>()) }
    var totalPages by remember { mutableIntStateOf(1) }
    var loading by remember { mutableStateOf(true) }
    var composing by remember { mutableStateOf(false) }
    val handled = rememberSaveable { mutableIntStateOf(model.dockTick) }
    LaunchedEffect(model.dockTick) {
        if (model.dockTick != handled.intValue) {
            handled.intValue = model.dockTick
            composing = true
        }
    }
    LaunchedEffect(filter, page, model.revision, model.user) {
        if (model.user == null) return@LaunchedEffect
        loading = true
        try {
            val f = lostFilters.first { it.first == filter }
            val r = model.api.request("/api/user/lost-found?filter=${f.first}&tag=${f.third.pathSegment()}&page=$page&page_size=24")
            rows = r.objects("messages").ifEmpty { r.objects("data") }
            totalPages = r.optInt("total_pages", 1).coerceAtLeast(1)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        } finally {
            loading = false
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 20.dp, bottom = LocalBottomSpace.current),
    ) {
        item { PageHead("失物招领", "丢了东西？捡到东西？在这里让它回到主人身边。") }
        if (model.user == null) {
            item {
                Gap(10.dp)
                EmptyCard("登录后查看失物招领", "启事里可能含联系方式，未登录访客不能浏览列表。", Icons.Rounded.Lock, "去登录", { go("login") })
            }
            return@LazyColumn
        }
        item {
            Gap(10.dp)
            Seg(lostFilters.map { it.first to it.second }, filter, {
                filter = it
                page = 1
            })
        }
        if (loading) item { Spinner() }
        if (!loading && rows.isEmpty())
            item {
                Gap(14.dp)
                EmptyCard(
                    "暂时没有${lostFilters.first { it.first == filter }.second.let { if (it == "全部") "启事" else it }}",
                    "如果你有相关信息，点击底部「发布寻物 / 招领启事」即可发布第一条。",
                    Icons.Rounded.Inbox,
                )
            }
        items(rows, key = { it.s("id") }) { PostItem(it, model, go) }
        item { Pager(page, totalPages, { page = it }) }
        item {
            Gap(16.dp)
            Card(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.VerifiedUser, null, Modifier.size(18.dp), tint = W.accent)
                    Text("处理指南", style = MaterialTheme.typography.titleSmall)
                }
                listOf(
                    "写清时间地点" to "描述能帮助同学判断是否相关。",
                    "领取前先核验" to "请对方说出未公开的物品特征。",
                    "找回后更新状态" to "可发布带 #已找回 的简短更新，提醒大家停止扩散。",
                ).forEachIndexed { i, (t, d) -> Step(i + 1, t, d) }
                WButton("查看社区公约", { go("rules") }, small = true, icon = Icons.Rounded.Shield)
            }
        }
    }
    if (composing) LostFoundCompose(model) { composing = false }
}

@Composable
fun Step(n: Int, title: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(W.accentSoft), contentAlignment = Alignment.Center) {
            Text("$n", color = W.accentStrong, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(text, style = MaterialTheme.typography.bodySmall, color = W.ink2)
        }
    }
}

@Composable
private fun LostFoundCompose(model: WallModel, close: () -> Unit) {
    var kind by rememberSaveable { mutableStateOf("lost") }
    var item by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var resolved by rememberSaveable { mutableStateOf(false) }
    Sheet(
        "发布启事",
        close,
        footer = {
            WButton("取消", close)
            WButton(
                if (model.busy) "正在发布…" else "发布启事",
                {
                    model.perform(close) {
                        val subtype = if (kind == "found") "招领启事" else "寻物启事"
                        val status = if (resolved) "已找回" else if (kind == "found") "待认领" else "寻找中"
                        val lines =
                            listOfNotNull(
                                "【$subtype】${item.trim()}",
                                "物品：${item.trim()}",
                                "地点：${location.trim()}",
                                time.trim().ifBlank { null }?.let { "时间：$it" },
                                details.trim().ifBlank { null }?.let { "特征与说明：$it" },
                                "联系：${contact.trim().ifBlank { "请在评论区留言" }}",
                                "状态：$status",
                            )
                        val r =
                            model.api.request(
                                "/api/user/lost-found",
                                "POST",
                                json =
                                    JSONObject()
                                        .put("kind", kind)
                                        .put("item", item.trim())
                                        .put("location", location.trim())
                                        .put("time", time.trim())
                                        .put("details", details.trim())
                                        .put("contact", contact.trim())
                                        .put("resolved", resolved)
                                        .put("text", lines.joinToString("\n"))
                                        .put("tags", JSONArray(listOf("失物招领", subtype, status))),
                            )
                        model.error = if (r.s("moderation_status") == "pending") "启事已提交审核，请稍后回来查看" else "启事已发布到失物招领专区"
                    }
                },
                kind = Btn.Primary,
                enabled = item.isNotBlank() && location.isNotBlank() && !model.busy,
            )
        },
    ) {
        Seg(listOf("lost" to "我丢了东西", "found" to "我捡到东西"), kind, { kind = it }, Modifier.fillMaxWidth(), fill = true)
        Field(item, { item = it }, label = "物品名称 *", maxLength = 60)
        Field(location, { location = it }, label = "相关地点 *", maxLength = 80)
        Field(time, { time = it }, label = "大致时间", maxLength = 60)
        Field(contact, { contact = it }, label = "公开联系方式", maxLength = 80, hint = "留空则请对方在评论区留言")
        Field(details, { details = it }, label = "特征与说明", singleLine = false, minLines = 3, maxLength = 500)
        SwitchRow("已找回", resolved, { resolved = it })
    }
}

private fun toneIndex(tag: String) = abs(tag.hashCode()) % 6

/** 话题广场（网页 /p）。 */
@Composable
fun TopicsScreen(model: WallModel, go: (String) -> Unit, back: () -> Unit) {
    val c = W
    var query by rememberSaveable { mutableStateOf("") }
    var applied by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf("popular") }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var topics by remember { mutableStateOf(emptyList<JSONObject>()) }
    var total by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    LaunchedEffect(applied, sort, page, reload) {
        loading = true
        error = null
        try {
            val r = model.api.request("/api/topics?q=${applied.pathSegment()}&s=$sort&start=${(page - 1) * 24}&end=${page * 24}")
            topics = r.objects("data")
            total = r.optInt("total", topics.size)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.displayError()
        } finally {
            loading = false
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { PageHead("话题广场", "共 $total 个公开话题，按标签找到感兴趣的讨论。", back = "返回校园动态" to { go("wall") }) }
        item { SearchBar(query, { query = it }, "搜索话题名称", { applied = query.trim(); page = 1 }) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Select(listOf("popular" to "动态最多", "newest" to "最近更新", "name" to "名称排序"), sort, { sort = it; page = 1 })
                IconBtn(Icons.Rounded.Refresh, "刷新", { reload++ }, size = 40.dp)
            }
        }
        if (loading) item { Spinner() }
        error?.let { e -> item { EmptyCard("暂时无法加载话题", e, actionText = "重试", action = { reload++ }) } }
        if (!loading && error == null && topics.isEmpty())
            item { EmptyCard(if (applied.isNotBlank()) "没有匹配的话题" else "暂时还没有公开话题", icon = Icons.Rounded.Sell) }
        items(topics) { t ->
            val tag = t.s("tag", t.s("name"))
            val (fg, bg) = c.tones[toneIndex(tag)]
            Column(
                Modifier.fillMaxWidth().clip(RXl).background(bg).clickable { go(tagRoute(tag)) }.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text("#$tag", color = fg, style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${t.optInt("count", t.optInt("message_count"))}", color = c.ink, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Text(" 条动态", Modifier.weight(1f), color = c.ink2, style = MaterialTheme.typography.bodySmall)
                    Text(t.s("latest_at").let { if (it.length >= 16) it.substring(5, 16) else it.ifBlank { "暂无" } }, color = c.ink2, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Pager(page, (total + 23) / 24, { page = it }) }
    }
}

/** 单个话题下的动态（网页 /p/:tag）。 */
@Composable
fun TopicScreen(model: WallModel, tag: String, go: (String) -> Unit, back: () -> Unit) {
    val feed =
        rememberFeed(model, tag) { start, end ->
            model.api.request("/api/get_messages?s=newest&tag=${tag.pathSegment()}&start=$start&end=$end").objects("data")
        }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
    ) {
        item {
            PageHead("#$tag", "该话题下的公开动态。", back = "返回话题广场" to back) {
                WButton("发布", { go(if (model.user != null || model.community.optBoolean("guest_posting_enabled")) "compose/${tag.pathSegment()}" else "login") }, kind = Btn.Primary, small = true)
            }
        }
        feedItems(feed, model, go, "发布动态时带上 #$tag，审核通过后即可出现在这里。")
    }
}

@Composable
fun HelpScreen(model: WallModel, go: (String) -> Unit) {
    val c = W
    var notices by remember { mutableStateOf(emptyList<JSONObject>()) }
    LaunchedEffect(Unit) {
        try {
            val r = model.api.request("/api/notice")
            notices = r.objects("notices").ifEmpty { r.objects("content") }
        } catch (_: Exception) {}
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = PageGutter, end = PageGutter, top = 20.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHead("帮助与反馈", "遇到问题、有想法，或者想举报违规内容，都可以从这里开始。")
        listOf(
            listOf("help/form", "提交反馈与建议", "向管理员提交功能建议、网站 Bug、账号解封或其它求助支持。", "前往填写表单") to Pair(Icons.Rounded.SupportAgent, 1),
            listOf("wall", "举报违规内容", "发现人身攻击、违规广告或不良信息？可在帖子的「更多」菜单里一键举报。", "前往校园动态") to Pair(Icons.Rounded.GppMaybe, 3),
            listOf("rules", "社区公约", "查看当前交流准则、互动开放状态以及校园社区内容规范。", "查看社区规则") to Pair(Icons.Rounded.Shield, 2),
        ).forEach { (info, look) ->
            val (fg, bg) = c.tones[look.second]
            Card(Modifier.fillMaxWidth(), padding = PaddingValues(24.dp), onClick = { go(info[0]) }) {
                TileIcon(look.first, fg, bg, 48.dp)
                Gap(4.dp)
                Text(info[1], style = MaterialTheme.typography.headlineSmall)
                Text(info[2], style = MaterialTheme.typography.bodySmall, color = c.ink2)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(info[3], color = c.accentStrong, style = MaterialTheme.typography.labelLarge)
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(16.dp), tint = c.accentStrong)
                }
            }
        }
        if (notices.isNotEmpty()) {
            SectionHead("公告")
            notices.forEach { n ->
                Card(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(n.s("title"), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        when (n.s("priority")) {
                            "urgent" -> Badge("紧急", Tone.Danger)
                            "important" -> Badge("重要", Tone.Warning)
                        }
                    }
                    if (n.s("summary").isNotBlank()) Text(n.s("summary"), style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    Text(n.s("content", n.s("text")), style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                }
            }
        }
        Callout("反馈问题时，请附上具体的操作步骤或截图，方便管理员排查。", icon = Icons.Rounded.Info)
    }
}

@Composable
fun HelpFormScreen(model: WallModel, back: () -> Unit) {
    var category by rememberSaveable { mutableStateOf("feature") }
    var title by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf(model.user?.s("email").orEmpty()) }
    var text by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHead("提交反馈", "你的每一条建议，管理员都会认真阅读。", back = "返回帮助中心" to back)
        Card(Modifier.fillMaxWidth(), spacing = 16.dp) {
            Text("反馈分类", style = MaterialTheme.typography.labelMedium, color = W.ink2)
            Choices(
                listOf("bug" to "网站故障", "feature" to "功能建议", "account" to "账号问题", "content" to "内容与社区", "other" to "其他反馈"),
                category,
                { category = it },
            )
            Field(title, { title = it }, label = "反馈主题", placeholder = "例如：建议增加某某功能 / 页面加载异常", maxLength = 200)
            Field(email, { email = it }, label = "联系邮箱（选填）", placeholder = "需要进一步沟通时，管理员可通过邮箱联系你", maxLength = 320)
            Field(text, { text = it }, label = "反馈详细说明 *", placeholder = "请尽可能详细地描述你遇到的情况或改进建议…", singleLine = false, minLines = 6, maxLength = 10000)
            WButton(
                if (model.busy) "正在提交…" else "提交反馈",
                {
                    model.perform(back) {
                        val r = model.api.request("/api/help/form", "POST", fields = mapOf("category" to category, "title" to title, "email" to email, "text" to text))
                        model.error = "反馈已提交" + r.s("ticket_id").let { if (it.isBlank()) "" else "，编号 $it" }
                    }
                },
                kind = Btn.Primary,
                large = true,
                enabled = text.isNotBlank() && !model.busy,
            )
        }
        Card(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Info, null, Modifier.size(18.dp), tint = W.accent)
                Text("填写建议", style = MaterialTheme.typography.titleSmall)
            }
            Text("描述清楚问题出现的页面、操作步骤和时间；账号问题请写明学号，便于管理员核实。", style = MaterialTheme.typography.bodySmall, color = W.ink2)
        }
    }
}

@Composable
fun RulesScreen(model: WallModel, back: () -> Unit) {
    val c = W
    val rules = model.community.s("community_rules").lines().map { it.trim() }.filter { it.isNotEmpty() }
    val paused = !model.community.optBoolean("posting_enabled", true) || !model.community.optBoolean("commenting_enabled", true)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PageHead("社区公约", "规则由平台管理员维护，适用于留言、评论和投票内容。", back = "返回" to back)
        Badge("当前有效", Tone.Success, Icons.Rounded.CheckCircle)
        if (paused) Callout(model.community.s("pause_reason").ifBlank { "部分互动功能目前由管理员暂时关闭，请稍后再试。" }, Tone.Warning, Icons.Rounded.Info)
        if (rules.isEmpty()) Text("管理员暂未发布额外社区规则。", Modifier.fillMaxWidth().padding(32.dp), color = c.ink3)
        rules.forEachIndexed { i, rule ->
            Card(Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("${i + 1}", style = MaterialTheme.typography.headlineSmall, color = c.accent)
                    Text(rule, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Gap(8.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
            WButton("进入校园动态", back, kind = Btn.Primary, icon = Icons.Rounded.Forum)
        }
    }
}

@Composable
fun ReportScreen(model: WallModel, rawId: String, back: () -> Unit) {
    val c = W
    val messageId = rawId.substringBefore("/comment/")
    val commentId = rawId.substringAfter("/comment/", "")
    val targetText = if (commentId.isNotBlank()) "评论" else "留言"
    var category by rememberSaveable { mutableStateOf("abuse") }
    var email by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }
    var excerpt by remember { mutableStateOf<String?>(null) }
    var missing by remember { mutableStateOf(false) }
    LaunchedEffect(rawId) {
        try {
            val m = model.api.request("/api/get_message_details/$messageId", "POST").optJSONObject("message")
            excerpt =
                if (commentId.isNotBlank()) m?.objects("comments")?.firstOrNull { it.s("id") == commentId }?.s("text")
                else m?.s("text")
            missing = m == null || (commentId.isNotBlank() && excerpt == null)
        } catch (_: Exception) {
            missing = true
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHead("举报违规$targetText", "共同守护友善的校园交流社区。我们会严格保密举报人信息并及时核实处理。", back = "返回留言详情" to back)
        if (missing) Callout("被举报${targetText}已删除或暂时不可访问，无法继续提交举报。", Tone.Warning)
        excerpt?.let {
            Column(
                Modifier.fillMaxWidth().clip(RMd).background(c.surface2).border(1.dp, c.line, RMd).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("被举报$targetText #$messageId${if (commentId.isNotBlank()) " 的评论" else ""}", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                Text(it.ifBlank { "该内容仅包含附件" }, style = MaterialTheme.typography.bodyMedium, maxLines = 6, overflow = TextOverflow.Ellipsis)
            }
        }
        Card(Modifier.fillMaxWidth(), spacing = 16.dp) {
            Text("违规分类", style = MaterialTheme.typography.labelMedium, color = c.ink2)
            Choices(
                listOf(
                    "abuse" to "辱骂攻击 / 恶意人肉 / 骚扰",
                    "spam" to "广告推销 / 刷屏刷榜",
                    "porn" to "色情低俗 / 违法违禁信息",
                    "rumor" to "虚假造谣 / 不实传闻",
                    "other" to "其它违规情况",
                ),
                category,
                { category = it },
            )
            Field(email, { email = it }, label = "联系邮箱（选填）", placeholder = "如需管理员联系，可填写常用邮箱")
            Field(text, { text = it }, label = "举报详细说明 *", placeholder = "请详细描述具体的违规事实或理由…", singleLine = false, minLines = 5, maxLength = 1000)
            WButton(
                if (model.busy) "正在提交…" else "提交举报",
                {
                    model.perform(back) {
                        require(messageId.matches(Regex("[0-9]+")))
                        val path = if (commentId.isNotBlank()) "/api/help/report/$messageId/comment/${commentId.pathSegment()}" else "/api/help/report/$messageId"
                        model.api.request(path, "POST", fields = mapOf("category" to category, "text" to text, "email" to email))
                        model.error = "举报已提交，感谢你帮助维护社区"
                    }
                },
                kind = Btn.Danger,
                large = true,
                icon = Icons.Rounded.Shield,
                enabled = text.isNotBlank() && !missing && !model.busy,
            )
        }
    }
}
