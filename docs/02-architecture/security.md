# 安全架构

## 信任边界

- 主 WebView 只允许 `https://webshop.browndust2.global` 主导航。
- Google、Apple、Firebase 和登录会话中转页面在不挂载 Native Bridge 的临时 WebView 中运行。
- Google 地区域名按受约束的国家域名结构判断；YouTube 仅精确允许 `accounts.youtube.com` 认证中转。
- Token Bridge 使用 AndroidX WebKit 来源受限 Web Message Listener，只接受官网主框架消息。

## Token 生命周期

1. 文档起始脚本包装官网 `fetch`。
2. 只检查 Firebase `accounts:signInWithIdp` 成功响应。
3. 原生层校验候选 Token 后写入内存状态。
4. 用户主动复制后，App 清除 WebView 会话。
5. 关闭结果面板或 App 时清除内存状态。

Token 面板显示期间启用 `FLAG_SECURE`，避免截图和最近任务缩略图泄露。Android 13 及以上的剪贴板内容标记为敏感。

## 调试日志

调试模式默认关闭。日志最多保留当前进程内最近 200 条，只记录时间、来源、动作、scheme、host、非默认端口和安全判定；不记录 path、查询参数、fragment、OAuth code、state、Cookie、响应体或 Refresh Token。

## 发布安全

- release keystore 和密码只保存在维护者离线备份与 GitHub Actions Secrets。
- Tag 版本必须与 `versionName` 一致。
- Release 工作流测试、Lint、签名构建后生成 APK 和 SHA-256。
- 密钥丢失后不能继续为相同 Application ID 提供可覆盖安装的升级版本，因此必须保留至少两份离线备份。
