package com.juanjoselopera.proy_prog_mobile.app.util

import android.app.Activity
import android.os.Build
import com.juanjoselopera.proy_prog_mobile.R

fun Activity.applySlideInTransition() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_OPEN,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_CLOSE,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
