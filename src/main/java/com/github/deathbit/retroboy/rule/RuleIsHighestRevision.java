package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

import java.util.Optional;
import java.util.regex.Pattern;

public class RuleIsHighestRevision implements Rule {
    private static final Pattern REV_TAG = Pattern.compile("\\(Rev\\s+(\\d+)\\)");

    @Override
    public boolean pass(RuleContext ruleContext, FileContext fileContext) {
        var newerRevisionFileName = newerRevisionFileName(ruleContext, fileContext.getFileName());
        if (newerRevisionFileName.isEmpty()) {
            return true;
        }

        ruleContext.getFailureReasons().add("IS_HIGHEST_REVISION失败: 存在更高的 Rev 修订版本: " + newerRevisionFileName.get());
        return false;
    }

    private Optional<String> newerRevisionFileName(RuleContext ruleContext, String fileName) {
        if (ruleContext.getFileContextMap() == null) {
            return Optional.empty();
        }

        return ruleContext.getFileContextMap().keySet().stream()
                .filter(candidateFileName -> fileName.equals(previousRevision(candidateFileName)))
                .findFirst();
    }

    private String previousRevision(String filename) {
        var matcher = REV_TAG.matcher(filename);
        if (!matcher.find()) {
            return null;
        }

        try {
            var revision = Integer.parseInt(matcher.group(1));
            if (revision == 1) {
                return filename.substring(0, matcher.start())
                        .concat(filename.substring(matcher.end()))
                        .replaceAll("\\s+\\.", ".")
                        .replaceAll("\\s{2,}", " ")
                        .trim();
            }
            return filename.substring(0, matcher.start())
                    .concat("(Rev " + (revision - 1) + ")")
                    .concat(filename.substring(matcher.end()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
