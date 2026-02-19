package app.aaps.core.interfaces.overview.graph

data class NsClientStatusData(
    val pump: NsClientStatusItem? = null,
    val openAps: NsClientStatusItem? = null,
    val uploader: NsClientStatusItem? = null
)

data class NsClientStatusItem(
    val label: String,
    val value: String,
    val level: NsClientLevel,
    val dialogTitle: String,
    val dialogText: String
)

enum class NsClientLevel {
    INFO,
    WARN,
    URGENT
}
