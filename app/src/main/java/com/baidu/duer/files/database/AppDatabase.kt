package com.baidu.duer.files.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/7
 * @Description :
 */
@Database(entities = [Tab::class, Collect::class, Mime::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var instance: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(Database::class.java) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java, "database-app"
                        )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration() // 数据库更新时删除数据重新创建
                            // .addMigrations(MIGRATION_1_2) // 或者指定版本1-2升级时的升级策略，因此会新建一个表
                            .build()
                    }
                }
            }
            return instance
        }
    }

    abstract fun tabDao(): TabDao

    abstract fun collectDao(): CollectDao

    abstract fun mimeDao(): MimeDao
}
    