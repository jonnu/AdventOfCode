package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

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

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle5.txt")) {

            String line = reader.readLine();
            while (line != null) {

                if (line.startsWith("seeds")) {
                    long[] seeds = Arrays.stream(line.substring(7).split("\\s+")).mapToLong(Long::valueOf).toArray();
                    System.out.println("seeds: " + Arrays.stream(seeds).boxed().collect(Collectors.toSet()));
                }
                line = reader.readLine();

                if (line == null || line.isBlank()) {
                    continue;
                }

                Optional<String> st = X.keySet().stream().filter(line::startsWith).findFirst();
                if (st.isPresent()) {
                    System.out.println("Switch map to: " + st.get());
                    continue;
                }

                long[] parts = Arrays.stream(line.split("\\s+")).mapToLong(Long::valueOf).toArray();
                long destinationStart = parts[0];
                long sourceStart = parts[1];
                long rangeLength = parts[2];
                System.out.printf("dest: %d, source: %d, length: %d%n", destinationStart, sourceStart, rangeLength);
            }

            // move between maps.
            //RangeMap<Long, >
            // need to use intervals + btree./bsearch
        }
    }

    private static <K, V> Map<K, V> navigate(final Map<K, V> input, final K haystack) {
//        RangeMap<Long, Long> m = TreeRangeMap.create();
//        m.put();
        return null;
    }

}
