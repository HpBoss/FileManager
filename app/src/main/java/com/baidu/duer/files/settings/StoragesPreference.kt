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
import com.baidu.duer.files.storage.Storage
import com.baidu.duer.files.storage.StorageListActivity
import com.baidu.duer.files.util.createIntent
import com.baidu.duer.files.util.startActivitySafe

class StoragesPreference : Preference {
    private var emptySummary = summary

    private val observer = Observer<List<Storage>> { onStorageListChanged(it) }

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

        Settings.STORAGES.observeForever(observer)
    }

    override fun onDetached() {
        super.onDetached()

        Settings.STORAGES.removeObserver(observer)
    }

    private fun onStorageListChanged(storages: List<Storage>) {
        val context = context
        val names = storages.filter { it.isVisible }.map { it.getName(context) }
        val summary = if (names.isNotEmpty()) ListFormatterCompat.format(names) else emptySummary
        setSummary(summary)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val summaryText = holder.findViewById(android.R.id.summary) as TextView
        summaryText.ellipsize = TextUtils.TruncateAt.END
        summaryText.isSingleLine = true
    }

    override fun onClick() {
        context.startActivitySafe(StorageListActivity::class.createIntent())
    }
}
