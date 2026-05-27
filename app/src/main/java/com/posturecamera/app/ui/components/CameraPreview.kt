package com.posturecamera.app.ui.components

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.posturecamera.app.util.Constants
import android.util.Size
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 相机预览组件
 *
 * @param modifier 修饰符
 * @param onImageCaptured 图片捕获回调
 * @param onRequestTakePicture 拍照请求回调(暴露拍照方法给外部)
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptured: (ImageProxy) -> Unit,
    onRequestTakePicture: (() -> Unit) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = remember {
        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(Constants.PREVIEW_WIDTH, Constants.PREVIEW_HEIGHT),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()

        Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
    }

    val imageCapture = remember {
        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(Constants.CAPTURE_WIDTH, Constants.CAPTURE_HEIGHT),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()

        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .build()
    }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    // 异步获取 ProcessCameraProvider
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    // 创建 PreviewView
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // 绑定相机生命周期
    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()

        // 先设置 SurfaceProvider，再绑定生命周期
        preview.setSurfaceProvider(previewView.surfaceProvider)

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
    }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cameraExecutor.shutdown()
        }
    }

    // 暴露拍照方法给外部
    DisposableEffect(onRequestTakePicture) {
        onRequestTakePicture {
            imageCapture.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        onImageCaptured(image)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        exception.printStackTrace()
                    }
                }
            )
        }
        onDispose { }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
