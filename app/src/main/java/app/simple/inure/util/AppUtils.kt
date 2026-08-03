package app.simple.inure.util

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import app.simple.inure.BuildConfig
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo

@Suppress("KotlinConstantConditions")
object AppUtils {

    const val UNLOCKER_PACKAGE_NAME = "app.simple.inureunlocker"
    const val RECEIVER_PACKAGE_NAME = "$UNLOCKER_PACKAGE_NAME.receivers.LicenceVerificationReceiver"

    /** The Play flavor was removed from the JoseloFarias fork. */
    fun isPlayFlavor(): Boolean = false

    /**
     * Returns true for the JoseloFarias fork flavor
     */
    fun isJoseloFariasFlavor(): Boolean {
        return BuildConfig.FLAVOR == "joselofarias"
    }

    /**
     * Returns true if the flavor is beta
     */
    fun isBetaFlavor(): Boolean {
        return BuildConfig.FLAVOR == "beta"
    }

    /**
     * Returns true if DEBUG
     */
    fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }

    /**
     * Returns true if the package name is the unlocker package name
     */
    fun PackageInfo.isUnlocker(): Boolean {
        return packageName == UNLOCKER_PACKAGE_NAME
    }

    fun PackageInfo.isNewerUnlocker(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode >= 13L
        } else {
            @Suppress("DEPRECATION")
            versionCode >= 13
        }
    }

    fun Context.isNewerUnlocker(): Boolean {
        return packageManager.getPackageInfo(UNLOCKER_PACKAGE_NAME)?.isNewerUnlocker() ?: false
    }
}
