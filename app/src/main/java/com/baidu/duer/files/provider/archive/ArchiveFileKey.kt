package com.baidu.duer.files.provider.archive

import android.os.Parcelable
import com.baidu.duer.files.util.ParcelableParceler
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

@Parcelize
internal data class ArchiveFileKey(
    private val archiveFile: @WriteWith<ParcelableParceler> Path,
    private val entryName: String
) : Parcelable
