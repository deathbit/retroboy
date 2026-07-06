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

import static org.assertj.core.api.Assertions.assertThat;

class RulesTest {

    @Test
    void shouldEvaluateAreaFileNameBlackList() {
        var fileContext = fileContext("Blocked.nes", "Blocked", Set.of("Japan"));
        var ruleContext = ruleContext(
                Map.of(fileContext.getFileName(), fileContext),
                List.of(AreaConfig.builder()
                        .area(Area.JPN)
                        .fileNameBlackList(Set.of("Blocked.nes"))
                        .build()));

        var result = Rules.IS_JAPAN_BASE.evaluate(ruleContext, fileContext);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailures()).contains("IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST: 命中地区文件名黑名单: Blocked.nes");
    }

    @Test
    void shouldEvaluatePreviousRevisionRule() {
        var fileContextMap = new LinkedHashMap<String, FileContext>();
        fileContextMap.put("Game (USA).nes", fileContext("Game (USA).nes", "Game", Set.of("USA")));
        fileContextMap.put("Game (USA) (Rev 1).nes", fileContext("Game (USA) (Rev 1).nes", "Game", Set.of("USA", "Rev 1")));
        fileContextMap.put("Game (USA) (Rev 2).nes", fileContext("Game (USA) (Rev 2).nes", "Game", Set.of("USA", "Rev 2")));
        var ruleContext = ruleContext(fileContextMap, List.of(areaConfig(Area.USA)));

        var originalResult = Rules.IS_USA_BASE.evaluate(ruleContext, fileContextMap.get("Game (USA).nes"));
        var rev1Result = Rules.IS_USA_BASE.evaluate(ruleContext, fileContextMap.get("Game (USA) (Rev 1).nes"));
        var rev2Result = Rules.IS_USA_BASE.evaluate(ruleContext, fileContextMap.get("Game (USA) (Rev 2).nes"));

        assertThat(originalResult.isPassed()).isFalse();
        assertThat(originalResult.getFailures())
                .contains("IS_NOT_PREVIOUS_REVISION: 存在新版修订，已被替代: Game (USA) (Rev 1).nes");
        assertThat(rev1Result.isPassed()).isFalse();
        assertThat(rev1Result.getFailures())
                .contains("IS_NOT_PREVIOUS_REVISION: 存在新版修订，已被替代: Game (USA) (Rev 2).nes");
        assertThat(rev2Result.isPassed()).isTrue();
    }

    @Test
    void shouldPreferEuropeVersionForEuropeArea() {
        var fileContextMap = Map.of(
                "Game (Europe).nes", fileContext("Game (Europe).nes", "Game", Set.of("Europe")),
                "Game (Germany).nes", fileContext("Game (Germany).nes", "Game", Set.of("Germany")),
                "Other (Germany).nes", fileContext("Other (Germany).nes", "Other", Set.of("Germany"))
        );
        var ruleContext = ruleContext(fileContextMap, List.of(areaConfig(Area.EUR)));

        var europeResult = Rules.IS_EUROPE_BASE.evaluate(ruleContext, fileContextMap.get("Game (Europe).nes"));
        var germanyResult = Rules.IS_EUROPE_BASE.evaluate(ruleContext, fileContextMap.get("Game (Germany).nes"));
        var otherResult = Rules.IS_EUROPE_BASE.evaluate(ruleContext, fileContextMap.get("Other (Germany).nes"));

        assertThat(europeResult.isPassed()).isTrue();
        assertThat(germanyResult.isPassed()).isFalse();
        assertThat(germanyResult.getFailures())
                .contains("PREFER_EUROPE_VERSION: 存在同名 Europe 版本，已排除地区版本，保留: Game (Europe).nes");
        assertThat(otherResult.isPassed()).isTrue();
    }

    private FileContext fileContext(String fileName, String namePart, Set<String> tags) {
        return FileContext.builder()
                .fileName(fileName)
                .fullName(fileName.substring(0, fileName.lastIndexOf('.')))
                .namePart(namePart)
                .tagPart(tags.stream().map(tag -> "(" + tag + ")").reduce("", String::concat))
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
                        .collect(java.util.stream.Collectors.toSet()))
                .globalTagBlackList(Set.of())
                .fileContextMap(fileContextMap)
                .build();
    }

    private AreaConfig areaConfig(Area area) {
        return AreaConfig.builder()
                .area(area)
                .build();
    }
}
