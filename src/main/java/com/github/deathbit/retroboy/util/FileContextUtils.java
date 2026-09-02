package com.github.deathbit.retroboy.util;

import com.github.deathbit.retroboy.domain.FileContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FileContextUtils {

    private FileContextUtils() {
    }

    public static Map<String, FileContext> buildLookupMap(List<FileContext> fileContexts) {
        var lookupMap = new LinkedHashMap<String, FileContext>();
        if (fileContexts == null) {
            return lookupMap;
        }
        for (var fileContext : fileContexts) {
            putLookupName(lookupMap, fileContext.getFullName(), fileContext);
            if (fileContext.getAliasNames() == null) {
                continue;
            }
            for (var aliasName : fileContext.getAliasNames()) {
                putLookupName(lookupMap, aliasName, fileContext);
            }
        }
        return lookupMap;
    }

    public static FileContext requireFileContext(Map<String, FileContext> lookupMap, String fullName) {
        var fileContext = lookupMap.get(fullName);
        if (fileContext == null) {
            throw new RuntimeException("FileContext not found for rom: " + fullName);
        }
        return fileContext;
    }

    private static void putLookupName(Map<String, FileContext> lookupMap, String lookupName, FileContext fileContext) {
        if (lookupName == null || lookupName.isBlank()) {
            return;
        }
        var existing = lookupMap.putIfAbsent(lookupName, fileContext);
        if (existing != null && existing != fileContext) {
            throw new RuntimeException("FileContext lookup name conflict: " + lookupName);
        }
    }
}

