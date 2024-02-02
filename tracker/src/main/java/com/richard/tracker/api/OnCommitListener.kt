package com.richard.tracker.api


interface OnCommitListener {

    fun commitClickData(clickData: MutableMap<String, Any?>?)

    fun commitExposureData(exposureData: MutableList<MutableMap<String, Any?>?>)
}