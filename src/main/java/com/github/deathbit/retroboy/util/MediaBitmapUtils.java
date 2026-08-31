package com.github.deathbit.retroboy.util;

import com.github.deathbit.retroboy.enums.MediaAssetType;

public final class MediaBitmapUtils {

    private MediaBitmapUtils() {
    }

    /**
     * Returns a bitmap with the asset type marked as present.
     */
    public static int withMedia(int mediaBitmap, MediaAssetType mediaAssetType) {
        return mediaBitmap | mediaAssetType.getBitMask();
    }

    /**
     * Returns a bitmap with the asset type marked as absent.
     */
    public static int withoutMedia(int mediaBitmap, MediaAssetType mediaAssetType) {
        return mediaBitmap & ~mediaAssetType.getBitMask();
    }

    /**
     * A set bit means the media asset exists.
     */
    public static boolean hasMedia(int mediaBitmap, MediaAssetType mediaAssetType) {
        return (mediaBitmap & mediaAssetType.getBitMask()) != 0;
    }

    public static boolean isMediaMissing(int mediaBitmap, MediaAssetType mediaAssetType) {
        return !hasMedia(mediaBitmap, mediaAssetType);
    }
}

