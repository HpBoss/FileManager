package com.baidu.duer.files.settings

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.baidu.duer.files.compat.ListFormatterCompat
import com.baidu.duer.files.navigation.StandardDirectoriesLiveData
import com.baidu.duer.files.navigation.StandardDirectory
import com.baidu.duer.files.util.createIntent

class StandardDirectoriesPreference : Preference {
    private val observer = Observer<List<StandardDirectory>> { onStandardDirectoriesChanged(it) }
    private var emptySummary = summary

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

    init {
        isPersistent = false
    }

    override fun onAttached() {
        super.onAttached()

        StandardDirectoriesLiveData.observeForever(observer)
    }

    override fun onDetached() {
        super.onDetached()

        StandardDirectoriesLiveData.removeObserver(observer)
    }

    private fun onStandardDirectoriesChanged(standardDirectories: List<StandardDirectory>) {
        val context = context
        val titles = standardDirectories.filter { it.isEnabled }.map { it.getTitle(context) }
        val summary = if (titles.isNotEmpty()) ListFormatterCompat.format(titles) else emptySummary
        setSummary(summary)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val summaryText = holder.findViewById(android.R.id.summary) as TextView
        summaryText.ellipsize = TextUtils.TruncateAt.END
        summaryText.isSingleLine = true
    }

    override fun onClick() {
        context.startActivity(StandardDirectoryListActivity::class.createIntent())
    }
}
