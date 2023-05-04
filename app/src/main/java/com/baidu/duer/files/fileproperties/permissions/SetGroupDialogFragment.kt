package com.baidu.duer.files.fileproperties.permissions

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filejob.FileJobService
import com.baidu.duer.files.provider.common.PosixFileAttributes
import com.baidu.duer.files.provider.common.PosixGroup
import com.baidu.duer.files.provider.common.toByteString
import com.baidu.duer.files.util.SelectionLiveData
import com.baidu.duer.files.util.putArgs
import com.baidu.duer.files.util.show
import com.baidu.duer.files.util.viewModels
import java8.nio.file.Path

class SetGroupDialogFragment : SetPrincipalDialogFragment() {
    override val viewModel: SetPrincipalViewModel by viewModels { { SetGroupViewModel() } }

    @StringRes
    override val titleRes: Int = R.string.file_properties_permissions_set_group_title

    override fun createAdapter(selectionLiveData: SelectionLiveData<Int>): PrincipalListAdapter =
        GroupListAdapter(selectionLiveData)

    override val PosixFileAttributes.principal
        get() = group()!!

    override fun setPrincipal(path: Path, principal: PrincipalItem, recursive: Boolean) {
        val group = PosixGroup(principal.id, principal.name?.toByteString())
        FileJobService.setGroup(path, group, recursive, requireContext())
    }

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            SetGroupDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }
}
