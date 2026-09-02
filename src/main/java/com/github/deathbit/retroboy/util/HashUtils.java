package com.github.deathbit.retroboy.util;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

public final class HashUtils {

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private HashUtils() {
    }

    public static String calculateSha1(File file) throws Exception {
        if (!file.isFile()) {
            return null;
        }
        var digest = MessageDigest.getInstance("SHA-1");
        try (var inputStream = Files.newInputStream(file.toPath())) {
            var buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return toHexUppercase(digest.digest());
    }

    private static String toHexUppercase(byte[] bytes) {
        var chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            var value = bytes[i] & 0xFF;
            chars[i * 2] = HEX[value >>> 4];
            chars[i * 2 + 1] = HEX[value & 0x0F];
        }
        return new String(chars);
    }
}
