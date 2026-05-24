# 角色与工程规范
你是一位资深的 Android 开发工程师。请帮我从零搭建一个纯本地的「用车成本记录」Android App（MVP 版本）。
我的核心诉求是：代码必须整洁、易维护，遵循单向数据流 (UDF) 和 MVVM 架构。拒绝臃肿的 Activity/Fragment 代码，保持职责分离。

# 技术栈选型
- 语言：Kotlin
- UI 框架：Jetpack Compose（完全摒弃传统的 XML 布局）
- 本地数据库：Room
- 异步与响应式：Kotlin Coroutines + StateFlow
- 架构模式：MVVM

# MVP 核心功能定义
完全单机、无服务器。包含以下功能闭环：
1. 【输入】：极简的费用录入表单（半屏弹窗或 Dialog 即可），包含：金额（必填，数字键盘）、分类（单选：加油、充电、洗车、保养、保险、其他）、日期（默认当天，支持修改）、备注（可选）。
2. 【计算】：统计“本月总花销”。
3. 【展示】：首页顶部显示“本月总花销”的数字看板，下方以列表形式按时间倒序平铺展示所有记账流水。

# 工作流指令（请严格按以下步骤拆解执行，每完成一步请停下来等我确认）

## 第一步：项目脚手架与依赖配置
1. 请给出创建该项目的标准终端命令（或 Android Studio 的配置指引）。
2. 提供 `build.gradle.kts` (App 级别) 中需要添加的 Room、Compose 和 ViewModel 核心依赖代码。
3. 输出清晰的目录结构设计树（建议按功能划分，如 feature, data, ui）。

## 第二步：持久化层设计 (Data Layer)
1. 编写 `VehicleCost` Entity（包含 id, amount, category, date, note 等字段）。
2. 编写 `CostDao` 接口，必须包含三个核心 SQL 方法：插入一条记录、查询所有流水（按日期降序）、查询指定月份的总金额聚合。
3. 构建 `AppDatabase` 类完成 Room 数据库的初始化配置。

## 第三步：状态与逻辑管理 (ViewModel Layer)
1. 编写 `CostViewModel`，通过 `StateFlow` 向 UI 层暴露数据状态（当前月总花销金额、流水列表）。
2. 编写添加新账单的业务方法。

## 第四步：UI 渲染 (Presentation Layer)
1. 用 Jetpack Compose 编写首页 `HomeScreen`，上方放置统计卡片，下方放置 `LazyColumn` 流水列表。
2. 编写首页右下角的 FAB（悬浮按钮），点击后弹出录入表单。
3. 编写 `AddCostDialog`（或 BottomSheet）实现完整的录入交互。

## 第五步：组装与运行验证
1. 在 `MainActivity` 中组装上述组件，跑通整体链路。
2. 告诉我编译运行该 MVP 版本的注意事项。