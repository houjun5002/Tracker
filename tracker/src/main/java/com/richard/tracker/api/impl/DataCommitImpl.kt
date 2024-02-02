package com.richard.tracker.api.impl

import com.richard.tracker.api.IDataCommit
import com.richard.tracker.constant.GlobalConfig
import com.richard.tracker.manager.ExposureManager
import com.richard.tracker.util.TrackerLog
import com.richard.tracker.util.TrackerUtil


class DataCommitImpl: IDataCommit {

    override fun commitClickEvent(clickData: MutableMap<String, Any?>?) {
        TrackerUtil.trackClickData(clickData)
    }

    override fun commitExposureEvent(exposureData: MutableMap<String, Any?>?, exposureTime: Long) {
        TrackerLog.d("曝光时间==$exposureTime===" + "曝光数据："+ exposureData.toString())
        if (GlobalConfig.batchOpen) {
            ExposureManager.get().commitLogs.add(exposureData)
        } else {
            TrackerUtil.trackExploreData(mutableListOf<MutableMap<String, Any?>?>().apply{
                add(exposureData)
            })
        }
    }
}