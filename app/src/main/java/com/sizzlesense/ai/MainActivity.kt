package com.sizzlesense.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sizzlesense.ai.presentation.main.SizzleMainScreen
import com.sizzlesense.ai.presentation.theme.SizzleSenseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SizzleSenseTheme {
                SizzleMainScreen()
            }
        }
    }
}
