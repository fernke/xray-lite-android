package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proxy_nodes")
data class ProxyNode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val protocol: String = "vless", // vless, trojan, vmess, custom_json
    val address: String,
    val port: Int = 443,
    val uuid: String = "",
    val encryption: String = "none",
    val flow: String = "xtls-rprx-vision", // xtls-rprx-vision or empty
    val network: String = "tcp", // tcp, ws, grpc, httpupgrade
    val security: String = "reality", // reality, tls, none
    val sni: String = "",
    val fingerprint: String = "chrome",
    val publicKey: String = "", // reality pbk
    val shortId: String = "",   // reality sid
    val spiderX: String = "/",  // reality spx
    val path: String = "",      // ws path
    val host: String = "",      // ws host header
    val serviceName: String = "", // grpc serviceName
    val customJson: String? = null,
    val latencyMs: Long = -1L,  // -1 untested, -2 timeout, >0 latency
    val lastTestedTime: Long = 0L,
    val isSelected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
