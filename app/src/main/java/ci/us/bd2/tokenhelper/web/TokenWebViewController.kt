package ci.us.bd2.tokenhelper.web

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.FrameLayout

class TokenWebViewController {
    private var container: FrameLayout? = null
    private var mainWebView: WebView? = null
    private var popupWebView: WebView? = null
    private var destroyed = false

    internal fun attach(container: FrameLayout, mainWebView: WebView) {
        destroyed = false
        this.container = container
        this.mainWebView = mainWebView
    }

    internal fun attachPopup(webView: WebView) {
        closePopup()
        popupWebView = webView
        container?.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
    }

    fun reload() {
        mainWebView?.reload()
    }

    fun goHome() {
        closePopup()
        mainWebView?.loadUrl(NavigationPolicy.HOME_URL)
    }

    fun goBack(): Boolean {
        val popup = popupWebView
        if (popup != null) {
            if (popup.canGoBack()) {
                popup.goBack()
            } else {
                closePopup()
            }
            return true
        }

        val main = mainWebView ?: return false
        if (!main.canGoBack()) return false
        main.goBack()
        return true
    }

    fun clearBrowsingData(onComplete: () -> Unit) {
        if (destroyed) {
            onComplete()
            return
        }
        closePopup()
        val main = mainWebView
        clearWebViewData(main)

        CookieManager.getInstance().removeAllCookies {
            CookieManager.getInstance().flush()
            main?.post {
                if (!destroyed) main.loadUrl(NavigationPolicy.HOME_URL)
                onComplete()
            } ?: onComplete()
        }
    }

    internal fun closePopup() {
        val popup = popupWebView ?: return
        popupWebView = null
        container?.removeView(popup)
        popup.stopLoading()
        popup.loadUrl("about:blank")
        popup.removeAllViews()
        popup.destroy()
    }

    fun destroy() {
        destroyed = true
        closePopup()
        val main = mainWebView
        mainWebView = null
        container?.removeAllViews()
        container = null
        clearWebViewData(main)
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        if (main != null) {
            TokenWebMessageBridge.remove(main)
            main.stopLoading()
            main.loadUrl("about:blank")
            main.removeAllViews()
            main.destroy()
        }
    }

    private fun clearWebViewData(webView: WebView?) {
        WebStorage.getInstance().deleteAllData()
        webView?.clearCache(true)
        webView?.clearHistory()
        webView?.clearFormData()
    }
}
