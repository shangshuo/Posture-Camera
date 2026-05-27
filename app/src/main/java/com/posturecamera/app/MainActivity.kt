package com.posturecamera.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.posturecamera.app.data.local.AppDatabase
import com.posturecamera.app.data.repository.PhotoRepository
import com.posturecamera.app.ui.components.PrivacyPolicyDialog
import com.posturecamera.app.ui.screens.CameraScreen
import com.posturecamera.app.ui.theme.PostureCameraTheme
import com.posturecamera.app.util.AppPrefs
import com.posturecamera.app.util.CameraViewModelFactory
import com.posturecamera.app.util.PermissionHandler

/**
 * 主Activity
 */
class MainActivity : ComponentActivity() {

    private lateinit var viewModelFactory: CameraViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化依赖
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "posture_camera_db"
        ).build()

        val repository = PhotoRepository(
            photoDao = database.photoDao(),
            context = applicationContext
        )

        viewModelFactory = CameraViewModelFactory(
            repository = repository,
            context = applicationContext
        )

        val appPrefs = AppPrefs(applicationContext)

        setContent {
            PostureCameraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showPrivacyDialog by remember { mutableStateOf(!appPrefs.isPrivacyDecisionMade()) }
                    var triggerPermissionRequest by remember { mutableStateOf(false) }
                    
                    // 记录是否已经展示过隐私协议并准备好申请权限
                    var privacyHandled by remember { mutableStateOf(appPrefs.isPrivacyDecisionMade()) }

                    if (showPrivacyDialog) {
                        PrivacyPolicyDialog(
                            onAccept = {
                                appPrefs.setPrivacyAccepted(true)
                                appPrefs.setPrivacyDecisionMade(true)
                                showPrivacyDialog = false
                                // 只有同意政策后，且是首次，才触发权限申请
                                if (!appPrefs.isFirstLaunchRequested()) {
                                    triggerPermissionRequest = true
                                }
                                privacyHandled = true
                            },
                            onDecline = {
                                // 拒绝协议后，也进入应用流程，但标记协议已处理
                                appPrefs.setPrivacyAccepted(false)
                                appPrefs.setPrivacyDecisionMade(true)
                                showPrivacyDialog = false
                                // 拒绝隐私政策，不触发权限申请
                                privacyHandled = true
                            }
                        )
                    }

                    if (privacyHandled) {
                        PermissionHandler(
                            triggerRequest = triggerPermissionRequest,
                            onPermissionResult = {
                                triggerPermissionRequest = false
                                appPrefs.setFirstLaunchRequested(true)
                            }
                        ) { isGranted ->
                            CameraScreen(
                                viewModelFactory = viewModelFactory,
                                isCameraPermissionGranted = isGranted
                            )
                        }
                    }
                }
            }
        }
    }
}
