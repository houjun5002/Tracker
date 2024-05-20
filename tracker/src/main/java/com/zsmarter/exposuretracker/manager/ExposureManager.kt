package com.zsmarter.exposuretracker.manager

import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.util.ArrayMap
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.zsmarter.exposuretracker.constant.GlobalConfig
import com.zsmarter.exposuretracker.constant.TrackerConstants
import com.zsmarter.exposuretracker.model.ExposureModel
import com.zsmarter.exposuretracker.model.ReuseLayoutHook
import com.zsmarter.exposuretracker.util.CommonHelper
import com.zsmarter.exposuretracker.util.DataProcess
import com.zsmarter.exposuretracker.util.TrackerLog
import com.zsmarter.exposuretracker.util.TrackerUtil
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList


class ExposureManager {

    /**
     * whether or not to hit the exposure event
     */
    private var isSampleHit: Boolean = false

    private var traverseTime: Long = 0

    private lateinit var exposureHandler: Handler

    var commitLogs: MutableList<MutableMap<String, Any?>?> = mutableListOf()/// 最终数组
    private var isVisible = false
    private var exposureViewProportion: Float? = null
    var motionEvent: MotionEvent? = null

    init {
        val exposureThread = HandlerThread("ViewTracker_exposure")
        exposureThread.start()

        exposureHandler =
            Handler(exposureThread.looper, Handler.Callback { msg ->
                when (msg.what) {
                    SINGLE_COMMIT_EXPOSURE -> {
                        val exposureInner: ExposureInner = msg.obj as ExposureInner
                        when (exposureInner.triggerType) {////controlName viewTag
                            TrackerConstants.TRIGGER_WINDOW_CHANGED -> for (controlName in exposureInner.lastVisibleViewMap.keys) {
                                // If the current window invokes change, all the visible views need to be committed.
                                val model: ExposureModel? = exposureInner.lastVisibleViewMap[controlName]
                                model?.let {
                                    model.endTime = System.currentTimeMillis()
                                    reportExposureData(model, controlName)
                                }
                            }
                            TrackerConstants.TRIGGER_VIEW_CHANGED -> for (controlName in exposureInner.lastVisibleViewMap.keys) {
                                // If the view is visible in the last trigger timing, but invisible this time, then we commit the view as a exposure event.
                                if (!exposureInner.currentVisibleViewMap.containsKey(controlName)) {
                                    val model: ExposureModel? = exposureInner.lastVisibleViewMap[controlName]
                                    model?.let {
                                        model.endTime = System.currentTimeMillis()
                                        reportExposureData(model, controlName)
                                    }

                                }
                            }
                        }
                    }
                    BATCH_COMMIT_EXPOSURE -> {
                        // Scene 3 (switch back and forth when press Home button) is excluded.
                        TrackerUtil.trackExploreData(commitLogs)
                        TrackerLog.v("onActivityPaused batch commit")
                        // clear after committed.
                        commitLogs.clear()
                    }
                    else -> {
                    }
                }
                false
            })
    }

    fun getExposureHandler(): Handler? {
        return exposureHandler
    }


    private fun reportExposureData(
        model: ExposureModel,
        viewTag: String
    ) {
        val duration: Long = getExposureViewDuration(model)
        TrackerLog.e("ExposureView report ${model.tag} exposure data $duration")
        if (duration > 0) {
            val indexMap =
                HashMap<String, Any>()
            if (!GlobalConfig.exposureIndex.containsKey(viewTag)) {
                // commit firstly
                GlobalConfig.exposureIndex[viewTag] = 1
                indexMap["exposureIndex"] = 1
            } else {
                val index: Int = GlobalConfig.exposureIndex[viewTag] ?:0
                GlobalConfig.exposureIndex[viewTag] = index + 1
                indexMap["exposureIndex"] = index + 1
            }
            addParamsToHashMap(model,model.params,duration)
            model.params?.put("startTime",model.beginTime)
            model.params?.put("durationTime",duration)
            DataProcess.commitExposureParams(
                model.params,
                duration
            )
        } else {
            TrackerLog.d("曝光时间不足${GlobalConfig.timeThreshold}毫秒==${viewTag}")
        }
    }

    fun setAbsPosition(ev: MotionEvent) {
        motionEvent = ev
    }

    fun getAbsPosition(): Map<String, Float>? {
        val positionMap = mutableMapOf<String, Float>()
        positionMap["x"] = motionEvent?.x ?: 0f
        positionMap["y"] = motionEvent?.y ?: 0f
        positionMap["rawX"] = motionEvent?.rawX ?: 0f
        positionMap["rawY"] = motionEvent?.rawY ?: 0f
        return positionMap
    }

    /**
     * for the exposure event
     *
     * @param view
     * @return
     */
    fun triggerViewCalculate(
        triggerType: Int,
        view: View?,
        lastVisibleViewMap: MutableMap<String, ExposureModel?>
    ) {
        if (!GlobalConfig.trackerExposureOpen) {
            return
        }
        TrackerLog.d("GlobalConfig.trackerExposureOpen=="+GlobalConfig.trackerExposureOpen)
        if (view == null) {
            TrackerLog.d("view is null")
            return
        }
        // Sample not hit
        isSampleHit = CommonHelper.isSamplingHit(GlobalConfig.exposureSampling)

        if (!isSampleHit) {
            TrackerLog.d("exposure isSampleHit is false")
            return
        }
        val currentVisibleViewMap: MutableMap<String, ExposureModel?> = ArrayMap()
        //TrackerLog.d("triggerViewCalculate  lastVisibleViewMap=="+GsonUtils.toJson(lastVisibleViewMap))
        traverseViewTree(view, lastVisibleViewMap, currentVisibleViewMap)  ///遍历view
        commitExposure(triggerType, lastVisibleViewMap, currentVisibleViewMap) ///提交view
    }

    fun traverseViewTree(view: View?, reuseLayoutHook: ReuseLayoutHook?) {
        if (!GlobalConfig.trackerExposureOpen) {
            return
        }

        reuseLayoutHook?.checkHookLayout(view)

        if (view is ViewGroup) {
            val group = view
            val childCount = group.childCount
            for (i in 0 until childCount) {
                traverseViewTree(group.getChildAt(i), reuseLayoutHook)
            }
        }
    }

    /**
     * find all the view that can be seen in screen.
     *
     * @param view
     */
    private fun traverseViewTree(
        view: View,
        lastVisibleViewMap: MutableMap<String, ExposureModel?>,
        currentVisibleViewMap: MutableMap<String, ExposureModel?>
    ) {
        if (CommonHelper.isViewHasExposureTag(view)) {//判断是否有tag
            wrapExposureCurrentView(view, lastVisibleViewMap, currentVisibleViewMap)
        }
        if (view is ViewGroup) {
            val group = view
            val childCount = group.childCount
            for (i in 0 until childCount) {
                traverseViewTree(group.getChildAt(i), lastVisibleViewMap, currentVisibleViewMap)
            }
        }
    }

    private fun wrapExposureCurrentView(
        view: View,
        lastVisibleViewMap: MutableMap<String, ExposureModel?>,
        currentVisibleViewMap: MutableMap<String, ExposureModel?>
    ) {

        var params = view.getTag(TrackerConstants.TAG_EXPLORE_DATA) as MutableMap<String, Any?>?
        if (params == null) {
            params = view.getTag(TrackerConstants.TAG_EXPLORE_AND_CLICK_DATA) as MutableMap<String, Any?>?
        }
        val viewTag = view.getTag(TrackerConstants.VIEW_TAG_UNIQUE_NAME) as String?
        val checkWindowFocus = checkWindowFocus(view)  //检查视图是否拥有焦点,同时检查isShow

        val exposureValid = checkExposureViewDimension(view) //是否可见getGlobalVisibleRect
        val needExposureProcess = checkWindowFocus && exposureValid
        if (!needExposureProcess) {
            return
        }
        TrackerLog.d(view.toString()+"可见===")

        if (viewTag.isNullOrBlank()) {
            throw IllegalArgumentException("没有有给曝光的view设置->VIEW_TAG_UNIQUE_NAME")
        }
        exposureViewProportion = getExposureViewProportion(view)
        ///需要曝光,上次list包含则直接添加到current,
        // only add the visible view in screen
        if (lastVisibleViewMap.containsKey(viewTag)) {
            val model = lastVisibleViewMap[viewTag]
            model?.params = params
            currentVisibleViewMap[viewTag] = model
        } else if (!currentVisibleViewMap.containsKey(viewTag)) {
            val model = ExposureModel()
            model.beginTime = System.currentTimeMillis()
            model.tag = viewTag
            model.params = params
            currentVisibleViewMap[viewTag] = model
        }
    }

    private fun addParamsToHashMap(
        model: ExposureModel?,
        params: MutableMap<String, Any?>?,
        duration: Long
    ) {
        params?.values?.forEach { value ->
            (value as? ArrayList<HashMap<String, Any?>>)?.forEachIndexed  {index,item ->
                item.apply {
                    this["beginTime"] = model?.beginTime
                    this["exposeProportion"] = exposureViewProportion
                    this["duration"] = duration
                }
            }
        }
    }

    ///提交数据
    private fun commitExposure(
        triggerType: Int,
        lastVisibleViewMap: MutableMap<String, ExposureModel?>,
        currentVisibleViewMap: MutableMap<String, ExposureModel?>
    ) {
        val exposureInner = ExposureInner()
        exposureInner.triggerType = triggerType
        exposureInner.lastVisibleViewMap = mutableMapOf()
        for ((key, value) in lastVisibleViewMap) {
            exposureInner.lastVisibleViewMap[key] = (value!!.clone() as ExposureModel)
        }
        exposureInner.currentVisibleViewMap = mutableMapOf()
        for ((key, value) in currentVisibleViewMap) {
            exposureInner.currentVisibleViewMap[key] = (value!!.clone() as ExposureModel)
        }
        lastVisibleViewMap.clear()
        lastVisibleViewMap.putAll(currentVisibleViewMap)//提交则清空
        ///TrackerLog.d("commitExposure commitExposure+==========="+ (exposureInner.lastVisibleViewMap.isNotEmpty() || exposureInner.currentVisibleViewMap.isNotEmpty()))
        // transfer time-consuming operation to new thread.
        if (exposureInner.lastVisibleViewMap.isNotEmpty() || exposureInner.currentVisibleViewMap.isNotEmpty()) {
            val message = exposureHandler.obtainMessage()
            message.what = SINGLE_COMMIT_EXPOSURE
            message.obj = exposureInner
            exposureHandler.sendMessage(message)
        }
    }

    private fun checkWindowFocus(view: View): Boolean {
        if(!view.isShown)return false
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val visibleRect = Rect()
                // 检查视图的可见性并更新 isVisible 变量
                isVisible = view.getGlobalVisibleRect(visibleRect)
                // 移除监听器，避免重复调用
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        return isVisible
    }
    /**
     * check the visible width and height of the view, compared with the its original width and height.
     *
     * @param view
     * @return
     */
    private fun checkExposureViewDimension(view: View): Boolean {
        val width = view.width
        val height = view.height
        val GlobalVisibleRect = Rect()
        val isVisibleRect = view.getGlobalVisibleRect(GlobalVisibleRect)
        if (isVisibleRect) {
            val visibleWidth = GlobalVisibleRect.width()
            val visibleHeight = GlobalVisibleRect.height()

            return visibleWidth * 1.00 / width > GlobalConfig.dimThreshold && visibleHeight * 1.00 / height > GlobalConfig.dimThreshold
        } else {
            return false
        }
    }


    private fun getExposureViewDuration(model: ExposureModel): Long {
        if (model.beginTime > 0 && model.endTime > 0 && model.endTime > model.beginTime
        ) {
            val duration = model.endTime - model.beginTime
            // omit the value less than 100
            if (duration > GlobalConfig.timeThreshold && duration < GlobalConfig.maxTimeThreshold) {
                return duration
            }
        }
        return 0
    }

    private fun getExposureViewProportion(view: View): Float {
        val width = view.width.toFloat()
        val height = view.height.toFloat()
        val globalVisibleRect = Rect()
        val visibleArea: Float
        // 获取控件在屏幕上的可见区域
        val isVisible = view.getGlobalVisibleRect(globalVisibleRect)
        if (isVisible) {
            // 计算可见区域的面积
            val visibleWidth = globalVisibleRect.right - globalVisibleRect.left
            val visibleHeight = globalVisibleRect.bottom - globalVisibleRect.top
            visibleArea = visibleWidth.toFloat() * visibleHeight.toFloat()
        } else {
            // 如果控件不可见，则可见区域为0
            visibleArea = 0f
        }
        // 计算曝光比例
        val exposeProportion = visibleArea / (width * height)
        return exposeProportion
    }



    companion object {
        /**
         * 单例
         */
        private var instance: ExposureManager? = null
            get() {
                if (field == null) {
                    field = ExposureManager()
                }
                return field
            }
        @Synchronized
        fun get(): ExposureManager {
            return instance!!
        }

        /**
         * 单个提交
         */
        val SINGLE_COMMIT_EXPOSURE = 0

        /**
         * 批量提交
         */
        val BATCH_COMMIT_EXPOSURE = 1
    }

    private inner class ExposureInner {
        var triggerType = 0
        var lastVisibleViewMap: MutableMap<String, ExposureModel> = mutableMapOf()
        var currentVisibleViewMap: MutableMap<String, ExposureModel> = mutableMapOf()
    }
}