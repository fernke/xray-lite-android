package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProxyNode
import com.example.ui.components.LatencyBadge
import com.example.ui.components.ProtocolTag
import com.example.ui.theme.BlueAccent
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
import com.example.xray.VlessLinkParser

@Composable
fun NodesScreen(
    viewModel: MainViewModel,
    onAddNode: () -> Unit,
    onEditNode: (ProxyNode) -> Unit,
    onOpenImportDialog: () -> Unit,
    onViewNodeJson: (ProxyNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nodes by viewModel.nodes.collectAsState()
    val isTestingLatency by viewModel.isTestingLatency.collectAsState()
    val testingNodeIds by viewModel.testingNodeIds.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredNodes = remember(nodes, searchQuery) {
        if (searchQuery.isBlank()) nodes
        else nodes.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.address.contains(searchQuery, ignoreCase = true) ||
            it.protocol.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Slate900)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "节点管理",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = "共 ${nodes.size} 个节点配置",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.testAllNodesLatency() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Slate800)
                                .testTag("batch_ping_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "批量测速",
                                tint = if (isTestingLatency) CyanNeon else Slate200,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onOpenImportDialog,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Slate800)
                                .testTag("nodes_import_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "导入链接",
                                tint = CyanNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("搜索节点名称、服务器或协议...", fontSize = 13.sp, color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    singleLine = true
                )
            }

            // List of Nodes
            if (filteredNodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (nodes.isEmpty()) "尚未添加任何代理节点" else "未找到匹配的节点",
                            fontSize = 15.sp,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenImportDialog,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate900)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("从剪贴板导入 VLESS 链接")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNodes, key = { it.id }) { node ->
                        NodeItemCard(
                            node = node,
                            isTesting = testingNodeIds.contains(node.id),
                            onSelect = { viewModel.selectNode(node) },
                            onPing = { viewModel.testNodeLatency(node) },
                            onEdit = { onEditNode(node) },
                            onViewJson = { onViewNodeJson(node) },
                            onDelete = { viewModel.deleteNode(node) },
                            onCopyLink = {
                                val link = VlessLinkParser.toVlessUri(node)
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("VLESS", link))
                                Toast.makeText(context, "已复制 VLESS 链接到剪贴板", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddNode,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_node_fab"),
            containerColor = CyanNeon,
            contentColor = Slate900,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "手动添加节点", modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun NodeItemCard(
    node: ProxyNode,
    isTesting: Boolean = false,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    onEdit: () -> Unit,
    onViewJson: () -> Unit,
    onDelete: () -> Unit,
    onCopyLink: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("node_item_${node.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (node.isSelected) Slate800 else Slate850
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (node.isSelected) listOf(CyanNeon.copy(alpha = 0.8f), BlueAccent)
                else listOf(Slate700, Slate800)
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (node.isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "选择",
                        tint = if (node.isSelected) CyanNeon else Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = node.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (node.isSelected) Color.White else Slate100,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${node.address}:${node.port}",
                            fontSize = 12.sp,
                            color = Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LatencyBadge(
                        latencyMs = node.latencyMs,
                        isLoading = isTesting,
                        onClick = onPing
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onPing,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "测试延迟",
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多选项",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Slate850)
                        ) {
                            DropdownMenuItem(
                                text = { Text("复制 VLESS 链接", color = Slate100, fontSize = 13.sp) },
                                onClick = {
                                    menuExpanded = false
                                    onCopyLink()
                                },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("查看生成的 Xray JSON", color = Slate100, fontSize = 13.sp) },
                                onClick = {
                                    menuExpanded = false
                                    onViewJson()
                                },
                                leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("编辑节点参数", color = Slate100, fontSize = 13.sp) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("删除节点", color = RoseError, fontSize = 13.sp) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseError, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Protocol badges
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
        }
    }
}
