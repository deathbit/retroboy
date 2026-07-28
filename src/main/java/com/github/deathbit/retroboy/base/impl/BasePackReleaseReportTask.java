package com.github.deathbit.retroboy.base.impl;

import com.github.deathbit.retroboy.base.BasePackHandler;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.AuthorInfo;
import com.github.deathbit.retroboy.enums.BasePackTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class BasePackReleaseReportTask implements BasePackHandler {

    private static final DateTimeFormatter CREATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AppConfig appConfig;

    @Override
    public String name() {
        return appConfig.getBasePackReleaseReportTaskConfig().getTaskName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getBasePackReleaseReportTaskConfig().isEnabled();
    }

    @Override
    public BasePackTask task() {
        return BasePackTask.BASE_PACK_RELEASE_REPORT_TASK;
    }

    @Override
    public void handle() {
        var releaseReportPath = appConfig.getBasePackReleaseReportTaskConfig().getTargetPath();
        var content = new StringBuilder();
        appendTitle(content);
        appendDirectory(content);
        appendIntroduction(content);
        appendCollectionFeatures(content);
        appendBasicInfo(content);
        appendInstallInstructions(content);
        appendReleaseNotes(content);
        appendDirectoryInstructions(content);
        appendFrequentlyAskedQuestions(content);
        appendDonationInfo(content);
        appendAuthorInfo(content);
        appendDisclaimer(content);
        appendFuturePlan(content);

        try {
            Files.createDirectories(releaseReportPath.getParent());
            Files.writeString(releaseReportPath, content.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write base pack release report: " + releaseReportPath, e);
        }
    }

    private void appendTitle(StringBuilder content) {
        appendLine(content, "RetroBoy 基础包使用说明");
        appendBlankLine(content);
    }

    private void appendDirectory(StringBuilder content) {
        appendLine(content, "目录");
        appendLine(content, "一、简介");
        appendLine(content, "二、合集特点");
        appendLine(content, "三、基础信息");
        appendLine(content, "四、安装说明");
        appendLine(content, "五、版本更新记录");
        appendLine(content, "六、目录说明");
        appendLine(content, "七、常见问题");
        appendLine(content, "八、捐助本项目");
        appendLine(content, "九、关于作者");
        appendLine(content, "十、免责声明");
        appendLine(content, "十一、未来规划");
        appendBlankLine(content);
    }

    private void appendIntroduction(StringBuilder content) {
        appendLine(content, "一、简介");
        appendLine(content, "RetroBoy 基础包是一套面向普通玩家整理的复古游戏基础环境包，它把 ES-DE 前端界面、RetroArch 模拟器环境、主题、核心、BIOS、字体、着色器和基础配置整理到同一个目录中。");
        appendLine(content, "用户不需要自己寻找前端、模拟器、核心文件或基础配置，只需要先解压基础包，后续再按需覆盖对应平台包，就可以通过 ES-DE 浏览游戏并使用 RetroArch 启动游玩。");
        appendLine(content, "基础包采用程序化方式生成和校验，尽量减少手工复制、配置遗漏、目录混乱和版本不一致等问题。安装完成后，整个目录可以移动到任意位置继续使用，适合长期收藏、备份和更新。");
        appendBlankLine(content);
    }

    private void appendCollectionFeatures(StringBuilder content) {
        appendLine(content, "二、合集特点");
        appendFeature(content, "基础包 + 平台包组合发布，基础环境只需安装一次，后续增加平台只需覆盖对应平台包。");
        appendFeature(content, "压缩包发布，免安装，按说明解压后即可使用。");
        appendFeature(content, "插上手柄即可游玩，尽量减少复杂配置。");
        appendFeature(content, "基于 ES-DE 前端 + RetroArch 模拟器架构，界面美观，兼容性好。");
        appendFeature(content, "安装完成后，整个目录可以随意移动到其他位置，移动后仍可正常运行。");
        appendFeature(content, "基础包预装 ES-DE 官方主题，可自由切换界面风格。");
        appendFeature(content, "基础包预装 RetroArch 模拟器核心、BIOS 和常用资源。");
        appendFeature(content, "已修复 RetroArch 中文字体乱码问题。");
        appendFeature(content, "已预配置 MegaBezel CRT 着色器，提供老电视、老显示器风格的视觉效果。");
        appendFeature(content, "目录结构规范，方便查找、备份、迁移和后续更新。");
        appendBlankLine(content);
    }

    private void appendBasicInfo(StringBuilder content) {
        var globalConfig = appConfig.getGlobalConfig();
        appendLine(content, "三、基础信息");
        appendInfo(content, "基础包版本", globalConfig.getBasePackVersion());
        appendInfo(content, "ES-DE版本", globalConfig.getEsdeVersion());
        appendInfo(content, "RetroArch版本", globalConfig.getRetroarchVersion());
        appendInfo(content, "创建时间", LocalDateTime.now().format(CREATE_TIME_FORMATTER));
        appendInfo(content, "百度网盘", globalConfig.getBaiduPan());
        appendInfo(content, "项目仓库", globalConfig.getRepo());
        appendInfo(content, "QQ群", globalConfig.getQqGroup());
        appendInfo(content, "反馈邮箱", globalConfig.getFeedbackEmail());
        appendBlankLine(content);
    }

    private void appendInstallInstructions(StringBuilder content) {
        appendLine(content, "四、安装说明");
        appendLine(content, "本游戏合集采用“基础包 + 平台包”的覆盖安装方式。基础包会覆盖目标目录中的同名文件，如果你修改过配置、存档或其他重要文件，请先自行备份。");
        appendLine(content, "如果你还没有安装过基础包，请先解压 BASE.zip；如果已经安装过基础包，通常只需要解压或覆盖对应平台包。");
        appendLine(content, "1. 请先关闭 ES-DE、RetroArch，以及其他可能正在使用相关目录的程序。");
        appendLine(content, "2. 请解压 BASE.zip 到目标目录。如果系统提示是否合并文件夹，请选择“是”或“合并”。");
        appendLine(content, "3. 如果系统提示文件或文件夹已存在，请选择“覆盖”。");
        appendLine(content, "4. 以安装到 D 盘根目录为例，基础包应解压到 D:\\，解压完成后的关键路径应类似以下内容。");
        appendInstallPath(content, "   ES-DE 主目录", "D:\\ES-DE");
        appendInstallPath(content, "   RetroArch 目录", "D:\\ES-DE\\Emulators\\RetroArch-Win64");
        appendLine(content, "5. 安装顺序非常重要。请先安装基础包，再安装平台包，否则可能会导致前端、模拟器、主题、字体、BIOS 或核心配置不完整。");
        appendLine(content, "6. 解压完成后，启动 ES-DE。");
        appendLine(content, "7. 如果后续安装平台包后没有看到新游戏，或游戏列表没有刷新，请在 ES-DE 中重新扫描游戏列表，或重启 ES-DE 后再次检查。");
        appendBlankLine(content);
    }

    private void appendReleaseNotes(StringBuilder content) {
        var releaseNotes = appConfig.getBasePackReleaseReportTaskConfig().getReleaseNotes();
        appendLine(content, "五、版本更新记录");
        if (releaseNotes == null || releaseNotes.isEmpty()) {
            appendLine(content, "暂无版本更新记录");
            appendBlankLine(content);
            return;
        }

        releaseNotes.forEach(releaseNote -> {
            appendLine(content, "[" + releaseNote.getVersion() + "] " + releaseNote.getDate());
            releaseNote.getChanges().forEach(change -> appendFeature(content, change));
        });
        appendBlankLine(content);
    }

    private void appendDirectoryInstructions(StringBuilder content) {
        appendLine(content, "六、目录说明");
        appendLine(content, "这里以安装到D盘根目录为例。");
        appendFeature(content, "ES-DE主目录：D:\\ES-DE");
        appendFeature(content, "ES-DE配置目录：D:\\ES-DE\\ES-DE");
        appendFeature(content, "ES-DE主题目录：D:\\ES-DE\\ES-DE\\themes");
        appendFeature(content, "平台ROM根目录：D:\\ES-DE\\ROMs");
        appendFeature(content, "RetroArch主目录：D:\\ES-DE\\Emulators\\RetroArch-Win64");
        appendFeature(content, "RetroArch核心目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\cores");
        appendFeature(content, "BIOS目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\system");
        appendFeature(content, "MegaBezel着色器目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders\\Mega_Bezel_Packs");
        appendBlankLine(content);
    }

    private void appendFrequentlyAskedQuestions(StringBuilder content) {
        appendLine(content, "七、常见问题");
        appendQuestion(content, 1, "我应该先解压哪个文件？",
                "如果还没有安装基础包，请先解压 BASE.zip，再解压具体平台包；如果已经安装过基础包，通常只需要覆盖解压平台包。");
        appendQuestion(content, 2, "覆盖安装时提示文件已存在，应该怎么办？",
                "请选择覆盖。基础包会更新前端、模拟器和基础资源文件；如果你修改过重要文件，请先自行备份。");
        appendQuestion(content, 3, "安装基础包后没有看到游戏怎么办？",
                "基础包只提供运行环境，不包含具体平台游戏。请继续安装对应平台包，然后重启 ES-DE 或重新扫描游戏列表。");
        appendQuestion(content, 4, "基础包里包含哪些内容？",
                "基础包包含 ES-DE、RetroArch、主题、核心、BIOS、字体、着色器和基础配置等内容。");
        appendQuestion(content, 5, "游戏无法启动或运行效果不正常怎么办？",
                "请先确认已经完整安装基础包和对应平台包，再检查 RetroArch 和对应核心文件是否存在。如果仍有问题，可以带上平台、地区和游戏名称反馈。");
        appendQuestion(content, 6, "我可以把安装好的目录移动到其他位置吗？",
                "可以。合集安装完成后，整个目录可以移动到其他位置，通常不需要重新配置。移动后请从新的目录启动 ES-DE。");
        appendQuestion(content, 7, "升级基础包需要删除旧目录吗？",
                "通常不需要，直接覆盖安装即可。除非更新说明中特别要求删除某些目录或文件。");
        appendBlankLine(content);
    }

    private void appendDonationInfo(StringBuilder content) {
        appendLine(content, "八、捐助本项目");
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

    private void appendAuthorInfo(StringBuilder content) {
        AuthorInfo authorInfo = appConfig.getGlobalConfig().getAuthorInfo();
        appendLine(content, "九、关于作者");
        appendInfo(content, "作者", authorInfo.getName());
        appendInfo(content, "微信", authorInfo.getWechat());
        appendInfo(content, "QQ", authorInfo.getQq());
        appendInfo(content, "抖音", authorInfo.getTiktok());
        appendInfo(content, "小红书", authorInfo.getXhs());
        appendInfo(content, "B站", authorInfo.getBilibili());
        appendInfo(content, "快手", authorInfo.getKs());
        appendBlankLine(content);
    }

    private void appendDisclaimer(StringBuilder content) {
        appendLine(content, "十、免责声明");
        appendLine(content, "RetroBoy 基础包中的前端、模拟器、核心、BIOS、主题、着色器、字体及相关内容主要来源于互联网公开资料，");
        appendLine(content, "版权归原作者、原厂商及相关权利方所有。本人仅对公开资料进行采集、整理、校对、分类和配置适配，");
        appendLine(content, "不主张对原始内容拥有任何版权或其他权利。");
        appendLine(content, "本合集仅供复古游戏爱好者交流、学习、研究和个人收藏参考使用，请勿用于任何商业用途或其他未经授权的场景。");
        appendLine(content, "如果你是相关内容的权利方，认为本合集中的任何内容侵犯了你的合法权益，请通过上方反馈邮箱联系本人，");
        appendLine(content, "并提供必要的权属证明和具体链接或文件名称。本人将在核实后第一时间删除或调整相关内容。");
        appendBlankLine(content);
    }

    private void appendFuturePlan(StringBuilder content) {
        appendLine(content, "十一、未来规划");
        appendFeature(content, "支持更多平台");
        appendFeature(content, "支持汉化游戏");
        appendFeature(content, "支持移动端和怀旧掌机设备");
    }

    private void appendQuestion(StringBuilder content, int index, String question, String answer) {
        appendLine(content, "Q" + index + ". " + question);
        appendLine(content, "A" + index + ". " + answer);
        appendBlankLine(content);
    }

    private void appendInstallPath(StringBuilder content, String name, String path) {
        appendInfo(content, name, path);
    }

    private void appendFeature(StringBuilder content, String feature) {
        appendLine(content, "- " + feature);
    }

    private void appendInfo(StringBuilder content, String name, String value) {
        appendLine(content, name + "：" + (value == null ? "" : value));
    }

    private void appendLine(StringBuilder content, String line) {
        content.append(line).append(System.lineSeparator());
    }

    private void appendBlankLine(StringBuilder content) {
        content.append(System.lineSeparator());
    }
}
