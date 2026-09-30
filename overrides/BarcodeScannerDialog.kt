package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.FoodItem
import com.example.domain.model.BarcodeLookupException
import com.example.util.BarcodeUtils
import com.example.util.StringKey
import com.example.util.appString
import com.example.util.localizedFoodName
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

/**
 * Production barcode scanner backed by CameraX + on-device ML Kit barcode recognition.
 *
 * The scanner only identifies the barcode. Product lookup remains inside SlimTrack's
 * repository/database layer via [lookupProduct], keeping camera code independent from data code.
 */
@Composable
fun BarcodeScannerDialog(
    lookupProduct: suspend (String) -> FoodItem?,
    saveProduct: suspend (FoodItem) -> FoodItem,
    onDismiss: () -> Unit,
    onProductFound: (FoodItem) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var barcodeInput by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<FoodItem?>(null) }
    var notFound by remember { mutableStateOf(false) }
    var lookupError by remember { mutableStateOf<StringKey?>(null) }
    var incompleteProductName by remember { mutableStateOf<String?>(null) }
    var showCustomForm by remember { mutableStateOf(false) }
    var isLookingUp by remember { mutableStateOf(false) }
    var scanEnabled by remember { mutableStateOf(true) }
    var cameraError by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        scanEnabled = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun lookup(code: String) {
        val normalized = BarcodeUtils.normalize(code)
        if (isLookingUp) return
        if (normalized.length !in setOf(8, 12, 13, 14)) {
            lookupError = StringKey.DIALOG_BARCODE_INVALID_CODE
            notFound = false
            searchResult = null
            return
        }

        barcodeInput = normalized
        lookupError = null
        incompleteProductName = null
        showCustomForm = false
        notFound = false
        searchResult = null
        isLookingUp = true
        scanEnabled = false

        scope.launch {
            try {
                // The repository owns UPC/EAN equivalence and online/offline policy.
                searchResult = lookupProduct(normalized)
                notFound = searchResult == null
            } catch (error: CancellationException) {
                throw error
            } catch (error: BarcodeLookupException) {
                incompleteProductName = error.productName
                lookupError = when (error.reason) {
                    BarcodeLookupException.Reason.NETWORK -> StringKey.DIALOG_BARCODE_NETWORK_ERROR
                    BarcodeLookupException.Reason.SERVICE -> StringKey.DIALOG_BARCODE_SERVICE_ERROR
                    BarcodeLookupException.Reason.INCOMPLETE_NUTRITION -> StringKey.DIALOG_BARCODE_INCOMPLETE
                    BarcodeLookupException.Reason.INVALID_RESPONSE -> StringKey.DIALOG_BARCODE_SERVICE_ERROR
                }
            } catch (_: Exception) {
                lookupError = StringKey.DIALOG_BARCODE_SERVICE_ERROR
            } finally {
                isLookingUp = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = appString(StringKey.DIALOG_BARCODE_TITLE),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = appString(StringKey.DIALOG_BARCODE_SUBTITLE),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when {
                    hasCameraPermission && !cameraError && !showCustomForm -> {
                        BarcodeCameraPreview(
                            enabled = scanEnabled && !isLookingUp,
                            onBarcodeDetected = { detected -> lookup(detected) },
                            onCameraError = {
                                cameraError = true
                                scanEnabled = false
                            }
                        )
                        Text(
                            text = appString(StringKey.DIALOG_BARCODE_CAMERA_HINT),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    !hasCameraPermission && !showCustomForm -> {
                        CameraMessageCard(
                            text = appString(StringKey.DIALOG_BARCODE_CAMERA_PERMISSION)
                        )
                        OutlinedButton(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(appString(StringKey.DIALOG_BARCODE_GRANT_PERMISSION))
                        }
                    }

                    !showCustomForm -> {
                        CameraMessageCard(
                            text = appString(StringKey.DIALOG_BARCODE_CAMERA_ERROR)
                        )
                    }
                }

                if (isLookingUp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = appString(StringKey.DIALOG_BARCODE_SEARCHING),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                searchResult?.let { product ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "✓ ${localizedFoodName(product.name)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${product.calories.toInt()} ${appString(StringKey.UNIT_KCAL)} / 100 ${appString(StringKey.UNIT_GRAM)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Text(
                    text = appString(StringKey.DIALOG_BARCODE_SOURCE),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (notFound || lookupError != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = appString(lookupError ?: StringKey.DIALOG_BARCODE_NOT_FOUND),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        incompleteProductName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        if (!showCustomForm) {
                            OutlinedButton(
                                onClick = { showCustomForm = true },
                                enabled = !isLookingUp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(appString(StringKey.DIALOG_BARCODE_ADD_MANUALLY))
                            }
                        }
                        if (hasCameraPermission && !cameraError) {
                            OutlinedButton(
                                onClick = {
                                    barcodeInput = ""
                                    searchResult = null
                                    notFound = false
                                    lookupError = null
                                    incompleteProductName = null
                                    showCustomForm = false
                                    scanEnabled = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(appString(StringKey.DIALOG_BARCODE_SCAN_AGAIN))
                            }
                        }
                    }
                }

                if (showCustomForm) {
                    CustomFoodForm(
                        initialName = incompleteProductName.orEmpty(),
                        saving = isLookingUp,
                        onSave = { name, category, calories, protein, fat, carbs ->
                            isLookingUp = true
                            scope.launch {
                                try {
                                    val product = saveProduct(FoodItem(
                                        name = name, category = category, calories = calories,
                                        protein = protein, fat = fat, carbs = carbs,
                                        barcode = barcodeInput, isCustom = true
                                    ))
                                    onProductFound(product)
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (_: Exception) {
                                    lookupError = StringKey.DIALOG_BARCODE_SAVE_ERROR
                                } finally {
                                    isLookingUp = false
                                }
                            }
                        }
                    )
                }

                Text(
                    text = appString(StringKey.DIALOG_BARCODE_MANUAL_HINT),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = barcodeInput,
                    enabled = !isLookingUp && !showCustomForm,
                    onValueChange = {
                        barcodeInput = it.filter(Char::isDigit).take(14)
                        notFound = false
                        lookupError = null
                        incompleteProductName = null
                        searchResult = null
                    },
                    label = {
                        Text(
                            appString(StringKey.DIALOG_BARCODE_INPUT_LABEL),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    placeholder = {
                        Text("4820000000000", style = MaterialTheme.typography.labelMedium)
                    },
                    trailingIcon = {
                        if (barcodeInput.length >= 8) {
                            androidx.compose.material3.IconButton(
                                onClick = { lookup(barcodeInput) },
                                enabled = !isLookingUp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = appString(StringKey.SEARCH)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("barcode_input_field"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { lookup(barcodeInput) }
                    ),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            if (searchResult != null) {
                Button(
                    onClick = {
                        searchResult?.let(onProductFound)
                    },
                    modifier = Modifier.testTag("confirm_scanned_product_button"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(appString(StringKey.ADD), fontWeight = FontWeight.Bold)
                }
            } else if (barcodeInput.length >= 8 && !showCustomForm) {
                Button(
                    onClick = { lookup(barcodeInput) },
                    enabled = !isLookingUp,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(appString(StringKey.SEARCH), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_barcode_dialog_button")
            ) {
                Text(appString(StringKey.CANCEL))
            }
        },
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun CameraMessageCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BarcodeCameraPreview(
    enabled: Boolean,
    onBarcodeDetected: (String) -> Unit,
    onCameraError: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestBarcodeCallback by rememberUpdatedState(onBarcodeDetected)
    val latestCameraError by rememberUpdatedState(onCameraError)

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val options = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_ITF
            )
            .build()
    }
    val scanner = remember { BarcodeScanning.getClient(options) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember(scanner) {
        FoodBarcodeAnalyzer(scanner) { code -> latestBarcodeCallback(code) }
    }

    LaunchedEffect(enabled) {
        analyzer.setEnabled(enabled)
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null
        var disposed = false

        cameraProviderFuture.addListener(
            {
                if (disposed) return@addListener
                try {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(cameraExecutor, analyzer) }

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                } catch (_: Throwable) {
                    latestCameraError()
                }
            },
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            disposed = true
            cameraProvider?.unbindAll()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("barcode_camera_preview")
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.82f)
                .height(92.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
        )
    }
}

@OptIn(ExperimentalGetImage::class)
private class FoodBarcodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val enabled = AtomicBoolean(true)
    private val processing = AtomicBoolean(false)

    fun setEnabled(value: Boolean) {
        enabled.set(value)
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (!enabled.get() || !processing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            processing.set(false)
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val code = barcodes
                    .asSequence()
                    .mapNotNull { it.rawValue }
                    .map(BarcodeUtils::normalize)
                    .firstOrNull { it.length >= 8 }

                if (code != null && enabled.compareAndSet(true, false)) {
                    onDetected(code)
                }
            }
            .addOnCompleteListener {
                processing.set(false)
                imageProxy.close()
            }
    }
}
