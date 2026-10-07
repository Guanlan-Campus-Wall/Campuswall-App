package xyz.zongtech.campuswall

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject

class WallModel(application: Application) : AndroidViewModel(application) {
    val api = WallApi(application)
    var user by mutableStateOf<JSONObject?>(null)
    var community by mutableStateOf(JSONObject())
    var error by mutableStateOf<String?>(null)
    var revision by mutableIntStateOf(0)
    var busy by mutableStateOf(false)
    var captchaToken by mutableStateOf("")
    var captchaState: String = ""
    val ownedPosts = mutableStateListOf<String>()
    var pendingMessage by mutableStateOf<String?>(null)
    var openNotifications by mutableStateOf(false)
    var unread by mutableIntStateOf(0)
    /** 底部输入条被点按的次数，表白墙与失物招领页据此打开发布抽屉。 */
    var dockTick by mutableIntStateOf(0)
    private val preferences = application.getSharedPreferences("preferences", 0)
    var themeMode by mutableStateOf(preferences.getString("theme", "system") ?: "system")
    var palette by mutableStateOf(preferences.getString("palette", "clay") ?: "clay")
    val favoriteIds = mutableStateListOf<String>()

    fun setAppearance(theme: String = themeMode, palette: String = this.palette) {
        themeMode = theme
        this.palette = palette
        preferences.edit().putString("theme", theme).putString("palette", palette).apply()
    }

    fun refreshFavorites() {
        if (user == null) {
            favoriteIds.clear()
            return
        }
        viewModelScope.launch {
            try {
                val ids = api.request("/api/user/me/favorites/ids").strings("ids")
                favoriteIds.clear()
                favoriteIds.addAll(ids)
            } catch (_: Exception) {}
        }
    }

    init {
        refreshSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            try {
                user = api.request("/api/user/me").optJSONObject("user")
            } catch (_: Exception) {
                user = null
            }
            refreshFavorites()
            try {
                community =
                    api.request("/api/community/config").optJSONObject("community") ?: JSONObject()
            } catch (_: Exception) {}
        }
    }

    fun perform(success: () -> Unit = {}, block: suspend () -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try {
                block()
                revision++
                success()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.displayError()
            } finally {
                busy = false
            }
        }
    }

    fun refreshUnread() {
        if (user == null) {
            unread = 0
            return
        }
        viewModelScope.launch {
            try {
                unread =
                    api.request("/api/user/me/notifications?page=1&page_size=1").optInt("unread")
            } catch (_: Exception) {}
        }
    }

    fun can(capability: String) = user?.strings("capabilities")?.contains(capability) == true
}
