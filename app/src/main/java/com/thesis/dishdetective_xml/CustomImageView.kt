package com.thesis.dishdetective_xml

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView

class CustomImageView(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attrs) {
    private var mListener: ((MotionEvent) -> Boolean)? = null

    fun setOnTouchListener(listener: (MotionEvent) -> Boolean) {
        mListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mListener?.let {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Handle action down event
                    it(event)
                }
                MotionEvent.ACTION_UP -> {
                    // Handle action up event
                    performClick()
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}