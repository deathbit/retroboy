package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.domain.screenscraper.*;

import java.util.List;

/**
 * ScreenScraper API component for accessing game database information and media
 */
public interface ScreenScraperComponent {

    /**
     * Get ScreenScraper infrastructure information including server status and quotas
     *
     * @param credentials API credentials containing developer ID, password, and software name
     * @return ServerInfo containing CPU usage, threads, and quota information
     * @throws Exception if the API request fails
     */
    ServerInfo getInfrastructureInfo(ApiCredentials credentials) throws Exception;

    /**
     * Get ScreenScraper user information including contribution stats and quotas
     *
     * @param credentials API credentials with user ID and password
     * @return UserInfo containing user level, contributions, threads, and quotas
     * @throws Exception if the API request fails
     */
    UserInfo getUserInfo(ApiCredentials credentials) throws Exception;

    /**
     * Get list of ScreenScraper user levels
     *
     * @param credentials API credentials
     * @return List of user levels with IDs and names
     * @throws Exception if the API request fails
     */
    List<UserLevel> getUserLevels(ApiCredentials credentials) throws Exception;

    /**
     * Get list of number of players options
     *
     * @param credentials API credentials
     * @return List of player count options
     * @throws Exception if the API request fails
     */
    List<PlayerCount> getPlayerCounts(ApiCredentials credentials) throws Exception;

    /**
     * Get list of media support types
     *
     * @param credentials API credentials
     * @return List of support types (CD, DVD, Cartridge, etc.)
     * @throws Exception if the API request fails
     */
    List<SupportType> getSupportTypes(ApiCredentials credentials) throws Exception;

    /**
     * Get list of ROM types
     *
     * @param credentials API credentials
     * @return List of ROM types (rom, iso, folder, etc.)
     * @throws Exception if the API request fails
     */
    List<RomType> getRomTypes(ApiCredentials credentials) throws Exception;

    /**
     * Get list of regions with multilingual names and media
     *
     * @param credentials API credentials
     * @return List of regions (France, USA, Japan, etc.)
     * @throws Exception if the API request fails
     */
    List<Region> getRegions(ApiCredentials credentials) throws Exception;

    /**
     * Get list of languages with multilingual names and media
     *
     * @param credentials API credentials
     * @return List of languages
     * @throws Exception if the API request fails
     */
    List<Language> getLanguages(ApiCredentials credentials) throws Exception;

    /**
     * Get list of game genres with multilingual names and media
     *
     * @param credentials API credentials
     * @return List of genres (Action, Adventure, RPG, etc.)
     * @throws Exception if the API request fails
     */
    List<Genre> getGenres(ApiCredentials credentials) throws Exception;

    /**
     * Get list of game families with media
     *
     * @param credentials API credentials
     * @return List of game families (Mario, Sonic, etc.)
     * @throws Exception if the API request fails
     */
    List<Family> getFamilies(ApiCredentials credentials) throws Exception;

    /**
     * Get list of game classifications/ratings with multilingual names and media
     *
     * @param credentials API credentials
     * @return List of classifications (ESRB, PEGI, etc.)
     * @throws Exception if the API request fails
     */
    List<Classification> getClassifications(ApiCredentials credentials) throws Exception;

    /**
     * Get list of system media types
     *
     * @param credentials API credentials
     * @return List of system media types (logos, screenshots, etc.)
     * @throws Exception if the API request fails
     */
    List<SystemMediaInfo> getSystemMediaList(ApiCredentials credentials) throws Exception;

    /**
     * Get list of game media types
     *
     * @param credentials API credentials
     * @return List of game media types (box art, screenshots, videos, etc.)
     * @throws Exception if the API request fails
     */
    List<GameMediaInfo> getGameMediaList(ApiCredentials credentials) throws Exception;

    /**
     * Get list of game information types
     *
     * @param credentials API credentials
     * @return List of game information types
     * @throws Exception if the API request fails
     */
    List<GameInfo> getGameInfoList(ApiCredentials credentials) throws Exception;

    /**
     * Get list of ROM information types
     *
     * @param credentials API credentials
     * @return List of ROM information types
     * @throws Exception if the API request fails
     */
    List<RomInfo> getRomInfoList(ApiCredentials credentials) throws Exception;

    /**
     * Download game group media image (genre, family, etc.)
     *
     * @param credentials API credentials
     * @param groupId     Numeric ID of the group
     * @param media       Text ID of the media to return
     * @param crc         CRC hash of existing local image (optional, for optimization)
     * @param md5         MD5 hash of existing local image (optional, for optimization)
     * @param sha1        SHA1 hash of existing local image (optional, for optimization)
     * @param maxWidth    Maximum width in pixels (optional)
     * @param maxHeight   Maximum height in pixels (optional)
     * @return Byte array of the image or null if not modified
     * @throws Exception if the API request fails
     */
    byte[] downloadGroupMedia(ApiCredentials credentials, Integer groupId, String media,
                              String crc, String md5, String sha1, Integer maxWidth, Integer maxHeight) throws Exception;

    /**
     * Download company media images (publisher/developer logos)
     *
     * @param credentials API credentials
     * @param companyId   Numeric ID of the company
     * @param media       Text ID of the media to return
     * @param crc         CRC hash of existing local image (optional, for optimization)
     * @param md5         MD5 hash of existing local image (optional, for optimization)
     * @param sha1        SHA1 hash of existing local image (optional, for optimization)
     * @param maxWidth    Maximum width in pixels (optional)
     * @param maxHeight   Maximum height in pixels (optional)
     * @return Byte array of the image or null if not modified
     * @throws Exception if the API request fails
     */
    byte[] downloadCompanyMedia(ApiCredentials credentials, Integer companyId, String media,
                                String crc, String md5, String sha1, Integer maxWidth, Integer maxHeight) throws Exception;

    /**
     * Get list of systems with full information and media
     *
     * @param credentials API credentials
     * @return List of gaming systems (NES, SNES, PlayStation, etc.)
     * @throws Exception if the API request fails
     */
    List<GameSystem> getSystemList(ApiCredentials credentials) throws Exception;

    /**
     * Download system image media
     *
     * @param credentials API credentials
     * @param systemId    Numeric ID of the system
     * @param media       Text ID of the media to return
     * @param crc         CRC hash of existing local image (optional, for optimization)
     * @param md5         MD5 hash of existing local image (optional, for optimization)
     * @param sha1        SHA1 hash of existing local image (optional, for optimization)
     * @param maxWidth    Maximum width in pixels (optional)
     * @param maxHeight   Maximum height in pixels (optional)
     * @return Byte array of the image or null if not modified
     * @throws Exception if the API request fails
     */
    byte[] downloadSystemMedia(ApiCredentials credentials, Integer systemId, String media,
                               String crc, String md5, String sha1, Integer maxWidth, Integer maxHeight) throws Exception;

    /**
     * Download system video media
     *
     * @param credentials API credentials
     * @param systemId    Numeric ID of the system
     * @param media       Text ID of the media to return
     * @param crc         CRC hash of existing local video (optional, for optimization)
     * @param md5         MD5 hash of existing local video (optional, for optimization)
     * @param sha1        SHA1 hash of existing local video (optional, for optimization)
     * @return Byte array of the video or null if not modified
     * @throws Exception if the API request fails
     */
    byte[] downloadSystemVideo(ApiCredentials credentials, Integer systemId, String media,
                               String crc, String md5, String sha1) throws Exception;

    /**
     * Search for games by name (returns up to 30 games sorted by probability)
     *
     * @param credentials API credentials
     * @param systemId    Numeric ID of the system (optional)
     * @param searchTerm  Name of the game to search for
     * @return List of games matching the search term
     * @throws Exception if the API request fails
     */
    List<Game> searchGames(ApiCredentials credentials, Integer systemId, String searchTerm) throws Exception;

    /**
     * Get detailed information about a game including ROM data and media
     *
     * @param credentials API credentials
     * @param systemId    Numeric ID of the system
     * @param crc         CRC hash of the ROM (required unless gameId is provided)
     * @param md5         MD5 hash of the ROM (optional but recommended)
     * @param sha1        SHA1 hash of the ROM (optional but recommended)
     * @param romType     Type of ROM (rom, iso, folder)
     * @param romName     File name with extension or folder name
     * @param romSize     Size in bytes of the file or folder
     * @param gameId      Force search by game ID (optional, ignores ROM hashes)
     * @return Game information with full metadata and media
     * @throws Exception if the API request fails or game not found
     */
    Game getGameInfo(ApiCredentials credentials, Integer systemId, String crc, String md5, String sha1,
                     String romType, String romName, Long romSize, Integer gameId) throws Exception;

    /**
     * Download game image media
     *
     * @param credentials API credentials
     * @param systemId    Numeric ID of the system
     * @param gameId      Numeric ID of the game
     * @param media       Text ID of the media to return
     * @param crc         CRC hash of existing local image (optional, for optimization)
     * @param md5         MD5 hash of existing local image (optional, for optimization)
     * @param sha1        SHA1 hash of existing local image (optional, for optimization)
     * @param maxWidth    Maximum width in pixels (optional)
     * @param maxHeight   Maximum height in pixels (optional)
     * @return Byte array of the image or null if not modified
     * @throws Exception if the API request fails
     */
    byte[] downloadGameMedia(ApiCredentials credentials, Integer systemId, Integer gameId, String media,
                             String crc, String md5, String sha1, Integer maxWidth, Integer maxHeight) throws Exception;

    /**
     * Download game video media
     *
     * @param credentials API credentials
     * @param systemId    Numeric ID of the system
     * @param gameId      Numeric ID of the game
     * @param media       Text ID of the media to return
     * @param crc         CRC hash of existing local video (optional, for optimization)
     * @param md5         MD5 hash of existing local video (optional, for optimization)
     * @param sha1        SHA1 hash of existing local video (optional, for optimization)
     * @return Byte array of the video or null if not modified
     * @throws Exception if the API request fails
     */
    byte[] downloadGameVideo(ApiCredentials credentials, Integer systemId, Integer gameId, String media,
                             String crc, String md5, String sha1) throws Exception;

    /**
     * Download game manual (PDF format)
     *
     * @param credentials API credentials
     * @param systemId    Numeric ID of the system
     * @param gameId      Numeric ID of the game
     * @param media       Text ID of the media to return
     * @param crc         CRC hash of existing local manual (optional, for optimization)
     * @param md5         MD5 hash of existing local manual (optional, for optimization)
     * @param sha1        SHA1 hash of existing local manual (optional, for optimization)
     * @return Byte array of the PDF manual or null if not modified
     * @throws Exception if the API request fails
     */
    byte[] downloadGameManual(ApiCredentials credentials, Integer systemId, Integer gameId, String media,
                              String crc, String md5, String sha1) throws Exception;

    /**
     * Submit a game rating as a ScreenScraper member
     *
     * @param credentials API credentials with user ID and password
     * @param gameId      Numeric ID of the game
     * @param rating      Game score out of 20 (1-20)
     * @return Response message indicating success or failure
     * @throws Exception if the API request fails
     */
    String submitGameRating(ApiCredentials credentials, Integer gameId, Integer rating) throws Exception;

    /**
     * Submit information or media proposal to ScreenScraper
     * Note: This method requires multipart/form-data POST request
     *
     * @param credentials   API credentials with user ID and password
     * @param gameId        Numeric ID of the game (or use romId)
     * @param romId         Numeric ID of the ROM (optional, alternative to gameId)
     * @param proposalData  Map containing proposal data (type, region, language, text, media file, etc.)
     * @return Response message indicating success or failure
     * @throws Exception if the API request fails
     */
    String submitProposal(ApiCredentials credentials, Integer gameId, Integer romId,
                          java.util.Map<String, Object> proposalData) throws Exception;
}
