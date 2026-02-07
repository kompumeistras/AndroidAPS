package app.aaps.pump.omnipod.eros.ui.wizard.deactivation.viewmodel.info

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import app.aaps.pump.omnipod.common.ui.wizard.deactivation.viewmodel.info.PodDiscardedViewModel
import javax.inject.Inject

@Stable
class ErosPodDiscardedViewModel @Inject constructor() : PodDiscardedViewModel() {

    @StringRes
    override fun getTitleId(): Int = app.aaps.pump.omnipod.common.R.string.omnipod_common_pod_deactivation_wizard_pod_discarded_title

    @StringRes
    override fun getTextId() = app.aaps.pump.omnipod.common.R.string.omnipod_common_pod_deactivation_wizard_pod_discarded_text
}