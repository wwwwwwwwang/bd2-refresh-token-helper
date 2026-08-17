package ci.us.bd2.tokenhelper.web

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class WebDebugSource(val label: String) {
    MAIN("主页面"),
    POPUP("登录弹窗"),
    SYSTEM("系统"),
}

data class WebDebugEntry(
    val timestamp: String,
    val source: WebDebugSource,
    val action: String,
    val url: String?,
    val detail: String?,
) {
    fun render(): String = buildString {
        append(timestamp)
        append(" [")
        append(source.label)
        append("] ")
        append(action)
        if (url != null) {
            append(" | ")
            append(url)
        }
        if (!detail.isNullOrBlank()) {
            append(" | ")
            append(detail)
        }
    }
}

class WebDebugLogStore(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val timestampProvider: () -> String = DEFAULT_TIMESTAMP_PROVIDER,
) {
    private val mutableEnabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = mutableEnabled.asStateFlow()

    private val mutableEntries = MutableStateFlow<List<WebDebugEntry>>(emptyList())
    val entries: StateFlow<List<WebDebugEntry>> = mutableEntries.asStateFlow()

    @Synchronized
    fun setEnabled(value: Boolean) {
        if (mutableEnabled.value == value) return
        if (value) {
            mutableEnabled.value = true
            record(WebDebugSource.SYSTEM, "调试模式已开启")
        } else {
            record(WebDebugSource.SYSTEM, "调试模式已关闭")
            mutableEnabled.value = false
        }
    }

    @Synchronized
    fun record(
        source: WebDebugSource,
        action: String,
        rawUrl: String? = null,
        detail: String? = null,
    ) {
        if (!mutableEnabled.value) return
        val entry = WebDebugEntry(
            timestamp = timestampProvider(),
            source = source,
            action = action,
            url = rawUrl?.let(WebDebugUrlSanitizer::sanitize),
            detail = detail,
        )
        mutableEntries.value = (mutableEntries.value + entry).takeLast(maxEntries.coerceAtLeast(1))
    }

    fun exportText(): String = entries.value.joinToString(separator = "\n", transform = WebDebugEntry::render)

    fun clear() {
        mutableEntries.value = emptyList()
    }

    private companion object {
        const val DEFAULT_MAX_ENTRIES = 200
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        val DEFAULT_TIMESTAMP_PROVIDER: () -> String = { LocalTime.now().format(TIME_FORMATTER) }
    }
}
