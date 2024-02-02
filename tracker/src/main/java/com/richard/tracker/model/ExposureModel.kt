package com.richard.tracker.model

import com.richard.tracker.util.TrackerLog
import java.util.*


class ExposureModel : Cloneable{

    var tag: String? = null

    var beginTime: Long = 0
    var endTime: Long = 0

    var params: MutableMap<String, Any?>? = null

    /**
     * deep copy
     *
     * @return
     * @throws CloneNotSupportedException
     */
    public override fun clone(): Any {
        var exposureModel: ExposureModel? = null
        try {
            exposureModel = super.clone() as ExposureModel
        } catch (e: CloneNotSupportedException) {
            TrackerLog.e(e.message)
        }
        if (exposureModel != null && params != null) {
            exposureModel.params = params?.toMutableMap() as HashMap<String, Any?>?
        }
        return exposureModel!!
    }

}