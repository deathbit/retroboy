package com.github.deathbit.retroboy.base.impl;

import com.github.deathbit.retroboy.base.BasePackHandler;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.BasePackTask;
import com.github.deathbit.retroboy.util.ReleaseReportUtils;
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
        ReleaseReportUtils.appendTitle(content);
        appendDirectory(content);
        ReleaseReportUtils.appendLine(content, "一、简介");
        ReleaseReportUtils.appendIntroduction(content);
        ReleaseReportUtils.appendLine(content, "二、合集特点");
        ReleaseReportUtils.appendCollectionFeatures(content);
        appendBasicInfo(content);
        ReleaseReportUtils.appendLine(content, "四、安装说明");
        ReleaseReportUtils.appendInstallInstructions(content);
        appendReleaseNotes(content);
        ReleaseReportUtils.appendLine(content, "六、目录说明");
        ReleaseReportUtils.appendDirectoryInstructions(content);
        ReleaseReportUtils.appendLine(content, "七、常见问题");
        ReleaseReportUtils.appendFrequentlyAskedQuestions(content);
        ReleaseReportUtils.appendLine(content, "八、捐助本项目");
        ReleaseReportUtils.appendDonationInfo(content);
        ReleaseReportUtils.appendLine(content, "九、关于作者");
        ReleaseReportUtils.appendAuthorInfo(content, appConfig.getGlobalConfig().getAuthor());
        ReleaseReportUtils.appendLine(content, "十、免责声明");
        ReleaseReportUtils.appendDisclaimer(content);
        ReleaseReportUtils.appendLine(content, "十一、未来规划");
        ReleaseReportUtils.appendFuturePlan(content);

        try {
            Files.createDirectories(releaseReportPath.getParent());
            Files.writeString(releaseReportPath, content.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write base pack release report: " + releaseReportPath, e);
        }
    }

    private void appendDirectory(StringBuilder content) {
        ReleaseReportUtils.appendLine(content, "目录");
        ReleaseReportUtils.appendLine(content, "一、简介");
        ReleaseReportUtils.appendLine(content, "二、合集特点");
        ReleaseReportUtils.appendLine(content, "三、基础信息");
        ReleaseReportUtils.appendLine(content, "四、安装说明");
        ReleaseReportUtils.appendLine(content, "五、版本更新记录");
        ReleaseReportUtils.appendLine(content, "六、目录说明");
        ReleaseReportUtils.appendLine(content, "七、常见问题");
        ReleaseReportUtils.appendLine(content, "八、捐助本项目");
        ReleaseReportUtils.appendLine(content, "九、关于作者");
        ReleaseReportUtils.appendLine(content, "十、免责声明");
        ReleaseReportUtils.appendLine(content, "十一、未来规划");
        ReleaseReportUtils.appendBlankLine(content);
    }

    private void appendBasicInfo(StringBuilder content) {
        var globalConfig = appConfig.getGlobalConfig();
        var releaseReportTaskConfig = appConfig.getBasePackReleaseReportTaskConfig();
        ReleaseReportUtils.appendLine(content, "三、基础信息");
        ReleaseReportUtils.appendInfo(content, "基础包版本", releaseReportTaskConfig.getBasePackVersion());
        ReleaseReportUtils.appendInfo(content, "ES-DE版本", globalConfig.getEsdeVersion());
        ReleaseReportUtils.appendInfo(content, "RetroArch版本", globalConfig.getRaVersion());
        ReleaseReportUtils.appendInfo(content, "创建时间", LocalDateTime.now().format(CREATE_TIME_FORMATTER));
        ReleaseReportUtils.appendInfo(content, "百度网盘", globalConfig.getBaiduPan());
        ReleaseReportUtils.appendInfo(content, "项目仓库", globalConfig.getRepo());
        ReleaseReportUtils.appendInfo(content, "QQ群", globalConfig.getQqGroup());
        ReleaseReportUtils.appendInfo(content, "反馈邮箱", globalConfig.getFeedbackEmail());
        ReleaseReportUtils.appendBlankLine(content);
    }

    private void appendReleaseNotes(StringBuilder content) {
        var releaseNotes = appConfig.getBasePackReleaseReportTaskConfig().getReleaseNotes();
        ReleaseReportUtils.appendLine(content, "五、版本更新记录");
        if (releaseNotes == null || releaseNotes.isEmpty()) {
            ReleaseReportUtils.appendLine(content, "暂无版本更新记录");
            ReleaseReportUtils.appendBlankLine(content);
            return;
        }

        releaseNotes.forEach(releaseNote -> {
            ReleaseReportUtils.appendLine(content, "[" + releaseNote.getVersion() + "] " + releaseNote.getDate());
            releaseNote.getChanges().forEach(change -> ReleaseReportUtils.appendFeature(content, change));
        });
        ReleaseReportUtils.appendBlankLine(content);
    }
}
