# 观澜校园墙 · 安卓客户端

为龙华区观澜中学开发的原生安卓校园墙。使用 Kotlin 与 Jetpack Compose，界面与[网页版](https://wall.zongtech.xyz)保持一致，[AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) 液态玻璃只用于底部浮动栏和开关等少数组件，直接连接校园墙 API。

[下载安卓安装包](https://github.com/Guanlan-Campus-Wall/Campuswall-App/releases) · [网页版](https://wall.zongtech.xyz) · [构建记录](https://github.com/Guanlan-Campus-Wall/Campuswall-App/actions)

支持 Android 8.0 及以上。包含动态、话题、表白墙、失物招领、账号资料、发布与互动、消息、举报反馈及按权限开放的管理功能。系统相机、文件选择、分享、音视频播放和通知均使用安卓原生能力。安全验证单独交给系统浏览器处理。

## 界面结构

- `ui/Theme.kt` 照搬网页设计变量：象牙色背景、白色卡片与细边框、陶土色强调色、衬线标题、五套配色和深色模式。
- `ui/Kit.kt` 是与网页同款的卡片、按钮、标签、输入框、分段选择、弹层等基础组件，不做实时模糊，滚动更流畅。
- `ui/Admin.kt` 管理后台与网页后台一一对应：侧边导航、数据概览、帖子/评论审核队列与批量操作、回收站、用户与权限、公告、反馈、举报、通知渠道、站点设置、审计与日志。
- `glass/` 基于 `io.github.kyant0:backdrop`，只提供底部浮动栏 `GlassCapsule`、圆形按钮 `GlassCircle` 和开关 `GlassToggle`。页面图层仅在浮动栏显示时录制。

透镜折射需要 Android 13；更低版本自动使用半透明底色。

## 开发与验证

安装 Java 21 和 Android SDK 37，在 `local.properties` 设置自己的 `sdk.dir`，执行：

```sh
./gradlew assembleDebug lintDebug testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

第二条命令需要运行中的安卓设备或模拟器。接口测试使用本地模拟服务器，不向线上发布测试帖子。

## 自动发布

GitHub Actions 对提交执行编译、静态检查及 Android 35 设备测试。推送与 `versionName` 一致的 `v*` 标签后，通过测试才会构建正式签名 APK/AAB 并发布中文预览版。发布说明保存在 `docs/releases/版本标签.md`。

仓库 Secrets 保存 `ANDROID_KEYSTORE_BASE64`、`ANDROID_KEYSTORE_PASSWORD`、`ANDROID_KEY_ALIAS`、`ANDROID_KEY_PASSWORD`。签名密钥不进入源码；未来升级必须保留同一密钥。Release 附安装包校验和与签名验证报告。

## 当前验收范围

原生页面已经接入网站服务端。自动测试覆盖登录请求、错误响应、匿名帖子和加密会话；真实账号发帖、审核与不同厂商手机仍需试用验收。后台消息采用系统定期任务，间隔约 15 分钟，受省电策略影响。
