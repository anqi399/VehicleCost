package com.example.vehiclecost.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.vehiclecost.ui.viewmodel.CostViewModel
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(viewModel: CostViewModel) {
    val purchaseDate by viewModel.purchaseDateFlow.collectAsStateWithLifecycle()
    val carPhotoUri by viewModel.carPhotoUriFlow.collectAsStateWithLifecycle()
    val currentMonthTotal by viewModel.currentMonthTotal.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()

    val daysOwned = purchaseDate?.let { 
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it).coerceAtLeast(0) 
    } ?: 0

    Column(modifier = Modifier.fillMaxSize()) {
        // Car Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            if (carPhotoUri != null) {
                AsyncImage(
                    model = Uri.parse(carPhotoUri),
                    contentDescription = "Car Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 100f
                            )
                        )
                )
            } else {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Car",
                    modifier = Modifier.align(Alignment.Center).size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "已陪伴天数",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (carPhotoUri != null) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$daysOwned 天",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (carPhotoUri != null) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Stats Dashboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("本月预估总花销", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¥${String.format(Locale.getDefault(), "%.2f", currentMonthTotal)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (categoryBreakdown.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("各项占比", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoryBreakdown) { item ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(item.category, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "¥${String.format(Locale.getDefault(), "%.0f", item.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${(item.percentage * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                    LinearProgressIndicator(
                                        progress = { item.percentage },
                                        modifier = Modifier.width(48.dp).height(4.dp).padding(top = 2.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
