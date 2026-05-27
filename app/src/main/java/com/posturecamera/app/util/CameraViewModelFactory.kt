package com.posturecamera.app.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.posturecamera.app.data.repository.PhotoRepository
import com.posturecamera.app.viewmodel.CameraViewModel

/**
 * CameraViewModel 工厂类
 *
 * 用于创建包含依赖注入的 CameraViewModel 实例
 *
 * @param repository 照片存储仓库
 * @param context 应用上下文
 */
class CameraViewModelFactory(
    private val repository: PhotoRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
