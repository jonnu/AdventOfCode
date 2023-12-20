package com.github.jonnu.advent.puzzle.y2023;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle13 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle13.txt")) {

            List<String> current = new ArrayList<>();
            List<Pattern> patterns = new ArrayList<>();

            String line = reader.readLine();
            for (;;) {

                if (line == null || line.isBlank()) {
                    patterns.add(Pattern.parse(current));
                    current.clear();
                    if (line == null) {
                        break;
                    }
                } else {
                    current.add(line);
                }

                line = reader.readLine();
            }

            System.out.println("Summarized notes value: " + patterns.stream().mapToInt(Pattern::summarize).sum());
        }
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Pattern {

        private static final char ASH = '.';
        private static final char ROCK = '#';

        Set<Point> rocks;
        int width;
        int height;

        public static Pattern parse(final List<String> input) {

            final int width = input.get(0).length();
            final int height = input.size();

            final Set<Point> point = IntStream.range(0, height)
                    .mapToObj(y -> IntStream.range(0, width)
                            .filter(x -> ROCK == input.get(y).charAt(x))
                            .mapToObj(x -> new Point(x, y))
                            .collect(Collectors.toSet()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            return new Pattern(point, width, height);
        }

        // @todo - create 2xMap<Integer, Integer> for rows and cols, with key
        // being row/col number and value being number of 'mistakes'.
        // need for part two, run simulation where mistakes == 1. for part one
        // we can run where number of mistakes == 0.
        public int summarize() {

            // try verts.
            for (int r = 1; r < width; r++) {

                int l = r - 1;
                //System.out.printf("Test %d & %d%n", l, r);
                boolean x = true;
                for (int a = l, b = r; a >= 0 && b < width; a--, b++) {
                    x = x && mirroredVertical(a, b);
                    //System.out.printf(" - inner test %d & %d = %s%n", a, b, x);
                }

                if (x) {
                    return r;
                }
            }

            //System.out.println("*** NO VERT FOUND ***");

            // try horiz.
            for (int b = 1; b < height; b++) {
                int t = b - 1;
                //System.out.printf("Test %d & %d%n", t, b);
                boolean z = true;
                for (int x = t, y = b; x >= 0 && y < height; x--, y++) {
                    z = z && mirroredHorizontal(x, y);
                    //System.out.printf(" - inner test %d & %d = %s%n", x, y, z);
                }

                if (z) {
                    return b * 100;
                }
            }

            return 0;//mirrored;
        }

        private boolean mirroredHorizontal(final int t, final int b) {
            return coordinates(p -> p.getY() == t, Point::getX).equals(coordinates(p -> p.getY() == b, Point::getX));
        }

        private boolean mirroredVertical(final int l, final int r) {
            return coordinates(p -> p.getX() == l, Point::getY).equals(coordinates(p -> p.getX() == r, Point::getY));
        }

        private Set<Integer> coordinates(final Predicate<Point> predicate, final Function<Point, Integer> flatten) {
            return rocks.stream().filter(predicate).map(flatten).collect(Collectors.toSet());
        }

        private void draw() {
            Set<Point> points = getRocks();
            final IntSummaryStatistics xStats = points.stream().mapToInt(Point::getX).summaryStatistics();
            final IntSummaryStatistics yStats = points.stream().mapToInt(Point::getY).summaryStatistics();
            System.out.printf("%n");
            for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
                for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                    Point p = new Point(x, y);
                    System.out.print(points.contains(p) ? "#" : ".");
                }
                System.out.printf("%n");
            }
            System.out.printf("%n");
        }
    }

}
