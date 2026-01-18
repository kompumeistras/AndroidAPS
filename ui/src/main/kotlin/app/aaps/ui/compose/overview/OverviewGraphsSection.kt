package app.aaps.ui.compose.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.aaps.ui.compose.graphs.BgGraphCompose
import app.aaps.ui.compose.graphs.CobGraphCompose
import app.aaps.ui.compose.graphs.DEFAULT_GRAPH_ZOOM_MINUTES
import app.aaps.ui.compose.graphs.IobGraphCompose
import app.aaps.ui.compose.graphs.viewmodels.GraphViewModel
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom

/**
 * Overview graphs section using Vico charts.
 *
 * Pattern: Shared State with Gesture Blocking
 * - All graphs share the SAME VicoScrollState and VicoZoomState objects for perfect sync
 * - BG graph: Interactive - user can scroll/zoom to control all graphs
 * - IOB/COB graphs: Non-interactive - wrapped in Box with pointerInput to block ALL gestures
 *   - Box.pointerInput intercepts touch events BEFORE they reach CartesianChartHost
 *   - Prevents secondary graphs from modifying shared state
 *   - They only read and display the state set by BG graph
 *
 * Gesture Blocking Implementation:
 * - awaitPointerEventScope with infinite loop swallows all pointer events
 * - No consume() calls needed - just absorbing events prevents propagation
 *
 * Debugging:
 * - MonitorScrollState and MonitorZoomState log all state changes with stack traces
 * - Use XXXX prefix in logs to identify synchronization issues
 *
 * Graphs (top to bottom):
 * - BG Graph: Blood glucose readings (200dp height) - Primary interactive
 * - IOB Graph: Insulin on board (75dp height) - Display only, follows BG graph
 * - COB Graph: Carbs on board (75dp height) - Display only, follows BG graph
 */
@Composable
fun OverviewGraphsSection(
    graphViewModel: GraphViewModel,
    modifier: Modifier = Modifier
) {
    // Shared state - both graphs use the same state objects for perfect sync
    val scrollState = rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End
    )
    val zoomState = rememberVicoZoomState(
        zoomEnabled = false,
        initialZoom = Zoom.x(DEFAULT_GRAPH_ZOOM_MINUTES)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BG Graph - primary interactive graph
        BgGraphCompose(
            viewModel = graphViewModel,
            scrollState = scrollState,
            zoomState = zoomState,
            modifier = Modifier.fillMaxWidth()
        )

        // IOB Graph - non-interactive (scrollEnabled/zoomEnabled = false in state)
        IobGraphCompose(
            viewModel = graphViewModel,
            scrollState = scrollState,
            zoomState = zoomState,
            modifier = Modifier.fillMaxWidth()
        )

        // COB Graph - non-interactive (scrollEnabled/zoomEnabled = false in state)
        CobGraphCompose(
            viewModel = graphViewModel,
            scrollState = scrollState,
            zoomState = zoomState,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
