package com.github.jonnu.advent.puzzle.y2024;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle4 implements Puzzle {

    private static final Map<Point, String> GRID = new HashMap<>();

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2024/puzzle4.txt")) {

            int y = 0;
            String line = reader.readLine();
            while (line != null) {
                final String row = line;
                final int index = y;
                GRID.putAll(IntStream.range(0, line.length())
                        .mapToObj(x -> new Point(x, index))
                        .collect(Collectors.toMap(Function.identity(), v -> String.valueOf(row.charAt(v.getX())))));
                y++;
                line = reader.readLine();
            }

            long xmasLine = GRID.entrySet()
                    .stream()
                    .filter(point -> "X".equals(point.getValue()))
                    .map(Map.Entry::getKey)
                    .flatMap(point -> point.neighbours().keySet().stream().map(direction -> line(point, direction)))
                    .filter(vector -> vector.stream().allMatch(Puzzle4::isWithinBounds) && vector.stream().map(GRID::get).toList().equals(List.of("X", "M", "A", "S")))
                    .count();

            System.out.println("There are " + xmasLine + " occurrences of XMAS.");

            // part 2.
            long xmasCross = GRID.entrySet()
                    .stream()
                    .filter(point -> "A".equals(point.getValue()))
                    .filter(point -> canFormXmas(point.getKey()))
                    .count();

            System.out.println("There are " + xmasCross + " occurrences of X-MAS.");
        }
    }

    private static boolean isWithinBounds(final Point point) {
        return GRID.containsKey(point);
    }

    private static boolean canFormXmas(final Point centre) {
        return Stream.of(Set.of(centre.move(Direction.NORTHWEST), centre, centre.move(Direction.SOUTHEAST)),
                         Set.of(centre.move(Direction.NORTHEAST), centre, centre.move(Direction.SOUTHWEST)))
                .allMatch(points ->
                        points.stream().allMatch(Puzzle4::isWithinBounds) &&
                        points.stream().map(GRID::get).collect(Collectors.toSet()).equals(Set.of("M", "A", "S")));
    }

    private static List<Point> line(final Point origin, final Direction direction) {
        Point prev = origin;
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            points.add(prev);
            prev = prev.move(direction);
        }
        return points;
    }

}
