package app.simple.inure.preferences

import android.os.Environment
import app.simple.inure.constants.SortConstant
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.SortApks
import java.io.File
import java.util.LinkedHashSet

object ApkBrowserPreferences {

    private const val CUSTOM_APK_PATHS = "custom_apk_paths"
    private const val DOWNLOADS_ENABLED = "apk_downloads_enabled"
    private const val SOURCE_MODEL_MIGRATED = "apk_source_model_migrated_v2"


    const val LOAD_SPLIT_ICON = "load_split_icon"
    const val SORT_STYLE = "apk_sort_style"
    const val REVERSED = "apk_sort_reversed"
    const val APK_FILTER = "apk_filter"
    const val SCAN_FOLDERS = "apk_scan_folders"

    fun isLoadSplitIcon(): Boolean = getSharedPreferences().getBoolean(LOAD_SPLIT_ICON, false)
    fun setLoadSplitIcon(value: Boolean) = getSharedPreferences().edit().putBoolean(LOAD_SPLIT_ICON, value).apply()

    fun getSortStyle(): String = getSharedPreferences().getString(SORT_STYLE, SortApks.NAME)!!
    fun setSortStyle(category: String) = getSharedPreferences().edit().putString(SORT_STYLE, category).apply()

    fun isReverseSorting(): Boolean = getSharedPreferences().getBoolean(REVERSED, false)
    fun setReverseSorting(value: Boolean) = getSharedPreferences().edit().putBoolean(REVERSED, value).apply()

    fun getApkFilter(): Int = getSharedPreferences().getInt(APK_FILTER, SortConstant.ALL_APKS)
    fun setApkFilter(filter: Int) = getSharedPreferences().edit().putInt(APK_FILTER, filter).apply()





    private fun joseloDownloadsDirectory(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }

    private fun joseloNormalizePath(path: String): String {
        return runCatching { File(path).canonicalPath }.getOrElse { File(path).absolutePath }
    }

    private fun joseloIsDownloadsPath(path: String): Boolean {
        return joseloNormalizePath(path) == joseloNormalizePath(joseloDownloadsDirectory().absolutePath)
    }

    private fun migrateLegacyScanSources() {
        val preferences = getSharedPreferences()
        if (preferences.getBoolean(SOURCE_MODEL_MIGRATED, false)) return

        val legacy = preferences.getStringSet(SCAN_FOLDERS, emptySet())?.toSet().orEmpty()
        val cleaned: MutableSet<String> = LinkedHashSet()
        for (item in legacy) {
            val norm = joseloNormalizePath(item)
            if (!joseloIsDownloadsPath(norm)) {
                cleaned.add(norm)
            }
        }

        preferences.edit()
            .putStringSet(CUSTOM_APK_PATHS, cleaned)
            .putBoolean(DOWNLOADS_ENABLED, true)
            .putBoolean(SOURCE_MODEL_MIGRATED, true)
            .apply()
    }

    fun isDownloadsEnabled(): Boolean {
        migrateLegacyScanSources()
        return getSharedPreferences().getBoolean(DOWNLOADS_ENABLED, true)
    }

    fun setDownloadsEnabled(enabled: Boolean) {
        getSharedPreferences().edit()
            .putBoolean(DOWNLOADS_ENABLED, enabled)
            .putBoolean(SOURCE_MODEL_MIGRATED, true)
            .apply()
    }

    fun getCustomApkPaths(): Set<String> {
        migrateLegacyScanSources()
        val stored = getSharedPreferences().getStringSet(CUSTOM_APK_PATHS, emptySet())?.toSet().orEmpty()
        val result: MutableSet<String> = LinkedHashSet()
        for (item in stored) {
            val norm = joseloNormalizePath(item)
            if (!joseloIsDownloadsPath(norm)) {
                result.add(norm)
            }
        }
        return result
    }

    fun setCustomApkPaths(paths: Set<String>) {
        var containsDownloads = false
        val cleaned: MutableSet<String> = LinkedHashSet()
        for (item in paths) {
            if (joseloIsDownloadsPath(item)) {
                containsDownloads = true
            } else {
                cleaned.add(joseloNormalizePath(item))
            }
        }

        val editor = getSharedPreferences().edit()
            .putStringSet(CUSTOM_APK_PATHS, cleaned)
            .putBoolean(SOURCE_MODEL_MIGRATED, true)

        if (containsDownloads) {
            editor.putBoolean(DOWNLOADS_ENABLED, true)
        }

        editor.apply()
    }

    fun addCustomApkPath(path: String) {
        if (joseloIsDownloadsPath(path)) {
            setDownloadsEnabled(true)
            return
        }

        val updated: MutableSet<String> = getCustomApkPaths().toMutableSet()
        updated.add(joseloNormalizePath(path))
        setCustomApkPaths(updated)
    }

    fun removeCustomApkPath(path: String) {
        if (joseloIsDownloadsPath(path)) {
            setDownloadsEnabled(false)
            return
        }

        val normalized = joseloNormalizePath(path)
        val updated: MutableSet<String> = getCustomApkPaths().toMutableSet()
        updated.removeAll { joseloNormalizePath(it) == normalized }
        setCustomApkPaths(updated)
    }

    fun addScanFolder(path: String): Boolean {
        if (joseloIsDownloadsPath(path)) {
            setDownloadsEnabled(true)
            return true
        }

        val normalized = joseloNormalizePath(path)
        val current = getCustomApkPaths()
        if (current.contains(normalized)) return false

        val updated: MutableSet<String> = current.toMutableSet()
        val changed = updated.add(normalized)
        if (changed) setCustomApkPaths(updated)
        return changed
    }

    fun removeScanFolder(path: String): Boolean {
        if (joseloIsDownloadsPath(path)) {
            setDownloadsEnabled(false)
            return true
        }

        val normalized = joseloNormalizePath(path)
        val current = getCustomApkPaths()
        if (!current.contains(normalized)) return false

        val updated: MutableSet<String> = current.toMutableSet()
        val changed = updated.remove(normalized)
        if (changed) setCustomApkPaths(updated)
        return changed
    }

    fun clearScanFolders() {
        setCustomApkPaths(emptySet())
    }

    fun isUsingCustomPaths(): Boolean {
        return isDownloadsEnabled() || getCustomApkPaths().isNotEmpty()
    }

    fun getScanFolders(): LinkedHashSet<String> {
        migrateLegacyScanSources()

        val folders: LinkedHashSet<String> = LinkedHashSet()

        if (isDownloadsEnabled()) {
            folders.add(joseloNormalizePath(joseloDownloadsDirectory().absolutePath))
        }

        folders.addAll(getCustomApkPaths())
        return folders
    }

}