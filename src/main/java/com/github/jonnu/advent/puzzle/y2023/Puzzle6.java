package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.Streams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle6 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle6.txt")) {

            String line1 = reader.readLine();
            String line2 = reader.readLine();

            List<RaceRecord> records = Streams.zip(
                    toLongStream(line1),
                    toLongStream(line2),
                    (t, d) -> RaceRecord.builder()
                            .time(t)
                            .distance(d)
                            .build())
                    .toList();

            long shortRaceProduct = records.stream()
                    .map(Puzzle6::solve)
                    .map(Double::longValue)
                    .reduce(1L, Math::multiplyExact);

            long longerRace = Streams.zip(
                            toLongStream(line1, ":", x -> x.replaceAll("\\s+", "")),
                            toLongStream(line2, ":", x -> x.replaceAll("\\s+", "")),
                            (t, d) -> RaceRecord.builder()
                                    .time(t)
                                    .distance(d)
                                    .build())
                    .map(Puzzle6::solve)
                    .findFirst()
                    .map(Double::longValue)
                    .orElse(0L);

            System.out.println("Product of short races: " + shortRaceProduct);
            System.out.println("Long race: " + longerRace);
        }
    }

    @AllArgsConstructor
    private static class Tuple<T> {

        private T[] data;

        public static <T> Tuple<T> pair(T one, T two) {
            return new Tuple<>(newArray(2, one, two));
        }

        public List<T> values() {
            return List.of(data);
        }

        @SafeVarargs
        private static <T> T[] newArray(int length, T... array) {
            return Arrays.copyOf(array, length);
        }
    }

    private static double solve(final RaceRecord record) {
        List<Double> values = solveQuadratic(-1.0d, record.getTime(), -record.getDistance()).values();

        double a = values.get(0) % 1 == 0 ? values.get(0) - 1 : Math.floor(values.get(0));
        double b = values.get(1) % 1 == 0 ? values.get(1) + 1 : Math.ceil(values.get(1));

        return Math.abs(a - b + 1);
    }

    private static Tuple<Double> solveQuadratic(final double a, final double b, final double c) {
        double delta = Math.sqrt(b * b - 4.0 * a * c);
        return Tuple.pair(
                (-b + delta) / (2.0 * a),
                (-b - delta) / (2.0 * a)
        );
    }

    private static Stream<Long> toLongStream(final String input) {
        return toLongStream(input, "\\s+", Function.identity());
    }

    private static Stream<Long> toLongStream(final String input, String split, Function<String, String> mutation) {
        return Arrays.stream(mutation.apply(input).split(split))
                .skip(1)
                .mapToLong(Long::valueOf)
                .boxed();
    }

    @Value
    @Builder
    private static class RaceRecord {
        long time;
        long distance;
    }

}
