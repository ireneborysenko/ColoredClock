package com.borysenko.coloredclock

import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.app.Activity
import android.graphics.Paint
import android.os.Handler
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    private val DATE_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val PAINT = Paint()
    private var mHalfWidth: Int = 0
    private var mHalfHeight:Int = 0

    private val mCalendar = Calendar.getInstance()

    private var mHandler: Handler? = null
    private var mDate: Date? = null

    private val mDrawRunner = Runnable { DrawView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PAINT.color = Color.WHITE
        PAINT.isAntiAlias = true
        setContentView(DrawView(this))

        mHandler = Handler()
        mDate = Date()

        mHandler!!.post(mDrawRunner)
    }

    internal inner class DrawView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
        private var drawThread: DrawThread? = null

        init {
            holder.addCallback(this)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            mHalfWidth = width shr 1
            mHalfHeight = height shr 1
            PAINT.textSize = (width / 7).toFloat()
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            drawThread = DrawThread(getHolder())
            drawThread!!.setRunning(true)
            drawThread!!.start()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            var retry = true
            drawThread!!.setRunning(false)
            while (retry) {
                try {
                    drawThread!!.join()
                    retry = false
                } catch (e: InterruptedException) {
                }
                mHandler?.removeCallbacks(mDrawRunner)
            }
        }

        internal inner class DrawThread(private val surfaceHolder: SurfaceHolder) : Thread() {

            private var running = false

            fun setRunning(running: Boolean) {
                this.running = running
            }

            override fun run() {
                var canvas: Canvas?
                while (running) {
                    canvas = null
                    try {
                        canvas = surfaceHolder.lockCanvas(null)
                        mDate = Date()

                        val date = DATE_FORMAT.format(mDate)
                        mCalendar.time = mDate

                        val hours = mCalendar.get(Calendar.HOUR_OF_DAY)
                        val minutes = mCalendar.get(Calendar.MINUTE)
                        val seconds = mCalendar.get(Calendar.SECOND)

                        canvas.drawColor(
                            Color.parseColor(String.format(Locale.US, "#%02d%02d%02d", hours, minutes, seconds)))
                        canvas.drawText(date, mHalfWidth - PAINT.measureText(date) / 2, mHalfHeight.toFloat(), PAINT)
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas)
                        }
                    }
                }
            }
        }
    }
}
