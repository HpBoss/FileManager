package com.baidu.duer.files.provider.common

import android.os.Parcel
import com.baidu.duer.files.compat.readSerializableCompat
import java8.nio.file.attribute.FileTime
import kotlinx.parcelize.Parceler
import org.threeten.bp.Instant

object FileTimeParceler : Parceler<FileTime?> {
    override fun create(parcel: Parcel): FileTime? =
        parcel.readSerializableCompat<Instant>()?.let { FileTime.from(it) }

    override fun FileTime?.write(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(this?.toInstant())
    }
}
