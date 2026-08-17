package ci.us.bd2.tokenhelper

internal fun debugModeActionLabel(enabled: Boolean): String =
    if (enabled) "关闭调试模式" else "开启调试模式"

internal fun debugModeChangedMessage(enabled: Boolean): String =
    if (enabled) "调试模式已开启" else "调试模式已关闭"
