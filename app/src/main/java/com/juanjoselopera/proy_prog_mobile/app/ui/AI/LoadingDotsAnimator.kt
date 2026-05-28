package com.juanjoselopera.proy_prog_mobile.app.ui.AI

import android.os.Handler
import android.os.Looper
import android.widget.TextView

class LoadingDotsAnimator(
    private val textView: TextView,
    private val baseText: String,
    private val intervalMs: Long = 500L
) {
    private val handler = Handler(Looper.getMainLooper())
    private var dotCount = 0
    private var running = false

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            dotCount = (dotCount % 3) + 1
            textView.text = baseText + ".".repeat(dotCount)
            handler.postDelayed(this, intervalMs)
        }
    }

    fun start() {
        if (running) return
        running = true
        handler.post(tick)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(tick)
        textView.text = baseText
    }
}
