package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle14 implements Puzzle {

    private static final List<Direction> SPIN_CYCLE = ImmutableList.of(
            Direction.NORTH,
            Direction.WEST,
            Direction.SOUTH,
            Direction.EAST
    );

    private final ResourceReader resourceReader;


    private final Map<String, Integer> cache = new HashMap<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle14.txt")) {

            int b = 0;

            String line = reader.readLine();
            final Map<Point, Rock> rocks = new HashMap<>();

            while (line != null) {
                for (int a = 0; a < line.length(); a++) {
                    if (line.charAt(a) == '.') {
                        continue;
                    }
                    rocks.put(new Point(a, b), Rock.parse(line.charAt(a)));
                }
                line = reader.readLine();
                b++;
            }

            Grid grid = new Grid(rocks);
            grid.tilt(Direction.NORTH);
            System.out.println("Total load on the north support beams: " + grid.computeWeight());

            // @TODO - Improve the part 2 algorithm. It takes ~57s.
            int cycle = 0;
            int cycles = 1_000_000_000;
            while (cycle < cycles) {

                grid.spin();
                String key = grid.toString();

                if (cache.containsKey(key)) {
                    int distance = cycles - cycle;
                    int loopLength = cycle - cache.get(key);
                    cycle = cycles - distance % loopLength;
                }

                cache.put(key, cycle);
                cycle++;
            }

            System.out.printf("After %d spin cycles, the weight on the north support is: %d%n", cycles, grid.computeWeight());
        }
    }

    private static class Grid {

        private static final Map<Direction, Comparator<Point>> POINT_COMPARATORS = ImmutableMap.<Direction, Comparator<Point>>builder()
                .put(Direction.NORTH, Comparator.comparing(Point::getX).thenComparing(Point::getY))
                .put(Direction.EAST, Comparator.comparing(Point::getY).thenComparing((a, b) -> Integer.compare(b.getX(), a.getX())))
                .put(Direction.SOUTH, Comparator.comparing(Point::getX).thenComparing((a, b) -> Integer.compare(b.getY(), a.getY())))
                .put(Direction.WEST, Comparator.comparing(Point::getY).thenComparing(Point::getX))
                .build();

        private final Map<Point, Rock> rocks;
        private final int xMax;
        private final int yMax;

        public Grid(final Map<Point, Rock> rocks) {
            this.rocks = rocks;
            this.xMax = rocks.keySet().stream().mapToInt(Point::getX).summaryStatistics().getMax();
            this.yMax = rocks.keySet().stream().mapToInt(Point::getY).summaryStatistics().getMax();
        }

        public void tilt(final Direction direction) {

            List<Point> points = rocks.entrySet()
                    .stream()
                    .filter(x -> Rock.ROUNDED.equals(x.getValue()))
                    .map(Map.Entry::getKey)
                    .sorted(Objects.requireNonNull(POINT_COMPARATORS.get(direction)))
                    .toList();

            points.forEach(point -> {
                Rock rock = rocks.get(point);
                rocks.remove(point);
                rocks.put(slideRock(point, direction), rock);
            });
        }

        private Point slideRock(final Point move, final Direction direction) {

            Map<Direction, RockPointMutator> rockPointMutators = ImmutableMap.<Direction, RockPointMutator>builder()
                    .put(Direction.NORTH, RockPointMutator.builder()
                            .filteringPredicate((p, r) -> p.getX() == r.getX() && p.getY() < r.getY())
                            .pointSorter(Math::max)
                            .pointMapper((z, r) -> new Point(r.getX(), z + 1))
                            .defaultPointMapper(r -> new Point(r.getX(), 0))
                            .build())
                    .put(Direction.EAST, RockPointMutator.builder()
                            .filteringPredicate((p, r) -> p.getY() == r.getY() && p.getX() > r.getX())
                            .pointSorter(Math::min)
                            .pointMapper((z, r) -> new Point(z - 1, r.getY()))
                            .defaultPointMapper(r -> new Point(xMax, r.getY()))
                            .build())
                    .put(Direction.SOUTH, RockPointMutator.builder()
                            .filteringPredicate((p, r) -> p.getX() == r.getX() && p.getY() > r.getY())
                            .pointSorter(Math::min)
                            .pointMapper((z, r) -> new Point(r.getX(), z - 1))
                            .defaultPointMapper(r -> new Point(r.getX(), yMax))
                            .build())
                    .put(Direction.WEST, RockPointMutator.builder()
                            .filteringPredicate((p, r) -> p.getY() == r.getY() && p.getX() < r.getX())
                            .pointSorter(Math::max)
                            .pointMapper((z, r) -> new Point(z + 1, r.getY()))
                            .defaultPointMapper(r -> new Point(0, r.getY()))
                            .build())
                    .build();

            RockPointMutator mutator = rockPointMutators.get(direction);

            return rocks.keySet()
                    .stream()
                    .filter(entry -> mutator.getFilteringPredicate().test(entry, move))
                    .mapToInt(point -> direction.isVertical() ? point.getY() : point.getX())
                    // @TODO - this line takes up 98% of the cpu time according to profiler.
                    .reduce((l, r) -> mutator.getPointSorter().applyAsInt(l, r))
                    .stream()
                    .mapToObj(z -> mutator.getPointMapper().apply(z, move))
                    .findFirst()
                    .orElseGet(() -> mutator.getDefaultPointMapper().apply(move));
        }

        public void spin() {
            for (Direction direction : SPIN_CYCLE) {
                tilt(direction);
            }
        }

        private int computeWeight() {
            return rocks.entrySet()
                    .stream()
                    .filter(entry -> Rock.ROUNDED.equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .mapToInt(point -> (yMax + 1) - point.getY())
                    .sum();
        }

        public String getCacheKey() {
            return IntStream.rangeClosed(0, yMax)
                    .mapToObj(y -> IntStream.rangeClosed(0, xMax)
                            .mapToObj(x -> new Point(x, y))
                            .map(x -> Rock.ROUNDED.equals(rocks.get(x)) ? "1" : "0")
                            .collect(Collectors.joining()))
                    .collect(Collectors.joining(""));
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int y = 0; y <= yMax; y++) {
                for (int x = 0; x <= xMax; x++) {
                    Point p = new Point(x, y);
                    builder.append(Optional.ofNullable(rocks.get(p))
                            .map(Object::toString)
                            .orElse("."));
                }
                builder.append("\n");
            }
            return builder.toString();
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class RockPointMutator {
        BiPredicate<Point, Point> filteringPredicate;
        BiFunction<Integer, Point, Point> pointMapper;
        IntBinaryOperator pointSorter;
        Function<Point, Point> defaultPointMapper;
    }

    @Getter
    @AllArgsConstructor
    public enum Rock {

        ROUNDED("O"),
        SQUARED("#");

        private final String character;

        @Override
        public String toString() {
            return getCharacter();
        }

        public static Rock parse(final char input) {
            return parse(Character.toString(input));
        }

        public static Rock parse(final String input) {
            return Arrays.stream(values())
                    .filter(p -> p.getCharacter().equals(input))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown rock character: " + input));
        }
    }
}
