package app.aaps.ui.compose.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.aaps.core.data.plugin.PluginType
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.ui.compose.AapsFab
import app.aaps.ui.compose.actions.ActionsScreen
import app.aaps.ui.compose.actions.viewmodels.ActionsViewModel
import app.aaps.ui.compose.alertDialogs.AboutAlertDialog
import app.aaps.ui.compose.alertDialogs.AboutDialogData
import app.aaps.ui.compose.graphs.viewmodels.GraphViewModel
import app.aaps.ui.compose.overview.OverviewScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    versionName: String,
    appIcon: Int,
    aboutDialogData: AboutDialogData?,
    actionsViewModel: ActionsViewModel,
    onMenuClick: () -> Unit,
    onProfileManagementClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onMenuItemClick: (MainMenuItem) -> Unit,
    onCategoryClick: (DrawerCategory) -> Unit,
    onCategoryExpand: (DrawerCategory) -> Unit,
    onCategorySheetDismiss: () -> Unit,
    onPluginClick: (PluginBase) -> Unit,
    onPluginEnableToggle: (PluginBase, PluginType, Boolean) -> Unit,
    onPluginPreferencesClick: (PluginBase) -> Unit,
    onDrawerClosed: () -> Unit,
    onNavDestinationSelected: (MainNavDestination) -> Unit,
    onSwitchToClassicUi: () -> Unit,
    onAboutDialogDismiss: () -> Unit,
    // Overview status callbacks
    onSensorInsertClick: () -> Unit,
    onFillClick: () -> Unit,
    onInsulinChangeClick: () -> Unit,
    onBatteryChangeClick: () -> Unit,
    // Actions callbacks
    onRunningModeClick: () -> Unit,
    onTempTargetClick: () -> Unit,
    onTempBasalClick: () -> Unit,
    onExtendedBolusClick: () -> Unit,
    onHistoryBrowserClick: () -> Unit,
    onTddStatsClick: () -> Unit,
    onBgCheckClick: () -> Unit,
    onNoteClick: () -> Unit,
    onExerciseClick: () -> Unit,
    onQuestionClick: () -> Unit,
    onAnnouncementClick: () -> Unit,
    onSiteRotationClick: () -> Unit,
    onCarbsClick: () -> Unit,
    onInsulinClick: () -> Unit,
    onTreatmentClick: () -> Unit,
    onCgmClick: (() -> Unit)?,
    onCalibrationClick: (() -> Unit)?,
    showCgmButton: Boolean,
    showCalibrationButton: Boolean,
    isDexcomSource: Boolean,
    onActionsError: (String, String) -> Unit,
    graphViewModel: GraphViewModel,
    preferences: app.aaps.core.keys.interfaces.Preferences,
    config: app.aaps.core.interfaces.configuration.Config,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showTreatmentSheet by remember { mutableStateOf(false) }

    // Sync drawer state with ui state
    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed && uiState.isDrawerOpen) {
            onDrawerClosed()
        }
    }

    // Show bottom sheet when category is selected
    LaunchedEffect(uiState.selectedCategoryForSheet) {
        if (uiState.selectedCategoryForSheet != null) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawer(
                categories = uiState.drawerCategories,
                versionName = versionName,
                appIcon = appIcon,
                onCategoryClick = { category ->
                    scope.launch {
                        drawerState.close()
                        onDrawerClosed()
                    }
                    onCategoryClick(category)
                },
                onCategoryExpand = onCategoryExpand,
                onMenuItemClick = { menuItem ->
                    scope.launch {
                        drawerState.close()
                        onDrawerClosed()
                    }
                    onMenuItemClick(menuItem)
                },
                isTreatmentsEnabled = uiState.isProfileLoaded
            )
        },
        gesturesEnabled = true,
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                MainTopBar(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                            onMenuClick()
                        }
                    },
                    onPreferencesClick = onPreferencesClick
                )
            },
            bottomBar = {
                MainNavigationBar(
                    currentDestination = uiState.currentNavDestination,
                    onDestinationSelected = onNavDestinationSelected,
                    onTreatmentClick = { showTreatmentSheet = true }
                )
            },
            floatingActionButton = {
                SwitchUiFab(onClick = onSwitchToClassicUi)
            }
        ) { paddingValues ->
            // Main content area
            when (uiState.currentNavDestination) {
                MainNavDestination.Overview -> {
                    OverviewScreen(
                        profileName = uiState.profileName,
                        isProfileModified = uiState.isProfileModified,
                        profileProgress = uiState.profileProgress,
                        tempTargetText = uiState.tempTargetText,
                        tempTargetState = uiState.tempTargetState,
                        tempTargetProgress = uiState.tempTargetProgress,
                        tempTargetReason = uiState.tempTargetReason,
                        runningMode = uiState.runningMode,
                        runningModeText = uiState.runningModeText,
                        runningModeProgress = uiState.runningModeProgress,
                        graphViewModel = graphViewModel,
                        actionsViewModel = actionsViewModel,
                        onProfileManagementClick = onProfileManagementClick,
                        onTempTargetClick = onTempTargetClick,
                        onRunningModeClick = onRunningModeClick,
                        onSensorInsertClick = onSensorInsertClick,
                        onFillClick = onFillClick,
                        onInsulinChangeClick = onInsulinChangeClick,
                        onBatteryChangeClick = onBatteryChangeClick,
                        paddingValues = paddingValues
                    )
                }

                MainNavDestination.Manage   -> {
                    ActionsScreen(
                        viewModel = actionsViewModel,
                        onProfileManagementClick = onProfileManagementClick,
                        onTempTargetClick = onTempTargetClick,
                        onTempBasalClick = onTempBasalClick,
                        onExtendedBolusClick = onExtendedBolusClick,
                        onHistoryBrowserClick = onHistoryBrowserClick,
                        onTddStatsClick = onTddStatsClick,
                        onBgCheckClick = onBgCheckClick,
                        onNoteClick = onNoteClick,
                        onExerciseClick = onExerciseClick,
                        onQuestionClick = onQuestionClick,
                        onAnnouncementClick = onAnnouncementClick,
                        onSiteRotationClick = onSiteRotationClick,
                        onError = onActionsError,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }

    // Plugin selection bottom sheet
    uiState.selectedCategoryForSheet?.let { category ->
        PluginSelectionSheet(
            category = category,
            isSimpleMode = uiState.isSimpleMode,
            pluginStateVersion = uiState.pluginStateVersion,
            sheetState = sheetState,
            onDismiss = onCategorySheetDismiss,
            onPluginClick = { plugin ->
                onCategorySheetDismiss()
                onPluginClick(plugin)
            },
            onPluginEnableToggle = onPluginEnableToggle,
            onPluginPreferencesClick = { plugin ->
                onCategorySheetDismiss()
                onPluginPreferencesClick(plugin)
            }
        )
    }

    // Treatment bottom sheet
    if (showTreatmentSheet) {
        TreatmentBottomSheet(
            onDismiss = { showTreatmentSheet = false },
            onCarbsClick = onCarbsClick,
            onInsulinClick = onInsulinClick,
            onTreatmentClick = onTreatmentClick,
            onCgmClick = onCgmClick,
            onCalibrationClick = onCalibrationClick,
            showCgmButton = showCgmButton,
            showCalibrationButton = showCalibrationButton,
            isDexcomSource = isDexcomSource,
            simpleMode = uiState.isSimpleMode,
            preferences = preferences,
            config = config
        )
    }

    // About dialog
    if (uiState.showAboutDialog && aboutDialogData != null) {
        AboutAlertDialog(
            data = aboutDialogData,
            onDismiss = onAboutDialogDismiss
        )
    }
}

@Composable
private fun SwitchUiFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AapsFab(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = app.aaps.core.ui.R.drawable.ic_swap_horiz),
            contentDescription = "Switch to classic UI"
        )
    }
}
