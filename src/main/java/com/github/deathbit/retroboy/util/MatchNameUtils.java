package com.github.deathbit.retroboy.util;

import java.text.Normalizer;

public class MatchNameUtils {

    private MatchNameUtils() {}

    private static String moveArticleToFront(String name) {
        for (String article : new String[]{", The", ", A"}) {
            int idx = name.indexOf(article);
            if (idx != -1) {
                String articleWord = article.substring(2); // "The" or "A"
                String rest = name.substring(0, idx) + name.substring(idx + article.length());
                return articleWord + " " + rest;
            }
        }
        return name;
    }

    /**
     * 将名称转换为用于比较的标准化字符串，两个数据源统一处理。
     * 处理步骤：
     *   1. 前置冠词：", The" / ", A" 移到字符串开头
     *   2. 长元音：Ā→Aa，ā→aa，Ē→Ee，ē→ee，Ī→Ii，ī→ii，Ō→Ou，ō→ou，Ū→Uu，ū→uu
     *   2.5. 拉丁重音折叠：NFD 分解后去掉组合符，é→e，à→a，ü→u 等
     *   3. 符号：& → and
     *   3.5. 标点：.,!'?`"()[] 直接删除；-:_/ 替换为空格
     *   4. 罗马数字（大写，10以内，匹配独立词）→ 阿拉伯数字
     *   5. 转小写，只保留字母和数字，连续空格合并为一个，trim
     */
    public static String toMatchName(String name) {
        // Step 2: 长元音（必须在 NFD 分解前完成，否则宏音符会被提前剥离）
        var s = moveArticleToFront(name)
                .replace("Ā", "Aa")
                .replace("ā", "aa")
                .replace("Ē", "Ee")
                .replace("ē", "ee")
                .replace("Ī", "Ii")
                .replace("ī", "ii")
                .replace("Ō", "Ou")
                .replace("ō", "ou")
                .replace("Ū", "Uu")
                .replace("ū", "uu");
        // Step 2.5: 拉丁重音折叠（NFD 分解 + 去掉所有组合符）：é→e，à→a，ü→u 等
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s
                // Step 3: & → and
                .replace("&", "and")
                // Step 3.5: 标点处理（在罗马数字转换前，避免 T.V. 中的 V 被误识别）
                .replaceAll("[.!'?`\"()\\[\\]]", "")  // 直接删除
                .replaceAll("[-:_/]", "")             // 直接删除
                // Step 4: 罗马数字（长优先，避免 VIII 被 VI/II 截断）
                .replaceAll("\\bVIII\\b", "8")
                .replaceAll("\\bVII\\b", "7")
                .replaceAll("\\bVI\\b", "6")
                .replaceAll("\\bIX\\b", "9")
                .replaceAll("\\bIV\\b", "4")
                .replaceAll("\\bIII\\b", "3")
                .replaceAll("\\bII\\b", "2")
                .replaceAll("\\bX\\b", "10")
                .replaceAll("\\bV\\b", "5")
                .replaceAll("\\bI\\b", "1")
                // Step 5: 通用处理
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .replaceAll(" +", " ")
                .trim();
    }
}
