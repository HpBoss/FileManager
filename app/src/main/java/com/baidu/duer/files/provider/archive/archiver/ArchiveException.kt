package com.baidu.duer.files.provider.archive.archiver

//#ifdef NONFREE
import com.github.junrar.exception.RarException
//#endif
import org.apache.commons.compress.compressors.CompressorException
import java.io.IOException
import org.apache.commons.compress.archivers.ArchiveException as ApacheArchiveException

class ArchiveException : IOException {
    constructor(cause: ApacheArchiveException) : super(cause)

    constructor(cause: CompressorException) : super(cause)

    //#ifdef NONFREE
    constructor(cause: RarException) : super(cause)
//#endif
}
