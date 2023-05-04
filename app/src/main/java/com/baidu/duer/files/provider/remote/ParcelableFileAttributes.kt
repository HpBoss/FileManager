package com.baidu.duer.files.provider.remote

import android.os.Parcel
import android.os.Parcelable
import com.baidu.duer.files.compat.readSerializableCompat
import com.baidu.duer.files.provider.common.PosixFileMode
import com.baidu.duer.files.provider.common.PosixFileModeBit
import com.baidu.duer.files.provider.common.toAttribute
import com.baidu.duer.files.util.toEnumSet
import java8.nio.file.attribute.FileAttribute
import java.io.Serializable

class ParcelableFileAttributes(val value: Array<out FileAttribute<*>>) : Parcelable {
    private constructor(source: Parcel) : this(
        source.readSerializableCompat<List<Set<PosixFileModeBit>>>()!!
            .map { it.toAttribute() }
            .toTypedArray()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        //noinspection unchecked
        val modes = value.map {
            when (val mode = PosixFileMode.fromAttribute(it)) {
                is Serializable -> mode
                else -> mode.toEnumSet()
            }
        }
        dest.writeSerializable(modes as Serializable)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableFileAttributes> {
            override fun createFromParcel(source: Parcel): ParcelableFileAttributes =
                ParcelableFileAttributes(source)

            override fun newArray(size: Int): Array<ParcelableFileAttributes?> = arrayOfNulls(size)
        }
    }
}

fun Array<out FileAttribute<*>>.toParcelable(): ParcelableFileAttributes =
    ParcelableFileAttributes(this)
