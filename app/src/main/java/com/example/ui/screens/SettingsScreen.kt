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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import android.app.Activity
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val activity = context as? Activity

    if (showSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showSubscriptionDialog = false },
            title = { Text("Premium Subscription", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a plan:")
                    Button(onClick = { 
                        showSubscriptionDialog = false 
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Monthly - $4.99")
                    }
                    Button(onClick = { 
                        showSubscriptionDialog = false 
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("6 Months - $24.99")
                    }
                    Button(onClick = { 
                        showSubscriptionDialog = false 
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Yearly - $39.99")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSubscriptionDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = { showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Blue900,
                    letterSpacing = (-0.5).sp
                )
            }

            val settingsItems = listOf(
                Pair("Premium Subscription", Icons.Default.WorkspacePremium),
                Pair("Privacy Policy", Icons.Default.PrivacyTip),
                Pair("Terms & Conditions", Icons.Default.Gavel),
                Pair("About", Icons.Default.Info),
                Pair("Contact Us", Icons.Default.Email),
                Pair("Delete Account", Icons.Default.DeleteForever)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(settingsItems) { (title, icon) ->
                    val isDestructive = title == "Delete Account"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when(title) {
                                    "Premium Subscription" -> showSubscriptionDialog = true
                                    "Delete Account" -> showDeleteDialog = true
                                    // other links could open intents
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDestructive) Color.Red.copy(alpha = 0.2f) else Slate100),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(if (isDestructive) Color.Red.copy(alpha = 0.1f) else Blue50, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = title, tint = if (isDestructive) Color.Red else Blue500, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDestructive) Color.Red else Slate800)
                        }
                    }
                }
            }
        }
    }
}
