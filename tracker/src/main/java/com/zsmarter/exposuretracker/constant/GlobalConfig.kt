package com.zsmarter.exposuretracker.constant

import android.app.Application


object GlobalConfig {
    var mApplication: Application? = null

    /**
     * whether or not to track click event
     */
    var trackerOpen = false

    /**
     * whether or not to track exposure event
     */
    var trackerExposureOpen = true

    /**
     * min threshold of the exposure duration
     */
    var timeThreshold = 500

    /**
     * max threshold of the exposure duration
     */
    var maxTimeThreshold = 60 * 60 * 1000

    /**
     * threshold of the view width and height
     */
    var dimThreshold = 0.8

    /**
     * whether or not to print the log
     */
    var logOpen = true

    /**
     * whether or not to commit the exposure event log in batch or one by one
     */
    var batchOpen = false

    var start = 0L

    /**
     * the exposure index in the lifecycle of APP
     */
    var exposureIndex: MutableMap<String, Int> = mutableMapOf()

    /**
     * for click event, 100% by default
     */
    var sampling = 100

    /**
     * for exposure event, 100% by default
     */
    var exposureSampling = 100
}