package com.richard.tracker.util

import android.text.TextUtils
import android.view.View
import com.richard.tracker.api.IDataCommit
import com.richard.tracker.constant.GlobalConfig
import com.richard.tracker.constant.TrackerConstants
import com.richard.tracker.manager.TrackerManager
import java.util.*

object DataProcess {


    @Synchronized
    fun commitExposureParams(exposureData: MutableMap<String, Any?>?, exposureTime: Long) {

        val commit: IDataCommit = TrackerManager.get().getTrackerCommit()
        commit.commitExposureEvent(exposureData, exposureTime)
    }


    @Synchronized
    fun commitClickParams(clickData: MutableMap<String, Any?>) {
        TrackerLog.d("costTime=" + (System.currentTimeMillis() - GlobalConfig.start))
        val commit: IDataCommit = TrackerManager.get().getTrackerCommit()
        commit.commitClickEvent(clickData)
    }
}