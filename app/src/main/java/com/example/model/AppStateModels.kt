package com.example.model

enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    STOPPING
}

enum class RoutingMode(val title: String, val description: String) {
    BYPASS_CN_AND_LAN("绕过局域网及大陆", "国内直连，国外走代理 (推荐)"),
    GLOBAL("全局代理", "所有流量均通过代理转发"),
    DIRECT("全局直连", "不走代理，所有流量直连"),
    AD_BLOCK("广告拦截强化", "绕过大陆并屏蔽已知广告跟踪规则")
}

data class TrafficStats(
    val uploadSpeedBytesPerSec: Long = 0L,
    val downloadSpeedBytesPerSec: Long = 0L,
    val totalUploadBytes: Long = 0L,
    val totalDownloadBytes: Long = 0L,
    val sessionDurationSeconds: Long = 0L
)

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

data class LogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val tag: String = "XrayCore",
    val message: String
)
