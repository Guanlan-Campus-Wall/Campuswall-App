# 观澜校园墙 · 安卓客户端

为龙华区观澜中学开发的原生安卓校园墙。使用 Kotlin 与 Jetpack Compose，界面全部采用 [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) 液态玻璃效果，直接连接校园墙 API。

[下载安卓安装包](https://github.com/Guanlan-Campus-Wall/Campuswall-App/releases) · [网页版](https://wall.zongtech.xyz) · [构建记录](https://github.com/Guanlan-Campus-Wall/Campuswall-App/actions)

支持 Android 8.0 及以上。包含动态、话题、表白墙、失物招领、账号资料、发布与互动、消息、举报反馈及按权限开放的管理功能。系统相机、文件选择、分享、音视频播放和通知均使用安卓原生能力。安全验证单独交给系统浏览器处理。

## 液态玻璃界面

`app/src/main/java/xyz/zongtech/campuswall/glass` 是基于 `io.github.kyant0:backdrop` 的组件库，所有页面只使用这里的组件：

- `GlassPanel` 玻璃卡片，会把自身导出为新的背景，卡片内的按钮、标签、开关继续折射卡片玻璃；
- `GlassButton` / `GlassIconButton` / `GlassChip` 按压放大并跟随手指产生弹性位移与高光；
- `GlassToggle` 开关、`GlassTextField` 输入框、`GlassSegmented` 分段选择器；
- `GlassTabs` 底部标签栏，选中指示器是一块可拖动的玻璃透镜；
- `GlassDialog` 弹窗与顶部提示渲染在页面图层之外，可以模糊整个页面。

实时模糊需要 Android 12，透镜折射需要 Android 13；更低版本自动改用更不透明的玻璃底色，保证文字清晰。

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
