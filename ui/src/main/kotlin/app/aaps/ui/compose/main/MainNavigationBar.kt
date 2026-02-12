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

sealed class MainNavDestination(val id: String, val iconRes: Int, val labelRes: Int) {
    data object Overview : MainNavDestination(
        id = "overview",
        iconRes = CoreUiR.drawable.ic_home,
        labelRes = CoreUiR.string.overview
    )

    data object Manage : MainNavDestination(
        id = "manage",
        iconRes = app.aaps.core.objects.R.drawable.ic_action,
        labelRes = CoreUiR.string.manage
    )
}

@Composable
fun MainNavigationBar(
    currentDestination: MainNavDestination,
    onDestinationSelected: (MainNavDestination) -> Unit,
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
        // Overview tab
        NavigationBarItem(
            selected = currentDestination == MainNavDestination.Overview,
            onClick = { onDestinationSelected(MainNavDestination.Overview) },
            icon = {
                Icon(
                    painter = painterResource(id = MainNavDestination.Overview.iconRes),
                    contentDescription = stringResource(MainNavDestination.Overview.labelRes)
                )
            },
            label = { Text(text = stringResource(MainNavDestination.Overview.labelRes)) },
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

        // Manage tab
        NavigationBarItem(
            selected = currentDestination == MainNavDestination.Manage,
            onClick = { onDestinationSelected(MainNavDestination.Manage) },
            icon = {
                Icon(
                    painter = painterResource(id = MainNavDestination.Manage.iconRes),
                    contentDescription = stringResource(MainNavDestination.Manage.labelRes)
                )
            },
            label = { Text(text = stringResource(MainNavDestination.Manage.labelRes)) },
            colors = navColors
        )
    }
}
