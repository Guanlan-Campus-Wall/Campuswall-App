package xyz.zongtech.campuswall.ui

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedRectangle
import java.util.UUID
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import xyz.zongtech.campuswall.NotificationWorker
import xyz.zongtech.campuswall.WallModel
import xyz.zongtech.campuswall.displayError
import xyz.zongtech.campuswall.displayState
import xyz.zongtech.campuswall.glass.Glass
import xyz.zongtech.campuswall.glass.GlassButton
import xyz.zongtech.campuswall.glass.GlassButtonStyle
import xyz.zongtech.campuswall.glass.GlassConfirmDialog
import xyz.zongtech.campuswall.glass.GlassPanel
import xyz.zongtech.campuswall.glass.GlassSegmented
import xyz.zongtech.campuswall.glass.GlassSwitchRow
import xyz.zongtech.campuswall.glass.GlassTextField
import xyz.zongtech.campuswall.glass.SectionTitle
import xyz.zongtech.campuswall.s
import xyz.zongtech.campuswall.strings

@Composable
fun LoginScreen(model: WallModel, back: () -> Unit) {
    var register by rememberSaveable { mutableStateOf(false) }
    var admin by rememberSaveable { mutableStateOf(false) }
    var account by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var captcha by remember { mutableStateOf(JSONObject()) }
    var result by remember { mutableStateOf("") }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            captcha = model.api.request("/api/user/captcha/config").optJSONObject("captcha") ?: JSONObject()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            model.error = e.displayError()
        }
    }
    val action = if (register) "register" else if (admin) "admin_login" else "login"
    val captchaRequired =
        captcha.optBoolean("enabled") &&
            (captcha.optJSONObject("protected_actions")?.optBoolean(action, true) ?: true)
    LaunchedEffect(action) { model.captchaToken = "" }
    Column(Modifier.fillMaxSize().imePadding()) {
        GlassTopBar("", back)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                Text(
                    if (register) "加入校园墙" else if (admin) "管理账号登录" else "欢迎回来",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text("观澜中学 · 与网页共用账号，校园日常随身看", color = Glass.colors.secondary)
            }
            if (!admin)
                GlassSegmented(
                    listOf("login" to "登录", "register" to "注册"),
                    if (register) "register" else "login",
                    { register = it == "register" },
                )
            GlassPanel(Modifier.fillMaxWidth(), strong = true) {
                GlassTextField(
                    account,
                    { account = it },
                    Modifier.fillMaxWidth(),
                    label = if (admin) "管理账号" else "学号",
                    singleLine = true,
                )
                GlassTextField(
                    password,
                    { password = it },
                    Modifier.fillMaxWidth(),
                    label = "密码",
                    singleLine = true,
                    secret = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                if (register) {
                    GlassTextField(nickname, { nickname = it }, Modifier.fillMaxWidth(), label = "昵称", singleLine = true)
                    GlassTextField(
                        email,
                        { email = it },
                        Modifier.fillMaxWidth(),
                        label = "邮箱",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                }
                if (captchaRequired)
                    GlassButton(
                        text = if (model.captchaToken.isEmpty()) "完成人机验证" else "验证已完成",
                        icon = Icons.Rounded.VerifiedUser,
                        onClick = {
                            model.captchaState = UUID.randomUUID().toString()
                            try {
                                CustomTabsIntent.Builder()
                                    .build()
                                    .launchUrl(
                                        context,
                                        Uri.parse(
                                            "https://wall.zongtech.xyz/native-captcha.html?action=$action&state=${model.captchaState}"
                                        ),
                                    )
                            } catch (_: android.content.ActivityNotFoundException) {
                                model.error = "请先安装浏览器，再完成人机验证。"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                GlassButton(
                    text = if (register) "提交注册" else "登录",
                    onClick = {
                        model.perform {
                            val verificationToken = model.captchaToken
                            model.captchaToken = ""
                            val r =
                                model.api.request(
                                    if (admin && !register) "/api/admin/login" else "/api/user/$action",
                                    "POST",
                                    fields =
                                        mapOf(
                                            "student_id" to account,
                                            "username" to account,
                                            "password" to password,
                                            "nickname" to nickname,
                                            "email" to email,
                                            "email_notify" to "true",
                                            "captcha_token" to verificationToken,
                                        ),
                                )
                            model.captchaToken = ""
                            password = ""
                            if (register) {
                                result = r.s("message", "注册已提交，等待审核")
                                register = false
                            } else {
                                model.refreshSession()
                                back()
                            }
                        }
                    },
                    enabled =
                        !model.busy &&
                            account.isNotBlank() &&
                            password.isNotBlank() &&
                            (!captchaRequired || model.captchaToken.isNotEmpty()),
                    modifier = Modifier.fillMaxWidth(),
                    style = GlassButtonStyle.Prominent,
                    height = 52.dp,
                )
                if (result.isNotEmpty()) Text(result, color = Glass.colors.accent)
            }
            if (!register)
                GlassButton(
                    if (admin) "返回学生登录" else "管理账号登录",
                    { admin = !admin },
                    Modifier.align(Alignment.CenterHorizontally),
                    icon = Icons.Rounded.AdminPanelSettings,
                )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun MeScreen(model: WallModel, go: (String) -> Unit) {
    val user = model.user
    var logout by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        GlassTopBar("我的")
        Column(
            Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = LocalBottomSpace.current),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GlassPanel(
                Modifier.fillMaxWidth(),
                tint = Glass.colors.accent,
                onClick = if (user == null) ({ go("login") }) else ({ go("profile/${user.s("id")}") }),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(avatarUrl(user?.s("id") ?: "0"), 64.dp)
                    Column(Modifier.weight(1f).padding(start = 16.dp)) {
                        Text(
                            user?.s("nickname")?.ifBlank { null } ?: "你好，同学",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            user?.s("bio")?.ifBlank { null }
                                ?: if (user == null) "登录后收藏、评论和分享校园生活" else "还没有填写个人简介",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Glass.colors.secondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        user?.s("role")?.takeIf { it.isNotBlank() && it != "user" }?.let {
                            Text(displayState(it), style = MaterialTheme.typography.labelMedium, color = Glass.colors.accent)
                        }
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Glass.colors.secondary)
                }
                if (user == null)
                    GlassButton("登录 / 注册", { go("login") }, Modifier.fillMaxWidth(), style = GlassButtonStyle.Prominent)
            }
            val needLogin: (String) -> Unit = { path -> go(if (user == null) "login" else path) }
            GlassPanel(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                MenuRow(Icons.AutoMirrored.Rounded.Article, "我的发布") { needLogin("posts") }
                MenuRow(Icons.AutoMirrored.Rounded.Chat, "我的评论") { needLogin("comments") }
                MenuRow(Icons.Rounded.Bookmark, "我的收藏") { needLogin("saved") }
            }
            GlassPanel(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                MenuRow(Icons.Rounded.Settings, "外观、账号与通知") { go("settings") }
                MenuRow(Icons.AutoMirrored.Rounded.HelpOutline, "公告、社区规则与反馈") { go("help") }
                if (user?.strings("capabilities")?.isNotEmpty() == true)
                    MenuRow(Icons.Rounded.AdminPanelSettings, "管理中心") { go("admin") }
            }
            if (user != null)
                GlassButton(
                    "退出登录",
                    { logout = true },
                    Modifier.fillMaxWidth(),
                    style = GlassButtonStyle.Danger,
                    icon = Icons.AutoMirrored.Rounded.Logout,
                )
        }
    }
    if (logout)
        GlassConfirmDialog(
            title = "退出登录？",
            text = "退出后将停止后台消息提醒。",
            confirmText = "退出",
            danger = true,
            onDismissRequest = { logout = false },
            onConfirm = {
                logout = false
                model.perform {
                    model.api.request("/api/user/logout", "POST")
                    model.api.cookies.clear()
                    model.user = null
                    model.ownedPosts.clear()
                    NotificationWorker.disable(model.api.context)
                }
            },
        )
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedRectangle(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(22.dp), tint = Glass.colors.accent)
        Text(title, Modifier.weight(1f).padding(start = 14.dp), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Glass.colors.secondary)
    }
}

@Composable
fun AccountScreen(model: WallModel, back: () -> Unit) {
    val user = model.user
    var nickname by rememberSaveable { mutableStateOf(user?.s("nickname").orEmpty()) }
    var bio by rememberSaveable { mutableStateOf(user?.s("bio").orEmpty()) }
    var gender by rememberSaveable { mutableStateOf(user?.s("gender", "0") ?: "0") }
    var email by rememberSaveable { mutableStateOf("") }
    var old by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    var notifications by remember {
        mutableStateOf(context.getSharedPreferences("preferences", 0).getBoolean("notifications", false))
    }
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
                }
        }
    Column(Modifier.fillMaxSize().imePadding()) {
        GlassTopBar("设置", back)
        Column(
            Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = LocalBottomSpace.current),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GlassPanel(Modifier.fillMaxWidth()) {
                SectionTitle("外观")
                GlassSegmented(
                    listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色"),
                    model.themeMode,
                    { model.setAppearance(theme = it) },
                )
                GlassSwitchRow(
                    "使用系统动态配色",
                    model.dynamicColor,
                    { model.setAppearance(dynamic = it) },
                    supporting =
                        if (Build.VERSION.SDK_INT >= 31) "玻璃的色彩跟随系统壁纸" else "需要 Android 12 及以上",
                    enabled = Build.VERSION.SDK_INT >= 31,
                )
            }
            GlassPanel(Modifier.fillMaxWidth()) {
                SectionTitle("通知")
                GlassSwitchRow(
                    "系统通知",
                    notifications,
                    { v ->
                        if (v) {
                            if (Build.VERSION.SDK_INT >= 33)
                                permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            else {
                                notifications = true
                                NotificationWorker.enable(context)
                            }
                        } else {
                            notifications = false
                            NotificationWorker.disable(context)
                        }
                    },
                    supporting = "后台定期检查新消息；受系统省电策略影响，可能延迟。",
                    enabled = user != null,
                )
                if (user != null)
                    GlassSwitchRow(
                        "邮件通知",
                        user.optBoolean("email_notify"),
                        { v ->
                            model.perform {
                                model.api.request(
                                    "/api/user/me/email/notify",
                                    "POST",
                                    fields = mapOf("email_notify" to "$v"),
                                )
                                model.refreshSession()
                            }
                        },
                    )
                else Text("登录后可开启消息提醒。", style = MaterialTheme.typography.bodySmall, color = Glass.colors.secondary)
            }
            if (user != null) {
                GlassPanel(Modifier.fillMaxWidth()) {
                    SectionTitle("个人资料") {
                        GlassButton("更换头像", { avatar.launch("image/*") }, height = 36.dp)
                    }
                    GlassTextField(nickname, { nickname = it }, Modifier.fillMaxWidth(), label = "昵称", singleLine = true)
                    GlassTextField(bio, { bio = it }, Modifier.fillMaxWidth(), label = "个人简介", minLines = 2)
                    Text("性别", style = MaterialTheme.typography.labelLarge, color = Glass.colors.secondary)
                    GlassSegmented(listOf("0" to "未设置", "1" to "男", "2" to "女"), gender, { gender = it })
                    GlassButton(
                        "保存资料",
                        {
                            model.perform {
                                model.api.request(
                                    "/api/user/me/profile",
                                    "PUT",
                                    fields = mapOf("nickname" to nickname, "bio" to bio, "gender" to gender),
                                )
                                model.refreshSession()
                                model.error = "资料已保存"
                            }
                        },
                        Modifier.fillMaxWidth(),
                        enabled = !model.busy,
                        style = GlassButtonStyle.Prominent,
                    )
                }
                GlassPanel(Modifier.fillMaxWidth()) {
                    SectionTitle("邮箱")
                    Text(
                        "当前：" + user.s("email", "尚未绑定").ifBlank { "尚未绑定" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Glass.colors.secondary,
                    )
                    GlassTextField(
                        email,
                        { email = it },
                        Modifier.fillMaxWidth(),
                        placeholder = "绑定或更换邮箱",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    GlassButton(
                        "发送验证邮件",
                        {
                            model.perform {
                                model.api.request("/api/user/me/email", "POST", fields = mapOf("email" to email))
                                model.error = "验证邮件已发送，请前往邮箱完成验证"
                            }
                        },
                        Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank() && !model.busy,
                    )
                }
                GlassPanel(Modifier.fillMaxWidth()) {
                    SectionTitle("修改密码")
                    GlassTextField(old, { old = it }, Modifier.fillMaxWidth(), label = "当前密码", singleLine = true, secret = true)
                    GlassTextField(password, { password = it }, Modifier.fillMaxWidth(), label = "新密码", singleLine = true, secret = true)
                    GlassButton(
                        "更新密码",
                        {
                            model.perform {
                                model.api.request(
                                    "/api/user/me/password",
                                    "POST",
                                    fields = mapOf("current_password" to old, "new_password" to password),
                                )
                                old = ""
                                password = ""
                                model.refreshSession()
                                model.error = "密码已更新"
                            }
                        },
                        Modifier.fillMaxWidth(),
                        enabled = old.isNotBlank() && password.isNotBlank() && !model.busy,
                    )
                }
            }
        }
    }
}
