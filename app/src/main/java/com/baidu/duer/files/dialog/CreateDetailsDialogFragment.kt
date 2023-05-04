package com.baidu.duer.files.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import coil.dispose
import coil.load
import com.baidu.duer.files.databinding.DetailsDialogFragmentBinding
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.MimeTypeIcon
import com.baidu.duer.files.file.icon
import com.baidu.duer.files.file.iconRes
import com.baidu.duer.files.filelist.name
import com.baidu.duer.files.filelist.supportsThumbnail
import com.baidu.duer.files.navigation.createFromAsset
import com.baidu.duer.files.util.*
import kotlinx.parcelize.Parcelize

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/14
 * @Description :
 */
class CreateDetailsDialogFragment : BaseDialogFragment() {
    private val args by args<Args>()
    private lateinit var mAdapter: DetailsDialogAdapter
    private val binding by lazy {
        DetailsDialogFragmentBinding.inflate(requireContext().layoutInflater)
    }

    override val width: Int
        get() = dpToDimensionPixelOffset(404)

    override val radius: Float
        get() = 16f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun initData() {
        val file = args.file
        mAdapter = DetailsDialogAdapter(requireContext())
        binding.apply {
            idFileIcon.setImageResource(file.mimeType.iconRes)
            idFileIcon.isVisible = true
            thumbnailImage.dispose()
            thumbnailImage.setImageDrawable(null)
            val supportsThumbnail = file.supportsThumbnail
            thumbnailImage.isVisible = supportsThumbnail
            val attributes = file.attributes
            if (supportsThumbnail) {
                thumbnailImage.load(file.path to attributes) {
                    listener { _, _ -> idFileIcon.isVisible = false }
                }
            }
            idFileName.typeface = createFromAsset()
            idFileName.text = file.name
            idFileInfoRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = mAdapter
                isNestedScrollingEnabled = false
            }
        }

        val fileInfoList = when (file.mimeType.icon) {
            MimeTypeIcon.IMAGE -> file.imageInfoList(requireContext())
            MimeTypeIcon.VIDEO -> file.videoInfoList(requireContext())
            MimeTypeIcon.AUDIO -> file.audioInfoList(requireContext())
            MimeTypeIcon.DIRECTORY -> file.directoryInfoList(requireContext())
            else -> file.fileInfoList(requireContext())
        }
        mAdapter.updateInfoListData(fileInfoList)
    }

    @Parcelize
    class Args(val file: FileItem) : ParcelableArgs

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            CreateDetailsDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }
}
