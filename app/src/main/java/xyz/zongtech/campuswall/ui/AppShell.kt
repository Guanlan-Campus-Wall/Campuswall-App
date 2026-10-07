package xyz.zongtech.campuswall.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Tag
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import kotlinx.coroutines.delay
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.glass.Glass
import xyz.zongtech.campuswall.glass.GlassButton
import xyz.zongtech.campuswall.glass.GlassButtonStyle
import xyz.zongtech.campuswall.glass.GlassIconButton
import xyz.zongtech.campuswall.glass.GlassLoading
import xyz.zongtech.campuswall.glass.GlassPanel
import xyz.zongtech.campuswall.glass.GlassTab
import xyz.zongtech.campuswall.glass.GlassTabs
import xyz.zongtech.campuswall.glass.GlassWallpaper
import xyz.zongtech.campuswall.glass.LocalBackdrop
import xyz.zongtech.campuswall.glass.LocalOverlayHost
import xyz.zongtech.campuswall.glass.OverlayHost
import xyz.zongtech.campuswall.glass.OverlayLayer
import xyz.zongtech.campuswall.pathSegment

private data class TabItem(val route: String, val title: String, val icon: ImageVector)

private val tabs =
    listOf(
        TabItem("feed", "校园", Icons.Rounded.Home),
        TabItem("topics", "话题", Icons.Rounded.Tag),
        TabItem("notifications", "消息", Icons.Rounded.Notifications),
        TabItem("me", "我的", Icons.Rounded.Person),
    )

/** 页面底部需要为悬浮标签栏和系统导航栏留出的空间。 */
val LocalBottomSpace = staticCompositionLocalOf { 0.dp }

@Composable
fun CampusWallApp(model: WallModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "feed"
    val tabRoute = tabs.any { it.route == route }
    val go: (String) -> Unit = { nav.navigate(it) }
    val back: () -> Unit = { nav.popBackStack() }
    val wallpaper = rememberLayerBackdrop()
    val chrome = rememberLayerBackdrop()
    val overlay = remember { OverlayHost() }
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(model.openNotifications) {
        if (model.openNotifications) {
            nav.navigate("notifications") { launchSingleTop = true }
            model.openNotifications = false
        }
    }
    LaunchedEffect(model.pendingMessage) {
        model.pendingMessage?.let {
            nav.navigate("message/$it")
            model.pendingMessage = null
        }
    }
    LaunchedEffect(model.user, model.revision) { model.refreshUnread() }

    Box(Modifier.fillMaxSize().background(Glass.colors.wallpaperBase.first())) {
        // 页面内容（含背景）记录为 chrome 背景，供底部标签栏和弹窗折射。
        Box(Modifier.layerBackdrop(chrome).fillMaxSize()) {
            GlassWallpaper(Modifier.layerBackdrop(wallpaper))
            CompositionLocalProvider(
                LocalBackdrop provides wallpaper,
                LocalOverlayHost provides overlay,
                LocalBottomSpace provides navInset + if (tabRoute) 96.dp else 16.dp,
            ) {
                AppNavHost(nav, model, go, back)
            }
        }

        CompositionLocalProvider(LocalBackdrop provides chrome, LocalOverlayHost provides overlay) {
            AnimatedVisibility(
                visible = tabRoute,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                BottomBar(
                    route = route,
                    unread = model.unread,
                    onTab = { path ->
                        nav.navigate(path) {
                            popUpTo("feed") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCompose = {
                        go(
                            if (
                                model.user != null ||
                                    model.community.optBoolean("guest_posting_enabled")
                            )
                                "compose"
                            else "login"
                        )
                    },
                )
            }
            Column(
                Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AnimatedVisibility(model.busy, enter = fadeIn(), exit = fadeOut()) {
                    GlassLoading(text = "处理中…")
                }
                Toast(model.error) { model.error = null }
            }
            OverlayLayer(overlay, chrome)
        }
    }
}

@Composable
private fun AppNavHost(
    nav: NavHostController,
    model: WallModel,
    go: (String) -> Unit,
    back: () -> Unit,
) {
    NavHost(
        navController = nav,
        startDestination = "feed",
        enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(260)) { it / 6 } },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(260)) { it / 6 } },
    ) {
        composable("feed") { FeedScreen(model, "观澜校园墙", go) }
        composable("topics") { TopicsScreen(model, go) }
        composable("tag/{tag}") {
            val tag = it.arguments?.getString("tag").orEmpty()
            FeedScreen(model, "# $tag", go, tag = tag, back = back)
        }
        composable("message/{id}") {
            DetailScreen(model, it.arguments?.getString("id").orEmpty(), go, back)
        }
        composable("compose") { ComposeScreen(model, back = back) }
        composable("compose/{tag}") {
            ComposeScreen(model, it.arguments?.getString("tag").orEmpty(), back)
        }
        composable("login") { LoginScreen(model, back) }
        composable("me") { MeScreen(model, go) }
        composable("notifications") { NotificationsScreen(model, go) }
        composable("saved") {
            FeedScreen(model, "我的收藏", go, path = "/api/user/me/favorites", back = back)
        }
        composable("posts") {
            FeedScreen(model, "我的发布", go, path = "/api/user/me/messages", back = back)
        }
        composable("comments") { MyCommentsScreen(model, go, back) }
        composable("lost") { LostFoundScreen(model, go, back) }
        composable("lost/new") { LostFoundComposeScreen(model, back) }
        composable("profile/{id}") {
            ProfileScreen(model, it.arguments?.getString("id").orEmpty(), go, back)
        }
        composable("settings") { AccountScreen(model, back) }
        composable("help") { HelpScreen(model, back) }
        composable("report/{id}") { ReportScreen(model, it.arguments?.getString("id").orEmpty(), back) }
        composable("admin") { AdminScreen(model, back) }
    }
}

@Composable
private fun BottomBar(
    route: String,
    unread: Int,
    onTab: (String) -> Unit,
    onCompose: () -> Unit,
) {
    val selected = tabs.indexOfFirst { it.route == route }.coerceAtLeast(0)
    Row(
        Modifier.navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 12.dp).widthIn(max = 560.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GlassTabs(
            selectedIndex = selected,
            onSelect = { onTab(tabs[it].route) },
            count = tabs.size,
            modifier = Modifier.weight(1f),
        ) {
            tabs.forEachIndexed { index, tab ->
                GlassTab(selected = index == selected, onClick = { onTab(tab.route) }) {
                    Box {
                        Icon(tab.icon, null, Modifier.size(24.dp))
                        if (tab.route == "notifications" && unread > 0)
                            Box(
                                Modifier.align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-4).dp)
                                    .clip(Capsule())
                                    .background(Glass.colors.danger)
                                    .padding(horizontal = 5.dp)
                            ) {
                                Text(
                                    if (unread > 99) "99+" else "$unread",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                )
                            }
                    }
                    Text(tab.title, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        GlassIconButton(
            Icons.Rounded.Edit,
            "写点什么",
            onCompose,
            style = GlassButtonStyle.Prominent,
            size = 64.dp,
        )
    }
}

@Composable
private fun Toast(message: String?, onDone: () -> Unit) {
    var last by remember { mutableStateOf("") }
    LaunchedEffect(message) {
        if (message != null) {
            last = message
            delay(2800)
            onDone()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
    ) {
        val colors = Glass.colors
        Row(
            Modifier.padding(horizontal = 24.dp)
                .widthIn(max = 520.dp)
                .drawBackdrop(
                    backdrop = LocalBackdrop.current,
                    shape = { Capsule() },
                    effects = {
                        vibrancy()
                        blur(12.dp.toPx())
                        lens(16.dp.toPx(), 32.dp.toPx())
                    },
                    onDrawSurface = { drawRect(colors.panelStrong) },
                )
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Rounded.Info, null, Modifier.size(20.dp), tint = colors.accent)
            Text(last, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** 页面顶部：返回按钮、标题与操作按钮都是玻璃元素。 */
@Composable
fun GlassTopBar(
    title: String,
    back: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        back?.let {
            GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "返回", it)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style =
                    if (back == null) MaterialTheme.typography.headlineMedium
                    else MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Glass.colors.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}

/** 空状态 / 错误提示。 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionText: String = "重试",
    action: (() -> Unit)? = null,
) {
    GlassPanel(
        modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(28.dp),
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(message, style = MaterialTheme.typography.bodyLarge, color = Glass.colors.secondary)
            action?.let { GlassButton(actionText, it) }
        }
    }
}

@Composable
fun Pager(page: Int, total: Int, onPage: (Int) -> Unit, enabled: Boolean = true, pageSize: Int = 20) {
    if (page == 1 && total <= pageSize) return
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassButton("上一页", { onPage(page - 1) }, enabled = enabled && page > 1)
        Text("第 $page 页", style = MaterialTheme.typography.labelLarge, color = Glass.colors.secondary)
        GlassButton("下一页", { onPage(page + 1) }, enabled = enabled && page * pageSize < total)
    }
}

fun tagRoute(tag: String) = "tag/${tag.pathSegment()}"
