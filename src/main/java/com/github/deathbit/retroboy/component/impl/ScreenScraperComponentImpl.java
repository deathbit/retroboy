package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ScreenScraperComponent;
import com.github.deathbit.retroboy.domain.screenscraper.ApiCredentials;
import com.github.deathbit.retroboy.domain.screenscraper.Classification;
import com.github.deathbit.retroboy.domain.screenscraper.Family;
import com.github.deathbit.retroboy.domain.screenscraper.Game;
import com.github.deathbit.retroboy.domain.screenscraper.GameInfo;
import com.github.deathbit.retroboy.domain.screenscraper.GameMediaInfo;
import com.github.deathbit.retroboy.domain.screenscraper.GameSystem;
import com.github.deathbit.retroboy.domain.screenscraper.Genre;
import com.github.deathbit.retroboy.domain.screenscraper.Language;
import com.github.deathbit.retroboy.domain.screenscraper.PlayerCount;
import com.github.deathbit.retroboy.domain.screenscraper.Region;
import com.github.deathbit.retroboy.domain.screenscraper.Rom;
import com.github.deathbit.retroboy.domain.screenscraper.RomInfo;
import com.github.deathbit.retroboy.domain.screenscraper.RomType;
import com.github.deathbit.retroboy.domain.screenscraper.ServerInfo;
import com.github.deathbit.retroboy.domain.screenscraper.SupportType;
import com.github.deathbit.retroboy.domain.screenscraper.SystemMediaInfo;
import com.github.deathbit.retroboy.domain.screenscraper.UserInfo;
import com.github.deathbit.retroboy.domain.screenscraper.UserLevel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ScreenScraper API component
 * Handles all API requests with proper error handling for ScreenScraper-specific error codes
 */
@Component
public class ScreenScraperComponentImpl implements ScreenScraperComponent {

    private static final String BASE_URL = "https://api.screenscraper.fr/api2";
    private static final String OUTPUT_FORMAT = "json";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ScreenScraperComponentImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Default constructor for Spring autowiring
     */
    public ScreenScraperComponentImpl() {
        this(new RestTemplate(), new ObjectMapper());
    }

    /**
     * Build base URL with common authentication parameters
     */
    private String buildBaseUrl(String endpoint, ApiCredentials credentials) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL + "/" + endpoint)
                .queryParam("devid", credentials.getDevId())
                .queryParam("devpassword", credentials.getDevPassword())
                .queryParam("softname", credentials.getSoftName())
                .queryParam("output", OUTPUT_FORMAT);

        if (credentials.getSsId() != null && !credentials.getSsId().isEmpty()) {
            builder.queryParam("ssid", credentials.getSsId());
        }
        if (credentials.getSsPassword() != null && !credentials.getSsPassword().isEmpty()) {
            builder.queryParam("sspassword", credentials.getSsPassword());
        }

        return builder.toUriString();
    }

    /**
     * Handle HTTP errors from ScreenScraper API with specific error messages
     */
    private void handleApiError(Exception ex) throws Exception {
        int statusCode;
        if (ex instanceof HttpClientErrorException) {
            statusCode = ((HttpClientErrorException) ex).getStatusCode().value();
        } else if (ex instanceof HttpServerErrorException) {
            statusCode = ((HttpServerErrorException) ex).getStatusCode().value();
        } else {
            throw ex;
        }

        switch (statusCode) {
            case 400:
                throw new Exception("Bad Request: Problem with the URL or missing required fields - " + ex.getMessage());
            case 401:
                throw new Exception("Unauthorized: API closed for non-members or inactive members - " + ex.getMessage());
            case 403:
                throw new Exception("Forbidden: Login error - Check your developer credentials - " + ex.getMessage());
            case 404:
                throw new Exception("Not Found: Game or ROM not found - " + ex.getMessage());
            case 423:
                throw new Exception("Locked: API completely closed due to server issues - " + ex.getMessage());
            case 426:
                throw new Exception("Upgrade Required: Software has been blacklisted - Update your software version - " + ex.getMessage());
            case 429:
                throw new Exception("Too Many Requests: Thread or quota limit reached - Reduce query speed - " + ex.getMessage());
            case 430:
                throw new Exception("Quota Exceeded: Daily scrape quota exceeded - " + ex.getMessage());
            case 431:
                throw new Exception("Quota Exceeded: Too many unrecognized ROMs - Sort through your files - " + ex.getMessage());
            default:
                throw new Exception("API Error: " + statusCode + " - " + ex.getMessage());
        }
    }

    /**
     * Execute GET request with error handling
     */
    private String executeGetRequest(String url) throws Exception {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            handleApiError(ex);
            throw ex; // This line is unreachable but required for compilation
        }
    }

    /**
     * Execute GET request for binary data (images, videos, PDFs)
     */
    private byte[] executeGetBinaryRequest(String url) throws Exception {
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            // Check if response is text indicating no change (CRCOK, MD5OK, SHA1OK, NOMEDIA)
            if (response.getBody() != null && response.getBody().length < 100) {
                String responseText = new String(response.getBody());
                if (responseText.contains("OK") || responseText.contains("NOMEDIA")) {
                    return null; // No update needed or media not found
                }
            }

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            handleApiError(ex);
            throw ex; // This line is unreachable but required for compilation
        }
    }

    @Override
    public ServerInfo getInfrastructureInfo(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("ssinfraInfos.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return null;

        JsonNode root = objectMapper.readTree(response);
        JsonNode serverNode = root.path("response").path("serveurs");

        return ServerInfo.builder()
                .cpu1(serverNode.path("cpu1").asDouble())
                .cpu2(serverNode.path("cpu2").asDouble())
                .cpu3(serverNode.path("cpu3").asDouble())
                .threadsMin(serverNode.path("threadsmin").asInt())
                .nbScrapeurs(serverNode.path("nbscrapeurs").asInt())
                .apiAcces(serverNode.path("apiacces").asInt())
                .closeForNoMember(serverNode.path("closefornomember").asInt())
                .closeForLeecher(serverNode.path("closeforleecher").asInt())
                .maxThreadForNonMember(serverNode.path("maxthreadfornonmember").asInt())
                .threadForNonMember(serverNode.path("threadfornonmember").asInt())
                .maxThreadForMember(serverNode.path("maxthreadformember").asInt())
                .threadForMember(serverNode.path("threadformember").asInt())
                .build();
    }

    @Override
    public UserInfo getUserInfo(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("ssuserInfos.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return null;

        JsonNode root = objectMapper.readTree(response);
        JsonNode userNode = root.path("response").path("ssuser");

        return UserInfo.builder()
                .id(userNode.path("id").asString())
                .numId(userNode.path("numid").asLong())
                .level(userNode.path("niveau").asInt())
                .contribution(userNode.path("contribution").asInt())
                .uploadSysteme(userNode.path("uploadsysteme").asInt())
                .uploadInfos(userNode.path("uploadinfos").asInt())
                .romAsso(userNode.path("romasso").asInt())
                .uploadMedia(userNode.path("uploadmedia").asInt())
                .propositionOk(userNode.path("propositionok").asInt())
                .propositionKo(userNode.path("propositionko").asInt())
                .quotaRefu(userNode.path("quotarefu").asDouble())
                .maxThreads(userNode.path("maxthreads").asInt())
                .maxDownloadSpeed(userNode.path("maxdownloadspeed").asInt())
                .requestsToday(userNode.path("requeststoday").asInt())
                .requestsKoToday(userNode.path("requestskotoday").asInt())
                .maxRequestsPerMin(userNode.path("maxrequestspermin").asInt())
                .maxRequestsPerDay(userNode.path("maxrequestsperday").asInt())
                .maxRequestsKoPerDay(userNode.path("maxrequestskoperday").asInt())
                .visits(userNode.path("visites").asInt())
                .lastVisitDate(userNode.path("datedernierevisite").asString())
                .favRegion(userNode.path("favregion").asString())
                .build();
    }

    @Override
    public List<UserLevel> getUserLevels(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("userlevelsListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode levelsNode = root.path("response").path("userlevels");

        List<UserLevel> levels = new ArrayList<>();
        if (levelsNode.isArray()) {
            for (JsonNode node : levelsNode) {
                levels.add(UserLevel.builder()
                        .id(node.path("id").asInt())
                        .nomFr(node.path("nom_fr").asString())
                        .build());
            }
        }
        return levels;
    }

    @Override
    public List<PlayerCount> getPlayerCounts(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("nbJoueursListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode playersNode = root.path("response").path("nbjoueurs");

        List<PlayerCount> counts = new ArrayList<>();
        if (playersNode.isArray()) {
            for (JsonNode node : playersNode) {
                counts.add(PlayerCount.builder()
                        .id(node.path("id").asInt())
                        .name(node.path("nom").asString())
                        .parent(node.path("parent").asInt())
                        .build());
            }
        }
        return counts;
    }

    @Override
    public List<SupportType> getSupportTypes(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("supportTypesListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode typesNode = root.path("response").path("supporttypes");

        List<SupportType> types = new ArrayList<>();
        if (typesNode.isArray()) {
            for (JsonNode node : typesNode) {
                types.add(SupportType.builder()
                        .name(node.path("nom").asString())
                        .build());
            }
        }
        return types;
    }

    @Override
    public List<RomType> getRomTypes(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("romTypesListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode typesNode = root.path("response").path("romtypes");

        List<RomType> types = new ArrayList<>();
        if (typesNode.isArray()) {
            for (JsonNode node : typesNode) {
                types.add(RomType.builder()
                        .name(node.path("nom").asString())
                        .build());
            }
        }
        return types;
    }

    @Override
    public List<Region> getRegions(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("regionsListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode regionsNode = root.path("response").path("regions");

        List<Region> regions = new ArrayList<>();
        if (regionsNode.isArray()) {
            for (JsonNode node : regionsNode) {
                regions.add(Region.builder()
                        .id(node.path("id").asInt())
                        .shortName(node.path("nomcourt").asString())
                        .nameDe(node.path("nom_de").asString())
                        .nameEn(node.path("nom_en").asString())
                        .nameEs(node.path("nom_es").asString())
                        .nameFr(node.path("nom_fr").asString())
                        .nameIt(node.path("nom_it").asString())
                        .namePt(node.path("nom_pt").asString())
                        .parent(node.path("parent").asInt())
                        .medias(parseMedias(node.path("medias")))
                        .build());
            }
        }
        return regions;
    }

    @Override
    public List<Language> getLanguages(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("languesListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode languagesNode = root.path("response").path("langues");

        List<Language> languages = new ArrayList<>();
        if (languagesNode.isArray()) {
            for (JsonNode node : languagesNode) {
                languages.add(Language.builder()
                        .id(node.path("id").asInt())
                        .shortName(node.path("nomcourt").asString())
                        .nameDe(node.path("nom_de").asString())
                        .nameEn(node.path("nom_en").asString())
                        .nameEs(node.path("nom_es").asString())
                        .nameFr(node.path("nom_fr").asString())
                        .nameIt(node.path("nom_it").asString())
                        .namePt(node.path("nom_pt").asString())
                        .parent(node.path("parent").asInt())
                        .medias(parseMedias(node.path("medias")))
                        .build());
            }
        }
        return languages;
    }

    @Override
    public List<Genre> getGenres(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("genresListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode genresNode = root.path("response").path("genres");

        List<Genre> genres = new ArrayList<>();
        if (genresNode.isArray()) {
            for (JsonNode node : genresNode) {
                genres.add(Genre.builder()
                        .id(node.path("id").asInt())
                        .nameDe(node.path("nom_de").asString())
                        .nameEn(node.path("nom_en").asString())
                        .nameEs(node.path("nom_es").asString())
                        .nameFr(node.path("nom_fr").asString())
                        .nameIt(node.path("nom_it").asString())
                        .namePt(node.path("nom_pt").asString())
                        .parent(node.path("parent").asInt())
                        .medias(parseMedias(node.path("medias")))
                        .build());
            }
        }
        return genres;
    }

    @Override
    public List<Family> getFamilies(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("famillesListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode familiesNode = root.path("response").path("familles");

        List<Family> families = new ArrayList<>();
        if (familiesNode.isArray()) {
            for (JsonNode node : familiesNode) {
                families.add(Family.builder()
                        .id(node.path("id").asInt())
                        .name(node.path("nom").asString())
                        .medias(parseMedias(node.path("medias")))
                        .build());
            }
        }
        return families;
    }

    @Override
    public List<Classification> getClassifications(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("classificationsListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode classificationsNode = root.path("response").path("classifications");

        List<Classification> classifications = new ArrayList<>();
        if (classificationsNode.isArray()) {
            for (JsonNode node : classificationsNode) {
                classifications.add(Classification.builder()
                        .id(node.path("id").asInt())
                        .shortName(node.path("nomcourt").asString())
                        .nameDe(node.path("nom_de").asString())
                        .nameEn(node.path("nom_en").asString())
                        .nameEs(node.path("nom_es").asString())
                        .nameFr(node.path("nom_fr").asString())
                        .nameIt(node.path("nom_it").asString())
                        .namePt(node.path("nom_pt").asString())
                        .parent(node.path("parent").asInt())
                        .medias(parseMedias(node.path("medias")))
                        .build());
            }
        }
        return classifications;
    }

    @Override
    public List<SystemMediaInfo> getSystemMediaList(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("mediasSystemeListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode mediasNode = root.path("response").path("medias");

        List<SystemMediaInfo> mediaList = new ArrayList<>();
        if (mediasNode.isArray()) {
            for (JsonNode node : mediasNode) {
                mediaList.add(SystemMediaInfo.builder()
                        .id(node.path("id").asInt())
                        .shortName(node.path("nomcourt").asString())
                        .name(node.path("nom").asString())
                        .category(node.path("categorie").asString())
                        .platformTypes(node.path("platformtypes").asString())
                        .platforms(node.path("platforms").asString())
                        .type(node.path("type").asString())
                        .fileFormat(node.path("format").asString())
                        .fileFormat2(node.path("format2").asString())
                        .autoGen(node.path("autogen").asInt())
                        .multiRegions(node.path("multiregions").asInt())
                        .multiPlatforms(node.path("multiplatforms").asInt())
                        .multiVersions(node.path("multiversions").asInt())
                        .extraInfosTxt(node.path("extrainfostxt").asString())
                        .build());
            }
        }
        return mediaList;
    }

    @Override
    public List<GameMediaInfo> getGameMediaList(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("mediasJeuListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode mediasNode = root.path("response").path("medias");

        List<GameMediaInfo> mediaList = new ArrayList<>();
        if (mediasNode.isArray()) {
            for (JsonNode node : mediasNode) {
                mediaList.add(GameMediaInfo.builder()
                        .id(node.path("id").asInt())
                        .shortName(node.path("nomcourt").asString())
                        .name(node.path("nom").asString())
                        .category(node.path("categorie").asString())
                        .platformTypes(node.path("platformtypes").asString())
                        .platforms(node.path("platforms").asString())
                        .type(node.path("type").asString())
                        .fileFormat(node.path("format").asString())
                        .fileFormat2(node.path("format2").asString())
                        .autoGen(node.path("autogen").asInt())
                        .multiRegions(node.path("multiregions").asInt())
                        .multiPlatforms(node.path("multiplatforms").asInt())
                        .multiVersions(node.path("multiversions").asInt())
                        .extraInfosTxt(node.path("extrainfostxt").asString())
                        .build());
            }
        }
        return mediaList;
    }

    @Override
    public List<GameInfo> getGameInfoList(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("infosJeuListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode infosNode = root.path("response").path("infos");

        List<GameInfo> infoList = new ArrayList<>();
        if (infosNode.isArray()) {
            for (JsonNode node : infosNode) {
                infoList.add(GameInfo.builder()
                        .id(node.path("id").asInt())
                        .shortName(node.path("nomcourt").asString())
                        .name(node.path("nom").asString())
                        .category(node.path("categorie").asString())
                        .platformTypes(node.path("platformtypes").asString())
                        .platforms(node.path("platforms").asString())
                        .type(node.path("type").asString())
                        .autoGen(node.path("autogen").asInt())
                        .multiRegions(node.path("multiregions").asInt())
                        .multiSupports(node.path("multisupports").asInt())
                        .multiVersions(node.path("multiversions").asInt())
                        .multiChoice(node.path("multichoice").asInt())
                        .build());
            }
        }
        return infoList;
    }

    @Override
    public List<RomInfo> getRomInfoList(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("infosRomListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode infosNode = root.path("response").path("infos");

        List<RomInfo> infoList = new ArrayList<>();
        if (infosNode.isArray()) {
            for (JsonNode node : infosNode) {
                infoList.add(RomInfo.builder()
                        .id(node.path("id").asInt())
                        .shortName(node.path("nomcourt").asString())
                        .name(node.path("nom").asString())
                        .category(node.path("categorie").asString())
                        .platformTypes(node.path("platformtypes").asString())
                        .platforms(node.path("platforms").asString())
                        .type(node.path("type").asString())
                        .autoGen(node.path("autogen").asInt())
                        .multiRegions(node.path("multiregions").asInt())
                        .multiSupports(node.path("multisupports").asInt())
                        .multiVersions(node.path("multiversions").asInt())
                        .multiChoice(node.path("multichoice").asInt())
                        .build());
            }
        }
        return infoList;
    }

    @Override
    public byte[] downloadGroupMedia(ApiCredentials credentials, Integer groupId, String media,
                                     String crc, String md5, String sha1, Integer maxWidth, Integer maxHeight) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaGroup.php", credentials))
                .queryParam("groupid", groupId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);
        if (maxWidth != null) builder.queryParam("maxwidth", maxWidth);
        if (maxHeight != null) builder.queryParam("maxheight", maxHeight);

        return executeGetBinaryRequest(builder.toUriString());
    }

    @Override
    public byte[] downloadCompanyMedia(ApiCredentials credentials, Integer companyId, String media,
                                       String crc, String md5, String sha1, Integer maxWidth, Integer maxHeight) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaCompagnie.php", credentials))
                .queryParam("companyid", companyId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);
        if (maxWidth != null) builder.queryParam("maxwidth", maxWidth);
        if (maxHeight != null) builder.queryParam("maxheight", maxHeight);

        return executeGetBinaryRequest(builder.toUriString());
    }

    @Override
    public List<GameSystem> getSystemList(ApiCredentials credentials) throws Exception {
        String url = buildBaseUrl("systemesListe.php", credentials);
        String response = executeGetRequest(url);

        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode systemsNode = root.path("response").path("systemes");

        List<GameSystem> systems = new ArrayList<>();
        if (systemsNode.isArray()) {
            for (JsonNode node : systemsNode) {
                systems.add(GameSystem.builder()
                        .id(node.path("id").asInt())
                        .parentId(node.path("parentid").asInt())
                        .names(parseNames(node.path("noms")))
                        .extensions(node.path("extensions").asString())
                        .company(node.path("compagnie").asString())
                        .type(node.path("type").asString())
                        .startDate(node.path("datedebut").asString())
                        .endDate(node.path("datefin").asString())
                        .romType(node.path("romtype").asString())
                        .supportType(node.path("supporttype").asString())
                        .medias(parseNestedMedias(node.path("medias")))
                        .build());
            }
        }
        return systems;
    }

    @Override
    public byte[] downloadSystemMedia(ApiCredentials credentials, Integer systemId, String media,
                                      String crc, String md5, String sha1, Integer maxWidth, Integer maxHeight) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaSysteme.php", credentials))
                .queryParam("systemeid", systemId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);
        if (maxWidth != null) builder.queryParam("maxwidth", maxWidth);
        if (maxHeight != null) builder.queryParam("maxheight", maxHeight);

        return executeGetBinaryRequest(builder.toUriString());
    }

    @Override
    public byte[] downloadSystemVideo(ApiCredentials credentials, Integer systemId, String media,
                                      String crc, String md5, String sha1) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaVideoSysteme.php", credentials))
                .queryParam("systemeid", systemId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);

        return executeGetBinaryRequest(builder.toUriString());
    }

    @Override
    public List<Game> searchGames(ApiCredentials credentials, Integer systemId, String searchTerm) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("jeuRecherche.php", credentials))
                .queryParam("recherche", searchTerm);

        if (systemId != null) {
            builder.queryParam("systemeid", systemId);
        }

        String response = executeGetRequest(builder.toUriString());
        if (response == null) return Collections.emptyList();

        JsonNode root = objectMapper.readTree(response);
        JsonNode gamesNode = root.path("response").path("jeux");

        List<Game> games = new ArrayList<>();
        if (gamesNode.isArray()) {
            for (JsonNode node : gamesNode) {
                games.add(parseGame(node));
            }
        }
        return games;
    }

    @Override
    public Game getGameInfo(ApiCredentials credentials, Integer systemId, String crc, String md5, String sha1,
                            String romType, String romName, Long romSize, Integer gameId) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("jeuInfos.php", credentials))
                .queryParam("systemeid", systemId);

        if (gameId != null) {
            builder.queryParam("gameid", gameId);
        } else {
            if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
            if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
            if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);
            if (romType != null) builder.queryParam("romtype", romType);
            if (romName != null) builder.queryParam("romnom", romName);
            if (romSize != null) builder.queryParam("romtaille", romSize);
        }

        String response = executeGetRequest(builder.toUriString());
        if (response == null) return null;

        JsonNode root = objectMapper.readTree(response);
        JsonNode gameNode = root.path("response").path("jeu");

        return parseGame(gameNode);
    }

    @Override
    public byte[] downloadGameMedia(ApiCredentials credentials, Integer systemId, Integer gameId, String media,
                                    String crc, String md5, String sha1, Integer maxWidth, Integer maxHeight) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaJeu.php", credentials))
                .queryParam("systemeid", systemId)
                .queryParam("jeuid", gameId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);
        if (maxWidth != null) builder.queryParam("maxwidth", maxWidth);
        if (maxHeight != null) builder.queryParam("maxheight", maxHeight);

        return executeGetBinaryRequest(builder.toUriString());
    }

    @Override
    public byte[] downloadGameVideo(ApiCredentials credentials, Integer systemId, Integer gameId, String media,
                                    String crc, String md5, String sha1) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaVideoJeu.php", credentials))
                .queryParam("systemeid", systemId)
                .queryParam("jeuid", gameId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);

        return executeGetBinaryRequest(builder.toUriString());
    }

    @Override
    public byte[] downloadGameManual(ApiCredentials credentials, Integer systemId, Integer gameId, String media,
                                     String crc, String md5, String sha1) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaManuelJeu.php", credentials))
                .queryParam("systemeid", systemId)
                .queryParam("jeuid", gameId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);

        return executeGetBinaryRequest(builder.toUriString());
    }

    @Override
    public String submitGameRating(ApiCredentials credentials, Integer gameId, Integer rating) throws Exception {
        if (rating < 1 || rating > 20) {
            throw new Exception("Rating must be between 1 and 20");
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("botNote.php", credentials))
                .queryParam("gameid", gameId)
                .queryParam("note", rating);

        return executeGetRequest(builder.toUriString());
    }

    @Override
    public String submitProposal(ApiCredentials credentials, Integer gameId, Integer romId,
                                 Map<String, Object> proposalData) throws Exception {
        // Build URL with basic parameters including developer credentials
        String baseUrl = BASE_URL + "/botProposition.php";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("devid", credentials.getDevId())
                .queryParam("devpassword", credentials.getDevPassword())
                .queryParam("softname", credentials.getSoftName())
                .queryParam("ssid", credentials.getSsId())
                .queryParam("sspassword", credentials.getSsPassword());

        if (gameId != null) {
            builder.queryParam("gameid", gameId);
        }
        if (romId != null) {
            builder.queryParam("romid", romId);
        }

        // Create multipart form data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (Map.Entry<String, Object> entry : proposalData.entrySet()) {
            body.add(entry.getKey(), entry.getValue());
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(builder.toUriString(), requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            handleApiError(ex);
            throw ex; // This line is unreachable but required for compilation
        }
    }

    /**
     * Helper method to parse medias from JSON node
     */
    private Map<String, String> parseMedias(JsonNode mediasNode) {
        Map<String, String> medias = new HashMap<>();
        if (mediasNode != null && !mediasNode.isMissingNode()) {
            mediasNode.properties().forEach(entry ->
                    medias.put(entry.getKey(), entry.getValue().asString())
            );
        }
        return medias;
    }

    /**
     * Helper method to parse nested medias (for systems and games)
     */
    private Map<String, Object> parseNestedMedias(JsonNode mediasNode) {
        Map<String, Object> medias = new HashMap<>();
        if (mediasNode != null && !mediasNode.isMissingNode()) {
            mediasNode.properties().forEach(entry -> {
                if (entry.getValue().isObject()) {
                    medias.put(entry.getKey(), parseMedias(entry.getValue()));
                } else {
                    medias.put(entry.getKey(), entry.getValue().asString());
                }
            });
        }
        return medias;
    }

    /**
     * Helper method to parse names from JSON node
     */
    private Map<String, String> parseNames(JsonNode namesNode) {
        Map<String, String> names = new HashMap<>();
        if (namesNode != null && !namesNode.isMissingNode()) {
            namesNode.properties().forEach(entry ->
                    names.put(entry.getKey(), entry.getValue().asString())
            );
        }
        return names;
    }

    /**
     * Helper method to parse game information from JSON node
     */
    private Game parseGame(JsonNode node) {
        if (node == null || node.isMissingNode()) return null;

        Game.GameBuilder builder = Game.builder()
                .id(node.path("id").asInt())
                .romId(node.path("romid").asInt())
                .notGame(node.path("notgame").asBoolean())
                .name(node.path("nom").asString())
                .names(parseNames(node.path("noms")))
                .cloneOf(node.path("cloneof").asInt())
                .editor(node.path("editeur").asString())
                .developer(node.path("developpeur").asString())
                .players(node.path("joueurs").asString())
                .score(node.path("note").asDouble())
                .topStaff(node.path("topstaff").asInt())
                .rotation(node.path("rotation").asString())
                .resolution(node.path("resolution").asString())
                .synopsis(parseNames(node.path("synopsis")))
                .dates(parseNames(node.path("dates")))
                .medias(parseNestedMedias(node.path("medias")));

        // Parse system if present
        JsonNode systemNode = node.path("systeme");
        if (!systemNode.isMissingNode()) {
            Map<String, String> systemNames = new HashMap<>();
            systemNames.put("nom", systemNode.path("nom").asString());
            builder.system(GameSystem.builder()
                    .id(systemNode.path("id").asInt())
                    .names(systemNames)
                    .parentId(systemNode.path("parentid").asInt())
                    .build());
        }

        // Parse ROMs list if present
        JsonNode romsNode = node.path("roms");
        if (romsNode.isArray()) {
            List<Rom> roms = new ArrayList<>();
            for (JsonNode romNode : romsNode) {
                roms.add(parseRom(romNode));
            }
            builder.roms(roms);
        }

        // Parse specific ROM if present
        JsonNode romNode = node.path("rom");
        if (!romNode.isMissingNode()) {
            builder.rom(parseRom(romNode));
        }

        return builder.build();
    }

    /**
     * Helper method to parse ROM information from JSON node
     */
    private Rom parseRom(JsonNode node) {
        if (node == null || node.isMissingNode()) return null;

        return Rom.builder()
                .id(node.path("id").asInt())
                .romNumSupport(node.path("romnumsupport").asInt())
                .romTotalSupport(node.path("romtotalsupport").asInt())
                .romFileName(node.path("romfilename").asString())
                .romSerial(node.path("romserial").asString())
                .romRegions(node.path("romregions").asString())
                .romLangues(node.path("romlangues").asString())
                .romType(node.path("romtype").asString())
                .romSupportType(node.path("romsupporttype").asString())
                .romSize(node.path("romsize").asLong())
                .romCrc(node.path("romcrc").asString())
                .romMd5(node.path("rommd5").asString())
                .romSha1(node.path("romsha1").asString())
                .romCloneOf(node.path("romcloneof").asInt())
                .beta(node.path("beta").asInt())
                .demo(node.path("demo").asInt())
                .trad(node.path("trad").asInt())
                .hack(node.path("hack").asInt())
                .unl(node.path("unl").asInt())
                .alt(node.path("alt").asInt())
                .best(node.path("best").asInt())
                .netplay(node.path("netplay").asInt())
                .gamelink(node.path("gamelink").asInt())
                .nbScrap(node.path("nbscrap").asInt())
                .players(node.path("joueurs").asString())
                .dates(parseNames(node.path("dates")))
                .publisher(node.path("editeur").asString())
                .developer(node.path("developpeur").asString())
                .synopsis(parseNames(node.path("synopsis")))
                .build();
    }
}
