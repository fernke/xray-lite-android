package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProxyNode
import com.example.model.RoutingMode
import com.example.model.VpnState
import com.example.ui.components.LatencyBadge
import com.example.ui.components.ProtocolTag
import com.example.ui.components.StatusIndicator
import com.example.ui.components.formatByteSize
import com.example.ui.components.formatSpeed
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanDeep
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToNodes: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onOpenImportDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vpnState by viewModel.vpnState.collectAsState()
    val trafficStats by viewModel.trafficStats.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val routingMode by viewModel.routingMode.collectAsState()
    val testingNodeIds by viewModel.testingNodeIds.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(CyanNeon, BlueAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "App Icon",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Xray Lite",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = "极简内核代理客户端",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusIndicator(state = vpnState)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onOpenImportDialog,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Slate800)
                            .testTag("import_clipboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "从剪贴板导入",
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Central Connect Switch
        item {
            ConnectButtonSection(
                vpnState = vpnState,
                onToggle = { viewModel.toggleVpn(context) }
            )
        }

        // Real-time Traffic Telemetry Card
        item {
            TrafficTelemetryCard(trafficStats = trafficStats, isConnected = vpnState == VpnState.CONNECTED)
        }

        // Active Node Card
        item {
            ActiveNodeCard(
                node = selectedNode,
                isTesting = selectedNode?.id?.let { testingNodeIds.contains(it) } ?: false,
                onSwitchNode = onNavigateToNodes,
                onPing = { selectedNode?.let { viewModel.testNodeLatency(it) } }
            )
        }

        // Routing Mode Selector
        item {
            RoutingModeSection(
                selectedMode = routingMode,
                onSelectMode = { viewModel.setRoutingMode(it) }
            )
        }

        // Quick Actions Row
        item {
            QuickActionsSection(
                onImport = onOpenImportDialog,
                onTestAll = { viewModel.testAllNodesLatency() },
                onViewConfig = onNavigateToConfig
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ConnectButtonSection(
    vpnState: VpnState,
    onToggle: () -> Unit
) {
    val isConnected = vpnState == VpnState.CONNECTED
    val isConnecting = vpnState == VpnState.CONNECTING || vpnState == VpnState.STOPPING

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected || isConnecting) 1.14f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = if (isConnected || isConnecting) 0.45f else 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    val primaryColor = when (vpnState) {
        VpnState.CONNECTED -> EmeraldOnline
        VpnState.CONNECTING, VpnState.STOPPING -> AmberPending
        VpnState.DISCONNECTED -> CyanNeon
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow ring
            if (isConnected || isConnecting) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = haloAlpha))
                )
            }

            // Middle ring
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Slate850)
                    .border(3.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
            )

            // Inner button
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            if (isConnected) listOf(EmeraldOnline, Color(0xFF059669))
                            else if (isConnecting) listOf(AmberPending, Color(0xFFD97706))
                            else listOf(Slate800, Slate850)
                        )
                    )
                    .border(
                        2.dp,
                        if (isConnected || isConnecting) Color.White.copy(alpha = 0.6f) else Slate700,
                        CircleShape
                    )
                    .clickable { onToggle() }
                    .testTag("connect_power_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "开关",
                        tint = if (isConnected || isConnecting) Color.White else CyanNeon,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (vpnState) {
                            VpnState.CONNECTED -> "已连接"
                            VpnState.CONNECTING -> "连接中"
                            VpnState.STOPPING -> "断开中"
                            VpnState.DISCONNECTED -> "点击启动"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected || isConnecting) Color.White else Slate200
                    )
                }
            }
        }
    }
}

@Composable
fun TrafficTelemetryCard(
    trafficStats: com.example.model.TrafficStats,
    isConnected: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Slate700, Slate800)))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "实时流量",
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "实时流量监测",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate100
                    )
                }

                if (isConnected) {
                    val minutes = trafficStats.sessionDurationSeconds / 60
                    val seconds = trafficStats.sessionDurationSeconds % 60
                    Text(
                        text = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds),
                        fontSize = 12.sp,
                        color = CyanGlow,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Upload speed & total
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "上传",
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "上传速率", fontSize = 11.sp, color = Slate400)
                        Text(
                            text = if (isConnected) formatSpeed(trafficStats.uploadSpeedBytesPerSec) else "0.0 KB/s",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = "总计: ${formatByteSize(trafficStats.totalUploadBytes)}",
                            fontSize = 10.sp,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Download speed & total
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(EmeraldOnline.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "下载",
                            tint = EmeraldOnline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "下载速率", fontSize = 11.sp, color = Slate400)
                        Text(
                            text = if (isConnected) formatSpeed(trafficStats.downloadSpeedBytesPerSec) else "0.0 KB/s",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = "总计: ${formatByteSize(trafficStats.totalDownloadBytes)}",
                            fontSize = 10.sp,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveNodeCard(
    node: ProxyNode?,
    isTesting: Boolean = false,
    onSwitchNode: () -> Unit,
    onPing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSwitchNode() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Slate700, Slate800)))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前活动节点",
                    fontSize = 12.sp,
                    color = Slate400,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "切换节点 ›",
                    fontSize = 12.sp,
                    color = CyanNeon,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (node != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = node.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${node.address}:${node.port}",
                            fontSize = 12.sp,
                            color = Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LatencyBadge(
                            latencyMs = node.latencyMs,
                            isLoading = isTesting,
                            onClick = onPing
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onPing,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Slate800)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "测试延迟",
                                tint = CyanNeon,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProtocolTag(text = node.protocol)
                    if (node.security.isNotEmpty() && node.security != "none") {
                        ProtocolTag(text = node.security, color = BlueAccent)
                    }
                    if (node.network.isNotEmpty()) {
                        ProtocolTag(text = node.network, color = VioletAccent)
                    }
                    if (node.flow.isNotEmpty()) {
                        ProtocolTag(text = "vision", color = EmeraldOnline)
                    }
                }
            } else {
                Text(
                    text = "暂无已选节点，点击前往节点列表选择或导入",
                    fontSize = 14.sp,
                    color = Slate400,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutingModeSection(
    selectedMode: RoutingMode,
    onSelectMode: (RoutingMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "分流与路由策略",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate400,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedMode == RoutingMode.BYPASS_CN_AND_LAN,
                onClick = { onSelectMode(RoutingMode.BYPASS_CN_AND_LAN) },
                label = { Text("绕过大陆", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanDeep,
                    selectedLabelColor = Color.White,
                    containerColor = Slate850,
                    labelColor = Slate200
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedMode == RoutingMode.BYPASS_CN_AND_LAN,
                    borderColor = Slate700,
                    selectedBorderColor = CyanNeon
                )
            )

            FilterChip(
                selected = selectedMode == RoutingMode.GLOBAL,
                onClick = { onSelectMode(RoutingMode.GLOBAL) },
                label = { Text("全局代理", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanDeep,
                    selectedLabelColor = Color.White,
                    containerColor = Slate850,
                    labelColor = Slate200
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedMode == RoutingMode.GLOBAL,
                    borderColor = Slate700,
                    selectedBorderColor = CyanNeon
                )
            )

            FilterChip(
                selected = selectedMode == RoutingMode.AD_BLOCK,
                onClick = { onSelectMode(RoutingMode.AD_BLOCK) },
                label = { Text("广告过滤", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanDeep,
                    selectedLabelColor = Color.White,
                    containerColor = Slate850,
                    labelColor = Slate200
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedMode == RoutingMode.AD_BLOCK,
                    borderColor = Slate700,
                    selectedBorderColor = CyanNeon
                )
            )
        }
    }
}

@Composable
fun QuickActionsSection(
    onImport: () -> Unit,
    onTestAll: () -> Unit,
    onViewConfig: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "快捷工具",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate400,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onImport,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("剪贴板导入", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onTestAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("批量测速", fontSize = 12.sp)
            }
        }
    }
}
