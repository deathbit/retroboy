package com.github.deathbit.retroboy.util;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public final class MagicTools {
    private static final String MAGIC =
        "WowMuchEncryptedVeryImpressIGuessThisHasToBeQuiteLongT"
            + "oAlsoSupportSomeVeryLongKeys";

    private MagicTools() {}

    public static String magic(String plainText) {
        byte[] input = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] key = MAGIC.getBytes(StandardCharsets.UTF_8);

        if (input.length >= 80) {
            throw new IllegalArgumentException("Length of input str is too long");
        }

        StringJoiner joiner = new StringJoiner(";");

        for (int i = 0; i < input.length; i++) {
            int inputByte = input[i] & 0xFF;
            int keyByte = key[i] & 0xFF;
            joiner.add(String.valueOf(inputByte + keyByte));
        }

        return joiner.toString();
    }

    public static String unMagic(String encoded) {
        String[] parts = encoded.split(";");
        byte[] key = MAGIC.getBytes(StandardCharsets.UTF_8);

        if (parts.length >= 80) {
            throw new IllegalArgumentException("Length of input str is too long");
        }

        byte[] output = new byte[parts.length];

        for (int i = 0; i < parts.length; i++) {
            int encodedValue = Integer.parseInt(parts[i]);
            int keyByte = key[i] & 0xFF;
            output[i] = (byte) (encodedValue - keyByte);
        }

        return new String(output, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        System.out.println(unMagic("204;198;236;130;203;181;203;126;191;167;200;198;192;228;169;156"));
    }
}
