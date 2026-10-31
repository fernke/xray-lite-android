package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.ProxyNode
import com.example.model.XrayConfigProfile
import com.example.xray.PresetConfigs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ProxyNode::class, XrayConfigProfile::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun proxyNodeDao(): ProxyNodeDao
    abstract fun configProfileDao(): ConfigProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xray_lite.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getDatabase(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val starterNodes = listOf(
                ProxyNode(
                    name = "香港 01 | BGP 专线 Reality",
                    protocol = "vless",
                    address = "hk01.gateway.edge",
                    port = 443,
                    uuid = "7d4b68e2-9012-4211-8be3-a128e46920aa",
                    encryption = "none",
                    flow = "xtls-rprx-vision",
                    network = "tcp",
                    security = "reality",
                    sni = "www.cloudflare.com",
                    fingerprint = "chrome",
                    publicKey = "7y9B0gQzLgN3E3M-q2N6F5_d6W9aB7c4D2e1F0g8H6j",
                    shortId = "6ba855d1",
                    spiderX = "/",
                    isSelected = true,
                    latencyMs = 38L,
                    lastTestedTime = System.currentTimeMillis()
                ),
                ProxyNode(
                    name = "日本 东京 | CN2 GIA Reality",
                    protocol = "vless",
                    address = "jp-tyo.nodeserver.io",
                    port = 443,
                    uuid = "9e5c4120-77a8-4bb9-95de-64359218adbc",
                    encryption = "none",
                    flow = "xtls-rprx-vision",
                    network = "tcp",
                    security = "reality",
                    sni = "gateway.icloud.com",
                    fingerprint = "chrome",
                    publicKey = "K9z1a8B2cC3d4E5f6G7h8I9j0K1l2M3n4O5p6Q7r8S9",
                    shortId = "8a3e01bc",
                    spiderX = "/",
                    isSelected = false,
                    latencyMs = 65L,
                    lastTestedTime = System.currentTimeMillis()
                ),
                ProxyNode(
                    name = "新加坡 01 | 极速流媒体 WebSocket+TLS",
                    protocol = "vless",
                    address = "sg01.faststream.net",
                    port = 443,
                    uuid = "c3182b54-1590-41da-b570-56e6d1e4e555",
                    encryption = "none",
                    flow = "",
                    network = "ws",
                    security = "tls",
                    sni = "sg01.faststream.net",
                    fingerprint = "chrome",
                    path = "/vless-ws",
                    host = "sg01.faststream.net",
                    isSelected = false,
                    latencyMs = 78L,
                    lastTestedTime = System.currentTimeMillis()
                ),
                ProxyNode(
                    name = "美国 洛杉矶 | Anycast gRPC",
                    protocol = "vless",
                    address = "us-lax.cloudnode.org",
                    port = 443,
                    uuid = "d04a6217-1010-4bfb-bc11-a3c3e210984a",
                    encryption = "none",
                    flow = "",
                    network = "grpc",
                    security = "tls",
                    sni = "us-lax.cloudnode.org",
                    fingerprint = "firefox",
                    serviceName = "XrayGrpcService",
                    isSelected = false,
                    latencyMs = 152L,
                    lastTestedTime = System.currentTimeMillis()
                )
            )
            db.proxyNodeDao().insertAll(starterNodes)

            // Preset Config Profiles
            db.configProfileDao().insertAll(PresetConfigs.getAllPresets())
        }
    }
}
