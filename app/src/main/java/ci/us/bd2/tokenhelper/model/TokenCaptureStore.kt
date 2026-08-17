package ci.us.bd2.tokenhelper.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenCaptureStore {
    private val mutableToken = MutableStateFlow<String?>(null)

    val token: StateFlow<String?> = mutableToken.asStateFlow()

    fun capture(value: String): Boolean {
        if (mutableToken.value == value) return false
        mutableToken.value = value
        return true
    }

    fun clear() {
        mutableToken.value = null
    }
}
