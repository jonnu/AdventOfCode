package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle5 implements Puzzle {

    private static final int DANGER_THRESHOLD = 2;
    private static final String POINT_SEPARATOR = " -> ";
    private static final String COORDINATE_SEPARATOR = ",";

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle5.txt")) {

            final List<Line> lines = new ArrayList<>();

            String line = reader.readLine();
            while (line != null) {
                lines.add(Arrays.stream(line.split(POINT_SEPARATOR))
                        .map(pair -> pair.split(COORDINATE_SEPARATOR))
                        .map(x -> new Point(Integer.parseInt(x[0]), Integer.parseInt(x[1])))
                        .collect(Collector.of(Line::new, Line::combine, (a, b) -> { throw new UnsupportedOperationException(); })));
                line = reader.readLine();
            }

            final Map<Point, Long> straightCounts = lines.stream()
                    .filter(Line::isStraight)
                    .map(Line::covers)
                    .flatMap(Set::stream)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            final Map<Point, Long> allCounts = lines.stream()
                    .map(Line::covers)
                    .flatMap(Set::stream)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            final long straightOverlappingPoints = straightCounts.values().stream().filter(x -> x >= DANGER_THRESHOLD).count();
            final long allOverlappingPoints = allCounts.values().stream().filter(x -> x >= DANGER_THRESHOLD).count();

            System.out.println("Straight lines with two-point overlaps: " + straightOverlappingPoints);
            System.out.println("All lines with two-point overlaps: " + allOverlappingPoints);
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    private static class Line {

        Point x;
        Point y;

        public boolean isStraight() {
            return x.getX() == y.getX() || x.getY() == y.getY();
        }

        public Set<Point> covers() {
            final double xd = Math.signum(y.getX() - x.getX());
            final double yd = Math.signum(y.getY() - x.getY());
            final IntStream xs = IntStream.rangeClosed(Math.min(x.getX(), y.getX()), Math.max(x.getX(), y.getX()));
            final IntStream ys = IntStream.rangeClosed(Math.min(x.getY(), y.getY()), Math.max(x.getY(), y.getY()));
            final Iterator<Integer> xi = xd >= 0 ? xs.iterator() : xs.boxed().sorted(Collections.reverseOrder()).iterator();
            final Iterator<Integer> yi = yd >= 0 ? ys.iterator() : ys.boxed().sorted(Collections.reverseOrder()).iterator();
            int x = getX().getX();
            int y = getX().getY();
            final Set<Point> covered = new HashSet<>();
            while (xi.hasNext() || yi.hasNext()) {
                x = xi.hasNext() ? xi.next() : x;
                y = yi.hasNext() ? yi.next() : y;
                covered.add(new Point(x, y));
            }
            return covered;
        }

        public void combine(final Point point) {
            if (x == null) {
                setX(point);
            } else if (y == null) {
                setY(point);
            }
        }

        @Override
        public String toString() {
            return x.toString() + " - " + y.toString();
        }
    }

}
