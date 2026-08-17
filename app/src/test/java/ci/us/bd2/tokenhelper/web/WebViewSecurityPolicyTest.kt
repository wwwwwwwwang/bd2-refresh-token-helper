package ci.us.bd2.tokenhelper.web

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewSecurityPolicyTest {
    @Test
    fun mainFrameOnlyAllowsOfficialOrigin() {
        assertTrue(WebViewSecurityPolicy.allowsMainFrame("https://webshop.browndust2.global/en-US"))
        assertFalse(WebViewSecurityPolicy.allowsMainFrame("https://evil.example/login"))
        assertFalse(WebViewSecurityPolicy.allowsMainFrame("https://webshop.browndust2.global.evil.example"))
        assertFalse(WebViewSecurityPolicy.allowsMainFrame("http://webshop.browndust2.global"))
    }

    @Test
    fun popupRequiresUserGesture() {
        assertTrue(WebViewSecurityPolicy.allowsPopupCreation(isUserGesture = true))
        assertFalse(WebViewSecurityPolicy.allowsPopupCreation(isUserGesture = false))
    }

    @Test
    fun popupOnlyAllowsOfficialAuthenticationHosts() {
        assertTrue(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.google.com/o/oauth2/v2/auth"))
        assertTrue(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.google.de/signin/v2/challenge"))
        assertTrue(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.google.co.jp/signin/v2/challenge"))
        assertTrue(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.google.com.sg/signin/v2/challenge"))
        assertTrue(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.youtube.com/accounts/SetSID"))
        assertTrue(WebViewSecurityPolicy.allowsPopupNavigation("https://appleid.apple.com/auth/authorize"))
        assertTrue(WebViewSecurityPolicy.allowsPopupNavigation("https://webshop.browndust2.global/en-US"))
        assertTrue(
            WebViewSecurityPolicy.allowsPopupNavigation(
                "https://signin.browndust2.global/__/auth/handler?state=test",
            ),
        )
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.google.com.evil.example"))
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.google.co.jp.evil.example"))
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.google.com.sg.evil.example"))
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.google.example.com"))
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts-google.com.sg"))
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("https://accounts.youtube.com.evil.example"))
        assertFalse(
            WebViewSecurityPolicy.allowsPopupNavigation(
                "https://signin.browndust2.global.evil.example/__/auth/handler",
            ),
        )
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("https://evil.example/login"))
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("http://accounts.google.com"))
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("http://accounts.google.de"))
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("http://accounts.google.com.sg"))
        assertFalse(WebViewSecurityPolicy.allowsPopupNavigation("http://accounts.youtube.com"))
    }

    @Test
    fun popupOnlyBlocksUnknownMainFrameNavigation() {
        assertFalse(
            WebViewSecurityPolicy.shouldBlockPopupRequest(
                "https://accounts.google.com.sg/signin/v2/challenge",
                isForMainFrame = true,
            ),
        )
        assertFalse(
            WebViewSecurityPolicy.shouldBlockPopupRequest(
                "https://accounts.google.com/o/oauth2/v2/auth",
                isForMainFrame = true,
            ),
        )
        assertTrue(
            WebViewSecurityPolicy.shouldBlockPopupRequest(
                "https://accounts.googleusercontent.com/embedded",
                isForMainFrame = true,
            ),
        )
        assertFalse(
            WebViewSecurityPolicy.shouldBlockPopupRequest(
                "https://accounts.googleusercontent.com/embedded",
                isForMainFrame = false,
            ),
        )
    }
}
