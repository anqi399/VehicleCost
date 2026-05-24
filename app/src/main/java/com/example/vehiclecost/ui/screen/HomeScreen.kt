package com.example.vehiclecost.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vehiclecost.data.entity.VehicleCost
import com.example.vehiclecost.ui.component.AddCostDialog
import com.example.vehiclecost.ui.component.YearMonthPicker
import com.example.vehiclecost.ui.viewmodel.CostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: CostViewModel) {
    val currentMonthCosts by viewModel.currentMonthCosts.collectAsStateWithLifecycle()
    val currentMonthTotal by viewModel.currentMonthTotal.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var editingCost by remember { mutableStateOf<VehicleCost?>(null) }
    var costToDelete by remember { mutableStateOf<VehicleCost?>(null) }

    val displayMonth = if (selectedMonth.length == 4) {
        "${selectedMonth}年 全年"
    } else {
        "${selectedMonth.substring(0, 4)}年${selectedMonth.substring(5, 7)}月"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("03成长记") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "记一笔")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Dashboard Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Month Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showMonthPicker = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = displayMonth,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Month")
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (selectedMonth.length == 4) "年度总花销 (元)" else "本月总花销 (元)", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f", currentMonthTotal),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Category Breakdown Overview
                    if (categoryBreakdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
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

            // List of costs
            Text(
                text = "历史记录 (向左滑动删除)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (currentMonthCosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("该时间段暂无记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp) // padding for FAB
                ) {
                    items(
                        items = currentMonthCosts,
                        key = { it.id }
                    ) { cost ->
                        CostItem(
                            cost = cost,
                            onEdit = { editingCost = cost },
                            onDelete = { costToDelete = cost }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog || editingCost != null) {
        AddCostDialog(
            initialCost = editingCost,
            onDismiss = { 
                showAddDialog = false
                editingCost = null
            },
            onConfirm = { amount, category, date, note ->
                if (editingCost != null) {
                    val updatedCost = editingCost!!.copy(
                        amount = amount,
                        category = category,
                        date = date,
                        note = note
                    )
                    viewModel.updateCost(updatedCost)
                } else {
                    viewModel.addCost(amount, category, date, note)
                }
                showAddDialog = false
                editingCost = null
            }
        )
    }

    if (showMonthPicker) {
        YearMonthPicker(
            initialSelection = selectedMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = { 
                viewModel.changeMonth(it)
                showMonthPicker = false
            }
        )
    }

    if (costToDelete != null) {
        AlertDialog(
            onDismissRequest = { costToDelete = null },
            title = { Text("确认删除") },
            text = { Text("您确定要删除这笔记录吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCost(costToDelete!!)
                        costToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { costToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostItem(cost: VehicleCost, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                // ALWAYS return false to snap back, but trigger the delete confirmation
                onDelete()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.surface
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) MaterialTheme.colorScheme.onError else Color.Transparent
                )
            }
        }
    ) {
        ListItem(
            modifier = Modifier.clickable { onEdit() },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            headlineContent = { Text(cost.category, fontWeight = FontWeight.Bold) },
            supportingContent = { 
                Column {
                    Text(dateFormat.format(Date(cost.date)))
                    if (cost.note.isNotBlank()) {
                        Text(cost.note, maxLines = 1)
                    }
                }
            },
            trailingContent = { 
                Text(
                    text = String.format(Locale.getDefault(), "-%.2f", cost.amount),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ) 
            }
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
