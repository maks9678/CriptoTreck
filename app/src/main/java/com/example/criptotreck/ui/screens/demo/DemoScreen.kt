package com.example.criptotreck.ui.screens.demo

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.criptotreck.ui.screens.details.CryptoDetailActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Навигация и Демо Activity", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Выберите подход/Activity для демонстрации:",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                DemoActivityCard(
                    title = "Approach 1: Compose Activity",
                    description = "Полноценное Activity, написанное полностью на Jetpack Compose.",
                    buttonText = "Открыть ComposeActivity",
                    onClick = {
                        context.startActivity(Intent(context, ComposeActivity::class.java))
                    }
                )
            }

            item {
                DemoActivityCard(
                    title = "Approach 3: Traditional XML Activity",
                    description = "Классический подход с использованием XML-разметки (activity_xml.xml).",
                    buttonText = "Открыть XmlActivity",
                    onClick = {
                        context.startActivity(Intent(context, XmlActivity::class.java))
                    }
                )
            }

            item {
                DemoActivityCard(
                    title = "Approach 4: Hybrid Activity (XML + Compose)",
                    description = "Гибридный подход: XML layout с встроенным ComposeView.",
                    buttonText = "Открыть HybridActivity",
                    onClick = {
                        context.startActivity(Intent(context, HybridActivity::class.java))
                    }
                )
            }

            item {
                DemoActivityCard(
                    title = "Feature Activity: CryptoDetailActivity",
                    description = "Отдельное Activity с передачей параметров (EXTRA_ASSET_ID = \"1\") через Intent.",
                    buttonText = "Открыть CryptoDetailActivity (Bitcoin)",
                    onClick = {
                        val intent = Intent(context, CryptoDetailActivity::class.java).apply {
                            putExtra("EXTRA_ASSET_ID", "1")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun DemoActivityCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = buttonText)
            }
        }
    }
}
