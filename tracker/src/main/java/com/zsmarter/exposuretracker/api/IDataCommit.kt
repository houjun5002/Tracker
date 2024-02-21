package com.zsmarter.exposuretracker.api


interface IDataCommit {

    fun commitClickEvent(
        clickData: MutableMap<String, Any?>?
    )

    fun commitExposureEvent(
        exposureData: MutableMap<String, Any?>?,
        exposureTime: Long
    )

}