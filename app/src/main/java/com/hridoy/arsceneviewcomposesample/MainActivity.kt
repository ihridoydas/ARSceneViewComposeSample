package com.hridoy.arsceneviewcomposesample

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.ar.core.Config
import com.google.ar.core.TrackingFailureReason
import com.hridoy.arsceneviewcomposesample.ui.theme.ARSceneViewComposeSampleTheme
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ARSceneViewComposeSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ARScreen()
                }
            }
        }
    }
}

@Composable
fun ARScreen() {
    val nodes = remember { mutableStateListOf<ArNode>() }
//    val _node = MutableStateFlow<ArNode?>(null)
//    val nodes = _node.asStateFlow()
//    val node by  nodes.collectAsState()

    var arModelNode : ArModelNode? = null

    var status by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize()) {

        ARScene(
            modifier = Modifier,
            nodes = nodes,//node?.let { listOf(it) } ?: emptyList(),
            planeRenderer = true,
            onCreate = { arSceneView ->
                // Apply your configuration
                arSceneView.configureSession { arSession, config ->
                    config.focusMode = Config.FocusMode.FIXED
                    config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                    config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
//                    config.augmentedImageDatabase = resources.openRawResource(R.raw.av_db).use { database ->
//                        AugmentedImageDatabase.deserialize(arSession, database)
//                    }
                }

                //Tracking ちゃんとできない場合
                fun TrackingFailureReason.getDescription(context: Context) = when (this) {
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


                arSceneView?.apply {
                    onArTrackingFailureChanged = { reason ->
                        status = reason?.getDescription(context).toString()

                    }

                }


                arModelNode = ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL).apply {

                    loadModelGlbAsync(
                        glbFileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb",
                        autoAnimate = true,
                        scaleToUnits = 2.5f,
                        // Place the model origin at the bottom center
                        centerOrigin = Position(y = -1.0f)
                    ) {
                        arSceneView.planeRenderer.isVisible = true

                    }
                    onAnchorChanged = { anchor ->

                    }
                    onHitResult = { node, _ ->

                    }
                }
                arSceneView.addChild(arModelNode!!)


            },
            onSessionCreate = { session ->
                // Configure the ARCore session

            },
            onFrame = { arFrame ->
                // Retrieve ARCore frame update
                PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL
            },
            onTap = { hitResult ->
                // User tapped in the AR view
            }
        )

    }

    Column(
        modifier = Modifier.padding(5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = status,
            modifier = Modifier.background(Color.White),
        )
    }


}
