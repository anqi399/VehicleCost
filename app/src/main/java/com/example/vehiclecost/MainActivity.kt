package com.example.vehiclecost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vehiclecost.data.AppDatabase
import com.example.vehiclecost.ui.screen.HomeScreen
import com.example.vehiclecost.data.repository.SettingsRepository
import com.example.vehiclecost.ui.theme.VehicleCostTheme
import com.example.vehiclecost.ui.viewmodel.CostViewModel
import com.example.vehiclecost.ui.viewmodel.CostViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Database and Settings
        val database = AppDatabase.getDatabase(applicationContext)
        val settingsRepository = SettingsRepository(applicationContext)
        val factory = CostViewModelFactory(database.costDao(), settingsRepository)

        enableEdgeToEdge()
        setContent {
            VehicleCostTheme {
                val viewModel: CostViewModel = viewModel(factory = factory)
                MainApp(viewModel = viewModel)
            }
        }
    }
}