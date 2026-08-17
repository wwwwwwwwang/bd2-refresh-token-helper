# 隐私说明

## 数据处理

App 只在用户设备上打开 Brown Dust 2 官方 Web Shop，并从官网 Firebase 登录响应中识别 Refresh Token。

- Refresh Token 只保存在 App 当前进程内存中。
- App 不把 Token 上传到作者、GitHub、BD2-Ops 或其他服务器。
- App 不把 Token 写入文件、数据库、日志、通知、崩溃报告或历史记录。
- App 不接入广告、统计、推送或第三方分析 SDK。
- App 只申请联网权限，不申请存储、通讯录、位置、相机或麦克风权限。

## WebView 数据

Google、Apple、Firebase 和 Brown Dust 2 官网可能在 WebView 中短暂使用 Cookie 与站点存储完成登录。App 会在首次加载、捕获 Token、用户主动清理和 WebView 销毁时清理相关数据。

系统剪贴板由用户点击“复制完整 Token”后写入。Android 8-12 可能允许其他前台应用读取剪贴板，复制后应尽快粘贴到可信目标并避免同时运行不可信应用。

## 联系与披露

请勿在公开 Issue 中粘贴真实 Refresh Token。安全问题按照仓库根目录的 `SECURITY.md` 私密报告。
