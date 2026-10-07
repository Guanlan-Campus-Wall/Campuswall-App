package xyz.zongtech.campuswall

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import xyz.zongtech.campuswall.ui.WebTheme
import xyz.zongtech.campuswall.ui.CampusWallApp

class MainActivity : ComponentActivity() {
    private val model: WallModel by viewModels()

    override fun attachBaseContext(newBase: android.content.Context) {
        val configuration = android.content.res.Configuration(newBase.resources.configuration)
        configuration.setLocale(java.util.Locale.SIMPLIFIED_CHINESE)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        receive(intent)
        coil.Coil.setImageLoader(
            coil.ImageLoader.Builder(this)
                .okHttpClient(model.api.client)
                .components { add(coil.decode.SvgDecoder.Factory()) }
                .build()
        )
        setContent {
            val dark =
                when (model.themeMode) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemInDarkTheme()
                }
            DisposableEffect(dark) {
                // 系统栏保持透明，图标颜色跟随应用内的深浅色设置。
                val style = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                enableEdgeToEdge(style, style)
                onDispose {}
            }
            WebTheme(dark, model.palette) { CampusWallApp(model) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        receive(intent)
    }

    private fun receive(intent: Intent?) {
        if (intent?.getBooleanExtra("open_notifications", false) == true)
            model.openNotifications = true
        val uri = intent?.data ?: return
        if (
            uri.scheme == "https" &&
                uri.host == "wall.zongtech.xyz" &&
                uri.path?.startsWith("/wall/message/") == true
        )
            uri.lastPathSegment
                ?.toLongOrNull()
                ?.takeIf { it > 0 }
                ?.let { model.pendingMessage = it.toString() }
        if (
            uri.scheme == "campuswall" &&
                uri.host == "captcha" &&
                model.captchaState.isNotEmpty() &&
                uri.getQueryParameter("state") == model.captchaState
        ) {
            model.captchaToken = uri.getQueryParameter("token").orEmpty()
            model.captchaState = ""
        }
    }
}
