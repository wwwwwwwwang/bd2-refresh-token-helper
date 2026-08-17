package ci.us.bd2.tokenhelper

import android.content.ClipDescription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class SensitiveClipboardTest {
    @Test
    @Config(sdk = [26])
    fun marksTokenAsSensitiveOnLegacyAndroid() {
        val clip = SensitiveClipboard.create("refresh-token")

        assertEquals("refresh-token", clip.getItemAt(0).text.toString())
        assertTrue(clip.description.extras?.getBoolean(SENSITIVE_KEY) == true)
    }

    @Test
    @Config(sdk = [33])
    fun marksTokenAsSensitiveOnAndroid13() {
        val clip = SensitiveClipboard.create("refresh-token")

        assertTrue(clip.description.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE) == true)
    }

    private companion object {
        const val SENSITIVE_KEY = "android.content.extra.IS_SENSITIVE"
    }
}
