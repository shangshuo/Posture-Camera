package com.posturecamera.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.posturecamera.app.data.model.PhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * 照片数据访问对象
 */
@Dao
interface PhotoDao {

    /**
     * 插入照片记录
     *
     * @param photo 照片实体
     * @return 插入的记录ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    /**
     * 获取所有照片（按时间戳降序）
     *
     * @return 照片列表Flow
     */
    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    /**
     * 根据ID获取照片
     *
     * @param id 照片ID
     * @return 照片实体，如果不存在则返回null
     */
    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): PhotoEntity?

    /**
     * 删除照片记录
     *
     * @param photo 照片实体
     */
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
}
