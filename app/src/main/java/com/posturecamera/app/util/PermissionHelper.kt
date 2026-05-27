package com.posturecamera.app.util

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

/**
 * 权限处理帮助对象
 */
object PermissionHelper {

    // 相机权限
    val CAMERA_PERMISSION = Manifest.permission.CAMERA

    // 存储权限（仅Android 9及以下）
    val STORAGE_PERMISSION: String? = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    } else {
        null
    }

    /**
     * 检查是否需要请求存储权限
     *
     * @return true如果需要存储权限
     */
    fun needsStoragePermission(): Boolean {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    }

    /**
     * 获取需要请求的权限列表
     *
     * @return 权限列表
     */
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf(CAMERA_PERMISSION)
        STORAGE_PERMISSION?.let { permissions.add(it) }
        return permissions
    }
}

/**
 * 权限处理Composable
 *
 * @param triggerRequest 是否触发请求
 * @param onPermissionResult 权限请求结果回调
 * @param content 权限授予后显示的内容
 */
@Composable
fun PermissionHandler(
    triggerRequest: Boolean = false,
    initialAllGranted: Boolean = false,
    onPermissionResult: (Boolean) -> Unit = {},
    content: @Composable (isGranted: Boolean) -> Unit
) {
    val context = LocalContext.current

    // 权限状态
    var allPermissionsGranted by remember {
        mutableStateOf(
            initialAllGranted || PermissionHelper.getRequiredPermissions().all {
                ActivityCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var showRationale by remember { mutableStateOf(false) }
    var showPurposeDialog by remember { mutableStateOf(false) }

    // 权限请求启动器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        allPermissionsGranted = granted
        onPermissionResult(granted)
        if (!granted) {
            // 如果用户点击了“拒绝且不再询问”，shouldShowRequestPermissionRationale 会返回 false
            // 这里简单处理：只要没过，就提示去设置（或者根据 rationale 决定是否显示引导对话框）
            showRationale = true
        }
    }

    // 响应触发请求信号
    LaunchedEffect(triggerRequest) {
        if (triggerRequest && !allPermissionsGranted) {
            showPurposeDialog = true
        }
    }

    // 权限申请目的说明对话框
    if (showPurposeDialog) {
        AlertDialog(
            onDismissRequest = { showPurposeDialog = false },
            title = { Text("权限申请说明") },
            text = {
                Text("体态相机需要使用您的“相机”权限，用于实时捕捉您的身体姿态并计算对齐角度。我们承诺不会在未经许可的情况下上传您的私人照片。")
            },
            confirmButton = {
                TextButton(onClick = {
                    showPurposeDialog = false
                    launcher.launch(PermissionHelper.getRequiredPermissions().toTypedArray())
                }) {
                    Text("继续")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurposeDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 权限拒绝提示对话框（引导去设置）
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("权限受限") },
            text = { Text("由于相机权限被拒绝，体态识别功能无法正常使用。请在应用设置中手动开启相机权限。") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 始终显示内容，通过参数告知权限状态
    content(allPermissionsGranted)
}
