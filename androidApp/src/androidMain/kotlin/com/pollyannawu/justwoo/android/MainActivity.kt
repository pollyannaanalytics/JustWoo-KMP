package com.pollyannawu.justwoo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import com.pollyannawu.justwoo.android.ui.nav.RootContent
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.nav.DefaultRootComponent
import com.pollyannawu.justwoo.session.SessionState
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val session: SessionState by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            session = session,
        )

        setContent {
            JustWooTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JustWooColors.Cream),
                    color = JustWooColors.Cream,
                ) {
                    RootContent(root)
                }
            }
        }
    }
}
