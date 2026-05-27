package com.posturecamera.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.posturecamera.app.data.local.PhotoDao
import com.posturecamera.app.data.model.PhotoEntity
import com.posturecamera.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 照片存储仓库
 */
class PhotoRepository(
    private val photoDao: PhotoDao,
    private val context: Context
) {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * 保存照片
     */
    suspend fun savePhoto(
        bitmap: Bitmap,
        timestamp: Long,
        pitch: Float,
        roll: Float,
        yaw: Float
    ): Result<PhotoEntity> = withContext(Dispatchers.IO) {
        try {
            val fileName = "IMG_${dateFormat.format(Date(timestamp))}.jpg"
            val uri = saveToMediaStore(bitmap, fileName)

            val photo = PhotoEntity(
                uri = uri.toString(),
                fileName = fileName,
                timestamp = timestamp,
                pitch = pitch,
                roll = roll,
                yaw = yaw
            )
            val id = photoDao.insertPhoto(photo)

            Result.success(photo.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveToMediaStore(bitmap: Bitmap, fileName: String): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/体态相机")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw IOException("无法创建 MediaStore 条目")

        contentResolver.openOutputStream(uri)?.use { outputStream ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, outputStream)) {
                throw IOException("图片压缩失败")
            }
        } ?: throw IOException("无法打开输出流")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
        }

        return uri
    }

    fun getAllPhotos() = photoDao.getAllPhotos()
    suspend fun getPhotoById(id: Long) = photoDao.getPhotoById(id)
    suspend fun deletePhoto(photo: PhotoEntity) = photoDao.deletePhoto(photo)
}
