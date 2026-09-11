# RetroBoy

RetroBoy 是一个面向复古游戏合集制作的自动化整理工具，用于把 ES-DE 前端、RetroArch 模拟器环境、ROM、媒体素材、游戏列表、核心配置和发布说明组织成可直接分发的基础包与平台包。

项目目标是覆盖尽可能多的复古游戏平台，围绕 **ES-DE + RetroArch** 的使用场景，完成从 ROM 筛选、地区分类、文件重命名、Wiki 条目映射、媒体素材检查、`gamelist.xml` 处理、调试报告、使用说明生成到 ZIP 发布包构建的一整套流程。当前代码和配置中已经完成的是 **NES** 平台，后续会在同一套流程上继续扩展更多平台。

> 说明：本项目是个人整理和维护复古游戏合集的自动化工具，默认配置中的路径和资源目录以 Windows 环境为例。

## 功能特点

- 基础包 + 平台包组合发布，基础环境只需安装一次，后续增加平台只需覆盖对应平台包。
- 基于 ES-DE 前端 + RetroArch 模拟器架构，适合构建免安装、可迁移的复古游戏环境。
- 支持自动部署 ES-DE、RetroArch、核心资源、BIOS、字体、着色器和默认配置。
- 支持修复 RetroArch 中文字体乱码问题。
- 支持预配置 MegaBezel CRT 着色器，提供怀旧显示效果。
- 按平台和地区自动筛选 ROM，目标是支持全部主流复古游戏平台，目前已完成 `NES` 平台和 `JPN`、`USA`、`EUR` 三个地区。
- 基于 No-Intro 游戏数据库、平台修正和地区黑白名单筛选游戏。
- 支持按地区复制 ROM，并统一重命名为更适合前端显示的文件名称。
- 支持维护 Wiki 条目与最终 ROM 名称的映射关系。
- 支持复制并检查封面、盒背、3D 盒图、截图、标题图、视频、说明书等媒体素材。
- 支持生成或更新 ES-DE 所需的 `gamelist.xml`。
- 支持生成调试信息报告，便于排查筛选、重命名、Wiki 映射和媒体缺失问题。
- 支持生成面向最终用户的中文使用说明，包括安装方式、目录说明、FAQ、免责声明和游戏清单。
- 支持打包生成带版本号的基础包和平台包，例如 `BASE_1.0.zip`、`NES_1.0.zip`。

## 下载链接

你可以通过百度网盘下载 RetroBoy 基础包和已发布的平台包：

[https://pan.baidu.com/s/1FsRW8323Ga_XI142mA-0xQ?pwd=4fva](https://pan.baidu.com/s/1FsRW8323Ga_XI142mA-0xQ?pwd=4fva)

| 百度网盘二维码 |
| --- |
| <img src="img/百度网盘.jpg" alt="百度网盘二维码" width="240"> |

## 项目结构

```text
retroboy/
├── img/                                  # README 使用的二维码图片
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md
└── src/main/
  ├── java/com/github/deathbit/retroboy/
  │   ├── RetroboyApplication.java          # Spring Boot 入口
  │   ├── StartupRunner.java                # 启动后执行基础包和平台包构建
  │   ├── component/                        # 文件、配置、压缩发布等底层组件
  │   ├── config/                           # application.yaml 配置映射对象
  │   ├── domain/                           # 构建过程中的上下文和结果对象
  │   ├── enums/                            # 平台、地区、任务、媒体类型枚举
  │   ├── base/                             # 基础包构建任务
  │   ├── platform/                         # 平台包构建流程
  │   ├── rule/                             # ROM 筛选规则
  │   └── util/                             # 通用输出和路径工具
  └── resources/
    └── application.yaml                  # 主配置文件
```

## 核心概念

### 基础包

基础包是可复用的公共运行环境，主要包含：

- ES-DE 主程序和基础目录结构
- RetroArch 主程序
- RetroArch 核心、BIOS、assets、database、info、overlays、shaders 等资源
- 默认 RetroArch 配置
- 中文字体修复
- MegaBezel CRT 着色器配置

基础包最终可以发布为带版本号的压缩包：

```text
{resHome}/release/BASE_{basePackVersion}.zip
```

例如：

```text
res/release/BASE_1.0.zip
```

### 平台包

平台包是某一个游戏机平台的独立内容包，当前代码中支持：

- `NES`

平台包主要包含：

- 按地区整理后的 ROM
- 平台媒体素材
- ES-DE `gamelist.xml`
- RetroArch 平台核心配置
- 调试报告
- 使用说明文档

平台包最终可以发布为带版本号的压缩包：

```text
{resHome}/release/{Platform}_{version}.zip
```

例如：

```text
res/release/NES_1.0.zip
```

## 运行流程

程序入口是 `RetroboyApplication`，启动后由 `StartupRunner` 自动执行：

1. 基础包构建流程
2. 平台包构建流程
3. 执行完成后退出程序

是否执行基础包或平台包由 `application.yaml` 中的全局开关控制：

```yaml
globalConfig:
  enableBase: true
  enablePlatform: true
```

### 基础包构建流程

基础包任务实现位于 `base` 包，按 `BasePackTask` 枚举顺序执行：

1. `DELETE_ALL_TASK`：删除目标环境目录。
2. `SET_UP_ESDE_BASE_TASK`：复制 ES-DE 基础包。
3. `SET_UP_ESDE_UPDATE_TASK`：清理并覆盖 ES-DE 更新内容，例如主题和 ROM 根目录。
4. `SET_UP_RETROARCH_BASE_TASK`：复制 RetroArch 基础包。
5. `SET_UP_RETROARCH_UPDATE_TASK`：清理并覆盖 RetroArch 更新资源。
6. `SET_UP_RETROARCH_DEFAULT_CONFIG_TASK`：写入 RetroArch 默认配置。
7. `SET_UP_RETROARCH_FIX_CHINESE_FONT_TASK`：替换中文字体并修改字体配置。
8. `SET_UP_RETROARCH_MEGA_BEZEL_SHADER_TASK`：复制 MegaBezel 相关文件并写入视频/着色器配置。
9. `BASE_PACK_RELEASE_REPORT_TASK`：生成基础包使用说明。
10. `BASE_PACK_RELEASE_TASK`：压缩发布基础包。

每个任务都可以通过 `application.yaml` 中对应配置项的 `enabled` 单独开启或关闭。

### 平台包构建流程

平台包流程由 `DefaultPlatformPackHandler` 串联执行：

1. `PlatformContextInitializer`：按平台配置 Map 的键初始化平台上下文。
2. `WikiHandler`、`NoIntroHandler`、`SSHandler`：读取 Wiki、No-Intro 和 ScreenScraper 数据。
3. `FileContextHandler`：扫描 ROM，解析文件名和 SHA1，并应用文件别名映射。
4. 三个匹配 Handler：依次关联 Wiki 包、No-Intro 包、ROM 文件和 ScreenScraper 包。
5. `MatchPostProcessorHandler`：补充并校验发行日期。
6. `MediaDownloaderHandler`：下载缺失的 ScreenScraper 媒体。
7. `MoveHandler`、`RenameHandler`：复制 ROM 并应用重命名规则。
8. `GameListHandler`、`MediaHandler`：生成 gamelist、复制媒体并统计完整率。
9. `MappedAreaOutputHandler`、`CoreHandler`：整理输出地区目录并复制核心配置。
10. `DebugReportHandler`、`ReleaseReportHandler`：生成调试报告和使用说明。
11. `ReleaseHandler`：在 `release: true` 时生成平台 ZIP 包。

平台 `enabled: true` 时执行完整处理链；`release` 只控制最后的 ZIP 发布，不跳过媒体、gamelist 和报告处理。

## ROM 筛选规则

筛选逻辑位于 `NoIntroHandler`，数据来自 classpath 下的 `platform/{platformName}/{platformName}_db.xml`，不读取外部 DAT 文件。

- 先应用 `PlatformProcessor.preProcessGameDB()` 的平台修正。
- 保留 `licensed`、`bios`、`devstatus`、`physical` 均为空且 `regparent` 含 `PARENT` 的条目。
- 按 `regparent` 中的地区分类，先排除 `areaGameBlackList`，再接受 `areaGameWhiteList`。
- 其余条目保留基础地区 `USA`、`JPN`、`EUR` 或 `clone` 为 `P` 的根条目。

NES 的 Wiki 地区映射由 `NesPlatformProcessor` 决定：`JPN`、`USA` 保持不变，其他游戏地区映射为 `PAL`；它不是可开关的备用 ROM 筛选步骤。

## 资源目录约定

默认配置中的资源根目录是项目根目录下的 `res`：

```text
res
```

资源根目录还会放置发布包根目录附加文件，例如：

```text
res/
├── 微信赞赏码.png
└── 支付宝收款码.jpg
```

基础包资源目录约定如下：

```text
res/
└── base/
  ├── 使用说明-基础包.txt                    # 基础包说明文档，由 BASE_PACK_RELEASE_REPORT_TASK 生成
  ├── ES-DE/                              # ES-DE 基础包
  ├── ES-DE-Update/                       # ES-DE 更新内容
  │   ├── themes/
  │   └── ROMs/
  ├── RetroArch-Win64/                    # RetroArch 基础包
  ├── RetroArch-Win64-Update/             # RetroArch 更新内容
  │   ├── assets/
  │   ├── autoconfig/
  │   ├── cheats/
  │   ├── cores/
  │   ├── database/
  │   ├── info/
  │   ├── overlays/
  │   ├── shaders/
  │   ├── system/
  │   └── retroarch.cfg
  ├── RetroArch-Win64-FixChineseFont/
  │   └── chinese-fallback-font.ttf
  └── RetroArch-Win64-MegaBezelShader/
    ├── Mega_Bezel_Packs/
    ├── global.slangp
    └── retroarch.slangp
```

平台资源目录以 NES 平台为例，约定如下：

```text
res/
└── platform/
  └── nes/
    ├── roms/
    │   └── *.nes
    ├── ss/
    │   └── {ScreenScraper包ID}/             # 下载的媒体文件
    ├── miximages/                          # 可选的组合预览图
    ├── gamelist.xml
    ├── core_config/
    │   └── Mesen/
    └── report/
      ├── 调试信息-NES.txt
      └── 使用说明-NES.txt
```

资源目录由 `src/main/resources/config/global.yaml` 中的 `globalConfig.resHome` 统一配置，基础包路径通过 `${globalConfig.resHome}` 引用它。配置中的资源子路径使用 `/`，Java 使用 `Path` / `resolve` 解析，兼容 Windows 和 macOS。

在 IDEA 的 Run/Debug Configurations 中，将 **Working directory** 设置为 `$PROJECT_DIR$`；命令行运行时也应先进入项目根目录。`res` 已被 `.gitignore` 忽略，换电脑时需要单独同步其内容。建议在 IDEA 中将 `res` 标记为 **Mark Directory as → Excluded**，避免索引大量资源文件。

> 目标安装目录仍为 `D:\ES-DE`，RetroArch 目标目录也保持不变。macOS 可以开发和读取项目内资源，但不能直接访问 Windows 的 D 盘；当前默认配置的完整安装、复制和发布流程仍需在 Windows 执行。

## 主要配置说明

配置文件位于：

```text
src/main/resources/application.yaml
```

### 全局配置

```yaml
globalConfig:
  enableBase: true
  enablePlatform: true
  esdeHome: 'D:\ES-DE'
  raHome: 'D:\ES-DE\Emulators\RetroArch-Win64'
  resHome: 'res'
  repo: 'https://github.com/deathbit/retroboy'
  esdeVersion: '3.4.1'
  raVersion: '1.22.2'
  baiduPan: 'https://pan.baidu.com/s/1FsRW8323Ga_XI142mA-0xQ?pwd=4fva'
  qqGroup: '1021421949'
  feedbackEmail: '809730879@qq.com'
```

常用字段说明：

| 字段 | 说明 |
| --- | --- |
| `enableBase` | 是否执行基础包构建流程 |
| `enablePlatform` | 是否执行平台包构建流程 |
| `esdeHome` | ES-DE 目标安装目录 |
| `raHome` | RetroArch 目标目录 |
| `resHome` | 原始资源、报告和发布包根目录 |
| `repo` | 项目仓库地址，会写入使用说明 |
| `esdeVersion` | ES-DE 版本，会写入使用说明 |
| `raVersion` | RetroArch 版本，会写入使用说明 |
| `baiduPan` | 基础包网盘地址，会写入使用说明 |
| `qqGroup` | QQ 群，会写入使用说明 |
| `feedbackEmail` | 反馈邮箱，会写入使用说明和免责声明 |

### 基础包发布配置

基础包说明文档和发布包配置位于：

```yaml
basePackReleaseReportTaskConfig:
  taskName: '生成基础包说明文档'
  enabled: true
  targetPath: '${globalConfig.resHome}/base/使用说明-基础包.txt'
  basePackVersion: '1.0'
  releaseNotes:
    - version: '1.0'
      date: '1991-02-25'
      changes:
        - '首次发布 RetroBoy 基础包。'

basePackReleaseTaskConfig:
  taskName: '发布基础包'
  enabled: true
  targetPath: '${globalConfig.resHome}/release/BASE.zip'
  rootFilePaths:
    - '${globalConfig.resHome}/base/使用说明-基础包.txt'
    - '${globalConfig.resHome}/微信赞赏码.png'
    - '${globalConfig.resHome}/支付宝收款码.jpg'
```

`targetPath` 可以保持为 `BASE.zip`，发布时会自动按 `basePackVersion` 输出为 `BASE_1.0.zip`。`rootFilePaths` 中的文件会按文件名放入压缩包根目录。

### 平台配置

平台配置位于 `platformPackTaskConfigMap`。当前示例为 `NES`：

```yaml
platformPackTaskConfigMap:
  NES:
    version: '1.0'
    enabled: true
    release: false
    core: 'Mesen'
    wiki: 'https://en.wikipedia.org/wiki/List_of_Nintendo_Entertainment_System_games'
```

常用字段说明：

| 字段 | 说明 |
| --- | --- |
| `version` | 平台包版本 |
| `enabled` | 是否构建该平台 |
| `release` | 是否生成平台 ZIP 包 |
| `core` | 默认 RetroArch 核心配置目录名，例如 `Mesen` |
| `coreConfigs` | 发布平台包时额外打包的核心配置文件 |
| `wiki` | 对应平台的 Wikipedia 游戏列表地址 |
| `releaseNotes` | 平台包版本更新记录，会写入使用说明 |
| `areaGameBlackList` | 排除指定地区的 No-Intro 游戏，格式为 `地区 - 游戏标题` |
| `areaGameWhiteList` | 补充指定地区的 No-Intro 游戏，格式同上 |
| `renameOptions` | 指定 ROM 的手动重命名规则 |

平台由 Map 的键（例如 `NES`）确定，无需在条目中重复指定。

## Wiki 包映射

`WikiGamePackageToNoIntroGamePackageMatchHandler` 依次执行完整匹配、部分匹配、去空格匹配、模糊匹配，最后应用平台配置中的 `packageMappingList` 直接映射。

`packageMappingList` 的每个条目格式为 `wikiId -> gameId`。两端 ID 必须存在于前面各层尚未匹配的包中，且不能重复。`noFuzzyRatioMatch` 可指定跳过模糊匹配、留给直接映射处理的 Wiki 包 ID。

匹配报告写入 `res/platform/{platformName}/match.json`。它是生成物，不是输入；删除后会在下一次执行匹配阶段时重新生成。存在未匹配 Wiki 包或未使用 No-Intro 包时，程序会写出报告并终止，便于修正映射。

## 媒体素材

当前支持的媒体类型定义在 `MediaAssetType`：

| 顺序 | 目录名 | 主扩展名 | 备用扩展名 |
| --- | --- | --- | --- |
| 1 | `3dboxes` | `png` | `jpg` |
| 2 | `backcovers` | `png` | `jpg` |
| 3 | `covers` | `png` | `jpg` |
| 4 | `fanart` | `png` | `jpg` |
| 5 | `manuals` | `pdf` | 无 |
| 6 | `marquees` | `png` | `jpg` |
| 7 | `miximages` | `png` | `jpg` |
| 8 | `physicalmedia` | `png` | `jpg` |
| 9 | `screenshots` | `png` | `jpg` |
| 10 | `titlescreens` | `png` | `jpg` |
| 11 | `videos` | `mp4` | 无 |

ScreenScraper 媒体下载到以下目录，再由 `MediaHandler` 按类型、地区和扩展名选择并复制到 ES-DE：

```text
{resHome}/platform/{platformName}/ss/{ScreenScraper包ID}/
```

ES-DE 输出媒体位于 `{esdeHome}/ES-DE/downloaded_media/{platformName}`。可选的组合预览图从 `{resHome}/platform/{platformName}/miximages` 复制。

报告中的媒体状态使用：

- `●`：该媒体存在
- `○`：该媒体缺失

媒体状态顺序与上表一致。

## 生成产物

### ES-DE 目录中的产物

平台包流程会向 ES-DE 目录写入或更新：

```text
D:\ES-DE\ROMs\nes\NES-JPN\*.nes
D:\ES-DE\ROMs\nes\NES-USA\*.nes
D:\ES-DE\ROMs\nes\NES-EUR\*.nes
D:\ES-DE\ES-DE\downloaded_media\nes\...
D:\ES-DE\ES-DE\gamelists\nes\gamelist.xml
D:\ES-DE\Emulators\RetroArch-Win64\config\Mesen\...
```

### 资源目录中的报告

基础包流程会生成：

```text
res/base/使用说明-基础包.txt
```

平台包流程会生成：

```text
res/platform/nes/调试信息-NES.txt
res/platform/nes/使用说明-NES.txt
```

调试报告包括：

- 授权游戏列表
- 原始 ROM 信息
- 各地区通过游戏列表
- 各地区未通过游戏列表及原因
- 重命名结果
- 媒体缺失列表
- Wiki 名称映射列表

使用说明包括：

- 简介
- 合集特点
- 基础信息
- 安装说明
- 版本更新记录
- 目录说明
- 常见问题
- 捐助本项目
- 关于作者
- 免责声明
- 未来规划
- 游戏清单

### 发布包

当对应发布开关开启时，会生成：

```text
res/release/BASE_1.0.zip
res/release/NES_1.0.zip
```

基础包发布包根目录会额外包含基础包说明文档、微信赞赏码和支付宝收款码。平台包发布包根目录会额外包含微信赞赏码、支付宝收款码、平台调试报告和平台使用说明。

## 使用方式

### 1. 准备环境

确认已安装 JDK 17，并准备好 ES-DE、RetroArch、ROM、媒体素材和核心配置等资源。保留 `src/main/resources/platform` 下的游戏数据库、Wiki HTML、ScreenScraper CSV 和 JSON 缓存，它们仍是程序输入。

### 2. 修改配置

编辑：

```text
src/main/resources/application.yaml
```

根据本机实际目录修改：

- `resHome`
- `esdeHome`
- `raHome`
- 基础包各任务的 `sourcePath` / `targetPath`
- 平台包的 `enabled`、`release`
- 平台黑名单、地区黑名单、重命名规则和更新记录

### 3. 构建或运行项目

编译检查：

```bash
./mvnw test
```

运行程序：

```bash
./mvnw spring-boot:run
```

也可以先打包再运行：

```bash
./mvnw package
java -jar target/retroboy-0.0.1-SNAPSHOT.jar
```

> 当前代码执行完成后会调用 `System.exit(0)` 主动退出。

## 推荐工作流

### 构建基础包

1. 在 `globalConfig` 中设置：

```yaml
enableBase: true
enablePlatform: false
```

2. 检查各基础包任务路径和 `enabled`。
3. 运行程序。
4. 确认带版本号的基础包生成，例如 `BASE_1.0.zip`。

### 初次整理平台包

1. 在 `globalConfig` 中设置：

```yaml
enableBase: false
enablePlatform: true
```

2. 在平台配置中先设置：

```yaml
enabled: true
release: false
```

3. 准备原始 ROM 和核心配置，确认游戏数据库、Wiki HTML 及 ScreenScraper 列表和缓存齐全。
4. 运行完整平台流程；如果匹配阶段报错，根据报告修正 `packageMappingList` 等配置后重新运行。
5. 检查生成的 ROM、媒体、gamelist、调试报告和使用说明。
6. 确认无误后设置：

```yaml
release: true
```

7. 再次运行程序生成平台发布包。

## 最终用户安装说明摘要

生成的 `使用说明-{Platform}.txt` 会面向最终用户说明安装方式。核心逻辑如下：

1. 如果尚未安装基础包，先解压基础包，例如 `BASE_1.0.zip`。
2. 再解压当前平台包，例如 `NES_1.0.zip`。
3. 基础包和平台包都解压到同一个目标目录。
4. 如果系统提示合并文件夹或覆盖文件，选择合并/覆盖。
5. 解压完成后启动 ES-DE。
6. 如果没有看到新游戏，可以重启 ES-DE 或重新扫描游戏列表。

以安装到 D 盘根目录为例，关键路径类似：

```text
D:\ES-DE
D:\ES-DE\Emulators\RetroArch-Win64
D:\ES-DE\ROMs\nes
```

## 扩展新平台

如需支持新平台，需要完成以下工作：

1. 在 `Platform` 枚举中增加平台值。
2. 在 `application.yaml` 的 `platformPackTaskConfigMap` 中增加平台配置。
3. 准备平台资源目录：游戏数据库、Wiki HTML、ScreenScraper 列表、ROM、媒体素材和核心配置。
4. 实现对应的 `WikiParser`、`PlatformProcessor`，并配置需要的包映射。
5. 运行平台包流程并检查调试报告。
6. 确认无误后开启发布。

## 注意事项

- 默认配置会执行文件删除、复制和覆盖操作，运行前请仔细确认路径。
- 建议先使用测试目录验证流程，再对正式资源目录执行。
- `deleteAllTaskConfig.enabled` 为 `true` 时会删除配置中的目标目录，请谨慎使用。
- 平台包发布开关 `release` 默认为是否实际生成 ZIP 的关键控制项。
- `release: false` 只关闭 ZIP 发布，其他处理步骤仍会执行。
- Wiki 包直接映射的 ID 必须存在且未被前面的匹配层使用，否则程序会抛出异常。
- 当前路径配置主要面向 Windows，跨平台运行前需要额外适配路径。

## 捐助本项目

RetroBoy 是个人整理和维护的复古游戏合集项目。如果这个项目节省了你的整理时间，或帮助你更方便地搭建 ES-DE 与 RetroArch 环境，欢迎自愿捐助支持项目继续维护。

你可以通过微信赞赏码或支付宝收款码进行捐助。捐助完全自愿，不影响你使用本项目的任何内容。

| 微信赞赏码 | 支付宝收款码 |
| --- | --- |
| <img src="img/微信赞赏码.png" alt="微信赞赏码" width="240"> | <img src="img/支付宝收款码.jpg" alt="支付宝收款码" width="240"> |

如果你希望在项目仓库中展示为支持者，可以在捐助后提供“平台 + 用户名”，例如“抖音 - 张三”或“B站 - 李四”。我会根据捐助金额从高到低，将支持者名单展示在 RetroBoy 项目仓库中，以感谢大家对项目的支持。

请不要在备注或公开渠道填写手机号、身份证号、详细地址等隐私信息。如需反馈问题，请优先使用反馈邮箱，并尽量附带平台、修改后文件名称和问题描述。

## 联系作者

你可以通过以下方式联系作者、反馈问题或关注项目更新。具体联系方式以 `application.yaml` 中 `globalConfig.author` 和 `feedbackEmail` 的配置为准。

| 渠道 | 信息 |
| --- | --- |
| 作者 | 村长不可爱 |
| 微信 | 村长\|paprika225 |
| QQ | 村长不可爱\|809730879 |
| 抖音 | 村长不可爱\|paprika0225 |
| 小红书 | 村长不可爱\|zhangjian225 |
| B站 | 村长不可爱\|49829377 |
| 快手 | 村长不可爱\|paprika225 |
| 反馈邮箱 | 809730879@qq.com |
| QQ 群 | 1021421949 |

反馈问题时建议提供：平台、地区、游戏名称、修改后文件名称、问题现象、必要截图，以及 `report` 目录下的调试信息。

## 未来规划

- 支持更多复古游戏平台，逐步把当前 NES 平台流程扩展到其他主流平台。
- 支持汉化游戏整理，让中文玩家更容易查找和游玩汉化版本。
- 支持移动端和怀旧掌机设备，适配更多实际游玩场景。
- 继续完善媒体素材检查、Wiki 映射、调试报告和发布说明，让平台包质量更容易验证和维护。

## 免责声明

RetroBoy 项目中的游戏信息、媒体素材、说明资料及相关内容主要来源于互联网公开资料，版权归原作者、原厂商及相关权利方所有。本人仅对公开资料进行采集、整理、校对、分类和配置适配，不主张对原始内容拥有任何版权或其他权利。

本项目及其生成的合集内容仅供复古游戏爱好者交流、学习、研究和个人收藏参考使用，请勿用于任何商业用途或其他未经授权的场景。如果你是相关内容的权利方，认为本项目中的任何内容侵犯了你的合法权益，请通过项目配置中的反馈邮箱联系本人，并提供必要的权属证明和具体链接或文件名称。本人将在核实后第一时间删除或调整相关内容。

## License

本项目采用 MIT License，详见 `LICENSE`。
