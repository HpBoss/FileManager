package com.baidu.duer.files.assort

import android.provider.MediaStore
import com.baidu.duer.files.R
import com.baidu.duer.files.app.application
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.formatShort
import com.baidu.duer.files.file.loadFileItem
import com.baidu.duer.files.filelist.name
import com.baidu.duer.files.navigation.getCompressResources
import com.baidu.duer.files.navigation.getMediaStoreDirectory
import com.baidu.duer.files.navigation.getMimeTypeResources
import com.baidu.duer.files.util.*
import java8.nio.file.Paths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.io.File

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/22
 * @Description :
 */
class FileRecentListLiveData : CloseableLiveData<Stateful<List<FileItem>>>() {
    private var mCurrentData: LocalDate = LocalDate.MAX
    private var weekDays = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")

    init {
        loadValue()
    }

    override fun loadValue() {
        coroutineScope?.cancel()
        value = Loading(value?.value)
        coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope?.launch {
            val value = try {
                Success(getNewRecent() as List<FileItem>)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }

    private fun getNewRecent(): ArrayList<FileItem> {
        val multipleResultList = arrayListOf(
            getMediaStoreDirectory(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            getMediaStoreDirectory(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI),
            getMediaStoreDirectory(MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
            getMimeTypeResources(extraDocumentMimeTypes),
            getCompressResources(),
            getMimeTypeResources(arrayListOf(apkMimeType))
        )

        val resultList =
            multipleResultList.stream().collect(
                { ArrayList() },
                { obj: ArrayList<FileItem>, c: ArrayList<FileItem> ->
                    obj.addAll(
                        c
                    )
                }
            ) { obj: ArrayList<FileItem>, c: ArrayList<FileItem> ->
                obj.addAll(
                    c
                )
            }.apply {
                sortByDescending {
                    it.attributes.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDate().toEpochDay()
                }
            }

        val addTitleFileList: ArrayList<FileItem> = ArrayList()
        resultList.forEach {
            // 不展示隐藏文件
            if (it.name.startsWith(".")) return@forEach
            val item = Paths.get(it.path.toString()).loadFileItem()
            val modifyDate = item.attributes.lastModifiedTime()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

            if (File(it.path.toString()).exists()
                && LocalDate.now().toEpochDay() - modifyDate.toEpochDay() <= 30
            ) {
                val modifyShortDate =
                    item.attributes.lastModifiedTime().toInstant().formatShort(application)
                if (modifyDate != mCurrentData) {
                    val description =
                        when (LocalDate.now().toEpochDay() - modifyDate.toEpochDay()) {
                            in 1..7 -> modifyShortDate + "\u0020" + weekDays[modifyDate.dayOfWeek.value - 1]
                            0L -> modifyDate.format(DateTimeFormatter.ofPattern("M月d日")) +
                                    "\u0020" + application.getString(R.string.date_today_suffix)
                            else -> modifyShortDate
                        }
                    mCurrentData = modifyDate
                    item.fileModifyDate = description
                    addTitleFileList.add(item)
                }
                addTitleFileList.add(it)
            }
        }

        if (addTitleFileList.isNotEmpty()) {
            val newItem = Paths.get(addTitleFileList.last().path.toUri()).loadFileItem()
            newItem.otherType = BOTTOM_NOTICE
            addTitleFileList.add(newItem)
        }
        return addTitleFileList
    }

    companion object {
        const val BOTTOM_NOTICE = "bottom_notice"
    }
}
