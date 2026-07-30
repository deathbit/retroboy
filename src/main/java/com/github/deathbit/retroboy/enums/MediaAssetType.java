package com.github.deathbit.retroboy.enums;

import lombok.Getter;

@Getter
public enum MediaAssetType {
    THREE_D_BOX(0, "3dboxes", "png", "jpg"),
    BACK_COVER(1, "backcovers", "png", "jpg"),
    COVER(2, "covers", "png", "jpg"),
    FANART(3, "fanart", "png", "jpg"),
    MANUAL(4, "manuals", "pdf", null),
    MARQUEE(5, "marquees", "png", "jpg"),
    MIX_IMAGE(6, "miximages", "png", "jpg"),
    PHYSICAL_MEDIA(7, "physicalmedia", "png", "jpg"),
    SCREENSHOT(8, "screenshots", "png", "jpg"),
    TITLE_SCREEN(9, "titlescreens", "png", "jpg"),
    VIDEO(10, "videos", "mp4", null);

    private final int bitIndex;
    private final int bitMask;
    private final String directoryName;
    private final String primaryExtension;
    private final String fallbackExtension;

    MediaAssetType(int bitIndex, String directoryName, String primaryExtension, String fallbackExtension) {
        this.bitIndex = bitIndex;
        this.bitMask = 1 << bitIndex;
        this.directoryName = directoryName;
        this.primaryExtension = primaryExtension;
        this.fallbackExtension = fallbackExtension;
    }

    public static int allMissingBitmap() {
        int bitmap = 0;
        for (var mediaAssetType : values()) {
            bitmap |= mediaAssetType.getBitMask();
        }
        return bitmap;
    }
}
