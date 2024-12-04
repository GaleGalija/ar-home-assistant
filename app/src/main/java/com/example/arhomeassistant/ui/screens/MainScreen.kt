package com.example.arhomeassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arhomeassistant.ui.ar.ARCameraView
import com.example.arhomeassistant.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    var showMaintenanceGuide by remember { mutableStateOf(false) }
    var showApplianceDetails by remember { mutableStateOf(false) }
    
    val selectedAppliance by viewModel.selectedAppliance.collectAsState()
    val maintenanceGuide by viewModel.currentMaintenanceGuide.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR Home Assistant") },
                actions = {
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        bottomSheet = {
            if (showMaintenanceGuide && maintenanceGuide != null) {
                MaintenanceGuideSheet(
                    guide = maintenanceGuide!!,
                    onDismiss = { showMaintenanceGuide = false }
                )
            } else if (showApplianceDetails && selectedAppliance != null) {
                ApplianceDetailsSheet(
                    appliance = selectedAppliance!!,
                    onDismiss = { showApplianceDetails = false },
                    onStartMaintenance = {
                        showApplianceDetails = false
                        showMaintenanceGuide = true
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // AR Camera View
            ARCameraView(
                onApplianceDetected = { appliance ->
                    viewModel.selectAppliance(appliance)
                    showApplianceDetails = true
                },
                onMarkerPlaced = { marker ->
                    viewModel.saveARMarker(marker)
                }
            )
            
            // Overlay Controls
            ARControls(
                onScanMode = { viewModel.setScanMode(it) },
                onCaptureImage = { viewModel.captureARScene() }
            )
        }
    }
}

@Composable
fun MaintenanceGuideSheet(
    guide: MaintenanceGuide,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = guide.title,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress indicator
            LinearProgressIndicator(
                progress = (currentStep + 1).toFloat() / guide.steps.size,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current step
            guide.steps.getOrNull(currentStep)?.let { step ->
                MaintenanceStepCard(
                    step = step,
                    onViewARModel = { /* Load AR model */ }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { currentStep-- },
                    enabled = currentStep > 0
                ) {
                    Text("Previous")
                }
                
                Button(
                    onClick = { currentStep++ },
                    enabled = currentStep < guide.steps.size - 1
                ) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
fun ApplianceDetailsSheet(
    appliance: Appliance,
    onDismiss: () -> Unit,
    onStartMaintenance: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = appliance.name,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Model: ${appliance.model}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = "Manufacturer: ${appliance.manufacturer}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Maintenance status
            MaintenanceStatusCard(appliance)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onStartMaintenance,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Maintenance Guide")
            }
        }
    }
}

@Composable
fun MaintenanceStepCard(
    step: MaintenanceStep,
    onViewARModel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Step ${step.order}",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (step.arModelUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onViewARModel) {
                    Text("View in AR")
                }
            }
            
            if (!step.tips.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tips:",
                    style = MaterialTheme.typography.titleSmall
                )
                step.tips.forEach { tip ->
                    Text("• $tip")
                }
            }
        }
    }
}

@Composable
fun MaintenanceStatusCard(appliance: Appliance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Maintenance Status",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val lastMaintenance = appliance.lastMaintenanceDate
            val nextMaintenance = lastMaintenance?.plusDays(appliance.maintenanceInterval.toLong())
            
            if (lastMaintenance != null) {
                Text("Last maintenance: ${lastMaintenance.formatDate()}")
                Text("Next maintenance: ${nextMaintenance?.formatDate()}")
            } else {
                Text("No maintenance records found")
            }
        }
    }
}

@Composable
fun ARControls(
    onScanMode: (Boolean) -> Unit,
    onCaptureImage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = { onScanMode(true) }
        ) {
            Icon(Icons.Default.Search, "Scan Mode")
        }
        
        IconButton(
            onClick = onCaptureImage
        ) {
            Icon(Icons.Default.Camera, "Capture")
        }
    }
}
