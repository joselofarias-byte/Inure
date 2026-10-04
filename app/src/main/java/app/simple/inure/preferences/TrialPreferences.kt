package app.simple.inure.preferences

/**
 * Compatibility API for upstream feature gates.
 * JoseloFarias is an independent GPL/FOSS build and is always feature-complete.
 */
@Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
object TrialPreferences {
    const val MAX_TRIAL_DAYS = 0
    const val IS_APP_FULL_VERSION_ENABLED = "is_full_version_"
    const val HAS_LICENSE_KEY = "has_license_key"

    fun setFirstLaunchDate(date: Long) = Unit
    fun getFirstLaunchDate(): Long = 0L
    fun getDaysLeft(): Int = 0
    fun getMaxDays(): Int = 0
    fun setFullVersion(value: Boolean): Boolean = true
    inline fun isAppFullVersionEnabled(): Boolean = true
    fun isWithinTrialPeriod(): Boolean = true
    fun isTrialWithoutFull(): Boolean = false
    fun isFullVersion(): Boolean = true
    fun reset() = Unit
    fun migrateLegacy() = Unit
    fun setLegacyMigrated(value: Boolean) = Unit
    fun setHasLicenceKey(hasLicence: Boolean) = Unit
    fun hasLicenceKey(): Boolean = true
    fun setUnlockerVerificationRequired(value: Boolean): Boolean = true
    fun isUnlockerVerificationRequired(): Boolean = false
    fun setLastVerificationDate(date: Long) = Unit
    fun getLastVerificationDate(): Long = Long.MAX_VALUE
}
