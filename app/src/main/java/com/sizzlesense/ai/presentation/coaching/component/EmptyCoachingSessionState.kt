package com.sizzlesense.ai.presentation.coaching.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun EmptyCoachingSessionState(
    currentAttemptNumber: Int,
    isAnalyzing: Boolean,
    onAnalyzeCurrentAttempt: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No session analyzed yet.",
                color = SizzleTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(SizzleTheme.spacing.sm))
            if (isAnalyzing) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onAnalyzeCurrentAttempt) {
                    Text("Analyze Attempt $currentAttemptNumber Now")
                }
            }
        }
    }
}
