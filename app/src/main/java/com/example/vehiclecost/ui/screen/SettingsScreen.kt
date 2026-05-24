package com.example.vehiclecost.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Add
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vehiclecost.ui.viewmodel.CostViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: CostViewModel) {
    val context = LocalContext.current
    val purchaseDate by viewModel.purchaseDateFlow.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var isImporting by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = purchaseDate ?: System.currentTimeMillis()
    )

    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Copy to internal storage so we don't lose access
            val copiedUri = copyToInternalStorage(context, it)
            if (copiedUri != null) {
                viewModel.saveCarPhotoUri(copiedUri)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
            ListItem(
                headlineContent = { Text("设置提车日期") },
                supportingContent = { 
                    Text(purchaseDate?.let { dateFormat.format(Date(it)) } ?: "未设置") 
                },
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier.clickable { showDatePicker = true }
            )
            HorizontalDivider()
            
            ListItem(
                headlineContent = { Text("更换爱车照片") },
                supportingContent = { Text("用于主页顶部展示") },
                leadingContent = { Icon(Icons.Default.Face, contentDescription = null) },
                modifier = Modifier.clickable { launcher.launch("image/*") }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("一键导入历史账单") },
                supportingContent = { Text(if (isImporting) "正在导入..." else "从内置的 JSON 模板批量恢复数据") },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier.clickable { 
                    if (!isImporting) {
                        isImporting = true
                        viewModel.importLegacyData(context) { success, count ->
                            isImporting = false
                            if (success) {
                                Toast.makeText(context, "成功导入 $count 条记录！", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "导入失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
            HorizontalDivider()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { 
                        viewModel.savePurchaseDate(it)
                    }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun copyToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, "car_photo_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
