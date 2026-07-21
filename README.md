# Retroboy

Retroboy 是一个面向 **RetroArch + ES-DE** 环境的怀旧游戏收藏构建工具。它通过 Spring Boot 启动任务，把本地 RetroArch 资源、配置文件、ROM、DAT 授权清单和筛选规则串联起来，自动完成模拟器环境初始化、ROM 分区筛选、复制、重命名和处理报告生成。

> 本项目只用于整理你合法拥有的 ROM 文件。请自行确认资源来源、使用方式和所在地区法律要求。

## 功能概览

- **RetroArch 环境整理**
  - 清空指定 RetroArch 资源目录和 ROM 输出目录。
  - 复制默认的 RetroArch 资源、核心、系统文件、ROM 目录内容和基础配置文件。
  - 批量修改 `retroarch.cfg` 中的配置项。
- **中文显示修复**
  - 替换 RetroArch 中文 fallback 字体。
  - 写入通知字体路径，改善中文显示。
- **Mega Bezel 着色器配置**
  - 复制 Mega Bezel shader 包和默认 shader preset。
  - 自动设置 Vulkan、宽高比、旋转、shader 开关等 RetroArch 配置。
- **ROM 规则筛选**
  - 读取 DAT 文件中的授权游戏名称。
  - 按平台、地区、标签黑名单、文件名黑名单和修订版本规则筛选 ROM。
  - 当前已实现 NES 平台，并支持 JPN / USA / EUR 三个地区输出。
- **自动重命名与报告**
  - 将 `Game, The`、`Game, A` 这类标题整理为 `The Game`、`A Game`。
  - 对去除标签后同名的 ROM 使用地区级 `renameOptions` 处理。
  - 在 `report/` 目录生成每个平台和地区的筛选报告。

## 项目结构

```text
.
├── pom.xml
├── README.md
├── report/                         # 运行生成或保存的筛选报告
├── src
│   ├── main
│   │   ├── java/com/github/deathbit/retroboy
│   │   │   ├── RetroboyApplication.java
│   │   │   ├── StartupRunner.java  # 应用启动后的任务编排入口
│   │   │   ├── component/          # 文件操作与 RetroArch 配置修改组件
│   │   │   ├── config/             # Spring 配置绑定与启动任务配置模型
│   │   │   ├── domain/             # 规则、文件、复制、重命名等领域对象
│   │   │   ├── enums/              # 平台、地区、启动任务枚举
│   │   │   ├── handler/            # 平台处理器，当前包含 NES
│   │   │   ├── rule/               # ROM 筛选规则引擎
│   │   │   └── utils/
│   │   └── resources/application.yaml
│   └── test                         # 规则和启动任务掩码测试
```

## 运行流程

应用启动后由 `StartupRunner` 按 `startupTaskMask` 配置执行任务：

1. `CLEAN_UP`：清空配置中列出的目录，删除指定文件。
2. `DEFAULT_CONFIG`：复制默认资源和基础配置，并写入基础 RetroArch 选项。
3. `FIX_CHINESE_FONT`：替换中文 fallback 字体并设置字体路径。
4. `SET_MEGA_BEZEL_SHADER`：复制 Mega Bezel 资源并写入 shader 相关配置。
5. `SET_PLATFORM`：按 `platformTaskMask` 执行平台处理器，目前内置 NES 处理器。

`startupTaskMask` 支持两种写法：

- 推荐写法：`CLEAN_UP|DEFAULT_CONFIG|SET_PLATFORM`
- 显式排除：`CLEAN_UP|DEFAULT_CONFIG|!FIX_CHINESE_FONT`
- 兼容旧数字掩码：例如 `31` 表示启用全部 5 个任务

未出现在名称列表中的任务默认不会执行；空值或空白值会跳过全部任务。

`platformTaskMask` 用于独立控制 `SET_PLATFORM` 内部要执行的平台处理器：

- 推荐写法：`NES|SNES`
- 显式排除：`NES|SNES|!MD`

未出现在名称列表中的平台默认不会执行；空值或空白值会跳过全部平台。目前只有配置了对应处理器的平台会实际运行。

## 配置说明

主要配置位于：

```text
src/main/resources/application.yaml
```

当前示例配置使用 Windows 路径，并假设本地目录中已经准备好 RetroArch、ES-DE、ROM、DAT 文件和 shader 资源。运行前请先改成你自己的路径。

### 全局配置

```yaml
app:
  config:
    globalConfig:
      raConfig: 'D:\ES-DE\Emulators\RetroArch-Win64\retroarch.cfg'
      startupTaskMask: 'CLEAN_UP|DEFAULT_CONFIG|FIX_CHINESE_FONT|SET_MEGA_BEZEL_SHADER|SET_PLATFORM'
      platformTaskMask: 'NES|SNES|!MD'
      globalTagBlacklist:
        - 'Virtual Console'
```

- `raConfig`：要修改的 RetroArch 配置文件。
- `startupTaskMask`：启动时要执行的任务。
- `platformTaskMask`：`SET_PLATFORM` 启动任务中要执行的平台。
- `globalTagBlacklist`：所有平台共用的 ROM 标签黑名单。

### 启动任务配置

- `cleanUpTask.cleanDirs`：需要清空内容的目录。
- `cleanUpTask.deleteFiles`：需要删除的文件。
- `defaultConfigTask.copyDirContentsInputs`：复制目录内容到目标目录。
- `defaultConfigTask.copyFileInputs`：复制单个文件到目标目录。
- `defaultConfigTask.raConfigInputs`：批量修改 RetroArch 配置项。
- `fixChineseFontTask`：中文字体删除、复制和配置项写入。
- `setMegaBezelShaderTask`：Mega Bezel 目录、preset 文件和 shader 配置项写入。

> 注意：`CLEAN_UP` 和 `DEFAULT_CONFIG` 会删除、覆盖和复制大量本地文件。首次运行前建议备份 RetroArch 与 ES-DE 目录，并确认配置路径无误。

### 平台规则配置

当前内置平台为 `NES`：

```yaml
app:
  config:
    ruleConfigMap:
      NES:
        platform: 'NES'
        datFile: 'D:\Resources\dats\Nintendo - Nintendo Entertainment System.dat'
        romDir: 'D:\Resources\roms\nes'
        targetDirBase: 'D:\ES-DE\ROMs\nes'
        targetAreaConfigs:
          - area: 'JPN'
          - area: 'USA'
          - area: 'EUR'
```

字段说明：

- `datFile`：DAT 文件路径，用于判断 ROM 是否存在于授权游戏清单。
- `romDir`：待筛选的源 ROM 目录。
- `targetDirBase`：筛选后 ROM 的输出根目录。
- `targetAreaConfigs`：地区输出配置，当前支持 `JPN`、`USA`、`EUR`。
- `tagBlackList`：平台级标签黑名单。
- `areaFileNameBlackList`：平台级文件名黑名单。
- `targetAreaConfigs[].fileNameBlackList`：地区级文件名黑名单。
- `targetAreaConfigs[].renameOptions`：去除标签后出现同名冲突时的手动重命名规则。
- `globalConfig.downloadedMediaDirBase`：ES-DE 已下载媒体根目录，例如 `D:\ES-DE\ES-DE\downloaded_media`。

## ROM 筛选规则

基础规则会过滤：

- DAT 授权清单中不存在的游戏。
- 文件名中包含 `[b]` 的坏档。
- 命中全局标签黑名单的 ROM。
- 命中平台标签黑名单的 ROM。
- 命中平台或地区文件名黑名单的 ROM。
- 存在更高 `Rev` 修订版本的旧版本 ROM。

地区规则：

- `JPN`：保留 `Japan` 或 `World`。
- `USA`：保留 `USA` 或 `World`。
- `EUR`：保留 `Europe`、`Australia`、`Germany`、`Sweden`、`France`、`Spain` 或 `World`。
- `EUR` 中如果同名游戏同时存在 `Europe` 版和其他 PAL 地区版，会优先保留 `Europe` 版。

筛选通过的 ROM 会复制到：

```text
{targetDirBase}/{AREA}/
```

例如：

```text
D:\ES-DE\ROMs\nes\USA
D:\ES-DE\ROMs\nes\JPN
D:\ES-DE\ROMs\nes\EUR
```

## 报告输出

平台处理器会在项目根目录的 `report/` 下写入报告：

```text
report/NES-JPN.yaml
report/NES-USA.yaml
report/NES-EUR.yaml
```

报告使用 YAML 格式，包含：

- 平台、地区、源 ROM 目录。
- 文件总数、通过数量、未通过数量。
- 通过列表。
- 自动重命名记录。
- 去除标签后同名冲突记录。
- 通过游戏缺失的 ES-DE 媒体文件，按媒体类型分组。
- 未通过列表和失败原因。

## 环境要求

- JDK 26
- Maven Wrapper（仓库已包含 `mvnw` / `mvnw.cmd`）
- 本地 RetroArch / ES-DE 目录
- 本地 ROM、DAT、字体和 shader 资源

`pom.xml` 中设置了 `<java.version>26</java.version>`，因此使用低版本 JDK 运行 Maven 会出现 `release version 26 not supported`。

## 构建、测试与运行

在项目根目录执行：

```bash
./mvnw test
```

运行应用：

```bash
./mvnw spring-boot:run
```

Windows PowerShell 可使用：

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

## 扩展新平台

添加新平台通常需要：

1. 在 `Platform` 枚举中增加平台值。
2. 新建平台 `Handler`，继承 `AbstractHandler` 并返回对应平台。
3. 如默认地区规则不适用，重写 `getRuleMap()`。
4. 在 `application.yaml` 的 `ruleConfigMap` 中增加平台配置。
5. 在 `StartupRunner` 中注入并调用新平台处理器。

## 许可证

本项目使用 MIT License，详见 [LICENSE](LICENSE)。
