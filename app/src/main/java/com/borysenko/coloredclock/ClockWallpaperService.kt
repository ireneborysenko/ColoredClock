package com.borysenko.coloredclock

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.util.*

/**
 * Created by Android Studio.
 * User: Iryna
 * Date: 17/06/19
 * Time: 16:57
 */
class ClockWallpaperService : WallpaperService() {
    companion object {
        private const val DELAY_MILLIS = 1000
        private val PAINT = Paint()
        init {
            PAINT.color = Color.WHITE
            PAINT.isAntiAlias = true
        }
    }

    override fun onCreateEngine(): Engine {
        return ClockEngine()
    }

    private inner class ClockEngine internal constructor() : Engine() {

        private val mCalendar = Calendar.getInstance()

        private val mHandler: Handler = Handler()
        private var mVisible = true

        private val mDrawRunner = Runnable { draw() }

        override fun onVisibilityChanged(visible: Boolean) {
            this.mVisible = visible
            if (visible) {
                mHandler.post(mDrawRunner)
            } else {
                mHandler.removeCallbacks(mDrawRunner)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            this.mVisible = false
            mHandler.removeCallbacks(mDrawRunner)
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                mCalendar.time = Date()

                val hours = mCalendar.get(Calendar.HOUR_OF_DAY)
                val minutes = mCalendar.get(Calendar.MINUTE)
                val seconds = mCalendar.get(Calendar.SECOND)

                canvas!!.drawColor(
                    Color.parseColor(String.format(Locale.US, "#%02d%02d%02d", hours, minutes, seconds))
                )
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
            mHandler.removeCallbacks(mDrawRunner)
            if (mVisible) {
                mHandler.postDelayed(mDrawRunner, DELAY_MILLIS.toLong())
            }
        }
    }
}