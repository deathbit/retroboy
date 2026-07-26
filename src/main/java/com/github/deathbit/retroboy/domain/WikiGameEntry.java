package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiGameEntry {
    private Area area;
    private String wikiName;
    private AreaRenameResult areaRenameResult;
    private int missingMediaBitmap;

    public boolean isMediaMissing(MediaAssetType mediaAssetType) {
        return (missingMediaBitmap & mediaAssetType.getBitMask()) != 0;
    }
}
