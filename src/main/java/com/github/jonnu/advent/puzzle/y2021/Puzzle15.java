package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle15 implements Puzzle {

    private static final BiFunction<Point, Point, Integer> MANHATTAN_DISTANCE = (a, b) -> Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    private static final ToIntBiFunction<Integer, Integer> INCREASE_HEURISTIC = (factor, i) -> i + factor >= 10 ? 1 + ((i + factor) % 10) : i + factor;
    private static final int GROWTH_FACTOR = 5;

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle15.txt")) {

            List<List<Integer>> grid = new ArrayList<>();

            String line = reader.readLine();
            while (line != null) {
                final String data = line;
                grid.add(IntStream.range(0, line.length()).mapToObj(i -> Integer.parseInt(Character.toString(data.charAt(i)))).collect(Collectors.toList()));
                line = reader.readLine();
            }

            Path path = calculatePath(grid);
            System.out.println("Lowest risk on part1 grid: " + path.getRisk());

            grid = growGrid(grid, GROWTH_FACTOR, INCREASE_HEURISTIC);
            path = calculatePath(grid);
            System.out.println("Lowest risk on part2 grid: " + path.getRisk());
        }
    }

    private Path calculatePath(final List<List<Integer>> grid) {

        final Point start = new Point(0, 0);
        final Point finish = new Point(grid.get(0).size() - 1, grid.size() - 1);

        final Predicate<Point> inBounds = p -> p.getX() >= 0 && p.getX() < grid.get(0).size() && p.getY() >= 0 && p.getY() < grid.size();

        final Map<Point, Integer> costs = new HashMap<>();
        final Map<Point, Point> movement = new HashMap<>();
        final Queue<Path> frontier = new PriorityQueue<>(Comparator.comparingInt(path -> path.getRisk() + MANHATTAN_DISTANCE.apply(path.getPoint(), finish)));
        frontier.add(new Path(0, start));

        while (!frontier.isEmpty()) {

            final Path path = frontier.poll();

            if (path.getPoint().equals(finish)) {
                Set<Point> visited = new HashSet<>();
                Point current = finish;
                while (!current.equals(start)) {
                    visited.add(current);
                    current = movement.get(current);
                }
                visited.add(start);
                path.setVisited(visited);
                return path;
            }

            path.getPoint().cardinalNeighbours().values().stream().filter(inBounds).forEach(point -> {
                int cost = path.getRisk() + grid.get(point.getY()).get(point.getX());
                if (!costs.containsKey(point) || cost < costs.get(point)) {
                    costs.put(point, cost);
                    frontier.add(new Path(cost, point));
                    movement.put(point, path.getPoint());
                }
            });
        }

        return null;
    }

    private static List<List<Integer>> growGrid(final List<List<Integer>> original, final int factor, final ToIntBiFunction<Integer, Integer> heuristic) {
        final List<List<Integer>> destination = new ArrayList<>();

        for (int y = 0; y < original.size() * factor; y++) {
            destination.add(new ArrayList<>());
            for (int x = 0; x < original.get(y % original.size()).size() * factor; x++) {
                int f = Math.floorDiv(x, original.size()) + Math.floorDiv(y, original.get(y % original.size()).size());
                destination.get(y).add(x, heuristic.applyAsInt(f, original.get(y % original.size()).get(x % original.get(y % original.size()).size())));
            }
        }

        return destination;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @RequiredArgsConstructor
    private static class Path {
        final int risk;
        final Point point;
        @Setter Set<Point> visited;
    }

}
