package com.sizzlesense.ai

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sizzlesense.ai.presentation.main.SizzleMainScreen
import com.sizzlesense.ai.presentation.navigation.EvalBenchEntry
import com.sizzlesense.ai.presentation.theme.SizzleSenseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var evalBenchEntry: EvalBenchEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            SizzleSenseTheme {
                SizzleMainScreen(
                    evalBenchContent = { evalBenchEntry.Content() }
                )
            }
        }
    }
}
