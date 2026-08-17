package ci.us.bd2.tokenhelper.web

object TokenCaptureCapability {
    fun isSupported(
        webMessageListenerSupported: Boolean,
        documentStartScriptSupported: Boolean,
    ): Boolean = webMessageListenerSupported && documentStartScriptSupported
}
