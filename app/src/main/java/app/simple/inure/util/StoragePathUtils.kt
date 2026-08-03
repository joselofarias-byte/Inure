package app.simple.inure.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File

object StoragePathUtils {

    fun treeUriToFile(context: Context, uri: Uri): File? {
        if (!DocumentsContract.isTreeUri(uri)) return null
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull() ?: return null
        if (documentId.startsWith("raw:")) return File(documentId.removePrefix("raw:")).canonicalFile

        val parts = documentId.split(":", limit = 2)
        val volumeId = parts.firstOrNull() ?: return null
        val relativePath = parts.getOrNull(1).orEmpty()
        val root = when {
            volumeId.equals("primary", ignoreCase = true) -> Environment.getExternalStorageDirectory()
            volumeId.equals("home", ignoreCase = true) -> File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOCUMENTS)
            else -> findVolumeRoot(context, volumeId)
        } ?: return null

        return runCatching { File(root, relativePath).canonicalFile }.getOrNull()
    }

    private fun findVolumeRoot(context: Context, volumeId: String): File? {
        return context.getExternalFilesDirs(null)
            .filterNotNull()
            .map { File(it.absolutePath.substringBefore("/Android/")) }
            .firstOrNull { it.name.equals(volumeId, ignoreCase = true) }
    }
}
