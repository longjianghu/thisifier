# Thisifier - IntelliJ IDEA 插件功能说明书

## 1. 概述

- **插件名称**: Thisifier
- **插件类型**: 代码增强工具
- **目标用户**: 主要使用 Java 语言的 IntelliJ IDEA 开发者，尤其是在遵循强调 `this` 关键字编码风格的项目中
- **核心价值**: 通过一次右键点击，快速将当前类内部的方法调用转换为 `this.xxx()` 形式，提升编码效率和代码风格一致性

## 2. 核心功能

### 2.1 上下文感知右键菜单

- 当用户在 Java 文件中右键时，若光标位于一个**当前类的实例方法调用**上
- 右键菜单将显示 **"Add 'this' to method call"** 选项

### 2.2 智能方法转换

- 触发后，插件自动在方法调用前添加 `this.` 前缀
- **转换示例**:
    - `methodName();` → `this.methodName();`
    - `methodName(arg1, arg2);` → `this.methodName(arg1, arg2);`

### 2.3 智能忽略机制

- **已存在 `this`**: 若方法调用已是 `this.xxx()` 形式，菜单项将变为**灰色不可用**
- **处理静态方法**: 若光标位于**静态方法调用**上，菜单项将变为**灰色不可用**
- **处理父类方法**: 对于继承自父类的方法调用，菜单项将变为**灰色不可用**

### 2.4 批量操作支持

- 自动过滤不符合条件的方法调用，只对有效的实例方法进行转换

## 3. 技术特性

### 3.1 性能要求

- 操作响应需迅速，用户无感知延迟
- 代码分析处理应在毫秒级完成

### 3.2 兼容性

- **最低支持版本**: IntelliJ IDEA 2022.3+
- **兼容环境**: Android Studio 等基于 IntelliJ 的 IDE
- **Java 版本**: Java 17

### 3.3 稳定性

- 不得导致 IDE 崩溃或文件损坏
- 所有代码修改必须通过安全的 PSI API 进行
- 支持标准的撤销/重做操作 (Ctrl+Z / Ctrl+Shift+Z)

## 4. 安装与使用

### 4.1 安装方式

1. 在 IntelliJ IDEA 中打开 `Settings` → `Plugins`
2. 搜索 "Thisifier" 并安装
3. 重启 IntelliJ IDEA

### 4.2 使用方法

详细使用说明请参考 [USAGE.md](USAGE.md) 文件。

## 5. 构建与开发

### 5.1 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── sohocn/
│   │           └── thisifier/
│   │               ├── actions/
│   │               │   └── AddThisAction.java
│   │               └── util/
│   │                   └── MethodDetectionUtil.java
│   └── resources/
│       └── META-INF/
│           └── plugin.xml
└── test/
    └── java/
        └── com/
            └── sohocn/
                └── thisifier/
                    └── actions/
                        └── AddThisActionTest.java
```

### 5.2 构建命令

```bash
# 构建插件
./gradlew buildPlugin

# 运行 IDE 测试
./gradlew runIde

# 运行单元测试
./gradlew test
```

## 6. 测试要求

### 6.1 测试场景覆盖

建议使用 IntelliJ Platform 测试框架覆盖以下场景：

- [x] 普通实例方法调用转换
- [x] 已存在 `this` 的方法调用（应忽略）
- [x] 静态方法调用（应不显示菜单）
- [x] 继承的方法调用（应忽略）
- [x] 带参数的方法调用
- [x] 批量处理方法调用
- [ ] 复杂表达式中的方法调用
- [ ] 方法链式调用处理

### 6.2 发布计划

- [x] 核心功能开发
- [x] 单元测试编写
- [ ] 集成测试验证
- [ ] 打包插件文件
- [ ] 发布至 JetBrains 官方插件市场

## 7. 用户交互设计

### 7.1 菜单位置

```
Editor Right-Click Menu
├── Cut
├── Copy
├── Paste
├── ────────── (分隔线)
├── Add 'this' to method call  ← 插件选项
└── Refactor
```

### 7.2 视觉反馈

- ✅ **可用状态**: 正常显示菜单项
- ⚠️ **不可用状态**: 菜单项灰色显示
- 🔍 **无显示**: 不符合条件时不显示菜单项