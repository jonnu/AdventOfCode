package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    private static final Map<Pipe, Set<Pipe>> ALLOWED = ImmutableMap.<Pipe, Set<Pipe>>builder()
            .put(Pipe.ANIMAL, Pipe.ALL)
            .put(Pipe.VERTICAL, Pipe.ALL)
            .put(Pipe.HORIZONTAL, Pipe.ALL)
            .put(Pipe.BEND_SW, Set.of(Pipe.HORIZONTAL, Pipe.VERTICAL, Pipe.BEND_NW, Pipe.BEND_SE))
            .put(Pipe.BEND_SE, Set.of(Pipe.HORIZONTAL, Pipe.VERTICAL, Pipe.BEND_NE, Pipe.BEND_SW))
            .put(Pipe.BEND_NE, Set.of(Pipe.HORIZONTAL, Pipe.VERTICAL, Pipe.BEND_NW, Pipe.BEND_SE))
            .put(Pipe.BEND_NW, Set.of(Pipe.HORIZONTAL, Pipe.VERTICAL, Pipe.BEND_NE, Pipe.BEND_SW, Pipe.BEND_SE))
            .build();

    private static final Map<Pipe, Set<Direction>> CONNECTIONS = ImmutableMap.<Pipe, Set<Direction>>builder()
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

            Map<Point, Pipe> grid = new HashMap<>();
            int y = 0;
            while (line != null) {

                for (int x = 0; x < line.length(); x++) {
                    Point point = new Point(x, y);
                    Pipe pipe = Pipe.parse(line.charAt(x));
                    grid.put(point, pipe);

                    if (pipe.equals(Pipe.ANIMAL)) {
                        queue.add(point);
                    }
                }

                line = reader.readLine();
                y++;
            }

            draw(grid, Set.of());

            int steps = 1;
            Set<Point> processed = new HashSet<>();
            while (!queue.isEmpty()) {

                Point point = queue.poll();
                Pipe current = grid.get(point);


                List<Point> points = point.cardinalNeighbours()
                        .stream()
                        // todo: need to evaluate the cardinal neighbors.
                        // tuples of current pipe + direction = valid.
                        // otherwise JF is valid (because they back onto each other) which isn't true.
                        .filter(p -> current.valid().contains(grid.get(p)))
                        .filter(p -> !processed.contains(p))
                        .collect(Collectors.toList());

                System.out.println("Processing " + current + " point: " + point + " (" + points + ")");
                if (!points.isEmpty()) {
                    steps++;
                }

                processed.add(point);

                points.forEach(p -> {
//                    if (processed.contains(p)) {
//                        return;
//                    }
                    System.out.println("  could go to " + p + " via " + grid.get(p));
                    queue.add(p);

                });

            }

            System.out.println(Math.floor(steps / 2));
        }
    }

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    private static <T> void draw(final Map<Point, T> points, final Set<Point> highlight) {
        final IntSummaryStatistics xStats = points.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = points.keySet().stream().mapToInt(Point::getY).summaryStatistics();
        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                System.out.printf("%s%s%s", highlight.contains(p) ? ANSI_RED : "", points.get(p), highlight.contains(p) ? ANSI_RESET : "");
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

        // annoyingly box-drawing characters dont work in the ide.
        VERTICAL("|", "\u2502"),
        HORIZONTAL("-", "\u2500"),
        BEND_NE("L", "\u2514"),
        BEND_NW("J", "\u2518"),
        BEND_SW("7", "\u2510"),
        BEND_SE("F", "\u250C")
        ;

        public static final Set<Pipe> ALL = Arrays.stream(Pipe.values())
                .filter(p -> !p.equals(Pipe.ANIMAL) && !p.equals(Pipe.GROUND))
                .collect(Collectors.toSet());

        private final String character;
        private final String alternative;

        @Override
        public String toString() {
            return getCharacter();
        }

        public Set<Pipe> valid() {
            if (equals(Pipe.HORIZONTAL) || equals(Pipe.VERTICAL) || equals(Pipe.ANIMAL)) {
                return ALL;
            }
            Set<Pipe> ret = new HashSet<>(ALL);
            ret.remove(this);
            return ret;
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
