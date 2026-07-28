package com.github.deathbit.retroboy.rule.complex;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.rule.Rule;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class RuleIsEurope implements Rule {
    private static final List<String> EUROPE_PAL_FALLBACK_REGIONS = List.of("France", "Australia", "Germany", "Spain", "Sweden");

    @Override
    public boolean pass(RuleContext ruleContext, FileContext fileContext) {
        if (fileContext.getTagPart().contains("Europe")) {
            return true;
        }
        if (ruleContext.getPlatformPackTaskConfig().isUsePal() && findPalFallbackRegion(fileContext) != null) {
            return isPreferredPalFallbackRegion(ruleContext, fileContext);
        }
        ruleContext.getRomNotPassReasons().add("IS_EUROPE失败: 不属于 Europe 地区");
        return false;
    }

    private boolean isPreferredPalFallbackRegion(RuleContext ruleContext, FileContext fileContext) {
        var currentRegion = findPalFallbackRegion(fileContext);
        if (hasSameNameEuropeRom(ruleContext, fileContext)) {
            ruleContext.getRomNotPassReasons().add("IS_EUROPE失败: 存在同名 Europe ROM，忽略 PAL 备用地区 " + currentRegion);
            return false;
        }
        var preferredRegion = findPreferredPalFallbackRegion(ruleContext, fileContext);
        if (currentRegion.equals(preferredRegion)) {
            return true;
        }
        ruleContext.getRomNotPassReasons().add(String.format("IS_EUROPE失败: PAL 备用地区优先级低于 %s", preferredRegion));
        return false;
    }

    private boolean hasSameNameEuropeRom(RuleContext ruleContext, FileContext fileContext) {
        return ruleContext.getFileContextMap().values().stream()
                .anyMatch(candidate -> fileContext.getNamePart().equals(candidate.getNamePart())
                        && candidate.getTagPart().contains("Europe"));
    }

    private String findPreferredPalFallbackRegion(RuleContext ruleContext, FileContext fileContext) {
        return ruleContext.getFileContextMap().values().stream()
                .filter(candidate -> fileContext.getNamePart().equals(candidate.getNamePart()))
                .map(this::findPalFallbackRegion)
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(EUROPE_PAL_FALLBACK_REGIONS::indexOf))
                .orElse(null);
    }

    private String findPalFallbackRegion(FileContext fileContext) {
        return EUROPE_PAL_FALLBACK_REGIONS.stream()
                .filter(region -> fileContext.getTagPart().contains(region))
                .findFirst()
                .orElse(null);
    }
}

