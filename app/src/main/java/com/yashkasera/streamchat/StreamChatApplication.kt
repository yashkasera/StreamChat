package com.yashkasera.streamchat

import android.app.Application

class StreamChatApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppObjectController.init(this)
    }
}