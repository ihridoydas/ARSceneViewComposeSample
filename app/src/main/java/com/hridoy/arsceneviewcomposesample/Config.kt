package com.hridoy.arsceneviewcomposesample

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.ar.core.Config
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ArSceneView

fun ArSceneView.setupARConfigurations() {
    configureSession { arSession, config ->
        config.focusMode = Config.FocusMode.AUTO
        config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
    }
}

fun setupTrackingFailureChanged(trackingFailureReason: TrackingFailureReason) {
    trackingFailureReason.apply {
        when (this) {
            TrackingFailureReason.NONE -> NONE
            TrackingFailureReason.BAD_STATE -> BAD_STATE
            TrackingFailureReason.INSUFFICIENT_LIGHT -> INSUFFICIENT_LIGHT
            TrackingFailureReason.EXCESSIVE_MOTION -> EXCESSIVE_MOTION
            TrackingFailureReason.INSUFFICIENT_FEATURES -> INSUFFICIENT_FEATURES
            TrackingFailureReason.CAMERA_UNAVAILABLE -> CAMERA_UNAVAILABLE
        }
    }
}

//https://engawapg.net/jetpack-compose/2065/fullscreen/
fun enableFullScreen(context: Context) {
    val window = context.findActivity().window
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(WindowInsetsCompat.Type.systemBars())
    }
}

fun disableFullScreen(context: Context) {
    val window = context.findActivity().window
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(window, window.decorView).apply {
        show(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}
