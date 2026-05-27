package com.posturecamera.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.posturecamera.app.data.local.AppDatabase
import com.posturecamera.app.data.local.PhotoDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PhotoRepository 集成测试
 *
 * 使用 Robolectric 模拟 Android 环境，测试数据库操作的完整性。
 * 注意：MediaStore 操作在单元测试环境中被简化处理。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class PhotoRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PhotoDao
    private lateinit var repository: PhotoRepository

    @Before
    fun setup() {
        // 获取应用上下文
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 创建内存数据库
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // 仅用于测试
            .build()

        dao = db.photoDao()
        repository = PhotoRepository(dao, context)
    }

    @After
    fun teardown() {
        // 关闭数据库
        db.close()
    }

    @Test
    fun `should save photo metadata to database`() = runBlocking {
        // Given: 准备测试数据
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val timestamp = System.currentTimeMillis()
        val pitch = 0.5f
        val roll = 0.3f
        val yaw = 0.1f

        // When: 调用保存照片方法
        val result = repository.savePhoto(bitmap, timestamp, pitch, roll, yaw)

        // Then: 验证保存成功
        assertTrue("保存照片应该成功", result.isSuccess)

        val photo = result.getOrThrow()
        assertNotNull("照片对象不应为空", photo)
        assertEquals("俯仰角应该匹配", pitch, photo.pitch, 0.01f)
        assertEquals("横滚角应该匹配", roll, photo.roll, 0.01f)
        assertEquals("偏航角应该匹配", yaw, photo.yaw, 0.01f)
        assertEquals("时间戳应该匹配", timestamp, photo.timestamp)

        // 验证数据库中确实保存了记录
        val savedPhoto = dao.getPhotoById(photo.id)
        assertNotNull("数据库中应该能找到保存的照片", savedPhoto)
        assertEquals("ID应该一致", photo.id, savedPhoto?.id)
    }

    @Test
    fun `should retrieve all photos in descending order by timestamp`() = runBlocking {
        // Given: 准备多条照片数据
        val timestamp1 = System.currentTimeMillis() - 2000
        val timestamp2 = System.currentTimeMillis() - 1000
        val timestamp3 = System.currentTimeMillis()

        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)

        // When: 保存照片
        repository.savePhoto(bitmap, timestamp1, 0.1f, 0.2f, 0.3f)
        repository.savePhoto(bitmap, timestamp2, 0.4f, 0.5f, 0.6f)
        repository.savePhoto(bitmap, timestamp3, 0.7f, 0.8f, 0.9f)

        // Then: 验证能获取所有照片，且按时间戳降序排列
        val allPhotos = dao.getAllPhotos().first()
        assertEquals("应该有3张照片", 3, allPhotos.size)

        // 验证降序排列：最新的在前面
        assertEquals("第一张应该是最新的", timestamp3, allPhotos[0].timestamp)
        assertEquals("第二张应该是中间的", timestamp2, allPhotos[1].timestamp)
        assertEquals("第三张应该是最早的", timestamp1, allPhotos[2].timestamp)
    }

    @Test
    fun `should delete photo from database`() = runBlocking {
        // Given: 保存一张照片
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val result = repository.savePhoto(bitmap, System.currentTimeMillis(), 0.5f, 0.3f, 0.1f)
        val savedPhoto = result.getOrThrow()

        // When: 删除照片
        repository.deletePhoto(savedPhoto)

        // Then: 验证照片已被删除
        val deletedPhoto = dao.getPhotoById(savedPhoto.id)
        assertEquals("照片应该已被删除", null, deletedPhoto)

        val allPhotos = dao.getAllPhotos().first()
        assertTrue("照片列表应该为空", allPhotos.isEmpty())
    }

    @Test
    fun `should handle zero sensor values correctly`() = runBlocking {
        // Given: 准备零值传感器数据
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val timestamp = System.currentTimeMillis()
        val pitch = 0.0f
        val roll = 0.0f
        val yaw = 0.0f

        // When: 保存照片
        val result = repository.savePhoto(bitmap, timestamp, pitch, roll, yaw)

        // Then: 验证零值也能正确保存
        assertTrue("保存应该成功", result.isSuccess)
        val photo = result.getOrThrow()
        assertEquals("俯仰角应该是0", 0.0f, photo.pitch, 0.01f)
        assertEquals("横滚角应该是0", 0.0f, photo.roll, 0.01f)
        assertEquals("偏航角应该是0", 0.0f, photo.yaw, 0.01f)
    }

    @Test
    fun `should handle negative sensor values correctly`() = runBlocking {
        // Given: 准备负值的传感器数据
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val timestamp = System.currentTimeMillis()
        val pitch = -15.5f
        val roll = -30.2f
        val yaw = -45.8f

        // When: 保存照片
        val result = repository.savePhoto(bitmap, timestamp, pitch, roll, yaw)

        // Then: 验证负值也能正确保存
        assertTrue("保存应该成功", result.isSuccess)
        val photo = result.getOrThrow()
        assertEquals("俯仰角应该是负值", pitch, photo.pitch, 0.01f)
        assertEquals("横滚角应该是负值", roll, photo.roll, 0.01f)
        assertEquals("偏航角应该是负值", yaw, photo.yaw, 0.01f)
    }
}
