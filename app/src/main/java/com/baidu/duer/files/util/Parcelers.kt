package com.baidu.duer.files.util

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.baidu.duer.files.app.appClassLoader
import com.baidu.duer.files.compat.writeParcelableListCompat
import kotlinx.parcelize.Parceler

object BundleParceler : Parceler<Bundle?> {
    override fun create(parcel: Parcel): Bundle? = parcel.readBundle(appClassLoader)

    override fun Bundle?.write(parcel: Parcel, flags: Int) {
        parcel.writeBundle(this)
    }
}

object ParcelableParceler : Parceler<Any?> {
    override fun create(parcel: Parcel): Any? = parcel.readParcelable(appClassLoader)

    override fun Any?.write(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(this as Parcelable?, flags)
    }
}

object ParcelableListParceler : Parceler<List<Any?>> {
    override fun create(parcel: Parcel): List<Any?> =
        parcel.readParcelableListCompat(appClassLoader)

    override fun List<Any?>.write(parcel: Parcel, flags: Int) {
        @Suppress("UNCHECKED_CAST")
        parcel.writeParcelableListCompat(this as List<Parcelable?>, flags)
    }
}

object ValueParceler : Parceler<Any?> {
    override fun create(parcel: Parcel): Any? = parcel.readValue(appClassLoader)

    override fun Any?.write(parcel: Parcel, flags: Int) {
        parcel.writeValue(this)
    }
}
