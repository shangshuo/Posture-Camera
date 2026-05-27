package com.posturecamera.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.posturecamera.app.data.model.PhotoEntity

/**
 * Room数据库
 */
@Database(
    entities = [PhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * 获取照片DAO
     *
     * @return PhotoDao实例
     */
    abstract fun photoDao(): PhotoDao
}
