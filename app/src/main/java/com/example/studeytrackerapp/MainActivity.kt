package com.example.studeytrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.studeytrackerapp.ui.navigation.AppNavigation
import com.example.studeytrackerapp.ui.theme.StudeytrackerappTheme
import com.example.studeytrackerapp.ui.viewmodel.AddSessionViewModel
import com.example.studeytrackerapp.ui.viewmodel.HomeViewModel
import com.example.studeytrackerapp.util.AppConfig
import com.example.studeytrackerapp.util.AppModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudeytrackerappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val repository = AppModule.provideRepository(this@MainActivity)
                    val navController = rememberNavController()
                    
                    // Initialize AI Repository with Gemini API key
                    val aiRepository = if (AppConfig.GEMINI_API_KEY.isNotBlank()) {
                        AppModule.provideAIRepository(AppConfig.GEMINI_API_KEY)
                    } else {
                        null
                    }
                    
                    AppContent(
                        repository = repository,
                        navController = navController,
                        aiRepository = aiRepository
                    )
                }
            }
        }
    }
}

@Composable
fun AppContent(
    repository: com.example.studeytrackerapp.data.repository.StudyRepository,
    navController: androidx.navigation.NavHostController,
    aiRepository: com.example.studeytrackerapp.data.repository.AIRepository?
) {
    val viewModelFactory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            when {
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                    @Suppress("UNCHECKED_CAST")
                    return HomeViewModel(repository, aiRepository) as T
                }
                modelClass.isAssignableFrom(AddSessionViewModel::class.java) -> {
                    @Suppress("UNCHECKED_CAST")
                    return AddSessionViewModel(repository) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    
    val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = viewModelFactory
    )
    
    val addSessionViewModel: AddSessionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = viewModelFactory
    )
    
    AppNavigation(
        navController = navController,
        homeViewModel = homeViewModel,
        addSessionViewModel = addSessionViewModel
    )
}