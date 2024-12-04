package com.example.arhomeassistant.ui.ar

import android.content.Context
import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.coroutines.launch

@Composable
fun ARCameraView(
    onApplianceDetected: (Appliance) -> Unit,
    onMarkerPlaced: (ARMarker) -> Unit
) {
    var arSceneView: SceneView? by remember { mutableStateOf(null) }
    var arSession: Session? by remember { mutableStateOf(null) }
    
    AndroidView(
        factory = { context ->
            SceneView(context).apply {
                setupAR(context)
                arSceneView = this
            }
        },
        update = { view ->
            // Update view if needed
        }
    )
    
    DisposableEffect(Unit) {
        onDispose {
            arSceneView?.destroy()
            arSession?.close()
        }
    }
}

private fun SceneView.setupAR(context: Context) {
    // Configure AR session
    val session = Session(context)
    val config = Config(session)
    config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
    config.focusMode = Config.FocusMode.AUTO
    session.configure(config)
    
    // Set up plane detection
    config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
    
    // Enable object detection
    config.augmentedImageDatabase = createAugmentedImageDatabase(session, context)
    
    scene = Scene(this)
    scene.addOnUpdateListener { frameTime ->
        val frame = session.update()
        processFrame(frame)
    }
}

private fun createAugmentedImageDatabase(session: Session, context: Context): AugmentedImageDatabase {
    val database = AugmentedImageDatabase(session)
    // Add images for detection
    // Example: database.addImage("washer", loadWasherImage())
    return database
}

private fun processFrame(frame: Frame) {
    // Process detected planes
    frame.getUpdatedTrackables(Plane::class.java).forEach { plane ->
        when (plane.trackingState) {
            TrackingState.TRACKING -> handlePlaneTracking(plane)
            TrackingState.PAUSED -> handlePlanePaused(plane)
            TrackingState.STOPPED -> handlePlaneStopped(plane)
        }
    }
    
    // Process detected images
    frame.getUpdatedTrackables(AugmentedImage::class.java).forEach { image ->
        when (image.trackingState) {
            TrackingState.TRACKING -> handleImageTracking(image)
            else -> { /* Handle other states */ }
        }
    }
}

private fun handlePlaneTracking(plane: Plane) {
    // Create visual indicator for detected plane
    if (!plane.isSubsumed) {
        // Add plane visualization
        val anchor = plane.createAnchor(plane.centerPose)
        createPlaneVisualization(anchor)
    }
}

private fun handleImageTracking(image: AugmentedImage) {
    // Create AR content for detected appliance
    val anchor = image.createAnchor(image.centerPose)
    createApplianceVisualization(anchor, image.name)
}

private fun createPlaneVisualization(anchor: Anchor) {
    val anchorNode = AnchorNode(anchor)
    val node = TransformableNode(arFragment.transformationSystem)
    node.setParent(anchorNode)
    
    // Add visual plane indicator
    ViewRenderable.builder()
        .setView(context, R.layout.plane_indicator)
        .build()
        .thenAccept { renderable ->
            node.renderable = renderable
        }
}

private fun createApplianceVisualization(anchor: Anchor, applianceName: String) {
    val anchorNode = AnchorNode(anchor)
    val node = TransformableNode(arFragment.transformationSystem)
    node.setParent(anchorNode)
    
    // Load 3D model for the appliance
    ModelRenderable.builder()
        .setSource(context, getApplianceModelResource(applianceName))
        .build()
        .thenAccept { renderable ->
            node.renderable = renderable
            
            // Add information panel
            createInfoPanel(node, applianceName)
        }
}

private fun createInfoPanel(parentNode: Node, applianceName: String) {
    ViewRenderable.builder()
        .setView(context, R.layout.appliance_info_panel)
        .build()
        .thenAccept { renderable ->
            val node = Node()
            node.setParent(parentNode)
            node.renderable = renderable
            node.localPosition = Vector3(0f, 0.5f, 0f)
        }
}

private fun getApplianceModelResource(applianceName: String): Int {
    return when (applianceName.toLowerCase()) {
        "washer" -> R.raw.washer_model
        "dryer" -> R.raw.dryer_model
        "dishwasher" -> R.raw.dishwasher_model
        "refrigerator" -> R.raw.refrigerator_model
        else -> R.raw.default_appliance_model
    }
}
