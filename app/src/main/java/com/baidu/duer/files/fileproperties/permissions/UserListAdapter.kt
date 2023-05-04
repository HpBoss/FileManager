package com.baidu.duer.files.fileproperties.permissions

import androidx.annotation.DrawableRes
import com.baidu.duer.files.R
import com.baidu.duer.files.util.SelectionLiveData

class UserListAdapter(
    selectionLiveData: SelectionLiveData<Int>
) : PrincipalListAdapter(selectionLiveData) {
    @DrawableRes
    override val principalIconRes: Int = R.drawable.person_icon_control_normal_24dp
}
