package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle23 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle23.txt")) {

            Map<Point, Tile> tiles = new HashMap<>();
            String line = reader.readLine();

            int row = 0;
            int max = line.length();

            while (line != null) {

                final int y = row;
                final String data = line;

                tiles.putAll(IntStream.range(0, line.length())
                        .mapToObj(x -> new Point(x, y))
                        .collect(Collectors.toMap(
                                Function.identity(),
                                point -> Tile.parse(data.charAt(point.getX()))))
                );

                line = reader.readLine();
                row++;
            }

            Grid2D grid = new Grid2D(tiles, (a, b) -> Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()));

            // Dijkstra but longest, + heuristic for going to bottom-right?
            System.out.print(grid);

            grid.navigate(new Point(0, 1), new Point(max - 2, row - 1));
        }
    }



    interface PathfindingNode {

        default long getCost() {
            return 1L;
        }

        default boolean isNavigable() {
            return true;
        };
    }

    @Value
    @RequiredArgsConstructor
    public static class Grid2D {

        Map<Point, Tile> grid;
        BiFunction<Point, Point, Integer> heuristic;

        int minX;
        int maxX;
        int minY;
        int maxY;

        public Grid2D(final Map<Point, Tile> grid, final BiFunction<Point, Point, Integer> heuristic) {
            this.grid = grid;
            this.heuristic = heuristic;

            final IntSummaryStatistics xStats = grid.keySet().stream().mapToInt(Point::getX).summaryStatistics();
            final IntSummaryStatistics yStats = grid.keySet().stream().mapToInt(Point::getY).summaryStatistics();

            minX = xStats.getMin();
            maxX = xStats.getMax();
            minY = yStats.getMin();
            maxY = yStats.getMax();
        }

        public void navigate(final Point origin, final Point destination) {

            Queue<Path> queue = new PriorityQueue<>((a, b) -> Integer.compare(heuristic.apply(destination, a.current()), heuristic.apply(destination, b.current())));
            // bad heuristic: 7m17s
            //Queue<Path> queue = new PriorityQueue<>(Comparator.comparing(Path::length).reversed());
            Queue<Path> completed = new PriorityQueue<>((a, b) -> Comparator.<Integer>reverseOrder().compare(a.length(), b.length()));

            queue.add(new Path(origin));

            while (!queue.isEmpty()) {
                final Path current = queue.poll();

                // Check if we reached the destination.
                if (current.current().equals(destination)) {
                    completed.add(current);
                    continue;
                }

                // Evaluate which ways we can move here.
                // takes 6.407 min for part 1. too slow. needs optimisation.
                current.current()
                        .neighbours()
                        .entrySet()
                        .stream()
                        // cannot navigate outside the grid.
                        .filter(entry -> withinBounds(entry.getValue()))
                        // cannot navigate into forests.
                        .filter(entry -> !grid.get(entry.getValue()).equals(Tile.FOREST))
                        // filter to "allowed" directions. tiles with direction (slopes) are explicit.
                        .filter(entry -> Optional.ofNullable(grid.get(entry.getValue()))
                                .map(tile -> Optional.ofNullable(tile.getDirection())
                                        .map(List::of)
                                        .orElse(Direction.cardinal()))
                                .orElse(List.of())
                                .contains(entry.getKey()))
                        // cannot "revisit" a previously visited node.
                        .filter(entry -> !current.getPathway().contains(entry.getValue()))
                        .map(Map.Entry::getValue)
                        .forEach(neighbour -> queue.add(new Path(current, neighbour)));
            }

            while (!completed.isEmpty()) {
                Path complete = completed.poll();
                System.out.println(complete);
                break;
            }
        }

        public boolean withinBounds(final Point point) {
            return point.getX() >= minX && point.getX() <= maxX && point.getY() >= minY && point.getY() <= maxY;
        }

        @Override
        public String toString() {

            StringBuilder builder = new StringBuilder();
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    Point p = new Point(x, y);
                    builder.append(Optional.ofNullable(grid.get(p)).map(Object::toString).orElse("."));
                }
                builder.append("\n");
            }

            return builder.append("\n").toString();
        }
    }

    @Data
    @AllArgsConstructor
    public static class Path {

        private final List<Point> pathway;

        public Path(final Point origin) {
            pathway = new ArrayList<>(List.of(origin));
        }

        public Path(final Path path, final Point next) {
            List<Point> points = new ArrayList<>(path.getPathway());
            points.add(next);
            pathway = points;
        }

        public Point current() {
            return pathway.get(length());
        }

        public int length() {
            // -1 as we don't count the origin as a 'step'.
            return pathway.size() - 1;
        }

        public String toString() {
            return "Path(" + length() + ") [" + pathway.stream().map(Point::toString).collect(Collectors.joining(", ")) + "]";
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Tile {

        PATH(".", null),
        FOREST("#", null),
        SLOPE_N("^", Direction.NORTH),
        SLOPE_E(">", Direction.EAST),
        SLOPE_S("v", Direction.SOUTH),
        SLOPE_W("<", Direction.WEST)
        ;

        private final String character;
        private final Direction direction;

        public static Tile parse(final char character) {
            return Arrays.stream(Tile.values())
                    .filter(tile -> tile.getCharacter().equals(String.valueOf(character)))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown tile character: " + character));
        }

        @Override
        public String toString() {
            return getCharacter();
        }
    }
}
