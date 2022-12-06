package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.Arrays;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle6 implements Puzzle {

    private static final int PACKET_MARKER_SIZE = 4;
    private static final int MESSAGE_MARKER_SIZE = 14;

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        try (BufferedReader reader = resourceReader.read("y2022/puzzle6.txt")) {
            final Result startOfPacketMarker = findMarkerWithinStream(reader, PACKET_MARKER_SIZE);
            System.out.println("Start-of-packet marker at position: " + startOfPacketMarker.getPosition() + " (" + startOfPacketMarker.getPayload() + ")");
        }

        try (BufferedReader reader = resourceReader.read("y2022/puzzle6.txt")) {
            final Result startOfMessageMarker = findMarkerWithinStream(reader, MESSAGE_MARKER_SIZE);
            System.out.println("Start-of-message marker at position: " + startOfMessageMarker.getPosition() + " (" + startOfMessageMarker.getPayload() + ")");
        }

    }

    @SneakyThrows
    private static Result findMarkerWithinStream(final BufferedReader reader, int markerSize) {

        int position = markerSize;
        char[] buffer = new char[markerSize];

        reader.mark(markerSize);
        while (reader.read(buffer) != -1) {

            if (allCharactersUnique(buffer)) {
                break;
            }

            reader.reset();
            reader.skip(1);
            reader.mark(markerSize);
            position++;
        }

        return Result.builder()
                .position(position)
                .payload(new String(buffer))
                .build();
    }

    private static boolean allCharactersUnique(final char[] input) {
        Arrays.sort(input);
        for (int i = 1; i < input.length; i++) {
            if (input[i] == input[i - 1]) {
                return false;
            }
        }
        return true;
    }

    @Value
    @Builder
    private static class Result {
        int position;
        String payload;
    }
}
