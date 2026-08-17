# 发布流程

## 版本规则

- `versionName` 使用语义化版本，例如 `1.0.0`。
- `versionCode` 每次发布必须递增，不能复用。
- Git Tag 使用 `v<versionName>`，例如 `v1.0.0`。

## 发布前检查

1. 确认 `main` 的 Android CI 成功。
2. 确认四项 `ANDROID_RELEASE_*` Secrets 已配置。
3. 确认 release keystore 和密码备份可读取且未进入 Git。
4. 更新 `versionCode`、`versionName` 和 `docs/CHANGELOG.md`。
5. 推送与 `versionName` 一致的 Tag。

## 自动发布

`release.yml` 会执行：

```text
testReleaseUnitTest -> lintRelease -> assembleRelease
  -> 重命名 APK -> 生成 SHA-256 -> 创建 GitHub Release
```

发布完成后下载 APK，使用 `.sha256` 文件校验，并在已安装上一正式版的设备上执行覆盖安装测试。

`v1.0.0` 使用全新的正式签名，无法覆盖安装早期 Actions 测试 APK。测试用户首次迁移时必须先卸载测试版；从 `v1.0.0` 开始，所有正式版本必须继续使用同一 keystore。

## 回滚

GitHub Release 不覆盖已有 Tag。发现问题时将有问题的 Release 标记为 pre-release 或删除其下载入口，修复后递增 `versionCode` 和补丁版本发布新版本；不得用旧密钥以外的签名覆盖同一应用。
