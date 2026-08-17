package ci.us.bd2.tokenhelper.model

import ci.us.bd2.tokenhelper.web.WebDebugLogStore
import ci.us.bd2.tokenhelper.web.WebDebugSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebDebugLogStoreTest {
    @Test
    fun ignoresEventsUntilDebugModeIsEnabled() {
        val store = WebDebugLogStore(timestampProvider = { "12:00:00.000" })

        store.record(WebDebugSource.MAIN, "页面开始", "https://example.com/?token=secret")

        assertFalse(store.enabled.value)
        assertTrue(store.entries.value.isEmpty())
    }

    @Test
    fun storesSanitizedEventsAndExportsReadableText() {
        val store = WebDebugLogStore(timestampProvider = { "12:00:00.000" })
        store.setEnabled(true)

        store.record(
            source = WebDebugSource.POPUP,
            action = "导航已阻止",
            rawUrl = "https://accounts.example/path?code=secret",
            detail = "主框架=true",
        )

        val exported = store.exportText()
        assertEquals(2, store.entries.value.size)
        assertTrue(exported.contains("[登录弹窗] 导航已阻止"))
        assertTrue(exported.contains("https://accounts.example"))
        assertFalse(exported.contains("/path"))
        assertFalse(exported.contains("secret"))
        assertTrue(exported.contains("主框架=true"))
    }

    @Test
    fun retainsOnlyNewestEntriesAndCanClearThem() {
        val store = WebDebugLogStore(
            maxEntries = 3,
            timestampProvider = { "12:00:00.000" },
        )
        store.setEnabled(true)

        repeat(4) { index ->
            store.record(WebDebugSource.SYSTEM, "事件$index")
        }

        assertEquals(3, store.entries.value.size)
        assertFalse(store.exportText().contains("调试模式已开启"))
        assertFalse(store.exportText().contains("事件0"))
        assertTrue(store.exportText().contains("事件3"))

        store.clear()
        assertTrue(store.entries.value.isEmpty())
    }

    @Test
    fun disablingStopsNewEventsButKeepsExistingLog() {
        val store = WebDebugLogStore(timestampProvider = { "12:00:00.000" })
        store.setEnabled(true)
        store.record(WebDebugSource.MAIN, "页面开始")

        store.setEnabled(false)
        val sizeAfterDisable = store.entries.value.size
        store.record(WebDebugSource.MAIN, "不应记录")

        assertFalse(store.enabled.value)
        assertEquals(sizeAfterDisable, store.entries.value.size)
        assertFalse(store.exportText().contains("不应记录"))
    }

    @Test
    fun defaultCapacityRetainsTwoHundredNewestEntries() {
        val store = WebDebugLogStore(timestampProvider = { "12:00:00.000" })
        store.setEnabled(true)

        repeat(201) { index -> store.record(WebDebugSource.SYSTEM, "事件$index") }

        assertEquals(200, store.entries.value.size)
        assertFalse(store.exportText().contains("事件0\n"))
        assertTrue(store.exportText().contains("事件200"))
    }
}
