package com.sizzlesense.ai.presentation.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.presentation.theme.SizzleTheme

/**
 * Screen container that pins [DismissibleErrorBanner] above the vertically scrollable body
 * so errors remain immediately visible regardless of the user's scroll position.
 */
@Composable
fun PinnedErrorScrollColumn(
    errorMessage: String?,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = SizzleTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.md)
    ) {
        errorMessage?.let { err ->
            DismissibleErrorBanner(
                errorMessage = err,
                onDismiss = onDismissError
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.md),
            content = content
        )
    }
}
