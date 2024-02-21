package com.zsmarter.exposuretracker.model

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.zsmarter.exposuretracker.constant.TrackerConstants
import com.zsmarter.exposuretracker.manager.ExposureManager
import com.zsmarter.exposuretracker.ui.TrackerFrameLayout
import com.zsmarter.exposuretracker.util.TrackerLog
import java.util.*


class ReuseLayoutHook(
    trackerFrameLayout: TrackerFrameLayout
) {

    private val HOOK_VIEW_TAG = -9100

    private lateinit var mRootLayout: TrackerFrameLayout
    private val mList: MutableList<ViewHookListener> =
        ArrayList()

    private interface ViewHookListener {
        fun isValid(view: View?): Boolean
        fun hookView(view: View?)
    }


    init {

        mRootLayout = trackerFrameLayout
        // replace with the onFling()
        mList.add(RecyclerViewHook())
        //mList.add(new AbsListViewHook());
        mList.add(ViewPagerHook())
    }


    private inner class RecyclerViewHook: ViewHookListener {

        override fun isValid(view: View?): Boolean {
            return view is RecyclerView
        }

        override fun hookView(view: View?) {
            val recyclerView: RecyclerView  = view as RecyclerView
            val tag: Any = recyclerView.getTag(HOOK_VIEW_TAG)?: false
            if (tag !is Boolean || tag) {
                return
            }

            recyclerView.addOnScrollListener(RecyclerScrollListener())
            recyclerView.setTag(HOOK_VIEW_TAG, true)
        }
    }

    private inner class ViewPagerHook : ViewHookListener {
        override fun isValid(view: View?): Boolean {
            return view is ViewPager
        }



        override fun hookView(view: View?) {
            val viewPager: ViewPager = view as ViewPager
            val tag: Any = viewPager.getTag(HOOK_VIEW_TAG)?: false

            if (tag !is Boolean || tag) {//添加过即返回
                return
            }

            viewPager.addOnPageChangeListener(ViewPagerOnPageChangeListener())
            viewPager.setTag(HOOK_VIEW_TAG, true)
            TrackerLog.d("ViewPager addOnPageChangeListener.")
        }
    }



    fun checkHookLayout(view: View?) {
        for (listener in mList) {
            if (listener != null && listener.isValid(view)) {
                listener.hookView(view)
            }
        }
    }


    private inner class RecyclerScrollListener: RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                ExposureManager.get().triggerViewCalculate(TrackerConstants.TRIGGER_VIEW_CHANGED, mRootLayout, mRootLayout.getLastVisibleViewMap());
            }
        }
    }

    private inner class ViewPagerOnPageChangeListener : ViewPager.OnPageChangeListener {
        private var state: Int = ViewPager.SCROLL_STATE_IDLE

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int) {

        }

        override fun onPageSelected(position: Int) {
            if (state != ViewPager.SCROLL_STATE_SETTLING) {
                ExposureManager.get().triggerViewCalculate(
                    TrackerConstants.TRIGGER_VIEW_CHANGED,
                    mRootLayout,
                    mRootLayout.getLastVisibleViewMap()
                )
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (this.state == ViewPager.SCROLL_STATE_SETTLING && state == ViewPager.SCROLL_STATE_IDLE) {
                ExposureManager.get().triggerViewCalculate(
                    TrackerConstants.TRIGGER_VIEW_CHANGED,
                    mRootLayout,
                    mRootLayout.getLastVisibleViewMap()
                )
            }
            this.state = state
        }
    }
}