package xyz.zongtech.campuswall.ui

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.view.LayoutInflater
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbDownOffAlt
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.ThumbUpOffAlt
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MultipartBody
import okhttp3.Request
import org.json.JSONObject
import xyz.zongtech.campuswall.BuildConfig
import xyz.zongtech.campuswall.R
import xyz.zongtech.campuswall.WallApi
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.displayError
import xyz.zongtech.campuswall.objects
import xyz.zongtech.campuswall.pathSegment
import xyz.zongtech.campuswall.s
import xyz.zongtech.campuswall.strings

private val shanghai: ZoneId = ZoneId.of("Asia/Shanghai")

/** 与网页 dayjs().fromNow() 一致的相对时间。 */
fun relativeTime(value: String): String {
    if (value.isBlank()) return "刚刚"
    val time =
        runCatching { LocalDateTime.parse(value.replace(' ', 'T').take(19)).atZone(shanghai).toInstant() }
            .recoverCatching { Instant.parse(value) }
            .getOrNull() ?: return value
    val seconds = Duration.between(time, Instant.now()).seconds
    return when {
        seconds < 45 -> "几秒前"
        seconds < 90 -> "1 分钟前"
        seconds < 2700 -> "${(seconds + 30) / 60} 分钟前"
        seconds < 5400 -> "1 小时前"
        seconds < 79200 -> "${(seconds + 1800) / 3600} 小时前"
        seconds < 129600 -> "1 天前"
        seconds < 2246400 -> "${(seconds + 43200) / 86400} 天前"
        seconds < 3974400 -> "1 个月前"
        seconds < 27648000 -> "${(seconds + 1296000) / 2592000} 个月前"
        else -> "${seconds / 31536000} 年前"
    }
}

fun shortTime(value: String): String =
    runCatching { LocalDateTime.parse(value.replace(' ', 'T').take(19)).format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) }
        .getOrDefault(value)

fun avatarUrl(userId: String) = "${BuildConfig.API_URL}/api/user/$userId/avatar"

@Composable
fun Avatar(url: String, size: Dp, modifier: Modifier = Modifier, anonymous: Boolean = false, onClick: (() -> Unit)? = null) {
    Box(modifier) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier.size(size)
                    .clip(CircleShape)
                    .background(W.surface3)
                    .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
        )
        if (anonymous)
            Box(
                Modifier.align(Alignment.BottomEnd).size(size * 0.42f).clip(CircleShape).background(W.ink).border(2.dp, W.bg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.PersonOff, "匿名", Modifier.size(size * 0.24f), tint = W.bg)
            }
    }
}

private fun greeting(): String {
    val hour = LocalDateTime.now(shanghai).hour
    return when {
        hour < 5 -> "夜深了"
        hour < 11 -> "早上好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        else -> "晚上好"
    }
}

/** 动态列表的通用加载逻辑（加载更多，与网页相同每页 15 条）。 */
@Composable
fun rememberFeed(model: WallModel, key: Any, load: suspend (start: Int, end: Int) -> List<JSONObject>): FeedState {
    val state = remember(key) { FeedState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(key, model.revision, state.reload) {
        state.loading = true
        state.error = null
        try {
            val rows = load(0, 15)
            state.rows = rows
            state.hasMore = rows.size == 15
            rows.filter { it.optBoolean("owned") }.forEach { if (it.s("id") !in model.ownedPosts) model.ownedPosts.add(it.s("id")) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            state.error = e.displayError()
        } finally {
            state.loading = false
        }
    }
    state.more = {
        if (!state.loadingMore && state.hasMore)
            scope.launch {
                state.loadingMore = true
                try {
                    val start = state.rows.size
                    val rows = load(start, start + 15)
                    state.rows = state.rows + rows.filter { r -> state.rows.none { it.s("id") == r.s("id") } }
                    state.hasMore = rows.size == 15
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    model.error = e.displayError()
                } finally {
                    state.loadingMore = false
                }
            }
    }
    return state
}

class FeedState {
    var rows by mutableStateOf(emptyList<JSONObject>())
    var loading by mutableStateOf(true)
    var loadingMore by mutableStateOf(false)
    var hasMore by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var reload by mutableIntStateOf(0)
    var more: () -> Unit = {}
}

/** 帖子列表 + 加载更多，帖子之间用发丝线分隔（网页手机端样式）。 */
fun androidx.compose.foundation.lazy.LazyListScope.feedItems(state: FeedState, model: WallModel, go: (String) -> Unit, emptyText: String = "这里还很安静，来发第一条吧。") {
    if (state.loading && state.rows.isEmpty()) item { Spinner() }
    state.error?.let { e -> item { EmptyCard("加载失败", e, actionText = "重试", action = { state.reload++ }) } }
    if (!state.loading && state.error == null && state.rows.isEmpty()) item { EmptyCard("暂时没有动态", emptyText, icon = Icons.Rounded.ChatBubbleOutline) }
    items(state.rows, key = { it.s("id") }) { PostItem(it, model, go) }
    if (state.hasMore)
        item {
            Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                WButton(if (state.loadingMore) "加载中…" else "加载更多", state.more, icon = Icons.Rounded.KeyboardArrowDown, enabled = !state.loadingMore)
            }
        }
}

@Composable
fun WallScreen(model: WallModel, go: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var applied by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("all") }
    var sort by rememberSaveable { mutableStateOf("newest") }
    val feed =
        rememberFeed(model, listOf(applied, filter, sort)) { start, end ->
            model.api
                .request("/api/get_messages?s=$sort&w=${applied.pathSegment()}&f=$filter&start=$start&end=$end")
                .objects("data")
        }
    val name = model.user?.let { it.s("nickname").ifBlank { it.s("username") } }.orEmpty().ifBlank { "同学" }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 20.dp, bottom = LocalBottomSpace.current),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.WbSunny, null, Modifier.size(26.dp), tint = W.accent)
                    Text("${greeting()}，$name", style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                SearchBar(query, { query = it }, "搜索动态或标签", { applied = query.trim() })
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Seg(listOf("all" to "全部", "files" to "图影音", "polls" to "投票"), filter, { filter = it })
                    Box(Modifier.weight(1f))
                    Select(listOf("newest" to "最新发布", "likes" to "点赞最多", "dislikes" to "点踩最多"), sort, { sort = it })
                    IconBtn(Icons.Rounded.Refresh, "刷新列表", { feed.reload++ }, size = 36.dp)
                }
                if (applied.isNotEmpty() && !feed.loading)
                    Callout("关键词「$applied」共找到 ${feed.rows.size} 条动态", icon = Icons.Rounded.Search)
                Gap(4.dp)
            }
        }
        feedItems(feed, model, go)
    }
}

data class LostFound(
    val kind: String,
    val resolved: Boolean,
    val item: String,
    val location: String,
    val time: String,
    val details: String,
    val contact: String,
) {
    val status: String
        get() = if (resolved) "已找回" else if (kind == "found") "待认领" else "寻找中"
}

private fun lostField(text: String, label: String): String =
    Regex("$label[：:]\\s*([^\\n]+)").find(text)?.groupValues?.get(1)?.trim().orEmpty()

fun describeLostFound(post: JSONObject): LostFound? {
    val tags = post.strings("tags")
    val data = post.optJSONObject("lost_found")
    if (data == null && "寻物启事" !in tags && "招领启事" !in tags) return null
    val text = post.s("text")
    val d = data ?: JSONObject()
    return LostFound(
        kind = d.s("kind").ifBlank { if ("招领启事" in tags) "found" else "lost" },
        resolved = d.optBoolean("resolved") || "已找回" in tags,
        item = d.s("item").ifBlank { lostField(text, "物品") },
        location = d.s("location").ifBlank { lostField(text, "地点") },
        time = d.s("time").ifBlank { lostField(text, "时间") },
        details = d.s("details").ifBlank { lostField(text, "特征与说明") },
        contact = d.s("contact").ifBlank { lostField(text, "联系") },
    )
}

/** 网页 MessageCard：头像、身份、正文、媒体、投票、话题、表态栏与评论串。 */
@Composable
fun PostItem(
    initial: JSONObject,
    model: WallModel,
    go: (String) -> Unit,
    detail: Boolean = false,
    onEdit: ((JSONObject) -> Unit)? = null,
    onDelete: ((JSONObject) -> Unit)? = null,
) {
    var post by remember(initial.toString()) { mutableStateOf(initial) }
    val id = post.s("id")
    val c = W
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var commentOpen by remember { mutableStateOf(detail) }
    var expanded by remember { mutableStateOf(detail) }
    var showAllComments by remember { mutableStateOf(detail) }
    var textExpanded by remember { mutableStateOf(detail) }
    val anonymous = post.optBoolean("anonymous", true)
    val pending = post.s("moderation_status") == "pending"
    val hidden = post.s("moderation_status") == "hidden"
    val unavailable = pending || hidden
    val lostFound = describeLostFound(post)
    val comments = post.objects("comments")
    val user = model.user
    val guestNeedsLogin = user == null && !model.community.optBoolean("guest_commenting_enabled")
    val favorited = id in model.favoriteIds

    fun refresh() {
        scope.launch {
            try {
                model.api.request("/api/get_message_details/$id", "POST").optJSONObject("message")?.let { post = it }
            } catch (_: Exception) {}
        }
    }

    fun react(kind: String) {
        if (unavailable) return
        scope.launch {
            try {
                val r = model.api.request("/api/wall/$kind/$id", "POST")
                post =
                    JSONObject(post.toString())
                        .put("likes", r.optInt("likes", post.optInt("likes")))
                        .put("dislikes", r.optInt("dislikes", post.optInt("dislikes")))
                        .put("liked", r.optBoolean("liked", post.optBoolean("liked")))
                        .put("disliked", r.optBoolean("disliked", post.optBoolean("disliked")))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                model.error = e.displayError()
            }
        }
    }

    Column(
        Modifier.fillMaxWidth()
            .then(if (!detail) Modifier.clickable(interactionSource = null, indication = null) { go("message/$id") } else Modifier)
            .padding(top = 20.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(
                avatarUrl(if (anonymous) "0" else post.s("user_id")),
                42.dp,
                anonymous = anonymous,
                onClick = if (anonymous || post.s("user_id").isBlank()) null else ({ go("profile/${post.s("user_id")}") }),
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (anonymous) "匿名用户" else post.s("display_name_snapshot", "同学"),
                        Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (post.optBoolean("official")) Badge("官方", Tone.Accent)
                }
                Text(
                    "${relativeTime(post.s("timestamp"))} · ${if (anonymous) "匿名" else "展示昵称"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.ink3,
                )
            }
            Box {
                IconBtn(Icons.Rounded.MoreHoriz, "更多操作", { menu = true }, size = 36.dp)
                DropdownMenu(menu, { menu = false }, containerColor = c.surface) {
                    MenuItem(Icons.Rounded.Share, "分享", enabled = !unavailable) {
                        menu = false
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "https://wall.zongtech.xyz/wall/message/$id"),
                                "分享校园墙",
                            )
                        )
                    }
                    if (!unavailable) {
                        if (!detail)
                            MenuItem(Icons.Rounded.OpenInNew, "查看详情") {
                                menu = false
                                go("message/$id")
                            }
                        MenuItem(Icons.Rounded.Flag, "举报违规") {
                            menu = false
                            go("report/$id")
                        }
                    }
                    onEdit?.let { edit ->
                        MenuItem(Icons.Rounded.Edit, "编辑") {
                            menu = false
                            edit(post)
                        }
                    }
                    onDelete?.let { del ->
                        MenuItem(Icons.Rounded.Delete, "删除", danger = true) {
                            menu = false
                            del(post)
                        }
                    }
                }
            }
        }
        val flags = listOfNotNull(
            if (post.optBoolean("pinned")) Triple("置顶", Tone.Warning, Icons.Rounded.PushPin) else null,
            if (post.optBoolean("featured")) Triple("精华", Tone.Success, Icons.Rounded.Star) else null,
            if (post.s("edited_at").isNotBlank()) Triple("已编辑", Tone.Neutral, null) else null,
        )
        if (flags.isNotEmpty())
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { flags.forEach { (t, tone, icon) -> Badge(t, tone, icon) } }
        if (unavailable)
            Callout(
                if (pending) "这条留言正在等待审核，通过后才会出现在公开页面。"
                else "这条留言已被管理员下架：${post.s("hidden_reason", "违反社区规范")}",
                Tone.Warning,
                if (pending) Icons.Rounded.HourglassTop else Icons.Rounded.VisibilityOff,
            )
        lostFound?.let { LostFoundBlock(it) }
        val body = if (lostFound != null) lostFound.details.ifBlank { if (lostFound.item.isBlank()) post.s("text") else "" } else post.s("text")
        if (body.isNotBlank()) {
            Text(
                body,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.5.sp, lineHeight = 29.sp),
                maxLines = if (textExpanded) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { if (!textExpanded && it.hasVisualOverflow) expanded = false },
            )
            if (!textExpanded && body.length > 140)
                Text("展开全文", Modifier.clickable { textExpanded = true }, color = c.accent, style = MaterialTheme.typography.labelMedium)
        }
        val files = post.strings("files")
        if (files.isNotEmpty()) MediaGrid(files, model.api, limit = if (detail) files.size else 9)
        post.optJSONObject("poll")?.let { Poll(it, id, model) { p -> post = JSONObject(post.toString()).put("poll", p) } }
        val tags = post.strings("tags")
        if (tags.isNotEmpty())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { t -> Tag("#$t") { go(tagRoute(t)) } }
            }
        Row(verticalAlignment = Alignment.CenterVertically) {
            React(if (post.optBoolean("liked")) Icons.Rounded.ThumbUp else Icons.Outlined.ThumbUp, post.optInt("likes"), "点赞", post.optBoolean("liked")) { react("like") }
            React(Icons.Outlined.Sms, comments.size, "评论", commentOpen) { commentOpen = !commentOpen }
            React(if (post.optBoolean("disliked")) Icons.Rounded.ThumbDown else Icons.Outlined.ThumbDown, post.optInt("dislikes"), "点踩", post.optBoolean("disliked"), down = true) { react("dislike") }
            Box(Modifier.weight(1f))
            if (user != null && !unavailable)
                IconBtn(
                    if (favorited) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    if (favorited) "取消收藏" else "收藏",
                    {
                        scope.launch {
                            try {
                                model.api.request("/api/user/me/favorites/$id", if (favorited) "DELETE" else "POST")
                                if (favorited) model.favoriteIds.remove(id) else model.favoriteIds.add(id)
                                model.error = if (favorited) "已取消收藏" else "已加入「我的收藏」"
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                model.error = e.displayError()
                            }
                        }
                    },
                    size = 36.dp,
                    tint = if (favorited) Color(0xFFD89B2B) else Color.Unspecified,
                )
        }
        if (guestNeedsLogin && !unavailable)
            Row(
                Modifier.fillMaxWidth().clip(RMd).background(c.accentSoft).clickable { go("login") }.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Rounded.Login, null, Modifier.size(18.dp), tint = c.accentStrong)
                Text("  登录后参与讨论", color = c.accentStrong, style = MaterialTheme.typography.labelLarge)
            }
        val canComment = model.community.optBoolean("commenting_enabled", true) && (user != null || model.community.optBoolean("guest_commenting_enabled"))
        if (comments.isNotEmpty() || (commentOpen && !unavailable && (canComment || user != null)))
            Thread(
                post,
                comments,
                model,
                go,
                showAll = showAllComments,
                composer = commentOpen && !unavailable,
                onShowAll = {
                    showAllComments = true
                    commentOpen = true
                },
                onChanged = { refresh() },
            )
    }
    if (!detail) Hairline()
}

@Composable
fun MenuItem(icon: ImageVector, text: String, danger: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text, color = if (danger) W.danger else W.ink) },
        onClick = onClick,
        enabled = enabled,
        leadingIcon = { Icon(icon, null, tint = if (danger) W.danger else W.ink2) },
    )
}

@Composable
private fun React(icon: ImageVector, count: Int, label: String, on: Boolean, down: Boolean = false, onClick: () -> Unit) {
    val c = W
    val color = if (on) (if (down) c.ink else c.accent) else c.ink3
    Row(
        Modifier.clip(RSm).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = "$label $count" }.padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = color)
        Text("$count", color = color, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun LostFoundBlock(lf: LostFound) {
    val c = W
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Badge(
                if (lf.kind == "found") "招领启事" else "寻物启事",
                Tone.Accent,
                if (lf.kind == "found") Icons.Rounded.Inventory2 else Icons.Rounded.Search,
            )
            Badge(lf.status, if (lf.resolved) Tone.Success else Tone.Warning)
        }
        Text(lf.item.ifBlank { if (lf.kind == "found") "招领启事" else "寻物启事" }, style = MaterialTheme.typography.headlineSmall)
        LfFact(Icons.Rounded.LocationOn, lf.location.ifBlank { "地点未填写" })
        if (lf.time.isNotBlank()) LfFact(Icons.Rounded.Schedule, "${if (lf.kind == "found") "拾获" else "丢失"} · ${lf.time}")
        if (lf.contact.isNotBlank()) LfFact(Icons.Rounded.Sms, lf.contact)
    }
}

@Composable
private fun LfFact(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, Modifier.size(16.dp), tint = W.ink3)
        Text(text, style = MaterialTheme.typography.bodySmall, color = W.ink2)
    }
}

/** 评论串：楼层、引用、回复、举报、删除，以及评论输入框。 */
@Composable
private fun Thread(
    post: JSONObject,
    comments: List<JSONObject>,
    model: WallModel,
    go: (String) -> Unit,
    showAll: Boolean,
    composer: Boolean,
    onShowAll: () -> Unit,
    onChanged: () -> Unit,
) {
    val c = W
    val id = post.s("id")
    var text by rememberSaveable(id) { mutableStateOf("") }
    var reply by remember { mutableStateOf<Pair<Int, JSONObject>?>(null) }
    var deleting by remember { mutableStateOf<JSONObject?>(null) }
    val canComment = model.community.optBoolean("commenting_enabled", true) && (model.user != null || model.community.optBoolean("guest_commenting_enabled"))
    val visible = if (showAll) comments else comments.take(2)
    Column(
        Modifier.fillMaxWidth().clip(RMd).background(c.surface2).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        visible.forEachIndexed { index, comment ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("#${index + 1}", color = c.ink3, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (comment.optBoolean("owned")) Badge("我的评论", Tone.Accent)
                        Text(comment.s("timestamp"), style = MaterialTheme.typography.labelSmall, color = c.ink3)
                    }
                    if (comment.s("refer_id").isNotBlank())
                        Row(
                            Modifier.clip(RSm).background(c.surface3).padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Reply, null, Modifier.size(14.dp), tint = c.ink3)
                            Text(comment.s("refer").ifBlank { "评论内容已不可见" }, style = MaterialTheme.typography.bodySmall, color = c.ink2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    if (comment.s("text").isNotBlank()) Text(comment.s("text"), style = MaterialTheme.typography.bodyMedium)
                    val files = comment.strings("files")
                    if (files.isNotEmpty()) MediaGrid(files, model.api, limit = files.size, compact = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (canComment) TextLink("回复", Icons.AutoMirrored.Rounded.Reply) { reply = (index + 1) to comment }
                        if (comment.s("id").isNotBlank())
                            TextLink("举报", Icons.Rounded.Flag) { go("report/${("$id/comment/${comment.s("id")}").pathSegment()}") }
                        if (comment.optBoolean("owned")) TextLink("删除", Icons.Rounded.Delete, danger = true) { deleting = comment }
                    }
                }
            }
        }
        if (comments.size > 2 && !showAll)
            Text("展开全部 ${comments.size} 条评论", Modifier.clickable(onClick = onShowAll), color = c.accent, style = MaterialTheme.typography.labelMedium)
        if (composer && !canComment && model.user != null)
            Callout(model.community.s("pause_reason").ifBlank { "管理员暂时关闭了评论功能" }, Tone.Warning)
        if (composer && canComment)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reply?.let { (floor, cmt) ->
                    Row(
                        Modifier.fillMaxWidth().clip(RSm).background(c.accentSoft).padding(start = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("正在回复 #$floor 楼：${cmt.s("text")}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = c.accentStrong, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        IconBtn(Icons.Rounded.Close, "取消回复", { reply = null }, size = 32.dp)
                    }
                }
                Field(
                    text,
                    { text = it },
                    placeholder = reply?.let { "回复 #${it.first} 楼…" } ?: "友善表达，写下你的评论…",
                    singleLine = false,
                    minLines = 3,
                    maxLength = 500,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    WButton(
                        if (model.busy) "发送中…" else "发表评论",
                        {
                            model.perform({ onChanged() }) {
                                val fields = mutableMapOf("text" to text)
                                reply?.second?.let {
                                    fields["refer_id"] = it.s("id")
                                    fields["refer"] = it.s("text")
                                }
                                model.api.request("/api/wall/comment/$id", "POST", fields = fields)
                                text = ""
                                reply = null
                            }
                        },
                        kind = Btn.Primary,
                        small = true,
                        icon = Icons.AutoMirrored.Rounded.Send,
                        enabled = text.isNotBlank() && !model.busy,
                    )
                }
            }
    }
    deleting?.let { cmt ->
        Confirm(
            "删除我的评论",
            "删除后评论会立即从公开页面和你的评论列表中移除，留言作者收到的历史通知仍会保留。",
            "确认删除",
            { deleting = null },
            {
                model.perform({ onChanged() }) {
                    model.api.request("/api/user/me/comments/$id/${cmt.s("id").pathSegment()}", "DELETE")
                    deleting = null
                }
            },
        )
    }
}

@Composable
fun TextLink(text: String, icon: ImageVector? = null, danger: Boolean = false, onClick: () -> Unit) {
    val color = if (danger) W.danger else W.ink3
    Row(Modifier.clip(RSm).clickable(onClick = onClick).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        icon?.let { Icon(it, null, Modifier.size(14.dp), tint = color) }
        Text(text, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun Poll(poll: JSONObject, id: String, model: WallModel, onUpdate: (JSONObject) -> Unit) {
    val c = W
    val options = poll.objects("options")
    val total = poll.optInt("total_votes", options.sumOf { it.optInt("votes") })
    val voted = poll.optBoolean("has_voted") || poll.s("selected_option_id").isNotBlank()
    val closed = poll.optBoolean("is_closed")
    val show = voted || closed
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxWidth().clip(RMd).background(c.surface).border(1.dp, c.line, RMd).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(poll.s("question"), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Badge(if (closed) "已结束" else "单选")
        }
        options.forEach { option ->
            val votes = option.optInt("votes")
            val fraction = if (total > 0) votes / total.toFloat() else 0f
            val mine = poll.s("selected_option_id") == option.s("id")
            Box(
                Modifier.fillMaxWidth()
                    .clip(RSm)
                    .border(1.dp, if (mine) c.accentLine else c.lineStrong, RSm)
                    .clickable(enabled = !show && !model.busy) {
                        scope.launch {
                            try {
                                val r = model.api.request("/api/wall/poll/$id/vote", "POST", json = JSONObject().put("option_id", option.opt("id")))
                                r.optJSONObject("poll")?.let(onUpdate)
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                model.error = e.displayError()
                            }
                        }
                    }
            ) {
                if (show) Box(Modifier.matchParentSize().fillMaxWidth(fraction.coerceAtLeast(0.001f)).background(if (mine) c.accentSoft2 else c.surface3))
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(option.s("text", option.s("label")), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    if (show) Text("${(fraction * 100).toInt()}% · $votes", style = MaterialTheme.typography.labelMedium, color = c.ink2)
                }
            }
        }
        Text(
            "$total 人参与" + if (closed) " · 投票已结束" else if (voted) " · 你已投票" else "",
            style = MaterialTheme.typography.labelSmall,
            color = c.ink3,
        )
    }
}

private val imageTypes = setOf("jpg", "jpeg", "png", "gif", "webp", "avif", "heic")
private val videoTypes = setOf("mp4", "webm", "mov", "m4v")
private val audioTypes = setOf("mp3", "wav", "ogg", "m4a", "aac", "flac")

fun uploadUrl(file: String) = "${BuildConfig.API_URL}/static/uploads/${file.pathSegment()}"

/** 网页 `.media` 宫格：1 张单列，2/4 张两列，其余三列。 */
@Composable
fun MediaGrid(files: List<String>, api: WallApi, limit: Int = 9, compact: Boolean = false) {
    var preview by remember { mutableStateOf<String?>(null) }
    val shown = files.take(limit)
    val cols = if (compact) 3 else when (shown.size) {
        1 -> 1
        2, 4 -> 2
        else -> 3
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        shown.chunked(cols).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEachIndexed { i, file ->
                    val index = rowIndex * cols + i
                    val remaining = if (index == shown.lastIndex) files.size - shown.size else 0
                    MediaTile(file, Modifier.weight(1f), single = cols == 1, remaining = remaining) { preview = file }
                }
                repeat(cols - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
    preview?.let { MediaViewer(it, api) { preview = null } }
}

@Composable
private fun MediaTile(file: String, modifier: Modifier, single: Boolean, remaining: Int, onClick: () -> Unit) {
    val c = W
    val ext = file.substringAfterLast('.').lowercase()
    Box(
        modifier
            .then(if (single) Modifier.heightIn(max = 360.dp) else Modifier.aspectRatio(1f))
            .clip(RMd)
            .background(c.surface3)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (ext) {
            in imageTypes ->
                AsyncImage(
                    uploadUrl(file),
                    "图片，点击查看",
                    Modifier.fillMaxWidth().then(if (single) Modifier else Modifier.fillMaxSize()),
                    contentScale = if (single) ContentScale.FillWidth else ContentScale.Crop,
                )
            else ->
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        when (ext) {
                            in videoTypes -> Icons.Rounded.PlayArrow
                            in audioTypes -> Icons.Rounded.MusicNote
                            else -> Icons.Rounded.AttachFile
                        },
                        null,
                        Modifier.size(28.dp),
                        tint = c.accent,
                    )
                    Text(file.substringAfterLast('/'), style = MaterialTheme.typography.labelSmall, color = c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
        }
        if (remaining > 0)
            Box(Modifier.matchParentSize().background(Color.Black.copy(0.45f)), contentAlignment = Alignment.Center) {
                Text("+$remaining", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            }
    }
}

/** 全屏预览：图片双指缩放，音视频原生播放，其它附件交给系统应用打开。 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun MediaViewer(file: String, api: WallApi, close: () -> Unit) {
    val ext = file.substringAfterLast('.').lowercase()
    val url = uploadUrl(file)
    val context = LocalContext.current
    if (ext !in imageTypes && ext !in videoTypes && ext !in audioTypes) {
        LaunchedEffect(file) {
            try {
                val local =
                    withContext(Dispatchers.IO) {
                        val dir = File(context.cacheDir, "attachments").apply { mkdirs() }
                        val target = File(dir, file.substringAfterLast('/').substringAfterLast('\\'))
                        api.client.newCall(Request.Builder().url(url).build()).execute().use { r ->
                            check(r.isSuccessful) { "附件暂时无法读取" }
                            r.body!!.byteStream().use { input -> target.outputStream().use { input.copyTo(it) } }
                        }
                        target
                    }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", local)
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(uri, android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.widget.Toast.makeText(
                    context,
                    if (e is android.content.ActivityNotFoundException) "手机上没有可打开此文件的应用" else e.displayError(),
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
            } finally {
                close()
            }
        }
        return
    }
    Dialog(close, DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            if (ext in imageTypes) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                AsyncImage(
                    url,
                    "图片预览，双指缩放",
                    Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset = if (scale == 1f) Offset.Zero else offset + pan
                            }
                        }
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y),
                    contentScale = ContentScale.Fit,
                )
            } else {
                val player =
                    remember(url) {
                        val cookies = api.cookies.loadForRequest(url.toHttpUrl()).joinToString("; ") { "${it.name}=${it.value}" }
                        ExoPlayer.Builder(context)
                            .setMediaSourceFactory(
                                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(
                                    androidx.media3.datasource.DefaultHttpDataSource.Factory().setDefaultRequestProperties(mapOf("Cookie" to cookies))
                                )
                            )
                            .build()
                            .apply {
                                setMediaItem(MediaItem.fromUri(url))
                                prepare()
                                playWhenReady = true
                            }
                    }
                DisposableEffect(player) { onDispose { player.release() } }
                AndroidView(
                    factory = {
                        (LayoutInflater.from(it).inflate(R.layout.player_view, android.widget.FrameLayout(it), false) as PlayerView).apply { this.player = player }
                    },
                    modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
                )
            }
            IconBtn(Icons.Rounded.Close, "关闭", close, Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp), tint = Color.White)
        }
    }
}

@Composable
fun DetailScreen(model: WallModel, id: String, go: (String) -> Unit, back: () -> Unit) {
    var post by remember { mutableStateOf<JSONObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<JSONObject?>(null) }
    var deleting by remember { mutableStateOf(false) }
    LaunchedEffect(id, model.revision, reload) {
        try {
            post = model.api.request("/api/get_message_details/$id", "POST").optJSONObject("message")
            error = null
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.displayError()
        }
    }
    val p = post
    val owner = p != null && (p.optBoolean("owned") || id in model.ownedPosts || (model.user != null && p.s("user_id") == model.user?.s("id")))
    LazyColumn(
        Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
    ) {
        item { PageHead("留言详情", "#$id", back = "返回全部动态" to back) }
        error?.let { e -> item { EmptyCard("这条留言暂时无法查看", e, actionText = "重试", action = { reload++ }) } }
        if (p == null && error == null) item { Spinner() }
        p?.let {
            item(key = it.toString().hashCode()) {
                PostItem(it, model, go, detail = true, onEdit = if (owner) ({ e -> editing = e }) else null, onDelete = if (owner) ({ deleting = true }) else null)
                Hairline()
            }
        }
    }
    editing?.let { e -> EditPostSheet(model, e) { editing = null } }
    if (deleting)
        Confirm("删除这条动态？", "动态将从校园墙移除。", "删除", { deleting = false }, {
            model.perform(back) {
                model.api.request("/api/user/me/messages/$id", "DELETE")
                deleting = false
            }
        })
}

@Composable
fun EditPostSheet(model: WallModel, post: JSONObject, close: () -> Unit) {
    var text by remember { mutableStateOf(post.s("text")) }
    var tags by remember { mutableStateOf(post.strings("tags").joinToString(",")) }
    var anonymous by remember { mutableStateOf(post.optBoolean("anonymous", true)) }
    Sheet(
        "编辑动态",
        close,
        footer = {
            WButton("取消", close)
            WButton(
                "保存",
                {
                    model.perform(close) {
                        model.api.request(
                            "/api/user/me/messages/${post.s("id")}",
                            "PUT",
                            fields = mapOf("text" to text, "tags" to tags.replace('，', ','), "anonymous" to anonymous.toString()),
                        )
                    }
                },
                kind = Btn.Primary,
                enabled = !model.busy,
            )
        },
    ) {
        Field(text, { text = it }, label = "正文", singleLine = false, minLines = 5, maxLength = 2000)
        Field(tags, { tags = it }, label = "话题", placeholder = "多个话题用逗号分隔")
        if (post.optJSONObject("lost_found") == null) SwitchRow("匿名发布", anonymous, { anonymous = it })
    }
}

private val presetTags = listOf("日常", "表白", "树洞", "提问", "吐槽", "寻物", "学习", "互助")

/** 网页的「发布校园动态」弹窗，这里是一个独立页面。 */
@Composable
fun ComposeScreen(model: WallModel, initialTag: String, back: () -> Unit) {
    val c = W
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("draft.${model.user?.s("id") ?: "guest"}", 0) }
    var text by rememberSaveable { mutableStateOf(prefs.getString("text", "").orEmpty()) }
    var tags by rememberSaveable { mutableStateOf(listOfNotNull(initialTag.ifBlank { null })) }
    var tagInput by rememberSaveable { mutableStateOf("") }
    var anonymous by rememberSaveable { mutableStateOf(true) }
    var official by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf("post") }
    var question by rememberSaveable { mutableStateOf("") }
    var options by rememberSaveable { mutableStateOf(listOf("", "")) }
    var duration by rememberSaveable { mutableStateOf("3") }
    var files by remember { mutableStateOf(emptyList<Uri>()) }
    var cameraUri by rememberSaveable { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { files = (files + it).distinct().take(20) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { if (it) files = (files + Uri.parse(cameraUri)).take(20) }
    LaunchedEffect(text) { prefs.edit().putString("text", text).apply() }
    val user = model.user
    val canPost = model.community.optBoolean("posting_enabled", true) && (user != null || model.community.optBoolean("guest_posting_enabled"))
    fun addTag(raw: String) {
        val t = raw.trim().trim(',', '，', '#')
        if (t.isNotEmpty() && t !in tags && tags.size < 8) tags = tags + t
        tagInput = ""
    }
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = PageGutter, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PageHead("发布校园动态", back = "返回" to back)
            if (!model.community.optBoolean("posting_enabled", true))
                Callout(model.community.s("pause_reason").ifBlank { "管理员暂时关闭了发帖功能" }, Tone.Warning)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(avatarUrl(if (user == null || anonymous) "0" else user.s("id")), 40.dp, anonymous = user == null || anonymous)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(if (user == null || anonymous) "匿名用户" else user.s("nickname").ifBlank { user.s("username") }, style = MaterialTheme.typography.titleSmall)
                    Text(if (user == null || anonymous) "公开页面不会显示你的身份" else "公开页面将显示你的昵称", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                }
                if (user != null) Chip(if (anonymous) "匿名发布" else "展示昵称", !anonymous, { anonymous = !anonymous })
                else Badge("游客仅能匿名发布")
            }
            Seg(listOf("post" to "图文动态", "poll" to "发起投票"), mode, { mode = it }, Modifier.fillMaxWidth(), fill = true)
            Card(Modifier.fillMaxWidth(), padding = PaddingValues(14.dp)) {
                Field(
                    text,
                    { text = it },
                    placeholder = if (mode == "poll") "补充投票背景或说明（选填）" else "这一刻，想和大家分享什么？",
                    singleLine = false,
                    minLines = 6,
                    maxLength = 2000,
                )
                if (mode == "post" || files.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        files.forEach { uri ->
                            Box(Modifier.size(84.dp).clip(RMd).background(c.surface3)) {
                                AsyncImage(uri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                Text(displayName(context, uri), Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color.Black.copy(0.4f)).padding(4.dp), color = Color.White, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Box(
                                    Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp).clip(CircleShape).background(Color.Black.copy(0.55f)).clickable { files = files - uri },
                                    contentAlignment = Alignment.Center,
                                ) { Icon(Icons.Rounded.Close, "移除", Modifier.size(14.dp), tint = Color.White) }
                            }
                        }
                        if (files.size < 20)
                            Column(
                                Modifier.size(84.dp).clip(RMd).border(1.dp, c.lineStrong, RMd).clickable { picker.launch(arrayOf("image/*", "video/*", "audio/*")) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(Icons.Rounded.Add, null, tint = c.ink2)
                                Text(if (files.isEmpty()) "图片 / 视频" else "继续添加", style = MaterialTheme.typography.labelSmall, color = c.ink2)
                                Text("${files.size}/20", style = MaterialTheme.typography.labelSmall, color = c.ink3)
                            }
                    }
                    WButton(
                        "拍照",
                        {
                            val dir = File(context.cacheDir, "photos").apply { mkdirs() }
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", File.createTempFile("photo", ".jpg", dir))
                            cameraUri = uri.toString()
                            camera.launch(uri)
                        },
                        small = true,
                        kind = Btn.Ghost,
                    )
                }
            }
            if (mode == "poll")
                Card(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("投票设置", style = MaterialTheme.typography.titleSmall)
                            Text("每个访问者只能选择一项，投票后不可修改。", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                        }
                        Badge("单选")
                    }
                    Field(question, { question = it }, placeholder = "输入投票问题", maxLength = 200)
                    options.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${index + 1}", Modifier.width(18.dp), color = c.ink3)
                            Field(option, { v -> options = options.mapIndexed { i, o -> if (i == index) v.take(80) else o } }, Modifier.weight(1f), placeholder = "选项 ${index + 1}")
                            if (options.size > 2) IconBtn(Icons.Rounded.Close, "删除选项 ${index + 1}", { options = options.filterIndexed { i, _ -> i != index } }, size = 36.dp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WButton("添加选项", { options = options + "" }, small = true, enabled = options.size < 6, icon = Icons.Rounded.Add)
                        Box(Modifier.weight(1f))
                        Text("结束时间", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                        Select(listOf("1" to "1 天后", "3" to "3 天后", "7" to "7 天后", "none" to "长期有效"), duration, { duration = it })
                    }
                }
            Card(Modifier.fillMaxWidth()) {
                Row {
                    Text("# 添加话题", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Text("最多 8 个", style = MaterialTheme.typography.bodySmall, color = c.ink3)
                }
                if (tags.isNotEmpty())
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.forEach { t -> Chip("#$t", true, { tags = tags - t }, icon = Icons.Rounded.Close) }
                    }
                Field(
                    tagInput,
                    { v -> if (v.endsWith(',') || v.endsWith('，')) addTag(v) else tagInput = v },
                    placeholder = "输入标签后按回车确认，如：日常、寻物",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { addTag(tagInput) }),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetTags.filter { it !in tags }.forEach { t -> Tag("#$t") { addTag(t) } }
                }
            }
            if (model.can("content.publish.official")) SwitchRow("以官方身份发布", official, { official = it })
        }
        Hairline()
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = PageGutter, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${text.length} / 2000", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = c.ink3)
            WButton("取消", back)
            WButton(
                if (model.busy) "正在发布…" else "发布",
                {
                    model.perform(back) {
                        val clean = options.map { it.trim() }.filter { it.isNotEmpty() }
                        if (mode == "poll") require(question.isNotBlank() && clean.size >= 2) { "请填写投票问题和至少两个选项" }
                        else require(text.isNotBlank() || files.isNotEmpty()) { "请填写正文或添加图片" }
                        val uploads = files.map { model.api.upload(it) }
                        val body =
                            MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("text", text)
                                .addFormDataPart("tags", (tags + listOfNotNull(tagInput.trim().ifBlank { null })).joinToString(","))
                                .addFormDataPart("anonymous", (user == null || anonymous).toString())
                                .addFormDataPart("post_as_admin", official.toString())
                        if (mode == "poll") {
                            body.addFormDataPart("poll_question", question.trim())
                            clean.forEach { body.addFormDataPart("poll_options", it) }
                            if (duration != "none")
                                body.addFormDataPart("poll_closes_at", Instant.now().plus(Duration.ofDays(duration.toLong())).toString())
                        }
                        uploads.forEach { body.addFormDataPart("filenames", it) }
                        val r = model.api.request("/api/wall/submit", "POST", body = body.build())
                        prefs.edit().clear().apply()
                        model.error = if (r.s("moderation_status") == "pending") "已提交，审核通过后公开" else "发布成功"
                    }
                },
                kind = Btn.Primary,
                icon = Icons.AutoMirrored.Rounded.Send,
                enabled = canPost && !model.busy,
            )
        }
    }
}

fun displayName(context: android.content.Context, uri: Uri): String =
    runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else null }
        }
        .getOrNull() ?: uri.lastPathSegment.orEmpty()
