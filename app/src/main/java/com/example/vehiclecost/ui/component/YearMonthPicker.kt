package com.example.vehiclecost.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearMonthPicker(
    initialSelection: String, // "YYYY-MM" or "YYYY"
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // Parse initial
    var currentYear by remember {
        mutableStateOf(
            if (initialSelection.length >= 4) initialSelection.substring(0, 4).toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            else Calendar.getInstance().get(Calendar.YEAR)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Year Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { currentYear -= 1 }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Year")
                    }
                    Text("$currentYear 年", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { currentYear += 1 }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Months Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(160.dp)
                ) {
                    val months = (1..12).toList()
                    items(months) { month ->
                        val isSelected = initialSelection == String.format("%04d-%02d", currentYear, month)
                        if (isSelected) {
                            Button(
                                onClick = { onConfirm(String.format("%04d-%02d", currentYear, month)) },
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("${month}月") }
                        } else {
                            TextButton(
                                onClick = { onConfirm(String.format("%04d-%02d", currentYear, month)) },
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("${month}月", color = MaterialTheme.colorScheme.onSurface) }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // All Year Button
                OutlinedButton(
                    onClick = { onConfirm("$currentYear") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (initialSelection == "$currentYear") {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text("$currentYear 年全年")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
