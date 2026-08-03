package app.simple.inure.dialogs.apks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.apks.ApksSort.Companion.showApksSort
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.util.StoragePathUtils

class ApksMenu : ScopedBottomSheetFragment() {

    private lateinit var loadSplitIconSwitch: Switch
    private lateinit var openSettings: DynamicRippleTextView
    private lateinit var filter: DynamicRippleImageButton
    private lateinit var scanFolders: DynamicRippleLinearLayout
    private lateinit var scanFoldersDescription: TypeFaceTextView
    private lateinit var clearScanFolders: DynamicRippleImageButton

    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val folder = StoragePathUtils.treeUriToFile(requireContext(), uri)
            if (folder != null && folder.isDirectory) {
                ApkBrowserPreferences.addScanFolder(folder.absolutePath)
                updateScanFoldersDescription()
            } else {
                showWarning(getString(R.string.scan_folder_invalid), false)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_apk_browser, container, false)
        loadSplitIconSwitch = view.findViewById(R.id.load_split_icon)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)
        filter = view.findViewById(R.id.filter)
        scanFolders = view.findViewById(R.id.scan_folders)
        scanFoldersDescription = view.findViewById(R.id.scan_folders_description)
        clearScanFolders = view.findViewById(R.id.clear_scan_folders)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSplitIconSwitch.isChecked = ApkBrowserPreferences.isLoadSplitIcon()
        updateScanFoldersDescription()

        loadSplitIconSwitch.setOnSwitchCheckedChangeListener { ApkBrowserPreferences.setLoadSplitIcon(it) }
        scanFolders.setOnClickListener { folderPicker.launch(null) }
        clearScanFolders.setOnClickListener {
            ApkBrowserPreferences.clearScanFolders()
            updateScanFoldersDescription()
        }
        openSettings.setOnClickListener { openSettings() }
        filter.setOnClickListener {
            parentFragmentManager.showApksSort()
            dismiss()
        }
    }

    private fun updateScanFoldersDescription() {
        val folders = ApkBrowserPreferences.getScanFolders()
        scanFoldersDescription.text = if (folders.isEmpty()) {
            getString(R.string.scan_folders_none)
        } else {
            folders.joinToString(separator = "\n")
        }
    }

    companion object {
        fun newInstance(): ApksMenu = ApksMenu().apply { arguments = Bundle() }
        fun FragmentManager.showApksMenu(): ApksMenu = newInstance().also { it.show(this, ApksMenu::class.java.simpleName) }
    }
}
