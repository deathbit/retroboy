package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.game.SSGame;
import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class SSHandler {

    private static final String API_URL_TEMPLATE = "https://api.screenscraper.fr/api2/jeuInfos.php?devid=muldjord&devpassword=uWu5VRc9QDVMPpD8&softname=skyscraper3.20.3&output=json&ssid=zjkiki&sspassword=zjkiki225&gameid=%s";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final Map<String, String> REGION_AREA_MAPPING = Map.ofEntries(
            Map.entry("ae", "UAE"),
            Map.entry("afr", "AFR"),
            Map.entry("ame", "AME"),
            Map.entry("asi", "ASI"),
            Map.entry("au", "AUS"),
            Map.entry("bg", "BGR"),
            Map.entry("br", "BRA"),
            Map.entry("ca", "CAN"),
            Map.entry("cl", "CHL"),
            Map.entry("cn", "CHN"),
            Map.entry("cus", "CUS"),
            Map.entry("cz", "CZE"),
            Map.entry("de", "GER"),
            Map.entry("dk", "DEN"),
            Map.entry("eu", "EUR"),
            Map.entry("fi", "FIN"),
            Map.entry("fr", "FRA"),
            Map.entry("gr", "GRE"),
            Map.entry("hu", "HUN"),
            Map.entry("il", "ISR"),
            Map.entry("it", "ITA"),
            Map.entry("jp", "JPN"),
            Map.entry("kr", "KOR"),
            Map.entry("kw", "KWT"),
            Map.entry("mex", "MEX"),
            Map.entry("mor", "MOR"),
            Map.entry("nl", "NLD"),
            Map.entry("no", "NOR"),
            Map.entry("nz", "NZL"),
            Map.entry("oce", "OCE"),
            Map.entry("pe", "PER"),
            Map.entry("pl", "POL"),
            Map.entry("pt", "POR"),
            Map.entry("ru", "RUS"),
            Map.entry("sa", "SAU"),
            Map.entry("se", "SWE"),
            Map.entry("sk", "SVK"),
            Map.entry("sp", "SPA"),
            Map.entry("ss", "SS"),
            Map.entry("tr", "TUR"),
            Map.entry("tw", "TWN"),
            Map.entry("uk", "UK"),
            Map.entry("us", "USA"),
            Map.entry("wor", "WOR"),
            Map.entry("za", "ZAF")
    );

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();

    public void handle(PlatformContext platformContext) throws Exception {
        var platformName = platformContext.getPlatform().getName();
        var gameIds = readGameIds(platformName);
        var outputDirectory = Path.of("src/main/resources/platform/%s/ss".formatted(platformName));
        Files.createDirectories(outputDirectory);

        var ssGamePackages = new ArrayList<SSGamePackage>();
        ProgressBar pb = new ProgressBar("抓取ScreenScraper数据");
        pb.startTask(gameIds.size());
        for (int i = 0; i < gameIds.size(); i++) {
            var gameId = gameIds.get(i);
            var outputPath = outputDirectory.resolve(gameId + ".json");
            if (!Files.exists(outputPath)) {
                fetchAndSaveGame(gameId, outputPath);
            }
            var ssGamePackage = readGamePackage(outputPath);
            if (ssGamePackage != null) {
                ssGamePackages.add(ssGamePackage);
            }
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
        applySha1Mapping(platformContext, ssGamePackages);
        platformContext.setSsGamePackages(ssGamePackages);
    }

    private void applySha1Mapping(PlatformContext platformContext, List<SSGamePackage> ssGamePackages) {
        var addMappingList = platformContext.getPlatformPackTaskConfig().getSha1MappingAddList();
        var removeMappingList = platformContext.getPlatformPackTaskConfig().getSha1MappingRemoveList();
        if ((addMappingList == null || addMappingList.isEmpty())
                && (removeMappingList == null || removeMappingList.isEmpty())) {
            return;
        }

        var ssGamePackageById = new LinkedHashMap<String, SSGamePackage>();
        for (var ssGamePackage : ssGamePackages) {
            var existing = ssGamePackageById.putIfAbsent(ssGamePackage.getId(), ssGamePackage);
            if (existing != null) {
                throw new IllegalStateException("ScreenScraper game ID 重复: " + ssGamePackage.getId());
            }
        }

        applySha1MappingAddList(addMappingList, ssGamePackageById);
        applySha1MappingRemoveList(removeMappingList, ssGamePackageById);
    }

    private void applySha1MappingAddList(List<String> mappingList, Map<String, SSGamePackage> ssGamePackageById) {
        if (mappingList == null || mappingList.isEmpty()) {
            return;
        }

        for (var mapping : parseSha1Mappings(mappingList, "sha1MappingAddList")) {
            var ssGamePackage = requireSSGamePackage(ssGamePackageById, mapping.gameId(), "sha1MappingAddList");
            var sha1s = ssGamePackage.getSha1s();
            if (sha1s == null) {
                sha1s = new ArrayList<>();
                ssGamePackage.setSha1s(sha1s);
            }
            if (!sha1s.contains(mapping.sha1())) {
                sha1s.add(mapping.sha1());
            }
        }
    }

    private void applySha1MappingRemoveList(List<String> mappingList, Map<String, SSGamePackage> ssGamePackageById) {
        if (mappingList == null || mappingList.isEmpty()) {
            return;
        }

        for (var mapping : parseSha1Mappings(mappingList, "sha1MappingRemoveList")) {
            var ssGamePackage = requireSSGamePackage(ssGamePackageById, mapping.gameId(), "sha1MappingRemoveList");
            var sha1s = ssGamePackage.getSha1s();
            if (sha1s == null || !sha1s.remove(mapping.sha1())) {
                throw new IllegalArgumentException("sha1MappingRemoveList SHA1 不存在: " + mapping.gameId() + " - " + mapping.sha1());
            }
        }
    }

    private List<Sha1Mapping> parseSha1Mappings(List<String> mappingList, String configName) {
        var mappings = new ArrayList<Sha1Mapping>();
        var seenMappings = new LinkedHashSet<String>();
        for (var mapping : mappingList) {
            var parts = mapping.split("\\s+-\\s+", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException(configName + " 格式错误: " + mapping);
            }

            var gameId = parts[0].trim();
            var sha1 = normalizeSha1(parts[1]);
            if (gameId.isEmpty() || sha1.isEmpty()) {
                throw new IllegalArgumentException(configName + " 不能为空: " + mapping);
            }
            var mappingKey = gameId + " - " + sha1;
            if (!seenMappings.add(mappingKey)) {
                throw new IllegalArgumentException(configName + " 映射重复: " + mappingKey);
            }
            mappings.add(new Sha1Mapping(gameId, sha1));
        }
        return mappings;
    }

    private SSGamePackage requireSSGamePackage(Map<String, SSGamePackage> ssGamePackageById, String gameId, String configName) {
        var ssGamePackage = ssGamePackageById.get(gameId);
        if (ssGamePackage == null) {
            throw new IllegalArgumentException(configName + " gameId 不存在: " + gameId);
        }
        return ssGamePackage;
    }

    private List<String> readGameIds(String platformName) throws Exception {
        var ssResource = new ClassPathResource("platform/%s/%s_ss.csv".formatted(platformName, platformName));
        if (!ssResource.exists()) {
            throw new IllegalArgumentException("ScreenScraper游戏列表资源不存在: " + ssResource.getPath());
        }

        var gameIds = new ArrayList<String>();
        try (var reader = new InputStreamReader(ssResource.getInputStream(), StandardCharsets.UTF_8)) {
            var csv = new StringBuilder();
            var buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                csv.append(buffer, 0, read);
            }
            var lines = csv.toString().split("\\R");
            for (int i = 1; i < lines.length; i++) {
                var line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }
                var fields = parseCsvLine(line);
                if (fields.isEmpty() || fields.get(0).isBlank()) {
                    throw new IllegalArgumentException("ScreenScraper游戏列表第%s行缺少Game ID: %s".formatted(i + 1, line));
                }
                gameIds.add(fields.get(0).trim());
            }
        }
        return gameIds;
    }

    private List<String> parseCsvLine(String line) {
        var fields = new ArrayList<String>();
        var field = new StringBuilder();
        var inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            var c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append(c);
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ';' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields;
    }

    private void fetchAndSaveGame(String gameId, Path outputPath) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL_TEMPLATE.formatted(URLEncoder.encode(gameId, StandardCharsets.UTF_8))))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IllegalStateException("ScreenScraper接口调用失败: gameId=%s, httpStatus=%s".formatted(gameId, response.statusCode()));
        }

        var root = JsonParser.parseString(response.body()).getAsJsonObject();
        var header = getObject(root, "header", gameId);
        var success = header.has("success") && "true".equalsIgnoreCase(header.get("success").getAsString());
        if (!success) {
            var error = header.has("error") ? header.get("error").getAsString() : "";
            throw new IllegalStateException("ScreenScraper接口返回失败: gameId=%s, error=%s".formatted(gameId, error));
        }

        var jeu = getObject(getObject(root, "response", gameId), "jeu", gameId);
        Files.writeString(outputPath, GSON.toJson(jeu), StandardCharsets.UTF_8);
    }

    private SSGamePackage readGamePackage(Path inputPath) throws Exception {
        var jeu = JsonParser.parseString(Files.readString(inputPath, StandardCharsets.UTF_8)).getAsJsonObject();
        var packageId = getString(jeu, "id");
        if (packageId == null || packageId.isBlank()) {
            throw new IllegalStateException("ScreenScraper游戏JSON缺少id: " + inputPath);
        }

        var ssGameByArea = buildSSGameByArea(packageId, jeu);
        if (ssGameByArea.isEmpty()) {
            return null;
        }

        return SSGamePackage.builder()
                .id(packageId)
                .ssGameByArea(ssGameByArea)
                .developer(getNestedString(jeu, "developpeur", "text"))
                .publisher(getNestedString(jeu, "editeur", "text"))
                .description(findTextByLanguage(getArray(jeu, "synopsis"), "en"))
                .genre(findGenre(jeu))
                .player(getNestedString(jeu, "joueurs", "text"))
                .sha1s(buildSha1s(jeu))
                .build();
    }

    private Map<String, SSGame> buildSSGameByArea(String packageId, JsonObject jeu) {
        var releaseDateByArea = buildReleaseDateByArea(jeu);
        var ssGameByArea = new LinkedHashMap<String, SSGame>();
        for (var nom : getArray(jeu, "noms")) {
            if (!nom.isJsonObject()) {
                continue;
            }
            var nomObject = nom.getAsJsonObject();
            var region = getString(nomObject, "region");
            if (region == null || "ss".equals(region)) {
                continue;
            }
            var area = mapRegionToArea(region);
            var existing = ssGameByArea.put(area, SSGame.builder()
                    .id(packageId + "." + area)
                    .packageId(packageId)
                    .area(area)
                    .title(getString(nomObject, "text"))
                    .releaseDate(releaseDateByArea.get(area))
                    .build());
            if (existing != null) {
                throw new IllegalStateException("ScreenScraper游戏地区重复: packageId=%s, area=%s".formatted(packageId, area));
            }
        }
        return ssGameByArea;
    }

    private Map<String, String> buildReleaseDateByArea(JsonObject jeu) {
        var releaseDateByArea = new LinkedHashMap<String, String>();
        for (var date : getArray(jeu, "dates")) {
            if (!date.isJsonObject()) {
                continue;
            }
            var dateObject = date.getAsJsonObject();
            var region = getString(dateObject, "region");
            if (region == null || "ss".equals(region)) {
                continue;
            }
            releaseDateByArea.put(mapRegionToArea(region), getString(dateObject, "text"));
        }
        return releaseDateByArea;
    }

    private String findGenre(JsonObject jeu) {
        var genres = getArray(jeu, "genres");
        if (genres.isEmpty() || !genres.get(0).isJsonObject()) {
            return null;
        }
        return findTextByLanguage(getArray(genres.get(0).getAsJsonObject(), "noms"), "en");
    }

    private List<String> buildSha1s(JsonObject jeu) {
        var sha1s = new LinkedHashSet<String>();
        for (var rom : getArray(jeu, "roms")) {
            if (!rom.isJsonObject()) {
                continue;
            }
            var sha1 = getString(rom.getAsJsonObject(), "romsha1");
            if (sha1 != null && !sha1.isBlank()) {
                sha1s.add(normalizeSha1(sha1));
            }
        }
        return new ArrayList<>(sha1s);
    }

    private String normalizeSha1(String sha1) {
        return sha1.trim().toUpperCase(Locale.ROOT);
    }


    private String findTextByLanguage(JsonArray array, String language) {
        for (var element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            var object = element.getAsJsonObject();
            if (language.equals(getString(object, "langue"))) {
                return getString(object, "text");
            }
        }
        return null;
    }

    private String mapRegionToArea(String region) {
        var area = REGION_AREA_MAPPING.get(region);
        if (area == null) {
            throw new IllegalStateException("未知ScreenScraper地区: " + region);
        }
        return area;
    }

    private JsonArray getArray(JsonObject object, String memberName) {
        if (!object.has(memberName) || !object.get(memberName).isJsonArray()) {
            return new JsonArray();
        }
        return object.getAsJsonArray(memberName);
    }

    private String getNestedString(JsonObject object, String objectName, String memberName) {
        if (!object.has(objectName) || !object.get(objectName).isJsonObject()) {
            return null;
        }
        return getString(object.getAsJsonObject(objectName), memberName);
    }

    private String getString(JsonObject object, String memberName) {
        if (!object.has(memberName)) {
            return null;
        }
        JsonElement element = object.get(memberName);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    private JsonObject getObject(JsonObject parent, String memberName, String gameId) {
        if (!parent.has(memberName) || !parent.get(memberName).isJsonObject()) {
            throw new IllegalStateException("ScreenScraper接口返回缺少对象: gameId=%s, member=%s".formatted(gameId, memberName));
        }
        return parent.getAsJsonObject(memberName);
    }

    private record Sha1Mapping(String gameId, String sha1) {
    }
}
