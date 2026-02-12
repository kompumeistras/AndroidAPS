package app.aaps.ui.compose.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aaps.core.interfaces.pump.actions.CustomAction
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.R as CoreUiR
import app.aaps.core.objects.R as ObjectsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBottomSheet(
    onDismiss: () -> Unit,
    // Visibility flags
    showTempTarget: Boolean,
    showTempBasal: Boolean,
    showCancelTempBasal: Boolean,
    showExtendedBolus: Boolean,
    showCancelExtendedBolus: Boolean,
    showTddStats: Boolean,
    // Cancel text strings
    cancelTempBasalText: String,
    cancelExtendedBolusText: String,
    // Custom actions
    customActions: List<CustomAction>,
    // Callbacks
    onProfileManagementClick: () -> Unit,
    onTempTargetClick: () -> Unit,
    onTempBasalClick: () -> Unit,
    onCancelTempBasalClick: () -> Unit,
    onExtendedBolusClick: () -> Unit,
    onCancelExtendedBolusClick: () -> Unit,
    onBgCheckClick: () -> Unit,
    onNoteClick: () -> Unit,
    onExerciseClick: () -> Unit,
    onQuestionClick: () -> Unit,
    onAnnouncementClick: () -> Unit,
    onSiteRotationClick: () -> Unit,
    onTddStatsClick: () -> Unit,
    onCustomActionClick: (CustomAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            // Section: Actions
            SectionHeader(stringResource(CoreUiR.string.actions))

            // Profile Management
            ManageItem(
                text = stringResource(CoreUiR.string.profile_management),
                description = stringResource(CoreUiR.string.manage_profile_desc),
                iconPainter = painterResource(CoreUiR.drawable.ic_actions_profileswitch),
                color = AapsTheme.elementColors.profileSwitch,
                onDismiss = onDismiss,
                onClick = onProfileManagementClick
            )

            // Temp Target
            if (showTempTarget) {
                ManageItem(
                    text = stringResource(CoreUiR.string.temp_target_management),
                    description = stringResource(CoreUiR.string.manage_temp_target_desc),
                    iconPainter = painterResource(ObjectsR.drawable.ic_temptarget_high),
                    color = AapsTheme.elementColors.tempTarget,
                    onDismiss = onDismiss,
                    onClick = onTempTargetClick
                )
            }

            // Temp Basal or Cancel Temp Basal
            if (showCancelTempBasal) {
                ManageItem(
                    text = cancelTempBasalText,
                    description = null,
                    iconPainter = painterResource(CoreUiR.drawable.ic_cancel_basal),
                    color = AapsTheme.elementColors.tempBasal,
                    onDismiss = onDismiss,
                    onClick = onCancelTempBasalClick
                )
            } else if (showTempBasal) {
                ManageItem(
                    text = stringResource(CoreUiR.string.tempbasal_button),
                    description = stringResource(CoreUiR.string.manage_temp_basal_desc),
                    iconPainter = painterResource(ObjectsR.drawable.ic_actions_start_temp_basal),
                    color = AapsTheme.elementColors.tempBasal,
                    onDismiss = onDismiss,
                    onClick = onTempBasalClick
                )
            }

            // Extended Bolus or Cancel Extended Bolus
            if (showCancelExtendedBolus) {
                ManageItem(
                    text = cancelExtendedBolusText,
                    description = null,
                    iconPainter = painterResource(CoreUiR.drawable.ic_actions_cancel_extended_bolus),
                    color = AapsTheme.elementColors.extendedBolus,
                    onDismiss = onDismiss,
                    onClick = onCancelExtendedBolusClick
                )
            } else if (showExtendedBolus) {
                ManageItem(
                    text = stringResource(CoreUiR.string.extended_bolus_button),
                    description = stringResource(CoreUiR.string.manage_extended_bolus_desc),
                    iconPainter = painterResource(ObjectsR.drawable.ic_actions_start_extended_bolus),
                    color = AapsTheme.elementColors.extendedBolus,
                    onDismiss = onDismiss,
                    onClick = onExtendedBolusClick
                )
            }

            // Section: Careportal
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
            SectionHeader(stringResource(CoreUiR.string.careportal))

            ManageItem(
                text = stringResource(CoreUiR.string.careportal_bgcheck),
                iconPainter = painterResource(ObjectsR.drawable.ic_cp_bgcheck),
                color = AapsTheme.elementColors.bgCheck,
                onDismiss = onDismiss,
                onClick = onBgCheckClick,
                coloredText = false
            )
            ManageItem(
                text = stringResource(CoreUiR.string.careportal_note),
                iconPainter = painterResource(ObjectsR.drawable.ic_cp_note),
                color = AapsTheme.elementColors.careportal,
                onDismiss = onDismiss,
                onClick = onNoteClick,
                coloredText = false
            )
            ManageItem(
                text = stringResource(CoreUiR.string.careportal_exercise),
                iconPainter = painterResource(ObjectsR.drawable.ic_cp_exercise),
                color = AapsTheme.elementColors.exercise,
                onDismiss = onDismiss,
                onClick = onExerciseClick,
                coloredText = false
            )
            ManageItem(
                text = stringResource(CoreUiR.string.careportal_question),
                iconPainter = painterResource(ObjectsR.drawable.ic_cp_question),
                color = AapsTheme.elementColors.careportal,
                onDismiss = onDismiss,
                onClick = onQuestionClick,
                coloredText = false
            )
            ManageItem(
                text = stringResource(CoreUiR.string.careportal_announcement),
                iconPainter = painterResource(ObjectsR.drawable.ic_cp_announcement),
                color = AapsTheme.elementColors.announcement,
                onDismiss = onDismiss,
                onClick = onAnnouncementClick,
                coloredText = false
            )

            // Section: Tools
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
            SectionHeader(stringResource(CoreUiR.string.tools))

            val toolsColor = MaterialTheme.colorScheme.primary
            ManageItem(
                text = stringResource(CoreUiR.string.site_rotation),
                description = stringResource(CoreUiR.string.manage_site_rotation_desc),
                iconPainter = painterResource(CoreUiR.drawable.ic_site_rotation),
                color = toolsColor,
                onDismiss = onDismiss,
                onClick = onSiteRotationClick
            )
            if (showTddStats) {
                ManageItem(
                    text = stringResource(CoreUiR.string.tdd_short),
                    iconPainter = painterResource(ObjectsR.drawable.ic_cp_stats),
                    color = toolsColor,
                    onDismiss = onDismiss,
                    onClick = onTddStatsClick
                )
            }

            // Section: Pump actions (only if non-empty)
            if (customActions.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                SectionHeader(stringResource(CoreUiR.string.pump_actions))

                val pumpColor = AapsTheme.elementColors.pump
                customActions.forEach { action ->
                    ManageItem(
                        text = stringResource(action.name),
                        iconPainter = painterResource(action.iconResourceId),
                        color = pumpColor,
                        onDismiss = onDismiss,
                        onClick = { onCustomActionClick(action) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
private fun ManageItem(
    text: String,
    iconPainter: Painter,
    color: Color,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    description: String? = null,
    coloredText: Boolean = true
) {
    ListItem(
        headlineContent = {
            Text(text = text, color = if (coloredText) color else Color.Unspecified)
        },
        supportingContent = description?.let {
            { Text(text = it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        leadingContent = {
            TonalIcon(painter = iconPainter, color = color)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable {
            onDismiss()
            onClick()
        }
    )
}

@Composable
private fun TonalIcon(
    painter: Painter,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .background(
                color = color.copy(alpha = 0.12f),
                shape = CircleShape
            )
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}
