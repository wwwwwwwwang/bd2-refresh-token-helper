package ci.us.bd2.tokenhelper.web

import java.net.URI

object WebViewSecurityPolicy {
    private val googleRegionalAccountsHost =
        Regex(
            pattern = "^accounts\\.google\\.(?:[a-z]{2}|(?:co|com)\\.[a-z]{2})$",
            option = RegexOption.IGNORE_CASE,
        )

    private val popupHosts =
        setOf(
            "accounts.google.com",
            "accounts.youtube.com",
            "appleid.apple.com",
            "signin.browndust2.global",
            "webshop.browndust2.global",
        )

    fun allowsMainFrame(url: String?): Boolean =
        url != null && NavigationPolicy.decide(url) == NavigationDecision.ALLOW_MAIN

    fun allowsPopupCreation(isUserGesture: Boolean): Boolean = isUserGesture

    fun shouldBlockPopupRequest(url: String?, isForMainFrame: Boolean): Boolean =
        isForMainFrame && !allowsPopupNavigation(url)

    fun allowsPopupNavigation(url: String?): Boolean {
        if (url == "about:blank") return true
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) &&
            isAllowedPopupHost(uri.host)
    }

    private fun isAllowedPopupHost(host: String?): Boolean =
        host != null &&
            (popupHosts.any { host.equals(it, ignoreCase = true) } ||
                googleRegionalAccountsHost.matches(host))
}
