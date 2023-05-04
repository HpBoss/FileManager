package com.baidu.duer.files.provider.archive

import android.os.Parcel
import android.os.Parcelable
import com.baidu.duer.files.provider.root.RootPosixFileStore
import com.baidu.duer.files.provider.root.RootablePosixFileStore
import java8.nio.file.Path

internal class ArchiveFileStore(private val archiveFile: Path) : RootablePosixFileStore(
    archiveFile, LocalArchiveFileStore(archiveFile), { RootPosixFileStore(it) }
) {
    private constructor(source: Parcel) : this(
        source.readParcelable<Parcelable>(Path::class.java.classLoader) as Path
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(archiveFile as Parcelable, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ArchiveFileStore> {
            override fun createFromParcel(source: Parcel): ArchiveFileStore =
                ArchiveFileStore(source)

            override fun newArray(size: Int): Array<ArchiveFileStore?> = arrayOfNulls(size)
        }
    }
}
