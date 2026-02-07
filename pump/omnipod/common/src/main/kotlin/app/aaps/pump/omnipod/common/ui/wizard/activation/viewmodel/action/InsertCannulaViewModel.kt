package app.aaps.pump.omnipod.common.ui.wizard.activation.viewmodel.action

import androidx.compose.runtime.Stable

import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.pump.PumpEnactResult
import app.aaps.core.interfaces.rx.AapsSchedulers
import javax.inject.Provider

@Stable
abstract class InsertCannulaViewModel(
    pumpEnactResultProvider: Provider<PumpEnactResult>,
    logger: AAPSLogger,
    aapsSchedulers: AapsSchedulers
) : PodActivationActionViewModelBase(pumpEnactResultProvider, logger, aapsSchedulers)