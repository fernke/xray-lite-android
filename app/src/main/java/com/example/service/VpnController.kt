package com.example.service

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.example.model.ProxyNode
import com.example.model.RoutingMode
import com.example.model.TrafficStats
import com.example.model.VpnState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VpnController {
    private val _vpnState = MutableStateFlow(VpnState.DISCONNECTED)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val _trafficStats = MutableStateFlow(TrafficStats())
    val trafficStats: StateFlow<TrafficStats> = _trafficStats.asStateFlow()

    private val _activeNode = MutableStateFlow<ProxyNode?>(null)
    val activeNode: StateFlow<ProxyNode?> = _activeNode.asStateFlow()

    private val _routingMode = MutableStateFlow(RoutingMode.BYPASS_CN_AND_LAN)
    val routingMode: StateFlow<RoutingMode> = _routingMode.asStateFlow()

    fun updateState(state: VpnState) {
        _vpnState.value = state
    }

    fun updateTrafficStats(stats: TrafficStats) {
        _trafficStats.value = stats
    }

    fun setActiveNode(node: ProxyNode?) {
        _activeNode.value = node
    }

    fun setRoutingMode(mode: RoutingMode) {
        _routingMode.value = mode
    }

    /**
     * Start the VPN service with the specified node.
     */
    fun start(context: Context, node: ProxyNode, mode: RoutingMode = _routingMode.value) {
        _activeNode.value = node
        _routingMode.value = mode
        _vpnState.value = VpnState.CONNECTING

        val intent = Intent(context, XrayVpnService::class.java).apply {
            action = XrayVpnService.ACTION_START
            putExtra(XrayVpnService.EXTRA_NODE_NAME, node.name)
            putExtra(XrayVpnService.EXTRA_NODE_ADDRESS, node.address)
            putExtra(XrayVpnService.EXTRA_NODE_PORT, node.port)
            putExtra(XrayVpnService.EXTRA_ROUTING_MODE, mode.name)
        }
        context.startService(intent)
    }

    /**
     * Stop the running VPN service.
     */
    fun stop(context: Context) {
        _vpnState.value = VpnState.STOPPING
        val intent = Intent(context, XrayVpnService::class.java).apply {
            action = XrayVpnService.ACTION_STOP
        }
        context.startService(intent)
    }

    /**
     * Helper to prepare VpnService permission dialog if needed.
     */
    fun prepare(context: Context): Intent? {
        return VpnService.prepare(context)
    }
}
