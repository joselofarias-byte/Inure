package app.simple.inure.updatehistory.model

data class DeltaChange(
    val category: DeltaCategory,
    val item: String,
    val kind: DeltaKind,
    val severity: DeltaSeverity,
    val previousValue: String? = null,
    val currentValue: String? = null
)

enum class DeltaKind { ADDED, REMOVED, CHANGED }

enum class DeltaCategory {
    VERSION,
    TARGET_SDK,
    SIGNATURE,
    REQUESTED_PERMISSION,
    GRANTED_PERMISSION,
    APP_OP,
    EXPORTED_ACTIVITY,
    EXPORTED_SERVICE,
    EXPORTED_RECEIVER,
    EXPORTED_PROVIDER,
    TRACKER,
    DOMAIN,
    NATIVE_LIBRARY
}

data class AppDelta(
    val packageName: String,
    val fromVersionCode: Long,
    val toVersionCode: Long,
    val generatedAt: Long,
    val changes: List<DeltaChange>
) {
    val highestSeverity: DeltaSeverity
        get() = DeltaSeverity.highest(changes.map { it.severity })

    val hasSecurityRelevantChanges: Boolean
        get() = changes.any { it.severity.rank >= DeltaSeverity.HIGH.rank }

    val isEmpty: Boolean
        get() = changes.isEmpty()
}
