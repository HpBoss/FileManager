package com.baidu.duer.files.file

import android.net.Uri
import android.provider.DocumentsContract
import com.baidu.duer.files.app.contentResolver
import com.baidu.duer.files.compat.DocumentsContractCompat

@JvmInline
value class DocumentUri(val value: Uri) {
    val documentId: String
        get() = DocumentsContract.getDocumentId(value)
}

fun Uri.asDocumentUriOrNull(): DocumentUri? =
    if (isDocumentUri) DocumentUri(this) else null

fun Uri.asDocumentUri(): DocumentUri {
    require(isDocumentUri)
    return DocumentUri(this)
}

private val Uri.isDocumentUri: Boolean
    get() = DocumentsContractCompat.isDocumentUri(this)

val DocumentUri.displayName: String?
    get() {
        try {
            contentResolver.query(
                value, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME
                    )
                    if (displayNameIndex != -1) {
                        val displayName = cursor.getString(displayNameIndex)
                        if (!displayName.isNullOrEmpty()) {
                            return displayName
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

val DocumentUri.displayNameOrUri: String
    get() = displayName ?: value.toString()
