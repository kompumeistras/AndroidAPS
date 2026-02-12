package app.aaps.ui.compose.main

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aaps.core.ui.compose.icons.Treatment
import app.aaps.core.ui.R as CoreUiR

@Composable
fun MainNavigationBar(
    onManageClick: () -> Unit,
    onTreatmentClick: () -> Unit,
    quickWizardCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val navColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onSurface,
        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        // Overview tab (always selected)
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = {
                Icon(
                    painter = painterResource(id = CoreUiR.drawable.ic_home),
                    contentDescription = stringResource(CoreUiR.string.overview)
                )
            },
            label = { Text(text = stringResource(CoreUiR.string.overview)) },
            colors = navColors
        )

        // Treatment action button (opens bottom sheet)
        NavigationBarItem(
            selected = false,
            onClick = onTreatmentClick,
            icon = {
                BadgedBox(
                    badge = {
                        if (quickWizardCount > 0) {
                            Badge { Text(text = quickWizardCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Treatment,
                        contentDescription = stringResource(CoreUiR.string.treatments),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text(text = stringResource(CoreUiR.string.treatments)) },
            colors = navColors
        )

        // Manage action button (opens bottom sheet)
        NavigationBarItem(
            selected = false,
            onClick = onManageClick,
            icon = {
                Icon(
                    painter = painterResource(id = app.aaps.core.objects.R.drawable.ic_action),
                    contentDescription = stringResource(CoreUiR.string.manage)
                )
            },
            label = { Text(text = stringResource(CoreUiR.string.manage)) },
            colors = navColors
        )
    }
}
