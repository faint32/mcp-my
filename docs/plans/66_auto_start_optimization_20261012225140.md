<!-- SACRED DOCUMENT — DO NOT MODIFY except for checkmarks ([ ] → [x]) and review findings. -->
<!-- You MUST NEVER alter, revert, or delete files outside the scope of this plan. -->
<!-- Plans in docs/plans/ are PERMANENT artifacts. There are ZERO exceptions. -->

# Plan 66: 自动启动优化（开机自启 + 一键启动）

**创建时间**: 2026-10-12 22:51:40  
**目标**: 实现手机重启后自动运行 MCP 服务，优化权限引导流程

---

## 概述

当前项目已经实现了开机自动启动的基础功能（`BootCompletedReceiver` + `autoStartOnBoot` 配置），但存在以下问题：
1. **自动启动默认关闭** — `autoStartOnBoot = false`，用户需要手动开启
2. **权限提示不够明显** — 用户可能不知道需要授予哪些权限
3. **缺少一键启动功能** — 需要用户手动检查并授予每个权限

本计划实现**方案 A（轻量级改进）**：
- 将 `autoStartOnBoot` 默认改为 `true`
- 优化权限提示 UI，更清晰地引导用户
- 添加"一键启动"按钮，检测所有权限状态并引导用户授权

---

## User Story 1: 默认启用开机自动启动

**目的**: 首次安装后，默认开启开机自动启动，用户重启手机后无需手动操作即可运行 MCP 服务。

**Acceptance Criteria**:
- [ ] `ServerConfig.autoStartOnBoot` 默认值改为 `true`
- [ ] 现有用户升级后保持原有设置（不改变已设置的值）
- [ ] 单元测试验证默认值

---

### Task 1.1: 修改 ServerConfig 默认值

**File**: `app/src/main/kotlin/com/danielealbano/androidremotecontrolmcp/data/model/ServerConfig.kt`

**Operation**: Modify

```kotlin
data class ServerConfig(
    val port: Int = DEFAULT_PORT,
    val bindingAddress: BindingAddress = BindingAddress.LOCALHOST,
    val bearerToken: String = "",
    val autoStartOnBoot: Boolean = true, // 改为 true
    val toolCallIndicatorEnabled: Boolean = false,
    // ... 其他字段保持不变
)
```

**Definition of Done**:
- [ ] 默认值已修改
- [ ] 代码编译通过

---

### Task 1.2: 更新单元测试

**File**: `app/src/test/kotlin/com/danielealbano/androidremotecontrolmcp/data/model/ServerConfigTest.kt`

**Operation**: Modify

更新测试验证新的默认值：

```kotlin
@Test
fun `default autoStartOnBoot should be true`() {
    val config = ServerConfig()
    assertTrue(config.autoStartOnBoot)
}
```

**Definition of Done**:
- [ ] 测试已更新
- [ ] 测试通过

---

## User Story 2: 优化权限提示 UI

**目的**: 让用户清楚地知道需要授予哪些权限才能正常使用 MCP 服务，提供更明显的视觉提示和引导。

**Acceptance Criteria**:
- [ ] 必需权限（无障碍服务）与可选权限（相机、麦克风、定位）有明确区分
- [ ] 未授予的必需权限显示警告样式（红色/橙色）
- [ ] 权限卡片包含简短的说明文字
- [ ] UI 响应式更新权限状态

---

### Task 2.1: 增强 PermissionsSection 组件

**File**: `app/src/main/kotlin/com/danielealbano/androidremotecontrolmcp/ui/components/PermissionsSection.kt`

**Operation**: Modify

优化权限卡片 UI：
- 必需权限使用 `Card` 组件，未授予时显示警告色
- 可选权限使用 `OutlinedCard`
- 添加权限说明文字（从 `strings.xml` 读取）
- 添加权限状态图标（勾选/叉号）

**Definition of Done**:
- [ ] UI 已优化
- [ ] 必需/可选权限有视觉区分
- [ ] 代码符合 Compose 最佳实践

---

### Task 2.2: 添加权限说明字符串资源

**File**: `app/src/main/res/values/strings.xml`

**Operation**: Modify

添加权限说明文字：

```xml
<string name="permission_accessibility_required">必需：用于控制设备 UI 和截屏</string>
<string name="permission_notification_optional">可选：用于前台服务通知</string>
<string name="permission_camera_optional">可选：用于相机拍照和录像工具</string>
<string name="permission_microphone_optional">可选：用于录音功能</string>
<string name="permission_location_optional">可选：用于定位工具</string>
<string name="permission_notification_listener_optional">可选：用于通知管理工具</string>
```

**File**: `app/src/main/res/values-zh/strings.xml`

**Operation**: Modify

同步添加中文字符串（已在上面定义）。

**Definition of Done**:
- [ ] 字符串资源已添加
- [ ] 英文和中文资源文件都已更新

---

## User Story 3: 添加"一键启动"功能

**目的**: 提供一个"一键启动"按钮，检测所有权限状态，未授予的自动引导用户跳转到设置页面，所有必需权限授予后自动启动 MCP 服务。

**Acceptance Criteria**:
- [ ] HomeScreen 添加"一键启动"按钮
- [ ] 点击按钮时检测必需权限（无障碍服务）
- [ ] 如果未授予，显示对话框说明并引导跳转到设置
- [ ] 如果已授予，自动启动 MCP 服务
- [ ] 按钮状态根据服务运行状态自适应（运行时显示"停止服务"）

---

### Task 3.1: 添加权限检查逻辑到 MainViewModel

**File**: `app/src/main/kotlin/com/danielealbano/androidremotecontrolmcp/ui/viewmodels/MainViewModel.kt`

**Operation**: Modify

添加方法 `checkAndStartServer()`:

```kotlin
fun checkAndStartServer(context: Context): QuickStartResult {
    val accessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(
        context,
        McpAccessibilityService::class.java
    )
    
    return if (accessibilityEnabled) {
        startServer()
        QuickStartResult.Started
    } else {
        QuickStartResult.MissingAccessibility
    }
}

sealed class QuickStartResult {
    object Started : QuickStartResult()
    object MissingAccessibility : QuickStartResult()
}
```

**Definition of Done**:
- [ ] 方法已添加
- [ ] 逻辑正确
- [ ] 代码编译通过

---

### Task 3.2: 在 HomeScreen 添加"一键启动"按钮

**File**: `app/src/main/kotlin/com/danielealbano/androidremotecontrolmcp/ui/screens/HomeScreen.kt`

**Operation**: Modify

在 `ServerStatusCard` 下方添加醒目的"一键启动"按钮：

```kotlin
// 在服务未运行时显示
if (serverStatus !is ServerStatus.Running) {
    Button(
        onClick = { /* 处理点击 */ },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("一键启动 MCP 服务")
    }
}
```

点击时调用 `viewModel.checkAndStartServer(context)`，根据结果显示对话框或直接启动。

**Definition of Done**:
- [ ] 按钮已添加
- [ ] 点击逻辑正确
- [ ] UI 符合 Material Design 3

---

### Task 3.3: 添加权限引导对话框

**File**: `app/src/main/kotlin/com/danielealbano/androidremotecontrolmcp/ui/screens/HomeScreen.kt`

**Operation**: Modify

添加 `AlertDialog` 组件，当检测到缺少必需权限时显示：

```kotlin
if (showPermissionDialog) {
    AlertDialog(
        onDismissRequest = { showPermissionDialog = false },
        title = { Text("需要授予无障碍权限") },
        text = { Text("MCP 服务需要无障碍权限才能控制设备。点击"去设置"跳转到系统设置页面。") },
        confirmButton = {
            TextButton(onClick = {
                PermissionUtils.openAccessibilitySettings(context)
                showPermissionDialog = false
            }) {
                Text("去设置")
            }
        },
        dismissButton = {
            TextButton(onClick = { showPermissionDialog = false }) {
                Text("取消")
            }
        }
    )
}
```

**Definition of Done**:
- [ ] 对话框已添加
- [ ] 文案清晰
- [ ] 跳转逻辑正确

---

### Task 3.4: 添加字符串资源

**File**: `app/src/main/res/values/strings.xml`

**Operation**: Modify

```xml
<string name="quick_start_button">一键启动 MCP 服务</string>
<string name="quick_start_dialog_title">需要授予无障碍权限</string>
<string name="quick_start_dialog_message">MCP 服务需要无障碍权限才能控制设备。点击"去设置"跳转到系统设置页面。</string>
<string name="go_to_settings">去设置</string>
```

**File**: `app/src/main/res/values-zh/strings.xml`

**Operation**: Modify

同步添加中文字符串（已在上面定义）。

**Definition of Done**:
- [ ] 字符串资源已添加

---

## User Story 4: 更新文档

**目的**: 更新项目文档，说明新的自动启动默认行为和一键启动功能。

**Acceptance Criteria**:
- [ ] `PROJECT.md` 更新默认配置说明
- [ ] `README.md` 更新使用说明（如果需要）

---

### Task 4.1: 更新 PROJECT.md

**File**: `docs/PROJECT.md`

**Operation**: Modify

在 "Default Configuration" 章节更新：

```markdown
- **Auto-start on Boot**: **Enabled** (default true, can be disabled in settings)
```

**Definition of Done**:
- [ ] 文档已更新

---

## User Story 5: 测试

**目的**: 全面测试自动启动和一键启动功能，确保所有场景正常工作。

**Acceptance Criteria**:
- [ ] 单元测试通过
- [ ] Linting 通过
- [ ] 构建成功
- [ ] 手动测试通过

---

### Task 5.1: 运行单元测试

**Command**: `make test-unit`

**Definition of Done**:
- [ ] 所有单元测试通过

---

### Task 5.2: 运行 Linting

**Command**: `make lint`

**Definition of Done**:
- [ ] 无 linting 错误

---

### Task 5.3: 构建 APK

**Command**: `make build`

**Definition of Done**:
- [ ] 构建成功
- [ ] APK 可安装

---

### Task 5.4: 手动测试

**测试场景**:

| 场景 | 操作 | 预期结果 |
|------|------|----------|
| 首次安装 | 安装 APK 后打开应用 | `autoStartOnBoot` 为 true |
| 未授予无障碍权限 | 点击"一键启动"按钮 | 显示对话框，引导跳转到设置 |
| 已授予无障碍权限 | 点击"一键启动"按钮 | 直接启动 MCP 服务 |
| 开机自动启动 | 授予权限，开启自动启动，重启手机 | 手机启动后 MCP 服务自动运行 |
| 权限 UI 显示 | 打开应用查看权限区域 | 必需权限有视觉区分，说明文字清晰 |

**Definition of Done**:
- [ ] 所有场景测试通过
- [ ] 无明显 bug

---

## 实施顺序

1. User Story 1: 默认启用开机自动启动
2. User Story 2: 优化权限提示 UI
3. User Story 3: 添加"一键启动"功能
4. User Story 4: 更新文档
5. User Story 5: 测试

---

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 用户不希望默认开机自启 | 用户体验 | 提供明显的关闭开关，说明如何禁用 |
| 权限未授予导致服务无法启动 | 功能失效 | 一键启动功能会检测并引导用户授权 |
| UI 改动影响现有用户体验 | 用户适应成本 | 保持现有功能不变，仅添加新功能和优化提示 |

---

**计划创建者**: Claude (Opus 4.8)  
**遵循规范**: CLAUDE.md, PROJECT.md, ARCHITECTURE.md
