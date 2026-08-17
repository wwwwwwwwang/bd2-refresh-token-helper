package ci.us.bd2.tokenhelper.web

object TokenInterceptorScript {
    val source: String =
        """
        (() => {
          if (window.__bd2TokenInterceptorInstalled) return;
          window.__bd2TokenInterceptorInstalled = true;

          const originalFetch = window.fetch;
          window.fetch = async function(...args) {
            const response = await originalFetch.apply(this, args);
            const requestUrl = typeof args[0] === 'string' ? args[0] : args[0]?.url || '';

            if (response.ok && requestUrl.includes('accounts:signInWithIdp')) {
              response.clone().json().then((payload) => {
                if (typeof payload?.refreshToken === 'string') {
                  window.Bd2TokenBridge?.postMessage(payload.refreshToken);
                }
              }).catch(() => {});
            }

            return response;
          };
        })();
        """.trimIndent()
}
