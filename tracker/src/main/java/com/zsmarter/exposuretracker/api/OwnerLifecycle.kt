package com.zsmarter.exposuretracker.api

import android.view.View


open interface OwnerLifecycle {
    fun onCreate(owner: View?)

    fun onStart(owner: View?)

    fun onResume(owner: View?)

    fun onPause(owner: View?)

    fun onStop(owner: View?)

    fun onDestroy(owner: View?)
}