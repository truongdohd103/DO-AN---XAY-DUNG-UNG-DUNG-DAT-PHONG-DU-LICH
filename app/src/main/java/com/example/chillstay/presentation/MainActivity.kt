package com.example.chillstay.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.rememberNavController
import com.example.chillstay.data.repository.InMemorySampleRepository
import com.example.chillstay.domain.usecase.GetSampleItems
import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val repo = InMemorySampleRepository()
                val vm = HomeViewModel(GetSampleItems(repo))
                val navController = rememberNavController()
                AppNavHost(navController = navController, homeViewModel = vm)
            }
        }
    }
}

class MainViewModel : ViewModel()

@Composable
fun MainScreen() {}
