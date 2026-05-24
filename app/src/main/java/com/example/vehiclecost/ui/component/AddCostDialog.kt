package com.example.vehiclecost.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.vehiclecost.data.entity.VehicleCost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCostDialog(
    initialCost: VehicleCost? = null,
    selectedMonthPattern: String = "", // e.g. "2026-05" or "2026"
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, dateMillis: Long, note: String, tag: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(initialCost?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(initialCost?.note ?: "") }
    
    val categories = listOf("加油", "停车", "洗车", "充电", "保养", "保险", "违章", "车用品", "其他")
    var selectedCategory by remember { mutableStateOf(initialCost?.category ?: categories[0]) }

    val parkingTags = listOf("固定月租", "临时停车")
    var selectedTag by remember { mutableStateOf(initialCost?.tag ?: "") }
    
    // Auto clear tag if not parking, but keep it if parking.
    LaunchedEffect(selectedCategory) {
        if (selectedCategory == "停车" && selectedTag.isEmpty()) {
            selectedTag = parkingTags[0]
        } else if (selectedCategory != "停车") {
            selectedTag = ""
        }
    }

    var dateMillis by remember { 
        mutableStateOf(
            initialCost?.date ?: run {
                if (selectedMonthPattern.length == 7) {
                    val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    val parsedDate = format.parse(selectedMonthPattern)
                    if (parsedDate != null) {
                        val currentCal = Calendar.getInstance()
                        val selectedCal = Calendar.getInstance().apply { time = parsedDate }
                        if (currentCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                            currentCal.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH)) {
                            System.currentTimeMillis() // It's current month, use today
                        } else {
                            // It's a past/future month, default to 1st of that month
                            selectedCal.timeInMillis
                        }
                    } else {
                        System.currentTimeMillis()
                    }
                } else {
                    System.currentTimeMillis()
                }
            }
        ) 
    }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    
    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCost == null) "记录花费" else "编辑花费") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("金额 (必填)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true
                )

                Text("分类", style = MaterialTheme.typography.titleSmall)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(140.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (selectedCategory == "停车") {
                    Text("停车标签", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        parkingTags.forEach { tag ->
                            FilterChip(
                                selected = tag == selectedTag,
                                onClick = { selectedTag = tag },
                                label = { Text(tag) }
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("日期: ${dateFormat.format(Date(dateMillis))}")
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfirm(amount, selectedCategory, dateMillis, note, selectedTag)
                    }
                },
                enabled = amountStr.toDoubleOrNull() != null && amountStr.toDoubleOrNull()!! > 0
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
