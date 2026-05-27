package com.posturecamera.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

/**
 * 隐私政策协议对话框
 *
 * @param onAccept 用户同意隐私政策的回调
 * @param onDecline 用户不同意隐私政策的回调
 */
@Composable
fun PrivacyPolicyDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* 不允许点击外部消失 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier.padding(24.dp),
        title = {
            Text(
                text = "用户协议与隐私政策",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                            append("隐私政策 - 体态相机\n\n")
                        }
                        append("版本号：v1.0.0\n更新日期：2026年5月2日\n生效日期：2026年5月2日\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("一、引言与适用范围\n")
                        }
                        append("体态相机（以下简称\"本应用\"或\"我们\"）是一款拍照工具类单机应用，专注于为您提供体态记录与照片拍摄功能。我们高度重视用户的个人信息保护。本隐私政策依据《中华人民共和国个人信息保护法》等相关法律法规制定。\n")
                        withStyle(style = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                            append("特别说明： 作为一款单机应用，大部分功能可在不联网的情况下使用。我们遵循“最小必要”原则。\n\n")
                        }

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("二、我们收集的信息\n")
                        }
                        append("1. 您主动提供的信息： 反馈内容（电子邮箱及问题描述）、用户昵称（本地存储）。\n")
                        append("2. 自动收集的信息： 设备型号、操作系统、屏幕分辨率、唯一设备标识符（仅本地存储）、应用运行日志、匿名使用统计。\n")
                        append("3. 权限相关：\n")
                        append("   - 相机权限：用于拍摄体态记录。\n")
                        append("   - 相册权限：用于保存或选择照片进行分析。\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("三、信息存储与安全\n")
                        }
                        append("- 存储地点： 您的个人信息主要存储于您所使用的设备本地，不上传服务器。\n")
                        append("- 存储期限： 仅为实现目的所必需的最短时间。问题反馈信息保留30天。\n")
                        append("- 安全措施： 本地数据设备级加密、HTTPS加密传输、定期安全检测。\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("四、信息的使用目的\n")
                        }
                        append("提供拍照核心功能、保障应用稳定性、优化产品体验、响应客服咨询。我们不会将信息用于广告画像分析。\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("五、信息的共享与转让\n")
                        }
                        append("本应用当前版本未与任何第三方共享用户个人信息。\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("六、您的权利与选择\n")
                        }
                        append("您享有查阅、更正、删除（通过清理应用数据或卸载）、撤回同意及投诉举报的权利。\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("七、权限申请说明\n")
                        }
                        append("我们严格遵循“权限最小化”原则，您可随时在系统设置中管理。拒绝权限不影响核心功能的使用。\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("八、第三方服务\n")
                        }
                        append("当前版本未嵌入任何第三方SDK。\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("九、广告标识符说明\n")
                        }
                        append("当前版本未展示任何广告，不涉及广告标识符使用。\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("十、未成年人保护\n")
                        }
                        append("未满18周岁的用户应在监护人陪同下阅读本政策。若未满14周岁，需取得监护人明确同意。\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("十一、政策更新与联系\n")
                        }
                        append("我们可能会适时更新本政策。如有疑问，请通过以下方式联系：\n")
                        append("- 公司： 北京存元健康科技有限公司\n")
                        append("- 邮箱： cyhealth.china@outlook.com\n")
                        append("- 地址： 北京市朝阳区奥运村街道奥运媒体村天畅园8号楼2803室\n\n")
                        
                        append("详情请参阅：")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) {
                            append("https://www.zhenbrain.com.cn/privacypolicy/PrivacyPolicy.html")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // 绿色
                    contentColor = Color.White // 白色字体
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Text("同意")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDecline,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Text("拒绝", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
