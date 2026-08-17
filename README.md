# BD2 Refresh Token 获取助手

[![Android CI](https://github.com/wwwwwwwwang/bd2-refresh-token-helper/actions/workflows/android.yml/badge.svg)](https://github.com/wwwwwwwwang/bd2-refresh-token-helper/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/wwwwwwwwang/bd2-refresh-token-helper)](https://github.com/wwwwwwwwang/bd2-refresh-token-helper/releases/latest)
[![License](https://img.shields.io/github/license/wwwwwwwwang/bd2-refresh-token-helper)](LICENSE)

Android 原生工具，用内置 WebView 打开 Brown Dust 2 官方 Web Shop，自动捕获 Google 或 Apple 登录产生的 Firebase Refresh Token，并在设备本地完整展示和复制。

本项目与 NEOWIZ、GAMFS N 或 Brown Dust 2 官方无关联。

## 当前范围

- 支持 Android 8.0（API 26）及以上版本。
- 支持 Google、Apple 官网登录流程。
- App 原生层只在进程内存中保存 Token，不写入文件、数据库、日志或网络。
- 不上传 Token，不连接 BD2-Ops，不自动添加或更新游戏账号。
- 不提供 Token 历史记录。

## 下载正式 APK

1. 打开 [最新 GitHub Release](https://github.com/wwwwwwwwang/bd2-refresh-token-helper/releases/latest)。
2. 下载 `bd2-refresh-token-helper-v*.apk` 和对应的 `.sha256` 文件。
3. 校验 APK 的 SHA-256 后安装。
4. 手机首次侧载时，按系统提示允许当前文件管理器或浏览器安装未知来源应用。

正式 APK 使用项目独立的长期 release 密钥签名。升级安装时应始终从本仓库 Release 下载，避免安装签名不同的第三方版本。

## 使用方法

1. 打开 App，等待 Brown Dust 2 官方 Web Shop 加载完成。
2. 在官网选择 Google 或 Apple，并完成正常登录。
3. 捕获成功后，App 会显示包含完整 Refresh Token 的原生面板。
4. 点击“复制完整 Token”，然后粘贴到可信的 BD2-Ops 游戏账号表单。
5. 关闭结果面板后，App 会清除内存中的 Token。

正常切换账号时先使用官网退出功能。如果官网退出不彻底、认证提供方自动选择旧账号或页面状态异常，打开“更多 -> 清除网页登录数据”，确认后清理本 App WebView 的 Cookie、缓存和站点存储。该操作不影响手机 Chrome。

### 收集登录调试日志

如果登录过程中提示“已阻止登录窗口打开未知站点”：

1. 打开“更多 -> 开启调试模式”；
2. 重新执行一次 Google 或 Apple 登录，直到问题再次出现；
3. 打开“更多 -> 查看调试日志”；
4. 点击“复制脱敏日志”并反馈日志内容。

调试日志只保存在 App 当前进程内存中，最多保留最近 200 条。日志记录主页面和登录弹窗的访问、跳转、安全判定、HTTP 状态码与 SSL 错误码，但 URL 只保留 scheme、host 和非默认端口，不记录 path、用户信息、查询参数、fragment、OAuth code、state、Refresh Token、Cookie、请求头或响应内容。关闭调试模式后停止新增日志，关闭 App 后日志自动消失。

## 安全说明

- App 只申请联网权限，禁止 Android 自动备份和设备迁移数据。
- 完整 Token 仅由用户主动复制到系统剪贴板。
- App 会在官网首次加载前、Token 捕获后和 WebView 销毁时清除 Cookie、缓存和站点存储，避免 Firebase 会话跨次保留。
- Token 面板显示期间启用安全窗口，系统截图和最近任务缩略图不会显示 Token。
- 剪贴板内容标记为敏感，Android 13 及以上不会在系统剪贴板预览中显示完整 Token。
- 官网主 WebView 只允许 `https://webshop.browndust2.global` 主导航。
- Google 登录弹窗允许 `accounts.google.com` 及 Google 根据出口地区跳转的 HTTPS 国家域名，例如 `accounts.google.de`、`accounts.google.co.jp` 和 `accounts.google.com.sg`；主机名必须完整匹配，附加伪造后缀仍会被阻止。
- Google 登录过程可能通过精确的 `accounts.youtube.com` 官方账号域名同步登录会话；不会放行普通 YouTube 页面或其他 YouTube 子域名。
- Token 使用 AndroidX WebKit 的来源受限 Web Message Listener 传给原生界面，不使用无限制的 `addJavascriptInterface`。
- Google/Apple 临时登录 WebView 不挂载 Token Bridge。
- WebView 调试模式默认关闭，日志只记录脱敏后的站点与导航信息。

Refresh Token 相当于长期登录凭据。不要发送给他人，不要粘贴到不受信任的网站、聊天窗口或日志中。

官网登录期间，Firebase Web SDK 可能短暂使用本 App 的 WebView 存储完成认证；App 会在捕获后立即清理。Android 8-12 无法阻止所有前台应用读取系统剪贴板，复制后应尽快粘贴到 BD2-Ops，并避免同时运行不可信应用。

## 已知限制

Google 或 Apple 可能根据认证安全策略拒绝嵌入式 WebView。App 不伪造 User-Agent，也不绕过认证提供方限制。如果页面明确提示不允许使用当前浏览器，请保留提示截图并反馈；替代登录承载方式需要根据真机结果单独设计。

App 依赖设备上的 Android System WebView。若提示不支持安全 Token 捕获，请先在应用商店更新 Android System WebView 或 Chrome 后重试。

安全捕获要求 System WebView 同时支持来源受限 Web Message 和文档起始脚本。能力不足时 App 会持续显示“当前 WebView 不支持安全捕获”，不会使用可能错过登录请求的延迟注入。

## GitHub Actions

普通提交由 `.github/workflows/android.yml` 执行：

- JVM 单元测试；
- Android Lint；
- debug APK 构建；
- APK、测试报告和 Lint 报告上传。

推送与 `app/build.gradle.kts` 版本一致的 `v*` Tag 时，`.github/workflows/release.yml` 使用以下 GitHub Actions Secrets 构建正式 APK：

- `ANDROID_RELEASE_KEYSTORE_BASE64`
- `ANDROID_RELEASE_KEY_ALIAS`
- `ANDROID_RELEASE_KEY_PASSWORD`
- `ANDROID_RELEASE_STORE_PASSWORD`

Release 工作流会运行正式单元测试、Lint 和签名构建，生成 APK 与 SHA-256 文件并创建 GitHub Release。签名文件和密码不会进入仓库或构建产物。

更多资料见 [项目文档](docs/README.md)。
