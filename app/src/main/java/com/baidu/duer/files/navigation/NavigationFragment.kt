package com.baidu.duer.files.navigation

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.duer.files.databinding.NavigationFragmentBinding
import com.baidu.duer.files.dialog.BasePopupDialogFragment
import com.baidu.duer.files.dialog.SettingMenu
import com.baidu.duer.files.dialog.SettingPopupDialogFragment
import com.baidu.duer.files.dialog.SortWayItem
import com.baidu.duer.files.file.DocumentTreeUri
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.asDocumentTreeUri
import com.baidu.duer.files.file.releasePersistablePermission
import com.baidu.duer.files.filelist.TabType
import com.baidu.duer.files.provider.document.documentTreeUri
import com.baidu.duer.files.provider.document.isDocumentPath
import com.baidu.duer.files.search.SearchFileActivity
import com.baidu.duer.files.settings.LicenseActivity
import com.baidu.duer.files.settings.PrivacyPolicyActivity
import com.baidu.duer.files.settings.StandardDirectoryListActivity
import com.baidu.duer.files.storage.AddStorageDialogActivity
import com.baidu.duer.files.storage.Storage
import com.baidu.duer.files.util.*
import java8.nio.file.Path
import java8.nio.file.Paths

class NavigationFragment : Fragment(), NavigationItem.Listener,
    BasePopupDialogFragment.SimpleClickListener {
    private lateinit var binding: NavigationFragmentBinding

    private lateinit var adapter: NavigationListAdapter

    private lateinit var searchActivityLauncher: ActivityResultLauncher<Intent>

    lateinit var listener: Listener

    private val settingMenuList by lazy {
        SettingMenu.values().map { SortWayItem(it.value) }.toMutableList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        registerActivityResult()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        NavigationFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    private fun setScreenDirectionTopMargin(newConfig: Configuration) {
        val lp = binding.navHeader.root.layoutParams as LinearLayout.LayoutParams
        // 竖屏的DrawerLayout中NavigationView默认和状态栏重合
        // 为了保证横竖屏UI效果一直，需要设置不同的topMargin
        lp.topMargin =
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 20.px else 40.px
        binding.navHeader.root.layoutParams = lp
    }

    private fun registerActivityResult() {
        searchActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == SEARCH_OPEN_FILE_RESULT_CODE) {
                it.data?.getParcelableExtra<FileItem>("file")?.let { file ->
                    if (file.attributes.isDirectory) {
                        navigateToRoot(
                            Paths.get(Environment.getExternalStorageDirectory().absolutePath),
                            TabType.ALL
                        )
                    }
                    listener.openFile(file)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.recyclerView.setHasFixedSize(true)
        activity?.resources?.configuration?.let { setScreenDirectionTopMargin(it) }
        val context = requireContext()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = NavigationListAdapter(this, context)
        binding.recyclerView.adapter = adapter
        binding.navHeader.idSettingButton.setOnClickListener {
            SettingPopupDialogFragment.show(settingMenuList, this)
        }
        binding.navHeader.searchBox.setOnClickListener {
            searchActivityLauncher.launch(Intent(SearchFileActivity::class.createIntent()))
        }

        NavigationItemListLiveData.observe(viewLifecycleOwner) { onNavigationItemsChanged(it) }
        listener.observeCurrentPath(viewLifecycleOwner) { notifyNavigationAdapter() }
        listener.observeCurrentTabType(viewLifecycleOwner) { notifyNavigationAdapter() }
    }

    private fun onNavigationItemsChanged(navigationItems: List<NavigationItem?>) {
        adapter.replace(navigationItems)
    }

    private fun notifyNavigationAdapter() {
        adapter.notifyCheckedChanged()
    }

    override val currentPath: Path
        get() = listener.currentPath

    override val currentTabType: Int
        get() = listener.currentTabType

    override fun navigateTo(path: Path) {
        listener.navigateTo(path)
    }

    override fun navigateToRoot(path: Path, tabType: Int) {
        listener.navigateToRoot(path, tabType)
    }

    override fun browseContent(tabType: Int) {
        listener.browseContent(tabType)
    }

    override fun onAddStorage() {
        startActivitySafe(AddStorageDialogActivity::class.createIntent())
    }

    override fun onEditStorage(storage: Storage) {
        startActivitySafe(storage.createEditIntent())
    }

    // TODO
    // FIXME: Navigate away on async storage removal
    fun removeDocumentTree(treeUri: DocumentTreeUri) {
        treeUri.releasePersistablePermission()
        val currentPath = listener.currentPath
        if (currentPath.isDocumentPath
            && currentPath.documentTreeUri.asDocumentTreeUri() == treeUri
        ) {
            listener.navigateToDefaultRoot()
        }
    }

    override fun onEditStandardDirectory(standardDirectory: StandardDirectory) {
        startActivitySafe(StandardDirectoryListActivity::class.createIntent())
    }

    override fun onEditBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        startActivitySafe(
            EditBookmarkDirectoryDialogActivity::class.createIntent()
                .putArgs(EditBookmarkDirectoryDialogFragment.Args(bookmarkDirectory))
        )
    }

    override fun closeNavigationDrawer() {
        listener.closeNavigationDrawer()
    }

    override fun selectSettingMenu(name: String?) {
        when (name) {
            SettingMenu.LICENSES.value -> startActivitySafe(LicenseActivity::class.createIntent())
            SettingMenu.DU_PRIVACY_AGREEMENT.value -> startActivitySafe(PrivacyPolicyActivity::class.createIntent())
        }
    }

    interface Listener {
        val currentPath: Path
        val currentTabType: Int
        fun navigateTo(path: Path)
        fun navigateToRoot(path: Path, tabType: Int)
        fun browseContent(tabType: Int)
        fun navigateToDefaultRoot()
        fun observeCurrentPath(owner: LifecycleOwner, observer: (Path) -> Unit)
        fun observeCurrentTabType(owner: LifecycleOwner, observer: (Int) -> Unit)
        fun closeNavigationDrawer()
        fun openFile(file: FileItem)
    }
}
