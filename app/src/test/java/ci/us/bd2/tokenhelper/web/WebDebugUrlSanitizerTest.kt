package ci.us.bd2.tokenhelper.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WebDebugUrlSanitizerTest {
    @Test
    fun removesCredentialsQueryAndFragment() {
        val sanitized = WebDebugUrlSanitizer.sanitize(
            "https://user:password@accounts.google.com:8443/o/oauth2/auth?code=secret&state=value#result",
        )

        assertEquals("https://accounts.google.com:8443", sanitized)
        assertFalse(sanitized.contains("user"))
        assertFalse(sanitized.contains("secret"))
        assertFalse(sanitized.contains("state"))
    }

    @Test
    fun removesPlainAndEncodedSecretsFromPath() {
        val plain = WebDebugUrlSanitizer.sanitize(
            "https://signin.example/callback/state/SECRET/refresh-token/TOKEN",
        )
        val encoded = WebDebugUrlSanitizer.sanitize(
            "https://signin.example/callback/%53%45%43%52%45%54",
        )

        assertEquals("https://signin.example", plain)
        assertEquals("https://signin.example", encoded)
        assertFalse(plain.contains("SECRET"))
        assertFalse(plain.contains("TOKEN"))
        assertFalse(encoded.contains("%53"))
    }

    @Test
    fun keepsSafeSpecialPagesWithoutLeakingPayload() {
        assertEquals("about:blank", WebDebugUrlSanitizer.sanitize("about:blank"))
        assertEquals("javascript:[内容已隐藏]", WebDebugUrlSanitizer.sanitize("javascript:alert('secret')"))
        assertEquals("data:[内容已隐藏]", WebDebugUrlSanitizer.sanitize("data:text/plain,secret"))
    }

    @Test
    fun hidesUnparseableAndMissingUrls() {
        assertEquals("[无地址]", WebDebugUrlSanitizer.sanitize(null))
        assertEquals("[地址解析失败]", WebDebugUrlSanitizer.sanitize("not a valid url with secret"))
    }
}
