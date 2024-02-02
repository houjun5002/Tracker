package com.richard.datatracker.ui

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.richard.datatracker.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_common.setOnClickListener {
            startActivity(Intent(this, CommonActivity::class.java))
        }

        btn_scroll.setOnClickListener {
            startActivity(Intent(this, ScrollActivity::class.java))
        }

        btn_banner.setOnClickListener {
            startActivity(Intent(this, BannerActivity::class.java))
        }

        btn_view_pager.setOnClickListener {
            startActivity(Intent(this, ViewPagerActivity::class.java))
        }

        btn_list.setOnClickListener {
            startActivity(Intent(this, RecyclerActivity::class.java))
        }

        btn_layer_test.setOnClickListener{
            startActivity(Intent(this, LayerTestActivity::class.java))
        }
    }

    // 判断一个 View 是否曝光
    fun isViewExposed(view: View?): Boolean {
        if (view == null || view.getWidth() === 0 || view.getHeight() === 0) {
            // View 为 null 或者宽高为 0，不曝光
            return false
        }
        val rect = Rect()
        val isVisible: Boolean = view.getGlobalVisibleRect(rect)
        return if (isVisible) {
            // 获取屏幕的宽度和高度
            val screenWidth: Int = view.getResources().getDisplayMetrics().widthPixels
            val screenHeight: Int = view.getResources().getDisplayMetrics().heightPixels
            // 计算可见矩形区域和屏幕的相交部分
            rect.intersect(0, 0, screenWidth, screenHeight)
            // 判断相交部分是否等于 View 的面积，如果是，则表示该 View 完全曝光
            val visibleArea: Int = rect.width() * rect.height()
            val totalArea: Int = view.getWidth() * view.getHeight()
            visibleArea >= totalArea
        } else {
            // 不可见，不曝光
            false
        }
    }
}