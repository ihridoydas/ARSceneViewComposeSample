package com.hridoy.arsceneviewcomposesample

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.core.view.isGone
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.ar.core.Anchor
import com.hridoy.arsceneviewcomposesample.ui.theme.ARSceneViewComposeSampleTheme
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import java.util.Objects

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //check Device Support or not 
        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(applicationContext, "Device not supported", Toast.LENGTH_LONG)
                .show()
        }
        setContent {
            ARSceneViewComposeSampleTheme {
                val systemUiController: SystemUiController = rememberSystemUiController()
                systemUiController.isStatusBarVisible = false // Status bar
                systemUiController.isNavigationBarVisible = true // Navigation bar
                systemUiController.isSystemBarsVisible = false // Status & Navigation bars
                //AR Scene View
                ARScreen()
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

    lateinit var arModelNode: ArModelNode
    lateinit var cursorNode: CursorNode
    lateinit var arSceneView: ArSceneView

    var status by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        ARScene(
            modifier = Modifier,
            nodes = nodes,//node?.let { listOf(it) } ?: emptyList(),
            planeRenderer = true,
            onCreate = { arSceneView ->
                // Apply your configuration
                arSceneView.setupAvConfigurations()

                arSceneView?.apply {
                    planeRenderer.isVisible = false
                    onArTrackingFailureChanged = { reason ->
                        status = reason?.getDescription(context).toString()

                    }
                    onArSessionFailed = { _: Exception ->
                        // If AR is not available or the camara permission has been denied, we add the model
                        // directly to the scene for a fallback 3D only usage
                        arModelNode.centerModel(origin = Position(x = 0.0f, y = 0.0f, z = 0.0f))
                        arModelNode.scaleModel(units = 1.0f)
                        arSceneView.addChild(arModelNode)
                    }
                    onTapAr = { hitResult, _ ->
                        anchorOrMove(hitResult.createAnchor(), arModelNode = arModelNode, arSceneView = arSceneView)
                    }


                }


//                arModelNode =
//                    ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL).apply {
//
//                        loadModelGlbAsync(
//                            glbFileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb",
//                            autoAnimate = true,
//                            scaleToUnits = 2.5f,
//                            // Center the model horizontally and vertically
//                            centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
//                        ) {
//                            arSceneView.planeRenderer.isVisible = true
//                        }
//                        onAnchorChanged = { anchor ->
//
//                        }
//                        onHitResult = { node, _ ->
//
//                        }
//                    }
//                arSceneView.addChild(arModelNode!!)


                cursorNode = CursorNode().apply {
                    onHitResult = { node, _ ->
                        !node.isTracking
                    }
                }
                arSceneView.addChild(cursorNode)

                arModelNode = ArModelNode(
                    modelGlbFileLocation = "models/spiderbot.glb",
                    onLoaded = { modelInstance ->

                    })





            },
            onSessionCreate = { session ->
                // Configure the ARCore session

            },
            onFrame = { arFrame ->
                // Retrieve ARCore frame update
            },
            onTap = { hitResult ->
                // User tapped in the AR view
            }
        )

        Text(
            text = status,
            modifier = Modifier
                .background(Color.White)
                .align(Alignment.Center),
        )

        Button(onClick = {
            cursorNode.createAnchor()?.let { anchorOrMove(anchor = it, arModelNode = arModelNode, arSceneView = arSceneView) }
        }, modifier = Modifier.align(Alignment.BottomCenter)) {
            Text(text = "Set Anchor")
        }
    }
}

fun anchorOrMove(anchor: Anchor,arModelNode: ArModelNode,arSceneView: ArSceneView) {
    if (!arSceneView.children.contains(arModelNode)) {
        arSceneView.addChild(arModelNode)
    }
    arModelNode.anchor = anchor
}

fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
    val MIN_OPENGL_VERSION = 3.0
    val TAG: String = MainActivity::class.java.simpleName
    val openGlVersionString =
        (Objects.requireNonNull(
            activity
                .getSystemService(Context.ACTIVITY_SERVICE)
        ) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
    if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
        Log.e(TAG, "Sceneform requires OpenGL ES ${MIN_OPENGL_VERSION} later")
        Toast.makeText(
            activity,
            "Sceneform requires OpenGL ES ${MIN_OPENGL_VERSION} or later",
            Toast.LENGTH_LONG
        )
            .show()
        activity.finish()
        return false
    }
    return true
}
