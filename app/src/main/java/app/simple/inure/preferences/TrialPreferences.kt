package app.simple.inure.preferences

import android.annotation.SuppressLint
import app.simple.inure.BuildConfig
import app.simple.inure.util.AppUtils
import app.simple.inure.util.CalendarUtils
import java.util.Date

@Suppress("NOTHING_TO_INLINE", "UseKtx")
object TrialPreferences {

    const val MAX_TRIAL_DAYS = 0xF

    private const val FIRST_LAUNCH = "first_launch_"
    const val IS_APP_FULL_VERSION_ENABLED = "is_full_version_"
    private const val IS_LEGACY_MIGRATED = "is_legacy_migrated_"
    private const val IS_UNLOCKER_VERIFICATION_REQUIRED = "is_unlocker_verification_required_"
    private const val LAST_VERIFICATION_DATE = "last_verification_date_"

    const val HAS_LICENSE_KEY = "has_license_key"

    // ---------------------------------------------------------------------------------------------------------- //

    @SuppressLint("UseKtx")
    fun setFirstLaunchDate(date: Long) {
        SharedPreferences.getEncryptedSharedPreferences().edit().putLong(FIRST_LAUNCH, date).apply()
    }

    fun getFirstLaunchDate(): Long {
        return SharedPreferences.getEncryptedSharedPreferences().getLong(FIRST_LAUNCH, -1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun getDaysLeft(): Int {
        if (BuildConfig.JOSELOFARIAS_UNLOCKED) return 0
        return kotlin.runCatching {
            MAX_TRIAL_DAYS - CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday())
                .coerceAtLeast(0).coerceAtMost(MAX_TRIAL_DAYS)
        }.getOrElse {
            -1
        }
    }

    fun getMaxDays(): Int {
        return MAX_TRIAL_DAYS
    }

    // ---------------------------------------------------------------------------------------------------------- //


    fun setFullVersion(value: Boolean): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().edit()
            .putBoolean(IS_APP_FULL_VERSION_ENABLED, value || BuildConfig.JOSELOFARIAS_UNLOCKED).commit()
    }

    inline fun isAppFullVersionEnabled(): Boolean {
        if (BuildConfig.JOSELOFARIAS_UNLOCKED) return true
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(IS_APP_FULL_VERSION_ENABLED, false) ||
                CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday()) <= MAX_TRIAL_DAYS
    }

    fun isWithinTrialPeriod(): Boolean {
        if (BuildConfig.JOSELOFARIAS_UNLOCKED) return true
        return CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday()) <= MAX_TRIAL_DAYS
    }

    fun isTrialWithoutFull(): Boolean {
        if (BuildConfig.JOSELOFARIAS_UNLOCKED) return false
        return CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday()) <= MAX_TRIAL_DAYS
                && !isAppFullVersionEnabled()
    }

    fun isFullVersion(): Boolean {
        return BuildConfig.JOSELOFARIAS_UNLOCKED ||
                SharedPreferences.getEncryptedSharedPreferences().getBoolean(IS_APP_FULL_VERSION_ENABLED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun reset() {
        setFirstLaunchDate(-1)
        setFullVersion(BuildConfig.JOSELOFARIAS_UNLOCKED)
    }

    fun migrateLegacy() {
        if (!isLegacyMigrated()) {
            setFirstLaunchDate(MainPreferences.getFirstLaunchDateLegacy())
            setFullVersion(MainPreferences.isFullVersionEnabledLegacy())

            setLegacyMigrated(true)
            MainPreferences.removeLegacyPreferences()
        }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLegacyMigrated(value: Boolean) {
        SharedPreferences.getEncryptedSharedPreferences().edit().putBoolean(IS_LEGACY_MIGRATED, value).apply()
    }

    private fun isLegacyMigrated(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(IS_LEGACY_MIGRATED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHasLicenceKey(hasLicence: Boolean) {
        SharedPreferences.getEncryptedSharedPreferences().edit()
            .putBoolean(HAS_LICENSE_KEY, hasLicence || BuildConfig.JOSELOFARIAS_UNLOCKED).apply()
    }

    fun hasLicenceKey(): Boolean {
        return BuildConfig.JOSELOFARIAS_UNLOCKED ||
                SharedPreferences.getEncryptedSharedPreferences().getBoolean(HAS_LICENSE_KEY, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUnlockerVerificationRequired(value: Boolean): Boolean {
        val effectiveValue = if (BuildConfig.JOSELOFARIAS_UNLOCKED) false else value
        return SharedPreferences.getEncryptedSharedPreferences().edit()
            .putBoolean(IS_UNLOCKER_VERIFICATION_REQUIRED, effectiveValue).commit()
    }

    fun isUnlockerVerificationRequired(): Boolean {
        if (BuildConfig.JOSELOFARIAS_UNLOCKED) return false
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(IS_UNLOCKER_VERIFICATION_REQUIRED, true) || AppUtils.isPlayFlavor()
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastVerificationDate(date: Long) {
        SharedPreferences.getEncryptedSharedPreferences().edit().putLong(LAST_VERIFICATION_DATE, date).apply()
    }

    fun getLastVerificationDate(): Long {
        return SharedPreferences.getEncryptedSharedPreferences().getLong(LAST_VERIFICATION_DATE, -1L)
    }
}
