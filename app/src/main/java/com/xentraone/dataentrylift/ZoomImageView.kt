package com.xentraone.dataentrylift

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

/** Image view with pinch-to-zoom, drag and double-tap zoom for the report
 *  preview. Starts fitted to the screen; double-tap zooms in/out. */
class ZoomImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val drawMatrix = Matrix()
    private var fitScale = 1f
    private var zoom = 1f
    private val maxZoom = 8f

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val previous = zoom
                zoom = min(maxZoom, max(1f, zoom * detector.scaleFactor))
                val delta = zoom / previous
                drawMatrix.postScale(delta, delta, detector.focusX, detector.focusY)
                clamp()
                imageMatrix = drawMatrix
                return true
            }
        }
    )

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
            ): Boolean {
                if (zoom > 1f) {
                    drawMatrix.postTranslate(-distanceX, -distanceY)
                    clamp()
                    imageMatrix = drawMatrix
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (zoom > 1f) {
                    reset()
                } else {
                    val target = 3f
                    drawMatrix.postScale(target, target, e.x, e.y)
                    zoom = target
                    clamp()
                    imageMatrix = drawMatrix
                }
                return true
            }
        }
    )

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        reset()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        reset()
    }

    private fun reset() {
        val d = drawable ?: return
        if (width == 0 || height == 0) return
        val dw = d.intrinsicWidth.toFloat()
        val dh = d.intrinsicHeight.toFloat()
        if (dw <= 0f || dh <= 0f) return
        fitScale = min(width / dw, height / dh)
        zoom = 1f
        drawMatrix.reset()
        drawMatrix.postScale(fitScale, fitScale)
        drawMatrix.postTranslate((width - dw * fitScale) / 2f, (height - dh * fitScale) / 2f)
        imageMatrix = drawMatrix
    }

    /** Keep the image inside the view: centered while smaller than the view,
     *  never showing empty edges once zoomed in. */
    private fun clamp() {
        val d = drawable ?: return
        val values = FloatArray(9)
        drawMatrix.getValues(values)
        val s = values[Matrix.MSCALE_X]
        val contentW = d.intrinsicWidth * s
        val contentH = d.intrinsicHeight * s
        var tx = values[Matrix.MTRANS_X]
        var ty = values[Matrix.MTRANS_Y]
        tx = if (contentW <= width) (width - contentW) / 2f
        else min(0f, max(width - contentW, tx))
        ty = if (contentH <= height) (height - contentH) / 2f
        else min(0f, max(height - contentH, ty))
        values[Matrix.MTRANS_X] = tx
        values[Matrix.MTRANS_Y] = ty
        drawMatrix.setValues(values)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never let parent views steal the gesture from the preview.
        parent?.requestDisallowInterceptTouchEvent(true)
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) performClick()
        return true
    }
}
