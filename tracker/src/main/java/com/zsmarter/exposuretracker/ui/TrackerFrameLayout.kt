package com.zsmarter.exposuretracker.ui

import android.app.Activity
import android.content.Context
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.zsmarter.exposuretracker.constant.TrackerConstants
import com.zsmarter.exposuretracker.manager.ExposureManager
import com.zsmarter.exposuretracker.model.ExposureModel
import com.zsmarter.exposuretracker.model.ReuseLayoutHook
import com.zsmarter.exposuretracker.util.TrackerLog


class TrackerFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {


    /**
     * Custom threshold is used to determine whether it is a click event,
     * When the user moves more than 20 pixels in screen, it is considered as the scrolling event instead of a click.
     */
    private val CLICK_LIMIT = 40f

    /**
     * the X Position
     */
    private var mOriX = 0f

    /**
     * the Y Position
     */
    private var mOriY = 0f

    private var mGestureDetector: GestureDetector

    private var mReuseLayoutHook: ReuseLayoutHook


    /**
     * all the visible views inside page, key is viewName
     */
    private val lastVisibleViewMap: MutableMap<String, ExposureModel?> = ArrayMap<String, ExposureModel?>()

    private var lastOnLayoutSystemTimeMillis: Long = 0


    init {
        mGestureDetector = GestureDetector(context, this)
        mReuseLayoutHook = ReuseLayoutHook(this)
        // after the onActivityResumed
//        CommonHelper.addCommonArgsInfo(this)
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(ev)
        if (context != null && context is Activity) {
            // trigger the click event
            //ClickManager.get().eventAspect(context as Activity, ev)
        }
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mOriX = ev.x
                mOriY = ev.y
                ExposureManager.get().setAbsPosition(ev)
            }
            MotionEvent.ACTION_MOVE -> if (Math.abs(ev.x - mOriX) > CLICK_LIMIT || Math.abs(
                    ev.y - mOriY
                ) > CLICK_LIMIT
            ) {
                // Scene 1: Scroll beginning
                ExposureManager.get().setAbsPosition(ev)
                val time = System.currentTimeMillis()
                ExposureManager.get().triggerViewCalculate(
                    TrackerConstants.TRIGGER_VIEW_CHANGED,
                    this,
                    lastVisibleViewMap
                )
                //TrackerLog.v("dispatchTouchEvent triggerViewCalculate =")
            } else {
                //TrackerLog.d("dispatchTouchEvent ACTION_MOVE but not in click limit")
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun getLastVisibleViewMap(): MutableMap<String, ExposureModel?> {
        return lastVisibleViewMap
    }

    /**
     * all the state change of view trigger the exposure event
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        // duplicate message in 1s
        val time = System.currentTimeMillis()
        if (time - lastOnLayoutSystemTimeMillis > 1000) {
            lastOnLayoutSystemTimeMillis = time

            ExposureManager.get().traverseViewTree(this, mReuseLayoutHook)
        }
        //ExposureManager.getInstance().triggerViewCalculate(TrackerInternalConstants.TRIGGER_VIEW_CHANGED, this, commonInfo, lastVisibleViewMap);
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDown(motionEvent: MotionEvent?): Boolean {
        TrackerLog.v("onDown")
        if (motionEvent != null) {
            ExposureManager.get().setAbsPosition(motionEvent)
        }
        return false
    }

    override fun onShowPress(motionEvent: MotionEvent?) {
        TrackerLog.v("onShowPress")
    }

    override fun onSingleTapUp(motionEvent: MotionEvent?): Boolean {
        TrackerLog.v("onSingleTapUp")
        // 用户轻击屏幕后抬起统计触发
        return false
    }

    override fun onLongPress(motionEvent: MotionEvent?) {
        TrackerLog.v("onLongPress")
    }

    override fun onScroll(
        motionEvent: MotionEvent?,
        motionEvent1: MotionEvent?,
        v: Float,
        v1: Float
    ): Boolean {
        return false
    }

    /**
     * Scene 2: Scroll ending
     *
     * @param motionEvent
     * @param motionEvent1
     * @param v
     * @param v1
     * @return
     */
    override fun onFling(
        motionEvent: MotionEvent?,
        motionEvent1: MotionEvent?,
        v: Float,
        v1: Float
    ): Boolean {
//        val time = System.currentTimeMillis()
//        TrackerLog.v("onFling triggerViewCalculate begin")
//        postDelayed({
//            ExposureManager.get().triggerViewCalculate(
//                TrackerConstants.TRIGGER_VIEW_CHANGED,
//                this@TrackerFrameLayout,
//                lastVisibleViewMap
//            )
//        }, 1000)
//        TrackerLog.v("onFling triggerViewCalculate end costTime=" + (System.currentTimeMillis() - time))
        return false
    }

    /**
     * the state change of window trigger the exposure event.
     * Scene 3: switch back and forth when press Home button.
     * Scene 4: enter into the next page
     * Scene 5: window replace
     *
     * @param hasFocus
     */
    override fun dispatchWindowFocusChanged(hasFocus: Boolean) {
        TrackerLog.d("TrackerFrameLayout dispatchWindowFocusChanged")
        if(hasFocus){
            TrackerLog.d("TrackerFrameLayout  lastVisibleViewMap.clear()")//获取到页面则清空原始
            lastVisibleViewMap.clear()
        }
        //val ts = System.currentTimeMillis()
        ExposureManager.get().triggerViewCalculate(
            TrackerConstants.TRIGGER_WINDOW_CHANGED,
            this,
            lastVisibleViewMap
        )
        super.dispatchWindowFocusChanged(hasFocus)
    }


    override fun dispatchVisibilityChanged(changedView: View, visibility: Int) {
        // Scene 6: switch page in the TabActivity
        TrackerLog.e("TrackerFrameLayout dispatchVisibilityChanged triggerViewCalculate begin$VISIBLE")
        if (visibility == View.GONE) {
            //val ts = System.currentTimeMillis()
            ExposureManager.get().triggerViewCalculate(
                TrackerConstants.TRIGGER_WINDOW_CHANGED,
                this,
                lastVisibleViewMap
            )
            TrackerLog.e("dispatchVisibilityChanged triggerViewCalculate end costTime=")
        } else {
            TrackerLog.e("trigger dispatchVisibilityChanged, visibility =$visibility")
        }
        super.dispatchVisibilityChanged(changedView!!, visibility)
    }
}