package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle5 implements Puzzle {

    private final ResourceReader resourceReader;

    private static final Map<String, String> X = ImmutableMap.<String, String>builder()
            .put("seed-to-soil", "")
            .put("soil-to-fertilizer", "")
            .put("fertilizer-to-water", "")
            .put("water-to-light", "")
            .put("light-to-temperature", "")
            .put("temperature-to-humidity", "")
            .put("humidity-to-location", "")
            .build();

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
                    seeds.addAll(Arrays.stream(line.substring(7).split("\\s+")).map(Long::valueOf).collect(Collectors.toList()));
                    //System.out.println("seeds: " + seeds);
                }
                line = reader.readLine();

                if (line == null || line.isBlank()) {
                    continue;
                }

                Optional<String> st = X.keySet().stream().filter(line::startsWith).findFirst();
                if (st.isPresent()) {
                    //System.out.println("Switch map to: " + st.get());
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

                //System.out.println(almanac);
                //System.out.printf("dest: %d, source: %d, length: %d%n", destinationStart, sourceStart, rangeLength);
            }

            System.out.println("Lowest location: " + seeds.stream().mapToLong(this::locationForSeed).min().orElse(0L));

            System.out.println(seeds);
            List<Range> ranges = IntStream.range(0, seeds.size() / 2)
                    .mapToObj(i -> Range.builder()
                            .start(seeds.get(2 * i))
                            .end((seeds.get(2 * i) + seeds.get((2 * i) + 1)) - 1)
                            .build())
                    .collect(Collectors.toList());

            Set<Long> seedr = ranges.stream()
                    .map(range -> almanacs.get("seed-to-soil")
                            .stream()
                            //.peek(a -> System.out.println(a.getSource()))
                            .map(a -> a.getSource().intersection(range))
                            //.peek(System.out::println)
                            .filter(RangeIntersection::isIntersects)
                            .peek(x -> System.out.println("Range: " + x))
                            .flatMap(x -> LongStream.rangeClosed(x.overlap.start, x.overlap.end).boxed())
                            .collect(Collectors.toSet()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            System.out.println("Lowest location 2: " + seedr.stream().mapToLong(this::locationForSeed).min().orElse(0L));

        }
    }

    private static final List<String> ORDER = ImmutableList.of(
            "seed-to-soil",
            "soil-to-fertilizer",
            "fertilizer-to-water",
            "water-to-light",
            "light-to-temperature",
            "temperature-to-humidity",
            "humidity-to-location"
    );

    private long locationForSeed(long seed) {

        //long s = seed;
        for (String mapping : ORDER) {
            long finalSeed = seed;
            seed = almanacs.get(mapping)
                    .stream()
                    .filter(x -> x.contains(finalSeed))
                    .map(y -> y.map(finalSeed))
                    .findFirst()
                    .orElse(finalSeed);
            //System.out.println(mapping + " --> " + seed);
        }

        return seed;
    }

    @Value
    @Builder
    private static class Range {

        long start;
        long end;

        public boolean contains(final long value) {
            return value >= start && value <= end;
        }

        public long difference(final long input) {
            return input - getStart();
        }

        public RangeIntersection intersection(final Range other) {

            if (start > other.getEnd() || end < other.getStart()) {
                return RangeIntersection.builder().build();
            }

            return RangeIntersection.builder()
                    .intersects(true)
                    .overlap(Range.builder()
                            .start(Math.max(getStart(), other.getStart()))
                            .end(Math.min(getEnd(), other.getEnd()))
                            .build())
                    .build();
        }
    }

    @Value
    @Builder
    private static class RangeIntersection {
        boolean intersects;
        Range overlap;
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
