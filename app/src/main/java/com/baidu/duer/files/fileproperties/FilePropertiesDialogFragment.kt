package com.baidu.duer.files.fileproperties

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.FilePropertiesDialogBinding
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filelist.name
import com.baidu.duer.files.fileproperties.apk.FilePropertiesApkTabFragment
import com.baidu.duer.files.fileproperties.audio.FilePropertiesAudioTabFragment
import com.baidu.duer.files.fileproperties.basic.FilePropertiesBasicTabFragment
import com.baidu.duer.files.fileproperties.image.FilePropertiesImageTabFragment
import com.baidu.duer.files.fileproperties.permissions.FilePropertiesPermissionsTabFragment
import com.baidu.duer.files.fileproperties.video.FilePropertiesVideoTabFragment
import com.baidu.duer.files.ui.TabFragmentPagerAdapter
import com.baidu.duer.files.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize

class FilePropertiesDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { FilePropertiesFileViewModel(args.file) } }

    private lateinit var binding: FilePropertiesDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(getString(R.string.file_properties_title_format, args.file.name))
            .apply {
                binding = FilePropertiesDialogBinding.inflate(context.layoutInflater)
                setView(binding.root)
            }
            .setPositiveButton(android.R.string.ok, null)
            .create()

    // HACK: Work around child FragmentManager requiring a view.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize the shared file view model before child fragments are created.
        viewModel.fileLiveData
        val tabs = mutableListOf<Pair<Int, () -> Fragment>>()
            .apply {
                add(R.string.file_properties_basic to { FilePropertiesBasicTabFragment() })
                if (FilePropertiesPermissionsTabFragment.isAvailable(args.file)) {
                    add(
                        R.string.file_properties_permissions
                                to { FilePropertiesPermissionsTabFragment() }
                    )
                }
                if (FilePropertiesImageTabFragment.isAvailable(args.file)) {
                    add(
                        R.string.file_properties_image to {
                            FilePropertiesImageTabFragment().putArgs(
                                FilePropertiesImageTabFragment.Args(
                                    args.file.path, args.file.mimeType
                                )
                            )
                        }
                    )
                }
                if (FilePropertiesAudioTabFragment.isAvailable(args.file)) {
                    add(
                        R.string.file_properties_audio to {
                            FilePropertiesAudioTabFragment().putArgs(
                                FilePropertiesAudioTabFragment.Args(args.file.path)
                            )
                        }
                    )
                }
                if (FilePropertiesVideoTabFragment.isAvailable(args.file)) {
                    add(
                        R.string.file_properties_video to {
                            FilePropertiesVideoTabFragment().putArgs(
                                FilePropertiesVideoTabFragment.Args(args.file.path)
                            )
                        }
                    )
                }
                if (FilePropertiesApkTabFragment.isAvailable(args.file)) {
                    add(
                        R.string.file_properties_apk to {
                            FilePropertiesApkTabFragment().putArgs(
                                FilePropertiesApkTabFragment.Args(args.file.path)
                            )
                        }
                    )
                }
            }
            .map { getString(it.first) to it.second }
            .toTypedArray()
        val tabAdapter = TabFragmentPagerAdapter(childFragmentManager, *tabs)
        binding.viewPager.offscreenPageLimit = tabAdapter.count - 1
        binding.viewPager.adapter = tabAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            FilePropertiesDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }

    @Parcelize
    class Args(val file: FileItem) : ParcelableArgs
}
