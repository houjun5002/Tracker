package com.zsmarter.exposuretracker.api.impl

import com.zsmarter.exposuretracker.api.IDataCommit
import com.zsmarter.exposuretracker.constant.GlobalConfig
import com.zsmarter.exposuretracker.manager.ExposureManager
import com.zsmarter.exposuretracker.util.TrackerLog
import com.zsmarter.exposuretracker.util.TrackerUtil


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