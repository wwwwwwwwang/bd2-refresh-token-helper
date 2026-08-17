package ci.us.bd2.tokenhelper.web

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenCaptureCapabilityTest {
    @Test
    fun requiresWebMessageListenerAndDocumentStartScript() {
        assertTrue(TokenCaptureCapability.isSupported(true, true))
        assertFalse(TokenCaptureCapability.isSupported(false, true))
        assertFalse(TokenCaptureCapability.isSupported(true, false))
        assertFalse(TokenCaptureCapability.isSupported(false, false))
    }
}
