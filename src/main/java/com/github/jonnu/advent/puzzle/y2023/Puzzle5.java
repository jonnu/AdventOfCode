package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle5 implements Puzzle {

    private static final List<String> TYPES = ImmutableList.of(
            "seed-to-soil",
            "soil-to-fertilizer",
            "fertilizer-to-water",
            "water-to-light",
            "light-to-temperature",
            "temperature-to-humidity",
            "humidity-to-location"
    );

    private final ResourceReader resourceReader;
    private final Map<String, List<Almanac>> almanacs = new HashMap<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle5.txt")) {

            String line = reader.readLine();
            List<Long> seeds = new ArrayList<>();
            String current = "";

            while (line != null) {

                if (line.startsWith("seeds")) {
                    seeds.addAll(Arrays.stream(line.substring(7)
                            .split("\\s+"))
                            .map(Long::valueOf)
                            .toList());
                }
                line = reader.readLine();

                if (line == null || line.isBlank()) {
                    continue;
                }

                Optional<String> st = TYPES.stream().filter(line::startsWith).findFirst();
                if (st.isPresent()) {
                    current = st.get();
                    continue;
                }

                long[] parts = Arrays.stream(line.split("\\s+")).mapToLong(Long::valueOf).toArray();
                long destinationStart = parts[0];
                long sourceStart = parts[1];
                long rangeLength = parts[2];

                Range source = Range.builder()
                        .start(sourceStart)
                        .end(sourceStart + rangeLength - 1)
                        .build();
                Range destination = Range.builder()
                        .start(destinationStart)
                        .end(destinationStart + rangeLength - 1)
                        .build();
                Almanac almanac = Almanac.builder()
                        .source(source)
                        .destination(destination)
                        .build();

                almanacs.putIfAbsent(current, new ArrayList<>());
                almanacs.get(current).add(almanac);
            }

            System.out.println("Lowest initial seed location: " + seeds.stream().mapToLong(this::locationForSeed).min().orElse(0L));

            Queue<Range> ranges = IntStream.range(0, seeds.size() / 2)
                    .mapToObj(i -> Range.builder()
                            .start(seeds.get(2 * i))
                            .end((seeds.get(2 * i) + seeds.get((2 * i) + 1)) - 1)
                            .depth(1)
                            .build())
                    .collect(Collectors.toCollection(ArrayDeque::new));

            System.out.println("Lowest range seed location: " + lowestLocationForInitialSeed(ranges));
        }
    }

    private long locationForSeed(long seed) {
        for (String mapping : TYPES) {
            long finalSeed = seed;
            seed = almanacs.get(mapping)
                    .stream()
                    .filter(almanac -> almanac.contains(finalSeed))
                    .map(almanac -> almanac.map(finalSeed))
                    .findFirst()
                    .orElse(finalSeed);
        }

        return seed;
    }

    private long lowestLocationForInitialSeed(final Queue<Range> ranges) {
        long value = Long.MAX_VALUE;

        while (!ranges.isEmpty()) {
            Range range = ranges.poll();

            long x1 = range.getStart();
            long x2 = range.getEnd();

            if (range.getDepth() == 8) {
                value = Math.min(value, x1);
                continue;
            }

            boolean bit = false;
            for (Almanac almanac : almanacs.get(TYPES.get(range.getDepth() - 1))) {

                long z = almanac.getDestination().getStart();
                long y1 = almanac.getSource().getStart();
                long y2 = almanac.getSource().getEnd();
                long diff = z - y1;

                // Check if it is outside of range
                if (x2 <= y1 || y2 <= x1) {
                    continue;
                }

                if (x1 < y1) {
                    ranges.add(Range.builder()
                            .start(x1)
                            .end(y1)
                            .depth(range.getDepth())
                            .build());
                    x1 = y1;
                }

                if (y2 < x2) {
                    ranges.add(Range.builder()
                            .start(y2)
                            .end(x2)
                            .depth(range.getDepth())
                            .build());
                    x2 = y2;
                }

                ranges.add(Range.builder()
                        .start(x1 + diff)
                        .end(x2 + diff)
                        .depth(range.getDepth() + 1)
                        .build());

                bit = true;
            }

            if (!bit) {
                ranges.add(Range.builder()
                        .start(x1)
                        .end(x2)
                        .depth(range.getDepth() + 1)
                        .build());
            }
        }

        return value;
    }

    @Value
    @Builder
    private static class Range {

        long start;
        long end;
        int depth;

        public boolean contains(final long value) {
            return value >= start && value <= end;
        }

        public long difference(final long input) {
            return input - getStart();
        }

    }

    @Value
    @Builder
    private static class Almanac {

        Range source;
        Range destination;

        public boolean contains(final long input) {
            return source.contains(input);
        }

        public long map(final long input) {

            if (!source.contains(input)) {
                return input;
            }

            return destination.getStart() + source.difference(input);
        }
    }

}
