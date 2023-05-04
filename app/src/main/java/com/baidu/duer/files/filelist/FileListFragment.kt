package com.baidu.duer.files.filelist

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.app.application
import com.baidu.duer.files.app.clipboardManager
import com.baidu.duer.files.app.packageManager
import com.baidu.duer.files.compat.checkSelfPermissionCompat
import com.baidu.duer.files.database.AppDatabase
import com.baidu.duer.files.database.Collect
import com.baidu.duer.files.database.Mime
import com.baidu.duer.files.database.Tab
import com.baidu.duer.files.databinding.FileListFragmentAppBarIncludeBinding
import com.baidu.duer.files.databinding.FileListFragmentBinding
import com.baidu.duer.files.databinding.FileListFragmentContentIncludeBinding
import com.baidu.duer.files.databinding.FileListFragmentIncludeBinding
import com.baidu.duer.files.dialog.*
import com.baidu.duer.files.file.*
import com.baidu.duer.files.filejob.FileJob
import com.baidu.duer.files.filejob.FileJobService
import com.baidu.duer.files.filelist.FileSortOptions.By
import com.baidu.duer.files.fileproperties.FilePropertiesDialogFragment
import com.baidu.duer.files.message.FileOperateTaskProgress
import com.baidu.duer.files.message.OperateType
import com.baidu.duer.files.message.TaskProgressInfo
import com.baidu.duer.files.navigation.*
import com.baidu.duer.files.provider.archive.createArchiveRootPath
import com.baidu.duer.files.provider.archive.isArchivePath
import com.baidu.duer.files.provider.common.deleteIfExists
import com.baidu.duer.files.provider.linux.isLinuxPath
import com.baidu.duer.files.selectfile.SelectFileActivity
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.terminal.Terminal
import com.baidu.duer.files.ui.*
import com.baidu.duer.files.util.*
import com.baidu.duer.files.viewer.image.ImageViewerActivity
import com.baidu.duer.files.widget.CommonLoadingView
import com.baidu.duer.files.widget.DrawerLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import java8.nio.file.Path
import java8.nio.file.Paths
import kotlinx.coroutines.*
import kotlinx.parcelize.Parcelize
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class FileListFragment : Fragment(), BreadcrumbLayout.Listener, FileListAdapter.Listener,
    OpenApkDialogFragment.Listener, ConfirmDeleteFilesDialogFragment.Listener,
    CreateArchiveDialogFragment.Listener, CreateArchiveProgressDialogFragment.Listener,
    RenameFileDialogFragment.Listener, CreateFileDialogFragment.Listener,
    CreateDirectoryDialogFragment.Listener, NavigateToPathDialogFragment.Listener,
    NavigationFragment.Listener, ShowRequestAllFilesAccessRationaleDialogFragment.Listener,
    ShowRequestStoragePermissionRationaleDialogFragment.Listener,
    ShowRequestStoragePermissionInSettingsRationaleDialogFragment.Listener,
    SortPopupDialogFragment.SortClickListener, BasePopupDialogFragment.SimpleClickListener,
    UnzipFileDialogFragment.Listener {
    private val requestAllFilesAccessLauncher = registerForActivityResult(
        RequestAllFilesAccessContract(), this::onRequestAllFilesAccessResult
    )
    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(), this::onRequestStoragePermissionResult
    )
    private val requestStoragePermissionInSettingsLauncher = registerForActivityResult(
        RequestStoragePermissionInSettingsContract(),
        this::onRequestStoragePermissionInSettingsResult
    )
    private val pasteActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), this::getPastFilePathInfo
    )

    private val args by args<Args>()
    private val argsPath by lazy { args.intent.extraPath }
    private var archiveJob: FileJob? = null
    private var spanCount = 7
    private var currentArchiveType: String = FileOperateTaskProgress.NONE
    private var overlayPopupMenuItemList: MutableList<SortWayItem> = ArrayList()

    private val viewModel by viewModels { { FileListViewModel() } }

    private lateinit var binding: Binding

    private lateinit var navigationFragment: NavigationFragment

    private lateinit var menuBinding: MenuBinding

    private lateinit var overlayActionMode: ToolbarActionMode

    private lateinit var adapter: FileListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        Binding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    private fun setScreenDirectionActionBarDisplay(supportActionBar: ActionBar?, orientation: Int) {
        Log.i(TAG, "setScreenDirectionActionBarDisplay, orientation: $orientation")
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // 不显示标题
                setDisplayHomeAsUpEnabled(false)
                setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24)
                7
            } else {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.menu_icon_control_normal_38dp)
                5
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            navigationFragment = NavigationFragment()
            childFragmentManager.commit { add(R.id.navigationFragment, navigationFragment) }
        } else {
            navigationFragment = childFragmentManager.findFragmentById(R.id.navigationFragment)
                    as NavigationFragment
        }
        navigationFragment.listener = this
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        // 1竖屏 2横屏
        setScreenDirectionActionBarDisplay(
            activity.supportActionBar,
            activity.resources.configuration.orientation
        )
        // toolBar设置了NavigationOnClickListener后，onOptionsItemSelected无法接受home Item的点击事件
        binding.navigateUpButton.setOnClickListener { navigateUp() }
        overlayActionMode = OverlayToolbarActionMode(binding.overlayToolbar)
        val contentLayoutInitialPaddingBottom = binding.contentLayout.paddingBottom
        binding.appBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { _, verticalOffset ->
                binding.contentLayout.updatePaddingRelative(
                    bottom = contentLayoutInitialPaddingBottom +
                            binding.appBarLayout.totalScrollRange + verticalOffset
                )
            }
        )
        binding.appBarLayout.syncBackgroundElevationTo(binding.overlayToolbar)
        binding.breadcrumbLayout.setListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener { this.refresh() }
        val layoutManager = GridLayoutManager(
            activity,
            if (queryCurrentTabIsHorizontal(currentTabType)) spanCount else 1
        )
        binding.recyclerView.layoutManager = layoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (binding.recyclerView.adapter?.getItemViewType(position)) {
                    ListType.TITLE, ListType.BOTTOM_NOTICE -> layoutManager.spanCount
                    else -> 1
                }
            }
        }
        adapter = FileListAdapter(this, requireContext())
        binding.recyclerView.adapter = adapter

        if (!viewModel.hasTrail) {
            var path = argsPath
            val intent = args.intent
            var pickOptions: PickOptions? = null
            when (val action = intent.action ?: Intent.ACTION_VIEW) {
                Intent.ACTION_GET_CONTENT, Intent.ACTION_OPEN_DOCUMENT,
                Intent.ACTION_CREATE_DOCUMENT -> {
                    val readOnly = action == Intent.ACTION_GET_CONTENT
                    val mimeType = intent.type?.asMimeTypeOrNull() ?: MimeType.ANY
                    val extraMimeTypes = intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)
                        ?.mapNotNull { it.asMimeTypeOrNull() }?.takeIfNotEmpty()
                    val mimeTypes = extraMimeTypes ?: listOf(mimeType)
                    val localOnly = intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false)
                    val allowMultiple = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    // TODO: Actually support ACTION_CREATE_DOCUMENT.
                    pickOptions = PickOptions(readOnly, false, mimeTypes, localOnly, allowMultiple)
                }
                Intent.ACTION_OPEN_DOCUMENT_TREE -> {
                    val localOnly = intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false)
                    pickOptions = PickOptions(false, true, emptyList(), localOnly, false)
                }
                ACTION_VIEW_DOWNLOADS ->
                    path = Paths.get(
                        @Suppress("DEPRECATION")
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        ).path
                    )
                Intent.ACTION_VIEW ->
                    if (path != null) {
                        val mimeType = intent.type?.asMimeTypeOrNull()
                        if (mimeType != null && path.isArchiveFile(mimeType)) {
                            path = path.createArchiveRootPath()
                        }
                    }
                else ->
                    if (path != null) {
                        val mimeType = intent.type?.asMimeTypeOrNull()
                        if (mimeType != null && path.isArchiveFile(mimeType)) {
                            path = path.createArchiveRootPath()
                        }
                    }
            }
            if (path == null) {
                path = Paths.get(DEFAULT_PATH)
            }
            path?.let {
                navigateToRoot(path, TabType.RECENT)
            }
            if (pickOptions != null) {
                viewModel.pickOptions = pickOptions
            }
        }
        val viewLifecycleOwner = viewLifecycleOwner
        if (binding.persistentDrawerLayout != null) {
            Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.observe(viewLifecycleOwner) {
                onPersistentDrawerOpenChanged(it)
            }
        }
        viewModel.currentPathLiveData.observe(viewLifecycleOwner) { updateOverlayToolbar() }
        viewModel.breadcrumbLiveData.observe(viewLifecycleOwner) {
            Log.i(TAG, "breadcrumb: $it")
            viewModel.listLayoutState?.selectedIndex = it.selectedIndex
            binding.currentTitle.visibility =
                if (it.selectedIndex > 0) View.INVISIBLE else View.VISIBLE
            binding.navigateUpButton.visibility =
                if (it.selectedIndex > 0) View.VISIBLE else View.GONE
            binding.breadcrumbDivide.visibility =
                if (it.selectedIndex > 0 && !queryCurrentTabIsHorizontal(currentTabType))
                    View.VISIBLE else View.INVISIBLE
            binding.breadcrumbLayout.setData(it)
            viewModel.clearSelectedFiles()
        }
        viewModel.sortOptionsLiveData.observe(viewLifecycleOwner) { onSortOptionsChanged(it) }
        viewModel.pickOptionsLiveData.observe(viewLifecycleOwner) { onPickOptionsChanged(it) }
        viewModel.selectedFilesLiveData.observe(viewLifecycleOwner) { onSelectedFilesChanged(it) }
        // viewModel.pasteStateLiveData.observe(viewLifecycleOwner) { onPasteStateChanged(it) }
        Settings.FILE_NAME_ELLIPSIZE.observe(viewLifecycleOwner) { onFileNameEllipsizeChanged(it) }
        viewModel.fileListLiveData.observe(viewLifecycleOwner) { onFileListChanged(it) }
        Settings.FILE_LIST_SHOW_HIDDEN_FILES.observe(viewLifecycleOwner) {
            onShowHiddenFilesChanged(it)
        }
        viewModel.currentTabTypeLiveData.observe(viewLifecycleOwner) {
            Log.i(TAG, "currentTabType: $it")
            binding.currentTitle.text = tabNameMap[it]
        }
        // 监听文件压缩进度
        receiveFileOperateProgress(this) {
            Log.i(TAG, "oldType: $currentArchiveType, newType: ${it.progressType}")
            currentArchiveType = it.progressType
            if (it.progressType == FileOperateTaskProgress.FINISH) refresh()
        }
    }

    override fun onResume() {
        super.onResume()

        ensureStorageAccess()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuBinding = MenuBinding.inflate(menu, inflater)
    }

    // 点击MenuItem时就会回调该方法
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        updateSelectAllMenuItem()
        updateShowHiddenFilesMenuItem()
        updateHiddenAllItem()
        controlMenuItemShow(currentTabType)
        changeLayoutItemIcon()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Log.i(TAG, "action_home")
                binding.drawerLayout?.openDrawer(GravityCompat.START)
                if (binding.persistentDrawerLayout != null) {
                    Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.putValue(
                        !Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.valueCompat
                    )
                }
                true
            }
            R.id.action_add_file -> {
                Log.i(TAG, "action_add_file")
                showCreateDirectoryDialog()
                true
            }
            R.id.action_layout_change -> {
                Log.i(TAG, "action_layout_change")
                val isHorizontal = queryCurrentTabIsHorizontal(currentTabType)
                val isRunTimeout = runBlocking {
                    withTimeoutOrNull(3000) {
                        saveLayoutChangeInfo(currentTabType, !isHorizontal)
                    } == null
                }
                if (!isRunTimeout) {
                    val layoutManager = binding.recyclerView.layoutManager as GridLayoutManager
                    layoutManager.spanCount =
                        if (!isHorizontal && viewModel.currentTabType != TabType.RECENT) spanCount else 1
                    changeLayoutItemIcon()
                    updateAdapterFileList()
                }
                true
            }
            R.id.action_sort -> {
                Log.i(TAG, "action_sort")
                // 获取当前排序方式，构造排序状态list Bean
                run out@{
                    sortTypeMap.entries.forEach {
                        viewModel.sortOptions.let { sortOptions ->
                            if (it.value == sortOptions.by) {
                                fileSortList.getOrNull(it.key)?.apply {
                                    isChecked = true
                                    isAscend = sortOptions.order == FileSortOptions.Order.ASCENDING
                                }
                                SortPopupDialogFragment.show(fileSortList, this)
                                return@out
                            }
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed(): Boolean {
        Log.i(TAG, "onBackPressed")
        val drawerLayout = binding.drawerLayout
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }
        if (overlayActionMode.isActive) {
            overlayActionMode.finish()
            return true
        }
        if (viewModel.navigateUp(false)) {
            createListLayoutData(currentTabType, Int.MIN_VALUE)
            setGridLayoutManagerSpanCount(currentTabType)
            changeLayoutItemIcon(currentTabType)
            return true
        }
        // See also https://developer.android.com/about/versions/12/behavior-changes-all#back-press
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && requireActivity().isTaskRoot) {
            viewModel.isStorageAccessRequested = false
        }
        return false
    }

    private fun onPersistentDrawerOpenChanged(open: Boolean) {
        binding.persistentDrawerLayout?.let {
            if (open) {
                it.openDrawer(GravityCompat.START)
            } else {
                it.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun onFileListChanged(stateful: Stateful<List<FileItem>>) {
        val files = stateful.value
        val isSearching = viewModel.searchState.isSearching
        val hasFiles = if (Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat) {
            !files.isNullOrEmpty()
        } else {
            !files.isNullOrEmpty() && files.any { !it.isHidden }
        }
        if (stateful !is Loading) binding.swipeRefreshLayout.finishRefresh()
        binding.loading.fadeToVisibilityUnsafe(stateful is Loading && !(hasFiles || isSearching))
        /*binding.errorText.fadeToVisibilityUnsafe(stateful is Failure && !hasFiles)
        val throwable = (stateful as? Failure)?.throwable
        if (throwable != null) {
            throwable.printStackTrace()
            val error = throwable.toString()
            if (hasFiles) {
                showToast(error)
            } else {
                binding.errorText.text = error
            }
        }*/
        binding.emptyView.fadeToVisibilityUnsafe((stateful is Success || stateful is Failure) && !hasFiles)
        if (files != null) {
            if (stateful !is Loading) {
                updateAdapterFileList()
            }
        } else {
            // 从Android 10开始三方文件管理器无法直接访问/Android/data目录
            binding.emptyView.text =
                if (currentTabType == TabType.ALL && currentPath.compare(
                        Paths.get(Environment.getExternalStorageDirectory().absolutePath)
                    ) > 0
                    && !currentPath.toFile().canRead()
                ) {
                    getString(R.string.file_access_without_permission)
                } else {
                    getString(R.string.file_list_empty)
                }
            adapter.clear()
        }
        if (stateful is Success) {
            viewModel.pendingState
                ?.let { binding.recyclerView.layoutManager!!.onRestoreInstanceState(it) }
        }
    }

    private fun onSortOptionsChanged(sortOptions: FileSortOptions) {
        Log.i(TAG, "onSortOptionsChanged: $sortOptions")
        adapter.comparator = sortOptions.createComparator()
    }

    private fun navigateUp() {
        Log.i(TAG, "navigateUp")
        createListLayoutData(currentTabType, Int.MIN_VALUE)
        setGridLayoutManagerSpanCount(currentTabType)
        changeLayoutItemIcon(currentTabType)
        viewModel.navigateUp(true)
    }

    private fun refresh() {
        Log.i(TAG, "refresh")
        viewModel.reload()
    }

    private fun setShowHiddenFiles(showHiddenFiles: Boolean) {
        Settings.FILE_LIST_SHOW_HIDDEN_FILES.putValue(showHiddenFiles)
    }

    private fun onShowHiddenFilesChanged(showHiddenFiles: Boolean) {
        updateAdapterFileList()
        updateShowHiddenFilesMenuItem()
    }

    private fun updateAdapterFileList() {
        Log.i(TAG, "updateAdapterFileList")
        if (currentArchiveType == FileOperateTaskProgress.ONGOING) return
        var files = viewModel.fileListStateful.value ?: return

        if (!Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat) {
            files = files.filterNot { it.isHidden }
        }
        adapter.replaceListAndIsSearching(
            files,
            viewModel.searchState.isSearching,
            if (viewModel.currentTabType == TabType.RECENT) {
                ListType.RECENT
            } else if (queryCurrentTabIsHorizontal(currentTabType)) {
                ListType.HORIZONTAL
            } else {
                ListType.VERTICAL
            }
        )
    }

    private fun updateShowHiddenFilesMenuItem() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        val showHiddenFiles = Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat
        // menuBinding.showHiddenFilesItem.isChecked = showHiddenFiles
    }

    private fun updateHiddenAllItem() {
        menuBinding.menu.children.forEach {
            it.isVisible = viewModel.currentTabType != TabType.RECENT
        }
    }

    private fun share() {
        shareFile(currentPath, MimeType.DIRECTORY)
    }

    private fun copyPath() {
        copyPath(currentPath)
    }

    private fun openInTerminal() {
        val path = currentPath
        if (path.isLinuxPath) {
            Terminal.open(path.toFile().path, requireContext())
        } else {
            // TODO
        }
    }

    override fun onSortClick(sortType: By, orderType: FileSortOptions.Order) {
        viewModel.setSortBy(sortType)
        viewModel.setSortOrder(orderType)
    }

    override fun selectMoreMenu(name: String?) {
        when (name) {
            MoreMenu.ARCHIVE.value -> {
                showCreateArchiveDialog(viewModel.selectedFiles)
            }
            MoreMenu.COLLECT.value -> {
                collectFiles(viewModel.selectedFiles)
            }
            MoreMenu.CANCEL_COLLECT.value -> {
                cancelCollect(viewModel.selectedFiles)
            }
            MoreMenu.COPY.value -> {
                copyFiles(viewModel.selectedFiles)
                // 摒弃之前startActivityForResult()方式
                pasteActivityLauncher.launch(
                    Intent(
                        SelectFileActivity::class.createIntent().putExtra(SELECT_TYPE, PASTE)
                            .putExtra(
                                TITLE, getString(
                                    R.string.select_paste_file_to_path_count,
                                    viewModel.pasteState.files.size
                                )
                            ).putExtra(
                                PATH_DATA,
                                viewModel.pasteState.files.map { it.path.toString() }.toTypedArray()
                            )
                    )
                )
            }
            MoreMenu.DETAILS.value -> {
                showCreateDetailsDialog(viewModel.selectedFiles.single())
            }
            MoreMenu.RENAME.value -> {
                RenameFileDialogFragment.show(viewModel.selectedFiles.single(), this)
            }
        }
    }

    override fun navigateTo(path: Path) {
        Log.i(TAG, "navigateTo, path: $path, currentPath: $currentPath")
        createListLayoutData(currentTabType, path.compare(currentPath), path)
        setGridLayoutManagerSpanCount(currentTabType)
        changeLayoutItemIcon(currentTabType)
        val state = binding.recyclerView.layoutManager!!.onSaveInstanceState()
        viewModel.navigateTo(state!!, path)
    }

    override fun copyPath(path: Path) {
        clipboardManager.copyText(path.toUserFriendlyString(), requireContext())
    }

    override fun openInNewTask(path: Path) {
        val intent = FileListActivity.createViewIntent(path)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivitySafe(intent)
    }

    private fun onPickOptionsChanged(pickOptions: PickOptions?) {
        val title = if (pickOptions == null) {
            getString(R.string.file_list_title)
        } else {
            val titleRes = if (pickOptions.pickDirectory) {
                R.plurals.file_list_title_pick_directory
            } else {
                R.plurals.file_list_title_pick_file
            }
            val count = if (pickOptions.allowMultiple) Int.MAX_VALUE else 1
            getQuantityString(titleRes, count)
        }
        requireActivity().title = title
        updateSelectAllMenuItem()
        updateOverlayToolbar()
        adapter.pickOptions = pickOptions
    }

    private fun updateSelectAllMenuItem() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        val pickOptions = viewModel.pickOptions
        // menuBinding.selectAllItem.isVisible = pickOptions == null || pickOptions.allowMultiple
    }

    private fun pickFiles(files: FileItemSet) {
        pickPaths(files.mapTo(linkedSetOf()) { it.path })
    }

    private fun pickPaths(paths: LinkedHashSet<Path>) {
        val intent = Intent().apply {
            val pickOptions = viewModel.pickOptions!!
            if (paths.size == 1) {
                val path = paths.single()
                data = path.fileProviderUri
                extraPath = path
            } else {
                val mimeTypes = pickOptions.mimeTypes.map { it.value }
                val items = paths.map { ClipData.Item(it.fileProviderUri) }
                clipData = ClipData::class.create(null, mimeTypes, items)
                extraPathList = paths.toList()
            }
            var flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            if (!pickOptions.readOnly) {
                flags = flags or (Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            if (pickOptions.pickDirectory) {
                flags = flags or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            }
            addFlags(flags)
        }
        requireActivity().run {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun onSelectedFilesChanged(files: FileItemSet) {
        updateOverlayToolbar()
        adapter.replaceSelectedFiles(files)
        // 当有文件选中时左边栏不能进行点击操作
        binding.navigationFrameLayout.interceptClick = viewModel.selectedFiles.isNotEmpty()
    }

    private fun addMorePopupMenuItem(name: String, isOk: Boolean) {
        if (isOk) overlayPopupMenuItemList.add(SortWayItem(name))
    }

    private fun updateOverlayToolbar() {
        val files = viewModel.selectedFiles
        if (files.isEmpty()) {
            if (overlayActionMode.isActive) {
                overlayActionMode.finish()
            }
            return
        }
        val pickOptions = viewModel.pickOptions
        if (pickOptions != null) {
            overlayActionMode.title = getString(R.string.file_list_select_title_format, files.size)
            overlayActionMode.setMenuResource(R.menu.file_list_pick)
            val menu = overlayActionMode.menu
            menu.findItem(R.id.action_select_all).isVisible = pickOptions.allowMultiple
        } else {
            overlayActionMode.title = getString(R.string.file_list_select_title_format, files.size)
            overlayActionMode.setMenuResource(R.menu.file_list_select)
            // 仅通过改变选中的File数量来决定Overlap Toolbar的全选按钮状态
            // 检索出的fileListStateful包括隐藏文件的，但是目前我们引用中默认不显示隐藏文件，比较Size时需要考虑该情况
            viewModel.fileListStateful.value?.let {
                val currentShowFileSize = if (Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat) {
                    it.size
                } else {
                    it.stream().filter { file -> !file.isHidden }.count().toInt()
                }
                overlayActionMode.menu.findItem(R.id.action_select_all).apply {
                    icon =
                        ContextCompat.getDrawable(
                            requireContext(),
                            if (viewModel.selectedFiles.size == currentShowFileSize && it.isNotEmpty()) {
                                R.drawable.select_all_file_icon
                            } else {
                                R.drawable.unselect_all_file_icon
                            }
                        )
                    isChecked =
                        viewModel.selectedFiles.size == currentShowFileSize && it.isNotEmpty()
                }
            }
            // 当选择的所有文件中存在文件夹时不进行分享
            // 可能在overlayToolbar出现以后，再发生变化（eg：先选择文件、再选择了文件夹）
            overlayActionMode.menu.findItem(R.id.action_share).isVisible =
                !files.isContain {
                    it.attributes.isDirectory
                }
            val isAnyFileReadOnly = files.any { it.path.fileSystem.isReadOnly }
            val isCurrentPathReadOnly = viewModel.currentPath.fileSystem.isReadOnly
            lifecycleScope.launch {
                val result = async {
                    try {
                        withTimeout(3000) {
                            files.find {
                                AppDatabase.getDatabase(requireContext())?.collectDao()
                                    ?.getCollect(it.path.toString()) == null
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        e.printStackTrace()
                        null
                    }
                }
                val isCanCancelCollect = result.await() == null
                val isCanCollect =
                    !(viewModel.currentTabType == TabType.COLLECT || isCanCancelCollect)
                // 选中的所有文件中，如果存在压缩文件，则不展示压缩菜单选项
                val isCanArchive = viewModel.currentTabType != TabType.COLLECT
                // 构建More Dialog Item信息
                overlayPopupMenuItemList.clear()
                addMorePopupMenuItem(MoreMenu.ARCHIVE.value, isCanArchive && !isCurrentPathReadOnly)
                // 处于"我的收藏"或者选中所有File都是已被收藏时，收藏Item隐藏
                // 处于"我的收藏"Tab下单选、多选处理时，Menu中需要隐藏"收藏"、"压缩"Item
                addMorePopupMenuItem(MoreMenu.COLLECT.value, isCanCollect)
                addMorePopupMenuItem(MoreMenu.CANCEL_COLLECT.value, isCanCancelCollect)
                addMorePopupMenuItem(MoreMenu.COPY.value, !isAnyFileReadOnly)
                // 文件详情、重命名都在同一时间只能对单个文件进行操作
                addMorePopupMenuItem(MoreMenu.DETAILS.value, files.size <= 1)
                addMorePopupMenuItem(MoreMenu.RENAME.value, files.size <= 1)
            }
        }
        if (!overlayActionMode.isActive) {
            binding.appBarLayout.setExpanded(true)
            binding.appBarLayout.addOnOffsetChangedListener(
                AppBarLayoutExpandHackListener(binding.recyclerView)
            )
            overlayActionMode.start(object : ToolbarActionMode.Callback {
                override fun onToolbarActionModeStarted(toolbarActionMode: ToolbarActionMode) {}

                override fun onToolbarActionModeItemClicked(
                    toolbarActionMode: ToolbarActionMode,
                    item: MenuItem
                ): Boolean = onOverlayActionModeItemClicked(toolbarActionMode, item)

                override fun onToolbarActionModeFinished(toolbarActionMode: ToolbarActionMode) {
                    onOverlayActionModeFinished()
                }
            })
        }
    }

    private fun onOverlayActionModeItemClicked(
        toolbarActionMode: ToolbarActionMode,
        item: MenuItem
    ): Boolean =
        when (item.itemId) {
            R.id.action_pick -> {
                pickFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_cut -> {
                cutFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_delete -> {
                confirmDeleteFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_share -> {
                shareFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_select_all -> {
                overlayActionMode.menu.findItem(R.id.action_select_all).icon =
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (item.isChecked) {
                            viewModel.clearSelectedFiles()
                            R.drawable.unselect_all_file_icon
                        } else {
                            selectAllFiles()
                            R.drawable.select_all_file_icon
                        }
                    )
                true
            }
            R.id.action_more -> {
                MorePopupDialogFragment.show(overlayPopupMenuItemList, this)
                true
            }
            else -> false
        }

    private fun onOverlayActionModeFinished() {
        viewModel.clearSelectedFiles()
    }

    private fun cutFiles(files: FileItemSet) {
        viewModel.clearPasteState()
        viewModel.addToPasteState(false, files)
        pasteActivityLauncher.launch(
            Intent(
                SelectFileActivity::class.createIntent().putExtra(SELECT_TYPE, CUT)
                    .putExtra(
                        TITLE, getString(
                            R.string.select_cut_file_to_path_count,
                            viewModel.pasteState.files.size
                        )
                    ).putExtra(
                        PATH_DATA,
                        viewModel.pasteState.files.map { it.path.toString() }.toTypedArray()
                    )
            )
        )
        viewModel.selectFiles(files, false)
    }

    private fun copyFiles(files: FileItemSet) {
        viewModel.clearPasteState()
        viewModel.addToPasteState(true, files)
        viewModel.selectFiles(files, false)
    }

    private fun confirmDeleteFiles(files: FileItemSet) {
        ConfirmDeleteFilesDialogFragment.show(files, this)
    }

    override fun deleteFiles(files: FileItemSet) {
        Log.i(TAG, "deleteFiles, files: $files")
        val fileArray = files.map { it.path.toString() }.toTypedArray()
        val collectDao = AppDatabase.getDatabase(requireContext())?.collectDao()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withTimeout(3000) {
                    fileArray.forEach {
                        if (collectDao?.getCollect(it) != null) {
                            collectDao.delete(it)
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                e.printStackTrace()
            }
        }
        FileJobService.delete(makePathListForJob(files), requireContext())
        viewModel.selectFiles(files, false)
        CopyMoveDeleteProgressDialogFragment.show(CopyMoveDeleteType.DELETE, this)
    }

    private fun showCreateArchiveDialog(files: FileItemSet) {
        Log.i(TAG, "showCreateArchiveDialog, files: $files")
        CreateArchiveDialogFragment.show(
            files,
            findExistNameFile(),
            files.first().path.parent.toString(),
            this
        )
    }

    /**
     * 查找该文件列表中和fileName相同前缀的文件或文件夹个数
     */
    private fun findExistNameFile(
        fileName: String? = context?.resources?.getString(R.string.file_type_name_archive_num)
    ): Int {
        Log.i(TAG, "findExistNameFile, fileName: $fileName")
        val files = viewModel.fileListStateful.value ?: return 0
        return fileName?.let {
            files.filter { it.name.startsWith(fileName) }.size
        } ?: 0
    }

    override fun archive(
        path: Path?,
        files: FileItemSet,
        name: String,
        archiveType: String,
        compressorType: String?
    ) {
        Log.i(
            TAG,
            "archive, path: $path, name: $name, archiveType: $archiveType, compressorType: $compressorType"
        )
        val archiveFile = path?.resolve(name) ?: viewModel.currentPath.resolve(name)
        archiveJob = FileJobService.archive(
            makePathListForJob(files), archiveFile, archiveType, compressorType, requireContext()
        )
        viewModel.selectFiles(files, false)
        // 打开压缩进度Dialog
        CreateArchiveProgressDialogFragment.show(path.toString(), name, this)
    }

    override fun cancelArchiveFile(path: Path?, name: String) {
        Log.i(TAG, "cancelArchiveFile, path: $path, name: $name")
        archiveJob?.id?.let {
            FileJobService.cancelJob(it) {
                path?.resolve(name)?.deleteIfExists()
                sendDelayFileOperateProgress(
                    TaskProgressInfo(
                        -1,
                        -1,
                        FileOperateTaskProgress.NONE,
                        OperateType.CANCEL_COMPRESS
                    ),
                    1000L
                )
            }
        }
    }

    override fun cancelUnzipFile(path: Path, name: String) {
        Log.i(TAG, "cancelUnzipFile, path: $path, name: $name")
        archiveJob?.id?.let {
            FileJobService.cancelJob(it) {
                if (name.isNotEmpty()) {
                    deleteFiles(fileItemSetOf(path.resolve(name).loadFileItem()))
                    sendDelayFileOperateProgress(
                        TaskProgressInfo(
                            -1,
                            -1,
                            FileOperateTaskProgress.NONE,
                            OperateType.CANCEL_UNZIP
                        ),
                        1000L
                    )
                }
            }
        }
    }

    private fun shareFiles(files: FileItemSet) {
        shareFiles(files.map { it.path }, files.map { it.mimeType })
        viewModel.selectFiles(files, false)
    }

    private fun selectAllFiles() {
        adapter.selectAllFiles()
    }

    private fun collectFiles(files: FileItemSet) {
        Log.i(TAG, "collectFiles, files: $files")
        adapter.collectFiles(files)
        val dataBase = AppDatabase.getDatabase(requireContext())
        val copyOnWriteList: CopyOnWriteArrayList<FileItem> = CopyOnWriteArrayList(files)
        // 为了保证线程中操作集合的安全性，数据全部转换到线程安全的集合类
        lifecycleScope.launch {
            // 后台线程执行任务结束后才会执行后续代码逻辑
            val queryResult = withContext(Dispatchers.Default) {
                withTimeoutOrNull(3000) {
                    copyOnWriteList.filter {
                        dataBase?.collectDao()?.getCollect(it.path.toString()) == null
                    }
                } == null
            }
            // withTimeoutOrNull返回null，说明内部数据库操作超时，后续逻辑丧失运行必要条件
            if (queryResult) return@launch

            val collectList = copyOnWriteList.map {
                Collect(
                    path = it.path.toString(),
                    pathName = it.path.name
                )
            }

            withContext(Dispatchers.Default) {
                withTimeoutOrNull(3000) {
                    // list转vararg，list先转成Array,*Array分拆成多个单bean传入vararg
                    dataBase?.collectDao()?.insert(*collectList.map { it }.toTypedArray())
                } == null
            }
            // 收藏元素信息成功写到本地数据库后再刷新列表，因为在刷新列表时，
            // Adapter中每个Item会根据自己的Path去数据中查询自己是否是被收藏的
            viewModel.selectFiles(files, false)
        }
    }

    private fun cancelCollect(files: FileItemSet) {
        Log.i(TAG, "cancelCollect, files: $files")
        val dataBase = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                withTimeoutOrNull(3000) {
                    // 因为这里files本身没有进行任何添加、删除操作，也不用考虑是否线程安全的问题
                    files.map { it.path.toString() }.forEach {
                        dataBase?.collectDao()?.delete(it)
                    }
                }
            }
            // 无论上面数据库操作是否超时，执行下方程序都不影响效果，而且都能保证UI展示正常
            refresh()
            viewModel.selectFiles(files, false)
        }
    }

    private fun showCreateDetailsDialog(file: FileItem) {
        Log.i(TAG, "showCreateDetailsDialog, file path: ${file.path}")
        CreateDetailsDialogFragment.show(file, this)
    }

    /**
     * Description: 只要可以进行布局样式切换的Tab都需要保存自己的布局样式
     * Reason：每次Toolbar上切换的布局样式，只是针对当前的列表生效
     * Implement：每个tab对应的布局样式信息存储在本地Room数据库
     * 特殊场景：如果当前路径是布局样式a，进入搜索页面跳转至当前页面，当前页面由上一级页面或者数据的Tab展示样式决定
     * @param tabType 每个Tab对应的表示
     * @param isHorizontal 该Tab是否采用横向网格布局
     */
    private suspend fun saveLayoutChangeInfo(tabType: Int, isHorizontal: Boolean) {
        Log.i(TAG, "saveLayoutChangeInfo, tabType: $tabType, isHorizontal: $isHorizontal")
        // 当处于该Tab的二级目录时不进行Layout类型的保存
        if (viewModel.breadcrumb?.selectedIndex == 0) {
            Log.i(TAG, "current locate root")
            val dataBase = AppDatabase.getDatabase(requireContext())
            val tabDao = dataBase?.tabDao()
            tabDao?.apply {
                val tab = getTab(tabType)
                tab?.let {
                    tab.isHorizontal = isHorizontal
                    update(tab)
                } ?: insert(Tab(type = tabType, isHorizontal = isHorizontal))
            }
        }
        viewModel.listLayoutState?.let {
            if (tabType == it.tabType) {
                it.layout[it.selectedIndex] = isHorizontal
            }
        }
    }

    private fun queryCurrentTabIsHorizontal(type: Int): Boolean {
        Log.i(TAG, "queryCurrentTabIsHorizontal, type: $type")
        // 当任务超时时withTimeoutOrNull会返回null，如果使用withTimeout会抛出TimeoutCancellationException异常
        return if (viewModel.breadcrumb == null || viewModel.breadcrumb?.selectedIndex == 0) {
            val dataBase = AppDatabase.getDatabase(requireContext())
            val tabDao = dataBase?.tabDao()
            // 当前Tab未更改过布局展示样式时，进入默认网格布局
            runBlocking {
                try {
                    withTimeout(3000) {
                        tabDao?.getTab(type)?.isHorizontal ?: true
                    }
                } catch (e: TimeoutCancellationException) {
                    e.printStackTrace()
                    true
                }
            }
        } else {
            viewModel.listLayoutState?.let {
                it.layout.getOrNull(it.selectedIndex)
            } ?: true
        }
    }

    private fun createListLayoutData(tabType: Int, compareStatus: Int = 1, path: Path? = null) {
        Log.i(TAG, "createListLayoutData, tabType: $tabType, isForward: $compareStatus")
        val isHorizontal = queryCurrentTabIsHorizontal(tabType)
        if (viewModel.listLayoutState == null || viewModel.listLayoutState?.tabType != tabType) {
            viewModel.listLayoutState =
                ListLayoutData(tabType, arrayListOf(isHorizontal), 0)
        } else {
            viewModel.listLayoutState?.let { list ->
                val oldCount = currentPath.toString().count { element -> element == '/' }
                // 如果Path为空且compareStatus小于0，则表示只是进行简单的后退操作
                val currentCount =
                    path?.toString()?.count { element -> element == '/' } ?: (oldCount - 1)
                // 检测当前路径是前进还是后退，并且检测步幅大小
                // 后退多步场景：点击BreadCrumb中不相邻Item
                // 前进多步场景：搜索出文件夹，跳转到当前路径下
                when {
                    compareStatus > 0 -> {
                        repeat(currentCount - oldCount) {
                            list.layout.add(isHorizontal)
                            list.selectedIndex += 1
                        }
                    }
                    compareStatus < 0 -> {
                        repeat(oldCount - currentCount) {
                            list.layout.removeLast()
                            list.selectedIndex -= 1
                        }
                    }
                    else -> {
                        // TODO 新旧两路径相等
                    }
                }
            }
        }
    }

    private fun setGridLayoutManagerSpanCount(type: Int) {
        Log.i(TAG, "setGridLayoutManagerSpanCount, type: $type")
        val layoutManager = binding.recyclerView.layoutManager as GridLayoutManager
        layoutManager.spanCount = if (!queryCurrentTabIsHorizontal(type)) 1 else spanCount
    }

    private fun changeLayoutItemIcon(type: Int? = null) {
        Log.i(TAG, "changeLayoutItemIcon, type: $type")
        if (!this::menuBinding.isInitialized) return
        menuBinding.layoutChange.icon = context?.let {
            AppCompatResources.getDrawable(
                it,
                if (!queryCurrentTabIsHorizontal(type ?: currentTabType))
                    R.drawable.grid_order_icon else R.drawable.vertical_list_icon
            )
        }
    }

    override fun unzipFile(path: Path) {
        Log.i(TAG, "unzipFile, path: $path")
        pasteFiles(path)
    }

    override fun navigateToFilePath(path: Path) {
        Log.i(TAG, "navigateToFilePath, path: $path")
        // 当前所在路径不是目的路径时进行路径跳转
        if (path.compareTo(currentPath) != 0) {
            navigateToRoot(
                Paths.get(Environment.getExternalStorageDirectory().absolutePath),
                TabType.ALL
            )
            navigateTo(path)
        }
    }

    private fun pasteFiles(targetDirectory: Path) {
        val pasteState = viewModel.pasteState
        if (viewModel.pasteState.copy) {
            Log.i(TAG, "copy file, targetDirectory: $targetDirectory")
            // 考虑到解压、复制可能失败，因此限制只有在复制、解压成功后才能情况剪切板
            // 另外一种情况，如果复制、解压没有成功，用户退出了解压复制流程，需要及时情况粘贴板
            archiveJob = FileJobService.copy(
                makePathListForJob(pasteState.files), targetDirectory, requireContext()
            ) {
                activity?.runOnUiThread {
                    viewModel.clearPasteState()
                }
            }
        } else {
            Log.i(TAG, "move file, targetDirectory: $targetDirectory")
            val collectDao = AppDatabase.getDatabase(requireContext())?.collectDao()
            pasteState.files.forEach {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (collectDao?.getCollect(it.path.toString()) != null) {
                        collectDao.update(
                            it.path.toString(),
                            it.name,
                            targetDirectory.resolve(it.name).toString()
                        )
                    }
                }
            }
            FileJobService.move(
                makePathListForJob(pasteState.files), targetDirectory, requireContext()
            )
            viewModel.clearPasteState()
        }
    }

    private fun makePathListForJob(files: FileItemSet): List<Path> =
        files.map { it.path }.sorted()

    private fun onFileNameEllipsizeChanged(fileNameEllipsize: TextUtils.TruncateAt) {
        adapter.nameEllipsize = fileNameEllipsize
    }

    override fun clearSelectedFiles() {
        viewModel.clearSelectedFiles()
    }

    override fun selectFile(file: FileItem, selected: Boolean) {
        viewModel.selectFile(file, selected)
    }

    override fun selectFiles(files: FileItemSet, selected: Boolean) {
        viewModel.selectFiles(files, selected)
    }

    override fun openFile(file: FileItem) {
        val pickOptions = viewModel.pickOptions
        if (pickOptions != null) {
            if (file.attributes.isDirectory) {
                navigateTo(file.path)
            } else if (!pickOptions.pickDirectory) {
                pickFiles(fileItemSetOf(file))
            }
            return
        }
        if (file.mimeType.isApk) {
            openApk(file)
            return
        }
        if (file.isArchiveFile) {
            // 默认点击压缩文件，进行解压流程
            extractFile(file)
            UnzipFileDialogFragment.show(file, file.path.parent.toString(), this)
            return
        }
        if (file.isListable) {
            navigateTo(file.listablePath)
            return
        }
        val mimeTypeOpenAppInfo = runBlocking {
            try {
                withTimeout(3000) {
                    queryFileOpenApplicationInfo(file)
                }
            } catch (e: TimeoutCancellationException) {
                e.printStackTrace()
                null
            }
        }
        if (mimeTypeOpenAppInfo == null) {
            if (getOpenIntentActivities(file.path.fileProviderUri).isEmpty()) {
                requireContext().showToast(getString(R.string.file_open_toast_hint))
            } else {
                OpenFileDialogFragment.show(file, this)
            }
        } else {
            // 直接打开
            startActivity(
                file.path.fileProviderUri.createOpenFileOtherApk(
                    file.mimeType.value,
                    mimeTypeOpenAppInfo.packageName.toString(),
                    mimeTypeOpenAppInfo.className.toString()
                ).apply {
                    extraPath = file.path
                }
            )
        }
        // openFileWithIntent(file, false)
    }

    private suspend fun queryFileOpenApplicationInfo(file: FileItem): Mime? {
        val mime =
            AppDatabase.getDatabase(requireContext())?.mimeDao()?.getOpenApp(file.mimeType.value)
                ?: return null
        // 判断之前选择的默认打开应用是否已经卸载，或者应用指定的页面已经不存在
        var isUnload = true
        run out@{
            getOpenIntentActivities(file.path.fileProviderUri).forEach {
                it?.apply {
                    if (activityInfo.packageName == mime.packageName
                        && activityInfo.name == mime.className
                    ) {
                        isUnload = false
                        return@out
                    }
                }
            }
        }
        Log.i(TAG, "queryFileOpenApplicationInfo: file path: ${file.path}, isUnload: $isUnload")
        return if (isUnload) {
            AppDatabase.getDatabase(requireContext())?.mimeDao()?.delete(file.mimeType.value)
            null
        } else mime
    }

    // 扫描当前文件是否已经通过系统设置了默认打开应用
    private fun getOpenFileOnlyIntent(file: FileItem): ResolveInfo? {
        val createViewIntent = file.path.fileProviderUri.createViewIntent(file.mimeType)
        val resolveInfo =
            packageManager.resolveActivity(createViewIntent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (resolveInfo == null || resolveInfo.activityInfo?.packageName?.lowercase() == "android") {
            null
        } else {
            resolveInfo
        }
    }

    private fun openApk(file: FileItem) {
        if (!file.isListable) {
            installApk(file)
            return
        }
        when (Settings.OPEN_APK_DEFAULT_ACTION.valueCompat) {
            OpenApkDefaultAction.INSTALL -> installApk(file)
            OpenApkDefaultAction.VIEW -> viewApk(file)
            OpenApkDefaultAction.ASK -> OpenApkDialogFragment.show(file, this)
        }
    }

    override fun installApk(file: FileItem) {
        Log.i(TAG, "installApk, file Path: ${file.path}")
        val path = file.path
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!path.isArchivePath) path.fileProviderUri else null
        } else {
            // PackageInstaller only supports file URI before N.
            if (path.isLinuxPath) Uri.fromFile(path.toFile()) else null
        }
        if (uri != null) {
            startActivitySafe(uri.createInstallPackageIntent())
        } else {
            FileJobService.installApk(path, requireContext())
        }
    }

    override fun viewApk(file: FileItem) {
        navigateTo(file.listablePath)
    }

    override fun openFileWith(file: FileItem) {
        openFileWithIntent(file, true)
    }

    private fun openFileWithIntent(file: FileItem, withChooser: Boolean) {
        val path = file.path
        val mimeType = file.mimeType
        if (path.isArchivePath) {
            FileJobService.open(path, mimeType, withChooser, requireContext())
        } else {
            val intent = path.fileProviderUri.createViewIntent(mimeType)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .apply {
                    extraPath = path
                    maybeAddImageViewerActivityExtras(this, path, mimeType)
                }
                .let {
                    if (withChooser) {
                        it.withChooser(
                            EditFileActivity::class.createIntent()
                                .putArgs(EditFileActivity.Args(path, mimeType)),
                            OpenFileAsDialogActivity::class.createIntent()
                                .putArgs(OpenFileAsDialogFragment.Args(path))
                        )
                    } else {
                        it
                    }
                }
            startActivitySafe(intent)
        }
    }

    private fun maybeAddImageViewerActivityExtras(intent: Intent, path: Path, mimeType: MimeType) {
        if (!mimeType.isImage) {
            return
        }
        var paths = mutableListOf<Path>()
        // We need the ordered list from our adapter instead of the list from FileListLiveData.
        for (index in 0 until adapter.itemCount) {
            val file = adapter.getItem(index)
            val filePath = file.path
            if (file.mimeType.isImage || filePath == path) {
                paths.add(filePath)
            }
        }
        var position = paths.indexOf(path)
        if (position == -1) {
            return
        }
        // HACK: Don't send too many paths to avoid TransactionTooLargeException.
        if (paths.size > IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX) {
            val start = (position - IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX / 2)
                .coerceIn(0, paths.size - IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX)
            paths = paths.subList(start, start + IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX)
            position -= start
        }
        ImageViewerActivity.putExtras(intent, paths, position)
    }

    override fun cutFile(file: FileItem) {
        Log.i(TAG, "cutFile, file path: ${file.path}")
        cutFiles(fileItemSetOf(file))
    }

    override fun copyFile(file: FileItem) {
        // 竖直列表，点击右侧更多按钮，出现的菜单栏中点击的复制
        Log.i(TAG, "copyFile, file path: ${file.path}")
        copyFiles(fileItemSetOf(file))
    }

    override fun confirmDeleteFile(file: FileItem) {
        Log.i(TAG, "confirmDeleteFile, file path: ${file.path}")
        confirmDeleteFiles(fileItemSetOf(file))
    }

    override fun showRenameFileDialog(file: FileItem) {
        Log.i(TAG, "showRenameFileDialog, file path: ${file.path}")
        RenameFileDialogFragment.show(file, this)
    }

    override fun hasFileWithName(name: String): Boolean {
        Log.i(TAG, "hasFileWithName, name: $name")
        val fileListData = viewModel.fileListStateful
        return fileListData is Success && fileListData.value.any { it.name == name }
    }

    override fun renameFile(file: FileItem, newName: String) {
        Log.i(TAG, "renameFile, file path: ${file.path}, newName: $newName")
        val collectDao = AppDatabase.getDatabase(requireContext())?.collectDao()
        lifecycleScope.launch(Dispatchers.IO) {
            if (collectDao?.getCollect(file.path.toString()) != null) {
                collectDao.update(
                    file.path.toString(),
                    newName,
                    file.path.resolveSibling(newName).toString()
                )
            }
        }
        FileJobService.rename(file.path, newName, requireContext())
        viewModel.selectFile(file, false)
    }

    override fun extractFile(file: FileItem) {
        Log.i(TAG, "extractFile, file path: ${file.path}")
        copyFile(file.createDummyArchiveRoot())
    }

    override fun showCreateArchiveDialog(file: FileItem) {
        Log.i(TAG, "showCreateArchiveDialog, file: ${file.path}")
        showCreateArchiveDialog(fileItemSetOf(file))
    }

    override fun shareFile(file: FileItem) {
        shareFile(file.path, file.mimeType)
    }

    private fun shareFile(path: Path, mimeType: MimeType) {
        shareFiles(listOf(path), listOf(mimeType))
    }

    private fun shareFiles(paths: List<Path>, mimeTypes: List<MimeType>) {
        Log.i(TAG, "shareFiles, paths: $paths, mimeTypes: $mimeTypes")
        ShareFileDialogFragment.show(mimeTypes, paths.map { it.fileProviderUri }, this)
    }

    override fun copyPath(file: FileItem) {
        Log.i(TAG, "copyPath, file: ${file.path}")
        copyPath(file.path)
    }

    override fun addBookmark(file: FileItem) {
        addBookmark(file.path)
    }

    private fun addBookmark() {
        addBookmark(currentPath)
    }

    private fun addBookmark(path: Path) {
        BookmarkDirectories.add(BookmarkDirectory(null, path))
        showToast(R.string.file_add_bookmark_success)
    }

    override fun createShortcut(file: FileItem) {
        createShortcut(file.path, file.mimeType)
    }

    private fun createShortcut() {
        createShortcut(currentPath, MimeType.DIRECTORY)
    }

    private fun createShortcut(path: Path, mimeType: MimeType) {
        val context = requireContext()
        val isDirectory = mimeType == MimeType.DIRECTORY
        val shortcutInfo = ShortcutInfoCompat.Builder(context, path.toString())
            .setShortLabel(path.name)
            .setIntent(
                if (isDirectory) {
                    FileListActivity.createViewIntent(path)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                } else {
                    OpenFileActivity.createIntent(path, mimeType)
                }
            )
            .setIcon(
                IconCompat.createWithResource(
                    context, if (isDirectory) {
                        R.mipmap.directory_shortcut_icon
                    } else {
                        R.mipmap.file_shortcut_icon
                    }
                )
            )
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            showToast(R.string.shortcut_created)
        }
    }

    override fun showPropertiesDialog(file: FileItem) {
        FilePropertiesDialogFragment.show(file, this)
    }

    private fun showCreateFileDialog() {
        CreateFileDialogFragment.show(this)
    }

    override fun createFile(name: String) {
        Log.i(TAG, "createFile, name: $name")
        val path = currentPath.resolve(name)
        FileJobService.create(path, false, requireContext())
    }

    private fun showCreateDirectoryDialog() {
        Log.i(TAG, "showCreateDirectoryDialog")
        CreateDirectoryDialogFragment.show(this)
    }

    override fun createDirectory(name: String) {
        Log.i(TAG, "createDirectory, name: $name")
        val path = currentPath.resolve(name)
        FileJobService.create(path, true, requireContext())
    }

    override val currentPath: Path
        get() = viewModel.currentPath

    override val currentTabType: Int
        get() = viewModel.currentTabType

    override fun navigateToRoot(path: Path, tabType: Int) {
        Log.i(TAG, "navigateToRoot, tabType: $tabType, path: $path")
        controlMenuItemShow(tabType)
        createListLayoutData(tabType, path.compare(currentPath), path = path)
        viewModel.updateCurrentTabType(tabType)
        setGridLayoutManagerSpanCount(tabType)
        changeLayoutItemIcon(tabType)
        viewModel.resetTo(path)
    }

    override fun browseContent(tabType: Int) {
        Log.i(TAG, "browseContent, tabType: $tabType")
        controlMenuItemShow(tabType)
        createListLayoutData(tabType)
        viewModel.updateCurrentTabType(tabType)
        setGridLayoutManagerSpanCount(tabType)
        changeLayoutItemIcon(tabType)
        viewModel.resetTo(Paths.get(Environment.getExternalStorageDirectory().absolutePath))
    }

    override fun navigateToDefaultRoot() {
        navigateToRoot(Paths.get(DEFAULT_PATH), TabType.RECENT)
    }

    override fun observeCurrentPath(owner: LifecycleOwner, observer: (Path) -> Unit) {
        viewModel.currentPathLiveData.observe(owner, observer)
    }

    override fun observeCurrentTabType(owner: LifecycleOwner, observer: (Int) -> Unit) {
        viewModel.currentTabTypeLiveData.observe(owner, observer)
    }

    override fun closeNavigationDrawer() {
        binding.drawerLayout?.closeDrawer(GravityCompat.START)
    }

    private fun controlMenuItemShow(tabType: Int) {
        // 最近更新、我的收藏Tab，添加文件夹Menu都不加
        if (this::menuBinding.isInitialized) {
            menuBinding.menu.children.forEach {
                it.isVisible = if (it.itemId == R.id.action_add_file) {
                    tabType == TabType.ALL
                } else {
                    tabType != TabType.RECENT
                }
            }
        }
    }

    private fun ensureStorageAccess() {
        if (viewModel.isStorageAccessRequested) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                ShowRequestAllFilesAccessRationaleDialogFragment.show(this)
                viewModel.isStorageAccessRequested = true
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (shouldShowRequestPermissionRationale(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    ShowRequestStoragePermissionRationaleDialogFragment.show(this)
                } else {
                    requestStoragePermission()
                }
                viewModel.isStorageAccessRequested = true
            }
        }
    }

    override fun requestAllFilesAccess() {
        requestAllFilesAccessLauncher.launch(Unit)
    }

    private fun onRequestAllFilesAccessResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.isStorageAccessRequested = false
            refresh()
        }
    }

    override fun requestStoragePermission() {
        requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun onRequestStoragePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.isStorageAccessRequested = false
            refresh()
        } else if (!shouldShowRequestPermissionRationale(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            ShowRequestStoragePermissionInSettingsRationaleDialogFragment.show(this)
        }
    }

    override fun requestStoragePermissionInSettings() {
        requestStoragePermissionInSettingsLauncher.launch(Unit)
    }

    private fun onRequestStoragePermissionInSettingsResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.isStorageAccessRequested = false
            refresh()
        }
    }

    private fun getPastFilePathInfo(activityResult: ActivityResult) {
        // 回传得到结果
        // 复制后粘贴、剪切均在这里接收targetPath
        Log.i(TAG, "getPastFilePathInfo, activityResult: $activityResult")
        if (activityResult.resultCode == RESULT_CODE) {
            pasteFiles(Paths.get(activityResult.data?.getStringExtra(PATH)))
            if (viewModel.pasteState.copy) {
                CopyMoveDeleteProgressDialogFragment.show(CopyMoveDeleteType.COPY, this)
            }
            /* TODO 后续添加MOVE进度监听后恢复
            CopyMoveDeleteProgressDialogFragment.show(
                if (viewModel.pasteState.copy) CopyMoveDeleteType.COPY else CopyMoveDeleteType.MOVE,
                this
            )*/
        }
    }

    companion object {
        private const val ACTION_VIEW_DOWNLOADS =
            "com.baidu.duer.files.intent.action.VIEW_DOWNLOADS"

        private const val IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX = 1000
        private const val TAG = "FileListFragment"
    }

    private class RequestAllFilesAccessContract : ActivityResultContract<Unit, Boolean>() {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(
                android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.fromParts("package", context.packageName, null)
            )

        @RequiresApi(Build.VERSION_CODES.R)
        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            Environment.isExternalStorageManager()
    }

    private class RequestStoragePermissionInSettingsContract
        : ActivityResultContract<Unit, Boolean>() {
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            application.checkSelfPermissionCompat(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
    }

    @Parcelize
    class Args(val intent: Intent) : ParcelableArgs

    private class Binding private constructor(
        val root: View,
        val drawerLayout: DrawerLayout? = null,
        val persistentDrawerLayout: PersistentDrawerLayout? = null,
        val navigationFrameLayout: NavigationFrameLayout,
        val appBarLayout: CoordinatorAppBarLayout,
        val toolbar: Toolbar,
        val currentTitle: TextView,
        val navigateUpButton: ImageView,
        val overlayToolbar: Toolbar,
        val breadcrumbLayout: BreadcrumbLayout,
        val contentLayout: ViewGroup,
        val loading: CommonLoadingView,
        val errorText: TextView,
        val emptyView: TextView,
        val swipeRefreshLayout: SmartRefreshLayout,
        val recyclerView: RecyclerView,
        val breadcrumbDivide: View
    ) {
        companion object {
            fun inflate(
                inflater: LayoutInflater,
                root: ViewGroup?,
                attachToRoot: Boolean
            ): Binding {
                val binding = FileListFragmentBinding.inflate(inflater, root, attachToRoot)
                val bindingRoot = binding.root
                val includeBinding = FileListFragmentIncludeBinding.bind(bindingRoot)
                val appBarBinding = FileListFragmentAppBarIncludeBinding.bind(bindingRoot)
                val contentBinding = FileListFragmentContentIncludeBinding.bind(bindingRoot)
                appBarBinding.idCurrentTitle.typeface = createFromAsset()
                return Binding(
                    bindingRoot,
                    includeBinding.drawerLayout,
                    includeBinding.persistentDrawerLayout,
                    includeBinding.navigationFragment,
                    appBarBinding.appBarLayout,
                    appBarBinding.toolbar,
                    appBarBinding.idCurrentTitle,
                    appBarBinding.idImgButtonBack,
                    appBarBinding.overlayToolbar,
                    appBarBinding.breadcrumbLayout,
                    contentBinding.contentLayout,
                    contentBinding.progress,
                    contentBinding.errorText,
                    contentBinding.emptyView,
                    contentBinding.swipeRefreshLayout,
                    contentBinding.recyclerView,
                    contentBinding.breadcrumbDivide
                )
            }
        }
    }

    private class MenuBinding private constructor(
        val menu: Menu,
        val addFileItem: MenuItem,
        val layoutChange: MenuItem,
        val sortItem: MenuItem
    ) {
        companion object {
            fun inflate(menu: Menu, inflater: MenuInflater): MenuBinding {
                inflater.inflate(R.menu.file_list, menu)
                return MenuBinding(
                    menu,
                    menu.findItem(R.id.action_add_file),
                    menu.findItem(R.id.action_layout_change),
                    menu.findItem(R.id.action_sort),
                )
            }
        }
    }
}
