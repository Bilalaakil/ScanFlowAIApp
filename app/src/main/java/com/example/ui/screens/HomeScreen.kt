package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Document
import com.example.data.AppRepository
import com.example.ui.theme.*
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: AppRepository,
    onNavigateToAITools: () -> Unit,
    onNavigateToPdfTools: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val documents by repository.allDocuments.collectAsState(initial = emptyList())

    val scannerOptions = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(10)
        .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
        .setScannerMode(SCANNER_MODE_FULL)
        .build()

    val scanner = GmsDocumentScanning.getClient(scannerOptions)

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pdf?.uri?.let { pdfUri ->
                // Copy to local app storage and save to DB
                coroutineScope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(pdfUri)
                        val fileName = "Scan_${System.currentTimeMillis()}.pdf"
                        val file = File(context.filesDir, fileName)
                        val outputStream = FileOutputStream(file)
                        inputStream?.copyTo(outputStream)
                        inputStream?.close()
                        outputStream.close()
                        
                        repository.insertDocument(
                            Document(name = fileName, filePath = file.absolutePath)
                        )
                        Toast.makeText(context, "Document Saved", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error saving document", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationSleek(
                currentRoute = "home",
                onNavigateHome = { },
                onNavigateOCR = onNavigateToAITools,
                onNavigateSettings = onNavigateToSettings
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "ScanFlow AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Blue900,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "Intelligent Document Hub".uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Blue600.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Blue50, CircleShape)
                            .clickable {
                                Toast.makeText(context, "Search clicked", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate600, modifier = Modifier.size(20.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Blue100, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue700)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Primary Action: Scan
                item {
                    val startScan = {
                        var currentContext = context
                        while (currentContext is android.content.ContextWrapper) {
                            if (currentContext is Activity) break
                            currentContext = currentContext.baseContext
                        }
                        val activity = currentContext as? Activity
                        
                        activity?.let { act ->
                            scanner.getStartScanIntent(act)
                                .addOnSuccessListener { intentSender ->
                                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Scanner failed to start", Toast.LENGTH_SHORT).show()
                                }
                        }
                        Unit
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = startScan)
                    ) {
                        // Blurred shadow underlay
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(top = 8.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Blue600, Indigo500)
                                    ),
                                    alpha = 0.3f,
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .blur(16.dp)
                        )
                        // Button Surface
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Blue600, Indigo500)
                                    ),
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .padding(vertical = 28.dp), // Increased padding
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Document", tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Scan Document", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Auto-edge detection active", color = Blue100.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Feature Grid
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ToolCardSleek(
                            title = "AI Tools",
                            subtitle = "Summary & Rewrite",
                            icon = Icons.Default.AutoAwesome,
                            iconBgColor = Orange50,
                            iconColor = Orange500,
                            onClick = onNavigateToAITools,
                            modifier = Modifier.weight(1f)
                        )
                        ToolCardSleek(
                            title = "PDF Tools",
                            subtitle = "Merge & Compress",
                            icon = Icons.Default.PictureAsPdf,
                            iconBgColor = Blue50,
                            iconColor = Blue500,
                            onClick = onNavigateToPdfTools,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Recent Documents Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Files".uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue600,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "View All clicked", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Documents List
                if (documents.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                            Text("No documents yet. Tap Scan Document to start.", color = Slate400, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    items(documents) { doc ->
                        DocumentItemSleek(doc = doc, onDelete = {
                            coroutineScope.launch {
                                repository.deleteDocumentById(doc.id)
                                File(doc.filePath).delete()
                            }
                        })
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Pro Banner
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSettings() }
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Slate900, Blue900)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Orange500, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PREMIUM PLAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Orange500, letterSpacing = 1.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Unlimited AI Processing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Button(
                            onClick = onNavigateToSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Upgrade", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Ad Banner
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        BannerAd()
                    }
                }
            }
        }
    }
}

@Composable
fun BannerAd() {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(Color.Gray), contentAlignment = Alignment.Center) {
            Text("Ad Banner Placeholder", color = Color.White)
        }
    } else {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                var currentContext = context
                while (currentContext is android.content.ContextWrapper) {
                    if (currentContext is Activity) break
                    currentContext = currentContext.baseContext
                }
                val activityContext = currentContext as? Activity ?: context
                AdView(activityContext).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test Banner Ad Unit ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun ToolCardSleek(title: String, subtitle: String, icon: ImageVector, iconBgColor: Color, iconColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick).height(128.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBgColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate800)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = Slate500)
        }
    }
}

@Composable
fun DocumentItemSleek(doc: Document, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Blue50, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue600)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(doc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate800, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Saved locally", fontSize = 12.sp, color = Slate500)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Slate400)
            }
        }
    }
}

@Composable
fun BottomNavigationSleek(
    currentRoute: String,
    onNavigateHome: () -> Unit,
    onNavigateOCR: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(icon = Icons.Default.Home, label = "Home", isSelected = currentRoute == "home", onClick = onNavigateHome)
            BottomNavItem(icon = Icons.Default.Folder, label = "Files", isSelected = currentRoute == "files", onClick = {
                Toast.makeText(context, "Files clicked", Toast.LENGTH_SHORT).show()
            })
            BottomNavItem(icon = Icons.Default.DocumentScanner, label = "OCR", isSelected = currentRoute == "ocr", onClick = onNavigateOCR)
            BottomNavItem(icon = Icons.Default.Settings, label = "Settings", isSelected = currentRoute == "settings", onClick = onNavigateSettings)
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Blue100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = Blue600, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Blue600)
        } else {
            Icon(icon, contentDescription = label, tint = Slate400, modifier = Modifier.size(24.dp).padding(bottom = 4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Slate400)
        }
    }
}
