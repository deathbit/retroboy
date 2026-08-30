package com.github.deathbit.retroboy.util;

import com.github.deathbit.retroboy.domain.Author;
import com.github.deathbit.retroboy.enums.MediaAssetType;

import java.util.stream.Stream;

public final class ReleaseReportUtils {

    private static final String MEDIA_EXISTS = "●";
    private static final String MEDIA_MISSING = "○";

    private ReleaseReportUtils() {
    }

    public static void appendTitle(StringBuilder content) {
        appendLine(content, "RetroBoy 游戏合集使用说明");
        appendBlankLine(content);
    }

    public static void appendIntroduction(StringBuilder content) {
        appendLine(content, "RetroBoy 游戏合集是一套面向普通玩家整理的复古游戏整合包，它把前端界面、模拟器环境、游戏文件、媒体素材和基础配置整理到同一个目录中。");
        appendLine(content, "用户不需要自己寻找模拟器、配置核心、整理游戏列表或匹配封面视频，只需要按照说明解压基础包和平台包，就可以通过 ES-DE 浏览游戏并使用 RetroArch 启动游玩。");
        appendLine(content, "本合集采用程序化方式生成和校验，游戏按维基百科官方列表与地区规则整理，并且仅使用官方授权 ROM，尽量避免坏版、魔改版、重复版本和命名混乱等问题。");
        appendLine(content, "同时内置封面、截图、预览视频、说明书和游戏元信息，让游戏浏览、查找和筛选更加直观。安装完成后，整个目录可以移动到任意位置继续使用，适合长期收藏、备份和更新。");
        appendBlankLine(content);
    }

    public static void appendCollectionFeatures(StringBuilder content) {
        appendFeature(content, "基础包 + 平台包组合发布，基础环境只需安装一次，后续增加平台只需覆盖对应平台包。");
        appendFeature(content, "压缩包发布，免安装，按说明解压后即可使用。");
        appendFeature(content, "插上手柄即可游玩，尽量减少复杂配置。");
        appendFeature(content, "基于 ES-DE 前端 + RetroArch 模拟器架构，界面美观，兼容性好。");
        appendFeature(content, "安装完成后，整个目录可以随意移动到其他位置，移动后仍可正常运行。");
        appendFeature(content, "程序化生成和校验，减少人工整理带来的遗漏、重复和命名错误。");
        appendFeature(content, "游戏按维基百科官方列表整理，尽量做到清单准确、可核对。");
        appendFeature(content, "游戏按地区分类，方便浏览日本、北美、欧洲等不同版本。");
        appendFeature(content, "仅使用官方授权 ROM，避免坏版、魔改版和混乱版本。");
        appendFeature(content, "游戏名称经过统一整理，显示更清晰。");
        appendFeature(content, "内置封面、截图、视频、说明书等媒体素材，浏览体验更完整。");
        appendFeature(content, "包含开发商、发行商、游戏类型等元信息，方便筛选和查找。");
        appendFeature(content, "平台模拟器参数已预配置，通常不需要额外设置。");
        appendFeature(content, "平台着色器已预配置，默认即可获得较好的怀旧画面效果。");
        appendFeature(content, "基础包预装 ES-DE 官方主题，可自由切换界面风格。");
        appendFeature(content, "基础包预装 RetroArch 模拟器核心、BIOS 和常用资源。");
        appendFeature(content, "已修复 RetroArch 中文字体乱码问题。");
        appendFeature(content, "已预配置 MegaBezel CRT 着色器，提供老电视、老显示器风格的视觉效果。");
        appendFeature(content, "附带调试信息，方便查看游戏筛选、命名、媒体缺失和维基映射情况。");
        appendFeature(content, "目录结构规范，方便查找、备份、迁移和后续更新。");
        appendBlankLine(content);
    }

    public static void appendInstallInstructions(StringBuilder content) {
        appendLine(content, "本游戏合集采用“基础包 + 平台包”的覆盖安装方式。基础包和平台包都会覆盖目标目录中的同名文件，如果你修改过配置、存档或其他重要文件，请先自行备份。");
        appendLine(content, "如果你还没有安装过基础包，请先解压 BASE.zip；如果已经安装过基础包，通常只需要解压当前平台包。这里以 NES.zip 为例。");
        appendLine(content, "1. 请先关闭 ES-DE、RetroArch，以及其他可能正在使用相关目录的程序。");
        appendLine(content, "2. 如果尚未安装基础包，请解压 BASE.zip 到目标目录。如果系统提示是否合并文件夹，请选择“是”或“合并”。");
        appendLine(content, "3. 解压当前平台包，例如 NES.zip，到同一个目标目录。如果系统提示文件或文件夹已存在，请选择“覆盖”。");
        appendLine(content, "4. 以安装到 D 盘根目录为例，基础包和平台包都应解压到 D:\\，解压完成后的关键路径应类似以下内容。");
        appendInfo(content, "   ES-DE 主目录", "D:\\ES-DE");
        appendInfo(content, "   RetroArch 目录", "D:\\ES-DE\\Emulators\\RetroArch-Win64");
        appendInfo(content, "   NES 游戏目录", "D:\\ES-DE\\ROMs\\NES");
        appendLine(content, "5. 安装顺序非常重要。如果在未安装基础包的情况下只解压 NES.zip，可能会导致前端、模拟器、主题、字体、BIOS 或核心配置不完整。");
        appendLine(content, "6. 解压完成后，重新启动 ES-DE。");
        appendLine(content, "7. 如果启动后没有看到新游戏，或游戏列表没有刷新，请在 ES-DE 中重新扫描游戏列表，或重启 ES-DE 后再次检查。");
        appendLine(content, "8. 如果你已经安装过旧版本平台包，可以直接使用新版本平台包覆盖安装，通常不需要删除旧文件，除非更新说明中特别要求。");
        appendBlankLine(content);
    }

    public static void appendDirectoryInstructions(StringBuilder content) {
        appendLine(content, "这里以安装到D盘根目录为例，示例平台NES，示例游戏Gun Nac");
        appendFeature(content, "ES-DE主目录：D:\\ES-DE");
        appendFeature(content, "RetroArch主目录：D:\\ES-DE\\Emulators\\RetroArch-Win64");
        appendFeature(content, "平台游戏列表位置：D:\\ES-DE\\ES-DE\\gamelists\\nes\\gamelist.xml");
        appendFeature(content, "游戏ROM位置：D:\\ES-DE\\ROMs\\nes\\NES_FC - USA\\Gun Nac.nes");
        appendFeature(content, "游戏媒体位置：D:\\ES-DE\\ES-DE\\downloaded_media\\nes\\3dboxes\\NES_FC - USA\\Gun Nac.png");
        appendFeature(content, "BIOS目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\system");
        appendFeature(content, "MegaBezel着色器目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders\\Mega_Bezel_Packs");
        appendBlankLine(content);
    }

    public static void appendFrequentlyAskedQuestions(StringBuilder content) {
        appendQuestion(content, 1, "我应该先解压哪个文件？", "如果还没有安装基础包，请先解压 BASE.zip，再解压平台包，例如 NES.zip；如果已经安装过基础包，通常只需要覆盖解压平台包。");
        appendQuestion(content, 2, "覆盖安装时提示文件已存在，应该怎么办？", "请选择覆盖。基础包和平台包都会更新部分配置、游戏列表和资源文件；如果你修改过重要文件，请先自行备份。");
        appendQuestion(content, 3, "安装完成后没有看到游戏怎么办？", "请确认平台包解压到了基础包所在的同一个目录，并且覆盖安装成功。然后重启 ES-DE，或在 ES-DE 中重新扫描游戏列表。");
        appendQuestion(content, 4, "为什么有些游戏名称和维基百科名称不完全一样？", "维基百科名称用于对应官方参考清单，文件名称用于前端显示、文件系统兼容和统一命名，两者可能会因为地区译名或命名规则略有不同。");
        appendQuestion(content, 5, "这些游戏是怎么筛选出来的？", "游戏通过程序化规则筛选和校验，并参考维基百科官方列表、地区信息和授权 ROM 数据，尽量避免遗漏、重复、坏版和魔改版。");
        appendQuestion(content, 6, "为什么要区分不同地区？", "同一平台在不同地区可能有不同发行版本、名称和内容差异，按地区整理可以让游戏列表更清晰，也方便玩家按版本选择。");
        appendQuestion(content, 7, "媒体素材包括哪些内容？", "媒体素材通常包括封面、盒背、3D 盒图、截图、标题画面、预览视频、说明书等内容，用于提升 ES-DE 中的浏览体验。");
        appendQuestion(content, 8, "游戏无法启动或运行效果不正常怎么办？", "请先确认 RetroArch 和对应核心文件存在，再检查是否完整安装基础包。如果仍有问题，可以带上平台、地区和游戏名称反馈。");
        appendQuestion(content, 9, "我可以把安装好的目录移动到其他位置吗？", "可以。合集安装完成后，整个目录可以移动到其他位置，通常不需要重新配置。移动后请从新的目录启动 ES-DE。");
        appendQuestion(content, 10, "调试信息有什么用？", "调试信息用于排查问题，包括游戏筛选、重命名、媒体缺失和维基百科映射等内容。反馈问题时提供这些信息可以更快定位原因。");
        appendQuestion(content, 11, "游戏清单里的实心圆和空心圆是什么意思？", "媒体缺失情况一列按照“" + formatMediaAssetOrder() + "”的顺序显示，" + MEDIA_EXISTS + " 表示拥有该媒体，" + MEDIA_MISSING + " 表示缺失该媒体。");
        appendBlankLine(content);
    }

    public static void appendDonationInfo(StringBuilder content) {
        appendLine(content, "RetroBoy 是个人整理和维护的复古游戏合集项目。如果这个项目节省了你的整理时间，");
        appendLine(content, "或帮助你更方便地搭建 ES-DE 与 RetroArch 环境，欢迎自愿捐助支持项目继续维护。");
        appendLine(content, "你可以通过压缩包中的微信赞赏码或支付宝收款码进行捐助。捐助完全自愿，不影响你");
        appendLine(content, "使用本项目的任何内容。");
        appendLine(content, "如果你希望在项目仓库中展示为支持者，可以在捐助后提供“平台 + 用户名”，例如");
        appendLine(content, "“抖音 - 张三”或“B站 - 李四”。我会根据捐助金额从高到低，将支持者");
        appendLine(content, "名单展示在 RetroBoy 项目仓库中，以感谢大家对项目的支持。");
        appendLine(content, "请不要在备注或公开渠道填写手机号、身份证号、详细地址等隐私信息。如需反馈问题，");
        appendLine(content, "请优先使用上方反馈邮箱，并尽量附带平台、修改后文件名称和问题描述。");
        appendBlankLine(content);
    }

    public static void appendAuthorInfo(StringBuilder content, Author author) {
        appendInfo(content, "作者", author.getName());
        appendInfo(content, "微信", author.getWechat());
        appendInfo(content, "QQ", author.getQq());
        appendInfo(content, "抖音", author.getTiktok());
        appendInfo(content, "小红书", author.getXhs());
        appendInfo(content, "B站", author.getBilibili());
        appendInfo(content, "快手", author.getKs());
        appendInfo(content, "西瓜", author.getXg());
        appendBlankLine(content);
    }

    public static void appendDisclaimer(StringBuilder content) {
        appendLine(content, "RetroBoy 游戏合集中的基础包、平台包、前端、模拟器、核心、BIOS、主题、着色器、字体、游戏信息、媒体素材、说明资料及相关内容主要来源于互联网公开资料，");
        appendLine(content, "版权归原作者、原厂商及相关权利方所有。本人仅对公开资料进行采集、整理、校对、分类和配置适配，");
        appendLine(content, "不主张对原始内容拥有任何版权或其他权利。");
        appendLine(content, "本合集仅供复古游戏爱好者交流、学习、研究和个人收藏参考使用，请勿用于任何商业用途或其他未经授权的场景。");
        appendLine(content, "如果你是相关内容的权利方，认为本合集中的任何内容侵犯了你的合法权益，请通过上方反馈邮箱联系本人，");
        appendLine(content, "并提供必要的权属证明和具体链接或文件名称。本人将在核实后第一时间删除或调整相关内容。");
        appendBlankLine(content);
    }

    public static void appendFuturePlan(StringBuilder content) {
        appendFeature(content, "支持更多平台");
        appendFeature(content, "支持汉化游戏");
        appendFeature(content, "支持移动端和怀旧掌机设备");
        appendBlankLine(content);
    }

    public static void appendLine(StringBuilder content, String line) {
        content.append(line).append(System.lineSeparator());
    }

    public static void appendBlankLine(StringBuilder content) {
        content.append(System.lineSeparator());
    }

    public static void appendFeature(StringBuilder content, String feature) {
        appendLine(content, "- " + feature);
    }

    public static void appendInfo(StringBuilder content, String name, String value) {
        appendLine(content, name + "：" + (value == null ? "" : value));
    }

    public static void appendQuestion(StringBuilder content, int index, String question, String answer) {
        appendLine(content, "Q" + index + ". " + question);
        appendLine(content, "A" + index + ". " + answer);
        appendBlankLine(content);
    }

    private static String formatMediaAssetOrder() {
        return Stream.of(MediaAssetType.values())
                .map(MediaAssetType::getDirectoryName)
                .toList()
                .toString();
    }
}
