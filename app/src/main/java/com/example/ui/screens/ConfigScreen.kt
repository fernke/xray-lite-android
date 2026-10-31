package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.XrayConfigProfile
import com.example.ui.components.LatencyBadge
import com.example.ui.components.ProtocolTag
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
import com.example.ui.theme.Slate950
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.MainViewModel
import com.example.xray.XrayConfigGenerator

@Composable
fun ConfigScreen(
    viewModel: MainViewModel,
    onCreateProfile: () -> Unit,
    onEditProfile: (XrayConfigProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val customProfiles by viewModel.customProfiles.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val activeNode by viewModel.selectedNode.collectAsState()
    val routingMode by viewModel.routingMode.collectAsState()
    val testingProfileIds by viewModel.testingProfileIds.collectAsState()

    val currentGeneratedJson = remember(activeNode, routingMode) {
        viewModel.getActiveGeneratedJson()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "配置管理中心",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
                Text(
                    text = "管理自定义 Xray 配置文件及实时规则",
                    fontSize = 12.sp,
                    color = Slate400
                )
            }

            if (selectedTab == 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.testAllProfilesLatency() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Slate800)
                            .testTag("ping_all_configs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "测试全部配置延迟",
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onCreateProfile,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate900),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("新建配置", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Slate850,
            contentColor = CyanNeon,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanNeon
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("当前核心配置", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("自定义配置文件 (${customProfiles.size})", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
            )
        }

        if (selectedTab == 0) {
            // Active Config View
            ActiveConfigTab(
                jsonContent = currentGeneratedJson,
                onCopy = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("Xray Config", currentGeneratedJson))
                    Toast.makeText(context, "已复制当前 Xray JSON 配置", Toast.LENGTH_SHORT).show()
                },
                onSaveAsCustom = {
                    val newProfile = XrayConfigProfile(
                        name = "从活动节点导出 (${activeNode?.name ?: "自定义"})",
                        description = "导出于 ${java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}",
                        rawJson = currentGeneratedJson,
                        isPreset = false
                    )
                    viewModel.saveConfigProfile(newProfile)
                    selectedTab = 1
                }
            )
        } else {
            // Custom Profiles List Tab
            CustomProfilesTab(
                profiles = customProfiles,
                activeProfile = activeProfile,
                testingProfileIds = testingProfileIds,
                onPing = { viewModel.testProfileLatency(it) },
                onSelectActive = { viewModel.activateConfigProfile(it) },
                onEdit = { onEditProfile(it) },
                onDelete = { viewModel.deleteConfigProfile(it) },
                onCopy = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("Xray Profile", it.rawJson))
                    Toast.makeText(context, "已复制 [${it.name}] 配置内容", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun ActiveConfigTab(
    jsonContent: String,
    onCopy: () -> Unit,
    onSaveAsCustom: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xray-Core 动态聚合生成配置",
                    fontSize = 13.sp,
                    color = Slate400,
                    fontWeight = FontWeight.Medium
                )

                Row {
                    OutlinedButton(
                        onClick = onSaveAsCustom,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("存为自定义配置", fontSize = 11.sp, color = CyanNeon)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onCopy,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanDeep, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("复制", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate950),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Slate700, Slate800)))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = jsonContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = CyanGlow,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CustomProfilesTab(
    profiles: List<XrayConfigProfile>,
    activeProfile: XrayConfigProfile?,
    testingProfileIds: Set<Long>,
    onPing: (XrayConfigProfile) -> Unit,
    onSelectActive: (XrayConfigProfile) -> Unit,
    onEdit: (XrayConfigProfile) -> Unit,
    onDelete: (XrayConfigProfile) -> Unit,
    onCopy: (XrayConfigProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(profiles, key = { it.id }) { profile ->
            val proxyServer = remember(profile.rawJson) {
                XrayConfigGenerator.extractProxyServer(profile.rawJson)
            }
            val isTesting = testingProfileIds.contains(profile.id)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEdit(profile) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (profile.isActive) Slate800 else Slate850
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        if (profile.isActive) listOf(CyanNeon, BlueAccent)
                        else listOf(Slate700, Slate800)
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row: Profile Name & Latency Badge next to each configuration entry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (profile.isActive) CyanNeon else Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate100,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (profile.isPreset) {
                                        ProtocolTag(text = "预设模板", color = BlueAccent)
                                    } else {
                                        ProtocolTag(text = "自定义", color = VioletAccent)
                                    }
                                }
                                if (profile.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = profile.description,
                                        fontSize = 12.sp,
                                        color = Slate400,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Network Latency Checker Display next to each configuration entry
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LatencyBadge(
                                latencyMs = profile.latencyMs,
                                isLoading = isTesting,
                                onClick = { onPing(profile) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onPing(profile) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "测试配置延迟",
                                    tint = CyanNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Server target info if available
                    if (proxyServer != null) {
                        Text(
                            text = "代理服务器: ${proxyServer.first}:${proxyServer.second}",
                            fontSize = 11.sp,
                            color = Slate400,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (profile.isActive) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldOnline.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldOnline,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("当前生效", color = EmeraldOnline, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectActive(profile) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("激活应用", fontSize = 11.sp, color = CyanNeon)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onCopy(profile) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制", tint = Slate400, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { onEdit(profile) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "编辑", tint = CyanNeon, modifier = Modifier.size(16.dp))
                            }

                            if (!profile.isPreset) {
                                IconButton(
                                    onClick = { onDelete(profile) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = RoseError, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
