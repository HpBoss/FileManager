package com.baidu.duer.files.settings

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.baidu.duer.files.R
import com.baidu.duer.files.filejob.FileJobService
import com.baidu.duer.files.ui.MaterialPreferenceDialogFragmentCompat
import com.takisoft.preferencex.PreferenceFragmentCompat
import rikka.preference.SimpleMenuPreference

class RootStrategyPreference : SimpleMenuPreference {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onClick() {
        val jobCount = FileJobService.runningJobCount
        if (jobCount == 0) {
            super.onClick()
            return
        }
        dialogTitle = null
        dialogMessage = context.resources.getQuantityString(
            R.plurals.settings_root_strategy_message_format, jobCount, jobCount
        )
        setPositiveButtonText(android.R.string.yes)
        setNegativeButtonText(R.string.maybe_later)
        preferenceManager.showDialog(this)
    }

    private fun superOnClick() {
        super.onClick()
    }

    companion object {
        init {
            PreferenceFragmentCompat.registerPreferenceFragment(
                RootStrategyPreference::class.java, DialogFragment::class.java
            )
        }
    }

    private class DialogFragment : MaterialPreferenceDialogFragmentCompat() {
        override fun onDialogClosed(positiveResult: Boolean) {
            val preference = preference as RootStrategyPreference
            if (positiveResult) {
                preference.superOnClick()
            }
        }
    }
}
