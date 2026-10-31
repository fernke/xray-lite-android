package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnState
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun LatencyBadge(
    latencyMs: Long,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (color, text) = when {
        isLoading -> Pair(CyanNeon, "测速中...")
        latencyMs == -1L -> Pair(Slate400, "未测试")
        latencyMs == -2L -> Pair(RoseError, "超时")
        latencyMs < 100L -> Pair(CyanNeon, "${latencyMs} ms")
        latencyMs < 300L -> Pair(EmeraldOnline, "${latencyMs} ms")
        latencyMs < 600L -> Pair(AmberPending, "${latencyMs} ms")
        else -> Pair(RoseError, "${latencyMs} ms")
    }

    val clickableMod = if (onClick != null && !isLoading) modifier.clickable { onClick() } else modifier

    Row(
        modifier = clickableMod
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(8.dp),
                strokeWidth = 1.5.dp,
                color = CyanNeon
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProtocolTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CyanNeon
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun StatusIndicator(
    state: VpnState,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (state) {
        VpnState.CONNECTED -> Pair(EmeraldOnline, "已连接")
        VpnState.CONNECTING -> Pair(AmberPending, "连接中...")
        VpnState.STOPPING -> Pair(AmberPending, "断开中...")
        VpnState.DISCONNECTED -> Pair(Slate400, "已断开")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate800)
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatByteSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val group = digitGroups.coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, group.toDouble())
    return String.format(java.util.Locale.US, "%.1f %s", value, units[group])
}

fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec < 1024) return "${bytesPerSec} B/s"
    if (bytesPerSec < 1024 * 1024) return String.format(java.util.Locale.US, "%.1f KB/s", bytesPerSec / 1024.0)
    return String.format(java.util.Locale.US, "%.2f MB/s", bytesPerSec / (1024.0 * 1024.0))
}
