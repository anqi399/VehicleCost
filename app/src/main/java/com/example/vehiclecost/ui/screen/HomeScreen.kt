package com.example.vehiclecost.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: CostViewModel) {
    val currentMonthCosts by viewModel.currentMonthCosts.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

    var showMonthPicker by remember { mutableStateOf(false) }
    var editingCost by remember { mutableStateOf<VehicleCost?>(null) }
    var costToDelete by remember { mutableStateOf<VehicleCost?>(null) }

    val displayMonth = if (selectedMonth.length == 4) {
        "${selectedMonth}年 全年"
    } else {
        "${selectedMonth.substring(0, 4)}年${selectedMonth.substring(5, 7)}月"
    }

    val filterCategories = listOf("全部", "加油", "停车", "固定停车", "临时停车", "洗车", "车用品", "保险", "保养", "违章", "其他")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showMonthPicker = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(displayMonth, style = MaterialTheme.typography.titleLarge)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Month")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterCategories) { category ->
                    FilterChip(
                        selected = category == selectedCategoryFilter,
                        onClick = { viewModel.setCategoryFilter(category) },
                        label = { Text(category) }
                    )
                }
            }

            if (currentMonthCosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("该时间段暂无记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
                val grouped = currentMonthCosts.groupBy { dateFormat.format(Date(it.date)) }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp) // padding for FAB
                ) {
                    grouped.forEach { (dateStr, costs) ->
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = dateStr,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        items(
                            items = costs,
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
    }

    // Dialogs
    if (editingCost != null) {
        AddCostDialog(
            initialCost = editingCost,
            selectedMonthPattern = selectedMonth,
            onDismiss = { editingCost = null },
            onConfirm = { amount, category, date, note, tag ->
                val updatedCost = editingCost!!.copy(
                    amount = amount,
                    category = category,
                    date = date,
                    note = note,
                    tag = tag
                )
                viewModel.updateCost(updatedCost)
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
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
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
            headlineContent = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(cost.category, fontWeight = FontWeight.Bold)
                    if (cost.tag.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = cost.tag,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            },
            supportingContent = if (cost.note.isNotBlank()) {
                {
                    Text(
                        text = cost.note, 
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else null,
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
