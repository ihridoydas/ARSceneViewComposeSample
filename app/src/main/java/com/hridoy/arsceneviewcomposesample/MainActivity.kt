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
import androidx.core.view.isGone
import com.google.android.filament.textured.loadTexture
import com.google.ar.core.Config
import com.google.ar.core.TrackingFailureReason
import com.hridoy.arsceneviewcomposesample.ui.theme.ARSceneViewComposeSampleTheme
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.texture.TextureLoader
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
//    Box(modifier = Modifier.fillMaxSize()) {

        ARScene(
            modifier = Modifier,
            nodes = nodes,//node?.let { listOf(it) } ?: emptyList(),
            planeRenderer = true,
            onCreate = { arSceneView ->
                // Apply your configuration
                arSceneView.setupAvConfigurations()

                arSceneView?.apply {
                    onArTrackingFailureChanged = { reason ->
                        status = reason?.getDescription(context).toString()
                            // status.isGone = reason == null

                    }

                }


                arModelNode = ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL).apply {

                    loadModelGlbAsync(
                        glbFileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb",
                        autoAnimate = true,
                        scaleToUnits = 2.5f,
                        // Center the model horizontally and vertically
                        centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
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
            },
            onTap = { hitResult ->
                // User tapped in the AR view
            }
        )

//    }

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
