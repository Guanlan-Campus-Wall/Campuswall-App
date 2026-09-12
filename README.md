# 观澜校园墙 · 安卓客户端

为龙华区观澜中学开发的原生安卓校园墙。使用 Kotlin、Jetpack Compose 与 Material Design 3，直接连接校园墙 API。

[下载安卓安装包](https://github.com/Guanlan-Campus-Wall/Campuswall-App/releases) · [网页版](https://wall.zongtech.xyz) · [构建记录](https://github.com/Guanlan-Campus-Wall/Campuswall-App/actions)

支持 Android 8.0 及以上。包含动态、话题、表白墙、失物招领、账号资料、发布与互动、消息、举报反馈及按权限开放的管理功能。系统相机、文件选择、分享、音视频播放和通知均使用安卓原生能力。安全验证单独交给系统浏览器处理。

## 开发与验证

安装 Java 21 和 Android SDK 35，在 `local.properties` 设置自己的 `sdk.dir`，执行：

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
