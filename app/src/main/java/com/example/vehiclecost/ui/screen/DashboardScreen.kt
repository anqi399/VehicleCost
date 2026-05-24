package com.example.vehiclecost.ui.screen

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.vehiclecost.ui.viewmodel.CostViewModel
import com.example.vehiclecost.ui.viewmodel.DashboardPeriod
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: CostViewModel) {
    val purchaseDate by viewModel.purchaseDateFlow.collectAsStateWithLifecycle()
    val carPhotoUri by viewModel.carPhotoUriFlow.collectAsStateWithLifecycle()
    
    val currentPeriod by viewModel.dashboardPeriodType.collectAsStateWithLifecycle()
    val dashboardTotal by viewModel.dashboardTotal.collectAsStateWithLifecycle()
    val dashboardTrend by viewModel.dashboardTrend.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.dashboardCategoryBreakdown.collectAsStateWithLifecycle()

    val daysOwned = purchaseDate?.let { 
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it).coerceAtLeast(0) 
    } ?: 0

    Column(modifier = Modifier.fillMaxSize()) {
        // Car Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            if (carPhotoUri != null) {
                AsyncImage(
                    model = Uri.parse(carPhotoUri),
                    contentDescription = "Car Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 100f
                            )
                        )
                )
            } else {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Car",
                    modifier = Modifier.align(Alignment.Center).size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "已陪伴",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (carPhotoUri != null) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$daysOwned 天",
                    fontSize = 28.sp,
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
                // Segmented Control
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = currentPeriod == DashboardPeriod.WEEK,
                        onClick = { viewModel.setDashboardPeriod(DashboardPeriod.WEEK) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
                    ) { Text("本周") }
                    SegmentedButton(
                        selected = currentPeriod == DashboardPeriod.MONTH,
                        onClick = { viewModel.setDashboardPeriod(DashboardPeriod.MONTH) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
                    ) { Text("本月") }
                    SegmentedButton(
                        selected = currentPeriod == DashboardPeriod.YEAR,
                        onClick = { viewModel.setDashboardPeriod(DashboardPeriod.YEAR) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
                    ) { Text("本年") }
                    SegmentedButton(
                        selected = currentPeriod == DashboardPeriod.ALL,
                        onClick = { viewModel.setDashboardPeriod(DashboardPeriod.ALL) },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
                    ) { Text("全部") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total and Trend
                Text("总花销", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "¥${String.format(Locale.getDefault(), "%.2f", dashboardTotal)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    if (dashboardTrend.hasHistory) {
                        val trendColor = if (dashboardTrend.isPositive) Color(0xFFD32F2F) else Color(0xFF388E3C)
                        val trendSign = if (dashboardTrend.isPositive) "+" else ""
                        val arrow = if (dashboardTrend.isPositive) "↑" else "↓"
                        Text(
                            text = "环比 $trendSign¥${String.format(Locale.getDefault(), "%.0f", dashboardTrend.diff)} $arrow",
                            color = trendColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    } else {
                        Text(
                            text = "无历史对比",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pie Chart & Top 3 Categories
                if (categoryBreakdown.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pie Chart
                        val colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.error,
                            Color(0xFFFFB300),
                            Color(0xFF00ACC1)
                        )
                        
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                var startAngle = -90f
                                categoryBreakdown.forEachIndexed { index, item ->
                                    val sweepAngle = item.percentage * 360f
                                    val color = colors[index % colors.size]
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt),
                                        size = Size(size.width, size.height)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Categories List
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            categoryBreakdown.forEachIndexed { index, item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(colors[index % colors.size])
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(item.category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Row {
                                            Text(
                                                "¥${String.format(Locale.getDefault(), "%.0f", item.amount)}", 
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "${(item.percentage * 100).toInt()}%", 
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("当前周期暂无花销", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
