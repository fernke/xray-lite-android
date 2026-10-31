package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.model.ProxyNode
import com.example.model.RoutingMode
import com.example.model.TrafficStats
import com.example.model.VpnState
import com.example.model.XrayConfigProfile
import com.example.service.LatencyTester
import com.example.service.LogManager
import com.example.service.VpnController
import com.example.xray.VlessLinkParser
import com.example.xray.XrayConfigGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val proxyNodeDao = db.proxyNodeDao()
    private val configProfileDao = db.configProfileDao()

    val nodes: StateFlow<List<ProxyNode>> = proxyNodeDao.getAllNodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedNode: StateFlow<ProxyNode?> = proxyNodeDao.getSelectedNodeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val customProfiles: StateFlow<List<XrayConfigProfile>> = configProfileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<XrayConfigProfile?> = configProfileDao.getActiveProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val vpnState: StateFlow<VpnState> = VpnController.vpnState
    val trafficStats: StateFlow<TrafficStats> = VpnController.trafficStats
    val routingMode: StateFlow<RoutingMode> = VpnController.routingMode
    val logs = LogManager.logs

    private val _isTestingLatency = MutableStateFlow(false)
    val isTestingLatency: StateFlow<Boolean> = _isTestingLatency.asStateFlow()

    private val _testingNodeIds = MutableStateFlow<Set<Long>>(emptySet())
    val testingNodeIds: StateFlow<Set<Long>> = _testingNodeIds.asStateFlow()

    private val _testingProfileIds = MutableStateFlow<Set<Long>>(emptySet())
    val testingProfileIds: StateFlow<Set<Long>> = _testingProfileIds.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        // Log app startup
        LogManager.i("System", "Xray Lite 客户端已就绪，核心协议版本兼容 Xray-core v1.8+")
    }

    fun selectNode(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            proxyNodeDao.clearSelection()
            proxyNodeDao.setNodeSelected(node.id)
            VpnController.setActiveNode(node)
            LogManager.i("NodeManager", "已切换当前活动节点至: ${node.name} (${node.address}:${node.port})")
        }
    }

    fun saveNode(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            if (node.id == 0L) {
                // Check if this is the first node
                val count = proxyNodeDao.getCount()
                val isFirst = count == 0
                val nodeToInsert = if (isFirst) node.copy(isSelected = true) else node
                val id = proxyNodeDao.insert(nodeToInsert)
                if (isFirst) {
                    proxyNodeDao.setNodeSelected(id)
                }
                _userMessage.emit("成功添加节点: ${node.name}")
                LogManager.i("NodeManager", "新增节点: ${node.name} [${node.protocol.uppercase()}]")
            } else {
                proxyNodeDao.update(node)
                _userMessage.emit("已更新节点配置: ${node.name}")
                LogManager.i("NodeManager", "更新节点: ${node.name}")
            }
        }
    }

    fun deleteNode(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            proxyNodeDao.delete(node)
            _userMessage.emit("已删除节点: ${node.name}")
            LogManager.w("NodeManager", "已移除节点: ${node.name}")

            // If deleted was selected, select the first remaining
            if (node.isSelected) {
                val remaining = proxyNodeDao.getAllNodes()
                // Take one
                val list = withContext(Dispatchers.IO) {
                    val current = proxyNodeDao.getSelectedNode()
                    if (current == null) {
                        // select any
                    }
                }
            }
        }
    }

    /**
     * Import nodes or raw JSON config from clipboard text.
     */
    fun importFromText(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val trimmed = content.trim()
            if (trimmed.isEmpty()) {
                _userMessage.emit("剪贴板内容为空")
                return@launch
            }

            // 1. Try to parse as VLESS links
            val vlessNodes = VlessLinkParser.extractAllVless(trimmed)
            if (vlessNodes.isNotEmpty()) {
                proxyNodeDao.insertAll(vlessNodes)
                _userMessage.emit("成功从剪贴板导入 ${vlessNodes.size} 个 VLESS 节点")
                LogManager.i("Import", "从剪贴板解析并导入 ${vlessNodes.size} 个 VLESS 节点")
                return@launch
            }

            // 2. Try single vless link without line break
            val singleNode = VlessLinkParser.parse(trimmed)
            if (singleNode != null) {
                proxyNodeDao.insert(singleNode)
                _userMessage.emit("成功导入 VLESS 节点: ${singleNode.name}")
                LogManager.i("Import", "导入 VLESS 节点: ${singleNode.name}")
                return@launch
            }

            // 3. Try to parse as Xray JSON configuration
            val (isValidJson, errorMsg) = XrayConfigGenerator.validateJson(trimmed)
            if (isValidJson) {
                val newProfile = XrayConfigProfile(
                    name = "自定义导入配置 (${System.currentTimeMillis() % 10000})",
                    description = "从剪贴板导入的 Xray JSON 配置文件",
                    rawJson = XrayConfigGenerator.formatJson(trimmed),
                    isPreset = false
                )
                configProfileDao.insert(newProfile)
                _userMessage.emit("成功导入自定义 Xray JSON 配置文件")
                LogManager.i("Import", "已保存自定义 Xray 配置文件到配置中心")
                return@launch
            }

            _userMessage.emit("剪贴板内容非有效 VLESS 链接或 Xray JSON 配置")
            LogManager.w("Import", "剪贴板解析失败: $errorMsg")
        }
    }

    fun testNodeLatency(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            _testingNodeIds.value = _testingNodeIds.value + node.id
            LogManager.i("Ping", "正在测试节点延迟: ${node.name} (${node.address}:${node.port})...")
            val latency = LatencyTester.testLatency(node.address, node.port)
            proxyNodeDao.updateLatency(node.id, latency, System.currentTimeMillis())
            _testingNodeIds.value = _testingNodeIds.value - node.id
            if (latency >= 0) {
                LogManager.i("Ping", "节点 [${node.name}] 延迟响应: ${latency}ms")
                _userMessage.emit("节点 [${node.name}] 延迟: ${latency}ms")
            } else {
                LogManager.w("Ping", "节点 [${node.name}] 连接超时或无法访问")
                _userMessage.emit("节点 [${node.name}] 连接超时")
            }
        }
    }

    fun testAllNodesLatency() {
        if (_isTestingLatency.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isTestingLatency.value = true
            LogManager.i("Ping", "开始批量检测所有节点连通性与 TCP 握手延迟...")
            val list = nodes.value
            _testingNodeIds.value = list.map { it.id }.toSet()
            for (node in list) {
                val latency = LatencyTester.testLatency(node.address, node.port)
                proxyNodeDao.updateLatency(node.id, latency, System.currentTimeMillis())
                _testingNodeIds.value = _testingNodeIds.value - node.id
            }
            _isTestingLatency.value = false
            _userMessage.emit("全部节点延迟测试完成")
            LogManager.i("Ping", "批量延迟测试完毕，共测试 ${list.size} 个节点")
        }
    }

    fun testProfileLatency(profile: XrayConfigProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            _testingProfileIds.value = _testingProfileIds.value + profile.id
            LogManager.i("Ping", "正在检测配置文件 [${profile.name}] 的出站代理服务器延迟...")
            val result = LatencyTester.testProfileLatency(profile.rawJson)
            if (result != null) {
                val (host, port, latency) = result
                configProfileDao.updateLatency(profile.id, latency, System.currentTimeMillis())
                if (latency >= 0) {
                    LogManager.i("Ping", "配置 [${profile.name}] 代理服务器 $host:$port 延迟响应: ${latency}ms")
                    _userMessage.emit("配置 [${profile.name}] 延迟: ${latency}ms")
                } else {
                    LogManager.w("Ping", "配置 [${profile.name}] 代理服务器 $host:$port 超时或不可达")
                    _userMessage.emit("配置 [${profile.name}] 连接超时")
                }
            } else {
                configProfileDao.updateLatency(profile.id, -2L, System.currentTimeMillis())
                LogManager.w("Ping", "配置 [${profile.name}] 未找到有效代理出站服务器配置")
                _userMessage.emit("配置 [${profile.name}] 无有效出站配置")
            }
            _testingProfileIds.value = _testingProfileIds.value - profile.id
        }
    }

    fun testAllProfilesLatency() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = customProfiles.value
            if (list.isEmpty()) return@launch
            LogManager.i("Ping", "开始批量检测全部 ${list.size} 个配置文件的网络延迟...")
            _testingProfileIds.value = list.map { it.id }.toSet()
            for (profile in list) {
                val result = LatencyTester.testProfileLatency(profile.rawJson)
                if (result != null) {
                    val (_, _, latency) = result
                    configProfileDao.updateLatency(profile.id, latency, System.currentTimeMillis())
                } else {
                    configProfileDao.updateLatency(profile.id, -2L, System.currentTimeMillis())
                }
                _testingProfileIds.value = _testingProfileIds.value - profile.id
            }
            _userMessage.emit("全部配置文件延迟检测完成")
            LogManager.i("Ping", "全部配置文件延迟检测完毕")
        }
    }

    fun setRoutingMode(mode: RoutingMode) {
        VpnController.setRoutingMode(mode)
        viewModelScope.launch {
            _userMessage.emit("已切换路由模式: ${mode.title}")
            LogManager.i("Routing", "全局路由规则变更为: ${mode.title}")
        }
    }

    fun toggleVpn(context: Context) {
        val currentState = vpnState.value
        if (currentState == VpnState.CONNECTED || currentState == VpnState.CONNECTING) {
            VpnController.stop(context)
        } else {
            val active = selectedNode.value
            if (active == null) {
                viewModelScope.launch {
                    _userMessage.emit("请先选择一个代理节点")
                }
                return
            }
            VpnController.start(context, active)
        }
    }

    fun saveConfigProfile(profile: XrayConfigProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (profile.id == 0L) {
                configProfileDao.insert(profile)
                _userMessage.emit("成功新建配置文件: ${profile.name}")
                LogManager.i("Config", "新增自定义配置文件: ${profile.name}")
            } else {
                configProfileDao.update(profile)
                _userMessage.emit("已更新配置文件: ${profile.name}")
                LogManager.i("Config", "更新配置文件: ${profile.name}")
            }
        }
    }

    fun deleteConfigProfile(profile: XrayConfigProfile) {
        if (profile.isPreset) {
            viewModelScope.launch { _userMessage.emit("内置预设配置不支持删除") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            configProfileDao.delete(profile)
            _userMessage.emit("已删除配置文件: ${profile.name}")
            LogManager.w("Config", "已删除配置文件: ${profile.name}")
        }
    }

    fun activateConfigProfile(profile: XrayConfigProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            configProfileDao.clearActive()
            configProfileDao.setActiveProfile(profile.id)
            _userMessage.emit("已激活配置文件: ${profile.name}")
            LogManager.i("Config", "已切换核心加载配置为: ${profile.name}")
        }
    }

    fun getActiveGeneratedJson(): String {
        val node = selectedNode.value
        return if (node != null) {
            XrayConfigGenerator.generateConfig(node, routingMode.value)
        } else {
            "{}"
        }
    }

    fun clearLogs() {
        LogManager.clear()
    }
}
