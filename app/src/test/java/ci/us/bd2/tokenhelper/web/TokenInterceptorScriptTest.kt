package ci.us.bd2.tokenhelper.web

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenInterceptorScriptTest {
    private val script = TokenInterceptorScript.source

    @Test
    fun containsIdempotentInstallationMarker() {
        assertTrue(script.contains("__bd2TokenInterceptorInstalled"))
    }

    @Test
    fun onlyTargetsFirebaseIdentityProviderLogin() {
        assertTrue(script.contains("accounts:signInWithIdp"))
        assertFalse(script.contains("accounts:signInWithCustomToken"))
    }

    @Test
    fun postsRefreshTokenThroughRestrictedBridge() {
        assertTrue(script.contains("payload.refreshToken"))
        assertTrue(script.contains("Bd2TokenBridge?.postMessage"))
    }

    @Test
    fun doesNotPersistLogOrUploadToken() {
        assertFalse(script.contains("console.log"))
        assertFalse(script.contains("localStorage"))
        assertFalse(script.contains("ops.bd2.us.ci"))
    }
}
