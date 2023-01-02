package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle8 implements Puzzle {

    private static final Map<Integer, Set<String>> DIGIT_TO_SEGMENT = ImmutableMap.<Integer, Set<String>>builder()
            .put(0, ImmutableSet.of("a", "b", "c", "e", "f", "g"))
            .put(1, ImmutableSet.of("c", "f"))
            .put(2, ImmutableSet.of("a", "c", "d", "e", "g"))
            .put(3, ImmutableSet.of("a", "c", "d", "f", "g"))
            .put(4, ImmutableSet.of("b", "c", "d", "f"))
            .put(5, ImmutableSet.of("a", "b", "d", "f", "g"))
            .put(6, ImmutableSet.of("a", "b", "d", "e", "f", "g"))
            .put(7, ImmutableSet.of("a", "c", "f"))
            .put(8, ImmutableSet.of("a", "b", "c", "d", "e", "f", "g"))
            .put(9, ImmutableSet.of("a", "b", "c", "d", "f", "g"))
            .build();

    // @TODO - this could be quicker if we convert the digits to bytes or bitsets
    //         and use hamming weights and bitwise operations instead of sets.
    private static final byte[] DIGITS = new byte[] {
            // gfedcba
            0b01110111, // 0
            0b00100100, // 1
            0b01011101, // 2
            0b01101101, // 3
            0b00101110, // 4
            0b01101011, // 5
            0b01111011, // 6
            0b00100101, // 7
            0b01111111, // 8
            0b01101111  // 9
    };

    private static final Map<Integer, Integer> EASY_DIGITS = DIGIT_TO_SEGMENT.entrySet().stream()
            .collect(Collectors.groupingBy(entry -> entry.getValue().size(), Collectors.mapping(Map.Entry::getKey, Collectors.toList())))
            .entrySet()
            .stream()
            .filter(e -> e.getValue().size() == 1)
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle8.txt")) {

            final List<SignalPattern> patterns = new ArrayList<>();

            String line = reader.readLine();
            while (line != null) {

                String[] signal = line.split(" \\| ");
                patterns.add(SignalPattern.builder()
                        .inputs(Arrays.asList(signal[0].split(" ")))
                        .outputs(Arrays.asList(signal[1].split(" ")))
                        .build());

                line = reader.readLine();
            }

            long easyDigits = patterns.stream()
                    .map(SignalPattern::getOutputs)
                    .flatMap(List::stream)
                    .filter(output -> EASY_DIGITS.containsKey(output.length()))
                    .count();

            int decypheredSum = patterns.stream()
                    .mapToInt(SignalPattern::decypher)
                    .sum();

            System.out.println("Times digits 1, 4, 7, or 8 appear: " + easyDigits);
            System.out.println("Sum of all decoded output values : " + decypheredSum);
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    private static class SignalPattern {

        @Singular List<String> inputs;
        @Singular List<String> outputs;

        public int decypher() {

            final List<Set<String>> signals = inputs.stream()
                    .map(segment -> Arrays.stream(segment.split(""))
                            .collect(Collectors.toSet()))
                    .collect(Collectors.toList());

            final BiMap<Integer, Set<String>> results = HashBiMap.create();
            Queue<Set<String>> queue = new ArrayDeque<>(signals);

            while (!queue.isEmpty()) {

                final Set<String> signal = queue.poll();
                final int signalSize = signal.size();

                // 1, 4, 7, 8
                if (EASY_DIGITS.containsKey(signalSize)) {
                    results.put(EASY_DIGITS.get(signalSize), signal);
                    continue;
                }

                // 2 found by taking the delta between 3 and 6 (the top-right bar) and checking if 2 has it. (because 5 does not, and 3 has already been found).
                if (signal.size() == 5 && hasKeys(results, 3, 6) && Sets.union(signal, Sets.difference(results.get(3), results.get(6))).equals(signal)) {
                    results.put(2, signal);
                    continue;
                }

                // 3 is the only '5' segment will full intersection with 1.
                if (signal.size() == 5 && hasKeys(results, 1) && Sets.difference(results.get(1), signal).isEmpty()) {
                    results.put(3, signal);
                    continue;
                }

                // 5 - process of elimination, the last 5-segment digit that isn't 2 or 3.
                if (signal.size() == 5 && hasKeys(results, 2, 3)) {
                    results.put(5, signal);
                    continue;
                }

                // 6 is the only '6' segment with a difference with segment 1 (top-right).
                if (signal.size() == 6 && hasKeys(results, 1) && !Sets.difference(results.get(1), signal).isEmpty()) {
                    results.put(6, signal);
                    continue;
                }

                // 9 is the segment union of 3 and 4.
                if (hasKeys(results, 3, 4) && signal.equals(Sets.union(results.get(3), results.get(4)))) {
                    results.put(9, signal);
                    continue;
                }

                // 0 via process of elimination (the only 6-segment digit that isn't 6 or 9).
                if (signalSize == 6 && hasKeys(results, 6, 9)) {
                    results.put(0, signal);
                    continue;
                }

                // because we do not know the order of the digits, add unmatched items back into the queue.
                queue.add(signal);
            }

            return Integer.parseInt(outputs.stream()
                    .map(segment -> Arrays.stream(segment.split("")).collect(Collectors.toSet()))
                    .map(set -> results.inverse().get(set))
                    .map(String::valueOf)
                    .collect(Collectors.joining()));
        }
    }

    private static boolean hasKeys(final Map<Integer, ?> intMap, int... keys) {
        return Arrays.stream(keys).allMatch(intMap::containsKey);
    }
}
