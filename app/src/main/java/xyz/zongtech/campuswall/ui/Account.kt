package xyz.zongtech.campuswall.ui

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.UUID
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import xyz.zongtech.campuswall.NotificationWorker
import xyz.zongtech.campuswall.R
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.displayError
import xyz.zongtech.campuswall.displayState
import xyz.zongtech.campuswall.objects
import xyz.zongtech.campuswall.pathSegment
import xyz.zongtech.campuswall.s
import xyz.zongtech.campuswall.strings

@Composable
fun LoginScreen(model: WallModel, go: (String) -> Unit, back: () -> Unit) {
    val c = W
    var register by rememberSaveable { mutableStateOf(false) }
    var admin by rememberSaveable { mutableStateOf(false) }
    var account by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var emailNotify by rememberSaveable { mutableStateOf(true) }
    var captcha by remember { mutableStateOf(JSONObject()) }
    var result by remember { mutableStateOf("") }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            captcha = model.api.request("/api/user/captcha/config").optJSONObject("captcha") ?: JSONObject()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }
    LaunchedEffect(model.user) { if (model.user != null && !admin) back() }
    val action = if (register) "register" else if (admin) "admin_login" else "login"
    val captchaRequired = captcha.optBoolean("enabled") && (captcha.optJSONObject("protected_actions")?.optBoolean(action, true) ?: true)
    LaunchedEffect(action) { model.captchaToken = "" }
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(PageGutter),
    ) {
        Column(Modifier.fillMaxWidth().clip(RXl).border(1.dp, c.line, RXl)) {
            Column(Modifier.fillMaxWidth().background(c.surface3).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextLink("返回", Icons.AutoMirrored.Rounded.ArrowBack) { back() }
                Image(painterResource(R.drawable.school_badge), null, Modifier.size(64.dp).clip(CircleShape).background(Color.White).border(1.dp, c.line, CircleShape))
                Text("观澜校园墙", style = MaterialTheme.typography.displaySmall)
                Text(if (admin) "后台人员使用管理账号登录。" else "使用本校学号注册和登录，参与校园讨论。", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
            }
            Column(Modifier.fillMaxWidth().background(c.surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (admin) "管理员登录" else if (register) "注册账号" else "学号登录", style = MaterialTheme.typography.displaySmall)
                Text(if (admin) "运营、审核与管理人员入口。" else "学生使用学号登录。后台人员请走管理员入口。", style = MaterialTheme.typography.bodySmall, color = c.ink2)
                if (!admin) Seg(listOf("login" to "登录", "register" to "注册"), if (register) "register" else "login", { register = it == "register" }, Modifier.fillMaxWidth(), fill = true)
                Field(account, { account = it }, label = if (admin) "管理员账号" else "学号或用户名", placeholder = if (admin) "请输入管理员账号" else "请输入学号")
                Field(password, { password = it }, label = "登录密码", placeholder = "请输入密码", secret = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                if (register) {
                    Field(nickname, { nickname = it }, label = "昵称", placeholder = "公开页面展示的昵称", maxLength = 40)
                    Field(email, { email = it }, label = "邮箱（选填）", placeholder = "用于接收评论等消息", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                    SwitchRow("接收邮件通知", emailNotify, { emailNotify = it })
                }
                if (captchaRequired) {
                    Text("人机验证", style = MaterialTheme.typography.labelMedium, color = c.ink2)
                    WButton(
                        if (model.captchaToken.isEmpty()) "在浏览器中完成验证" else "验证已完成",
                        {
                            model.captchaState = UUID.randomUUID().toString()
                            try {
                                CustomTabsIntent.Builder().build().launchUrl(
                                    context,
                                    Uri.parse("https://wall.zongtech.xyz/native-captcha.html?action=$action&state=${model.captchaState}"),
                                )
                            } catch (_: android.content.ActivityNotFoundException) {
                                model.error = "请先安装浏览器，再完成人机验证。"
                            }
                        },
                        Modifier.fillMaxWidth(),
                        icon = if (model.captchaToken.isEmpty()) Icons.Rounded.Shield else Icons.Rounded.CheckCircle,
                        kind = if (model.captchaToken.isEmpty()) Btn.Outline else Btn.Soft,
                    )
                }
                WButton(
                    if (model.busy) "请稍候…" else if (register) "注册" else "登录",
                    {
                        model.perform {
                            val token = model.captchaToken
                            model.captchaToken = ""
                            val r =
                                model.api.request(
                                    if (admin) "/api/admin/login" else "/api/user/$action",
                                    "POST",
                                    fields =
                                        mapOf(
                                            "student_id" to account,
                                            "username" to account,
                                            "password" to password,
                                            "nickname" to nickname,
                                            "email" to email,
                                            "email_notify" to emailNotify.toString(),
                                            "captcha_token" to token,
                                        ),
                                )
                            password = ""
                            if (register) {
                                result = r.s("message", "注册已提交，等待审核")
                                register = false
                            } else {
                                model.refreshSession()
                                if (admin) go("admin") else back()
                            }
                        }
                    },
                    Modifier.fillMaxWidth(),
                    kind = Btn.Primary,
                    large = true,
                    enabled = !model.busy && account.isNotBlank() && password.isNotBlank() && (!captchaRequired || model.captchaToken.isNotEmpty()),
                )
                if (result.isNotEmpty()) Callout(result, Tone.Success, Icons.Rounded.CheckCircle)
                Hairline()
                WButton(
                    if (admin) "返回学生登录" else "管理员入口",
                    {
                        admin = !admin
                        register = false
                    },
                    Modifier.fillMaxWidth(),
                    kind = Btn.Ghost,
                    icon = Icons.Rounded.AdminPanelSettings,
                )
            }
        }
    }
}

private fun genderText(g: String) = when (g) {
    "1" -> "男"
    "2" -> "女"
    else -> "未设置性别"
}

@Composable
fun MeScreen(model: WallModel, go: (String) -> Unit) {
    val c = W
    val user = model.user
    if (user == null) {
        Column(Modifier.fillMaxSize().padding(PageGutter)) {
            EmptyCard("登录后查看个人中心", "收藏、评论、通知和资料设置都在这里。", Icons.Rounded.Person, "去登录", { go("login") })
        }
        return
    }
    val context = LocalContext.current
    var nickname by rememberSaveable { mutableStateOf(user.s("nickname")) }
    var bio by rememberSaveable { mutableStateOf(user.s("bio")) }
    var gender by rememberSaveable { mutableStateOf(user.s("gender", "0").ifBlank { "0" }) }
    var emailDraft by rememberSaveable { mutableStateOf(user.s("email_pending").ifBlank { user.s("email") }) }
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var themeOpen by remember { mutableStateOf(false) }
    var logout by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(context.getSharedPreferences("preferences", 0).getBoolean("notifications", false)) }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                notifications = true
                NotificationWorker.enable(context)
            } else model.error = "未获得通知权限，可在系统设置中开启"
        }
    val avatar =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null)
                model.perform {
                    model.api.upload(uri, true)
                    model.refreshSession()
                    model.error = "头像已更新"
                }
        }
    val caps = user.strings("capabilities")
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(start = PageGutter, end = PageGutter, top = 20.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().clip(RLg).background(c.surface).border(1.dp, c.line, RLg)) {
            Box(Modifier.fillMaxWidth().height(126.dp)) {
                Box(Modifier.fillMaxWidth().height(84.dp).background(Brush.linearGradient(listOf(c.accentSoft2, c.surface3))))
                Box(Modifier.padding(start = 20.dp, top = 44.dp).size(82.dp).clip(CircleShape).background(c.surface), contentAlignment = Alignment.Center) {
                    Avatar(avatarUrl(user.s("id")), 76.dp)
                }
            }
            Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(user.s("nickname").ifBlank { "未设置昵称" }, style = MaterialTheme.typography.headlineMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Meta(Icons.Rounded.Badge, if (user.s("student_id").isNotBlank()) "学号 ${user.s("student_id")}" else "@${user.s("username")}")
                    Meta(Icons.Rounded.Person, genderText(user.s("gender")))
                    Row(Modifier.clickable { go("profile/${user.s("id")}") }, verticalAlignment = Alignment.CenterVertically) {
                        Text("公开主页 ", color = c.accentStrong, style = MaterialTheme.typography.bodySmall)
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(14.dp), tint = c.accentStrong)
                    }
                }
                if (user.s("bio").isNotBlank()) Text(user.s("bio"), style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (caps.isNotEmpty())
                        WButton(if (user.s("role") == "reviewer") "运营后台" else "管理后台", { go("admin") }, kind = Btn.Soft, small = true, icon = Icons.Rounded.Shield)
                    WButton("退出登录", { logout = true }, small = true, icon = Icons.AutoMirrored.Rounded.Logout)
                }
            }
        }
        listOf(
            Quad("posts", Icons.Rounded.Article, "我的发布", "帖子与便签", 1),
            Quad("comments", Icons.Rounded.ChatBubbleOutline, "我的评论", "评论与回复", 2),
            Quad("saved", Icons.Rounded.Bookmark, "我的收藏", "留住喜欢的内容", 4),
            Quad("notifications", Icons.Rounded.Notifications, "消息通知", if (model.unread > 0) "${model.unread} 条未读" else "暂无未读", 3),
        ).chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { q ->
                    val (fg, bg) = c.tones[q.tone]
                    Card(Modifier.weight(1f), padding = PaddingValues(16.dp), onClick = { go(q.route) }, spacing = 10.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TileIcon(q.icon, fg, bg, 40.dp)
                            Box(Modifier.weight(1f))
                            if (q.route == "notifications" && model.unread > 0) Badge(if (model.unread > 99) "99+" else "${model.unread}", Tone.Danger)
                        }
                        Text(q.title, style = MaterialTheme.typography.titleSmall)
                        Text(q.hint, style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    }
                }
            }
        }
        if (user.optBoolean("is_muted"))
            Callout(
                "账号当前处于禁言状态 · 到期时间：${user.s("muted_until").ifBlank { "未设置" }}" + user.s("mute_reason").let { if (it.isBlank()) "" else " · 原因：$it" },
                Tone.Warning,
                Icons.Rounded.Warning,
            )
        FormCard("个人资料", "公开页面和发帖时展示的信息。") {
            Field(nickname, { nickname = it }, label = "展示昵称", placeholder = "公开页面展示的昵称", maxLength = 40)
            Field(bio, { bio = it }, label = "个人简介", placeholder = "介绍一下自己，公开主页会展示这段内容", singleLine = false, minLines = 3, maxLength = 200)
            Text("性别", style = MaterialTheme.typography.labelMedium, color = c.ink2)
            Seg(listOf("0" to "保密", "1" to "男", "2" to "女"), gender, { gender = it }, Modifier.fillMaxWidth(), fill = true)
            Text("用户名是你的登录标识，暂不支持自行修改。发帖时可选择匿名或使用上述昵称。", style = MaterialTheme.typography.bodySmall, color = c.ink3)
            WButton(
                "保存资料",
                {
                    model.perform {
                        model.api.request("/api/user/me/profile", "PUT", fields = mapOf("nickname" to nickname, "bio" to bio, "gender" to gender))
                        model.refreshSession()
                        model.error = "资料已保存"
                    }
                },
                kind = Btn.Primary,
                icon = Icons.Rounded.CheckCircle,
                enabled = !model.busy,
            )
        }
        FormCard("头像", "自动居中裁剪为正方形并压缩，最大 5 MB。") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Avatar(avatarUrl(user.s("id")), 56.dp)
                WButton("选择新头像", { avatar.launch("image/*") }, kind = Btn.Primary, icon = Icons.Rounded.CloudUpload, enabled = !model.busy)
            }
        }
        FormCard("邮箱通知", "验证通过后才会发送评论等通知，链接 24 小时内有效。") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (user.optBoolean("email_verified")) Badge("已验证", Tone.Success, Icons.Rounded.CheckCircle) else Badge("未验证", Tone.Warning)
                if (user.s("email_pending").isNotBlank()) Badge("待验证：${user.s("email_pending")}")
            }
            Field(emailDraft, { emailDraft = it }, label = if (user.optBoolean("email_verified")) "已验证邮箱" else "添加邮箱", placeholder = "用于接收评论等消息", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            SwitchRow(
                if (user.optBoolean("email_verified")) "接收邮件通知" else "验证完成前不能打开邮件通知",
                user.optBoolean("email_notify"),
                { v ->
                    model.perform {
                        model.api.request("/api/user/me/email/notify", "POST", fields = mapOf("email_notify" to "$v"))
                        model.refreshSession()
                    }
                },
                enabled = user.optBoolean("email_verified"),
            )
            WButton(
                if (user.optBoolean("email_verified")) "更换并重新验证" else "发送验证邮件",
                {
                    model.perform {
                        model.api.request("/api/user/me/email", "POST", fields = mapOf("email" to emailDraft.trim()))
                        model.refreshSession()
                        model.error = "验证邮件已发送，请前往邮箱完成验证"
                    }
                },
                kind = Btn.Primary,
                icon = Icons.Rounded.Mail,
                enabled = emailDraft.isNotBlank() && !model.busy,
            )
        }
        FormCard("手机通知与外观", "只对这台设备生效。") {
            SwitchRow(
                "系统通知",
                notifications,
                { v ->
                    if (v) {
                        if (Build.VERSION.SDK_INT >= 33) permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        else {
                            notifications = true
                            NotificationWorker.enable(context)
                        }
                    } else {
                        notifications = false
                        NotificationWorker.disable(context)
                    }
                },
                supporting = "后台约每 15 分钟检查新消息，受系统省电策略影响。",
            )
            ListRow("外观与主题", { themeOpen = true }, Icons.Rounded.Palette, palettes.firstOrNull { it.id == model.palette }?.label + " · " + when (model.themeMode) {
                "light" -> "浅色"
                "dark" -> "深色"
                else -> "跟随系统"
            }, trailing = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.ink3) })
        }
        if (user.optBoolean("has_password", true))
            FormCard("修改登录密码", "修改后，其他设备上的旧登录状态会自动失效。") {
                Field(current, { current = it }, label = "当前密码", placeholder = "请输入当前密码", secret = true)
                Field(next, { next = it }, label = "新密码", placeholder = "至少 8 个字符", secret = true)
                Field(confirm, { confirm = it }, label = "确认新密码", placeholder = "再次输入新密码", secret = true)
                WButton(
                    "确认修改密码",
                    {
                        model.perform {
                            require(next.length >= 8) { "新密码至少 8 个字符" }
                            require(next == confirm) { "两次输入的新密码不一致" }
                            model.api.request("/api/user/me/password", "POST", fields = mapOf("current_password" to current, "new_password" to next))
                            current = ""
                            next = ""
                            confirm = ""
                            model.refreshSession()
                            model.error = "密码已修改"
                        }
                    },
                    kind = Btn.Primary,
                    icon = Icons.Rounded.Shield,
                    enabled = current.isNotBlank() && next.isNotBlank() && !model.busy,
                )
            }
    }
    if (themeOpen) ThemeSheet(model) { themeOpen = false }
    if (logout)
        Confirm("退出登录？", "退出后将停止这台设备的后台消息提醒。", "退出登录", { logout = false }, {
            logout = false
            model.perform {
                try {
                    model.api.request("/api/user/logout", "POST")
                } finally {
                    model.api.cookies.clear()
                    model.user = null
                    model.ownedPosts.clear()
                    model.favoriteIds.clear()
                    NotificationWorker.disable(model.api.context)
                }
            }
        })
}

private data class Quad(val route: String, val icon: ImageVector, val title: String, val hint: String, val tone: Int)

@Composable
private fun Meta(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, null, Modifier.size(15.dp), tint = W.ink3)
        Text(text, style = MaterialTheme.typography.bodySmall, color = W.ink2)
    }
}

@Composable
fun FormCard(title: String, subtitle: String? = null, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), padding = PaddingValues(20.dp), spacing = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = W.ink3) }
        }
        content()
    }
}

private fun notificationMeta(n: JSONObject): List<Any> =
    when (n.s("type")) {
        "reply" -> listOf(Icons.AutoMirrored.Rounded.Reply, 1, "有人回复了你的评论", "查看回复", if (n.optLong("message_id") > 0) "message/${n.s("message_id")}" else "posts")
        "featured" -> listOf(Icons.Rounded.Star, 4, "你的留言被设为精华", "查看留言", if (n.optLong("message_id") > 0) "message/${n.s("message_id")}" else "posts")
        "moderation" -> listOf(Icons.Rounded.Shield, 3, "你的留言状态有更新", "查看我的发布", "posts")
        "comment_moderation" -> listOf(Icons.Rounded.Shield, 3, "你的评论状态有更新", "查看我的评论", "comments")
        else -> listOf(Icons.Rounded.Sms, 2, "有人评论了你的留言", "查看留言", if (n.optLong("message_id") > 0) "message/${n.s("message_id")}" else "posts")
    }

@Composable
fun NotificationsScreen(model: WallModel, go: (String) -> Unit, back: () -> Unit) {
    val c = W
    var rows by remember { mutableStateOf(emptyList<JSONObject>()) }
    var page by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var clearOpen by remember { mutableStateOf(false) }
    LaunchedEffect(page, model.revision, model.user) {
        if (model.user == null) return@LaunchedEffect
        loading = true
        try {
            val r = model.api.request("/api/user/me/notifications?page=$page&page_size=20")
            val incoming = r.objects("notifications")
            rows = if (page == 1) incoming else rows + incoming.filter { n -> rows.none { it.s("id") == n.s("id") } }
            hasMore = page * 20 < r.optInt("total")
            model.unread = r.optInt("unread", model.unread)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        } finally {
            loading = false
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PageHead("消息通知", "评论、回复与内容状态的提醒都在这里。", back = "个人中心" to back)
        }
        if (model.user == null) {
            item { EmptyCard("登录后查看消息", icon = Icons.Rounded.Notifications, actionText = "去登录", action = { go("login") }) }
            return@LazyColumn
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (model.unread > 0)
                    WButton("全部已读（${model.unread}）", { model.perform { model.api.request("/api/user/me/notifications/read-all", "POST") } }, kind = Btn.Primary, small = true, icon = Icons.Rounded.DoneAll)
                if (rows.isNotEmpty()) WButton("清空", { clearOpen = true }, small = true, icon = Icons.Rounded.DeleteSweep)
            }
        }
        if (loading && rows.isEmpty()) item { Spinner() }
        if (!loading && rows.isEmpty()) item { EmptyCard("暂时没有新消息", "有人评论或回复你时，会在这里提醒你。", Icons.Rounded.Notifications) }
        if (rows.isNotEmpty())
            item {
                Card(Modifier.fillMaxWidth(), padding = PaddingValues(0.dp), spacing = 0.dp) {
                    rows.forEachIndexed { i, n ->
                        val meta = notificationMeta(n)
                        val unread = !n.optBoolean("is_read")
                        val (fg, bg) = c.tones[meta[1] as Int]
                        Row(
                            Modifier.fillMaxWidth()
                                .background(if (unread) c.accentSoft.copy(alpha = 0.45f) else Color.Transparent)
                                .clickable {
                                    model.perform({ go(meta[4] as String) }) {
                                        if (unread) model.api.request("/api/user/me/notifications/${n.s("id")}/read", "POST")
                                    }
                                }
                                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            TileIcon(meta[0] as ImageVector, fg, bg, 40.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(meta[2] as String, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(relativeTime(n.s("created_at", n.s("timestamp"))), style = MaterialTheme.typography.labelSmall, color = c.ink3)
                                }
                                Text(n.s("content").ifBlank { "查看最新回复" }, style = MaterialTheme.typography.bodySmall, color = c.ink2, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                Text("${meta[3]} →", style = MaterialTheme.typography.labelMedium, color = c.accentStrong)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (unread) Box(Modifier.padding(top = 4.dp).size(8.dp).clip(CircleShape).background(c.accent))
                                IconBtn(Icons.Rounded.Delete, "删除通知", { model.perform { model.api.request("/api/user/me/notifications/${n.s("id")}", "DELETE") } }, size = 36.dp)
                            }
                        }
                        if (i < rows.lastIndex) Hairline()
                    }
                }
            }
        if (hasMore)
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    WButton(if (loading) "加载中…" else "加载更多通知", { page++ }, enabled = !loading)
                }
            }
    }
    if (clearOpen)
        Confirm("清空所有通知？", "清空后无法恢复，所有已读和未读通知都会被删除。", "确认清空", { clearOpen = false }, {
            model.perform {
                model.api.request("/api/user/me/notifications", "DELETE")
                clearOpen = false
                page = 1
            }
        })
}

@Composable
private fun AccountList(
    model: WallModel,
    title: String,
    subtitle: String,
    path: String,
    go: (String) -> Unit,
    back: () -> Unit,
    empty: String,
    owned: Boolean = false,
) {
    var editing by remember { mutableStateOf<JSONObject?>(null) }
    var deleting by remember { mutableStateOf<JSONObject?>(null) }
    var page by remember { mutableIntStateOf(1) }
    var rows by remember { mutableStateOf(emptyList<JSONObject>()) }
    var totalPages by remember { mutableIntStateOf(1) }
    var total by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(page, model.revision) {
        loading = true
        try {
            val r = model.api.request("$path${if ('?' in path) '&' else '?'}page=$page&page_size=20")
            rows = r.objects("messages").ifEmpty { r.objects("data") }
            total = r.optInt("total", rows.size)
            totalPages = r.optInt("total_pages", (total + 19) / 20).coerceAtLeast(1)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        } finally {
            loading = false
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
    ) {
        item { PageHead(title, subtitle, back = "个人中心" to back) { Badge("共 $total 条") } }
        if (owned && !model.community.optBoolean("posting_enabled", true))
            item { Callout(model.community.s("pause_reason").ifBlank { "管理员暂时关闭了发帖与留言编辑功能" }, Tone.Warning) }
        if (loading && rows.isEmpty()) item { Spinner() }
        if (!loading && rows.isEmpty()) item { Gap(12.dp); EmptyCard(empty, icon = Icons.Rounded.Article, actionText = "去校园动态看看", action = { go("wall") }) }
        items(rows, key = { it.s("id") }) { m ->
            PostItem(m, model, go, onEdit = if (owned) ({ editing = it }) else null, onDelete = if (owned) ({ deleting = it }) else null)
        }
        item { Pager(page, totalPages, { page = it }) }
    }
    editing?.let { EditPostSheet(model, it) { editing = null } }
    deleting?.let { m ->
        Confirm("删除这条动态？", "动态将从校园墙移除。", "删除", { deleting = null }, {
            model.perform {
                model.api.request("/api/user/me/messages/${m.s("id")}", "DELETE")
                deleting = null
            }
        })
    }
}

@Composable
fun MyPostsScreen(model: WallModel, go: (String) -> Unit, back: () -> Unit) =
    AccountList(model, "我的发布", "这里会显示当前账号发布的全部内容，包括公开页面无法追溯身份的匿名留言。", "/api/user/me/messages", go, back, "还没有发布过内容", owned = true)

@Composable
fun SavedScreen(model: WallModel, go: (String) -> Unit, back: () -> Unit) =
    AccountList(model, "我的收藏", "收藏的留言会保存在这里，方便以后回看。", "/api/user/me/favorites", go, back, "还没有收藏内容")

@Composable
fun MyCommentsScreen(model: WallModel, go: (String) -> Unit, back: () -> Unit) {
    val c = W
    var rows by remember { mutableStateOf(emptyList<JSONObject>()) }
    var page by remember { mutableIntStateOf(1) }
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
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
    ) {
        item { PageHead("我的评论", "汇总你在不同留言下发表的评论与回复，匿名身份不会因此在公开页面暴露。", back = "个人中心" to back) { Badge("$total 条") } }
        if (loading && rows.isEmpty()) item { Spinner() }
        if (!loading && rows.isEmpty()) item { Gap(12.dp); EmptyCard("还没有发表过评论", icon = Icons.Rounded.ChatBubbleOutline, actionText = "去校园动态看看", action = { go("wall") }) }
        items(rows, key = { "${it.s("message_id")}/${it.s("id")}" }) { cm ->
            Column(
                Modifier.fillMaxWidth().clickable { go("message/${cm.s("message_id")}") }.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(relativeTime(cm.s("timestamp")), style = MaterialTheme.typography.labelSmall, color = c.ink3)
                    Badge(if (cm.s("refer_id").isNotBlank()) "回复" else "评论")
                    if (cm.optBoolean("comment_hidden")) Badge("已下架", Tone.Danger)
                    if (cm.s("moderation_status").isNotBlank() && cm.s("moderation_status") != "visible") Badge(displayState(cm.s("moderation_status")), Tone.Warning)
                    Box(Modifier.weight(1f))
                    IconBtn(Icons.Rounded.Delete, "删除这条评论", { deleting = cm }, size = 34.dp)
                }
                if (cm.s("refer").isNotBlank())
                    Text("回复：${cm.s("refer")}", Modifier.clip(RSm).background(c.surface3).padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = c.ink2, maxLines = 2)
                if (cm.s("text").isNotBlank()) Text(cm.s("text"), style = MaterialTheme.typography.bodyMedium)
                Text("所在留言 #${cm.s("message_id")}" + cm.s("message_text").let { if (it.isBlank()) "" else "：$it" }, style = MaterialTheme.typography.bodySmall, color = c.ink3, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Hairline()
        }
        item { Pager(page, (total + 19) / 20, { page = it }) }
    }
    deleting?.let { cm ->
        Confirm("删除我的评论", "删除后评论会立即从公开页面和你的评论列表中移除。", "确认删除", { deleting = null }, {
            model.perform {
                model.api.request("/api/user/me/comments/${cm.s("message_id")}/${cm.s("id").pathSegment()}", "DELETE")
                deleting = null
            }
        })
    }
}

@Composable
fun ProfileScreen(model: WallModel, id: String, go: (String) -> Unit, back: () -> Unit) {
    val c = W
    var user by remember { mutableStateOf(JSONObject()) }
    LaunchedEffect(id) {
        try {
            user = model.api.request("/api/user/$id/profile").optJSONObject("user") ?: JSONObject()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        }
    }
    val feed =
        rememberFeed(model, id) { start, end ->
            if (start > 0) emptyList()
            else model.api.request("/api/user/$id/messages").let { r -> r.objects("messages").ifEmpty { r.objects("data") } }
        }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PageGutter, end = PageGutter, top = 16.dp, bottom = LocalBottomSpace.current),
    ) {
        item {
            TextLink("返回", Icons.AutoMirrored.Rounded.ArrowBack) { back() }
            Gap(10.dp)
            Card(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Avatar(avatarUrl(id), 64.dp)
                    Column(Modifier.weight(1f)) {
                        Text(user.s("nickname", "同学").ifBlank { "同学" }, style = MaterialTheme.typography.headlineMedium)
                        Text(genderText(user.s("gender")), style = MaterialTheme.typography.bodySmall, color = c.ink3)
                    }
                }
                Text(user.s("bio").ifBlank { "这位同学还没有填写简介" }, style = MaterialTheme.typography.bodyMedium, color = c.ink2)
            }
            Gap(8.dp)
            SectionHead("公开动态", Modifier.padding(top = 8.dp))
        }
        feedItems(feed, model, go, "这位同学还没有公开展示昵称的动态。")
    }
}
