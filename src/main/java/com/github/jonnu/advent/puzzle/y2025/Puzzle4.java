package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle4 implements Puzzle {

    private final ResourceReader resourceReader;

    private final Set<Point> grid = new HashSet<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2025/puzzle4.txt")) {

            int maxX = -1;
            int maxY = 0;
            String line = reader.readLine();
            while (line != null) {

                final int y = maxY;
                for (int x = 0; x < line.length(); x++) {
                    if (line.charAt(x) == '@') {
                        grid.add(new Point(x, y));
                    }
                }

                line = reader.readLine();
                maxX = line != null && !line.isEmpty() && maxX < 0 ? line.length() : maxX;
                maxY++;
            }

            final Point bounds = new Point(maxX, maxY);
            System.out.println("[Part 1] Forklift accessible rolls: " + accessible(grid, bounds).size());

            int removed = 0;
            for (;;) {
                Set<Point> accessible = accessible(grid, bounds);
                if (accessible.isEmpty()) {
                    break;
                }
                removed += accessible.size();
                grid.removeAll(accessible);
            }

            draw(grid, new HashSet<>());
            System.out.println("[Part 2] Forklift removable rolls: " + removed);
        }
    }

    private Set<Point> accessible(final Set<Point> grid, final Point bounds) {
        return grid.stream()
                .filter(point -> point.neighbours()
                        .values()
                        .stream()
                        .filter(neighbour -> neighbour.getX() >= 0
                                && neighbour.getX() <= bounds.getX()
                                && neighbour.getY() >= 0
                                && neighbour.getY() <= bounds.getY()
                                && grid.contains(neighbour)
                        )
                        .count() < 4
                )
                .collect(Collectors.toSet());
    }

    private static void draw(Set<Point> points, Set<Point> accessible) {
        final IntSummaryStatistics xStats = points.stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = points.stream().mapToInt(Point::getY).summaryStatistics();
        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                System.out.printf("%s", accessible.contains(p) ? "x" : points.contains(p) ? "@" : ".");
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }
}
