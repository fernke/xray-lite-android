package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ProxyNode
import com.example.model.RoutingMode
import com.example.xray.VlessLinkParser
import com.example.xray.XrayConfigGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Xray Lite", appName)
    }

    @Test
    fun `test vless parser with reality parameters`() {
        val link = "vless://7d4b68e2-9012-4211-8be3-a128e46920aa@hk01.gateway.edge:443?encryption=none&security=reality&type=tcp&flow=xtls-rprx-vision&sni=www.cloudflare.com&fp=chrome&pbk=7y9B0gQzLgN3E3M-q2N6F5_d6W9aB7c4D2e1F0g8H6j&sid=6ba855d1&spx=%2F#HK-01"
        val node = VlessLinkParser.parse(link)

        assertNotNull(node)
        assertEquals("HK-01", node?.name)
        assertEquals("hk01.gateway.edge", node?.address)
        assertEquals(443, node?.port)
        assertEquals("7d4b68e2-9012-4211-8be3-a128e46920aa", node?.uuid)
        assertEquals("reality", node?.security)
        assertEquals("xtls-rprx-vision", node?.flow)
        assertEquals("6ba855d1", node?.shortId)
    }

    @Test
    fun `test xray config generator`() {
        val node = ProxyNode(
            name = "Test Node",
            address = "test.server.com",
            port = 443,
            uuid = "11111111-2222-3333-4444-555555555555",
            security = "reality",
            network = "tcp"
        )
        val json = XrayConfigGenerator.generateConfig(node, RoutingMode.BYPASS_CN_AND_LAN)
        val (isValid, msg) = XrayConfigGenerator.validateJson(json)

        assertTrue(isValid)
        assertTrue(json.contains("test.server.com"))
        assertTrue(json.contains("11111111-2222-3333-4444-555555555555"))
        assertTrue(json.contains("inbounds"))
        assertTrue(json.contains("outbounds"))
        assertTrue(json.contains("routing"))
    }
}
