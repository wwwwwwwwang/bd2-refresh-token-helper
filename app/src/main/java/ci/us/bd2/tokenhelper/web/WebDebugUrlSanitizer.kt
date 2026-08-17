package ci.us.bd2.tokenhelper.web

import java.net.URI
import java.util.Locale

object WebDebugUrlSanitizer {
    private val hiddenContentSchemes = setOf("blob", "data", "file", "javascript")

    fun sanitize(rawUrl: String?): String {
        if (rawUrl.isNullOrBlank()) return "[无地址]"
        val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return "[地址解析失败]"
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return "[地址解析失败]"

        if (scheme in hiddenContentSchemes) return "$scheme:[内容已隐藏]"
        if (scheme == "about") {
            return if (uri.schemeSpecificPart == "blank") "about:blank" else "about:[内容已隐藏]"
        }

        val host = uri.host?.lowercase(Locale.ROOT) ?: return "$scheme:[内容已隐藏]"
        val port = uri.port.takeIf { it >= 0 && !isDefaultPort(scheme, it) }
        return buildString {
            append(scheme)
            append("://")
            append(host)
            if (port != null) append(":$port")
        }
    }

    private fun isDefaultPort(scheme: String, port: Int): Boolean =
        (scheme == "https" && port == 443) || (scheme == "http" && port == 80)
}
