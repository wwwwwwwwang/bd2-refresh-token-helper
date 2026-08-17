package ci.us.bd2.tokenhelper.web

import android.net.Uri
import android.webkit.WebView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

object TokenWebMessageBridge {
    private const val BRIDGE_NAME = "Bd2TokenBridge"
    private const val OFFICIAL_HOST = "webshop.browndust2.global"
    private val allowedOrigins = setOf("https://$OFFICIAL_HOST")

    fun install(webView: WebView, onToken: (String) -> Unit): Boolean {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) return false

        WebViewCompat.addWebMessageListener(
            webView,
            BRIDGE_NAME,
            allowedOrigins,
        ) { _, message, sourceOrigin, isMainFrame, _ ->
            val token = message.data.orEmpty()
            if (isTrustedMainFrame(sourceOrigin, isMainFrame) && TokenValidator.isValid(token)) {
                onToken(token)
            }
        }
        return true
    }

    fun remove(webView: WebView) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.removeWebMessageListener(webView, BRIDGE_NAME)
        }
    }

    private fun isTrustedMainFrame(sourceOrigin: Uri, isMainFrame: Boolean): Boolean =
        isMainFrame &&
            sourceOrigin.scheme.equals("https", ignoreCase = true) &&
            sourceOrigin.host?.equals(OFFICIAL_HOST, ignoreCase = true) == true
}
