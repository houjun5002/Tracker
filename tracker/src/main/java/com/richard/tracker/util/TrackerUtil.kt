package com.richard.tracker.util

import com.richard.tracker.manager.TrackerManager
import java.lang.IllegalArgumentException


object TrackerUtil {

    fun trackClickData(clickData: MutableMap<String, Any?>?) {
        TrackerLog.d("点击数据："+ clickData.toString())
        TrackerManager.get().commitListener?.commitClickData(clickData)
    }

    fun trackExploreData(exposureData: MutableList<MutableMap<String, Any?>?>) {
        TrackerLog.d("曝光数据：$exposureData")
        if (exposureData.isNotEmpty()) {
            TrackerManager.get().commitListener?.commitExposureData(exposureData)
        }
    }


}