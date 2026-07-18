package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.api.RetrofitClient
import com.example.api.GenerateContentRequest
import com.example.api.Content
import com.example.api.Part
import com.example.data.AppRepository
import com.example.data.AINote
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIToolsScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    var selectedTool by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Sleek Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        if (selectedTool != null) selectedTool = null else onBack() 
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    selectedTool ?: "AI Tools",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Blue900,
                    letterSpacing = (-0.5).sp
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (selectedTool == null) {
                    AIToolSelector(onToolSelected = { selectedTool = it })
                } else {
                    AIToolProcessor(toolName = selectedTool!!, repository = repository)
                }
            }
        }
    }
}

@Composable
fun AIToolSelector(onToolSelected: (String) -> Unit) {
    val tools = listOf(
        "Extract Text (OCR)" to Icons.Default.DocumentScanner,
        "Scan QR Code" to Icons.Default.QrCodeScanner,
        "Generate QR Code" to Icons.Default.QrCode,
        "Document Summary" to Icons.Default.Summarize,
        "AI Translator" to Icons.Default.Translate,
        "Grammar Fix" to Icons.Default.Spellcheck,
        "AI Rewrite" to Icons.Default.Edit,
        "Resume Review" to Icons.Default.Work
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tools) { (name, icon) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToolSelected(name) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Orange50, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = name, tint = Orange500, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate800)
                }
            }
        }
    }
}

@Composable
fun AIToolProcessor(toolName: String, repository: AppRepository) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isSaved by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isLoading = true
            try {
                val image = com.google.mlkit.vision.common.InputImage.fromFilePath(context, it)
                if (toolName == "Extract Text (OCR)") {
                    val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            inputText = visionText.text
                            isLoading = false
                        }
                        .addOnFailureListener { e ->
                            outputText = "OCR Failed: ${e.message}"
                            isLoading = false
                        }
                } else if (toolName == "Scan QR Code") {
                    val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                outputText = "QR Code Value:\n" + barcodes.joinToString("\n") { barcode -> barcode.rawValue ?: "" }
                            } else {
                                outputText = "No QR code found."
                            }
                            isLoading = false
                        }
                        .addOnFailureListener { e ->
                            outputText = "Scan Failed: ${e.message}"
                            isLoading = false
                        }
                }
            } catch (e: Exception) {
                outputText = "Error processing image: ${e.message}"
                isLoading = false
            }
        }
    }
    
    val systemInstruction = when(toolName) {
        "Document Summary" -> "You are an expert summarizer. Provide a concise summary of the text."
        "AI Translator" -> "You are an expert translator. Translate the given text to English if it is not, or to Spanish if it is English."
        "Grammar Fix" -> "Fix all grammar and spelling errors in the text."
        "AI Rewrite" -> "Rewrite the text to be more professional and clear."
        "Resume Review" -> "Review the provided resume text and provide constructive feedback to improve it."
        else -> "You are a helpful assistant."
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        if (toolName == "Generate QR Code") {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter text to generate QR", color = Slate500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = Slate300,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    outputText = "QR Code generation requires a custom view, but text is: $inputText\n(Feature placeholder for production)" 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = inputText.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                Text("Generate QR Code", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        } else {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text(if (toolName == "Extract Text (OCR)") "Extracted Text / Input" else "Input Text", color = Slate500) },
                modifier = Modifier.fillMaxWidth().weight(1f),
                maxLines = 10,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = Slate300,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (toolName == "Extract Text (OCR)" || toolName == "Scan QR Code") {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                ) {
                    Text("Select Image from Gallery", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (toolName != "Scan QR Code" && toolName != "Extract Text (OCR)") {
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            isLoading = true
                            outputText = ""
                            isSaved = false
                            coroutineScope.launch {
                                try {
                                    val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                                    val request = GenerateContentRequest(
                                        contents = listOf(Content(parts = listOf(Part(text = inputText)))),
                                        systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
                                    )
                                    val response = RetrofitClient.service.generateContent(apiKey, request)
                                    outputText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response generated."
                                } catch (e: Exception) {
                                    outputText = "Error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading && inputText.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Process with AI", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (outputText.isNotBlank()) {
            Text("Result:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate800)
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp, bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item { Text(outputText, color = Slate800) }
                }
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        repository.insertNote(AINote(title = toolName, content = outputText))
                        isSaved = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                enabled = !isSaved,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isSaved) Slate300 else Indigo500)
            ) {
                Text(if (isSaved) "Saved to Notes" else "Save Note", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
