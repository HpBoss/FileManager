package com.baidu.duer.files.provider.common

import android.os.Parcel
import android.os.Parcelable
import com.baidu.duer.files.compat.readSerializableCompat
import java8.nio.file.attribute.FileTime

class ParcelableFileTime(val value: FileTime) : Parcelable {
    private constructor(source: Parcel) : this(FileTime.from(source.readSerializableCompat()))

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(value.toInstant())
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableFileTime> {
            override fun createFromParcel(source: Parcel): ParcelableFileTime =
                ParcelableFileTime(source)

            override fun newArray(size: Int): Array<ParcelableFileTime?> = arrayOfNulls(size)
        }
    }
}

fun FileTime.toParcelable(): ParcelableFileTime = ParcelableFileTime(this)
