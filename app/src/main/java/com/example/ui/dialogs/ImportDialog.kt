package com.example.ui.dialogs

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.xray.VlessLinkParser
import com.example.xray.XrayConfigGenerator

@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onConfirmImport: (String) -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }

    // Auto-read clipboard on open
    LaunchedEffect(Unit) {
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = cm.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val clipText = clip.getItemAt(0).text?.toString() ?: ""
                if (clipText.contains("vless://", ignoreCase = true) || clipText.contains("\"outbounds\"")) {
                    inputText = clipText
                }
            }
        } catch (_: Exception) {}
    }

    val parsedPreview = remember(inputText) {
        val trimmed = inputText.trim()
        if (trimmed.isEmpty()) {
            null
        } else {
            val vlessList = VlessLinkParser.extractAllVless(trimmed)
            if (vlessList.isNotEmpty()) {
                if (vlessList.size == 1) {
                    val n = vlessList[0]
                    "已识别单节点: ${n.name} (${n.address}:${n.port}) [VLESS/${n.security.uppercase()}]"
                } else {
                    "已识别批量 ${vlessList.size} 个 VLESS 节点"
                }
            } else {
                val single = VlessLinkParser.parse(trimmed)
                if (single != null) {
                    "已识别 VLESS 节点: ${single.name} (${single.address}:${single.port})"
                } else {
                    val (isValid, _) = XrayConfigGenerator.validateJson(trimmed)
                    if (isValid) {
                        "已识别自定义 Xray JSON 配置文件"
                    } else {
                        "未识别到有效 VLESS 链接或 Xray JSON"
                    }
                }
            }
        }
    }

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
                    text = "从剪贴板导入",
                    color = Slate100,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = {
                        try {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = cm.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                inputText = clip.getItemAt(0).text?.toString() ?: ""
                            }
                        } catch (_: Exception) {}
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("读取剪贴板", fontSize = 11.sp, color = CyanNeon)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "支持单条或多条 vless:// 链接，也支持直接粘贴 Xray JSON 自定义配置：",
                    fontSize = 12.sp,
                    color = Slate400
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("vless://uuid@host:port?...#NodeName", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("import_text_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Slate100
                    ),
                    colors = customTextFieldColors()
                )

                parsedPreview?.let { preview ->
                    val isSuccess = !preview.startsWith("未识别")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                (if (isSuccess) EmeraldOnline else Slate850).copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSuccess) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = preview,
                            fontSize = 12.sp,
                            color = if (isSuccess) EmeraldOnline else Slate400,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onConfirmImport(inputText)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate900),
                modifier = Modifier.testTag("confirm_import_button")
            ) {
                Text("确认导入", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消", color = Slate400)
            }
        }
    )
}
