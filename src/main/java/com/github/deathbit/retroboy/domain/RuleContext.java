package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.RuleResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleContext {
    private Platform platform;
    private RuleConfig ruleConfig;
    private Set<String> licensed;
    private Set<String> globalTagBlackList;
    private Map<String, FileContext> fileContextMap;
    private Map<String, String> skippedFileReasonMap;
    private Map<Area, Set<String>> areaFinalMap;
    private Map<Area, Rule> ruleMap;
    private Map<Area, Map<String, RuleResult>> areaRuleResultMap;
    private Map<Area, List<String>> areaRenameReportMap;
    private Map<Area, List<String>> areaDuplicateNameReportMap;
    private List<String> dirsToCreate;
    private List<CopyFileInput> filesToCopy;
    private List<RenameFileInput> filesToRename;
}
