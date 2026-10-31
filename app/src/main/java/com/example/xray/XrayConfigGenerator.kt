package com.example.xray

import com.example.model.ProxyNode
import com.example.model.RoutingMode
import org.json.JSONArray
import org.json.JSONObject

object XrayConfigGenerator {

    /**
     * Generates a complete, standard Xray-core JSON configuration
     * based on the selected ProxyNode and RoutingMode.
     */
    fun generateConfig(
        node: ProxyNode,
        routingMode: RoutingMode = RoutingMode.BYPASS_CN_AND_LAN,
        socksPort: Int = 10808,
        httpPort: Int = 10809
    ): String {
        // If node already has a custom full json, return it formatted
        if (!node.customJson.isNullOrBlank()) {
            return formatJson(node.customJson)
        }

        val root = JSONObject()

        // 1. Log configuration
        val logObj = JSONObject().apply {
            put("access", "")
            put("error", "")
            put("loglevel", "warning")
        }
        root.put("log", logObj)

        // 2. DNS configuration
        val dnsObj = JSONObject().apply {
            val servers = JSONArray().apply {
                put("8.8.8.8")
                put("1.1.1.1")
                put("https://dns.google/dns-query")
                val directDns = JSONObject().apply {
                    put("address", "223.5.5.5")
                    put("port", 53)
                    put("domains", JSONArray().apply {
                        put("geosite:cn")
                    })
                    put("expectIPs", JSONArray().apply {
                        put("geoip:cn")
                    })
                }
                put(directDns)
            }
            put("servers", servers)
        }
        root.put("dns", dnsObj)

        // 3. Inbounds (SOCKS + HTTP + Sniffing)
        val inboundsArray = JSONArray()

        // SOCKS5 Inbound
        val socksInbound = JSONObject().apply {
            put("tag", "socks-in")
            put("port", socksPort)
            put("listen", "127.0.0.1")
            put("protocol", "socks")
            put("settings", JSONObject().apply {
                put("auth", "noauth")
                put("udp", true)
            })
            put("sniffing", JSONObject().apply {
                put("enabled", true)
                put("destOverride", JSONArray().apply {
                    put("http")
                    put("tls")
                    put("quic")
                })
                put("routeOnly", true)
            })
        }
        inboundsArray.put(socksInbound)

        // HTTP Inbound
        val httpInbound = JSONObject().apply {
            put("tag", "http-in")
            put("port", httpPort)
            put("listen", "127.0.0.1")
            put("protocol", "http")
            put("settings", JSONObject().apply {
                put("allowTransparent", false)
            })
        }
        inboundsArray.put(httpInbound)

        root.put("inbounds", inboundsArray)

        // 4. Outbounds (Proxy + Direct + Block)
        val outboundsArray = JSONArray()

        // Main Proxy Outbound (VLESS)
        val proxyOutbound = JSONObject().apply {
            put("tag", "proxy")
            put("protocol", "vless")

            // Settings
            val settings = JSONObject().apply {
                val vnextArray = JSONArray()
                val serverObj = JSONObject().apply {
                    put("address", node.address)
                    put("port", node.port)
                    val usersArray = JSONArray()
                    val userObj = JSONObject().apply {
                        put("id", node.uuid)
                        put("encryption", node.encryption.ifEmpty { "none" })
                        if (node.flow.isNotEmpty()) {
                            put("flow", node.flow)
                        }
                    }
                    usersArray.put(userObj)
                    put("users", usersArray)
                }
                vnextArray.put(serverObj)
                put("vnext", vnextArray)
            }
            put("settings", settings)

            // Stream Settings
            val streamSettings = JSONObject().apply {
                put("network", node.network.ifEmpty { "tcp" })
                put("security", node.security.ifEmpty { "none" })

                when (node.security) {
                    "reality" -> {
                        val realitySettings = JSONObject().apply {
                            put("show", false)
                            put("fingerprint", node.fingerprint.ifEmpty { "chrome" })
                            put("serverName", node.sni.ifEmpty { node.address })
                            put("publicKey", node.publicKey)
                            put("shortId", node.shortId)
                            put("spiderX", node.spiderX.ifEmpty { "/" })
                        }
                        put("realitySettings", realitySettings)
                    }
                    "tls" -> {
                        val tlsSettings = JSONObject().apply {
                            put("serverName", node.sni.ifEmpty { node.address })
                            put("fingerprint", node.fingerprint.ifEmpty { "chrome" })
                            put("allowInsecure", false)
                        }
                        put("tlsSettings", tlsSettings)
                    }
                }

                // Transport-specific stream settings
                when (node.network) {
                    "ws" -> {
                        val wsSettings = JSONObject().apply {
                            put("path", node.path.ifEmpty { "/" })
                            put("headers", JSONObject().apply {
                                if (node.host.isNotEmpty()) {
                                    put("Host", node.host)
                                }
                            })
                        }
                        put("wsSettings", wsSettings)
                    }
                    "grpc" -> {
                        val grpcSettings = JSONObject().apply {
                            put("serviceName", node.serviceName)
                            put("multiMode", false)
                        }
                        put("grpcSettings", grpcSettings)
                    }
                    "tcp" -> {
                        val tcpSettings = JSONObject().apply {
                            put("header", JSONObject().apply {
                                put("type", "none")
                            })
                        }
                        put("tcpSettings", tcpSettings)
                    }
                }
            }
            put("streamSettings", streamSettings)
        }
        outboundsArray.put(proxyOutbound)

        // Direct Outbound
        val directOutbound = JSONObject().apply {
            put("tag", "direct")
            put("protocol", "freedom")
            put("settings", JSONObject())
        }
        outboundsArray.put(directOutbound)

        // Block Outbound
        val blockOutbound = JSONObject().apply {
            put("tag", "block")
            put("protocol", "blackhole")
            put("settings", JSONObject().apply {
                put("response", JSONObject().apply {
                    put("type", "http")
                })
            })
        }
        outboundsArray.put(blockOutbound)

        root.put("outbounds", outboundsArray)

        // 5. Routing Rules
        val routingObj = JSONObject().apply {
            put("domainStrategy", "IPIfNonMatch")
            val rulesArray = JSONArray()

            // Always bypass private network IPs
            val privateRule = JSONObject().apply {
                put("type", "field")
                put("outboundTag", "direct")
                put("ip", JSONArray().apply {
                    put("geoip:private")
                })
            }
            rulesArray.put(privateRule)

            when (routingMode) {
                RoutingMode.BYPASS_CN_AND_LAN -> {
                    // Direct CN domains and IPs
                    val cnDomainRule = JSONObject().apply {
                        put("type", "field")
                        put("outboundTag", "direct")
                        put("domain", JSONArray().apply {
                            put("geosite:cn")
                        })
                    }
                    rulesArray.put(cnDomainRule)

                    val cnIpRule = JSONObject().apply {
                        put("type", "field")
                        put("outboundTag", "direct")
                        put("ip", JSONArray().apply {
                            put("geoip:cn")
                        })
                    }
                    rulesArray.put(cnIpRule)

                    // Block ads
                    val adsRule = JSONObject().apply {
                        put("type", "field")
                        put("outboundTag", "block")
                        put("domain", JSONArray().apply {
                            put("geosite:category-ads-all")
                        })
                    }
                    rulesArray.put(adsRule)
                }

                RoutingMode.AD_BLOCK -> {
                    // Block ads
                    val adsRule = JSONObject().apply {
                        put("type", "field")
                        put("outboundTag", "block")
                        put("domain", JSONArray().apply {
                            put("geosite:category-ads-all")
                        })
                    }
                    rulesArray.put(adsRule)

                    val cnDomainRule = JSONObject().apply {
                        put("type", "field")
                        put("outboundTag", "direct")
                        put("domain", JSONArray().apply {
                            put("geosite:cn")
                        })
                    }
                    rulesArray.put(cnDomainRule)
                }

                RoutingMode.GLOBAL -> {
                    // All traffic goes to proxy outbound (default)
                }

                RoutingMode.DIRECT -> {
                    val allDirectRule = JSONObject().apply {
                        put("type", "field")
                        put("outboundTag", "direct")
                        put("network", "tcp,udp")
                    }
                    rulesArray.put(allDirectRule)
                }
            }

            put("rules", rulesArray)
        }
        root.put("routing", routingObj)

        return root.toString(2)
    }

    /**
     * Nicely indents and formats JSON string.
     */
    fun formatJson(raw: String): String {
        return try {
            val json = JSONObject(raw)
            json.toString(2)
        } catch (e: Exception) {
            try {
                val array = JSONArray(raw)
                array.toString(2)
            } catch (e2: Exception) {
                raw
            }
        }
    }

    /**
     * Validate whether a string is a valid JSON Xray configuration.
     * Returns a pair of (isValid, errorMessage).
     */
    fun validateJson(raw: String): Pair<Boolean, String> {
        if (raw.isBlank()) {
            return Pair(false, "配置内容不能为空")
        }
        return try {
            val json = JSONObject(raw)
            if (!json.has("outbounds") && !json.has("inbounds")) {
                Pair(false, "警告: 缺少核心节点字段 (inbounds 或 outbounds)")
            } else {
                Pair(true, "JSON 格式与 Xray 核心结构校验通过")
            }
        } catch (e: Exception) {
            Pair(false, "JSON 语法错误: ${e.message}")
        }
    }

    /**
     * Extracts the primary proxy server address and port from an Xray JSON config.
     */
    fun extractProxyServer(rawJson: String): Pair<String, Int>? {
        return try {
            val json = JSONObject(rawJson)
            val outbounds = json.optJSONArray("outbounds") ?: return null
            for (i in 0 until outbounds.length()) {
                val outbound = outbounds.optJSONObject(i) ?: continue
                val tag = outbound.optString("tag")
                val protocol = outbound.optString("protocol")
                if (tag == "proxy" || tag == "main-proxy" || protocol in listOf("vless", "vmess", "trojan", "shadowsocks")) {
                    val settings = outbound.optJSONObject("settings")
                    val vnext = settings?.optJSONArray("vnext")
                    if (vnext != null && vnext.length() > 0) {
                        val server = vnext.getJSONObject(0)
                        val address = server.optString("address")
                        val port = server.optInt("port", 443)
                        if (address.isNotBlank()) {
                            return Pair(address, port)
                        }
                    }
                    val servers = settings?.optJSONArray("servers")
                    if (servers != null && servers.length() > 0) {
                        val server = servers.getJSONObject(0)
                        val address = server.optString("address")
                        val port = server.optInt("port", 443)
                        if (address.isNotBlank()) {
                            return Pair(address, port)
                        }
                    }
                }
            }
            null
        } catch (_: Exception) {
            // Regex fallback for comments or stubbed environments
            try {
                val outboundsIdx = rawJson.indexOf("\"outbounds\"")
                val searchTarget = if (outboundsIdx != -1) rawJson.substring(outboundsIdx) else rawJson
                val addressRegex = Regex("\"address\"\\s*:\\s*\"([^\"]+)\"")
                val portRegex = Regex("\"port\"\\s*:\\s*(\\d+)")
                val addressMatch = addressRegex.find(searchTarget)?.groupValues?.get(1)
                val portMatch = portRegex.find(searchTarget)?.groupValues?.get(1)?.toIntOrNull() ?: 443
                if (!addressMatch.isNullOrBlank()) {
                    Pair(addressMatch, portMatch)
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }
}
