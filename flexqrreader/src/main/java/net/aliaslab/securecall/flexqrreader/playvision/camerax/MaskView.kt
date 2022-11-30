package net.aliaslab.securecall.flexqrreader.playvision.camerax

import android.animation.TimeAnimator
import android.animation.TimeAnimator.TimeListener
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import com.journeyapps.barcodescanner.Size
import net.aliaslab.securecall.flexqrreader.R


class MaskView : View {

    private val maskPaint: Paint = Paint()

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        maskPaint.alpha = 40
        maskPaint.color = resources.getColor(R.color.black)
    }

    protected override fun onDraw(canvas: Canvas) {

        val width = width
        val height = height

        // Draw the exterior (i.e. outside the framing rect) darkened

        // Draw the exterior (i.e. outside the framing rect) darkened
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), maskPaint)
        canvas.drawRect(
            0f,
            height.toFloat(),
            width.toFloat(),
            (height + 1).toFloat(),
            maskPaint
        )
        canvas.drawRect(
            (width + 1).toFloat(),
            height.toFloat(),
            width.toFloat(),
            (height + 1).toFloat(),
            maskPaint
        )
        canvas.drawRect(0f, (height + 1).toFloat(), width.toFloat(), height.toFloat(), maskPaint)
    }
}