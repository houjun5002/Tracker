package com.zsmarter.exposuretracker.util

import android.util.Log
import com.zsmarter.exposuretracker.constant.GlobalConfig


object TrackerLog {
    private const val TAG = "TrackerLog"

    fun d(msg: String?) {
        if (GlobalConfig.logOpen) {
            Log.d(TAG, msg!!)
        }
    }

    fun v(msg: String?) {
        if (GlobalConfig.logOpen) {
            Log.v(TAG, msg!!)
        }
    }

    fun e(msg: String?) {
        if (GlobalConfig.logOpen) {
            Log.e(TAG, msg!!)
        }
    }
}