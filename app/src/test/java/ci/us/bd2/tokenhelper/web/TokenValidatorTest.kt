package ci.us.bd2.tokenhelper.web

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenValidatorTest {
    @Test
    fun rejectsBlankToken() {
        assertFalse(TokenValidator.isValid(""))
        assertFalse(TokenValidator.isValid("   "))
    }

    @Test
    fun rejectsTokenShorterThanMinimum() {
        assertFalse(TokenValidator.isValid("a".repeat(79)))
    }

    @Test
    fun rejectsTokenLongerThanMaximum() {
        assertFalse(TokenValidator.isValid("a".repeat(4097)))
    }

    @Test
    fun rejectsWhitespaceAndUnsupportedCharacters() {
        assertFalse(TokenValidator.isValid("a".repeat(80) + " token"))
        assertFalse(TokenValidator.isValid("a".repeat(80) + "+"))
    }

    @Test
    fun acceptsFirebaseStyleToken() {
        val token = "AMf-" + "aB0_-".repeat(30)

        assertTrue(TokenValidator.isValid(token))
    }
}
