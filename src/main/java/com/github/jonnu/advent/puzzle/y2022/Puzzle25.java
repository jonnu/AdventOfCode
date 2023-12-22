package com.github.jonnu.advent.puzzle.y2022;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableBiMap;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.Optional;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle25 implements Puzzle {

    private static final int BASE = 5;
    private static final int OFFSET = 2;
    private static final ImmutableBiMap<Long, String> DECIMAL_TO_SNAFU_REMAINDER = ImmutableBiMap.<Long, String>builder()
            .put(0L, "=")
            .put(1L, "-")
            .put(2L, "0")
            .put(3L, "1")
            .put(4L, "2")
            .build();

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle25.txt")) {

            long decimalSum = 0L;
            String line = reader.readLine();
            while (line != null) {
                decimalSum += snafu2long(line);
                line = reader.readLine();
            }

            System.out.println("Sum of SNAFUs: " + decimalSum);
            System.out.println("SNAFU value  : " + long2snafu(decimalSum));
        }
    }

    public static long snafu2long(final String snafu) {
        long value = 0;
        for (int c = snafu.length() - 1, p = 0; c >= 0; c--, p++) {
            value += (long) Math.pow(BASE, p) * snafuCharacter2long(snafu.charAt(c));
        }
        return value;
    }

    public static long snafuCharacter2long(final char snafu) {
        return Optional.of(Character.toString(snafu))
                .map(c -> DECIMAL_TO_SNAFU_REMAINDER.inverse().get(c))
                .map(c -> c - OFFSET)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SNAFU character: " + snafu));
    }

    public static String long2snafu(final long value) {
        if (value != 0) {
            long val = Math.floorDiv(value + OFFSET, BASE);
            long rem = (value + OFFSET) % BASE;
            return long2snafu(val) + DECIMAL_TO_SNAFU_REMAINDER.get(rem);
        }
        return "";
    }

}
