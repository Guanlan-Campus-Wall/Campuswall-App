package xyz.zongtech.campuswall.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.delay
import xyz.zongtech.campuswall.R
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.glass.GlassCapsule
import xyz.zongtech.campuswall.glass.LocalBackdrop
import xyz.zongtech.campuswall.pathSegment
import xyz.zongtech.campuswall.s
import xyz.zongtech.campuswall.strings

/** 页面底部要为悬浮输入条和系统导航栏留出的空间。 */
val LocalBottomSpace = staticCompositionLocalOf { 24.dp }

private data class Section(val route: String, val label: String)

fun tagRoute(tag: String) = "tag/${tag.pathSegment()}"

@Composable
fun CampusWallApp(model: WallModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "wall"
    val go: (String) -> Unit = { nav.navigate(it) { launchSingleTop = true } }
    val back: () -> Unit = { if (!nav.popBackStack()) nav.navigate("wall") }
    val chrome = rememberLayerBackdrop()
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val admin = route.startsWith("admin")
    val canPost = model.community.optBoolean("posting_enabled", true) &&
        (model.user != null || model.community.optBoolean("guest_posting_enabled"))
    val dock: Pair<String, () -> Unit>? =
        when (route) {
            "wall" -> "分享今天的校园见闻…" to { go(if (canPost) "compose" else "login") }
            "confessions" -> "写一张便签…" to { model.dockTick++ }
            "lost" -> if (model.user != null) "发布寻物 / 招领启事…" to { model.dockTick++ } else null
            else -> null
        }

    LaunchedEffect(model.openNotifications) {
        if (model.openNotifications) {
            go("notifications")
            model.openNotifications = false
        }
    }
    LaunchedEffect(model.pendingMessage) {
        model.pendingMessage?.let {
            go("message/$it")
            model.pendingMessage = null
        }
    }
    LaunchedEffect(model.user, model.revision) { model.refreshUnread() }

    Box(Modifier.fillMaxSize().background(W.bg)) {
        // 只有出现悬浮输入条时才记录页面图层给液态玻璃折射，平时不额外绘制。
        Column(Modifier.fillMaxSize().then(if (dock != null) Modifier.layerBackdrop(chrome) else Modifier).background(W.bg)) {
            if (!admin) SiteHeader(model, route, go)
            CompositionLocalProvider(LocalBottomSpace provides navInset + if (dock != null) 96.dp else 28.dp) {
                Box(Modifier.weight(1f)) { Routes(nav, model, go, back) }
            }
        }
        CompositionLocalProvider(LocalBackdrop provides chrome) {
            AnimatedVisibility(
                visible = dock != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
            ) {
                var last by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
                if (dock != null) last = dock
                last?.let { (label, action) -> Dock(label, action) }
            }
        }
        Toast(model.error, Modifier.align(Alignment.TopCenter)) { model.error = null }
    }
}

@Composable
private fun Routes(nav: NavHostController, model: WallModel, go: (String) -> Unit, back: () -> Unit) {
    NavHost(
        nav,
        startDestination = "wall",
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(120)) },
    ) {
        composable("home") { HomeScreen(model, go) }
        composable("wall") { WallScreen(model, go) }
        composable("confessions") { ConfessionScreen(model, go) }
        composable("lost") { LostFoundScreen(model, go) }
        composable("topics") { TopicsScreen(model, go, back) }
        composable("tag/{tag}") { TopicScreen(model, it.arguments?.getString("tag").orEmpty(), go, back) }
        composable("help") { HelpScreen(model, go) }
        composable("help/form") { HelpFormScreen(model, back) }
        composable("rules") { RulesScreen(model, back) }
        composable("report/{id}") { ReportScreen(model, it.arguments?.getString("id").orEmpty(), back) }
        composable("message/{id}") { DetailScreen(model, it.arguments?.getString("id").orEmpty(), go, back) }
        composable("compose") { ComposeScreen(model, "", back) }
        composable("compose/{tag}") { ComposeScreen(model, it.arguments?.getString("tag").orEmpty(), back) }
        composable("login") { LoginScreen(model, go, back) }
        composable("me") { MeScreen(model, go) }
        composable("notifications") { NotificationsScreen(model, go, back) }
        composable("posts") { MyPostsScreen(model, go, back) }
        composable("comments") { MyCommentsScreen(model, go, back) }
        composable("saved") { SavedScreen(model, go, back) }
        composable("profile/{id}") { ProfileScreen(model, it.arguments?.getString("id").orEmpty(), go, back) }
        composable("admin") { AdminScreen(model, "dashboard", go, back) }
        composable("admin/{section}") { AdminScreen(model, it.arguments?.getString("section").orEmpty(), go, back) }
    }
}

/** 网页 `.site-header`：校徽 + 站名 + 主题 + 账号；下方是可横向滚动的版块导航。 */
@Composable
private fun SiteHeader(model: WallModel, route: String, go: (String) -> Unit) {
    val c = W
    val user = model.user
    var themeOpen by remember { mutableStateOf(false) }
    val sections =
        buildList {
            if (user == null) add(Section("home", "首页"))
            add(Section("wall", "动态"))
            add(Section("confessions", "表白墙"))
            add(Section("lost", "失物招领"))
            add(Section("topics", "话题"))
            add(Section("help", "帮助反馈"))
        }
    val active =
        when {
            route == "home" -> "home"
            route == "wall" || route.startsWith("message") || route.startsWith("compose") -> "wall"
            route == "confessions" -> "confessions"
            route == "lost" -> "lost"
            route == "topics" || route.startsWith("tag") -> "topics"
            route.startsWith("help") || route == "rules" || route.startsWith("report") -> "help"
            else -> ""
        }
    Column(Modifier.fillMaxWidth().background(c.bg).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1f).clip(RSm).clickable { go(if (user == null) "home" else "wall") }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painterResource(R.drawable.school_badge),
                    null,
                    Modifier.size(34.dp).clip(CircleShape).background(Color.White).border(1.dp, c.line, CircleShape),
                )
                Text("观澜校园墙", style = MaterialTheme.typography.headlineSmall.copy(fontSize = 19.sp, fontWeight = FontWeight.Normal), maxLines = 1)
            }
            Box {
                IconBtn(if (c.dark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode, "外观与主题", { themeOpen = true })
                Box(Modifier.align(Alignment.BottomEnd).offset((-8).dp, (-9).dp).size(8.dp).clip(CircleShape).background(c.accent))
            }
            HGap(6.dp)
            if (user != null)
                Box(
                    Modifier.clip(CircleShape).clickable(role = Role.Button) { go("me") }.semantics {
                        contentDescription = "个人中心" + if (model.unread > 0) "，${model.unread} 条未读通知" else ""
                    }
                ) {
                    Avatar(avatarUrl(user.s("id")), 34.dp)
                    if (model.unread > 0)
                        Box(
                            Modifier.align(Alignment.TopEnd).offset(4.dp, (-4).dp).clip(CircleShape).background(c.danger).padding(horizontal = 5.dp)
                        ) {
                            Text(if (model.unread > 99) "99+" else "${model.unread}", color = Color.White, fontSize = 10.sp, lineHeight = 15.sp)
                        }
                }
            else WButton("登录", { go("login") }, kind = Btn.Primary, small = true)
        }
        Row(
            Modifier.fillMaxWidth()
                .drawBehind {
                    drawLine(c.line, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                    drawLine(c.line, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                }
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
        ) {
            sections.forEach { s ->
                val on = s.route == active
                Box(
                    Modifier.clickable(role = Role.Tab) {
                        if (!on) go(s.route)
                    }
                        .padding(horizontal = 12.dp)
                        .height(44.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(s.label, color = if (on) c.ink else c.ink3, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    if (on)
                        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp).background(c.accent, RoundedTop))
                }
            }
        }
    }
    if (themeOpen) ThemeSheet(model) { themeOpen = false }
}

private val RoundedTop = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)

@Composable
fun ThemeSheet(model: WallModel, close: () -> Unit) {
    Sheet("外观与主题", close) {
        Text("只保存在当前设备", style = MaterialTheme.typography.bodySmall, color = W.ink3)
        Text("明暗模式", style = MaterialTheme.typography.labelMedium, color = W.ink2)
        Seg(listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色"), model.themeMode, { model.setAppearance(theme = it) }, Modifier.fillMaxWidth(), fill = true)
        Text("强调色", style = MaterialTheme.typography.labelMedium, color = W.ink2)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            palettes.forEach { p ->
                val on = p.id == model.palette
                Column(
                    Modifier.clip(RMd).clickable { model.setAppearance(palette = p.id) }.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(p.swatch)
                            .border(if (on) 3.dp else 1.dp, if (on) W.ink else W.line, CircleShape)
                    )
                    Text(p.label, style = MaterialTheme.typography.labelSmall, color = if (on) W.ink else W.ink3)
                }
            }
        }
    }
}

/** 网页 `.dock`：悬浮在底部的输入条，这里用液态玻璃呈现。 */
@Composable
private fun Dock(label: String, action: () -> Unit) {
    val c = W
    GlassCapsule(
        action,
        Modifier.navigationBarsPadding().padding(start = 14.dp, end = 14.dp, bottom = 12.dp).widthIn(max = 640.dp).fillMaxWidth(),
        contentDescription = label,
    ) {
        Icon(Icons.Rounded.WbSunny, null, Modifier.padding(start = 18.dp).size(20.dp), tint = c.accent)
        Text(label, Modifier.weight(1f), color = c.ink3, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Box(Modifier.padding(end = 8.dp).size(40.dp).clip(CircleShape).background(c.accent), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.ArrowUpward, null, Modifier.size(20.dp), tint = c.accentInk)
        }
    }
}

@Composable
private fun Toast(message: String?, modifier: Modifier, onDone: () -> Unit) {
    var last by remember { mutableStateOf("") }
    LaunchedEffect(message) {
        if (message != null) {
            last = message
            delay(2800)
            onDone()
        }
    }
    AnimatedVisibility(message != null, modifier.statusBarsPadding().padding(top = 10.dp), enter = fadeIn() + slideInVertically { -it }, exit = fadeOut()) {
        Text(
            last,
            Modifier.padding(horizontal = 24.dp).widthIn(max = 520.dp).clip(RMd).background(W.toastBg).padding(horizontal = 18.dp, vertical = 12.dp),
            color = W.toastFg,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/** 页面统一的纵向留白。 */
val PageGutter = 20.dp

fun capabilities(model: WallModel) = model.user?.strings("capabilities").orEmpty()
