package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProtocolTag
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var sniffingEnabled by remember { mutableStateOf(true) }
    var routeOnlyEnabled by remember { mutableStateOf(true) }
    var bypassLanEnabled by remember { mutableStateOf(true) }
    var muxEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = CyanNeon,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "系统与内核设置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
                Text(
                    text = "配置本地入站端口、嗅探与 DNS 解析",
                    fontSize = 12.sp,
                    color = Slate400
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Local Inbound Ports
            item {
                SettingsSectionCard(
                    title = "本地入站代理端口",
                    icon = Icons.Default.Lan
                ) {
                    SettingItem(
                        label = "SOCKS5 本地代理端口",
                        sublabel = "127.0.0.1:10808 (支持 UDP 转发)",
                        value = "10808"
                    )
                    SettingItem(
                        label = "HTTP 本地代理端口",
                        sublabel = "127.0.0.1:10809 (供浏览器或局域网工具)",
                        value = "10809"
                    )
                    SettingItem(
                        label = "TUN 虚拟网卡 MTU",
                        sublabel = "标准隧道最大传输单元",
                        value = "1500"
                    )
                }
            }

            // Sniffing and Traffic Identification
            item {
                SettingsSectionCard(
                    title = "协议嗅探与流量识别",
                    icon = Icons.Default.Security
                ) {
                    SettingToggleItem(
                        label = "启用流量嗅探 (Sniffing)",
                        sublabel = "识别 HTTP/TLS/QUIC 目标域名以准确执行分流",
                        checked = sniffingEnabled,
                        onCheckedChange = { sniffingEnabled = it }
                    )
                    SettingToggleItem(
                        label = "仅用于路由决策 (RouteOnly)",
                        sublabel = "只用嗅探域名匹配出站规则，不覆盖连接目标",
                        checked = routeOnlyEnabled,
                        onCheckedChange = { routeOnlyEnabled = it }
                    )
                    SettingToggleItem(
                        label = "绕过局域网私有地址",
                        sublabel = "自动将 192.168.x.x, 10.x.x.x 划为直连",
                        checked = bypassLanEnabled,
                        onCheckedChange = { bypassLanEnabled = it }
                    )
                }
            }

            // DNS Settings
            item {
                SettingsSectionCard(
                    title = "DNS 防污染与解析",
                    icon = Icons.Default.Dns
                ) {
                    SettingItem(
                        label = "远程抗污染 DNS",
                        sublabel = "用于解析国外受保护域名",
                        value = "8.8.8.8, 1.1.1.1"
                    )
                    SettingItem(
                        label = "国内直连 DNS",
                        sublabel = "用于解析 geosite:cn 国内高速访问",
                        value = "223.5.5.5, 119.29.29.29"
                    )
                }
            }

            // About Card
            item {
                SettingsSectionCard(
                    title = "关于 Xray Lite",
                    icon = Icons.Default.Info
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("应用版本", fontSize = 14.sp, color = Slate100, fontWeight = FontWeight.Medium)
                            Text("Xray Lite v1.0.0", fontSize = 12.sp, color = Slate400)
                        }
                        ProtocolTag(text = "Release", color = EmeraldOnline)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("核心兼容性", fontSize = 14.sp, color = Slate100, fontWeight = FontWeight.Medium)
                            Text("兼容 Xray-core v1.8.x 标准配置规范", fontSize = 12.sp, color = Slate400)
                        }
                        ProtocolTag(text = "VLESS Reality", color = CyanNeon)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Slate700, Slate800)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate100)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingItem(
    label: String,
    sublabel: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 14.sp, color = Slate100, fontWeight = FontWeight.Medium)
            Text(text = sublabel, fontSize = 11.sp, color = Slate400)
        }
        Text(
            text = value,
            fontSize = 13.sp,
            color = CyanNeon,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Slate800)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SettingToggleItem(
    label: String,
    sublabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 14.sp, color = Slate100, fontWeight = FontWeight.Medium)
            Text(text = sublabel, fontSize = 11.sp, color = Slate400)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyanNeon,
                uncheckedThumbColor = Slate400,
                uncheckedTrackColor = Slate800
            )
        )
    }
}
