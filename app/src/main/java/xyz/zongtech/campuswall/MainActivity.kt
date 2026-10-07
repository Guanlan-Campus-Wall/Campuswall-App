package xyz.zongtech.campuswall

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import xyz.zongtech.campuswall.glass.GlassTheme
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
            val colors =
                if (Build.VERSION.SDK_INT >= 31 && model.dynamicColor) {
                    if (dark) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
                } else if (dark)
                    darkColorScheme(
                        primary = Color(0xFF9DD4B5),
                        onPrimary = Color(0xFF003824),
                        secondary = Color(0xFFB8CCBD),
                        tertiary = Color(0xFFF0B6C8),
                    )
                else
                    lightColorScheme(
                        primary = Color(0xFF285C49),
                        primaryContainer = Color(0xFFB7EFD3),
                        secondary = Color(0xFF516457),
                        tertiary = Color(0xFF8E4A60),
                    )
            DisposableEffect(dark) {
                // 系统栏保持透明，图标颜色跟随应用内的深浅色设置。
                val style = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                enableEdgeToEdge(style, style)
                onDispose {}
            }
            GlassTheme(colors, dark) { CampusWallApp(model) }
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
