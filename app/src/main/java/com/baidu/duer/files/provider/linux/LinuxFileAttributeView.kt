package com.baidu.duer.files.provider.linux

import android.os.Parcel
import android.os.Parcelable
import com.baidu.duer.files.compat.readBooleanCompat
import com.baidu.duer.files.compat.writeBooleanCompat
import com.baidu.duer.files.provider.root.RootPosixFileAttributeView
import com.baidu.duer.files.provider.root.RootablePosixFileAttributeView
import com.baidu.duer.files.util.readParcelable

internal class LinuxFileAttributeView constructor(
    private val path: LinuxPath,
    private val noFollowLinks: Boolean
) : RootablePosixFileAttributeView(
    path, LocalLinuxFileAttributeView(path.toByteString(), noFollowLinks),
    { RootPosixFileAttributeView(it) }
) {
    private constructor(source: Parcel) : this(
        source.readParcelable()!!, source.readBooleanCompat()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(path, flags)
        dest.writeBooleanCompat(noFollowLinks)
    }

    companion object {
        val SUPPORTED_NAMES = LocalLinuxFileAttributeView.SUPPORTED_NAMES

        @JvmField
        val CREATOR = object : Parcelable.Creator<LinuxFileAttributeView> {
            override fun createFromParcel(source: Parcel): LinuxFileAttributeView =
                LinuxFileAttributeView(source)

            override fun newArray(size: Int): Array<LinuxFileAttributeView?> = arrayOfNulls(size)
        }
    }
}
