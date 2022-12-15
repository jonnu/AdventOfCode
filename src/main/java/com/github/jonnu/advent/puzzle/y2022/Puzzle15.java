package com.github.jonnu.advent.puzzle.y2022;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle15 implements Puzzle {

    private static final int ROW_TO_COVERAGE_SCAN = 2_000_000;
    private static final int BEACON_X_MULTIPLIER = 4_000_000;
    private static final int MISSING_BEACON_MAXIMUM_X = 4_000_000;
    private static final int MISSING_BEACON_MAXIMUM_Y = 4_000_000;

    private static final Pattern PATTERN = Pattern.compile("^.*?x=(?<sx>-?\\d+),\\sy=(?<sy>-?\\d+):.*?x=(?<bx>-?\\d+),\\sy=(?<by>-?\\d+)$");

    private static final BiFunction<Point, Point, Integer> POINT_MANHATTAN_DISTANCE = (current, next) ->
            Math.abs(current.getX() - next.getX()) + Math.abs(current.getY() - next.getY());

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        final List<Sensor> sensors = new ArrayList<>();
        try (BufferedReader reader = resourceReader.read("y2022/puzzle15.txt")) {
            String line = reader.readLine();
            while (line != null) {
                final Matcher matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Unable to match input: " + line);
                }

                sensors.add(Sensor.createFromMatch(matcher));
                line = reader.readLine();
            }
        }

        // set of all beacons
        Set<Integer> beacons = sensors.stream()
                .map(Sensor::getBeacon)
                .filter(position -> position.getY() == ROW_TO_COVERAGE_SCAN)
                .map(Point::getX)
                .collect(Collectors.toSet());

        // coverage at y, minus beacons at y
        Set<Integer> allXs = sensors.stream()
                .map(sensor -> sensor.coverageAtY(ROW_TO_COVERAGE_SCAN))
                .map(Collection::stream)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .filter(coordinate -> !beacons.contains(coordinate))
                .collect(Collectors.toSet());

        System.out.println("Positions at y: " + ROW_TO_COVERAGE_SCAN + " that cannot contain a beacon: " + allXs.size());

        List<Integer> coefficients = sensors.stream()
                .map(Sensor::radius)
                .flatMap(Set::stream)
                .flatMap(radial -> Stream.of(radial - 1, radial + 1))
                .sorted()
                .collect(Collectors.toList());

        Point beacon = Lists.cartesianProduct(coefficients, coefficients)
                .stream()
                .map(pair -> new Point(pair.get(0), pair.get(1)))
                .filter(point -> point.abs() % 2 == 0)
                .filter(point -> point.getX() >= point.getY())
                // convert to intersection point
                .map(Point::intersection)
                // check in bounds
                .filter(point -> point.getY() >= 0 && point.getY() <= MISSING_BEACON_MAXIMUM_Y)
                .filter(point -> point.getX() >= 0 && point.getX() <= MISSING_BEACON_MAXIMUM_X)
                // check the point is not covered by any sensor
                .filter(point -> sensors.stream().noneMatch(sensor -> sensor.covers(point)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No intersection found."));

        System.out.printf("Frequency of distress beacon: %d%n", (long) beacon.getX() * BEACON_X_MULTIPLIER + beacon.getY());
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class Sensor {

        private static final String SENSOR_X = "sx";
        private static final String SENSOR_Y = "sy";
        private static final String BEACON_X = "bx";
        private static final String BEACON_Y = "by";

        Point point;
        Point beacon;
        int distance;

        /**
         * Given the 'radius' (the manhattan distance) drops by 2 per row away from the sensor,
         * we can work out the x-coverage on a given y by applying a formula.
         *
         * Example (r = 2):
         *
         *     ...#... y-2, coverage 1
         *     ..###.. y-1, coverage 3
         *     .##S##. y=0, coverage 5
         *     ..###.. y+1, coverage 3
         *     ...#... y+2, coverage 1
         */
        public Set<Integer> coverageAtY(final int y) {
            return Optional.of(distance - Math.abs(y - point.getY()))
                    .filter(radius -> radius > 0)
                    .map(radius -> IntStream.range(point.getX() - radius, point.getX() - radius + (2 * (radius + 1) - 1)))
                    .stream()
                    .flatMap(IntStream::boxed)
                    .collect(Collectors.toSet());
        }

        public boolean covers(final Point other) {
            return POINT_MANHATTAN_DISTANCE.apply(point, other) <= distance;
        }

        public Set<Integer> radius() {
            return ImmutableSet.of(
                    point.getX() + (point.getY() + distance),
                    point.getX() + (point.getY() - distance),
                    point.getX() - (point.getY() - distance),
                    point.getX() - (point.getY() + distance)
            );
        }

        public static Sensor createFromMatch(final Matcher matcher) {

            final Point sp = Point.builder()
                    .x(Integer.parseInt(matcher.group(SENSOR_X)))
                    .y(Integer.parseInt(matcher.group(SENSOR_Y)))
                    .build();
            final Point bp = Point.builder()
                    .x(Integer.parseInt(matcher.group(BEACON_X)))
                    .y(Integer.parseInt(matcher.group(BEACON_Y)))
                    .build();

            return Sensor.builder()
                    .point(sp)
                    .beacon(bp)
                    .distance(POINT_MANHATTAN_DISTANCE.apply(sp, bp))
                    .build();
        }
    }

    @Value
    @Builder
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Point {

        @Builder.Default int x = 0;
        @Builder.Default int y = 0;

        public int abs() {
            return Math.abs(x - y);
        }

        public Point intersection() {
            return new Point(abs() / 2 + Math.min(x, y), abs() / 2 * (int) Math.signum(x - y));
        }

        @Override
        public String toString() {
            return String.format("(x: %d, y: %d)", x, y);
        }
    }

}
