package com.example.vehiclecost.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    val reminderEnabled by viewModel.reminderEnabledFlow.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var isImporting by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = purchaseDate ?: System.currentTimeMillis()
    )

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
        } else {
            Toast.makeText(context, "请在系统设置中允许通知权限", Toast.LENGTH_LONG).show()
        }
    }

    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val copiedUri = copyToInternalStorage(context, it)
            if (copiedUri != null) {
                viewModel.saveCarPhotoUri(copiedUri)
            }
        }
    }

    // Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportDataToUri(context, it) { success ->
                if (success) {
                    Toast.makeText(context, "✅ 备份导出成功！", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "❌ 导出失败", Toast.LENGTH_SHORT).show()
                }
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
                headlineContent = { Text("每日记账提醒") },
                supportingContent = {
                    Text(if (reminderEnabled) "每晚提醒记账" else "关闭")
                },
                leadingContent = {
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@Switch
                                }
                            }
                            viewModel.setReminderEnabled(enabled)
                        }
                    )
                }
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
                        showImportConfirmDialog = true
                    }
                }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("备份与导出") },
                supportingContent = { Text("将所有账单导出为 JSON 文件以便备份") },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                modifier = Modifier.clickable { 
                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                    exportLauncher.launch("vehicle_cost_export_$dateStr.json") 
                }
            )
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

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text("确认导入历史账单") },
            text = { Text("当前导入为追加操作。如果您之前已经成功导入过该模板数据，再次点击会导致账单重复。您确定要执行导入吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirmDialog = false
                        isImporting = true
                        viewModel.importLegacyData(context) { success, count ->
                            isImporting = false
                            if (success) {
                                if (count > 0) {
                                    Toast.makeText(context, "成功追加导入 $count 条记录！", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "模板数据均已存在，无新记录导入", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "导入失败，请检查文件格式", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("执行导入")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
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
