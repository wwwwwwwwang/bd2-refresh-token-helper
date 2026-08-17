package ci.us.bd2.tokenhelper.web

import java.net.URI

enum class NavigationDecision {
    ALLOW_MAIN,
    OPEN_EXTERNAL,
    REJECT,
}

object NavigationPolicy {
    const val HOME_URL = "https://webshop.browndust2.global/"
    private const val OFFICIAL_HOST = "webshop.browndust2.global"

    fun decide(url: String): NavigationDecision {
        val uri = runCatching { URI(url) }.getOrNull() ?: return NavigationDecision.REJECT
        if (!uri.scheme.equals("https", ignoreCase = true)) return NavigationDecision.REJECT
        return if (uri.host.equals(OFFICIAL_HOST, ignoreCase = true)) {
            NavigationDecision.ALLOW_MAIN
        } else {
            NavigationDecision.OPEN_EXTERNAL
        }
    }
}
