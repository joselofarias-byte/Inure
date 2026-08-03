package app.simple.inure.viewmodels.panels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.ApkFile
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.SortApks.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.io.File

class ApkBrowserViewModel(application: Application) : WrappedViewModel(application) {

    var keywords = ""

    private var files = ArrayList<ApkFile>()
    private var scanJob: Job? = null
    @Volatile private var scanCompleted = false

    private val pathData: MutableLiveData<ArrayList<ApkFile>> by lazy {
        MutableLiveData<ArrayList<ApkFile>>().also {
            loadApkPaths()
        }
    }

    private val searchData: MutableLiveData<ArrayList<ApkFile>> by lazy {
        MutableLiveData<ArrayList<ApkFile>>().also {
            search("")
        }
    }

    private val info: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getApkFiles(): LiveData<ArrayList<ApkFile>> {
        return pathData
    }

    fun getSearchResults(): LiveData<ArrayList<ApkFile>> {
        return searchData
    }

    fun getPathInfo(): LiveData<String> {
        return info
    }

    fun shouldShowLoader(): Boolean {
        return !scanCompleted
    }

    private fun loadApkPaths() {
        scanJob?.cancel()
        scanCompleted = false
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            val roots = ApkBrowserPreferences.getScanFolders()
                .map(::File)
                .filter { it.exists() && it.isDirectory && it.canRead() }
                .distinctBy { file ->
                    runCatching { file.canonicalPath }.getOrElse { file.absolutePath }
                }

            val apkPaths = ArrayList<ApkFile>()
            val seenPaths = HashSet<String>()
            files.clear()

            for (root in roots) {
                ensureActive()
                root.walkTopDown()
                    .onFail { file, error -> Log.w("ApkBrowserViewModel", "Cannot scan ${file.absolutePath}", error) }
                    .forEach { candidate ->
                        ensureActive()
                        info.postValue(if (candidate.isDirectory) candidate.absolutePath else candidate.parent.orEmpty())
                        if (!candidate.isFile) return@forEach

                        val extension = candidate.extension.lowercase()
                        if (extension !in setOf("apk", "apks", "apkm", "xapk")) return@forEach
                        val canonical = runCatching { candidate.canonicalPath }.getOrElse { candidate.absolutePath }
                        if (!seenPaths.add(canonical)) return@forEach

                        val apkFile = ApkFile(candidate)
                        files.add(apkFile)
                        when (extension) {
                            "apk" -> if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APK)) apkPaths.add(apkFile)
                            "apks" -> if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKS)) apkPaths.add(apkFile)
                            "apkm" -> if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKM)) apkPaths.add(apkFile)
                            "xapk" -> if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_XAPK)) apkPaths.add(apkFile)
                        }
                    }
            }

            if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_HIDDEN).invert()) {
                apkPaths.removeAll { file -> file.file.absolutePath.split("/").any { it.startsWith(".") } }
            }

            apkPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())
            scanCompleted = true
            pathData.postValue(apkPaths)
        }
    }

    fun refresh() {
        loadApkPaths()
    }

    /**
     * This is a terrible way to check if a file is a .nomedia file or directory
     */
    @Suppress("unused")
    private fun File.isNomediaFileOrDirectory(): Boolean {
        return if (isFile) {
            if (absolutePath.split("/").any { it.startsWith(".") }) {
                return true
            } else {
                if (name == ".nomedia") {
                    return true
                } else {
                    for (i in 0..parentFile?.listFiles()?.size!!.minus(1)) {
                        if (parentFile?.listFiles()?.get(i)?.name == ".nomedia") {
                            Log.d("File", "Found .nomedia file in ${parentFile?.listFiles()?.get(i)?.absolutePath}")
                            return true
                        }
                    }

                    return false
                }
            }
        } else if (isDirectory) {
            if (absolutePath.split("/").any { it.startsWith(".") }) {
                return true
            } else {
                listFiles()?.forEach {
                    if (it.name == ".nomedia") {
                        return true
                    }
                }
            }
            false
        } else {
            false
        }
    }

    fun filter() {
        viewModelScope.launch(Dispatchers.IO) {
            val filteredPaths = ArrayList<ApkFile>()

            files.forEach {
                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APK) && it.file.extension == "apk") {
                    filteredPaths.add(it)
                }

                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKS) && it.file.extension == "apks") {
                    filteredPaths.add(it)
                }

                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKM) && it.file.extension == "apkm") {
                    filteredPaths.add(it)
                }

                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_XAPK) && it.file.extension == "xapk") {
                    filteredPaths.add(it)
                }
            }

            if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_HIDDEN).invert()) {
                val mediaPaths = ArrayList<ApkFile>()

                for (file in filteredPaths) {
                    if (file.file.absolutePath.split("/").any { it.startsWith(".") }.invert()) {
                        mediaPaths.add(file)
                        Log.d("ApkBrowserViewModel", "loadApkPaths: ${file.file.absolutePath} : is not nomedia file")
                    }
                }

                filteredPaths.clear()

                for (file in mediaPaths) {
                    filteredPaths.add(file)
                }
            }

            filteredPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            pathData.postValue(filteredPaths.clone() as ArrayList<ApkFile>)
        }
    }

    fun sort() {
        viewModelScope.launch(Dispatchers.IO) {
            val sortedPaths = ArrayList<ApkFile>()

            pathData.value?.forEach {
                sortedPaths.add(it)
            }

            sortedPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            pathData.postValue(sortedPaths.clone() as ArrayList<ApkFile>)
        }
    }

    fun search(keyword: String) {
        keywords = keyword

        viewModelScope.launch(Dispatchers.IO) {
            val filteredPaths = ArrayList<ApkFile>()

            if (keyword.isEmpty()) {
                @Suppress("UNCHECKED_CAST")
                searchData.postValue(filteredPaths.clone() as ArrayList<ApkFile>)
                return@launch
            }

            if (keyword.startsWith("$")) {
                when (keyword.lowercase()) {
                    "\$apk" -> {
                        files.forEach {
                            if (it.file.extension == "apk") {
                                filteredPaths.add(it)
                            }
                        }
                    }

                    "\$apks" -> {
                        files.forEach {
                            if (it.file.extension == "apks") {
                                filteredPaths.add(it)
                            }
                        }
                    }

                    "\$apkm" -> {
                        files.forEach {
                            if (it.file.extension == "apkm") {
                                filteredPaths.add(it)
                            }
                        }
                    }

                    "\$xapk" -> {
                        files.forEach {
                            if (it.file.extension == "xapk") {
                                filteredPaths.add(it)
                            }
                        }
                    }
                }
            } else {
                files.forEach {
                    if (it.file.name.contains(keyword, true) ||
                        it.file.absolutePath.contains(keyword, true) ||
                        it.file.extension.contains(keyword, true) ||
                        it.file.lastModified().toDate().contains(keyword, true)) {
                        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APK) && it.file.extension == "apk") {
                            filteredPaths.add(it)
                        }

                        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKS) && it.file.extension == "apks") {
                            filteredPaths.add(it)
                        }

                        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKM) && it.file.extension == "apkm") {
                            filteredPaths.add(it)
                        }

                        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_XAPK) && it.file.extension == "xapk") {
                            filteredPaths.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_HIDDEN).invert()) {
                val mediaPaths = ArrayList<ApkFile>()

                for (file in filteredPaths) {
                    if (file.file.absolutePath.split("/").any { it.startsWith(".") }.invert()) {
                        mediaPaths.add(file)
                        Log.d("ApkBrowserViewModel", "loadApkPaths: ${file.file.absolutePath} : is not nomedia file")
                    }
                }

                filteredPaths.clear()

                for (file in mediaPaths) {
                    filteredPaths.add(file)
                }
            }

            filteredPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            searchData.postValue(filteredPaths.clone() as ArrayList<ApkFile>)
        }
    }

    fun remove(file: ApkFile) {
        files.remove(file)
    }
}
