package com.posturecamera.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 照片实体类
 *
 * @property id 主键（自动生成）
 * @property uri 照片在 MediaStore 的 URI
 * @property fileName 文件名
 * @property timestamp 拍摄时间戳
 * @property pitch 俯仰角（度）
 * @property roll 横滚角（度）
 * @property yaw 偏航角（度）
 * @property createdAt 记录创建时间
 */
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val fileName: String,
    val timestamp: Long,
    val pitch: Float,
    val roll: Float,
    val yaw: Float,
    val createdAt: Long = System.currentTimeMillis()
)
