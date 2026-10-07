package xyz.zongtech.campuswall.ui

import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbDownOffAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.json.JSONObject
import xyz.zongtech.campuswall.BuildConfig
import xyz.zongtech.campuswall.R
import xyz.zongtech.campuswall.WallApi
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.displayError
import xyz.zongtech.campuswall.glass.Glass
import xyz.zongtech.campuswall.glass.GlassButton
import xyz.zongtech.campuswall.glass.GlassButtonStyle
import xyz.zongtech.campuswall.glass.GlassChip
import xyz.zongtech.campuswall.glass.GlassDialog
import xyz.zongtech.campuswall.glass.GlassIconButton
import xyz.zongtech.campuswall.glass.GlassLoading
import xyz.zongtech.campuswall.glass.GlassPanel
import xyz.zongtech.campuswall.glass.GlassProgressBar
import xyz.zongtech.campuswall.glass.GlassSegmented
import xyz.zongtech.campuswall.glass.GlassTextField
import xyz.zongtech.campuswall.glass.LocalBackdrop
import xyz.zongtech.campuswall.objects
import xyz.zongtech.campuswall.pathSegment
import xyz.zongtech.campuswall.s
import xyz.zongtech.campuswall.strings

/** 动态列表：校园首页、话题、收藏、我的发布、个人主页和失物招领共用。 */
@Composable
fun FeedScreen(
    model: WallModel,
    title: String,
    go: (String) -> Unit,
    tag: String = "",
    path: String = "/api/get_messages",
    back: (() -> Unit)? = null,
    subtitle: String? = null,
    header: (@Composable () -> Unit)? = null,
    extraActions: @Composable () -> Unit = {},
) {
    var rows by remember { mutableStateOf(emptyList<JSONObject>()) }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var query by rememberSaveable { mutableStateOf("") }
    var applied by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf("newest") }
    var contentFilter by rememberSaveable { mutableStateOf("all") }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var total by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    val mainFeed = path == "/api/get_messages"
    LaunchedEffect(path, tag, page, applied, sort, contentFilter, model.revision, refresh) {
        loading = true
        error = null
        try {
            val suffix =
                if (mainFeed)
                    "?start=${(page - 1) * 20}&end=${page * 20}&s=$sort&w=${applied.pathSegment()}&tag=${tag.pathSegment()}&f=$contentFilter"
                else "?page=$page&page_size=20&filter=$contentFilter"
            val r = model.api.request(path + suffix)
            val incoming = r.objects("data").ifEmpty { r.objects("messages") }
            total = r.optInt("total", incoming.size)
            rows =
                if (path.matches(Regex("/api/user/[0-9]+/messages")))
                    incoming.drop((page - 1) * 20).take(20)
                else incoming
            rows
                .filter { it.optBoolean("owned") }
                .forEach { if (it.s("id") !in model.ownedPosts) model.ownedPosts.add(it.s("id")) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.displayError()
        } finally {
            loading = false
        }
    }
    val confession = tag == "表白墙" || tag == "表白"
    Column(Modifier.fillMaxSize()) {
        GlassTopBar(
            title,
            back,
            subtitle ?: if (mainFeed && tag.isEmpty()) "观澜中学 · 同学们的日常" else null,
        ) {
            extraActions()
            if (mainFeed)
                GlassIconButton(
                    Icons.Rounded.FilterList,
                    "筛选附件或投票",
                    { showFilters = !showFilters },
                    style =
                        if (showFilters || contentFilter != "all") GlassButtonStyle.Prominent
                        else GlassButtonStyle.Normal,
                )
            if (tag.isNotEmpty())
                GlassIconButton(
                    Icons.Rounded.Edit,
                    "发布到此话题",
                    {
                        go(
                            if (
                                model.user != null ||
                                    model.community.optBoolean("guest_posting_enabled")
                            )
                                "compose/${tag.pathSegment()}"
                            else "login"
                        )
                    },
                )
            GlassIconButton(Icons.Rounded.Refresh, "刷新", { refresh++ })
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = LocalBottomSpace.current),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            header?.let { item { it() } }
            if (confession)
                item {
                    GlassPanel(Modifier.fillMaxWidth(), tint = Glass.colors.love) {
                        ParticleHeart()
                        Text(
                            "把心意写下来，温柔地说出口。",
                            Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Glass.colors.secondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            if (path == "/api/user/lost-found")
                item {
                    GlassSegmented(
                        listOf("all" to "全部", "lost" to "寻物", "found" to "招领", "resolved" to "已找回"),
                        contentFilter,
                        {
                            contentFilter = it
                            page = 1
                        },
                    )
                }
            if (mainFeed)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (tag.isEmpty())
                            Row(
                                Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                GlassChip("表白墙", false, { go(tagRoute("表白")) }, icon = Icons.Rounded.Favorite)
                                GlassChip("失物招领", false, { go("lost") }, icon = Icons.Rounded.Inventory2)
                                GlassChip("公告与帮助", false, { go("help") }, icon = Icons.Rounded.Campaign)
                            }
                        GlassTextField(
                            query,
                            { query = it },
                            Modifier.fillMaxWidth(),
                            placeholder = if (tag.isEmpty()) "搜索校园里的新鲜事" else "在此话题中搜索",
                            singleLine = true,
                            leadingIcon = Icons.Rounded.Search,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions =
                                KeyboardActions(
                                    onSearch = {
                                        applied = query
                                        page = 1
                                    }
                                ),
                            trailing = {
                                if (query.isNotEmpty())
                                    Icon(
                                        Icons.Rounded.Close,
                                        "清除搜索",
                                        Modifier.size(22.dp).clip(Capsule()).clickable {
                                            query = ""
                                            applied = ""
                                            page = 1
                                        },
                                        tint = Glass.colors.secondary,
                                    )
                            },
                        )
                        GlassSegmented(
                            listOf("newest" to "最新", "likes" to "热门", "dislikes" to "争议"),
                            sort,
                            {
                                sort = it
                                page = 1
                            },
                        )
                        if (showFilters || contentFilter != "all")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlassChip(
                                    "含附件",
                                    contentFilter == "files",
                                    {
                                        contentFilter = if (contentFilter == "files") "all" else "files"
                                        page = 1
                                    },
                                    icon = Icons.Rounded.AttachFile,
                                )
                                GlassChip(
                                    "含投票",
                                    contentFilter == "polls",
                                    {
                                        contentFilter = if (contentFilter == "polls") "all" else "polls"
                                        page = 1
                                    },
                                )
                            }
                    }
                }
            if (loading && rows.isEmpty())
                item { Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) { GlassLoading() } }
            else if (loading) item { GlassProgressBar() }
            error?.let { item { EmptyState(it) { refresh++ } } }
            if (!loading && error == null && rows.isEmpty())
                item { EmptyState("这里还很安静，来发第一条吧。") }
            items(rows, key = { it.s("id") }) { PostCard(it, model, go) }
            item {
                Pager(page, total, { page = it }, enabled = !loading)
            }
        }
    }
}

@Composable
fun Avatar(url: String, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier =
            modifier
                .size(size)
                .clip(Capsule())
                .border(1.dp, Color.White.copy(alpha = 0.5f), Capsule())
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
    )
}

fun avatarUrl(userId: String) = "${BuildConfig.API_URL}/api/user/$userId/avatar"

@Composable
fun PostCard(post: JSONObject, model: WallModel, go: (String) -> Unit, detail: Boolean = false) {
    val id = post.s("id")
    val anonymous = post.optBoolean("anonymous", true)
    val context = LocalContext.current
    val colors = Glass.colors
    GlassPanel(
        Modifier.fillMaxWidth(),
        onClick = if (detail) null else ({ go("message/$id") }),
        strong = detail,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(
                avatarUrl(if (anonymous) "0" else post.s("user_id")),
                42.dp,
                onClick = if (anonymous) null else ({ go("profile/${post.s("user_id")}") }),
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    if (anonymous) "匿名同学" else post.s("display_name_snapshot", "一位同学"),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(post.s("timestamp"), style = MaterialTheme.typography.labelSmall, color = colors.secondary)
            }
            if (post.optBoolean("featured"))
                Icon(Icons.Rounded.Star, "精华", Modifier.size(18.dp), tint = Color(0xFFF2B53A))
            if (post.optBoolean("pinned"))
                Icon(Icons.Rounded.PushPin, "置顶", Modifier.padding(start = 6.dp).size(18.dp), tint = colors.accent)
        }
        post.optJSONObject("lost_found")?.let { LostFoundInfo(it) }
        if (post.s("text").isNotBlank())
            Text(
                post.s("text"),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = if (detail) Int.MAX_VALUE else 8,
                overflow = TextOverflow.Ellipsis,
            )
        val tags = post.strings("tags")
        if (tags.isNotEmpty())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag -> GlassChip("# $tag", false, { go(tagRoute(tag)) }) }
            }
        post.strings("files").forEach { file -> Attachment(file, model.api) }
        post.optJSONObject("poll")?.let { Poll(it, id, model) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val liked = post.optBoolean("liked")
            ActionPill(
                if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                "${post.optInt("likes")}",
                if (liked) "取消点赞" else "点赞",
                tint = if (liked) colors.love else Color.Unspecified,
            ) {
                model.perform { model.api.request("/api/wall/like/$id", "POST") }
            }
            ActionPill(Icons.Rounded.ThumbDownOffAlt, "${post.optInt("dislikes")}", "踩") {
                model.perform { model.api.request("/api/wall/dislike/$id", "POST") }
            }
            ActionPill(
                Icons.Rounded.ChatBubbleOutline,
                "${post.optJSONArray("comments")?.length() ?: 0}",
                "评论",
            ) {
                go("message/$id")
            }
            Spacer(Modifier.weight(1f))
            GlassIconButton(
                Icons.Rounded.Share,
                "分享",
                {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, "https://wall.zongtech.xyz/wall/message/$id"),
                            "分享校园墙",
                        )
                    )
                },
                size = 40.dp,
            )
        }
    }
}

@Composable
private fun LostFoundInfo(info: JSONObject) {
    val colors = Glass.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassChip(
                when {
                    info.optBoolean("resolved") -> "已找回"
                    info.s("kind") == "found" -> "招领"
                    else -> "寻物"
                },
                selected = !info.optBoolean("resolved"),
                onClick = {},
            )
            Text(info.s("item"), style = MaterialTheme.typography.titleMedium)
        }
        listOf("地点" to "location", "时间" to "time", "联系方式" to "contact").forEach { (label, key) ->
            if (info.s(key).isNotBlank())
                Text("$label：${info.s(key)}", style = MaterialTheme.typography.bodyMedium, color = colors.secondary)
        }
    }
}

@Composable
private fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    description: String,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit,
) {
    GlassButton(
        onClick,
        Modifier.semantics { contentDescription = description },
        height = 40.dp,
        horizontalPadding = 14.dp,
    ) {
        Icon(icon, null, Modifier.size(18.dp), tint = if (tint == Color.Unspecified) LocalContentColor.current else tint)
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun Poll(poll: JSONObject, id: String, model: WallModel) {
    val colors = Glass.colors
    val revealed = poll.optBoolean("has_voted") || poll.optBoolean("is_closed")
    val options = poll.objects("options")
    val totalVotes = options.sumOf { it.optInt("votes") }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(poll.s("question"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        options.forEach { option ->
            val votes = option.optInt("votes")
            PollOption(
                text = option.s("text", option.s("label")),
                result = if (revealed) "$votes 票" else null,
                fraction = if (revealed) votes / totalVotes.toFloat() else 0f,
                enabled = !model.busy && !revealed,
            ) {
                model.perform {
                    model.api.request(
                        "/api/wall/poll/$id/vote",
                        "POST",
                        json = JSONObject().put("option_id", option.opt("id")),
                    )
                }
            }
        }
        if (poll.optBoolean("is_closed"))
            Text("投票已截止", style = MaterialTheme.typography.labelMedium, color = colors.secondary)
    }
}

@Composable
private fun PollOption(text: String, result: String?, fraction: Float, enabled: Boolean, onClick: () -> Unit) {
    val colors = Glass.colors
    Row(
        Modifier.fillMaxWidth()
            .drawBackdrop(
                backdrop = LocalBackdrop.current,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(2.dp.toPx())
                    lens(10.dp.toPx(), 20.dp.toPx())
                },
                shadow = null,
                onDrawSurface = {
                    drawRect(colors.panel)
                    if (fraction > 0f)
                        drawRect(
                            colors.accent.copy(alpha = 0.35f),
                            size = size.copy(width = size.width * fraction),
                        )
                },
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .height(44.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        result?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = colors.secondary) }
    }
}

private val imageTypes = listOf("jpg", "jpeg", "png", "gif", "webp", "avif")
private val mediaTypes = listOf("mp4", "webm", "mp3", "wav", "ogg", "m4a")

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun Attachment(file: String, api: WallApi? = null) {
    val url = "${BuildConfig.API_URL}/static/uploads/${file.pathSegment()}"
    val extension = file.substringAfterLast('.').lowercase()
    val image = extension in imageTypes
    val media = extension in mediaTypes
    var open by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var downloading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    if (image)
        AsyncImage(
            model = url,
            contentDescription = "帖子图片，点击放大",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .clip(RoundedRectangle(20.dp))
                    .clickable { open = true },
        )
    else
        GlassButton(
            onClick = {
                if (media) open = true
                else
                    scope.launch {
                        downloading = true
                        error = null
                        try {
                            val local =
                                withContext(Dispatchers.IO) {
                                    val dir = File(context.cacheDir, "attachments").apply { mkdirs() }
                                    val target =
                                        File(dir, file.substringAfterLast('/').substringAfterLast('\\'))
                                    val client = api?.client ?: okhttp3.OkHttpClient()
                                    client.newCall(Request.Builder().url(url).build()).execute().use { r ->
                                        check(r.isSuccessful) { "附件暂时无法读取" }
                                        r.body!!.byteStream().use { input ->
                                            target.outputStream().use { input.copyTo(it) }
                                        }
                                    }
                                    target
                                }
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", local)
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(
                                        uri,
                                        android.webkit.MimeTypeMap.getSingleton()
                                            .getMimeTypeFromExtension(extension)
                                            ?: "application/octet-stream",
                                    )
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            )
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            error =
                                if (e is android.content.ActivityNotFoundException) "手机上没有可打开此文件的应用"
                                else e.displayError()
                        } finally {
                            downloading = false
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !downloading,
        ) {
            Icon(if (media) Icons.Rounded.PlayArrow else Icons.Rounded.AttachFile, null, Modifier.size(20.dp))
            Text(
                if (downloading) "正在下载…" else if (media) "播放音视频" else "打开附件 · $file",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    error?.let { Text(it, color = Glass.colors.danger, style = MaterialTheme.typography.bodySmall) }
    if (open)
        GlassDialog(
            onDismissRequest = { open = false },
            scrollable = false,
            buttons = { GlassButton("关闭", { open = false }, Modifier.fillMaxWidth()) },
        ) {
            if (image) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                AsyncImage(
                    model = url,
                    contentDescription = "图片预览，双指缩放",
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(440.dp)
                            .clip(RoundedRectangle(20.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    offset = if (scale == 1f) Offset.Zero else offset + pan
                                }
                            }
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y,
                            ),
                )
            } else {
                val player =
                    remember(url) {
                        val cookies =
                            api?.cookies
                                ?.loadForRequest(url.toHttpUrl())
                                .orEmpty()
                                .joinToString("; ") { "${it.name}=${it.value}" }
                        val source =
                            androidx.media3.datasource.DefaultHttpDataSource.Factory()
                                .setDefaultRequestProperties(mapOf("Cookie" to cookies))
                        ExoPlayer.Builder(context)
                            .setMediaSourceFactory(
                                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(source)
                            )
                            .build()
                            .apply {
                                setMediaItem(MediaItem.fromUri(url))
                                prepare()
                            }
                    }
                DisposableEffect(player) { onDispose { player.release() } }
                AndroidView(
                    factory = {
                        (LayoutInflater.from(it).inflate(R.layout.player_view, android.widget.FrameLayout(it), false) as PlayerView).apply {
                            this.player = player
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedRectangle(20.dp)),
                )
            }
        }
}

/** 表白墙的粒子爱心：原生透视投影绘制，不使用网页渲染。 */
@Composable
fun ParticleHeart() {
    val context = LocalContext.current
    val love = Glass.colors.love
    val motion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    val points = remember {
        val random = Random(41)
        List(900) {
            val t = random.nextFloat() * 2 * PI
            val r = sqrt(random.nextFloat().toDouble())
            val x = 16 * sin(t).pow(3) * r
            val y = (13 * cos(t) - 5 * cos(2 * t) - 2 * cos(3 * t) - cos(4 * t)) * r
            val z = (random.nextFloat() - .5) * 12 * sqrt(1 - r * r)
            Triple(x, y, z)
        }
    }
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(motion) {
        if (motion) {
            val start = withFrameNanos { it }
            while (isActive) withFrameNanos { time = (it - start) / 1_000_000_000f }
        }
    }
    Canvas(Modifier.fillMaxWidth().height(180.dp)) {
        val angle = sin(time * .3) * .35
        val pulse = 1 + sin(time * 2.1) * .035
        points
            .sortedBy { it.third }
            .forEach { (x, y, z) ->
                val rotatedX = x * cos(angle) + z * sin(angle)
                val depth = -x * sin(angle) + z * cos(angle)
                val perspective = 65 / (65 - depth)
                val scale = size.height / 37 * pulse
                drawCircle(
                    love.copy(alpha = ((depth + 12) / 28).toFloat().coerceIn(.2f, .9f)),
                    radius = (1.0 + perspective).toFloat(),
                    center =
                        Offset(
                            (size.width / 2 + rotatedX * scale * perspective).toFloat(),
                            (size.height / 2 - y * scale * perspective).toFloat(),
                        ),
                )
            }
    }
}
