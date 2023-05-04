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
import com.baidu.duer.files.navigation.BookmarkDirectory
import com.baidu.duer.files.util.createIntent
import com.baidu.duer.files.util.startActivitySafe

class BookmarkDirectoriesPreference : Preference {
    private var emptySummary = summary

    private val observer = Observer<List<BookmarkDirectory>> { onBookmarkDirectoryListChanged(it) }

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

        Settings.BOOKMARK_DIRECTORIES.observeForever(observer)
    }

    override fun onDetached() {
        super.onDetached()

        Settings.BOOKMARK_DIRECTORIES.removeObserver(observer)
    }

    private fun onBookmarkDirectoryListChanged(bookmarkDirectories: List<BookmarkDirectory>) {
        val names = bookmarkDirectories.map { it.name }
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
        context.startActivitySafe(BookmarkDirectoryListActivity::class.createIntent())
    }
}
