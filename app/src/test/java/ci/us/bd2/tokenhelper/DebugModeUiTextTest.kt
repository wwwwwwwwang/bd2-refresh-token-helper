package ci.us.bd2.tokenhelper

import org.junit.Assert.assertEquals
import org.junit.Test

class DebugModeUiTextTest {
    @Test
    fun actionLabelDescribesTheNextOperation() {
        assertEquals("开启调试模式", debugModeActionLabel(enabled = false))
        assertEquals("关闭调试模式", debugModeActionLabel(enabled = true))
    }

    @Test
    fun changedMessageDescribesTheNewState() {
        assertEquals("调试模式已开启", debugModeChangedMessage(enabled = true))
        assertEquals("调试模式已关闭", debugModeChangedMessage(enabled = false))
    }
}
