@file:Suppress("FunctionNaming", "LongMethod")

package com.danielealbano.androidremotecontrolmcp.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danielealbano.androidremotecontrolmcp.R
import com.danielealbano.androidremotecontrolmcp.data.model.OptionalToolPermission
import com.danielealbano.androidremotecontrolmcp.data.model.OptionalToolPermissions
import com.danielealbano.androidremotecontrolmcp.data.model.ServerStatus
import com.danielealbano.androidremotecontrolmcp.data.model.ToolPermissionsConfig
import com.danielealbano.androidremotecontrolmcp.ui.theme.WarningAmber
import com.danielealbano.androidremotecontrolmcp.ui.viewmodels.MainViewModel

private data class ParamEntry(
    val paramName: String,
    val displayName: String,
)

private data class ToolEntry(
    val toolName: String,
    val displayName: String,
    val params: List<ParamEntry> = emptyList(),
)

private data class ToolCategory(
    val header: String,
    val tools: List<ToolEntry>,
)

@Composable
private fun toolCategories(context: android.content.Context): List<ToolCategory> =
    listOf(
        ToolCategory(
            context.getString(R.string.mcp_tools_category_screen),
            listOf(
                ToolEntry(
                    "get_screen_state",
                    context.getString(R.string.mcp_tool_get_screen_state),
                    listOf(ParamEntry("include_screenshot", context.getString(R.string.mcp_tool_param_include_screenshot))),
                ),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_system),
            listOf(
                ToolEntry("press_back", context.getString(R.string.mcp_tool_press_back)),
                ToolEntry("press_home", context.getString(R.string.mcp_tool_press_home)),
                ToolEntry("press_recents", context.getString(R.string.mcp_tool_press_recents)),
                ToolEntry("open_notifications", context.getString(R.string.mcp_tool_open_notifications)),
                ToolEntry("open_quick_settings", context.getString(R.string.mcp_tool_open_quick_settings)),
                ToolEntry("dismiss_keyboard", context.getString(R.string.mcp_tool_dismiss_keyboard)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_touch),
            listOf(
                ToolEntry("tap", context.getString(R.string.mcp_tool_tap)),
                ToolEntry("long_press", context.getString(R.string.mcp_tool_long_press)),
                ToolEntry("double_tap", context.getString(R.string.mcp_tool_double_tap)),
                ToolEntry("swipe", context.getString(R.string.mcp_tool_swipe)),
                ToolEntry("scroll", context.getString(R.string.mcp_tool_scroll)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_gestures),
            listOf(
                ToolEntry("pinch", context.getString(R.string.mcp_tool_pinch)),
                ToolEntry("custom_gesture", context.getString(R.string.mcp_tool_custom_gesture)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_node_actions),
            listOf(
                ToolEntry("find_nodes", context.getString(R.string.mcp_tool_find_nodes)),
                ToolEntry("click_node", context.getString(R.string.mcp_tool_click_node)),
                ToolEntry("long_click_node", context.getString(R.string.mcp_tool_long_click_node)),
                ToolEntry("tap_node", context.getString(R.string.mcp_tool_tap_node)),
                ToolEntry("scroll_to_node", context.getString(R.string.mcp_tool_scroll_to_node)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_text_input),
            listOf(
                ToolEntry("type_append_text", context.getString(R.string.mcp_tool_type_append_text)),
                ToolEntry("type_insert_text", context.getString(R.string.mcp_tool_type_insert_text)),
                ToolEntry("type_replace_text", context.getString(R.string.mcp_tool_type_replace_text)),
                ToolEntry("type_clear_text", context.getString(R.string.mcp_tool_type_clear_text)),
                ToolEntry("press_key", context.getString(R.string.mcp_tool_press_key)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_utility),
            listOf(
                ToolEntry("get_clipboard", context.getString(R.string.mcp_tool_get_clipboard)),
                ToolEntry("set_clipboard", context.getString(R.string.mcp_tool_set_clipboard)),
                ToolEntry("wait_for_node", context.getString(R.string.mcp_tool_wait_for_node)),
                ToolEntry("wait_for_idle", context.getString(R.string.mcp_tool_wait_for_idle)),
                ToolEntry("get_node_details", context.getString(R.string.mcp_tool_get_node_details)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_file_operations),
            listOf(
                ToolEntry("list_storage_locations", context.getString(R.string.mcp_tool_list_storage_locations)),
                ToolEntry("list_files", context.getString(R.string.mcp_tool_list_files)),
                ToolEntry("read_file", context.getString(R.string.mcp_tool_read_file)),
                ToolEntry("write_file", context.getString(R.string.mcp_tool_write_file)),
                ToolEntry("append_file", context.getString(R.string.mcp_tool_append_file)),
                ToolEntry("file_replace", context.getString(R.string.mcp_tool_file_replace)),
                ToolEntry("download_from_url", context.getString(R.string.mcp_tool_download_from_url)),
                ToolEntry("delete_file", context.getString(R.string.mcp_tool_delete_file)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_app_management),
            listOf(
                ToolEntry("open_app", context.getString(R.string.mcp_tool_open_app)),
                ToolEntry("list_apps", context.getString(R.string.mcp_tool_list_apps)),
                ToolEntry("close_app", context.getString(R.string.mcp_tool_close_app)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_camera),
            listOf(
                ToolEntry("list_cameras", context.getString(R.string.mcp_tool_list_cameras)),
                ToolEntry("list_camera_photo_resolutions", context.getString(R.string.mcp_tool_list_camera_photo_resolutions)),
                ToolEntry("list_camera_video_resolutions", context.getString(R.string.mcp_tool_list_camera_video_resolutions)),
                ToolEntry("take_camera_photo", context.getString(R.string.mcp_tool_take_camera_photo)),
                ToolEntry("save_camera_photo", context.getString(R.string.mcp_tool_save_camera_photo)),
                ToolEntry(
                    "save_camera_video",
                    context.getString(R.string.mcp_tool_save_camera_video),
                    listOf(ParamEntry("audio", context.getString(R.string.mcp_tool_param_audio))),
                ),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_intent),
            listOf(
                ToolEntry("send_intent", context.getString(R.string.mcp_tool_send_intent)),
                ToolEntry("open_uri", context.getString(R.string.mcp_tool_open_uri)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_notifications),
            listOf(
                ToolEntry("notification_list", context.getString(R.string.mcp_tool_notification_list)),
                ToolEntry("notification_open", context.getString(R.string.mcp_tool_notification_open)),
                ToolEntry("notification_dismiss", context.getString(R.string.mcp_tool_notification_dismiss)),
                ToolEntry("notification_snooze", context.getString(R.string.mcp_tool_notification_snooze)),
                ToolEntry("notification_action", context.getString(R.string.mcp_tool_notification_action)),
                ToolEntry("notification_reply", context.getString(R.string.mcp_tool_notification_reply)),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_location),
            listOf(
                ToolEntry(
                    "get_location",
                    context.getString(R.string.mcp_tool_get_location),
                    listOf(ParamEntry("fresh_fix", context.getString(R.string.mcp_tool_param_fresh_fix))),
                ),
            ),
        ),
        ToolCategory(
            context.getString(R.string.mcp_tools_category_sharing),
            listOf(
                ToolEntry("get_shared_content", context.getString(R.string.mcp_tool_get_shared_content)),
                ToolEntry("share_file_via_web", context.getString(R.string.mcp_tool_share_file_via_web)),
            ),
        ),
    )

private val ALL_TOOL_CATEGORIES: List<ToolCategory> = emptyList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpToolsSettingsScreen(
    onBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val toolCategories = toolCategories(context)
    val lifecycleOwner = LocalLifecycleOwner.current
    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()
    val perms by viewModel.toolPermissionsConfig.collectAsStateWithLifecycle()
    val cameraGranted by viewModel.isCameraPermissionGranted.collectAsStateWithLifecycle()
    val locationGranted by viewModel.isLocationPermissionGranted.collectAsStateWithLifecycle()
    val notificationListenerGranted by viewModel.isNotificationListenerEnabled.collectAsStateWithLifecycle()
    val microphoneGranted by viewModel.isMicrophonePermissionGranted.collectAsStateWithLifecycle()
    val controlsEnabled = serverStatus !is ServerStatus.Running && serverStatus !is ServerStatus.Starting

    // Refresh permissions on ON_RESUME — SAME pattern as PermissionsSettingsScreen.
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshPermissionStatus(context)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isGranted: (OptionalToolPermission) -> Boolean = { permission ->
        isPermissionGranted(
            permission = permission,
            camera = cameraGranted,
            location = locationGranted,
            notificationListener = notificationListenerGranted,
            microphone = microphoneGranted,
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_mcp_tools_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
            windowInsets = WindowInsets(0),
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text(
                    text = stringResource(R.string.mcp_tools_restart_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            toolCategories.forEach { category ->
                val categoryPermissions =
                    category.tools.mapNotNull { OptionalToolPermissions.permissionForTool(it.toolName) }.distinct()
                item(key = "header_${category.header}") {
                    ToolCategoryHeader(
                        header = category.header,
                        missingPermission = categoryPermissions.any { !isGranted(it) },
                        onNavigateToPermissions = onNavigateToPermissions,
                    )
                }
                items(category.tools, key = { it.toolName }) { tool ->
                    ToolRow(
                        tool = tool,
                        perms = perms,
                        controlsEnabled = controlsEnabled,
                        categoryGranted = categoryPermissions.all { isGranted(it) },
                        isGranted = isGranted,
                        onNavigateToPermissions = onNavigateToPermissions,
                        onToolToggle = viewModel::updateToolEnabled,
                        onParamToggle = viewModel::updateParamEnabled,
                    )
                }
            }
        }
    }
}

/** Resolves whether an optional permission is granted from the individual permission flags. */
private fun isPermissionGranted(
    permission: OptionalToolPermission,
    camera: Boolean,
    location: Boolean,
    notificationListener: Boolean,
    microphone: Boolean,
): Boolean =
    when (permission) {
        OptionalToolPermission.CAMERA -> camera
        OptionalToolPermission.LOCATION -> location
        OptionalToolPermission.NOTIFICATION_LISTENER -> notificationListener
        OptionalToolPermission.MICROPHONE -> microphone
    }

@Composable
private fun ToolCategoryHeader(
    header: String,
    missingPermission: Boolean,
    onNavigateToPermissions: () -> Unit,
) {
    // Match the warning triangle to the header text size (respects font scaling).
    val warningIconSize =
        with(LocalDensity.current) {
            MaterialTheme.typography.titleMedium.fontSize
                .toDp()
        }
    if (!missingPermission) {
        Text(
            text = header,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        )
        return
    }
    Column(modifier = Modifier.clickable { onNavigateToPermissions() }) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = header,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp),
            )
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(warningIconSize),
            )
        }
        Text(
            text = stringResource(R.string.settings_mcp_tools_missing_permission),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        )
    }
}

@Composable
private fun ToolRow(
    tool: ToolEntry,
    perms: ToolPermissionsConfig,
    controlsEnabled: Boolean,
    categoryGranted: Boolean,
    isGranted: (OptionalToolPermission) -> Boolean,
    onNavigateToPermissions: () -> Unit,
    onToolToggle: (String, Boolean) -> Unit,
    onParamToggle: (String, String, Boolean) -> Unit,
) {
    val toolEnabled = perms.isToolEnabled(tool.toolName)
    ListItem(
        headlineContent = { Text(tool.displayName) },
        trailingContent = {
            Switch(
                checked = toolEnabled,
                onCheckedChange = { onToolToggle(tool.toolName, it) },
                enabled = controlsEnabled && categoryGranted,
            )
        },
    )
    if (toolEnabled) {
        tool.params.forEach { param ->
            ToolParamRow(
                toolName = tool.toolName,
                param = param,
                perms = perms,
                controlsEnabled = controlsEnabled,
                categoryGranted = categoryGranted,
                isGranted = isGranted,
                onNavigateToPermissions = onNavigateToPermissions,
                onParamToggle = onParamToggle,
            )
        }
    }
}

@Composable
private fun ToolParamRow(
    toolName: String,
    param: ParamEntry,
    perms: ToolPermissionsConfig,
    controlsEnabled: Boolean,
    categoryGranted: Boolean,
    isGranted: (OptionalToolPermission) -> Boolean,
    onNavigateToPermissions: () -> Unit,
    onParamToggle: (String, String, Boolean) -> Unit,
) {
    val paramPermission = OptionalToolPermissions.permissionForParam(toolName, param.paramName)
    val paramGranted = paramPermission == null || isGranted(paramPermission)
    val showParamNote = categoryGranted && !paramGranted
    val leadingIcon: (@Composable () -> Unit)? =
        if (showParamNote) {
            { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarningAmber) }
        } else {
            null
        }
    val supportingNote: (@Composable () -> Unit)? =
        if (showParamNote) {
            {
                Text(
                    text = stringResource(R.string.settings_mcp_tools_param_missing_permission),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            null
        }
    ListItem(
        headlineContent = { Text(param.displayName) },
        modifier =
            if (showParamNote) {
                Modifier.padding(start = 32.dp).clickable { onNavigateToPermissions() }
            } else {
                Modifier.padding(start = 32.dp)
            },
        leadingContent = leadingIcon,
        supportingContent = supportingNote,
        trailingContent = {
            Switch(
                checked = perms.isParamEnabled(toolName, param.paramName),
                onCheckedChange = { onParamToggle(toolName, param.paramName, it) },
                enabled = controlsEnabled && categoryGranted && paramGranted,
            )
        },
    )
}
