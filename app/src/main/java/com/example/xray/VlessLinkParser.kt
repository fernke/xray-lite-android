package com.example.xray

import android.net.Uri
import com.example.model.ProxyNode
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object VlessLinkParser {

    /**
     * Parse a single vless:// URI string into a ProxyNode.
     * Returns null if parsing fails or URI is malformed.
     */
    fun parse(rawUri: String): ProxyNode? {
        val trimmed = rawUri.trim()
        if (!trimmed.startsWith("vless://", ignoreCase = true)) {
            return null
        }

        return try {
            val uri = Uri.parse(trimmed)
            val userInfo = uri.userInfo ?: ""
            val uuid = userInfo.trim()
            val host = uri.host ?: return null
            val port = if (uri.port != -1) uri.port else 443

            val encryption = uri.getQueryParameter("encryption") ?: "none"
            val security = uri.getQueryParameter("security") ?: "none"
            val network = uri.getQueryParameter("type")
                ?: uri.getQueryParameter("network")
                ?: "tcp"
            val flow = uri.getQueryParameter("flow") ?: ""
            val sni = uri.getQueryParameter("sni")
                ?: uri.getQueryParameter("peer")
                ?: ""
            val fp = uri.getQueryParameter("fp") ?: "chrome"
            val pbk = uri.getQueryParameter("pbk") ?: ""
            val sid = uri.getQueryParameter("sid") ?: ""
            val spx = uri.getQueryParameter("spx") ?: "/"
            val path = uri.getQueryParameter("path") ?: ""
            val headerHost = uri.getQueryParameter("host") ?: ""
            val serviceName = uri.getQueryParameter("serviceName") ?: ""

            var name = uri.fragment ?: ""
            if (name.isNotEmpty()) {
                name = try {
                    URLDecoder.decode(name, StandardCharsets.UTF_8.name())
                } catch (e: Exception) {
                    name
                }
            } else {
                name = "$host:$port"
            }

            ProxyNode(
                name = name,
                protocol = "vless",
                address = host,
                port = port,
                uuid = uuid,
                encryption = encryption,
                flow = flow,
                network = network.lowercase(),
                security = security.lowercase(),
                sni = sni,
                fingerprint = fp,
                publicKey = pbk,
                shortId = sid,
                spiderX = spx,
                path = path,
                host = headerHost,
                serviceName = serviceName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Extracts all vless:// links from text (e.g. copied from telegram/github/clipboard).
     */
    fun extractAllVless(text: String): List<ProxyNode> {
        val lines = text.split("\r\n", "\n", "\r")
        val result = mutableListOf<ProxyNode>()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("vless://", ignoreCase = true)) {
                val node = parse(trimmed)
                if (node != null) {
                    result.add(node)
                }
            }
        }
        return result
    }

    /**
     * Converts a ProxyNode into a standard vless:// URI string.
     */
    fun toVlessUri(node: ProxyNode): String {
        val queryParams = mutableListOf<String>()
        queryParams.add("encryption=${encodeParam(node.encryption.ifEmpty { "none" })}")
        if (node.security.isNotEmpty()) {
            queryParams.add("security=${encodeParam(node.security)}")
        }
        if (node.network.isNotEmpty()) {
            queryParams.add("type=${encodeParam(node.network)}")
        }
        if (node.flow.isNotEmpty()) {
            queryParams.add("flow=${encodeParam(node.flow)}")
        }
        if (node.sni.isNotEmpty()) {
            queryParams.add("sni=${encodeParam(node.sni)}")
        }
        if (node.fingerprint.isNotEmpty()) {
            queryParams.add("fp=${encodeParam(node.fingerprint)}")
        }
        if (node.security == "reality") {
            if (node.publicKey.isNotEmpty()) queryParams.add("pbk=${encodeParam(node.publicKey)}")
            if (node.shortId.isNotEmpty()) queryParams.add("sid=${encodeParam(node.shortId)}")
            if (node.spiderX.isNotEmpty()) queryParams.add("spx=${encodeParam(node.spiderX)}")
        }
        if (node.network == "ws") {
            if (node.path.isNotEmpty()) queryParams.add("path=${encodeParam(node.path)}")
            if (node.host.isNotEmpty()) queryParams.add("host=${encodeParam(node.host)}")
        }
        if (node.network == "grpc" && node.serviceName.isNotEmpty()) {
            queryParams.add("serviceName=${encodeParam(node.serviceName)}")
        }

        val queryString = queryParams.joinToString("&")
        val fragment = encodeParam(node.name)

        return "vless://${node.uuid}@${node.address}:${node.port}?$queryString#$fragment"
    }

    private fun encodeParam(value: String): String {
        return try {
            URLEncoder.encode(value, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            value
        }
    }
}
