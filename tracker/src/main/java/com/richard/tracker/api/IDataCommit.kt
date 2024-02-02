package com.richard.tracker.api

import java.util.*


interface IDataCommit {

    fun commitClickEvent(
        clickData: MutableMap<String, Any?>?
    )

    fun commitExposureEvent(
        exposureData: MutableMap<String, Any?>?,
        exposureTime: Long
    )

}