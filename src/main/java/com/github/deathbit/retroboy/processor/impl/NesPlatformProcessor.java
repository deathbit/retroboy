package com.github.deathbit.retroboy.processor.impl;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.domain.WikiDB;
import com.github.deathbit.retroboy.domain.WikiDBPackage;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import com.github.deathbit.retroboy.util.MatchNameUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class NesPlatformProcessor implements PlatformProcessor {

    private static final String WIKI_RESOURCE_PATH = "platform/nes/nes_wiki.html";
    private static final Gson GSON = new Gson();

    @Override
    public Platform platform() {
        return Platform.NES;
    }

    @Override
    public void processGameDB(Map<String, GameDB> gameDBMapByRomName) {
        gameDBMapByRomName.get("Adventures of Lolo (Japan) (En)").setClone("0061");
        gameDBMapByRomName.get("Adventures of Lolo II (Japan)").setClone("0064");
        gameDBMapByRomName.get("Mario Bros. (World)").setRegparent("(USA PARENT) (JPN PARENT) (EUR PARENT)");
        gameDBMapByRomName.get("Mario Bros. (World)").setClone("P");
        gameDBMapByRomName.get("Mike Tyson's Punch-Out!! (Europe) (Rev 1)").setClone("P");
        gameDBMapByRomName.get("Mike Tyson's Punch-Out!! (Europe) (Rev 1)").setRegparent("(EUR PARENT)"); // 1459
        gameDBMapByRomName.get("Mike Tyson's Punch-Out!! (Japan, USA) (En) (Rev 1)").setClone("1459");
        gameDBMapByRomName.get("Mike Tyson's Punch-Out!! (Japan, USA) (En) (Rev 1)").setRegparent("(USA PARENT) (JPN PARENT)");
        gameDBMapByRomName.get("Power Blazer (Japan)").setClone("1703");
        gameDBMapByRomName.get("Nagagutsu o Haita Neko - Sekai Isshuu 80 Nichi Daibouken (Japan)").setClone("1740");
        gameDBMapByRomName.get("Romancia (Japan)").setClone("P");
        gameDBMapByRomName.get("Romancia (Japan)").setRegparent("(JPN PARENT)");
        gameDBMapByRomName.get("Sky Shark (USA)").setRegparent("(USA PARENT)");
        gameDBMapByRomName.get("Tiger-Heli (USA)").setClone("2720");
        gameDBMapByRomName.get("Tiger-Heli (USA)").setRegparent("(USA PARENT)");
        gameDBMapByRomName.get("Kyuukyoku Harikiri Stadium (Japan)").setClone("P");
        gameDBMapByRomName.get("Kyuukyoku Harikiri Stadium (Japan)").setRegparent("(JPN PARENT)");



    }

    @Override
    public List<WikiDBPackage> processWiki() throws Exception {
        var resource = new ClassPathResource(WIKI_RESOURCE_PATH);
        var html = resource.getContentAsString(StandardCharsets.UTF_8);
        var document = Jsoup.parse(html);

        var rawList = new ArrayList<Map<String, WikiDB>>();

        for (var row : document.select("tbody tr")) {
            var cells = row.select("td");
            if (cells.size() < 7) {
                continue;
            }

            var primaryName = extractPrimaryTitle(cells.get(0));
            if (primaryName == null || primaryName.isEmpty()) {
                continue;
            }
            var jpName = extractTitleByRegion(cells.get(0), "JP");
            if (jpName == null || jpName.isEmpty()) {
                jpName = primaryName;
            }
            var usaName = extractTitleByRegion(cells.get(0), "NA");
            if (usaName == null || usaName.isEmpty()) {
                usaName = primaryName;
            }
            var palName = extractTitleByRegion(cells.get(0), "PAL");
            if (palName == null || palName.isEmpty()) {
                palName = primaryName;
            }

            var devCell = cells.get(1);
            var pubCell = cells.get(2);
            var firstReleased = extractDateText(cells.get(3));
            var jpnDate = extractDateText(cells.get(4));
            var usaDate = extractDateText(cells.get(5));
            var palDate = extractDateText(cells.get(6));

            var wikiDBByArea = new LinkedHashMap<String, WikiDB>();
            if (!jpnDate.isEmpty()) {
                wikiDBByArea.put("JPN", buildWikiDB(
                        jpName,
                        extractRegionValue(devCell, "JPN"),
                        extractRegionValue(pubCell, "JPN"),
                        firstReleased, jpnDate));
            }
            if (!usaDate.isEmpty()) {
                wikiDBByArea.put("USA", buildWikiDB(
                        usaName,
                        extractRegionValue(devCell, "USA"),
                        extractRegionValue(pubCell, "USA"),
                        firstReleased, usaDate));
            }
            if (!palDate.isEmpty()) {
                wikiDBByArea.put("PAL", buildWikiDB(
                        palName,
                        extractRegionValue(devCell, "PAL"),
                        extractRegionValue(pubCell, "PAL"),
                        firstReleased, palDate));
            }

            if (wikiDBByArea.isEmpty()) {
                continue;
            }

            rawList.add(wikiDBByArea);
        }

        int total = rawList.size();
        int width = String.valueOf(total).length();
        String idFormat = "%0" + width + "d";

        var packages = new ArrayList<WikiDBPackage>();
        for (int i = 0; i < rawList.size(); i++) {
            var wikiDBByArea = rawList.get(i);
            var packageId = String.format(idFormat, i + 1);
            for (var entry : wikiDBByArea.entrySet()) {
                entry.getValue().setId(packageId + "." + entry.getKey());
            }
            var matchNameByArea = new LinkedHashMap<String, String>();
            wikiDBByArea.forEach((area, wikiDB) ->
                    matchNameByArea.put(area, MatchNameUtils.toMatchName(wikiDB.getName())));
            packages.add(WikiDBPackage.builder()
                                      .id(packageId)
                                      .wikiDBByArea(wikiDBByArea)
                                      .matchNameByArea(matchNameByArea)
                                      .build());
        }

        return packages;
    }

    private WikiDB buildWikiDB(String name, String developer, String publisher,
                               String firstReleased, String releaseDate) {
        return WikiDB.builder()
                     .name(name)
                     .developer(developer)
                     .publisher(publisher)
                     .firstReleased(firstReleased)
                     .releaseDate(releaseDate)
                     .matchName(MatchNameUtils.toMatchName(name))
                     .build();
    }

    /**
     * 提取主标题（无地区 <sup> 标注，或标注非 JP 的第一个 <i>）
     */
    private String extractPrimaryTitle(Element titleCell) {
        for (var iEl : titleCell.select("i")) {
            var next = iEl.nextElementSibling();
            boolean isRegionTagged = next != null && "sup".equals(next.tagName())
                                     && !next.text().isBlank();
            if (!isRegionTagged) {
                return iEl.text().trim();
            }
        }
        var first = titleCell.selectFirst("i");
        return first != null ? first.text().trim() : "";
    }

    /**
     * 提取指定地区专属标题（其后紧跟对应地区 <sup> 标注的 <i>）。
     * area: "JP" → 匹配含 JP 的角标；"NA" → 匹配含 NA 的角标；
     *        "PAL" → 匹配含 PAL / FR / ESP 的角标（法国、西班牙等 PAL 子地区）。
     */
    private String extractTitleByRegion(Element titleCell, String area) {
        for (var iEl : titleCell.select("i")) {
            var next = iEl.nextElementSibling();
            if (next != null && "sup".equals(next.tagName())) {
                var tag = next.text().trim();
                boolean matches = switch (area) {
                    case "JP"  -> tag.contains("JP");
                    case "NA"  -> tag.contains("NA");
                    case "PAL" -> tag.contains("PAL") || tag.contains("FR") || tag.contains("ESP");
                    default    -> false;
                };
                if (matches) {
                    return iEl.text().trim();
                }
            }
        }
        return null;
    }

    /**
     * 从 data-mw 的 dts 模板参数解析日期，格式为 YYYY-MM-DD / YYYY-MM / YYYY。
     * 若单元格为 table-na 或 Unreleased 则返回空字符串。
     */
    private String extractDateText(Element cell) {
        if (cell.hasClass("table-na")) {
            return "";
        }
        var span = cell.selectFirst("span[data-sort-value]");
        if (span != null) {
            return parseDateFromDataMw(span.attr("data-mw"));
        }
        var text = cell.text().trim();
        return text.contains("Unreleased") ? "" : text;
    }

    private String parseDateFromDataMw(String dataMw) {
        if (dataMw == null || dataMw.isEmpty()) return "";
        try {
            var jsonObj = GSON.fromJson(dataMw, JsonObject.class);
            var parts = jsonObj.getAsJsonArray("parts");
            for (var part : parts) {
                if (!part.isJsonObject()) continue;
                var template = part.getAsJsonObject().getAsJsonObject("template");
                if (template == null) continue;
                var target = template.getAsJsonObject("target");
                if (target == null || !"dts".equals(target.get("wt").getAsString())) continue;
                var params = template.getAsJsonObject("params");
                var year  = params.has("1") ? params.getAsJsonObject("1").get("wt").getAsString() : null;
                var month = params.has("2") ? params.getAsJsonObject("2").get("wt").getAsString() : null;
                var day   = params.has("3") ? params.getAsJsonObject("3").get("wt").getAsString() : null;
                if (year == null) return "";
                if (month == null) return year;
                if (day == null) return year + "-" + month;
                return year + "-" + month + "-" + day;
            }
        } catch (Exception ignored) {}
        return "";
    }

    /**
     * 从开发商/发行商单元格中提取指定地区的值。
     * 各条目以 <br> 分隔，每条目可选跟随一个地区角标（JP / NA / PAL / 组合）。
     * 优先匹配地区标注，次选无标注的默认条目，最后兜底取第一条。
     * 忽略 Wikipedia 引用角标（class 含 mw-ref）。
     */
    private String extractRegionValue(Element cell, String area) {
        record Segment(String name, String region) {}
        var segments = new ArrayList<Segment>();
        var currentName = new StringBuilder();
        var currentRegion = "";

        for (var node : cell.childNodes()) {
            if (node instanceof Element el) {
                if ("br".equals(el.tagName())) {
                    var name = currentName.toString().trim();
                    if (!name.isEmpty()) {
                        segments.add(new Segment(name, currentRegion));
                    }
                    currentName = new StringBuilder();
                    currentRegion = "";
                } else if ("sup".equals(el.tagName()) && !el.hasClass("mw-ref")) {
                    currentRegion = el.text().trim();
                } else if (!"sup".equals(el.tagName())) {
                    currentName.append(el.text());
                }
            } else if (node instanceof TextNode tn) {
                currentName.append(tn.text());
            }
        }
        var lastName = currentName.toString().trim();
        if (!lastName.isEmpty()) {
            segments.add(new Segment(lastName, currentRegion));
        }

        for (var seg : segments) {
            if (matchesArea(seg.region(), area)) {
                return seg.name();
            }
        }
        for (var seg : segments) {
            if (seg.region().isEmpty()) {
                return seg.name();
            }
        }
        return segments.isEmpty() ? "" : segments.get(0).name();
    }

    private boolean matchesArea(String region, String area) {
        if (region.isEmpty()) return false;
        return switch (area) {
            case "JPN" -> region.contains("JP");
            case "USA" -> region.contains("NA");
            case "PAL" -> region.contains("PAL");
            default -> false;
        };
    }
}
