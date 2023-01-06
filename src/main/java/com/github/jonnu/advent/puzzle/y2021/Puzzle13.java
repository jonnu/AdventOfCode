package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle13 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        Set<Point> points = new HashSet<>();
        List<Fold> folds = new ArrayList<>();
        ParseMode parseMode = ParseMode.POINTS;

        try (BufferedReader reader = resourceReader.read("y2021/puzzle13.txt")) {
            String line = reader.readLine();
            while (line != null) {

                if (line.isEmpty()) {
                    parseMode = ParseMode.FOLDS;
                    line = reader.readLine();
                    continue;
                }

                switch (parseMode) {
                    case POINTS -> {
                        int[] xy = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
                        points.add(new Point(xy[0], xy[1]));
                    }
                    case FOLDS -> {
                        String dimension = line.substring(11, 12);
                        int foldPoint = Integer.parseInt(line.substring(13));
                        folds.add(Fold.create(dimension, foldPoint));
                    }
                }

                line = reader.readLine();
            }
        }

        Set<Point> selected;
        Set<Point> transformed = new HashSet<>();
        for (int f = 0; f < folds.size(); f++) {

            final Fold fold = folds.get(f);
            selected = points.stream().filter(fold.filter()).collect(Collectors.toSet());
            transformed = selected.stream().map(fold::apply).collect(Collectors.toSet());
            points.removeAll(selected);
            points.addAll(transformed);

            System.out.printf("After %d folds there are %d dots visible on the paper.%n", f + 1, points.size());
        }

        draw(points, transformed);
    }

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    private static void draw(final Set<Point> points, final Set<Point> highlight) {
        final IntSummaryStatistics xStats = points.stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = points.stream().mapToInt(Point::getY).summaryStatistics();
        System.out.printf("%n");
        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                System.out.print(points.contains(p) ? (highlight.contains(p) ? ANSI_RED + "▒" + ANSI_RESET : "▒") : " ");
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }

    @Getter(AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Fold {

        @Getter
        @AllArgsConstructor
        private enum FoldType {

            RIGHT_ONTO_LEFT("x", (fold, point) -> point.getX() > fold.getFoldPoint(),
                    (fold, point) -> new Point(fold.getFoldPoint() - (point.getX() - fold.getFoldPoint()), point.getY())),
            BOTTOM_ONTO_TOP("y", (fold, point) -> point.getY() > fold.getFoldPoint(),
                    (fold, point) -> new Point(point.getX(), fold.getFoldPoint() - (point.getY() - fold.getFoldPoint())));

            String axis;
            BiPredicate<Fold, Point> filter;
            BiFunction<Fold, Point, Point> translate;
        }

        int foldPoint;
        FoldType type;

        public Point apply(final Point point) {
            return getType().getTranslate().apply(this, point);
        }

        public Predicate<Point> filter() {
            return point -> getType().getFilter().test(this, point);
        }

        public static Fold create(final String axis, final int point) {
            return new Fold(point, Arrays.stream(FoldType.values())
                    .filter(t -> t.getAxis().equals(axis))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fold axis: " + axis)));
        }
    }

    private enum ParseMode {
        POINTS,
        FOLDS
    }
}
