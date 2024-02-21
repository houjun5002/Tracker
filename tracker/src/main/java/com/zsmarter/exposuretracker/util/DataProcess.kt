package com.zsmarter.exposuretracker.util

import com.zsmarter.exposuretracker.api.IDataCommit
import com.zsmarter.exposuretracker.constant.GlobalConfig
import com.zsmarter.exposuretracker.manager.TrackerManager

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