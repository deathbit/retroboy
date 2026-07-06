package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RulesTest {

    @Test
    void shouldEvaluateAreaFileNameBlackList() {
        var areaConfig = AreaConfig.builder()
                .fileNameBlackList(Set.of("Blocked.nes"))
                .build();
        var ruleContext = RuleContext.builder().build();
        var fileContext = FileContext.builder()
                .fileName("Blocked.nes")
                .build();

        var result = Rules.isNotHitAreaFileNameBlackList(areaConfig).evaluate(ruleContext, fileContext);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailures()).containsExactly("IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST: 命中地区文件名黑名单: Blocked.nes");
    }

    @Test
    void shouldApplyPreviousRevisionRule() {
        var fileContextMap = new LinkedHashMap<String, FileContext>();
        fileContextMap.put("Game.nes", fileContext("Game.nes", "Game", Set.of()));
        fileContextMap.put("Game (Rev 1).nes", fileContext("Game (Rev 1).nes", "Game", Set.of("Rev 1")));
        fileContextMap.put("Game (Rev 2).nes", fileContext("Game (Rev 2).nes", "Game", Set.of("Rev 2")));
        var skippedFileReasonMap = new LinkedHashMap<String, String>();
        var ruleContext = RuleContext.builder()
                .fileContextMap(fileContextMap)
                .skippedFileReasonMap(skippedFileReasonMap)
                .build();

        Rules.applyPreviousRevisionRule(ruleContext);

        assertThat(ruleContext.getFileContextMap()).containsOnlyKeys("Game (Rev 2).nes");
        assertThat(ruleContext.getSkippedFileReasonMap())
                .containsEntry("Game.nes", "存在新版修订，已被替代: Game (Rev 1).nes")
                .containsEntry("Game (Rev 1).nes", "存在新版修订，已被替代: Game (Rev 2).nes");
    }

    @Test
    void shouldPreferEuropeVersionForEuropeArea() {
        var areaFinalMap = Map.of(Area.EUR, new LinkedHashSet<>(List.of(
                "Game (Europe).nes",
                "Game (Germany).nes",
                "Other (Germany).nes"
        )));
        var europeRuleResultMap = new LinkedHashMap<String, RuleResult>();
        var areaRuleResultMap = Map.of(Area.EUR, europeRuleResultMap);
        var ruleContext = RuleContext.builder()
                .fileContextMap(Map.of(
                        "Game (Europe).nes", fileContext("Game (Europe).nes", "Game", Set.of("Europe")),
                        "Game (Germany).nes", fileContext("Game (Germany).nes", "Game", Set.of("Germany")),
                        "Other (Germany).nes", fileContext("Other (Germany).nes", "Other", Set.of("Germany"))
                ))
                .areaFinalMap(areaFinalMap)
                .areaRuleResultMap(areaRuleResultMap)
                .build();

        Rules.preferEuropeVersionForEuropeArea(ruleContext);

        assertThat(ruleContext.getAreaFinalMap().get(Area.EUR))
                .containsExactly("Game (Europe).nes", "Other (Germany).nes");
        assertThat(europeRuleResultMap.get("Game (Germany).nes").getFailures())
                .containsExactly("PREFER_EUROPE_VERSION: 存在同名 Europe 版本，已排除地区版本，保留: Game (Europe).nes");
    }

    private FileContext fileContext(String fileName, String namePart, Set<String> tags) {
        return FileContext.builder()
                .fileName(fileName)
                .namePart(namePart)
                .tags(tags)
                .build();
    }
}
