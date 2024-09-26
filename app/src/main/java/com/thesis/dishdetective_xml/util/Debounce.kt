package com.thesis.dishdetective_xml.util

import android.view.View

object Debounce {
    fun View.setDebounceClickListener(debounceTime: Long = 1000L, action: (View) -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime = 0L
            override fun onClick(v: View?) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > debounceTime) {
                    lastClickTime = currentTime
                    action(v!!)
                }
            }
        })
    }
}