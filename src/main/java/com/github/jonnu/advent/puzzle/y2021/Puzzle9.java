package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle9 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle9.txt")) {

            final Map<Point, Integer> cave = new HashMap<>();

            String line = reader.readLine();
            int row = 0;
            while (line != null) {
                int y = row;
                String data = line;
                cave.putAll(IntStream.range(0, line.length())
                        .mapToObj(x -> new Point(x, y))
                        .collect(Collectors.toMap(k -> k, v -> Character.getNumericValue(data.charAt(v.getX())))));

                line = reader.readLine();
                row++;
            }

            IntSummaryStatistics xStats = cave.keySet().stream().mapToInt(Point::getX).summaryStatistics();
            IntSummaryStatistics yStats = cave.keySet().stream().mapToInt(Point::getY).summaryStatistics();

            Set<Point> lowPoints = cave.entrySet().stream()
                    .filter(entry -> entry.getKey()
                            .cardinalNeighbours()
                            .stream()
                            .filter(neighbour -> neighbour.getX() >= xStats.getMin() && neighbour.getX() <= xStats.getMax())
                            .filter(neighbour -> neighbour.getY() >= yStats.getMin() && neighbour.getY() <= yStats.getMax())
                            .allMatch(point -> cave.get(point) > entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            int riskLevel = lowPoints.stream()
                    .mapToInt(point -> cave.get(point) + 1)
                    .sum();

            Set<Point> visited = new HashSet<>();
            Deque<Point> points = new ArrayDeque<>(lowPoints);
            PriorityQueue<List<Integer>> basins = new PriorityQueue<>((b1, b2) -> Integer.compare(b2.size(), b1.size()));

            List<Integer> current = new ArrayList<>();
            while (!points.isEmpty()) {
                Point point = points.poll();

                if (lowPoints.contains(point)) {
                    if (current.size() > 0) {
                        basins.add(current);
                    }
                    current = new ArrayList<>();
                }

                if (!visited.contains(point)) {

                    visited.add(point);
                    current.add(cave.get(point));

                    point.cardinalNeighbours()
                            .stream()
                            .filter(neighbour -> !visited.contains(neighbour))
                            .filter(neighbour -> neighbour.getX() >= xStats.getMin() && neighbour.getX() <= xStats.getMax())
                            .filter(neighbour -> neighbour.getY() >= yStats.getMin() && neighbour.getY() <= yStats.getMax())
                            .filter(neighbour -> cave.get(neighbour) < 9)
                            .forEach(points::addFirst);
                }
            }
            basins.add(current);

            long basinSize = Stream.generate(basins::poll)
                    .limit(3)
                    .takeWhile(Objects::nonNull)
                    .mapToLong(List::size)
                    .reduce(Math::multiplyExact)
                    .orElseThrow(() -> new IllegalArgumentException("Unable to find basins."));

            System.out.println("Sum of the risk levels of all low points: " + riskLevel);
            System.out.println("Multiple of three largest basins by size: " + basinSize);
        }
    }

}
