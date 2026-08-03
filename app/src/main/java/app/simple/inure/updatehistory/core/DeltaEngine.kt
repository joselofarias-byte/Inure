package app.simple.inure.updatehistory.core

import app.simple.inure.updatehistory.model.AppDelta
import app.simple.inure.updatehistory.model.AppSnapshot
import app.simple.inure.updatehistory.model.DeltaCategory
import app.simple.inure.updatehistory.model.DeltaChange
import app.simple.inure.updatehistory.model.DeltaKind
import app.simple.inure.updatehistory.model.DeltaSeverity

object DeltaEngine {
    private val criticalPermissionTokens = setOf(
        "READ_SMS", "RECEIVE_SMS", "SEND_SMS", "READ_CONTACTS",
        "WRITE_CONTACTS", "BIND_ACCESSIBILITY_SERVICE"
    )

    private val highPermissionTokens = setOf(
        "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
        "ACCESS_BACKGROUND_LOCATION", "RECORD_AUDIO", "CAMERA",
        "READ_CALL_LOG", "WRITE_CALL_LOG"
    )

    fun compare(
        previous: AppSnapshot,
        current: AppSnapshot,
        generatedAt: Long = System.currentTimeMillis()
    ): AppDelta {
        require(previous.packageName == current.packageName) {
            "Snapshots must belong to the same package"
        }

        val changes = buildList {
            if (previous.versionCode != current.versionCode || previous.versionName != current.versionName) {
                add(
                    DeltaChange(
                        category = DeltaCategory.VERSION,
                        item = current.packageName,
                        kind = DeltaKind.CHANGED,
                        severity = DeltaSeverity.INFO,
                        previousValue = versionLabel(previous),
                        currentValue = versionLabel(current)
                    )
                )
            }

            if (previous.targetSdk != current.targetSdk) {
                add(
                    DeltaChange(
                        category = DeltaCategory.TARGET_SDK,
                        item = "targetSdk",
                        kind = DeltaKind.CHANGED,
                        severity = DeltaSeverity.LOW,
                        previousValue = previous.targetSdk.toString(),
                        currentValue = current.targetSdk.toString()
                    )
                )
            }

            if (previous.signingCertificateSha256 != current.signingCertificateSha256) {
                add(
                    DeltaChange(
                        category = DeltaCategory.SIGNATURE,
                        item = "SHA-256",
                        kind = DeltaKind.CHANGED,
                        severity = DeltaSeverity.CRITICAL,
                        previousValue = previous.signingCertificateSha256,
                        currentValue = current.signingCertificateSha256
                    )
                )
            }

            addAll(setChanges(previous.requestedPermissions, current.requestedPermissions, DeltaCategory.REQUESTED_PERMISSION, ::permissionSeverity))
            addAll(setChanges(previous.grantedPermissions, current.grantedPermissions, DeltaCategory.GRANTED_PERMISSION, ::permissionSeverity))
            addAll(mapChanges(previous.appOps, current.appOps, DeltaCategory.APP_OP, DeltaSeverity.MEDIUM))
            addAll(setChanges(previous.exportedActivities, current.exportedActivities, DeltaCategory.EXPORTED_ACTIVITY) { DeltaSeverity.CRITICAL })
            addAll(setChanges(previous.exportedServices, current.exportedServices, DeltaCategory.EXPORTED_SERVICE) { DeltaSeverity.CRITICAL })
            addAll(setChanges(previous.exportedReceivers, current.exportedReceivers, DeltaCategory.EXPORTED_RECEIVER) { DeltaSeverity.CRITICAL })
            addAll(setChanges(previous.exportedProviders, current.exportedProviders, DeltaCategory.EXPORTED_PROVIDER) { DeltaSeverity.CRITICAL })
            addAll(setChanges(previous.trackers, current.trackers, DeltaCategory.TRACKER) { DeltaSeverity.HIGH })
            addAll(setChanges(previous.domains, current.domains, DeltaCategory.DOMAIN) { DeltaSeverity.HIGH })
            addAll(setChanges(previous.nativeLibraries, current.nativeLibraries, DeltaCategory.NATIVE_LIBRARY) { DeltaSeverity.HIGH })
        }.sortedWith(
            compareByDescending<DeltaChange> { it.severity.rank }
                .thenBy { it.category.name }
                .thenBy { it.item }
        )

        return AppDelta(
            packageName = current.packageName,
            fromVersionCode = previous.versionCode,
            toVersionCode = current.versionCode,
            generatedAt = generatedAt,
            changes = changes
        )
    }

    private fun versionLabel(snapshot: AppSnapshot): String {
        return "${snapshot.versionName ?: "unknown"} (${snapshot.versionCode})"
    }

    private fun permissionSeverity(permission: String): DeltaSeverity {
        val normalized = permission.uppercase()
        return when {
            criticalPermissionTokens.any(normalized::contains) -> DeltaSeverity.CRITICAL
            highPermissionTokens.any(normalized::contains) -> DeltaSeverity.HIGH
            else -> DeltaSeverity.MEDIUM
        }
    }

    private fun setChanges(
        previous: Set<String>,
        current: Set<String>,
        category: DeltaCategory,
        severity: (String) -> DeltaSeverity
    ): List<DeltaChange> {
        val added = (current - previous).map {
            DeltaChange(category, it, DeltaKind.ADDED, severity(it))
        }
        val removed = (previous - current).map {
            DeltaChange(category, it, DeltaKind.REMOVED, DeltaSeverity.INFO)
        }
        return added + removed
    }

    private fun mapChanges(
        previous: Map<String, Int>,
        current: Map<String, Int>,
        category: DeltaCategory,
        changedSeverity: DeltaSeverity
    ): List<DeltaChange> {
        val keys = previous.keys + current.keys
        return keys.mapNotNull { key ->
            val old = previous[key]
            val new = current[key]
            when {
                old == null && new != null -> DeltaChange(category, key, DeltaKind.ADDED, changedSeverity, currentValue = new.toString())
                old != null && new == null -> DeltaChange(category, key, DeltaKind.REMOVED, DeltaSeverity.INFO, previousValue = old.toString())
                old != new -> DeltaChange(category, key, DeltaKind.CHANGED, changedSeverity, old.toString(), new.toString())
                else -> null
            }
        }
    }
}
