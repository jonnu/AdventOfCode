package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
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

    private final Map<Point, Rock> grid = new HashMap<>();
    private final Map<String, Integer> cache = new HashMap<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle14.txt")) {

            int b = 0;

            String line = reader.readLine();

            while (line != null) {
                for (int a = 0; a < line.length(); a++) {
                    if (line.charAt(a) == '.') {
                        continue;
                    }
                    grid.put(new Point(a, b), Rock.parse(line.charAt(a)));
                }
                line = reader.readLine();
                b++;
            }

            Map<Point, Rock> part1 = grid;
            System.out.println("Total load on the north support beams: " + computeWeight(tilt(part1, Direction.NORTH)));

            Map<Point, Rock> result = grid;
            final IntSummaryStatistics xStats = grid.keySet().stream().mapToInt(Point::getX).summaryStatistics();
            final IntSummaryStatistics yStats = grid.keySet().stream().mapToInt(Point::getY).summaryStatistics();

            int cycle = 0;
            int cycles = 1_000_000_000;
            while (cycle < cycles) {

                result = spin(result);

                final Set<Point> rocks = result.entrySet()
                        .stream()
                        .filter(x -> x.getValue().equals(Rock.ROUNDED))
                        .map(Map.Entry::getKey).collect(Collectors.toSet());

                String key = IntStream.rangeClosed(0, yStats.getMax())
                        .mapToObj(y -> IntStream.rangeClosed(0, xStats.getMax())
                                .mapToObj(x -> rocks.contains(new Point(x, y)) ? "1" : "0")
                                .collect(Collectors.joining()))
                        .collect(Collectors.joining("|"));

                // todo - this takes 180s... needs improvement.
                if (cache.containsKey(key)) {
                    int distance = cycles - cycle;
                    int loopLength = cycle - cache.get(key);
                    cycle = cycles - distance % loopLength;
                }

                cache.put(key, cycle);
                cycle++;
            }

            System.out.printf("After %d spin cycles, the weight on the north support is: %d%n", cycles, computeWeight(result));
        }
    }

    public Map<Point, Rock> spin(Map<Point, Rock> rocks) {

        for (Direction direction : SPIN_CYCLE) {
            rocks = tilt(rocks, direction);
        }

        return rocks;
    }

    public Map<Point, Rock> tilt(Map<Point, Rock> rocks, Direction direction) {

        Map<Direction, Comparator<Point>> pointComparator = ImmutableMap.<Direction, Comparator<Point>>builder()
                .put(Direction.NORTH, Comparator.comparing(Point::getX).thenComparing(Point::getY))
                .put(Direction.EAST, Comparator.comparing(Point::getY).thenComparing((a, b) -> Integer.compare(b.getX(), a.getX())))
                .put(Direction.SOUTH, Comparator.comparing(Point::getX).thenComparing((a, b) -> Integer.compare(b.getY(), a.getY())))
                .put(Direction.WEST, Comparator.comparing(Point::getY).thenComparing(Point::getX))
                .build();

        List<Point> points = rocks.entrySet()
                .stream()
                .filter(x -> Rock.ROUNDED.equals(x.getValue()))
                .map(Map.Entry::getKey)
                .sorted(Objects.requireNonNull(pointComparator.get(direction)))
                .collect(Collectors.toList());

        points.forEach(point -> {
            Rock rock = rocks.get(point);
            rocks.remove(point);
            Point newpos = slideRock(grid, point, direction);
            rocks.put(newpos, rock);
        });

        return rocks;
    }

    private Point slideRock(Map<Point, Rock> rocks, Point move, Direction direction) {

        // we should put these somewhere.
        final IntSummaryStatistics xStats = rocks.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = rocks.keySet().stream().mapToInt(Point::getY).summaryStatistics();

        Map<Direction, RockPointMutator> rockPointMutators = ImmutableMap.<Direction, RockPointMutator>builder()
                .put(Direction.NORTH, RockPointMutator.builder()
                        .filteringPredicate((p, r) -> p.getX() == r.getX() && p.getY() < r.getY())
                        .pointSorter(Math::max)
                        .pointMapper((z, r) -> new Point(r.getX(), z + 1))
                        .defaultPointMapper(r -> new Point(r.getX(), yStats.getMin()))
                        .build())
                .put(Direction.EAST, RockPointMutator.builder()
                        .filteringPredicate((p, r) -> p.getY() == r.getY() && p.getX() > r.getX())
                        .pointSorter(Math::min)
                        .pointMapper((z, r) -> new Point(z - 1, r.getY()))
                        .defaultPointMapper(r -> new Point(xStats.getMax(), r.getY()))
                        .build())
                .put(Direction.SOUTH, RockPointMutator.builder()
                        .filteringPredicate((p, r) -> p.getX() == r.getX() && p.getY() > r.getY())
                        .pointSorter(Math::min)
                        .pointMapper((z, r) -> new Point(r.getX(), z - 1))
                        .defaultPointMapper(r -> new Point(r.getX(), yStats.getMax()))
                        .build())
                .put(Direction.WEST, RockPointMutator.builder()
                        .filteringPredicate((p, r) -> p.getY() == r.getY() && p.getX() < r.getX())
                        .pointSorter(Math::max)
                        .pointMapper((z, r) -> new Point(z + 1, r.getY()))
                        .defaultPointMapper(r -> new Point(xStats.getMin(), r.getY()))
                        .build())
                .build();

        RockPointMutator mutator = rockPointMutators.get(direction);

        return rocks.keySet()
                .stream()
                .filter(entry -> mutator.getFilteringPredicate().test(entry, move))
                //.peek(x -> System.out.println(" - filtered: " + x))
                .mapToInt(point -> direction.isVertical() ? point.getY() : point.getX())
                .reduce((l, r) -> mutator.getPointSorter().applyAsInt(l, r))
                .stream()
                .mapToObj(z -> mutator.getPointMapper().apply(z, move))
                //.peek(x -> System.out.println(" - pointmapper: " + x))
                .findFirst()
                .orElseGet(() -> mutator.getDefaultPointMapper().apply(move));
    }


    private int computeWeight(final Map<Point, Rock> rocks) {

        // we should put these somewhere.
        //final IntSummaryStatistics xStats = rocks.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = rocks.keySet().stream().mapToInt(Point::getY).summaryStatistics();

        return rocks.entrySet()
                .stream()
                .filter(entry -> Rock.ROUNDED.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .mapToInt(point -> (yStats.getMax() + 1) - point.getY())
                .sum();
    }

    private static <T> void draw(final Map<Point, T> points) {

        final IntSummaryStatistics xStats = points.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = points.keySet().stream().mapToInt(Point::getY).summaryStatistics();

        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                System.out.print(Optional.ofNullable(points.get(p))
                        .map(Object::toString)
                        .orElse("."));
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
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
