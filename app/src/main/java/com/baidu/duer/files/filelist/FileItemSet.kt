package com.baidu.duer.files.filelist

import android.os.Parcel
import android.os.Parcelable
import com.baidu.duer.files.compat.writeParcelableListCompat
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.util.LinkedMapSet
import com.baidu.duer.files.util.readParcelableListCompat
import java8.nio.file.Path

class FileItemSet() : LinkedMapSet<Path, FileItem>(FileItem::path), Parcelable {
    constructor(parcel: Parcel) : this() {
        addAll(parcel.readParcelableListCompat())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelableListCompat(toList(), flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FileItemSet> {
        override fun createFromParcel(parcel: Parcel): FileItemSet = FileItemSet(parcel)

        override fun newArray(size: Int): Array<FileItemSet?> = arrayOfNulls(size)
    }
}

fun fileItemSetOf(vararg files: FileItem) = FileItemSet().apply { addAll(files) }

/**
 * 从FileItemSet 查询指定条件的元素是否存在
 */
fun FileItemSet.isContain(func: (file: FileItem) -> Boolean): Boolean {
    this.forEach {
        if (func(it)) return true
    }
    return false
}
