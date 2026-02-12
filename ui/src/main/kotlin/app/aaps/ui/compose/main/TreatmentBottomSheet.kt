package app.aaps.ui.compose.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.icons.Byoda
import app.aaps.core.ui.compose.icons.Calculator
import app.aaps.core.ui.compose.icons.Calibration
import app.aaps.core.ui.compose.icons.Carbs
import app.aaps.core.ui.compose.icons.Treatment
import app.aaps.core.ui.compose.icons.XDrip
import app.aaps.core.ui.compose.preference.AdaptivePreferenceList
import app.aaps.core.ui.compose.preference.PreferenceSubScreenDef
import app.aaps.core.ui.compose.preference.ProvidePreferenceTheme
import app.aaps.core.ui.R as CoreUiR

private val treatmentButtonSettingsDef = PreferenceSubScreenDef(
    key = "treatment_button_settings",
    titleResId = CoreUiR.string.settings,
    items = listOf(
        BooleanKey.OverviewShowCgmButton,
        BooleanKey.OverviewShowCalibrationButton,
        BooleanKey.OverviewShowTreatmentButton,
        BooleanKey.OverviewShowInsulinButton,
        BooleanKey.OverviewShowCarbsButton,
        BooleanKey.OverviewShowWizardButton
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentBottomSheet(
    onDismiss: () -> Unit,
    onCarbsClick: () -> Unit,
    onInsulinClick: (() -> Unit)? = null,
    onTreatmentClick: (() -> Unit)? = null,
    onCgmClick: (() -> Unit)? = null,
    onCalibrationClick: (() -> Unit)? = null,
    onCalculatorClick: (() -> Unit)? = null,
    showCgmButton: Boolean = false,
    showCalibrationButton: Boolean = false,
    isDexcomSource: Boolean = false,
    simpleMode: Boolean = false,
    preferences: Preferences? = null,
    config: Config? = null
) {
    val sheetState = rememberModalBottomSheetState()
    var showSettings by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        if (showSettings && preferences != null && config != null) {
            TreatmentSettingsContent(
                preferences = preferences,
                config = config,
                onBack = { showSettings = false }
            )
        } else {
            TreatmentSelectionContent(
                onDismiss = onDismiss,
                onCgmClick = onCgmClick,
                onCalibrationClick = onCalibrationClick,
                onCarbsClick = onCarbsClick,
                onInsulinClick = onInsulinClick,
                onTreatmentClick = onTreatmentClick,
                onCalculatorClick = onCalculatorClick,
                showCgm = showCgmButton && preferences?.get(BooleanKey.OverviewShowCgmButton) ?: false,
                showCalibration = showCalibrationButton && preferences?.get(BooleanKey.OverviewShowCalibrationButton) ?: false,
                showTreatment = preferences?.get(BooleanKey.OverviewShowTreatmentButton) ?: true,
                showInsulin = preferences?.get(BooleanKey.OverviewShowInsulinButton) ?: true,
                showCarbs = preferences?.get(BooleanKey.OverviewShowCarbsButton) ?: true,
                showCalculator = preferences?.get(BooleanKey.OverviewShowWizardButton) ?: true,
                isDexcomSource = isDexcomSource,
                showSettingsIcon = !simpleMode && preferences != null && config != null,
                onSettingsClick = { showSettings = true }
            )
        }
    }
}

@Composable
private fun TreatmentSelectionContent(
    onDismiss: () -> Unit,
    onCgmClick: (() -> Unit)?,
    onCalibrationClick: (() -> Unit)?,
    onCarbsClick: () -> Unit,
    onInsulinClick: (() -> Unit)?,
    onTreatmentClick: (() -> Unit)?,
    onCalculatorClick: (() -> Unit)?,
    showCgm: Boolean,
    showCalibration: Boolean,
    showTreatment: Boolean,
    showInsulin: Boolean,
    showCarbs: Boolean,
    showCalculator: Boolean,
    isDexcomSource: Boolean,
    showSettingsIcon: Boolean,
    onSettingsClick: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(CoreUiR.string.treatments),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (showSettingsIcon) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(CoreUiR.string.settings),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        val disabledAlpha = 0.38f

        // CGM
        if (showCgm) {
            val cgmEnabled = onCgmClick != null
            val cgmIcon = if (isDexcomSource) Byoda else XDrip
            val cgmColor = if (isDexcomSource) AapsTheme.elementColors.cgmDex else AapsTheme.elementColors.cgmXdrip
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(CoreUiR.string.cgm),
                        color = if (cgmEnabled) cgmColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha)
                    )
                },
                leadingContent = {
                    TonalIcon(
                        imageVector = cgmIcon,
                        color = if (cgmEnabled) cgmColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha),
                        enabled = cgmEnabled
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = if (cgmEnabled) Modifier.clickable {
                    onDismiss()
                    onCgmClick!!()
                } else Modifier
            )
        }

        // Calibration
        if (showCalibration) {
            val calibrationEnabled = onCalibrationClick != null
            val calibrationColor = AapsTheme.elementColors.calibration
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(CoreUiR.string.calibration),
                        color = if (calibrationEnabled) calibrationColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha)
                    )
                },
                leadingContent = {
                    TonalIcon(
                        imageVector = Calibration,
                        color = if (calibrationEnabled) calibrationColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha),
                        enabled = calibrationEnabled
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = if (calibrationEnabled) Modifier.clickable {
                    onDismiss()
                    onCalibrationClick!!()
                } else Modifier
            )
        }

        // Treatment
        if (showTreatment) {
            val treatmentEnabled = onTreatmentClick != null
            val treatmentColor = MaterialTheme.colorScheme.primary
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(CoreUiR.string.overview_treatment_label),
                        color = if (treatmentEnabled) treatmentColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha)
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(CoreUiR.string.treatment_desc),
                        color = if (treatmentEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = disabledAlpha)
                    )
                },
                leadingContent = {
                    TonalIcon(
                        imageVector = Icons.Default.Add,
                        color = if (treatmentEnabled) treatmentColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha),
                        enabled = treatmentEnabled
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = if (treatmentEnabled) Modifier.clickable {
                    onDismiss()
                    onTreatmentClick!!()
                } else Modifier
            )
        }

        // Insulin
        if (showInsulin) {
            val insulinEnabled = onInsulinClick != null
            val insulinColor = AapsTheme.elementColors.insulin
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(CoreUiR.string.overview_insulin_label),
                        color = if (insulinEnabled) insulinColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha)
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(CoreUiR.string.treatment_insulin_desc),
                        color = if (insulinEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = disabledAlpha)
                    )
                },
                leadingContent = {
                    TonalIcon(
                        imageVector = Treatment,
                        color = if (insulinEnabled) insulinColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha),
                        enabled = insulinEnabled
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = if (insulinEnabled) Modifier.clickable {
                    onDismiss()
                    onInsulinClick!!()
                } else Modifier
            )
        }

        // Carbs
        if (showCarbs) {
            val carbsColor = AapsTheme.elementColors.carbs
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(CoreUiR.string.carbs),
                        color = carbsColor
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(CoreUiR.string.treatment_carbs_desc),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    TonalIcon(
                        imageVector = Carbs,
                        color = carbsColor,
                        enabled = true
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.clickable {
                    onDismiss()
                    onCarbsClick()
                }
            )
        }

        // Calculator (not migrated yet)
        if (showCalculator) {
            val calculatorEnabled = onCalculatorClick != null
            val calculatorColor = AapsTheme.generalColors.calculator
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(CoreUiR.string.boluswizard),
                        color = if (calculatorEnabled) calculatorColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha)
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(CoreUiR.string.treatment_calculator_desc),
                        color = if (calculatorEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = disabledAlpha)
                    )
                },
                leadingContent = {
                    TonalIcon(
                        imageVector = Calculator,
                        color = if (calculatorEnabled) calculatorColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha),
                        enabled = calculatorEnabled
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = if (calculatorEnabled) Modifier.clickable {
                    onDismiss()
                    onCalculatorClick!!()
                } else Modifier
            )
        }
    }
}

@Composable
private fun TreatmentSettingsContent(
    preferences: Preferences,
    config: Config,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(CoreUiR.string.back)
                )
            }
            Text(
                text = stringResource(CoreUiR.string.settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ProvidePreferenceTheme {
            AdaptivePreferenceList(
                items = treatmentButtonSettingsDef.items,
                preferences = preferences,
                config = config
            )
        }
    }
}

@Composable
private fun TonalIcon(
    imageVector: ImageVector,
    color: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .background(
                color = if (enabled) color.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TreatmentBottomSheetPreview() {
    MaterialTheme {
        TreatmentSelectionContent(
            onDismiss = {},
            onCgmClick = {},
            onCalibrationClick = {},
            onCarbsClick = {},
            onInsulinClick = {},
            onTreatmentClick = {},
            onCalculatorClick = null,
            showCgm = true,
            showCalibration = true,
            showTreatment = true,
            showInsulin = true,
            showCarbs = true,
            showCalculator = true,
            isDexcomSource = false,
            showSettingsIcon = true,
            onSettingsClick = {}
        )
    }
}
