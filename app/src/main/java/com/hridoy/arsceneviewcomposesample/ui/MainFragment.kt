package com.hridoy.arsceneviewcomposesample.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hridoy.arsceneviewcomposesample.R
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.loadHdrIndirectLight
import io.github.sceneview.loaders.loadHdrSkybox
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.nodes.ModelNode

class MainFragment : Fragment(R.layout.fragment_main) {
    
    lateinit var sceneView : SceneView
    lateinit var loading : View
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sceneView = view.findViewById<SceneView>(R.id.sceneView).apply {
            setLifecycle(lifecycle = lifecycle)
        }

        loading = view.findViewById(R.id.loadingView)

        lifecycleScope.launchWhenCreated {
            val hdrFile = "environments/studio_small_09_2k.hdr"
            sceneView.loadHdrIndirectLight(hdrFile, specularFilter = true){
                intensity(30_000f)
            }
            sceneView.loadHdrSkybox(hdrFile){
                intensity(50_000f)
            }

            val model = sceneView.modelLoader.loadModel("models/MaterialSuite.glb")!!
            val modelNode = ModelNode(sceneView,model).apply {
                transform(
                    position = Position(z= -4.0f),
                    rotation = Rotation(x = 15.0f)
                )
                scaleToUnitsCube(2.0f)
                //                centerOrigin(Position(x=-1.0f, y=-1.0f))
                playAnimation()
            }
            sceneView.addChildNode(modelNode)
            loading.isGone = true
        }
    }
    
}