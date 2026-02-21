package app.aaps.ui.compose.overview.manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.aaps.core.interfaces.pump.actions.CustomAction
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.TonalIcon
import app.aaps.core.ui.compose.icons.IcActivity
import app.aaps.core.ui.compose.icons.IcAnnouncement
import app.aaps.core.ui.compose.icons.IcBgCheck
import app.aaps.core.ui.compose.icons.IcCancelExtendedBolus
import app.aaps.core.ui.compose.icons.IcExtendedBolus
import app.aaps.core.ui.compose.icons.IcNote
import app.aaps.core.ui.compose.icons.IcProfile
import app.aaps.core.ui.compose.icons.IcQuestion
import app.aaps.core.ui.compose.icons.IcQuickwizard
import app.aaps.core.ui.compose.icons.IcSiteRotation
import app.aaps.core.ui.compose.icons.IcTbrCancel
import app.aaps.core.ui.compose.icons.IcTbrHigh
import app.aaps.core.ui.compose.icons.IcTtHigh
import app.aaps.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBottomSheet(
    onDismiss: () -> Unit,
    isSimpleMode: Boolean,
    // Visibility flags
    showTempTarget: Boolean,
    showTempBasal: Boolean,
    showCancelTempBasal: Boolean,
    showExtendedBolus: Boolean,
    showCancelExtendedBolus: Boolean,
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
    onQuickWizardClick: () -> Unit,
    onCustomActionClick: (CustomAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        ManageBottomSheetContent(
            isSimpleMode = isSimpleMode,
            showTempTarget = showTempTarget,
            showTempBasal = showTempBasal,
            showCancelTempBasal = showCancelTempBasal,
            showExtendedBolus = showExtendedBolus,
            showCancelExtendedBolus = showCancelExtendedBolus,
            cancelTempBasalText = cancelTempBasalText,
            cancelExtendedBolusText = cancelExtendedBolusText,
            customActions = customActions,
            onDismiss = onDismiss,
            onProfileManagementClick = onProfileManagementClick,
            onTempTargetClick = onTempTargetClick,
            onTempBasalClick = onTempBasalClick,
            onCancelTempBasalClick = onCancelTempBasalClick,
            onExtendedBolusClick = onExtendedBolusClick,
            onCancelExtendedBolusClick = onCancelExtendedBolusClick,
            onBgCheckClick = onBgCheckClick,
            onNoteClick = onNoteClick,
            onExerciseClick = onExerciseClick,
            onQuestionClick = onQuestionClick,
            onAnnouncementClick = onAnnouncementClick,
            onSiteRotationClick = onSiteRotationClick,
            onQuickWizardClick = onQuickWizardClick,
            onCustomActionClick = onCustomActionClick
        )
    }
}

@Composable
internal fun ManageBottomSheetContent(
    isSimpleMode: Boolean = false,
    showTempTarget: Boolean,
    showTempBasal: Boolean,
    showCancelTempBasal: Boolean,
    showExtendedBolus: Boolean,
    showCancelExtendedBolus: Boolean,
    cancelTempBasalText: String,
    cancelExtendedBolusText: String,
    customActions: List<CustomAction>,
    onDismiss: () -> Unit = {},
    onProfileManagementClick: () -> Unit = {},
    onTempTargetClick: () -> Unit = {},
    onTempBasalClick: () -> Unit = {},
    onCancelTempBasalClick: () -> Unit = {},
    onExtendedBolusClick: () -> Unit = {},
    onCancelExtendedBolusClick: () -> Unit = {},
    onBgCheckClick: () -> Unit = {},
    onNoteClick: () -> Unit = {},
    onExerciseClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {},
    onAnnouncementClick: () -> Unit = {},
    onSiteRotationClick: () -> Unit = {},
    onQuickWizardClick: () -> Unit = {},
    onCustomActionClick: (CustomAction) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Section: Manage
        SectionHeader(stringResource(CoreUiR.string.manage))

        // Profile Management
        ManageItem(
            text = stringResource(CoreUiR.string.profile_management),
            description = stringResource(CoreUiR.string.manage_profile_desc),
            iconPainter = rememberVectorPainter(IcProfile),
            color = AapsTheme.elementColors.profileSwitch,
            onDismiss = onDismiss,
            onClick = onProfileManagementClick
        )

        // Temp Target
        if (showTempTarget) {
            ManageItem(
                text = stringResource(CoreUiR.string.temp_target_management),
                description = stringResource(CoreUiR.string.manage_temp_target_desc),
                iconPainter = rememberVectorPainter(IcTtHigh),
                color = AapsTheme.elementColors.tempTarget,
                onDismiss = onDismiss,
                onClick = onTempTargetClick
            )
        }

        ManageItem(
            text = stringResource(CoreUiR.string.quickwizard_managemnt),
            description = stringResource(CoreUiR.string.manage_quickwizard_desc),
            iconPainter = rememberVectorPainter(IcQuickwizard),
            color = AapsTheme.elementColors.carbs,
            onDismiss = onDismiss,
            onClick = onQuickWizardClick
        )

        // Temp Basal or Cancel Temp Basal
        if (showCancelTempBasal) {
            ManageItem(
                text = cancelTempBasalText,
                description = null,
                iconPainter = rememberVectorPainter(IcTbrCancel),
                color = AapsTheme.elementColors.tempBasal,
                onDismiss = onDismiss,
                onClick = onCancelTempBasalClick
            )
        } else if (showTempBasal) {
            ManageItem(
                text = stringResource(CoreUiR.string.tempbasal_button),
                description = stringResource(CoreUiR.string.manage_temp_basal_desc),
                iconPainter = rememberVectorPainter(IcTbrHigh),
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
                iconPainter = rememberVectorPainter(IcCancelExtendedBolus),
                color = AapsTheme.elementColors.extendedBolus,
                onDismiss = onDismiss,
                onClick = onCancelExtendedBolusClick
            )
        } else if (showExtendedBolus) {
            ManageItem(
                text = stringResource(CoreUiR.string.extended_bolus_button),
                description = stringResource(CoreUiR.string.manage_extended_bolus_desc),
                iconPainter = rememberVectorPainter(IcExtendedBolus),
                color = AapsTheme.elementColors.extendedBolus,
                onDismiss = onDismiss,
                onClick = onExtendedBolusClick
            )
        }

        // Section: Careportal (hidden in simple mode)
        if (!isSimpleMode) {
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
            SectionHeader(stringResource(CoreUiR.string.careportal))

            ManageItem(
                text = stringResource(CoreUiR.string.careportal_bgcheck),
                iconPainter = rememberVectorPainter(IcBgCheck),
                color = AapsTheme.elementColors.bgCheck,
                onDismiss = onDismiss,
                onClick = onBgCheckClick,
                coloredText = false
            )
            ManageItem(
                text = stringResource(CoreUiR.string.careportal_note),
                iconPainter = rememberVectorPainter(IcNote),
                color = AapsTheme.elementColors.careportal,
                onDismiss = onDismiss,
                onClick = onNoteClick,
                coloredText = false
            )
            ManageItem(
                text = stringResource(CoreUiR.string.careportal_exercise),
                iconPainter = rememberVectorPainter(IcActivity),
                color = AapsTheme.elementColors.exercise,
                onDismiss = onDismiss,
                onClick = onExerciseClick,
                coloredText = false
            )
            ManageItem(
                text = stringResource(CoreUiR.string.careportal_question),
                iconPainter = rememberVectorPainter(IcQuestion),
                color = AapsTheme.elementColors.careportal,
                onDismiss = onDismiss,
                onClick = onQuestionClick,
                coloredText = false
            )
            ManageItem(
                text = stringResource(CoreUiR.string.careportal_announcement),
                iconPainter = rememberVectorPainter(IcAnnouncement),
                color = AapsTheme.elementColors.announcement,
                onDismiss = onDismiss,
                onClick = onAnnouncementClick,
                coloredText = false
            )
        }

        // Section: Tools
        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
        SectionHeader(stringResource(CoreUiR.string.tools))

        val toolsColor = MaterialTheme.colorScheme.primary
        ManageItem(
            text = stringResource(CoreUiR.string.site_rotation),
            description = stringResource(CoreUiR.string.manage_site_rotation_desc),
            iconPainter = rememberVectorPainter(IcSiteRotation),
            color = toolsColor,
            onDismiss = onDismiss,
            onClick = onSiteRotationClick
        )
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

@Preview(showBackground = true)
@Composable
private fun ManageBottomSheetContentPreview() {
    MaterialTheme {
        ManageBottomSheetContent(
            showTempTarget = true,
            showTempBasal = true,
            showCancelTempBasal = false,
            showExtendedBolus = true,
            showCancelExtendedBolus = false,
            cancelTempBasalText = "",
            cancelExtendedBolusText = "",
            customActions = emptyList()
        )
    }
}
