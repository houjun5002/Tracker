package com.richard.datatracker

import android.app.Application
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.facebook.drawee.backends.pipeline.Fresco
import com.richard.tracker.api.OnCommitListener
import com.richard.tracker.constant.GlobalConfig
import com.richard.tracker.manager.TrackerManager


class App: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        Fresco.initialize(this)

        TrackerManager.get().initTracker(
            this,
            trackerOpen = true,
            trackerExposureOpen = true,
            logOpen = true,
            batchOpen = true,
            onCommitListener = object : OnCommitListener {
                override fun commitClickData(clickData: MutableMap<String, Any?>?) {
                    //点击数据
                    Log.i("commitClickData", "commitClickData==${clickData.toString()}")
                }

                override fun commitExposureData(exposureData: MutableList<MutableMap<String, Any?>?>) {
                    //曝光数据

                    for (data in exposureData) {

                        Log.i("commitExposureData", "commitExposureData==${JSON.toJSONString(data)}")
                    }
                }
            })
    }

}