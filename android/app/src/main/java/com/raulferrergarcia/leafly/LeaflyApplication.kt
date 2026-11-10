package com.raulferrergarcia.leafly

import android.app.Application

class LeaflyApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
