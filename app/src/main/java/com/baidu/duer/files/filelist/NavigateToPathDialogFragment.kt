package com.baidu.duer.files.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.util.*
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

class NavigateToPathDialogFragment : PathDialogFragment() {
    private val args by args<Args>()

    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_list_navigate_to_title

    override val initialName: String?
        get() = args.path.toUserFriendlyString()

    override fun onOk(path: Path) {
        listener.navigateTo(path)
    }

    companion object {
        fun show(path: Path, fragment: Fragment) {
            NavigateToPathDialogFragment().putArgs(Args(path)).show(fragment)
        }
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs

    interface Listener : NameDialogFragment.Listener {
        fun navigateTo(path: Path)
    }
}
