package com.pollyannawu.justwoo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pollyannawu.justwoo.android.ui.nav.JustWooNavHost
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JustWooTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JustWooColors.Cream),
                    color = JustWooColors.Cream,
                ) {
                    JustWooNavHost()
                }
            }
        }
    }
}
