package com.zsmarter.exposuretracker.delegate

import android.view.View
import android.view.accessibility.AccessibilityEvent
import com.zsmarter.exposuretracker.constant.TrackerConstants
import com.zsmarter.exposuretracker.util.DataProcess
import com.zsmarter.exposuretracker.util.TrackerLog


class ViewDelegate: View.AccessibilityDelegate() {


    override fun sendAccessibilityEvent(clickView: View, eventType: Int) {
        TrackerLog.d("eventType: $eventType")
        if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            TrackerLog.d("click: $clickView")
            //优先查找ClickData
            var exposureData = clickView.getTag(TrackerConstants.TAG_CLICK_DATA) as MutableMap<String, Any?>?
            if (exposureData == null) {
                exposureData = clickView.getTag(TrackerConstants.TAG_EXPLORE_AND_CLICK_DATA) as MutableMap<String, Any?>?
            }
            exposureData?.let {
                DataProcess.commitClickParams(it)
            }
        }
        super.sendAccessibilityEvent(clickView, eventType)
    }
}