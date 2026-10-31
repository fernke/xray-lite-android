package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProxyNode
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeEditDialog(
    initialNode: ProxyNode?,
    onDismiss: () -> Unit,
    onSave: (ProxyNode) -> Unit
) {
    val isEditing = initialNode != null

    var name by remember { mutableStateOf(initialNode?.name ?: "") }
    var address by remember { mutableStateOf(initialNode?.address ?: "") }
    var portText by remember { mutableStateOf(initialNode?.port?.toString() ?: "443") }
    var uuid by remember { mutableStateOf(initialNode?.uuid ?: "") }
    var flow by remember { mutableStateOf(initialNode?.flow ?: "xtls-rprx-vision") }
    var network by remember { mutableStateOf(initialNode?.network ?: "tcp") }
    var security by remember { mutableStateOf(initialNode?.security ?: "reality") }
    var sni by remember { mutableStateOf(initialNode?.sni ?: "") }
    var fingerprint by remember { mutableStateOf(initialNode?.fingerprint ?: "chrome") }
    var publicKey by remember { mutableStateOf(initialNode?.publicKey ?: "") }
    var shortId by remember { mutableStateOf(initialNode?.shortId ?: "") }
    var path by remember { mutableStateOf(initialNode?.path ?: "") }
    var host by remember { mutableStateOf(initialNode?.host ?: "") }

    var networkExpanded by remember { mutableStateOf(false) }
    var securityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isEditing) "编辑 VLESS 节点" else "添加 VLESS 节点",
                color = Slate100,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("节点备注 / 名称", fontSize = 12.sp) },
                    placeholder = { Text("例如: 香港 01 BGP Reality") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                // Address & Port
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("服务器地址", fontSize = 12.sp) },
                        placeholder = { Text("域名或 IP") },
                        modifier = Modifier.weight(1.8f),
                        colors = customTextFieldColors()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it },
                        label = { Text("端口", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors()
                    )
                }

                // UUID
                OutlinedTextField(
                    value = uuid,
                    onValueChange = { uuid = it },
                    label = { Text("用户 ID (UUID)", fontSize = 12.sp) },
                    placeholder = { Text("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                // Network & Security Selectors
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Network Dropdown
                    ExposedDropdownMenuBox(
                        expanded = networkExpanded,
                        onExpandedChange = { networkExpanded = !networkExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = network.uppercase(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("传输协议", fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = networkExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = customTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = networkExpanded,
                            onDismissRequest = { networkExpanded = false },
                            modifier = Modifier.background(Slate850)
                        ) {
                            listOf("tcp", "ws", "grpc").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.uppercase(), color = Slate100) },
                                    onClick = {
                                        network = opt
                                        networkExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Security Dropdown
                    ExposedDropdownMenuBox(
                        expanded = securityExpanded,
                        onExpandedChange = { securityExpanded = !securityExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = security.uppercase(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("安全传输", fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = securityExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = customTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = securityExpanded,
                            onDismissRequest = { securityExpanded = false },
                            modifier = Modifier.background(Slate850)
                        ) {
                            listOf("reality", "tls", "none").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.uppercase(), color = Slate100) },
                                    onClick = {
                                        security = opt
                                        securityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // SNI & Fingerprint
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sni,
                        onValueChange = { sni = it },
                        label = { Text("SNI 伪装域名", fontSize = 12.sp) },
                        placeholder = { Text("www.cloudflare.com") },
                        modifier = Modifier.weight(1.5f),
                        colors = customTextFieldColors()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = fingerprint,
                        onValueChange = { fingerprint = it },
                        label = { Text("指纹", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors()
                    )
                }

                // Reality Specific Fields
                if (security == "reality") {
                    OutlinedTextField(
                        value = publicKey,
                        onValueChange = { publicKey = it },
                        label = { Text("Reality 公钥 (pbk)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )
                    OutlinedTextField(
                        value = shortId,
                        onValueChange = { shortId = it },
                        label = { Text("Reality ShortId (sid)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )
                }

                // WebSocket Specific Fields
                if (network == "ws") {
                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
                        label = { Text("WebSocket 路径 (path)", fontSize = 12.sp) },
                        placeholder = { Text("/ws") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("WebSocket 伪装 Host", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val port = portText.toIntOrNull() ?: 443
                    val nodeToSave = (initialNode ?: ProxyNode(
                        name = if (name.isBlank()) "$address:$port" else name,
                        address = address,
                        port = port
                    )).copy(
                        name = if (name.isBlank()) "$address:$port" else name,
                        address = address.trim(),
                        port = port,
                        uuid = uuid.trim(),
                        flow = flow,
                        network = network,
                        security = security,
                        sni = sni.trim(),
                        fingerprint = fingerprint.trim(),
                        publicKey = publicKey.trim(),
                        shortId = shortId.trim(),
                        path = path.trim(),
                        host = host.trim()
                    )
                    onSave(nodeToSave)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate900)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消", color = Slate400)
            }
        }
    )
}

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Slate850,
    unfocusedContainerColor = Slate850,
    focusedBorderColor = CyanNeon,
    unfocusedBorderColor = Slate700,
    focusedTextColor = Slate100,
    unfocusedTextColor = Slate100,
    focusedLabelColor = CyanNeon,
    unfocusedLabelColor = Slate400
)
