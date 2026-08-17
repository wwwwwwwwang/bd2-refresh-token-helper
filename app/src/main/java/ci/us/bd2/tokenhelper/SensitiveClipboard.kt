package ci.us.bd2.tokenhelper

import android.content.ClipData
import android.os.PersistableBundle

object SensitiveClipboard {
    private const val SENSITIVE_KEY = "android.content.extra.IS_SENSITIVE"

    fun create(token: String): ClipData =
        ClipData.newPlainText("BD2 Refresh Token", token).apply {
            description.extras = PersistableBundle().apply {
                putBoolean(SENSITIVE_KEY, true)
            }
        }
}
