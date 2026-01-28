# Retroboy

**Retroboy** 是一个基于 Spring Boot 的怀旧游戏收藏构建工具，用于自动化管理和配置 RetroArch 模拟器及 ES-DE 前端的游戏库。

## 项目简介

Retroboy 提供了一套完整的自动化解决方案，用于：

- 清理和初始化 RetroArch 模拟器环境
- 配置 RetroArch 的各项设置（着色器、字体、控制器等）
- 根据地区和平台规则自动筛选和组织 ROM 文件
- 支持自定义配置和扩展

## 核心功能

### 1. 自动化环境配置

应用启动时会自动执行以下任务：

#### 目录清理（CleanUpTask）

- 清空 RetroArch 的核心目录（info、assets、autoconfig、cheats、database、overlays、shaders、cores、system）
- 清空 ROM 目录
- 删除旧的配置文件

#### 默认配置部署（DefaultConfigTask）

- 批量复制 RetroArch 必需的资源文件
- 自动配置 RetroArch 参数：
    - 全屏模式
    - ROM 浏览目录
    - 控制器模拟摇杆模式

#### 中文字体修复（FixChineseFontTask）

- 替换 RetroArch 的中文回退字体
- 配置字体路径以正确显示中文

#### Mega Bezel 着色器设置（SetMegaBezelShaderTask）

- 部署 Mega Bezel 着色器包
- 配置着色器相关参数（Vulkan 驱动、宽高比、旋转设置等）
- 启用全局着色器

### 2. 智能 ROM 筛选系统

基于规则引擎的 ROM 文件筛选和分类系统：

#### 规则引擎

- **许可验证**：通过 DAT 文件验证 ROM 的合法性
- **质量过滤**：排除损坏版本（[b] 标签）和 BIOS 文件
- **标签黑名单**：过滤虚拟主机版本、合集版本等不需要的版本
- **地区筛选**：根据地区（日本、美国、欧洲）和 World 版本进行智能筛选

#### 支持的平台

- **NES**（Nintendo Entertainment System）：已实现完整支持
- 可扩展至其他平台（通过实现 `AbstractHandler`）

### 3. 配置组件

#### FileComponent

提供文件和目录操作的统一接口：

- 批量文件/目录操作（创建、删除、复制、重命名）
- 目录内容复制
- 文件批量处理

#### ConfigComponent

管理 RetroArch 配置文件：

- 配置项的读取和修改
- 批量配置更新

## 项目架构

```
retroboy/
├── configInput/                     # 配置相关
│   ├── AppConfig.java          # 主配置类
│   ├── GlobalConfig.java       # 全局配置
│   └── tasks/                  # 配置任务
│       ├── CleanUpTask.java
│       ├── DefaultConfigTask.java
│       ├── FixChineseFontTask.java
│       └── SetMegaBezelShaderTask.java
├── component/                  # 组件层
│   ├── FileComponent.java      # 文件操作接口
│   ├── ConfigComponent.java    # 配置操作接口
│   └── impl/                   # 组件实现
├── handler/                    # 平台处理器
│   ├── Handler.java            # 处理器接口
│   ├── AbstractHandler.java    # 抽象处理器
│   └── handlers/
│       └── nintendo/
│           └── NesHandler.java # NES 平台处理器
├── rule/                       # 规则引擎
│   ├── Rule.java               # 规则接口
│   └── Rules.java              # 预定义规则集
├── domain/                     # 领域模型
├── enums/                      # 枚举类型
│   ├── Platform.java           # 平台枚举
│   └── Area.java               # 地区枚举
├── utils/                      # 工具类
├── StartupRunner.java          # 启动执行器
└── RetroboyApplication.java    # 应用入口
```

## 配置说明

### 配置文件位置

`src/main/resources/application.yaml`

### 主要配置项

#### 全局配置

```yaml
app.configInput.globalConfig:
  raConfigFile: RetroArch 配置文件路径
  tagBlacklist: ROM 标签黑名单
```

#### 任务配置

- `cleanUpTask`: 清理任务配置
- `defaultConfigTask`: 默认配置任务
- `fixChineseFontTask`: 字体修复任务
- `setMegaBezelShaderTask`: 着色器配置任务

#### 平台规则配置

```yaml
app.configInput.ruleConfigMap:
  NES:
    platform: NES
    datFile: DAT 文件路径
    romDir: ROM 源目录
    targetDirBase: 目标目录基础路径
    targetAreaConfigs: 目标地区配置
```

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+

### 编译项目

```bash
./mvnw clean compile
```

### 运行应用

```bash
./mvnw spring-boot:run
```

### 运行测试

```bash
./mvnw test
```

## 使用指南

### 添加新平台支持

1. 在 `Platform` 枚举中添加新平台
2. 创建新的 Handler 类继承 `AbstractHandler`
3. 实现 `getRuleMap()` 和 `getPlatform()` 方法
4. 在 `application.yaml` 中添加平台配置
5. 在 `StartupRunner` 中注入并调用新的 Handler

### 自定义规则

在 `Rules` 类中定义新规则，或组合现有规则：

```java
public static final Rule CUSTOM_RULE = IS_LICENSED
        .and(IS_NOT_BAD)
        .and(customCondition);
```

### 修改启动任务

编辑 `StartupRunner.java` 的 `run()` 方法，添加或修改执行流程。

## 技术栈

- **Spring Boot 4.0.1** - 应用框架
- **Java 17** - 编程语言
- **Maven** - 构建工具
- **Lombok** - 代码简化
- **ProgressBar** - 进度显示（me.tongfei:progressbar:0.10.2）

## 依赖项

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>me.tongfei</groupId>
        <artifactId>progressbar</artifactId>
        <version>0.10.2</version>
    </dependency>
</dependencies>
```

## 许可证

本项目使用的许可证信息请参见项目配置。

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进本项目。

## 作者

deathbit

---

**注意**：本工具设计用于管理合法拥有的 ROM 文件。请确保遵守相关法律法规。
