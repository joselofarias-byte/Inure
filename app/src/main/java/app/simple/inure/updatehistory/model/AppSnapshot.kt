package app.simple.inure.updatehistory.model

data class AppSnapshot(
    val packageName: String,
    val versionCode: Long,
    val versionName: String?,
    val targetSdk: Int,
    val capturedAt: Long,
    val signingCertificateSha256: String?,
    val requestedPermissions: Set<String>,
    val grantedPermissions: Set<String>,
    val appOps: Map<String, Int>,
    val exportedActivities: Set<String>,
    val exportedServices: Set<String>,
    val exportedReceivers: Set<String>,
    val exportedProviders: Set<String>,
    val trackers: Set<String>,
    val domains: Set<String>,
    val nativeLibraries: Set<String>
) {
    init {
        require(packageName.isNotBlank()) { "packageName must not be blank" }
        require(versionCode >= 0L) { "versionCode must not be negative" }
        require(targetSdk >= 0) { "targetSdk must not be negative" }
        require(capturedAt >= 0L) { "capturedAt must not be negative" }
    }
}
