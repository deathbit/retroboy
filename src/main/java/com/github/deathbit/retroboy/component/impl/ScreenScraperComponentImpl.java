package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ScreenScraperComponent;
import com.github.deathbit.retroboy.domain.screenscraper.ApiCredentials;
import com.github.deathbit.retroboy.domain.screenscraper.ApiResponseHeader;
import com.github.deathbit.retroboy.domain.screenscraper.Classification;
import com.github.deathbit.retroboy.domain.screenscraper.ClassificationsResponse;
import com.github.deathbit.retroboy.domain.screenscraper.DownloadMediaResponse;
import com.github.deathbit.retroboy.domain.screenscraper.FamiliesResponse;
import com.github.deathbit.retroboy.domain.screenscraper.Family;
import com.github.deathbit.retroboy.domain.screenscraper.Game;
import com.github.deathbit.retroboy.domain.screenscraper.GameInfo;
import com.github.deathbit.retroboy.domain.screenscraper.GameInfoListResponse;
import com.github.deathbit.retroboy.domain.screenscraper.GameInfoResponse;
import com.github.deathbit.retroboy.domain.screenscraper.GameMediaInfo;
import com.github.deathbit.retroboy.domain.screenscraper.GameMediaListResponse;
import com.github.deathbit.retroboy.domain.screenscraper.GameSystem;
import com.github.deathbit.retroboy.domain.screenscraper.Genre;
import com.github.deathbit.retroboy.domain.screenscraper.GenresResponse;
import com.github.deathbit.retroboy.domain.screenscraper.InfrastructureInfoResponse;
import com.github.deathbit.retroboy.domain.screenscraper.Language;
import com.github.deathbit.retroboy.domain.screenscraper.LanguagesResponse;
import com.github.deathbit.retroboy.domain.screenscraper.PlayerCount;
import com.github.deathbit.retroboy.domain.screenscraper.PlayerCountsResponse;
import com.github.deathbit.retroboy.domain.screenscraper.Region;
import com.github.deathbit.retroboy.domain.screenscraper.RegionsResponse;
import com.github.deathbit.retroboy.domain.screenscraper.Rom;
import com.github.deathbit.retroboy.domain.screenscraper.RomInfo;
import com.github.deathbit.retroboy.domain.screenscraper.RomInfoListResponse;
import com.github.deathbit.retroboy.domain.screenscraper.RomType;
import com.github.deathbit.retroboy.domain.screenscraper.RomTypesResponse;
import com.github.deathbit.retroboy.domain.screenscraper.SearchGamesResponse;
import com.github.deathbit.retroboy.domain.screenscraper.ServerInfo;
import com.github.deathbit.retroboy.domain.screenscraper.SubmitResponse;
import com.github.deathbit.retroboy.domain.screenscraper.SupportType;
import com.github.deathbit.retroboy.domain.screenscraper.SupportTypesResponse;
import com.github.deathbit.retroboy.domain.screenscraper.SystemListResponse;
import com.github.deathbit.retroboy.domain.screenscraper.SystemMediaInfo;
import com.github.deathbit.retroboy.domain.screenscraper.SystemMediaListResponse;
import com.github.deathbit.retroboy.domain.screenscraper.UserInfo;
import com.github.deathbit.retroboy.domain.screenscraper.UserInfoResponse;
import com.github.deathbit.retroboy.domain.screenscraper.UserLevel;
import com.github.deathbit.retroboy.domain.screenscraper.UserLevelsResponse;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadCompanyMediaInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadCompanyMediaOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadGameManualInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadGameManualOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadGameMediaInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadGameMediaOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadGameVideoInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadGameVideoOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadGroupMediaInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadGroupMediaOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadSystemMediaInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadSystemMediaOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadSystemVideoInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.DownloadSystemVideoOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetClassificationsOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetFamiliesOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetGameInfoInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetGameInfoListOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetGameInfoOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetGameMediaListOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetGenresOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetInfrastructureInfoOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetLanguagesOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetPlayerCountsOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetRegionsOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetRomInfoListOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetRomTypesOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetSupportTypesOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetSystemListOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetSystemMediaListOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetUserInfoOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.GetUserLevelsOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.SearchGamesInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.SearchGamesOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.SubmitGameRatingInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.SubmitGameRatingOutput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.SubmitProposalInput;
import com.github.deathbit.retroboy.domain.screenscraper.dto.SubmitProposalOutput;
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

    /**
     * Static API credentials for ScreenScraper authentication
     * TODO: Configure these credentials from application.properties or environment variables
     * instead of hardcoding them. Consider using @Value or @ConfigurationProperties.
     */
    private static final ApiCredentials API_CREDENTIALS = ApiCredentials.builder()
            .devId("xxx")
            .devPassword("yyy")
            .softName("zzz")
            .ssId("zjkiki")  // Optional: User ID (null if not using user-specific features)
            .ssPassword("zjkiki225")  // Optional: User password (null if not using user-specific features)
            .build();

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
     * Build base URL with common authentication parameters using static API credentials
     */
    private String buildBaseUrl(String endpoint) {
        return buildBaseUrl(endpoint, API_CREDENTIALS);
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

    /**
     * Parse API response header from JSON
     * Returns null if header node is missing from the response
     */
    private ApiResponseHeader parseHeader(JsonNode root) {
        JsonNode headerNode = root.path("header");
        if (headerNode.isMissingNode() || headerNode.isNull()) {
            return null;
        }
        return ApiResponseHeader.builder()
                .apiVersion(headerNode.path("APIversion").asText())
                .dateTime(headerNode.path("dateTime").asText())
                .commandRequested(headerNode.path("commandRequested").asText())
                .success(headerNode.path("success").asText())
                .error(headerNode.path("error").asText())
                .build();
    }

    @Override
    public GetInfrastructureInfoOutput getInfrastructureInfo() throws Exception {
        String url = buildBaseUrl("ssinfraInfos.php");
        String response = executeGetRequest(url);

        if (response == null) return GetInfrastructureInfoOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
        JsonNode serverNode = root.path("response").path("serveurs");

        ServerInfo serverInfo = ServerInfo.builder()
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

        InfrastructureInfoResponse infrastructureInfoResponse = InfrastructureInfoResponse.builder()
                .serveurs(serverInfo)
                .build();

        return GetInfrastructureInfoOutput.builder()
                .header(header)
                .response(infrastructureInfoResponse)
                .build();
    }

    @Override
    public GetUserInfoOutput getUserInfo() throws Exception {
        String url = buildBaseUrl("ssuserInfos.php");
        String response = executeGetRequest(url);

        if (response == null) return GetUserInfoOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
        JsonNode userNode = root.path("response").path("ssuser");

        UserInfo userInfo = UserInfo.builder()
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

        UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                .ssuser(userInfo)
                .build();

        return GetUserInfoOutput.builder()
                .header(header)
                .response(userInfoResponse)
                .build();
    }

    @Override
    public GetUserLevelsOutput getUserLevels() throws Exception {
        String url = buildBaseUrl("userlevelsListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetUserLevelsOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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

        UserLevelsResponse userLevelsResponse = UserLevelsResponse.builder()
                .niveaux(levels)
                .build();

        return GetUserLevelsOutput.builder()
                .header(header)
                .response(userLevelsResponse)
                .build();
    }

    @Override
    public GetPlayerCountsOutput getPlayerCounts() throws Exception {
        String url = buildBaseUrl("nbJoueursListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetPlayerCountsOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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

        PlayerCountsResponse playerCountsResponse = PlayerCountsResponse.builder()
                .joueurs(counts)
                .build();

        return GetPlayerCountsOutput.builder()
                .header(header)
                .response(playerCountsResponse)
                .build();
    }

    @Override
    public GetSupportTypesOutput getSupportTypes() throws Exception {
        String url = buildBaseUrl("supportTypesListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetSupportTypesOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
        JsonNode typesNode = root.path("response").path("supporttypes");

        List<SupportType> types = new ArrayList<>();
        if (typesNode.isArray()) {
            for (JsonNode node : typesNode) {
                types.add(SupportType.builder()
                        .name(node.path("nom").asString())
                        .build());
            }
        }
        SupportTypesResponse responseObj = SupportTypesResponse.builder()
                .supportstypes(types)
                .build();
        return GetSupportTypesOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetRomTypesOutput getRomTypes() throws Exception {
        String url = buildBaseUrl("romTypesListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetRomTypesOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
        JsonNode typesNode = root.path("response").path("romtypes");

        List<RomType> types = new ArrayList<>();
        if (typesNode.isArray()) {
            for (JsonNode node : typesNode) {
                types.add(RomType.builder()
                        .name(node.path("nom").asString())
                        .build());
            }
        }
        RomTypesResponse responseObj = RomTypesResponse.builder()
                .romstypes(types)
                .build();
        return GetRomTypesOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetRegionsOutput getRegions() throws Exception {
        String url = buildBaseUrl("regionsListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetRegionsOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        RegionsResponse responseObj = RegionsResponse.builder()
                .regions(regions)
                .build();
        return GetRegionsOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetLanguagesOutput getLanguages() throws Exception {
        String url = buildBaseUrl("languesListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetLanguagesOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        LanguagesResponse responseObj = LanguagesResponse.builder()
                .langues(languages)
                .build();
        return GetLanguagesOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetGenresOutput getGenres() throws Exception {
        String url = buildBaseUrl("genresListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetGenresOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        GenresResponse responseObj = GenresResponse.builder()
                .genres(genres)
                .build();
        return GetGenresOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetFamiliesOutput getFamilies() throws Exception {
        String url = buildBaseUrl("famillesListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetFamiliesOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        FamiliesResponse responseObj = FamiliesResponse.builder()
                .familles(families)
                .build();
        return GetFamiliesOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetClassificationsOutput getClassifications() throws Exception {
        String url = buildBaseUrl("classificationsListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetClassificationsOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        ClassificationsResponse responseObj = ClassificationsResponse.builder()
                .classifications(classifications)
                .build();
        return GetClassificationsOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetSystemMediaListOutput getSystemMediaList() throws Exception {
        String url = buildBaseUrl("mediasSystemeListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetSystemMediaListOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        SystemMediaListResponse responseObj = SystemMediaListResponse.builder()
                .medias(mediaList)
                .build();
        return GetSystemMediaListOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetGameMediaListOutput getGameMediaList() throws Exception {
        String url = buildBaseUrl("mediasJeuListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetGameMediaListOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        GameMediaListResponse responseObj = GameMediaListResponse.builder()
                .medias(mediaList)
                .build();
        return GetGameMediaListOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetGameInfoListOutput getGameInfoList() throws Exception {
        String url = buildBaseUrl("infosJeuListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetGameInfoListOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        GameInfoListResponse responseObj = GameInfoListResponse.builder()
                .jeuinfos(infoList)
                .build();
        return GetGameInfoListOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetRomInfoListOutput getRomInfoList() throws Exception {
        String url = buildBaseUrl("infosRomListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetRomInfoListOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        RomInfoListResponse responseObj = RomInfoListResponse.builder()
                .rominfos(infoList)
                .build();
        return GetRomInfoListOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public DownloadGroupMediaOutput downloadGroupMedia(DownloadGroupMediaInput input) throws Exception {
        Integer groupId = input.getGroupId();
        String media = input.getMedia();
        String crc = input.getCrc();
        String md5 = input.getMd5();
        String sha1 = input.getSha1();
        Integer maxWidth = input.getMaxWidth();
        Integer maxHeight = input.getMaxHeight();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaGroup.php"))
                .queryParam("groupid", groupId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);
        if (maxWidth != null) builder.queryParam("maxwidth", maxWidth);
        if (maxHeight != null) builder.queryParam("maxheight", maxHeight);

        byte[] data = executeGetBinaryRequest(builder.toUriString());
        DownloadMediaResponse responseObj = DownloadMediaResponse.builder()
                .data(data)
                .build();
        return DownloadGroupMediaOutput.builder()
                .header(null)
                .response(responseObj)
                .build();
    }

    @Override
    public DownloadCompanyMediaOutput downloadCompanyMedia(DownloadCompanyMediaInput input) throws Exception {
        Integer companyId = input.getCompanyId();
        String media = input.getMedia();
        String crc = input.getCrc();
        String md5 = input.getMd5();
        String sha1 = input.getSha1();
        Integer maxWidth = input.getMaxWidth();
        Integer maxHeight = input.getMaxHeight();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaCompagnie.php"))
                .queryParam("companyid", companyId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);
        if (maxWidth != null) builder.queryParam("maxwidth", maxWidth);
        if (maxHeight != null) builder.queryParam("maxheight", maxHeight);

        byte[] data = executeGetBinaryRequest(builder.toUriString());
        DownloadMediaResponse responseObj = DownloadMediaResponse.builder()
                .data(data)
                .build();
        return DownloadCompanyMediaOutput.builder()
                .header(null)
                .response(responseObj)
                .build();
    }

    @Override
    public GetSystemListOutput getSystemList() throws Exception {
        String url = buildBaseUrl("systemesListe.php");
        String response = executeGetRequest(url);

        if (response == null) return GetSystemListOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
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
        SystemListResponse responseObj = SystemListResponse.builder()
                .systemes(systems)
                .build();
        return GetSystemListOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public DownloadSystemMediaOutput downloadSystemMedia(DownloadSystemMediaInput input) throws Exception {
        Integer systemId = input.getSystemId();
        String media = input.getMedia();
        String crc = input.getCrc();
        String md5 = input.getMd5();
        String sha1 = input.getSha1();
        Integer maxWidth = input.getMaxWidth();
        Integer maxHeight = input.getMaxHeight();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaSysteme.php"))
                .queryParam("systemeid", systemId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);
        if (maxWidth != null) builder.queryParam("maxwidth", maxWidth);
        if (maxHeight != null) builder.queryParam("maxheight", maxHeight);

        byte[] data = executeGetBinaryRequest(builder.toUriString());
        DownloadMediaResponse responseObj = DownloadMediaResponse.builder()
                .data(data)
                .build();
        return DownloadSystemMediaOutput.builder()
                .header(null)
                .response(responseObj)
                .build();
    }

    @Override
    public DownloadSystemVideoOutput downloadSystemVideo(DownloadSystemVideoInput input) throws Exception {
        Integer systemId = input.getSystemId();
        String media = input.getMedia();
        String crc = input.getCrc();
        String md5 = input.getMd5();
        String sha1 = input.getSha1();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaVideoSysteme.php"))
                .queryParam("systemeid", systemId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);

        byte[] data = executeGetBinaryRequest(builder.toUriString());
        DownloadMediaResponse responseObj = DownloadMediaResponse.builder()
                .data(data)
                .build();
        return DownloadSystemVideoOutput.builder()
                .header(null)
                .response(responseObj)
                .build();
    }

    @Override
    public SearchGamesOutput searchGames(SearchGamesInput input) throws Exception {
        Integer systemId = input.getSystemId();
        String searchTerm = input.getSearchTerm();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("jeuRecherche.php"))
                .queryParam("recherche", searchTerm);

        if (systemId != null) {
            builder.queryParam("systemeid", systemId);
        }

        String response = executeGetRequest(builder.toUriString());
        if (response == null) return SearchGamesOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
        JsonNode gamesNode = root.path("response").path("jeux");

        List<Game> games = new ArrayList<>();
        if (gamesNode.isArray()) {
            for (JsonNode node : gamesNode) {
                games.add(parseGame(node));
            }
        }
        SearchGamesResponse responseObj = SearchGamesResponse.builder()
                .jeux(games)
                .build();
        return SearchGamesOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public GetGameInfoOutput getGameInfo(GetGameInfoInput input) throws Exception {
        Integer systemId = input.getSystemId();
        String crc = input.getCrc();
        String md5 = input.getMd5();
        String sha1 = input.getSha1();
        String romType = input.getRomType();
        String romName = input.getRomName();
        Long romSize = input.getRomSize();
        Integer gameId = input.getGameId();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("jeuInfos.php"))
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
        if (response == null) return GetGameInfoOutput.builder().header(null).build();

        JsonNode root = objectMapper.readTree(response);
        ApiResponseHeader header = parseHeader(root);
        System.out.println(root.toPrettyString());
        JsonNode gameNode = root.path("response").path("jeu");

        Game game = parseGame(gameNode);
        GameInfoResponse responseObj = GameInfoResponse.builder()
                .jeu(game)
                .build();
        return GetGameInfoOutput.builder()
                .header(header)
                .response(responseObj)
                .build();
    }

    @Override
    public DownloadGameMediaOutput downloadGameMedia(DownloadGameMediaInput input) throws Exception {
        Integer systemId = input.getSystemId();
        Integer gameId = input.getGameId();
        String media = input.getMedia();
        String crc = input.getCrc();
        String md5 = input.getMd5();
        String sha1 = input.getSha1();
        Integer maxWidth = input.getMaxWidth();
        Integer maxHeight = input.getMaxHeight();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaJeu.php"))
                .queryParam("systemeid", systemId)
                .queryParam("jeuid", gameId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);
        if (maxWidth != null) builder.queryParam("maxwidth", maxWidth);
        if (maxHeight != null) builder.queryParam("maxheight", maxHeight);

        byte[] data = executeGetBinaryRequest(builder.toUriString());
        DownloadMediaResponse responseObj = DownloadMediaResponse.builder()
                .data(data)
                .build();
        return DownloadGameMediaOutput.builder()
                .header(null)
                .response(responseObj)
                .build();
    }

    @Override
    public DownloadGameVideoOutput downloadGameVideo(DownloadGameVideoInput input) throws Exception {
        Integer systemId = input.getSystemId();
        Integer gameId = input.getGameId();
        String media = input.getMedia();
        String crc = input.getCrc();
        String md5 = input.getMd5();
        String sha1 = input.getSha1();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaVideoJeu.php"))
                .queryParam("systemeid", systemId)
                .queryParam("jeuid", gameId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);

        byte[] data = executeGetBinaryRequest(builder.toUriString());
        DownloadMediaResponse responseObj = DownloadMediaResponse.builder()
                .data(data)
                .build();
        return DownloadGameVideoOutput.builder()
                .header(null)
                .response(responseObj)
                .build();
    }

    @Override
    public DownloadGameManualOutput downloadGameManual(DownloadGameManualInput input) throws Exception {
        Integer systemId = input.getSystemId();
        Integer gameId = input.getGameId();
        String media = input.getMedia();
        String crc = input.getCrc();
        String md5 = input.getMd5();
        String sha1 = input.getSha1();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("mediaManuelJeu.php"))
                .queryParam("systemeid", systemId)
                .queryParam("jeuid", gameId)
                .queryParam("media", media);

        if (crc != null && !crc.isEmpty()) builder.queryParam("crc", crc);
        if (md5 != null && !md5.isEmpty()) builder.queryParam("md5", md5);
        if (sha1 != null && !sha1.isEmpty()) builder.queryParam("sha1", sha1);

        byte[] data = executeGetBinaryRequest(builder.toUriString());
        DownloadMediaResponse responseObj = DownloadMediaResponse.builder()
                .data(data)
                .build();
        return DownloadGameManualOutput.builder()
                .header(null)
                .response(responseObj)
                .build();
    }

    @Override
    public SubmitGameRatingOutput submitGameRating(SubmitGameRatingInput input) throws Exception {
        Integer gameId = input.getGameId();
        Integer rating = input.getRating();

        if (rating < 1 || rating > 20) {
            throw new Exception("Rating must be between 1 and 20");
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(buildBaseUrl("botNote.php"))
                .queryParam("gameid", gameId)
                .queryParam("note", rating);

        String message = executeGetRequest(builder.toUriString());
        SubmitResponse responseObj = SubmitResponse.builder()
                .message(message)
                .build();
        return SubmitGameRatingOutput.builder()
                .header(null)
                .response(responseObj)
                .build();
    }

    @Override
    public SubmitProposalOutput submitProposal(SubmitProposalInput input) throws Exception {
        Integer gameId = input.getGameId();
        Integer romId = input.getRomId();
        Map<String, Object> proposalData = input.getProposalData();

        // Build URL with basic parameters including developer credentials
        String baseUrl = BASE_URL + "/botProposition.php";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("devid", API_CREDENTIALS.getDevId())
                .queryParam("devpassword", API_CREDENTIALS.getDevPassword())
                .queryParam("softname", API_CREDENTIALS.getSoftName())
                .queryParam("ssid", API_CREDENTIALS.getSsId())
                .queryParam("sspassword", API_CREDENTIALS.getSsPassword());

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
            SubmitResponse responseObj = SubmitResponse.builder()
                    .message(response.getBody())
                    .build();
            return SubmitProposalOutput.builder()
                    .header(null)
                    .response(responseObj)
                    .build();
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
