package com.posturecamera.app.util

/**
 * 应用常量定义
 */
object Constants {
    /**
     * 对齐阈值(度)
     *
     * 当陀螺仪偏转角度小于此值时,认为设备已对齐。
     * 从1.2度调整为2.0度,平衡精度和易用性
     */
    const val ALIGNMENT_THRESHOLD = 2.0f

    /**
     * 俯仰角圆点显示映射范围(度)
     *
     * 俯仰角圆点从中心移动到屏幕边缘对应的角度
     * 设置为45度，避免过于敏感
     */
    const val PITCH_DISPLAY_RANGE = 45f

    /**
     * 横滚角圆点显示映射范围(度)
     *
     * 横滚角圆点从中心移动到屏幕边缘对应的角度
     * 设置为45度，避免过于敏感
     */
    const val ROLL_DISPLAY_RANGE = 45f

    /**
     * 俯仰角边界警告触发角度(度)
     *
     * 俯仰角偏离竖直位置的角度超过此值时触发边界警告
     */
    const val PITCH_BOUNDARY_THRESHOLD = 45f

    /**
     * 横滚角边界警告触发角度(度)
     *
     * 横滚角偏离水平位置的角度超过此值时触发边界警告
     */
    const val ROLL_BOUNDARY_THRESHOLD = 45f

    /**
     * 传感器采样频率（微秒）
     *
     * 20000微秒 = 50Hz采样频率
     * TYPE_ROTATION_VECTOR 是系统级传感器融合，50Hz已足够平滑
     * 过高频率会增加功耗和发热，对工具类应用无必要
     */
    const val SENSOR_SAMPLING_PERIOD_US = 20000

    /**
     * 低通滤波系数
     *
     * 用于平滑传感器数据，减少抖动
     * 值越大，平滑效果越强（范围：0.0-1.0）
     * 0.3 的值让新数据占70%权重，确保快速响应，同时保留一定平滑效果
     * 注意：TYPE_ROTATION_VECTOR 输出本身已由系统融合滤波，
     * 此处滤波仅用于消除UI显示的微小抖动
     */
    const val LOW_PASS_FILTER_ALPHA = 0.3f

    /**
     * 相机预览分辨率
     */
    const val PREVIEW_WIDTH = 1280
    const val PREVIEW_HEIGHT = 720

    /**
     * 拍照分辨率
     */
    const val CAPTURE_WIDTH = 1920
    const val CAPTURE_HEIGHT = 1080

    /**
     * 图片压缩质量
     *
     * JPEG压缩质量百分比（0-100）
     */
    const val JPEG_QUALITY = 90

    /**
     * 数据库名称
     */
    const val DATABASE_NAME = "posture_camera_db"

    /**
     * 水印文字大小（像素）
     */
    const val WATERMARK_TEXT_SIZE = 36f

    /**
     * 水印行高（像素）
     */
    const val WATERMARK_LINE_HEIGHT = 50f

    /**
     * 水印内边距（像素）
     */
    const val WATERMARK_PADDING = 20f

    /**
     * 动画时长（毫秒）
     */
    const val ANIMATION_DURATION_MS = 300
}
