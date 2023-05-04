package com.baidu.duer.files.fileproperties.permissions

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filejob.FileJobService
import com.baidu.duer.files.provider.common.PosixFileAttributes
import com.baidu.duer.files.provider.common.PosixPrincipal
import com.baidu.duer.files.provider.common.PosixUser
import com.baidu.duer.files.provider.common.toByteString
import com.baidu.duer.files.util.SelectionLiveData
import com.baidu.duer.files.util.putArgs
import com.baidu.duer.files.util.show
import com.baidu.duer.files.util.viewModels
import java8.nio.file.Path

class SetOwnerDialogFragment : SetPrincipalDialogFragment() {
    override val viewModel: SetPrincipalViewModel by viewModels { { SetOwnerViewModel() } }

    @StringRes
    override val titleRes: Int = R.string.file_properties_permissions_set_owner_title

    override fun createAdapter(selectionLiveData: SelectionLiveData<Int>): PrincipalListAdapter =
        UserListAdapter(selectionLiveData)

    override val PosixFileAttributes.principal: PosixPrincipal
        get() = owner()!!

    override fun setPrincipal(path: Path, principal: PrincipalItem, recursive: Boolean) {
        val owner = PosixUser(principal.id, principal.name?.toByteString())
        FileJobService.setOwner(path, owner, recursive, requireContext())
    }

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            SetOwnerDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }
}
