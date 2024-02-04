package com.hridoy.arsceneviewcomposesample

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.hridoy.arsceneviewcomposesample.ui.theme.ARSceneViewComposeSampleTheme
import io.github.sceneview.ar.ARScene

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ARSceneViewComposeSampleTheme {
                // A surface container using the 'background' color from the theme
                AvApp()
            }
        }
    }
}

@Composable
fun AvApp(
    mainViewModel: MainViewModel = viewModel(),
) {
    val node by mainViewModel.node.collectAsState()
    //Screen Status
    var status by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            nodes = node?.let { listOf(it) } ?: emptyList(),
            planeRenderer = true,
            onCreate = {
                it.setupAvConfigurations()
            },
            onFrame = {
                mainViewModel.renderVideo(it.updatedAugmentedImages)
            },
            onTrackingFailureChanged = { trackingFailureReason ->
                //Tracking ちゃんとできない場合
                onArTrackingFailureChanged = { reason ->
                    status = reason?.apply {
                        setupTrackingFailureChanged(trackingFailureReason = this)
                    }.toString()
                }
            }
        )
        //Middle Section
        Box(modifier = Modifier
            .fillMaxSize(), contentAlignment = Alignment.Center) {
            if (status.isNotEmpty()) {
                Text(
                    text = if(status.equals("null")) "" else status,
                    modifier = Modifier
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.background,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview
@Composable
fun AvAppPreview() {
    ARSceneViewComposeSampleTheme {
        AvApp()
    }
}
