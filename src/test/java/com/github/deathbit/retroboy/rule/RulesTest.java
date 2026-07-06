package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleConfig;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RulesTest {

    @Test
    void shouldShortCircuitAndEvaluationWhenLeftRuleFails() {
        var rightEvaluations = new AtomicInteger();
        Rule left = (rc, fc) -> false;
        Rule right = (rc, fc) -> {
            rightEvaluations.incrementAndGet();
            return true;
        };

        var result = left.and(right).pass(null, null);

        assertThat(result).isFalse();
        assertThat(rightEvaluations).hasValue(0);
    }

    @Test
    void shouldShortCircuitOrEvaluationWhenLeftRulePasses() {
        var rightEvaluations = new AtomicInteger();
        Rule left = (rc, fc) -> true;
        Rule right = (rc, fc) -> {
            rightEvaluations.incrementAndGet();
            return false;
        };

        var result = left.or(right).pass(null, null);

        assertThat(result).isTrue();
        assertThat(rightEvaluations).hasValue(0);
    }

    @Test
    void shouldEvaluateAreaFileNameBlackList() {
        var fileContext = fileContext("Blocked.nes", "Blocked", Set.of("Japan"));
        var ruleContext = ruleContext(
                Map.of(fileContext.getFileName(), fileContext),
                List.of(AreaConfig.builder()
                        .area(Area.JPN)
                        .fileNameBlackList(Set.of("Blocked.nes"))
                        .build()));

        var result = Rules.failedRuleNames(Area.JPN, ruleContext, fileContext);

        assertThat(result).containsExactly("IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST");
    }

    @Test
    void shouldUseCurrentAreaForAreaFileNameBlackList() {
        var fileContext = fileContext("Blocked.nes", "Blocked", Set.of("World"));
        var ruleContext = ruleContext(
                Map.of(fileContext.getFileName(), fileContext),
                List.of(
                        AreaConfig.builder()
                                .area(Area.JPN)
                                .fileNameBlackList(Set.of("Blocked.nes"))
                                .build(),
                        AreaConfig.builder()
                                .area(Area.USA)
                                .fileNameBlackList(Set.of())
                                .build()));

        ruleContext.setCurrentArea(Area.USA);
        var usaResult = Rules.failedRuleNames(Area.USA, ruleContext, fileContext);
        ruleContext.setCurrentArea(Area.JPN);
        var japanResult = Rules.failedRuleNames(Area.JPN, ruleContext, fileContext);

        assertThat(usaResult).isEmpty();
        assertThat(japanResult).containsExactly("IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST");
    }

    @Test
    void shouldEvaluatePreviousRevisionRule() {
        var fileContextMap = new LinkedHashMap<String, FileContext>();
        fileContextMap.put("Game (USA).nes", fileContext("Game (USA).nes", "Game", Set.of("USA")));
        fileContextMap.put("Game (USA) (Rev 1).nes", fileContext("Game (USA) (Rev 1).nes", "Game", Set.of("USA", "Rev 1")));
        fileContextMap.put("Game (USA) (Rev 2).nes", fileContext("Game (USA) (Rev 2).nes", "Game", Set.of("USA", "Rev 2")));
        var ruleContext = ruleContext(fileContextMap, List.of(areaConfig(Area.USA)));

        var originalResult = Rules.failedRuleNames(Area.USA, ruleContext, fileContextMap.get("Game (USA).nes"));
        var rev1Result = Rules.failedRuleNames(Area.USA, ruleContext, fileContextMap.get("Game (USA) (Rev 1).nes"));
        var rev2Result = Rules.failedRuleNames(Area.USA, ruleContext, fileContextMap.get("Game (USA) (Rev 2).nes"));

        assertThat(originalResult).containsExactly("IS_NOT_PREVIOUS_REVISION");
        assertThat(rev1Result).containsExactly("IS_NOT_PREVIOUS_REVISION");
        assertThat(rev2Result).isEmpty();
    }

    @Test
    void shouldPreferEuropeVersionForEuropeArea() {
        var fileContextMap = Map.of(
                "Game (Europe).nes", fileContext("Game (Europe).nes", "Game", Set.of("Europe")),
                "Game (Germany).nes", fileContext("Game (Germany).nes", "Game", Set.of("Germany")),
                "Other (Germany).nes", fileContext("Other (Germany).nes", "Other", Set.of("Germany"))
        );
        var ruleContext = ruleContext(fileContextMap, List.of(areaConfig(Area.EUR)));

        var europeResult = Rules.failedRuleNames(Area.EUR, ruleContext, fileContextMap.get("Game (Europe).nes"));
        var germanyResult = Rules.failedRuleNames(Area.EUR, ruleContext, fileContextMap.get("Game (Germany).nes"));
        var otherResult = Rules.failedRuleNames(Area.EUR, ruleContext, fileContextMap.get("Other (Germany).nes"));

        assertThat(europeResult).isEmpty();
        assertThat(germanyResult).containsExactly("PREFER_EUROPE_VERSION");
        assertThat(otherResult).isEmpty();
    }

    @Test
    void shouldCollectAllFailedRuleNamesWhenOrRuleFails() {
        var fileContext = fileContext("Game (Australia).nes", "Game", Set.of("Australia"));
        var ruleContext = ruleContext(Map.of(fileContext.getFileName(), fileContext), List.of(areaConfig(Area.JPN)));

        var result = Rules.failedRuleNames(Area.JPN, ruleContext, fileContext);

        assertThat(result).containsExactly("IS_JAPAN", "IS_WORLD");
    }

    @Test
    void shouldCollectAllFailedRuleNamesWhenAndRuleFails() {
        var fileContext = fileContext("Game (Australia).nes", "Game", Set.of("Australia"));
        var ruleContext = ruleContext(Map.of(fileContext.getFileName(), fileContext), List.of(areaConfig(Area.JPN)));
        ruleContext.setLicensed(Set.of());

        var result = Rules.failedRuleNames(Area.JPN, ruleContext, fileContext);

        assertThat(result).containsExactly("IS_LICENSED", "IS_JAPAN", "IS_WORLD");
    }

    private FileContext fileContext(String fileName, String namePart, Set<String> tags) {
        return FileContext.builder()
                .fileName(fileName)
                .fullName(fileName.substring(0, fileName.lastIndexOf('.')))
                .namePart(namePart)
                .tagPart(tags.stream().map(tag -> "(" + tag + ")").collect(Collectors.joining()))
                .tags(tags)
                .build();
    }

    private RuleContext ruleContext(Map<String, FileContext> fileContextMap, List<AreaConfig> areaConfigs) {
        return RuleContext.builder()
                .ruleConfig(RuleConfig.builder()
                        .targetAreaConfigs(areaConfigs)
                        .tagBlackList(Set.of())
                        .fileNameBlackList(Set.of())
                        .build())
                .licensed(fileContextMap.values().stream()
                        .map(FileContext::getFullName)
                        .collect(Collectors.toSet()))
                .globalTagBlackList(Set.of())
                .fileContextMap(fileContextMap)
                .currentArea(areaConfigs.size() == 1 ? areaConfigs.get(0).getArea() : null)
                .build();
    }

    private AreaConfig areaConfig(Area area) {
        return AreaConfig.builder()
                .area(area)
                .build();
    }
}
