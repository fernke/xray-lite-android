package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.XrayConfigProfile
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.xray.PresetConfigs
import com.example.xray.XrayConfigGenerator

@Composable
fun ConfigEditorDialog(
    initialProfile: XrayConfigProfile?,
    onDismiss: () -> Unit,
    onSave: (XrayConfigProfile) -> Unit
) {
    val context = LocalContext.current
    val isEditing = initialProfile != null

    var name by remember { mutableStateOf(initialProfile?.name ?: "自定义配置文件") }
    var description by remember { mutableStateOf(initialProfile?.description ?: "") }
    var rawJson by remember {
        mutableStateOf(
            initialProfile?.rawJson ?: PresetConfigs.PRESET_BYPASS_CN.rawJson
        )
    }

    var validationStatus by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var templateMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "编辑 Xray 配置文件" else "新建自定义配置文件",
                    color = Slate100,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    OutlinedButton(
                        onClick = { templateMenuExpanded = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.IntegrationInstructions, contentDescription = null, tint = CyanNeon, modifier = Modifier.height(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("模板", fontSize = 11.sp, color = CyanNeon)
                    }

                    DropdownMenu(
                        expanded = templateMenuExpanded,
                        onDismissRequest = { templateMenuExpanded = false },
                        modifier = Modifier.background(Slate850)
                    ) {
                        DropdownMenuItem(
                            text = { Text("标准分流 + 抗污染模板", color = Slate100, fontSize = 12.sp) },
                            onClick = {
                                rawJson = PresetConfigs.PRESET_BYPASS_CN.rawJson
                                templateMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("全局代理模板", color = Slate100, fontSize = 12.sp) },
                            onClick = {
                                rawJson = PresetConfigs.PRESET_GLOBAL.rawJson
                                templateMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("双出站分流模板", color = Slate100, fontSize = 12.sp) },
                            onClick = {
                                rawJson = PresetConfigs.PRESET_DUAL_OUTBOUND.rawJson
                                templateMenuExpanded = false
                            }
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name & description
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("配置名称", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述说明 (可选)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                // Validation & Format Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Xray 核心 JSON 内容:", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Medium)

                    Row {
                        OutlinedButton(
                            onClick = {
                                val (isValid, msg) = XrayConfigGenerator.validateJson(rawJson)
                                validationStatus = Pair(isValid, msg)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = CyanNeon, modifier = Modifier.height(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("校验语法", fontSize = 11.sp, color = CyanNeon)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedButton(
                            onClick = {
                                rawJson = XrayConfigGenerator.formatJson(rawJson)
                                Toast.makeText(context, "已格式化 JSON", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.height(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("格式化", fontSize = 11.sp, color = EmeraldOnline)
                        }
                    }
                }

                // Validation Status feedback
                validationStatus?.let { (isValid, msg) ->
                    Text(
                        text = msg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isValid) EmeraldOnline else RoseError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                (if (isValid) EmeraldOnline else RoseError).copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                }

                // Monospace editor
                OutlinedTextField(
                    value = rawJson,
                    onValueChange = {
                        rawJson = it
                        validationStatus = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Slate100,
                        lineHeight = 16.sp
                    ),
                    colors = customTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val profileToSave = (initialProfile ?: XrayConfigProfile(
                        name = name,
                        rawJson = rawJson
                    )).copy(
                        name = if (name.isBlank()) "自定义配置" else name.trim(),
                        description = description.trim(),
                        rawJson = rawJson.trim(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(profileToSave)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate900)
            ) {
                Text("保存配置", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消", color = Slate400)
            }
        }
    )
}
