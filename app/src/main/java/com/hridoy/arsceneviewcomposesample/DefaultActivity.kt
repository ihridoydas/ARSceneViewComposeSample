package com.hridoy.arsceneviewcomposesample

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen

class DefaultActivity : AppCompatActivity(R.layout.activity_basic_view) {

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var statusText: TextView

    var modelNode: ArModelNode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar)?.apply {
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.top
            }
            title = ""
        })

        modelNode?.detachAnchor()
        modelNode?.placementMode = PlacementMode.BEST_AVAILABLE



        //Tracking ちゃんとできない場合
        fun TrackingFailureReason.getDescription(context: Context) = when(this) {
            TrackingFailureReason.NONE -> ""
            TrackingFailureReason.BAD_STATE -> context.getString(R.string.sceneview_bad_state_message)
            TrackingFailureReason.INSUFFICIENT_LIGHT -> context.getString(
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    R.string.sceneview_insufficient_light_message
                } else {
                    R.string.sceneview_insufficient_light_android_s_message
                }
            )
            TrackingFailureReason.EXCESSIVE_MOTION -> context.getString(R.string.sceneview_excessive_motion_message)
            TrackingFailureReason.INSUFFICIENT_FEATURES -> context.getString(R.string.sceneview_insufficient_features_message)
            TrackingFailureReason.CAMERA_UNAVAILABLE -> context.getString(R.string.sceneview_camera_unavailable_message)
            else -> context.getString(R.string.sceneview_unknown_tracking_failure, this)
        }

        statusText = findViewById(R.id.statusText)
        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            onArTrackingFailureChanged = { reason ->
                statusText.text = reason?.getDescription(context)
                statusText.isGone = reason == null
            }
        }
        loadingView = findViewById(R.id.loadingView)

    }
}