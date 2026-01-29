package com.github.deathbit.retroboy.component;

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

/**
 * ScreenScraper API component for accessing game database information and media
 */
public interface ScreenScraperComponent {

    /**
     * Get ScreenScraper infrastructure information including server status and quotas
     *
     * @return Output containing ServerInfo with CPU usage, threads, and quota information
     * @throws Exception if the API request fails
     */
    GetInfrastructureInfoOutput getInfrastructureInfo() throws Exception;

    /**
     * Get ScreenScraper user information including contribution stats and quotas
     *
     * @return Output containing ServerInfo and UserInfo with server status, user level, contributions, threads, and quotas
     * @throws Exception if the API request fails
     */
    GetUserInfoOutput getUserInfo() throws Exception;

    /**
     * Get list of ScreenScraper user levels
     *
     * @return Output containing list of user levels with IDs and names
     * @throws Exception if the API request fails
     */
    GetUserLevelsOutput getUserLevels() throws Exception;

    /**
     * Get list of number of players options
     *
     * @return Output containing list of player count options
     * @throws Exception if the API request fails
     */
    GetPlayerCountsOutput getPlayerCounts() throws Exception;

    /**
     * Get list of media support types
     *
     * @return Output containing list of support types (CD, DVD, Cartridge, etc.)
     * @throws Exception if the API request fails
     */
    GetSupportTypesOutput getSupportTypes() throws Exception;

    /**
     * Get list of ROM types
     *
     * @return Output containing list of ROM types (rom, iso, folder, etc.)
     * @throws Exception if the API request fails
     */
    GetRomTypesOutput getRomTypes() throws Exception;

    /**
     * Get list of regions with multilingual names and media
     *
     * @return Output containing list of regions (France, USA, Japan, etc.)
     * @throws Exception if the API request fails
     */
    GetRegionsOutput getRegions() throws Exception;

    /**
     * Get list of languages with multilingual names and media
     *
     * @return Output containing list of languages
     * @throws Exception if the API request fails
     */
    GetLanguagesOutput getLanguages() throws Exception;

    /**
     * Get list of game genres with multilingual names and media
     *
     * @return Output containing list of genres (Action, Adventure, RPG, etc.)
     * @throws Exception if the API request fails
     */
    GetGenresOutput getGenres() throws Exception;

    /**
     * Get list of game families with media
     *
     * @return Output containing list of game families (Mario, Sonic, etc.)
     * @throws Exception if the API request fails
     */
    GetFamiliesOutput getFamilies() throws Exception;

    /**
     * Get list of game classifications/ratings with multilingual names and media
     *
     * @return Output containing list of classifications (ESRB, PEGI, etc.)
     * @throws Exception if the API request fails
     */
    GetClassificationsOutput getClassifications() throws Exception;

    /**
     * Get list of system media types
     *
     * @return Output containing list of system media types (logos, screenshots, etc.)
     * @throws Exception if the API request fails
     */
    GetSystemMediaListOutput getSystemMediaList() throws Exception;

    /**
     * Get list of game media types
     *
     * @return Output containing list of game media types (box art, screenshots, videos, etc.)
     * @throws Exception if the API request fails
     */
    GetGameMediaListOutput getGameMediaList() throws Exception;

    /**
     * Get list of game information types
     *
     * @return Output containing list of game information types
     * @throws Exception if the API request fails
     */
    GetGameInfoListOutput getGameInfoList() throws Exception;

    /**
     * Get list of ROM information types
     *
     * @return Output containing list of ROM information types
     * @throws Exception if the API request fails
     */
    GetRomInfoListOutput getRomInfoList() throws Exception;

    /**
     * Download game group media image (genre, family, etc.)
     *
     * @param input Input containing groupId, media type, and optional hashes and dimensions
     * @return Output containing byte array of the image or null if not modified
     * @throws Exception if the API request fails
     */
    DownloadGroupMediaOutput downloadGroupMedia(DownloadGroupMediaInput input) throws Exception;

    /**
     * Download company media images (publisher/developer logos)
     *
     * @param input Input containing companyId, media type, and optional hashes and dimensions
     * @return Output containing byte array of the image or null if not modified
     * @throws Exception if the API request fails
     */
    DownloadCompanyMediaOutput downloadCompanyMedia(DownloadCompanyMediaInput input) throws Exception;

    /**
     * Get list of systems with full information and media
     *
     * @return Output containing list of gaming systems (NES, SNES, PlayStation, etc.)
     * @throws Exception if the API request fails
     */
    GetSystemListOutput getSystemList() throws Exception;

    /**
     * Download system image media
     *
     * @param input Input containing systemId, media type, and optional hashes and dimensions
     * @return Output containing byte array of the image or null if not modified
     * @throws Exception if the API request fails
     */
    DownloadSystemMediaOutput downloadSystemMedia(DownloadSystemMediaInput input) throws Exception;

    /**
     * Download system video media
     *
     * @param input Input containing systemId, media type, and optional hashes
     * @return Output containing byte array of the video or null if not modified
     * @throws Exception if the API request fails
     */
    DownloadSystemVideoOutput downloadSystemVideo(DownloadSystemVideoInput input) throws Exception;

    /**
     * Search for games by name (returns up to 30 games sorted by probability)
     *
     * @param input Input containing optional systemId and searchTerm
     * @return Output containing list of games matching the search term
     * @throws Exception if the API request fails
     */
    SearchGamesOutput searchGames(SearchGamesInput input) throws Exception;

    /**
     * Get detailed information about a game including ROM data and media
     *
     * @param input Input containing systemId, ROM hashes, romType, romName, romSize, and optional gameId
     * @return Output containing game information with full metadata and media
     * @throws Exception if the API request fails or game not found
     */
    GetGameInfoOutput getGameInfo(GetGameInfoInput input) throws Exception;

    /**
     * Download game image media
     *
     * @param input Input containing systemId, gameId, media type, and optional hashes and dimensions
     * @return Output containing byte array of the image or null if not modified
     * @throws Exception if the API request fails
     */
    DownloadGameMediaOutput downloadGameMedia(DownloadGameMediaInput input) throws Exception;

    /**
     * Download game video media
     *
     * @param input Input containing systemId, gameId, media type, and optional hashes
     * @return Output containing byte array of the video or null if not modified
     * @throws Exception if the API request fails
     */
    DownloadGameVideoOutput downloadGameVideo(DownloadGameVideoInput input) throws Exception;

    /**
     * Download game manual (PDF format)
     *
     * @param input Input containing systemId, gameId, media type, and optional hashes
     * @return Output containing byte array of the PDF manual or null if not modified
     * @throws Exception if the API request fails
     */
    DownloadGameManualOutput downloadGameManual(DownloadGameManualInput input) throws Exception;

    /**
     * Submit a game rating as a ScreenScraper member
     *
     * @param input Input containing gameId and rating (1-20)
     * @return Output containing response message indicating success or failure
     * @throws Exception if the API request fails
     */
    SubmitGameRatingOutput submitGameRating(SubmitGameRatingInput input) throws Exception;

    /**
     * Submit information or media proposal to ScreenScraper
     * Note: This method requires multipart/form-data POST request
     *
     * @param input Input containing gameId or romId and proposal data
     * @return Output containing response message indicating success or failure
     * @throws Exception if the API request fails
     */
    SubmitProposalOutput submitProposal(SubmitProposalInput input) throws Exception;
}
