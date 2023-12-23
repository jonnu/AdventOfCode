package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle10 implements Puzzle {

    private final ResourceReader resourceReader;

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    private static final Map<Pipe, Set<Direction>> CONNECTIONS = ImmutableMap.<Pipe, Set<Direction>>builder()
            .put(Pipe.ANIMAL, Direction.cardinal())
            .put(Pipe.VERTICAL, Set.of(Direction.NORTH, Direction.SOUTH))
            .put(Pipe.HORIZONTAL, Set.of(Direction.WEST, Direction.EAST))
            .put(Pipe.BEND_SW, Set.of(Direction.SOUTH, Direction.WEST))
            .put(Pipe.BEND_SE, Set.of(Direction.SOUTH, Direction.EAST))
            .put(Pipe.BEND_NE, Set.of(Direction.NORTH, Direction.EAST))
            .put(Pipe.BEND_NW, Set.of(Direction.NORTH, Direction.WEST))
            .build();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle10.txt")) {

            int result = 0;
            String line = reader.readLine();

            Queue<Point> queue = new ArrayDeque<>();
            Map<Point, Integer> processed = new HashMap<>();

            Map<Point, Pipe> grid = new HashMap<>();
            int y = 0;
            while (line != null) {

                for (int x = 0; x < line.length(); x++) {
                    Point point = new Point(x, y);
                    Pipe pipe = Pipe.parse(line.charAt(x));
                    grid.put(point, pipe);

                    if (pipe.equals(Pipe.ANIMAL)) {
                        queue.add(point);
                        processed.put(point, 0);
                    }
                }

                line = reader.readLine();
                y++;
            }

            draw(grid, Map.of());

            while (!queue.isEmpty()) {

                Point point = queue.poll();

                List<Point> points = point.cardinalNeighbours()
                        .entrySet()
                        .stream()
                        // I can only go into another pipe...
                        .filter(p -> Pipe.ALL.contains(grid.get(p.getValue())))
                        // and the pipe I'm leaving must have a connection in that direction...
                        .filter(p -> CONNECTIONS.get(grid.get(point)).contains(p.getKey()))
                        // If I'm going EAST (for example), the pipe in the point to the EAST must have an opposite (i.e. WEST) connection.
                        .filter(p -> CONNECTIONS.get(grid.get(p.getValue())).contains(p.getKey().opposite()))
                        .peek(p -> System.out.println("I'm going from " + point + " (" + grid.get(point).getAlternative() + ") " + p.getKey() + " into " + p.getValue() + " (" + grid.get(p.getValue()).getAlternative() + ") [connections: " + CONNECTIONS.get(grid.get(p.getValue())) + "] [opposite: " + p.getKey().opposite() + "]"))
                        // ...and I don't want to re-evaluate nodes in a closed loop.
                        .filter(p -> !processed.containsKey(p.getValue()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());

                final int steps = processed.get(point) + 1;
                points.forEach(p -> {
                    processed.put(p, steps);
                    queue.add(p);
                });
            }

            final int farthestPointFromOrigin = processed.values()
                    .stream()
                    .mapToInt(i -> i)
                    .max()
                    .orElseThrow();

            final IntSummaryStatistics xStats = grid.keySet().stream().mapToInt(Point::getX).summaryStatistics();
            final IntSummaryStatistics yStats = grid.keySet().stream().mapToInt(Point::getY).summaryStatistics();

            int numInside = 0;
            boolean inside;
            Pipe prev;
            Pipe curr;
            for (int b = yStats.getMin(); b <= yStats.getMax(); b++) {

                inside = false;
                prev = Pipe.GROUND;

                for (int a = xStats.getMin(); a <= xStats.getMax(); a++) {

                    Point point = new Point(a, b);
                    if (!processed.containsKey(point)) {
                        numInside += inside ? 1 : 0;
                        System.out.printf("%s", inside ? "I" : "O");
                        continue;
                    }

                    curr = grid.get(point);
                    switch (curr) {
                        case HORIZONTAL -> { System.out.print(grid.get(point).getAlternative()); continue; }
                        case VERTICAL -> { inside ^= true; }
                        default -> {
                            switch (prev) {
                                case BEND_NE, BEND_NW -> {
                                    if (Set.of(Pipe.BEND_SE, Pipe.BEND_SW).contains(curr)) {
                                        inside ^= true;
                                    } else {
                                        prev = curr;
                                    }
                                }
                                case BEND_SE, BEND_SW -> {
                                    if (Set.of(Pipe.BEND_NE, Pipe.BEND_NW).contains(curr)) {
                                        inside ^= true;
                                    } else {
                                        prev = curr;
                                    }
                                }
                                default -> prev = curr;
                            }
                        }
                    }

                    String out = processed.containsKey(point) ? grid.get(point).getAlternative() : inside ? "I" : "O";
                    System.out.printf("%s", out);
                }
                System.out.printf("%n");
            }
            System.out.printf("%n");

            System.out.println("Steps taken to reach farthest point from origin: " + farthestPointFromOrigin);
            System.out.println("Number of tiles enclosed by the loop: " + numInside);

            draw(grid, Map.of());
            draw(grid, processed);
        }
    }

    private static <T, R> void draw(final Map<Point, T> points, final Map<Point, R> highlight) {
        final IntSummaryStatistics xStats = points.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = points.keySet().stream().mapToInt(Point::getY).summaryStatistics();
        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                System.out.printf("%s%s%s", highlight.containsKey(p) ? ANSI_RED : "", highlight.containsKey(p) ? highlight.get(p).toString().substring(highlight.get(p).toString().length() - 1) : points.get(p), highlight.containsKey(p) ? ANSI_RESET : "");
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }

    @Getter
    @AllArgsConstructor
    enum Pipe {
        // remove these:
        GROUND(".", "."),
        ANIMAL("S", "s"),

        VERTICAL("|", "║"),
        HORIZONTAL("-", "\u2550"),
        BEND_NE("L", "╚"),
        BEND_NW("J", "\u255D"),
        BEND_SW("7", "╗"),
        BEND_SE("F", "╔")
        ;

        public static final Set<Pipe> ALL = Arrays.stream(Pipe.values())
                .filter(p -> !p.equals(Pipe.ANIMAL) && !p.equals(Pipe.GROUND))
                .collect(Collectors.toSet());

        private final String character;
        private final String alternative;

        @Override
        public String toString() {
            return getAlternative();
        }

        public static Pipe parse(final char input) {
            return parse(Character.toString(input));
        }

        public static Pipe parse(final String input) {
            return Arrays.stream(values())
                    .filter(p -> p.getCharacter().equals(input))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown pipe character: " + input));
        }
    }

}
