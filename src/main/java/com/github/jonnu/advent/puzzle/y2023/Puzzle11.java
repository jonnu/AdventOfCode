package com.github.jonnu.advent.puzzle.y2023;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle11 implements Puzzle {

    private static final int GALAXY = '#';
    private final ResourceReader resourceReader;

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

            final int maxX = galaxies.stream()
                    .mapToInt(Point::getX)
                    .summaryStatistics()
                    .getMax();

            final int maxY = galaxies.stream()
                    .mapToInt(Point::getY)
                    .summaryStatistics()
                    .getMax();

            final Set<Integer> colShift = IntStream.range(0, maxX)
                    .filter(x -> galaxies.stream().noneMatch(g -> x == g.getX()))
                    .boxed()
                    .collect(Collectors.toSet());

            final Set<Integer> rowShift = IntStream.range(0, maxY)
                    .filter(y -> galaxies.stream().noneMatch(g -> y == g.getY()))
                    .boxed()
                    .collect(Collectors.toSet());

            final long shortestPaths = shortestGalaxyPath(galaxies, rowShift, colShift, 1);
            final long shortestPathsMillion = shortestGalaxyPath(galaxies, rowShift, colShift, 1_000_000);

            System.out.println("Sum of shortest paths: " + shortestPaths);
            System.out.println("Sum of 10^6 shortest paths: " + shortestPathsMillion);
        }
    }

    private static long shortestGalaxyPath(final Set<Point> galaxies, final Set<Integer> rowShift, final Set<Integer> colShift, final int shiftFactor) {

        final Set<Point> shifted = galaxies.stream()
                .map(galaxy -> shift(colShift, rowShift, galaxy, shiftFactor))
                .collect(Collectors.toSet());

        return shifted.stream()
                .flatMap(original -> shifted.stream()
                        .filter(shift -> !shift.equals(original))
                        .map(shift -> new ShortestGalaxyPath(original, shift)))
                .distinct()
                .mapToLong(ShortestGalaxyPath::getDistance)
                .sum();
    }

    private static Point shift(final Collection<Integer> xShift, final Collection<Integer> yShift, final Point point, final int factor) {
        return new Point(
                point.getX() + (Math.max(1, (factor - 1)) * (int) xShift.stream().filter(i -> i < point.getX()).count()),
                point.getY() + (Math.max(1, (factor - 1)) * (int) yShift.stream().filter(i -> i < point.getY()).count())
        );
    }

    @Value
    private static class ShortestGalaxyPath {

        private static final BiFunction<Point, Point, Integer> MANHATTAN_DISTANCE = (current, next) ->
                Math.abs(current.getX() - next.getX()) + Math.abs(current.getY() - next.getY());

        Point a;
        Point b;
        long distance;

        ShortestGalaxyPath(Point a, Point b) {
            this.a = a;
            this.b = b;
            distance = MANHATTAN_DISTANCE.apply(a, b);
        }

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

}
