package com.hridoy.arsceneviewcomposesample

import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.filament.utils.HDRLoader
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.gorisse.thomas.sceneform.environment
import com.gorisse.thomas.sceneform.environment.loadEnvironment
import com.gorisse.thomas.sceneform.light.LightEstimationConfig
import com.gorisse.thomas.sceneform.lightEstimationConfig
import com.hridoy.arsceneviewcomposesample.databinding.ActivityBasicViewBinding

const val serverUrl = "https://sceneview.github.io/assets"
class DefaultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBasicViewBinding

    private lateinit var arFragment: ArFragment
    private val sceneView: com.google.ar.sceneform.ArSceneView get() = arFragment.arSceneView
    private val scene: Scene get() = sceneView.scene

    var model: Renderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityBasicViewBinding>(this, R.layout.activity_basic_view)
            .apply {
                lifecycleOwner = this@DefaultActivity
                activity = this@DefaultActivity
            }
        setSupportActionBar(binding.toolbar)

        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            (fragment as? ArFragment)?.let { onArFragmentAttached(it) }
        }
        supportFragmentManager.commit {
            add(R.id.arFragment, ArFragment::class.java, Bundle().apply {
                putBoolean(ArFragment.ARGUMENT_FULLSCREEN, false)
            })
        }

        lifecycleScope.launchWhenResumed {
            sceneView.environment = HDRLoader.loadEnvironment(
                this@DefaultActivity,
                "$serverUrl/environments/evening_meadow_2k.hdr"
            )
            ModelRenderable.builder()
                .setSource(
                    this@DefaultActivity,
                    Uri.parse("$serverUrl/models/ClearCoat.glb")
                )
                .setIsFilamentGltf(true)
                .build()
                .thenAccept {
                    model = it
                }
        }
    }

    fun onArFragmentAttached(fragment: ArFragment) {
        arFragment = fragment
        arFragment.setOnTapArPlaneListener(::onTapPlane)
    }

    fun onTapPlane(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        if (model == null) {
            toast("Loading...")
            return
        }

        val anchorNode = AnchorNode(hitResult.createAnchor()).apply {
            localScale = Vector3(0.05f, 0.05f, 0.05f)
        }
        TransformableNode(arFragment.transformationSystem).apply {
            setParent(anchorNode)
            setRenderable(model)
                .animate(true).start()
        }
        scene.addChild(anchorNode)
    }

    fun onEstimationModeChanged(button: CompoundButton?, checked: Boolean) {
        if (checked) {
            button?.id?.let { estimationMode ->
                sceneView.lightEstimationConfig = LightEstimationConfig(
                    mode = when (estimationMode) {
                       R.id.environmentalHdrMode,
                       R.id.environmentalHdrNoReflections,
                       R.id.environmentalHdrNoSpecularFilter -> {
                            Config.LightEstimationMode.ENVIRONMENTAL_HDR
                        }
                        R.id.ambientIntensityMode -> Config.LightEstimationMode.AMBIENT_INTENSITY
                        else -> Config.LightEstimationMode.DISABLED
                    },
                    environmentalHdrReflections = estimationMode != R.id.environmentalHdrNoReflections,
                    environmentalHdrSpecularFilter = estimationMode != R.id.environmentalHdrNoSpecularFilter
                )
            }
        }
    }
}
