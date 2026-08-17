package ci.us.bd2.tokenhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import ci.us.bd2.tokenhelper.model.TokenCaptureStore

class MainActivity : ComponentActivity() {
    private val tokenStore = TokenCaptureStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TokenHelperApp(
                    store = tokenStore,
                    window = window,
                    onExit = ::finish,
                )
            }
        }
    }

    override fun onDestroy() {
        tokenStore.clear()
        super.onDestroy()
    }
}
