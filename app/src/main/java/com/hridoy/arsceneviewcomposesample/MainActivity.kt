package com.hridoy.arsceneviewcomposesample

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.filament.utils.HDRLoader
import com.google.ar.core.Config
import com.hridoy.arsceneviewcomposesample.ui.components.StatusOfScreen
import com.hridoy.arsceneviewcomposesample.ui.theme.ARSceneViewComposeSampleTheme
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.environment.loadEnvironment
import io.github.sceneview.math.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Set Orientation Landscape
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        //check Device Support or not 
        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(applicationContext, "Device not supported", Toast.LENGTH_LONG)
                .show()
        }
        setContent {
            ARSceneViewComposeSampleTheme {
                val systemUiController: SystemUiController = rememberSystemUiController()
                systemUiController.isStatusBarVisible = true // Status bar
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
   // val nodes = remember { mutableStateListOf<ArNode>() }
    val _node = MutableStateFlow<ArNode?>(null)
    val nodes = _node.asStateFlow()
    val node by  nodes.collectAsState()

    lateinit var arModelNode: ArModelNode
    lateinit var cursorNode: CursorNode

    //Context
    val context= LocalContext.current
    //Global AR Screen View
    var sceneView  = remember { ArSceneView(context) }

    //Coroutine State
    val scope = rememberCoroutineScope()

    //Screen Status
    var status by remember { mutableStateOf("") }

    //Scroll State
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        ARScene(
            modifier = Modifier,
            nodes = node?.let { listOf(it) } ?: emptyList(),//nodes,//
            planeRenderer = true,
            onCreate = { arSceneView ->
                // Apply your configuration

                sceneView = arSceneView
                arSceneView.setupARConfigurations()
                arSceneView.isDepthOcclusionEnabled = false

                //Light Estimate
                scope.launch {
                    arSceneView.environment = HDRLoader.loadEnvironment(
                        context,
                        "$serverUrl/environments/evening_meadow_2k.hdr",
                        specularFilter = true,
                        createSkybox = true
                    )

                    arSceneView?.apply {
                        planeRenderer.isVisible = false
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
                session.configure {
                    it.depthMode = Config.DepthMode.RAW_DEPTH_ONLY
                }

            },
            onFrame = { arFrame ->
                // Retrieve ARCore frame update
                //this.depthEnabled = true
            },
            onTap = { hitResult ->
                // User tapped in the AR view
            },
            onTrackingFailureChanged = {
                //Tracking ちゃんとできない場合
                onArTrackingFailureChanged = { reason ->
                    status = reason?.apply {
                        setupTrackingFailureChanged(trackingFailureReason = this)
                    }.toString()

                }

            }
        )

        //Middle Section
        StatusOfScreen(status)
        //Bottom Section
        Column (modifier = Modifier.fillMaxSize(),
            Arrangement.Bottom,Alignment.CenterHorizontally){
            Row(modifier = Modifier
                .horizontalScroll(state = scrollState)
                .padding(5.dp)
                .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                Button(onClick = {
                    lightEstimate(sceneView, mode = LightEstimationMode.ENVIRONMENTAL_HDR)
                }, modifier = Modifier) {
                    Text(text = "ENVIRONMENTAL_HDR")
                }
                Button(onClick = {
                    lightEstimate(sceneView, mode = LightEstimationMode.ENVIRONMENTAL_HDR_FAKE_REFLECTIONS)
                }, modifier = Modifier) {
                    Text(text = "ENVIRONMENTAL_HDR_FAKE_REFLECTIONS")
                }
                Button(onClick = {

                    lightEstimate(sceneView, mode = LightEstimationMode.ENVIRONMENTAL_HDR_NO_REFLECTIONS)
                }, modifier = Modifier) {
                    Text(text = "ENVIRONMENTAL_HDR_NO_REFLECTIONS")
                }

                Button(onClick = {
                    lightEstimate(sceneView, mode = LightEstimationMode.AMBIENT_INTENSITY)
                }, modifier = Modifier) {
                    Text(text = "AMBIENT_INTENSITY")
                }
                Button(onClick = {
                    lightEstimate(sceneView, mode = LightEstimationMode.DISABLED)
                }, modifier = Modifier) {
                    Text(text = "DISABLED")
                }
                Button(onClick = {

                    //cursorNode.createAnchor()?.let { anchorOrMove(anchor = it, arModelNode = arModelNode, arSceneView = arSceneView) }
                }, modifier = Modifier) {
                    Text(text = "Set Anchor")
                }
            }
        }


    }
}