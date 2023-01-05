package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle11 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle11.txt")) {

            final Map<Point, Integer> octopuses = new HashMap<>();

            int row = 0;
            String line = reader.readLine();
            while (line != null) {
                final int y = row;
                final String data = line;
                octopuses.putAll(IntStream.range(0, line.length())
                        .mapToObj(x -> new Point(x, y))
                        .collect(Collectors.toMap(Function.identity(), point -> Integer.parseInt(Character.toString(data.charAt(point.getX()))))));
                line = reader.readLine();
                row++;
            }

            // 100 steps.
            Queue<Point> process = new ArrayDeque<>();

            System.out.println("Before any steps:");
            long flashCount = 0;

            int step = 0;
            while (!isSynchronised(octopuses)) {

                step++;

                // first, increment.
                octopuses.forEach((p, v) -> octopuses.replace(p, v + 1));

                // start flash sequence.
                Set<Point> flashing = octopuses.entrySet().stream().filter(x -> x.getValue() > 9).map(Map.Entry::getKey).collect(Collectors.toSet());
                Set<Point> flashed = new HashSet<>();
                process.addAll(flashing);

                while (!process.isEmpty()) {

                    Point current = process.poll();
                    if (flashed.contains(current)) {
                        continue;
                    }

                    flashed.add(current);

                    Set<Point> neighbours = Arrays.stream(Direction.values())
                            .map(current::move)
                            .filter(p -> p.getX() >= 0 && p.getX() <= 9)
                            .filter(p -> p.getY() >= 0 && p.getY() <= 9)
                            .collect(Collectors.toSet());

                    // Increment.
                    neighbours.forEach(p -> octopuses.replace(p, octopuses.get(p) + 1));

                    // Energy used. reset to 0.
                    //octopuses.replace(current, 0);

                    Set<Point> flashingNeighbours = neighbours.stream()
                            .filter(neighbour -> !flashed.contains(neighbour))
                            .filter(neighbour -> octopuses.get(neighbour) > 9)
                            .collect(Collectors.toSet());

                    process.addAll(flashingNeighbours);
                }

                // reset.
                flashed.forEach(p -> octopuses.replace(p, 0));

                flashCount += flashed.size();

                if (step == 100) {
                    System.out.println("After step " + step + " (count: " + flashCount + ")");
                    draw(octopuses, flashed);
                }

            }

            System.out.println("In Sync after " + step);
            draw(octopuses);
        }
    }

    private static boolean isSynchronised(final Map<Point, Integer> octopuses) {
        return octopuses.values().stream().distinct().count() == 1;
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String SET_PLAIN_TEXT = "\033[0;0m";
    public static final String SET_BOLD_TEXT = "\033[0;1m";
    private static void draw(final Map<Point, Integer> points) {
        draw(points, Collections.emptySet());
    }

    private static void draw(final Map<Point, Integer> points, final Set<Point> highlight) {
        final IntSummaryStatistics xStats = points.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = points.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                System.out.printf("%s%s%s", highlight.contains(p) ? ANSI_RED : "", points.get(p), highlight.contains(p) ? ANSI_RESET : "");
            }
            System.out.printf("%n");
        }
        System.out.printf("%n%n");
    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Point {

        int x;
        int y;

        public Point move(final Direction direction) {
            return new Point(x + direction.getDelta()[0], y + direction.getDelta()[1]);
        }

        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    @Getter
    @AllArgsConstructor
    private enum Direction {

        NORTH(new int[] { 0, -1 }),
        NORTHEAST(new int[] { 1, -1 }),
        EAST(new int[] { 1, 0 }),
        SOUTHEAST(new int[] { 1, 1 }),
        SOUTH(new int[] { 0, 1 }),
        SOUTHWEST(new int[] { -1, 1 }),
        WEST(new int[] { -1, 0 }),
        NORTHWEST(new int[] { -1, -1 });

        private final int[] delta;
    }
}
