package com.posturecamera.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posturecamera.app.ui.components.CameraPreview
import com.posturecamera.app.ui.components.CaptureButton
import com.posturecamera.app.ui.components.GyroscopeIndicator
import com.posturecamera.app.util.CameraViewModelFactory
import com.posturecamera.app.viewmodel.CameraViewModel
import com.posturecamera.app.viewmodel.CaptureState

/**
 * 相机屏幕
 */
@Composable
fun CameraScreen(
    viewModelFactory: CameraViewModelFactory,
    isCameraPermissionGranted: Boolean
) {
    val viewModel: CameraViewModel = viewModel(factory = viewModelFactory)
    val context = LocalContext.current

    val pitchDeviation by viewModel.pitchDeviation.collectAsState()
    val rollDeviation by viewModel.rollDeviation.collectAsState()
    val isAligned by viewModel.isAligned.collectAsState()
    val captureState by viewModel.captureState.collectAsState()

    // 拍照函数引用
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

    LaunchedEffect(isCameraPermissionGranted) {
        if (isCameraPermissionGranted) {
            viewModel.initializeOrientationSensor()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black) // 默认黑屏背景
        ) {
            if (isCameraPermissionGranted) {
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
            }

            // 陀螺仪双圆点指示器（始终显示，即使黑屏）
            GyroscopeIndicator(
                pitch = pitchDeviation,
                roll = rollDeviation,
                modifier = Modifier.fillMaxSize()
            )

            // 拍照按钮
            CaptureButton(
                onClick = {
                    if (isCameraPermissionGranted) {
                        if (isAligned) {
                            takePicture()
                        } else {
                            Toast.makeText(
                                context,
                                "请调整手机姿势至圆点居中",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "未获得相机权限，无法拍照",
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
