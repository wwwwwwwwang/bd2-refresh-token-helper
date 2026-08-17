package ci.us.bd2.tokenhelper.web

object TokenValidator {
    private const val MIN_LENGTH = 80
    private const val MAX_LENGTH = 4096
    private val allowedCharacters = Regex("^[A-Za-z0-9._~-]+$")

    fun isValid(value: String): Boolean =
        value.length in MIN_LENGTH..MAX_LENGTH && allowedCharacters.matches(value)
}
