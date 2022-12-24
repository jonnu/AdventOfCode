package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle23 implements Puzzle {

    private static final int MOVEMENT_ROUNDS = 10;
    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle23.txt")) {

            String line = reader.readLine();
            Grove grove = new Grove(line.length(), MOVEMENT_ROUNDS);

            while (line != null) {
                grove.add(Arrays.stream(line.split("(?<=.)")).collect(Collectors.toList()));
                line = reader.readLine();
            }

            //System.out.println("== Initial State ==");
            //grove.debug(false);
            for (int r = 1; r <= MOVEMENT_ROUNDS; r++) {

                // decide.
                grove.setElfNextMove();

                // attempt move.
                int moved = grove.executeElfNextMove();

                //System.out.printf("%n%n== End of Round %d ==%n", r);
                //grove.debug(true);
                if (r == MOVEMENT_ROUNDS) {
                    System.out.println("Empty ground tiles: " + grove.compute());
                }

                if (moved == 0) {
                    System.out.println("Ending after " + r + " rounds.");
                    break;
                }
            }

            //grove.debug(false);

        }
    }

    private static class Grove {

        private final List<List<Position>> grove;

        int xMin;
        int xMax;
        int yMin;
        int yMax;
        int size;
        int padding;
        int addedRows = 0;

        Set<Elf> elves = new HashSet<>();

        Grove(int size, int padding) {

            this.size = size;
            this.padding = padding;

            // viewport.
            this.xMin = padding;
            this.xMax = padding + size;
            this.yMin = padding;
            this.yMax = padding + size;

            grove = IntStream.range(0, yMax + padding)
                    .mapToObj(y -> IntStream.range(0, xMax + padding)
                            .mapToObj(x -> Position.empty(x, y))
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }

        public BiPredicate<Elf, Map<CardinalDirection, Point>> moveElfIfNotPresent(final CardinalDirection toMove, final Set<CardinalDirection> ifNotPresent) {
            return (elf, map) -> {

                //System.out.print("[" + elf.getPoint() + "] ");
                //System.out.print("Considering move to " + toMove + " first (if not: " + ifNotPresent.stream().map(Enum::name).collect(Collectors.joining(", ")) + ")");
                //System.out.print(" | Elves adjacent: " + map.keySet().stream().map(Enum::name).collect(Collectors.joining(", ")));
                if (ifNotPresent.stream().noneMatch(map::containsKey)) {
                    //System.out.print(" -> MOVE IS POSSIBLE! ");
                    elf.setNextMove(toMove);
                    //System.out.printf("[%s]%n", elf.getSuggestedMove());
                    return true;
                }
                //System.out.printf(" -> MOVE IS NOT POSSIBLE%n");
                return false;
            };
        }

        private final Queue<BiPredicate<Elf, Map<CardinalDirection, Point>>> movementPriority = new ArrayDeque<>(ImmutableList.of(
                moveElfIfNotPresent(CardinalDirection.NORTH, CardinalDirection.northern()),
                moveElfIfNotPresent(CardinalDirection.SOUTH, CardinalDirection.southern()),
                moveElfIfNotPresent(CardinalDirection.WEST, CardinalDirection.western()),
                moveElfIfNotPresent(CardinalDirection.EAST, CardinalDirection.eastern())
        ));

        public void setElfNextMove() {

            List<BiPredicate<Elf, Map<CardinalDirection, Point>>> movements = new ArrayList<>(movementPriority);

            elves.forEach(elf -> {
                final Map<CardinalDirection, Point> nearbyElves = elf.surroundings().entrySet().stream()
                        .filter(s -> containsElf(s.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                // If no other Elves are in one of those eight positions, the Elf does not do anything during this round.
                if (nearbyElves.isEmpty()) {
                    return;
                }

                for (int i = 0; i < movements.size(); i++) {
                    if (movements.get(i).test(elf, nearbyElves)) {
                        break;
                    }
                }
            });

            // move it to the rear.
            BiPredicate<Elf, Map<CardinalDirection, Point>> firstMovement = movementPriority.poll();
            movementPriority.add(firstMovement);
        }

        public int executeElfNextMove() {
            AtomicInteger elfMoves = new AtomicInteger();
            Map<Point, List<Elf>> proposals = elves.stream().filter(Elf::hasSuggestedMove).collect(Collectors.groupingBy(Elf::getSuggestedMove));
            proposals.forEach((point, elves) -> {

                // not interested if multiple elves want to move.
                if (elves.size() > 1) {
                    return;
                }

                // update Elf position
                Elf elf = elves.get(0);
                //System.out.println("Moving Elf from " + elf.getPoint() + " to " + elf.getSuggestedMove());
                grove.get(elf.getPoint().getY()).get(elf.getPoint().getX()).removeElf();
                elf.setPoint(elf.getSuggestedMove());
                grove.get(elf.getPoint().getY()).get(elf.getPoint().getX()).setElf(elf);
                elfMoves.getAndIncrement();

                // update view bounds.
                //xMin = Math.min(xMin, elf.getPoint().getX());
                //xMax = Math.max(xMax, elf.getPoint().getX());
                //yMin = Math.min(yMin, elf.getPoint().getY());
                //yMax = Math.max(yMax, elf.getPoint().getY());
            });

            elves.forEach(Elf::resetNextMove);
            return elfMoves.get();
        }

        public boolean containsElf(final Point point) {
            return grove.get(point.getY()).get(point.getX()).containsElf();
        }

        public long compute() {
            IntSummaryStatistics x = grove.stream()
                    .flatMap(Collection::stream)
                    .filter(Position::containsElf)
                    .map(Position::getPoint)
                    .map(Point::getX)
                    .collect(Collectors.summarizingInt(Integer::intValue));

            IntSummaryStatistics y = grove.stream()
                    .flatMap(Collection::stream)
                    .filter(Position::containsElf)
                    .map(Position::getPoint)
                    .map(Point::getY)
                    .collect(Collectors.summarizingInt(Integer::intValue));

            return grove.stream().flatMap(Collection::stream)
                    .filter(position -> position.withinBoundary(new Point(x.getMin(), y.getMin()), new Point(x.getMax(), y.getMax())))
                    .filter(position -> !position.containsElf())
                    .count();
        }

        public void add(final List<String> row) {
            int y = yMin + addedRows;
            IntStream.range(xMin, xMax)
                    .mapToObj(x -> Position.create(row.get(x - xMin), x, y))
                    .forEach(p -> grove.get(y).set(p.getPoint().getX(), p));
            elves.addAll(grove.get(y).stream().filter(Position::containsElf).map(Position::getElf).collect(Collectors.toSet()));
            addedRows++;
        }

        public void debug() {
            debug(true);
        }

        public void debug(boolean showPadding) {

            int xx = showPadding ? 0 : xMin;
            int xy = showPadding ? xMax + padding : xMax;
            int yx = showPadding ? 0 : yMin;
            int yy = showPadding ? yMax + padding : yMax;
            boolean number = false;
            for (int y = yx; y < yy; y++) {
                if (number && y == yx) {
                    System.out.printf("     ");
                    for (int x = xx; x < xy; x++) {
                        System.out.printf("%d", Math.floorDiv(x, 10));
                    }
                    System.out.printf("%n     ");
                    for (int x = xx; x < xy; x++) {
                        System.out.printf("%d", x % 10);
                    }
                    System.out.printf("%n");
                }
                if (number) {
                    System.out.printf("%2d | ", y);
                }

                for (int x = xx; x < xy; x++) {
                    System.out.printf("%s", grove.get(y).get(x));
                }
                System.out.printf("%n");
            }
            //System.out.printf("xmin: %d, xmax: %d, ymin: %d, ymax: %d%n", xMin, xMax, yMin, yMax);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Position {

        private static String ELF = "#";
        private static String EMPTY = ".";

        private Elf elf;
        private final Point point;

        public void removeElf() {
            this.elf = null;
        }

        public boolean containsElf() {
            return elf != null;
        }

        public boolean withinBoundary(final Point topLeft, final Point bottomRight) {
            return point.getX() >= topLeft.getX() && point.getX() <= bottomRight.getX() &&
                    point.getY() >= topLeft.getY() && point.getY() <= bottomRight.getY();
        }

        public static Position create(final String character, int x, int y) {
            final Point point = new Point(x, y);
            return new Position(character.equals(ELF) ? new Elf(point, null) : null, point);
        }

        public static Position empty(int x, int y) {
            return new Position(null, new Point(x, y));
        }

        @Override
        public String toString() {
            return elf == null ? EMPTY : ELF;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Elf {

        Point point;
        Point suggestedMove;

        public boolean hasSuggestedMove() {
            return suggestedMove != null;
        }

        public void setNextMove(final CardinalDirection direction) {
            suggestedMove = point.move(direction);
            //System.out.println("Suggesting move in " + direction.name() + " from " + point + " to " + suggestedMove);
        }

        public void resetNextMove() {
            suggestedMove = null;
        }

        public Map<CardinalDirection, Point> surroundings() {
            return Arrays.stream(CardinalDirection.values()).collect(Collectors.toMap(k -> k, v -> point.move(v)));
        }
    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Point {

        int x;
        int y;

        public Point move(final CardinalDirection direction) {
            return new Point(x + direction.getMovementDelta()[0], y + direction.getMovementDelta()[1]);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    @Getter
    @AllArgsConstructor
    private enum CardinalDirection {

        NORTH(false, new int[] { 0, -1 }),
        NORTHEAST(true, new int[] { 1, -1 }),
        EAST(false, new int[] { 1, 0 }),
        SOUTHEAST(true, new int[] { 1, 1 }),
        SOUTH(false, new int[] { 0, 1 }),
        SOUTHWEST(true, new int[] { -1, 1 }),
        WEST(false, new int[] { -1, 0 }),
        NORTHWEST(true, new int[] { -1, -1 });

        private boolean intercardinal;
        private int[] movementDelta;

        public static Set<CardinalDirection> northern() {
            return Arrays.stream(values()).filter(x -> x.getMovementDelta()[1] < 0).collect(Collectors.toSet());
        }

        public static Set<CardinalDirection> eastern() {
            return Arrays.stream(values()).filter(x -> x.getMovementDelta()[0] > 0).collect(Collectors.toSet());
        }

        public static Set<CardinalDirection> southern() {
            return Arrays.stream(values()).filter(x -> x.getMovementDelta()[1] > 0).collect(Collectors.toSet());
        }

        public static Set<CardinalDirection> western() {
            return Arrays.stream(values()).filter(x -> x.getMovementDelta()[0] < 0).collect(Collectors.toSet());
        }
    }
}