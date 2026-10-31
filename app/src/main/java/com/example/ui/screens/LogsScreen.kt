package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogLevel
import com.example.service.LogManager
import com.example.ui.theme.AmberPending
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
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logs by viewModel.logs.collectAsState()
    var selectedLevel by remember { mutableStateOf<LogLevel?>(null) }

    val filteredLogs = remember(logs, selectedLevel) {
        if (selectedLevel == null) logs
        else logs.filter { it.level == selectedLevel }
    }

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "实时运行日志",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Text(
                        text = "Xray 内核与路由连接事件流",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }
            }

            Row {
                IconButton(
                    onClick = {
                        val text = logs.joinToString("\n") {
                            "[${LogManager.formatTimestamp(it.timestamp)}] [${it.level}] [${it.tag}] ${it.message}"
                        }
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Xray Logs", text))
                        Toast.makeText(context, "已复制全部日志到剪贴板", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Slate800)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制日志",
                        tint = Slate200,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.clearLogs() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Slate800)
                        .testTag("clear_logs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "清空日志",
                        tint = Slate200,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Level Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedLevel == null,
                onClick = { selectedLevel = null },
                label = { Text("全部", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanDeep,
                    selectedLabelColor = Color.White,
                    containerColor = Slate850,
                    labelColor = Slate400
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedLevel == null,
                    borderColor = Slate700,
                    selectedBorderColor = CyanNeon
                )
            )

            FilterChip(
                selected = selectedLevel == LogLevel.INFO,
                onClick = { selectedLevel = if (selectedLevel == LogLevel.INFO) null else LogLevel.INFO },
                label = { Text("INFO", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Slate800,
                    selectedLabelColor = CyanNeon,
                    containerColor = Slate850,
                    labelColor = Slate400
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedLevel == LogLevel.INFO,
                    borderColor = Slate700,
                    selectedBorderColor = CyanNeon
                )
            )

            FilterChip(
                selected = selectedLevel == LogLevel.WARN,
                onClick = { selectedLevel = if (selectedLevel == LogLevel.WARN) null else LogLevel.WARN },
                label = { Text("WARN", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Slate800,
                    selectedLabelColor = AmberPending,
                    containerColor = Slate850,
                    labelColor = Slate400
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedLevel == LogLevel.WARN,
                    borderColor = Slate700,
                    selectedBorderColor = AmberPending
                )
            )

            FilterChip(
                selected = selectedLevel == LogLevel.ERROR,
                onClick = { selectedLevel = if (selectedLevel == LogLevel.ERROR) null else LogLevel.ERROR },
                label = { Text("ERROR", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Slate800,
                    selectedLabelColor = RoseError,
                    containerColor = Slate850,
                    labelColor = Slate400
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedLevel == LogLevel.ERROR,
                    borderColor = Slate700,
                    selectedBorderColor = RoseError
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logs View
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate950),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate800))
        ) {
            if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无日志记录", fontSize = 13.sp, color = Slate700)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        val levelColor = when (log.level) {
                            LogLevel.INFO -> CyanNeon
                            LogLevel.DEBUG -> Slate400
                            LogLevel.WARN -> AmberPending
                            LogLevel.ERROR -> RoseError
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = LogManager.formatTimestamp(log.timestamp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[${log.level}]",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = levelColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[${log.tag}]",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Slate200
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = log.message,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (log.level == LogLevel.ERROR) RoseError else Slate100,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
