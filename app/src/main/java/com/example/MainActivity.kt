package com.example

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProxyNode
import com.example.model.XrayConfigProfile
import com.example.ui.dialogs.ConfigEditorDialog
import com.example.ui.dialogs.ImportDialog
import com.example.ui.dialogs.NodeEditDialog
import com.example.ui.screens.ConfigScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.NodesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.MainViewModel
import com.example.xray.XrayConfigGenerator

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppShell(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppShell(viewModel: MainViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showImportDialog by remember { mutableStateOf(false) }
    var editingNode by remember { mutableStateOf<ProxyNode?>(null) }
    var showAddNodeDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<XrayConfigProfile?>(null) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var viewingNodeJson by remember { mutableStateOf<ProxyNode?>(null) }

    // Listen for ViewModel messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Permission launcher for Android 13+ Notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // BackHandler: when not on dashboard, back navigates to Dashboard
    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Slate900,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Slate850,
                contentColor = CyanNeon,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "仪表盘") },
                    label = { Text("仪表盘", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "节点") },
                    label = { Text("节点列表", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "配置") },
                    label = { Text("配置中心", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "日志") },
                    label = { Text("实时日志", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                    label = { Text("设置", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToNodes = { currentTab = 1 },
                    onNavigateToConfig = { currentTab = 2 },
                    onOpenImportDialog = { showImportDialog = true }
                )
                1 -> NodesScreen(
                    viewModel = viewModel,
                    onAddNode = { showAddNodeDialog = true },
                    onEditNode = { editingNode = it },
                    onOpenImportDialog = { showImportDialog = true },
                    onViewNodeJson = { viewingNodeJson = it }
                )
                2 -> ConfigScreen(
                    viewModel = viewModel,
                    onCreateProfile = { showCreateProfileDialog = true },
                    onEditProfile = { editingProfile = it }
                )
                3 -> LogsScreen(
                    viewModel = viewModel
                )
                4 -> SettingsScreen()
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        ImportDialog(
            onDismiss = { showImportDialog = false },
            onConfirmImport = { text ->
                viewModel.importFromText(text)
            }
        )
    }

    // Add or Edit Node Dialog
    if (showAddNodeDialog) {
        NodeEditDialog(
            initialNode = null,
            onDismiss = { showAddNodeDialog = false },
            onSave = { node ->
                viewModel.saveNode(node)
            }
        )
    }

    editingNode?.let { node ->
        NodeEditDialog(
            initialNode = node,
            onDismiss = { editingNode = null },
            onSave = { updated ->
                viewModel.saveNode(updated)
            }
        )
    }

    // Create or Edit Config Profile Dialog
    if (showCreateProfileDialog) {
        ConfigEditorDialog(
            initialProfile = null,
            onDismiss = { showCreateProfileDialog = false },
            onSave = { profile ->
                viewModel.saveConfigProfile(profile)
            }
        )
    }

    editingProfile?.let { profile ->
        ConfigEditorDialog(
            initialProfile = profile,
            onDismiss = { editingProfile = null },
            onSave = { updated ->
                viewModel.saveConfigProfile(updated)
            }
        )
    }

    // View Node JSON Dialog
    viewingNodeJson?.let { node ->
        val nodeJson = remember(node) {
            XrayConfigGenerator.generateConfig(node)
        }
        AlertDialog(
            onDismissRequest = { viewingNodeJson = null },
            containerColor = Slate900,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "${node.name} - Xray 配置",
                    color = Slate100,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Slate950, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = nodeJson,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = CyanNeon
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Xray Config", nodeJson))
                        Toast.makeText(context, "已复制配置到剪贴板", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate900)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("复制配置", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewingNodeJson = null }) {
                    Text("关闭", color = Slate400)
                }
            }
        )
    }
}
