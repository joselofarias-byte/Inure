package app.simple.inure.util

import app.simple.inure.BuildConfig

@Suppress("KotlinConstantConditions")
object AppUtils {
    fun isPlayFlavor(): Boolean = false
    fun isGithubFlavor(): Boolean = BuildConfig.FLAVOR == "joselofarias"
    fun isJoseloFariasFlavor(): Boolean = BuildConfig.FLAVOR == "joselofarias"
    fun isBetaFlavor(): Boolean = BuildConfig.FLAVOR == "beta"
    fun isDebug(): Boolean = BuildConfig.DEBUG
}
