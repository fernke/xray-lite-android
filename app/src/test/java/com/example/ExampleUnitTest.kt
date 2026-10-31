package com.example

import com.example.xray.PresetConfigs
import com.example.xray.XrayConfigGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testExtractProxyServerFromPresetConfigs() {
        val serverBypass = XrayConfigGenerator.extractProxyServer(PresetConfigs.PRESET_BYPASS_CN.rawJson)
        assertNotNull(serverBypass)
        assertEquals("hk.speedserver.net", serverBypass?.first)
        assertEquals(443, serverBypass?.second)

        val serverGlobal = XrayConfigGenerator.extractProxyServer(PresetConfigs.PRESET_GLOBAL.rawJson)
        assertNotNull(serverGlobal)
        assertEquals("us.cloudnode.org", serverGlobal?.first)
        assertEquals(443, serverGlobal?.second)

        val serverDual = XrayConfigGenerator.extractProxyServer(PresetConfigs.PRESET_DUAL_OUTBOUND.rawJson)
        assertNotNull(serverDual)
        assertEquals("jp.edgegateway.com", serverDual?.first)
        assertEquals(443, serverDual?.second)
    }
}
