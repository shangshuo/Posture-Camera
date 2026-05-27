package com.posturecamera.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posturecamera.app.ui.components.CameraPreview
import com.posturecamera.app.ui.components.CaptureButton
import com.posturecamera.app.ui.components.GyroscopeIndicator
import com.posturecamera.app.util.CameraViewModelFactory
import com.posturecamera.app.viewmodel.CameraViewModel
import com.posturecamera.app.viewmodel.CaptureState
import kotlin.math.abs

/**
 * 数据收集测试屏幕
 * 用于收集完整的传感器数据和坐标计算结果
 */
@Composable
fun DataCollectionScreen(
    viewModelFactory: CameraViewModelFactory
) {
    val viewModel: CameraViewModel = viewModel(factory = viewModelFactory)
    val context = LocalContext.current

    val rotationAngles by viewModel.rotationAngles.collectAsState()
    val isAligned by viewModel.isAligned.collectAsState()
    val captureState by viewModel.captureState.collectAsState()

    var takePicture by remember { mutableStateOf({}) }

    LaunchedEffect(captureState) {
        when (val state = captureState) {
            is CaptureState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetCaptureState()
            }
            is CaptureState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetCaptureState()
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initializeOrientationSensor()
    }

    // 计算偏差（重映射后直接使用原始值）
    val pitchDeviation = abs(rotationAngles.pitch)
    val rollDeviation = abs(rotationAngles.roll)

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 相机预览
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onImageCaptured = { imageProxy ->
                    viewModel.onImageCaptured(imageProxy)
                },
                onRequestTakePicture = { takePictureCallback ->
                    takePicture = takePictureCallback
                }
            )

            // 陀螺仪双圆点指示器
            GyroscopeIndicator(
                pitch = rotationAngles.pitch,
                roll = rotationAngles.roll,
                modifier = Modifier.fillMaxSize()
            )

            // ★ 完整数据收集面板
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "=== 传感器原始数据 ===",
                    color = Color.Yellow,
                    fontSize = 11.sp
                )
                Text(
                    text = "Pitch: ${String.format("%.1f", rotationAngles.pitch)}°",
                    color = Color.White,
                    fontSize = 10.sp
                )
                Text(
                    text = "Roll: ${String.format("%.1f", rotationAngles.roll)}°",
                    color = Color.White,
                    fontSize = 10.sp
                )
                Text(
                    text = "Yaw: ${String.format("%.1f", rotationAngles.yaw)}°",
                    color = Color.White,
                    fontSize = 10.sp
                )

                Text(
                    text = "=== 角度偏差计算 ===",
                    color = Color.Yellow,
                    fontSize = 11.sp
                )
                Text(
                    text = "Pitch偏差: ${String.format("%.1f", pitchDeviation)}°",
                    color = Color.Cyan,
                    fontSize = 10.sp
                )
                Text(
                    text = "Roll偏差: ${String.format("%.1f", rollDeviation)}°",
                    color = Color.Cyan,
                    fontSize = 10.sp
                )

                Text(
                    text = "=== 对齐状态 ===",
                    color = Color.Yellow,
                    fontSize = 11.sp
                )
                Text(
                    text = if (isAligned) "✓ 已对齐" else "✗ 未对齐",
                    color = if (isAligned) Color.Green else Color.Red,
                    fontSize = 11.sp
                )

                Text(
                    text = "=== 测试说明 ===",
                    color = Color.Yellow,
                    fontSize = 11.sp
                )
                Text(
                    text = "请测试以下9个场景并记录数据",
                    color = Color.White,
                    fontSize = 9.sp
                )
                Text(
                    text = "1.竖直平举 2.前倾15° 3.前倾30°",
                    color = Color.Gray,
                    fontSize = 8.sp
                )
                Text(
                    text = "4.后仰15° 5.后仰30° 6.左倾15°",
                    color = Color.Gray,
                    fontSize = 8.sp
                )
                Text(
                    text = "7.左倾30° 8.右倾15° 9.右倾30°",
                    color = Color.Gray,
                    fontSize = 8.sp
                )
            }

            // 拍照按钮
            CaptureButton(
                onClick = {
                    if (isAligned) {
                        takePicture()
                    } else {
                        Toast.makeText(
                            context,
                            "请调整手机姿势至圆点居中",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = isAligned,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}
