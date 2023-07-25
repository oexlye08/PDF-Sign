package id.my.okisulton.pdfsign.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PaintView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private val paint: Paint
    private val path: Path
    var arl: ArrayList<MotionEvent>

    init {
        paint = Paint()
        path = Path()
        arl = ArrayList()
        paint.isAntiAlias = true
        paint.strokeWidth = 8f
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        arl.add(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(eventX, eventY)
                return true
            }

            MotionEvent.ACTION_MOVE -> path.lineTo(eventX, eventY)
            MotionEvent.ACTION_UP -> {}
            else -> return false
        }

        // Schedules a repaint.
        invalidate()
        return true
    }
}