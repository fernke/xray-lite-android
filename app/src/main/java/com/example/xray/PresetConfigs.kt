package com.example.xray

import com.example.model.XrayConfigProfile

object PresetConfigs {

    val PRESET_BYPASS_CN = XrayConfigProfile(
        id = 1,
        name = "标准分流与防污染配置",
        description = "国内域名直连并解析到阿里DNS，国外流量走代理，过滤常见广告跟踪",
        isPreset = true,
        isActive = true,
        latencyMs = 45L,
        lastTestedTime = System.currentTimeMillis(),
        rawJson = """
{
  "log": {
    "loglevel": "warning"
  },
  "dns": {
    "servers": [
      "8.8.8.8",
      "1.1.1.1",
      {
        "address": "223.5.5.5",
        "port": 53,
        "domains": ["geosite:cn"]
      }
    ]
  },
  "inbounds": [
    {
      "tag": "socks-in",
      "port": 10808,
      "listen": "127.0.0.1",
      "protocol": "socks",
      "settings": {
        "auth": "noauth",
        "udp": true
      },
      "sniffing": {
        "enabled": true,
        "destOverride": ["http", "tls", "quic"],
        "routeOnly": true
      }
    },
    {
      "tag": "http-in",
      "port": 10809,
      "listen": "127.0.0.1",
      "protocol": "http"
    }
  ],
  "outbounds": [
    {
      "tag": "proxy",
      "protocol": "vless",
      "settings": {
        "vnext": [
          {
            "address": "hk.speedserver.net",
            "port": 443,
            "users": [
              {
                "id": "e4d909c2-9013-424a-9ec5-b67484df9468",
                "encryption": "none",
                "flow": "xtls-rprx-vision"
              }
            ]
          }
        ]
      },
      "streamSettings": {
        "network": "tcp",
        "security": "reality",
        "realitySettings": {
          "show": false,
          "fingerprint": "chrome",
          "serverName": "www.cloudflare.com",
          "publicKey": "7y9B0gQzLgN3E3M-q2N6F5_d6W9aB7c4D2e1F0g8H6j",
          "shortId": "6ba855d1",
          "spiderX": "/"
        }
      }
    },
    {
      "tag": "direct",
      "protocol": "freedom"
    },
    {
      "tag": "block",
      "protocol": "blackhole"
    }
  ],
  "routing": {
    "domainStrategy": "IPIfNonMatch",
    "rules": [
      {
        "type": "field",
        "outboundTag": "direct",
        "ip": ["geoip:private"]
      },
      {
        "type": "field",
        "outboundTag": "block",
        "domain": ["geosite:category-ads-all"]
      },
      {
        "type": "field",
        "outboundTag": "direct",
        "domain": ["geosite:cn"]
      },
      {
        "type": "field",
        "outboundTag": "direct",
        "ip": ["geoip:cn"]
      },
      {
        "type": "field",
        "outboundTag": "proxy",
        "network": "tcp,udp"
      }
    ]
  }
}
        """.trimIndent()
    )

    val PRESET_GLOBAL = XrayConfigProfile(
        id = 2,
        name = "全局出站代理配置",
        description = "强制所有流量无差别走代理出站节点，适合国际漫游与跨境网络加速",
        isPreset = true,
        isActive = false,
        latencyMs = 120L,
        lastTestedTime = System.currentTimeMillis(),
        rawJson = """
{
  "log": {
    "loglevel": "warning"
  },
  "dns": {
    "servers": [
      "8.8.8.8",
      "1.1.1.1"
    ]
  },
  "inbounds": [
    {
      "tag": "socks-in",
      "port": 10808,
      "listen": "127.0.0.1",
      "protocol": "socks",
      "settings": {
        "auth": "noauth",
        "udp": true
      }
    },
    {
      "tag": "http-in",
      "port": 10809,
      "listen": "127.0.0.1",
      "protocol": "http"
    }
  ],
  "outbounds": [
    {
      "tag": "proxy",
      "protocol": "vless",
      "settings": {
        "vnext": [
          {
            "address": "us.cloudnode.org",
            "port": 443,
            "users": [
              {
                "id": "b3e18a22-3118-498c-8f92-c17983ea9010",
                "encryption": "none"
              }
            ]
          }
        ]
      },
      "streamSettings": {
        "network": "ws",
        "security": "tls",
        "wsSettings": {
          "path": "/xray-ws",
          "headers": {
            "Host": "us.cloudnode.org"
          }
        }
      }
    },
    {
      "tag": "direct",
      "protocol": "freedom"
    }
  ],
  "routing": {
    "domainStrategy": "AsIs",
    "rules": [
      {
        "type": "field",
        "outboundTag": "direct",
        "ip": ["geoip:private"]
      },
      {
        "type": "field",
        "outboundTag": "proxy",
        "network": "tcp,udp"
      }
    ]
  }
}
        """.trimIndent()
    )

    val PRESET_DUAL_OUTBOUND = XrayConfigProfile(
        id = 3,
        name = "主备双出站分流配置",
        description = "配置主节点与备用线路，媒体流媒体服务走专属出站",
        isPreset = true,
        isActive = false,
        latencyMs = 52L,
        lastTestedTime = System.currentTimeMillis(),
        rawJson = """
{
  "log": {
    "loglevel": "info"
  },
  "dns": {
    "servers": ["1.1.1.1", "8.8.8.8"]
  },
  "inbounds": [
    {
      "tag": "socks-in",
      "port": 10808,
      "listen": "127.0.0.1",
      "protocol": "socks",
      "settings": {
        "udp": true
      }
    }
  ],
  "outbounds": [
    {
      "tag": "main-proxy",
      "protocol": "vless",
      "settings": {
        "vnext": [
          {
            "address": "jp.edgegateway.com",
            "port": 443,
            "users": [{"id": "d04a6217-1010-4bfb-bc11-a3c3e210984a", "encryption": "none"}]
          }
        ]
      },
      "streamSettings": {
        "network": "tcp",
        "security": "reality",
        "realitySettings": {
          "serverName": "gateway.icloud.com",
          "publicKey": "K9z1a8B2cC3d4E5f6G7h8I9j0K1l2M3n4O5p6Q7r8S9"
        }
      }
    },
    {
      "tag": "media-proxy",
      "protocol": "vless",
      "settings": {
        "vnext": [
          {
            "address": "sg.streaming.io",
            "port": 443,
            "users": [{"id": "4b689a77-9912-4211-8be3-a128e46920aa", "encryption": "none"}]
          }
        ]
      },
      "streamSettings": {
        "network": "grpc",
        "security": "tls",
        "grpcSettings": {
          "serviceName": "XrayGrpcTunnel"
        }
      }
    },
    {
      "tag": "direct",
      "protocol": "freedom"
    }
  ],
  "routing": {
    "domainStrategy": "IPIfNonMatch",
    "rules": [
      {
        "type": "field",
        "outboundTag": "media-proxy",
        "domain": ["geosite:netflix", "geosite:youtube", "geosite:spotify"]
      },
      {
        "type": "field",
        "outboundTag": "main-proxy",
        "network": "tcp,udp"
      }
    ]
  }
}
        """.trimIndent()
    )

    fun getAllPresets(): List<XrayConfigProfile> = listOf(
        PRESET_BYPASS_CN,
        PRESET_GLOBAL,
        PRESET_DUAL_OUTBOUND
    )
}
