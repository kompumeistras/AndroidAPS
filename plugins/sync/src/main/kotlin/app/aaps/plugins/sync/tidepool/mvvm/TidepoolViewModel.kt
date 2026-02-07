package app.aaps.plugins.sync.tidepool.mvvm

import androidx.compose.runtime.Stable
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.plugins.sync.tidepool.auth.AuthFlowOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class TidepoolUiState(
    val connectionStatus: String = "",
    val logList: List<TidepoolLog> = emptyList()
)

@Stable
class TidepoolViewModel @Inject constructor(
    private val tidepoolMvvmRepository: TidepoolMvvmRepository,
    private val authFlowOut: AuthFlowOut
) : ViewModel() {

    val uiState: StateFlow<TidepoolUiState>
        field = MutableStateFlow(TidepoolUiState())

    init {
        viewModelScope.launch {
            tidepoolMvvmRepository.connectionStatus.collect { status ->
                uiState.update { it.copy(connectionStatus = status.name) }
            }
        }
        viewModelScope.launch {
            tidepoolMvvmRepository.logList.collect { logList ->
                uiState.update { it.copy(logList = logList) }
            }
        }
    }

    fun loadInitialData() {
        tidepoolMvvmRepository.updateConnectionStatus(authFlowOut.connectionStatus)
    }
}
