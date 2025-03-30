package com.example.diplomamobile.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import com.example.diplomamobile.R

class MaxHeightScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ScrollView(context, attrs, defStyle) {

    private var maxHeight: Int = Int.MAX_VALUE

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.MaxHeightScrollView, 0, 0)
            try {
                maxHeight = a.getDimensionPixelSize(R.styleable.MaxHeightScrollView_maxHeight, Int.MAX_VALUE)
            } finally {
                a.recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxHeightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, maxHeightSpec)
    }

    fun setMaxHeight(height: Int) {
        maxHeight = height
        requestLayout()
    }
}
