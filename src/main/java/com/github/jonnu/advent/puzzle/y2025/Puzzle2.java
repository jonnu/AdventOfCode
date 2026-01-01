package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle2 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2025/puzzle2.txt")) {

            List<Pair<String>> input = Arrays.stream(reader.readLine().split(","))
                    .map(pair -> pair.split("-"))
                    .map(pair -> new Pair<>(pair[0], pair[1]))
                    .toList();

            System.out.println("[Part 1] " + part1(input));
            System.out.println("[Part 2] " + part2(input));
        }
    }

    private static long part1(final List<Pair<String>> input) {
        long sum = 0;
        for (Pair<String> pair : input) {
            for (long i = Long.parseLong(pair.left()); i <= Long.parseLong(pair.right()); i++) {
                String string = String.valueOf(i);
                int length = string.length();

                if (length % 2 == 0) {
                    final String head = string.substring(0, (length / 2));
                    final String tail = string.substring(length / 2);
                    if (Long.parseLong(head) == Long.parseLong(tail)) {
                        sum += i;
                    }
                }
            }
        }

        return sum;
    }

    private static long part2(final List<Pair<String>> input) {
        long sum = 0;
        for (Pair<String> pair : input) {
            for (long i = Long.parseLong(pair.left()); i <= Long.parseLong(pair.right()); i++) {
                String string = String.valueOf(i);
                int length = string.length();

                for (int j = 1; j <= length / 2; j++) {
                    if (length % j == 0) {
                        String sequence = string.substring(0, j);
                        String repeated = sequence.repeat(length / sequence.length());
                        if (repeated.equals(string)) {
                            sum += i;
                            break;
                        }
                    }
                }
            }
        }

        return sum;
    }

    record Pair<T>(T left, T right) {
    }
}
