package com.sizzlesense.ai.presentation.main.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.sizzlesense.ai.presentation.navigation.SizzleDestination
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun SizzleBottomNavigationBar(
    activeDestination: SizzleDestination,
    onDestinationSelected: (SizzleDestination) -> Unit
) {
    Column {
        HorizontalDivider(
            thickness = SizzleTheme.spacing.borderStroke,
            color = SizzleTheme.colors.outlineSubtle
        )
        NavigationBar(
            containerColor = SizzleTheme.colors.surfacePrimary
        ) {
            SizzleDestination.entries.forEach { destination ->
                NavigationBarItem(
                    selected = activeDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.toIcon(),
                            contentDescription = destination.label
                        )
                    },
                    label = {
                        Text(
                            text = destination.label,
                            maxLines = 1
                        )
                    }
                )
            }
        }
    }
}

private fun SizzleDestination.toIcon(): ImageVector = when (this) {
    SizzleDestination.KITCHEN -> Icons.Default.Videocam
    SizzleDestination.COACHING -> Icons.Default.School
    SizzleDestination.XRAY -> Icons.Default.Psychology
}
