package com.example.service

import com.example.xray.XrayConfigGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object LatencyTester {

    /**
     * Test network latency to the target host and port via TCP handshake.
     * Returns latency in milliseconds, or -2 for timeout/unreachable.
     */
    suspend fun testLatency(host: String, port: Int, timeoutMs: Int = 2500): Long {
        return withContext(Dispatchers.IO) {
            val startNano = System.nanoTime()
            var socket: Socket? = null
            try {
                socket = Socket()
                val socketAddress = InetSocketAddress(host, port)
                socket.connect(socketAddress, timeoutMs)
                val durationMs = (System.nanoTime() - startNano) / 1_000_000L
                durationMs.coerceAtLeast(1L)
            } catch (e: Exception) {
                -2L
            } finally {
                try {
                    socket?.close()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Test network latency of an Xray JSON configuration profile by extracting its outbound server.
     * Returns Triple(serverAddress, serverPort, latencyMs) or null if no proxy outbound found.
     */
    suspend fun testProfileLatency(rawJson: String, timeoutMs: Int = 2500): Triple<String, Int, Long>? {
        val server = XrayConfigGenerator.extractProxyServer(rawJson) ?: return null
        val (host, port) = server
        val latency = testLatency(host, port, timeoutMs)
        return Triple(host, port, latency)
    }
}
