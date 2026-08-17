package ci.us.bd2.tokenhelper.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenCaptureStoreTest {
    private val firstToken = "AMf-" + "a".repeat(100)
    private val secondToken = "AMf-" + "b".repeat(100)

    @Test
    fun storesFirstCapturedToken() {
        val store = TokenCaptureStore()

        assertTrue(store.capture(firstToken))
        assertEquals(firstToken, store.token.value)
    }

    @Test
    fun ignoresDuplicateToken() {
        val store = TokenCaptureStore()
        store.capture(firstToken)

        assertFalse(store.capture(firstToken))
        assertEquals(firstToken, store.token.value)
    }

    @Test
    fun replacesTokenWhenNewValueIsCaptured() {
        val store = TokenCaptureStore()
        store.capture(firstToken)

        assertTrue(store.capture(secondToken))
        assertEquals(secondToken, store.token.value)
    }

    @Test
    fun clearIsIdempotentAndRemovesToken() {
        val store = TokenCaptureStore()
        store.capture(firstToken)

        store.clear()
        store.clear()

        assertNull(store.token.value)
    }
}
