package com.equationl.autocontroller

import android.app.Application
import com.equationl.autocontroller.utils.BtHelper

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        BtHelper.instance.init(this)
    }
}