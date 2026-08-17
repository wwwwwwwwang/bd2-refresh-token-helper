package ci.us.bd2.tokenhelper.web

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

private const val OFFICIAL_ORIGIN = "https://webshop.browndust2.global"

@Composable
fun TokenWebViewHost(
    controller: TokenWebViewController,
    onToken: (String) -> Unit,
    onCaptureAvailabilityChanged: (Boolean) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    debugLogStore: WebDebugLogStore,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            createWebViewContainer(
                context = context,
                controller = controller,
                onToken = onToken,
                onCaptureAvailabilityChanged = onCaptureAvailabilityChanged,
                onLoadingChanged = onLoadingChanged,
                onError = onError,
                debugLogStore = debugLogStore,
            )
        },
        modifier = modifier.fillMaxSize(),
    )

    DisposableEffect(controller) {
        onDispose(controller::destroy)
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createWebViewContainer(
    context: Context,
    controller: TokenWebViewController,
    onToken: (String) -> Unit,
    onCaptureAvailabilityChanged: (Boolean) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    debugLogStore: WebDebugLogStore,
): FrameLayout {
    debugLogStore.record(WebDebugSource.SYSTEM, "创建 WebView 容器")
    val container = FrameLayout(context)
    val mainWebView = WebView(context)
    configureSettings(mainWebView.settings)
    CookieManager.getInstance().setAcceptCookie(true)
    CookieManager.getInstance().setAcceptThirdPartyCookies(mainWebView, true)

    controller.attach(container, mainWebView)
    container.addView(
        mainWebView,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ),
    )

    val captureAvailable = TokenCaptureCapability.isSupported(
        WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER),
        WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT),
    )
    onCaptureAvailabilityChanged(captureAvailable)
    debugLogStore.record(
        source = WebDebugSource.SYSTEM,
        action = "安全捕获能力检查",
        detail = "支持=$captureAvailable",
    )
    if (captureAvailable) {
        TokenWebMessageBridge.install(mainWebView, onToken)
        installInterceptor(mainWebView)
    } else {
        onError("当前 Android System WebView 版本不支持安全 Token 捕获，请更新后重试")
    }
    mainWebView.webViewClient = mainWebViewClient(
        context,
        captureAvailable,
        onLoadingChanged,
        onError,
        debugLogStore,
    )
    mainWebView.webChromeClient = mainWebChromeClient(
        context,
        controller,
        onError,
        debugLogStore,
    )
    controller.clearBrowsingData {}
    return container
}

@SuppressLint("SetJavaScriptEnabled")
private fun configureSettings(settings: WebSettings) {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.javaScriptCanOpenWindowsAutomatically = false
    settings.setSupportMultipleWindows(true)
    settings.allowFileAccess = false
    settings.allowContentAccess = false
    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
    settings.cacheMode = WebSettings.LOAD_DEFAULT
    settings.mediaPlaybackRequiresUserGesture = true
    settings.safeBrowsingEnabled = true
}

private fun installInterceptor(webView: WebView) {
    WebViewCompat.addDocumentStartJavaScript(
        webView,
        TokenInterceptorScript.source,
        setOf(OFFICIAL_ORIGIN),
    )
}

private fun mainWebViewClient(
    context: Context,
    captureAvailable: Boolean,
    onLoadingChanged: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    debugLogStore: WebDebugLogStore,
): WebViewClient =
    object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            onLoadingChanged(true)
            debugLogStore.record(WebDebugSource.MAIN, "页面开始", url)
            if (blockUnsafeMainNavigation(context, view, url, onError, debugLogStore)) return
            if (captureAvailable) onError(null)
        }

        override fun onPageFinished(view: WebView, url: String?) {
            onLoadingChanged(false)
            debugLogStore.record(WebDebugSource.MAIN, "页面完成", url)
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            if (!request.isForMainFrame) {
                debugLogStore.record(
                    WebDebugSource.MAIN,
                    "导航放行",
                    url,
                    navigationDetail(request, "子框架"),
                )
                return false
            }
            return when (NavigationPolicy.decide(url)) {
                NavigationDecision.ALLOW_MAIN -> {
                    debugLogStore.record(
                        WebDebugSource.MAIN,
                        "导航放行",
                        url,
                        navigationDetail(request, "官网主页面"),
                    )
                    false
                }
                NavigationDecision.OPEN_EXTERNAL -> {
                    debugLogStore.record(
                        WebDebugSource.MAIN,
                        "导航转交外部浏览器",
                        url,
                        navigationDetail(request, "外部链接"),
                    )
                    openExternal(context, request.url, onError, debugLogStore)
                    true
                }
                NavigationDecision.REJECT -> {
                    debugLogStore.record(
                        WebDebugSource.MAIN,
                        "导航已阻止",
                        url,
                        navigationDetail(request, "不安全地址"),
                    )
                    true
                }
            }
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            if (request.isForMainFrame) {
                debugLogStore.record(
                    WebDebugSource.MAIN,
                    "页面加载错误",
                    request.url.toString(),
                    "错误码=${error.errorCode}",
                )
                onLoadingChanged(false)
                onError("官网加载失败，请检查网络后重试")
            }
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse,
        ) {
            if (request.isForMainFrame) {
                debugLogStore.record(
                    WebDebugSource.MAIN,
                    "HTTP 响应错误",
                    request.url.toString(),
                    "状态码=${errorResponse.statusCode}",
                )
            }
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            debugLogStore.record(
                WebDebugSource.MAIN,
                "SSL 连接错误",
                error.url,
                "错误码=${error.primaryError}",
            )
            handler.cancel()
        }
    }

private fun mainWebChromeClient(
    context: Context,
    controller: TokenWebViewController,
    onError: (String?) -> Unit,
    debugLogStore: WebDebugLogStore,
): WebChromeClient =
    object : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message,
        ): Boolean {
            debugLogStore.record(
                source = WebDebugSource.POPUP,
                action = "请求创建登录窗口",
                rawUrl = view.url,
                detail = "对话框=$isDialog; 用户手势=$isUserGesture",
            )
            if (!WebViewSecurityPolicy.allowsPopupCreation(isUserGesture)) {
                debugLogStore.record(WebDebugSource.POPUP, "登录窗口创建已阻止")
                onError("已阻止非用户触发的登录窗口")
                return false
            }
            val popup = WebView(context)
            configureSettings(popup.settings)
            CookieManager.getInstance().setAcceptThirdPartyCookies(popup, true)
            popup.webViewClient = popupWebViewClient(onError, debugLogStore)
            popup.webChromeClient = object : WebChromeClient() {
                override fun onCloseWindow(window: WebView) {
                    debugLogStore.record(WebDebugSource.POPUP, "登录窗口请求关闭", window.url)
                    controller.closePopup()
                }
            }
            controller.attachPopup(popup)

            val transport = resultMsg.obj as? WebView.WebViewTransport
            if (transport == null) {
                debugLogStore.record(WebDebugSource.POPUP, "登录窗口创建失败")
                controller.closePopup()
                onError("登录窗口创建失败，请重试")
                return false
            }
            transport.webView = popup
            resultMsg.sendToTarget()
            debugLogStore.record(WebDebugSource.POPUP, "登录窗口已创建")
            return true
        }

        override fun onCloseWindow(window: WebView) {
            debugLogStore.record(WebDebugSource.POPUP, "主页面请求关闭登录窗口", window.url)
            controller.closePopup()
        }
    }

private fun popupWebViewClient(
    onError: (String?) -> Unit,
    debugLogStore: WebDebugLogStore,
): WebViewClient =
    object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            val allowed = WebViewSecurityPolicy.allowsPopupNavigation(url)
            debugLogStore.record(
                WebDebugSource.POPUP,
                if (allowed) "页面开始" else "页面开始时已阻止",
                url,
                "主框架=true",
            )
            if (!allowed) {
                view.stopLoading()
                onError("已阻止登录窗口打开未知站点")
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            val blocked = WebViewSecurityPolicy.shouldBlockPopupRequest(
                url,
                request.isForMainFrame,
            )
            debugLogStore.record(
                WebDebugSource.POPUP,
                if (blocked) "导航已阻止" else "导航放行",
                url,
                navigationDetail(request, if (request.isForMainFrame) "认证主页面" else "子框架"),
            )
            if (blocked) onError("已阻止登录窗口打开未知站点")
            return blocked
        }

        override fun onPageFinished(view: WebView, url: String?) {
            debugLogStore.record(WebDebugSource.POPUP, "页面完成", url)
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            if (request.isForMainFrame) {
                debugLogStore.record(
                    WebDebugSource.POPUP,
                    "页面加载错误",
                    request.url.toString(),
                    "错误码=${error.errorCode}",
                )
            }
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse,
        ) {
            if (request.isForMainFrame) {
                debugLogStore.record(
                    WebDebugSource.POPUP,
                    "HTTP 响应错误",
                    request.url.toString(),
                    "状态码=${errorResponse.statusCode}",
                )
            }
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            debugLogStore.record(
                WebDebugSource.POPUP,
                "SSL 连接错误",
                error.url,
                "错误码=${error.primaryError}",
            )
            handler.cancel()
        }
    }

private fun blockUnsafeMainNavigation(
    context: Context,
    webView: WebView,
    url: String?,
    onError: (String?) -> Unit,
    debugLogStore: WebDebugLogStore,
): Boolean {
    if (WebViewSecurityPolicy.allowsMainFrame(url)) {
        debugLogStore.record(WebDebugSource.MAIN, "页面开始检查通过", url)
        return false
    }
    debugLogStore.record(WebDebugSource.MAIN, "页面开始检查已阻止", url)
    webView.stopLoading()
    if (url != null && NavigationPolicy.decide(url) == NavigationDecision.OPEN_EXTERNAL) {
        openExternal(context, Uri.parse(url), onError, debugLogStore)
    }
    return true
}

private fun openExternal(
    context: Context,
    uri: Uri,
    onError: (String?) -> Unit,
    debugLogStore: WebDebugLogStore,
) {
    try {
        debugLogStore.record(WebDebugSource.SYSTEM, "打开外部浏览器", uri.toString())
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (_: ActivityNotFoundException) {
        debugLogStore.record(WebDebugSource.SYSTEM, "外部浏览器不可用", uri.toString())
        onError("未找到可打开此链接的浏览器")
    }
}

private fun navigationDetail(request: WebResourceRequest, decision: String): String =
    "主框架=${request.isForMainFrame}; 方法=${request.method}; 判定=$decision"
