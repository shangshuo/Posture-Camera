package com.posturecamera.app.util

import android.content.Context
import android.content.SharedPreferences

/**
 * 应用程序首选项管理类
 */
class AppPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "posture_camera_prefs"
        private const val KEY_PRIVACY_ACCEPTED = "privacy_accepted"
        private const val KEY_PRIVACY_DECISION_MADE = "privacy_decision_made"
        private const val KEY_FIRST_LAUNCH_REQUESTED = "first_launch_requested"
    }

    /**
     * 检查是否已接受隐私政策
     */
    fun isPrivacyAccepted(): Boolean {
        return prefs.getBoolean(KEY_PRIVACY_ACCEPTED, false)
    }

    /**
     * 设置隐私政策接受状态
     */
    fun setPrivacyAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_ACCEPTED, accepted).apply()
    }

    /**
     * 检查是否已对隐私政策做出决定（接受或拒绝）
     */
    fun isPrivacyDecisionMade(): Boolean {
        return prefs.getBoolean(KEY_PRIVACY_DECISION_MADE, false)
    }

    /**
     * 设置隐私政策决定状态
     */
    fun setPrivacyDecisionMade(made: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_DECISION_MADE, made).apply()
    }

    /**
     * 检查是否已经进行过首次启动的权限请求
     */
    fun isFirstLaunchRequested(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH_REQUESTED, false)
    }

    /**
     * 设置首次启动权限请求已完成
     */
    fun setFirstLaunchRequested(requested: Boolean) {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH_REQUESTED, requested).apply()
    }
}
