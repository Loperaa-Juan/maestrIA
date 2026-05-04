package com.juanjoselopera.proy_prog_mobile.app.util

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.children

private const val DEFAULT_DURATION_MS = 420L
private const val DEFAULT_STAGGER_MS = 90L

fun ViewGroup.animateChildrenSlideInFromRight(
    durationMs: Long = DEFAULT_DURATION_MS,
    staggerMs: Long = DEFAULT_STAGGER_MS,
) {
    val offset = resources.displayMetrics.widthPixels.toFloat()
    staggerEntrance(translationXFrom = offset, durationMs = durationMs, staggerMs = staggerMs)
}

fun ViewGroup.animateChildrenSlideInFromBottom(
    durationMs: Long = DEFAULT_DURATION_MS,
    staggerMs: Long = DEFAULT_STAGGER_MS,
) {
    val offset = resources.displayMetrics.heightPixels * 0.25f
    staggerEntrance(translationYFrom = offset, durationMs = durationMs, staggerMs = staggerMs)
}

private fun ViewGroup.staggerEntrance(
    translationXFrom: Float = 0f,
    translationYFrom: Float = 0f,
    durationMs: Long,
    staggerMs: Long,
) {
    children.forEachIndexed { index, child ->
        child.translationX = translationXFrom
        child.translationY = translationYFrom
        child.alpha = 0f
        child.animate()
            .translationX(0f)
            .translationY(0f)
            .alpha(1f)
            .setStartDelay(index * staggerMs)
            .setDuration(durationMs)
            .setInterpolator(DecelerateInterpolator(1.6f))
            .start()
    }
}

fun View.findFirstViewGroupById(id: Int): ViewGroup? = findViewById<View?>(id) as? ViewGroup
