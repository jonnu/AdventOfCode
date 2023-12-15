package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
//import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.github.jonnu.advent.puzzle.y2022.Puzzle15;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import org.checkerframework.common.value.qual.IntRange;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle11 implements Puzzle {

    private static final int GALAXY = '#';
    private final ResourceReader resourceReader;

    private static final BiFunction<Point, Point, Long> MANHATTAN_DISTANCE = (current, next) ->
            Math.abs(current.getX() - next.getX()) + Math.abs(current.getY() - next.getY());

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle11.txt")) {

            Set<Point> galaxies = new HashSet<>();
            String line = reader.readLine();
            int row = 0;
            while (line != null) {
                final int y = row;
                final char[] current = line.toCharArray();
                galaxies.addAll(IntStream.range(0, line.length())
                        .filter(x -> GALAXY == current[x])
                        .mapToObj(x -> new Point(x, y))
                        .collect(Collectors.toSet()));
                line = reader.readLine();
                row++;
            }

            final LongSummaryStatistics xStats = galaxies.stream().mapToLong(Point::getX).summaryStatistics();
            final LongSummaryStatistics yStats = galaxies.stream().mapToLong(Point::getY).summaryStatistics();

            final Set<Long> colShift = LongStream.range(0, xStats.getMax()).filter(x -> galaxies.stream().noneMatch(g -> x == g.getX())).boxed().collect(Collectors.toSet());
            final Set<Long> rowShift = LongStream.range(0, yStats.getMax()).filter(y -> galaxies.stream().noneMatch(g -> y == g.getY())).boxed().collect(Collectors.toSet());

            Set<Point> shiftedGalaxies = galaxies.stream().map(g -> shift(colShift, rowShift, g, 1_000_000)).collect(Collectors.toSet());

            //draw(shiftedGalaxies);

            Set<ShortestGalaxyPath> universe = new HashSet<>();
            // shortest path calc.
            shiftedGalaxies.forEach(galaxy -> {
                universe.addAll(shiftedGalaxies.stream()
                        .filter(g -> !g.equals(galaxy))
                        .map(g -> ShortestGalaxyPath.builder()
                                .a(galaxy)
                                .b(g)
                                .distance(MANHATTAN_DISTANCE.apply(g, galaxy))
                                .build())
                        .collect(Collectors.toSet()));
            });

            System.out.println(universe.stream().mapToLong(ShortestGalaxyPath::getDistance).sum());
        }
    }

    @Builder
    @Value
    private static class ShortestGalaxyPath {

        Point a;
        Point b;
        long distance;

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = (result*PRIME) + Math.min(this.a.hashCode(), this.b.hashCode());
            result = (result*PRIME) + Math.max(this.a.hashCode(), this.b.hashCode());
            result = (result*PRIME) + (int) distance;
            return result;
        }

        public boolean equals(Object other) {

            if (other == this) {
                return true;
            }

            if (!(other instanceof ShortestGalaxyPath)) {
                return false;
            }

            ShortestGalaxyPath compare = (ShortestGalaxyPath) other;
            return getDistance() == compare.getDistance() && Set.of(a, b).equals(Set.of(compare.getA(), compare.getB()));
        }
    }


    @AllArgsConstructor
    @Builder
    @Value
    public static class Point {
        long x;
        long y;
    }

    private static Point shift(final Collection<Long> xshift, final Collection<Long> yshift, Point point, int factor) {
        return new Point(
                point.getX() + (Math.max(1, (factor - 1)) * xshift.stream().filter(i -> i < point.getX()).count()),
                point.getY() + (Math.max(1, (factor - 1)) * yshift.stream().filter(i -> i < point.getY()).count())
        );
    }

    private static void draw(final Set<Point> points) {
        final LongSummaryStatistics xStats = points.stream().mapToLong(Point::getX).summaryStatistics();
        final LongSummaryStatistics yStats = points.stream().mapToLong(Point::getY).summaryStatistics();
        for (long y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (long x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                System.out.printf("%s", points.contains(p) ? "#" : ".");
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }
}
