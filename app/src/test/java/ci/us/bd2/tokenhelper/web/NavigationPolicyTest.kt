package ci.us.bd2.tokenhelper.web

import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationPolicyTest {
    @Test
    fun allowsExactOfficialHttpsHost() {
        assertEquals(
            NavigationDecision.ALLOW_MAIN,
            NavigationPolicy.decide("https://webshop.browndust2.global/en-US/"),
        )
    }

    @Test
    fun rejectsCleartextAndMalformedUrls() {
        assertEquals(
            NavigationDecision.REJECT,
            NavigationPolicy.decide("http://webshop.browndust2.global/"),
        )
        assertEquals(NavigationDecision.REJECT, NavigationPolicy.decide("not a url"))
    }

    @Test
    fun doesNotTrustMaliciousHostSuffix() {
        assertEquals(
            NavigationDecision.OPEN_EXTERNAL,
            NavigationPolicy.decide("https://webshop.browndust2.global.evil.test/"),
        )
    }

    @Test
    fun opensOtherHttpsLinksExternally() {
        assertEquals(
            NavigationDecision.OPEN_EXTERNAL,
            NavigationPolicy.decide("https://example.com/help"),
        )
    }
}
