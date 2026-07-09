package com.github.deathbit.retroboy.domain;

import java.util.List;

public final class HandlerMediaTypes {
    public static final List<MediaType> MEDIA_TYPES = List.of(
            imageMediaType("3dboxes"),
            imageMediaType("backcovers"),
            imageMediaType("covers"),
            imageMediaType("fanart"),
            new MediaType("manuals", ".pdf", null),
            imageMediaType("marquees"),
            imageMediaType("miximages"),
            imageMediaType("physicalmedia"),
            imageMediaType("screenshots"),
            imageMediaType("titlescreens"),
            new MediaType("videos", ".mp4", null)
    );

    private HandlerMediaTypes() {
    }

    private static MediaType imageMediaType(String name) {
        return new MediaType(name, ".png", ".jpg");
    }
}

