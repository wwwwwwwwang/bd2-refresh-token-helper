package ci.us.bd2.tokenhelper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ci.us.bd2.tokenhelper.model.TokenCaptureStore
import ci.us.bd2.tokenhelper.ui.TokenResultSheet
import ci.us.bd2.tokenhelper.ui.WebDebugLogSheet
import ci.us.bd2.tokenhelper.web.TokenWebViewController
import ci.us.bd2.tokenhelper.web.TokenWebViewHost
import ci.us.bd2.tokenhelper.web.WebDebugLogStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenHelperApp(
    store: TokenCaptureStore,
    window: Window,
    onExit: () -> Unit,
) {
    val token by store.token.collectAsStateWithLifecycle()
    val debugLogStore = remember { WebDebugLogStore() }
    val debugEnabled by debugLogStore.enabled.collectAsStateWithLifecycle()
    val debugEntries by debugLogStore.entries.collectAsStateWithLifecycle()
    val controller = remember { TokenWebViewController() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var showDebugLog by remember { mutableStateOf(false) }
    var clearingData by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var captureAvailable by remember { mutableStateOf<Boolean?>(null) }

    DisposableEffect(token) {
        if (token != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    BackHandler {
        if (!controller.goBack()) onExit()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BD2 Token 助手")
                        Text(
                            when (captureAvailable) {
                                true -> "安全捕获已启用"
                                false -> "当前 WebView 不支持安全捕获"
                                null -> "正在初始化安全捕获"
                            },
                            color = if (captureAvailable == false) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = controller::goHome) { Text("首页") }
                    TextButton(onClick = controller::reload) { Text("刷新") }
                    Box {
                        TextButton(onClick = { menuExpanded = true }) { Text("更多") }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(debugModeActionLabel(debugEnabled)) },
                                onClick = {
                                    menuExpanded = false
                                    val nextDebugEnabled = !debugEnabled
                                    debugLogStore.setEnabled(nextDebugEnabled)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            debugModeChangedMessage(nextDebugEnabled),
                                        )
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("查看调试日志") },
                                onClick = {
                                    menuExpanded = false
                                    showDebugLog = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("清除网页登录数据") },
                                onClick = {
                                    menuExpanded = false
                                    confirmClear = true
                                },
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            TokenWebViewHost(
                controller = controller,
                onToken = {
                    if (store.capture(it)) {
                        controller.clearBrowsingData {}
                    }
                },
                onCaptureAvailabilityChanged = { captureAvailable = it },
                onLoadingChanged = { loading = it },
                onError = { errorMessage = it },
                debugLogStore = debugLogStore,
            )
            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                )
            }
            errorMessage?.let { message ->
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(message, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = controller::reload) { Text("重试") }
                }
            }
        }
    }

    token?.let { capturedToken ->
        TokenResultSheet(
            token = capturedToken,
            onCopy = {
                copyToken(context, capturedToken)
                scope.launch { snackbarHostState.showSnackbar("完整 Token 已复制") }
            },
            onDismiss = store::clear,
        )
    }

    if (showDebugLog) {
        val debugLogText = debugEntries.joinToString(separator = "\n") { it.render() }
        WebDebugLogSheet(
            enabled = debugEnabled,
            logText = debugLogText,
            onCopy = {
                copyDebugLog(context, debugLogText)
                scope.launch { snackbarHostState.showSnackbar("脱敏调试日志已复制") }
            },
            onClear = {
                debugLogStore.clear()
                scope.launch { snackbarHostState.showSnackbar("调试日志已清空") }
            },
            onDismiss = { showDebugLog = false },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { if (!clearingData) confirmClear = false },
            title = { Text("清除网页登录数据？") },
            text = { Text("将清除本 App 内官网的 Cookie、缓存和站点存储，不影响手机 Chrome。") },
            confirmButton = {
                TextButton(
                    enabled = !clearingData,
                    onClick = {
                        clearingData = true
                        store.clear()
                        controller.clearBrowsingData {
                            clearingData = false
                            confirmClear = false
                            scope.launch { snackbarHostState.showSnackbar("网页登录数据已清除") }
                        }
                    },
                ) { Text(if (clearingData) "清理中…" else "确认清除") }
            },
            dismissButton = {
                TextButton(
                    enabled = !clearingData,
                    onClick = { confirmClear = false },
                ) { Text("取消") }
            },
        )
    }
}

private fun copyToken(context: Context, token: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(SensitiveClipboard.create(token))
}

private fun copyDebugLog(context: Context, logText: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("BD2 WebView 调试日志", logText))
}
