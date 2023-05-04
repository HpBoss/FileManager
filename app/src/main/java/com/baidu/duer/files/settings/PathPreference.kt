package com.baidu.duer.files.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.res.TypedArrayUtils
import androidx.core.content.res.use
import androidx.preference.Preference
import com.baidu.duer.files.R
import com.baidu.duer.files.filelist.FileListActivity
import com.baidu.duer.files.filelist.toUserFriendlyString
import com.baidu.duer.files.navigation.NavigationRootMapLiveData
import com.baidu.duer.files.util.startActivityForResultSafe
import com.baidu.duer.files.util.valueCompat
import com.takisoft.preferencex.PreferenceActivityResultListener
import com.takisoft.preferencex.PreferenceFragmentCompat
import java8.nio.file.Path

abstract class PathPreference : Preference, PreferenceActivityResultListener {
    private val pickDirectoryContract = FileListActivity.PickDirectoryContract()

    var path: Path = persistedPath
        set(value) {
            if (field == value) {
                return
            }
            field = value
            persistedPath = value
            notifyChanged()
        }

    constructor(context: Context) : super(context) {
        init(null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(attrs, defStyleAttr, 0)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr, defStyleRes)
    }

    @SuppressLint("PrivateResource", "RestrictedApi")
    private fun init(attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) {
        isPersistent = false
        context.obtainStyledAttributes(
            attrs, R.styleable.EditTextPreference, defStyleAttr, defStyleRes
        ).use {
            if (TypedArrayUtils.getBoolean(
                    it, R.styleable.EditTextPreference_useSimpleSummaryProvider,
                    R.styleable.EditTextPreference_useSimpleSummaryProvider, false
                )
            ) {
                summaryProvider = SimpleSummaryProvider
            }
        }
    }

    override fun onPreferenceClick(fragment: PreferenceFragmentCompat, preference: Preference) {
        fragment.startActivityForResultSafe(
            pickDirectoryContract.createIntent(fragment.requireContext(), path), requestCode
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == this.requestCode) {
            val result = pickDirectoryContract.parseResult(resultCode, data)
            if (result != null) {
                path = result
            }
        }
    }

    private val requestCode: Int
        // @see FragmentActivity#checkForValidRequestCode()
        get() = key.hashCode() and 0x0000FFFF

    protected abstract var persistedPath: Path

    object SimpleSummaryProvider : SummaryProvider<PathPreference> {
        override fun provideSummary(preference: PathPreference): CharSequence? {
            val path = preference.path
            val navigationRoot = NavigationRootMapLiveData.valueCompat[path]
            return navigationRoot?.getName(preference.context) ?: path.toUserFriendlyString()
        }
    }
}
