package com.borysenko.coloredclock

import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.app.Activity


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(DrawView(this))
    }

    internal inner class DrawView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

        private var drawThread: DrawThread? = null

        init {
            holder.addCallback(this)
        }

        override fun surfaceChanged(
            holder: SurfaceHolder, format: Int, width: Int,
            height: Int
        ) {

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
                        if (canvas == null)
                            continue
                        canvas.drawColor(Color.GREEN)
                    } finally {
                            surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
            }
        }
    }
}
