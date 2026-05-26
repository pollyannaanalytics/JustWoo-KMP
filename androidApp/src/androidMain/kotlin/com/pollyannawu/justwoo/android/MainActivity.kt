package com.pollyannawu.justwoo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import com.pollyannawu.justwoo.android.ui.MainViewModel
import com.pollyannawu.justwoo.android.ui.nav.RootContent
import com.pollyannawu.justwoo.ui.nav.DefaultRootComponent
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            initiallyAuthenticated = mainViewModel.isAuthenticated.value,
            authStartProvider = mainViewModel::resolveAuthStart,
        )

        setContent {
            val userId by mainViewModel.currentUserId.collectAsState()
            val houseId by mainViewModel.currentHouseId.collectAsState()
            val isAuthenticated by mainViewModel.isAuthenticated.collectAsState()

            LaunchedEffect(isAuthenticated) {
                root.onSessionChanged(isAuthenticated)
            }

            JustWooTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JustWooColors.Cream),
                    color = JustWooColors.Cream,
                ) {
                    RootContent(
                        component = root,
                        currentUserId = userId ?: 0L,
                        currentHouseId = houseId ?: 0L,
                    )
                }
            }
        }
    }
}
