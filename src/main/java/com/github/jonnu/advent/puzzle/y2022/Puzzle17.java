package com.github.jonnu.advent.puzzle.y2022;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle17 implements Puzzle {

    private static final int ROCKS_2_DROP = 2800;
    private static final int SATISFYING_DROPPED_ROCKS = 2022;
    private static final long ELEPHANT_SATISFYING_DROPPED_ROCKS = 1_000_000_000_000L;

    private static final int CHAMBER_HEIGHT = 4;

    private final ResourceReader resourceReader;

    @Getter
    @ToString
    @AllArgsConstructor
    private static class Point implements Comparable<Point> {
        private final int x;
        private final int y;

        public Point right() {
            return new Point(x + 1, y);
        }

        public Point left() {
            return new Point(x - 1, y);
        }

        public Point down() {
            return new Point(x, y - 1);
        }

        @Override
        public int compareTo(Point o) {
            int xy = Integer.compare(o.getY(), getY());
            if (xy != 0) {
                return xy;
            }
            return Integer.compare(getX(), o.getX());
        }
    }

    @Value
    @Builder
    @ToString
    @AllArgsConstructor
    private static class CacheObject {
        long counter;
        long height;
    }

    @Data
    @AllArgsConstructor
    private static class Cell {

        private final Point point;
        private Rock rock;

        public void empty() {
            this.rock = null;
            //System.out.println("Setting " + point + " to EMPTY");
        }

        public boolean containsRock() {
            return containsRock(null);
        }

        public boolean containsRock(final Rock ignore) {
            return rock != null && (ignore == null || !ignore.equals(rock));
        }

        public static Cell empty(int x, int y) {
            return new Cell(new Point(x, y), null);
        }

        @Override
        public String toString() {
            return containsRock(null) ? (rock.isFalling() ? "@" : "#") : ".";
        }
    }

    @Getter
    private static class Chamber {

        private static final int ROCK_SPAWN_DISTANCE_X = 2;
        private static final int ROCK_SPAWN_DISTANCE_Y = 3;

        private static final String WALL = "|";
        private static final String FLOOR = "-";
        private static final String CORNER = "+";
        private static final String ROCK_FALLING = "@";
        private static final String ROCK_RESTING = "####";

        private final ArrayList<ArrayList<Cell>> chamber;
        private final CountableIterator<Rock.Type> rockTypeIterator;
        private final CountableIterator<Jet> airJetPattern;
        @Getter private final Map<String, List<CacheObject>> cache = new HashMap<>();

        private int width = 7;
        private long height;
        private long highestRock;
        private long fallenRocks;
        private Rock current;

        private boolean active;
        private final List<Long> heightCache = new ArrayList<>();

        //Chamber(final Iterator<Rock.Type> rockTypeIterator, final Iterator<Jet> airJetPattern) {
        Chamber(final CountableIterator<Rock.Type> rockTypeIterator, final CountableIterator<Jet> airJetPattern) {

            this.airJetPattern = airJetPattern;
            this.rockTypeIterator = rockTypeIterator;
            this.height = CHAMBER_HEIGHT;
            this.highestRock = 0;//ROCK_SPAWN_DISTANCE_Y;
            this.fallenRocks = 0;

            // skip
            //rockTypeIterator.next();

            //System.out.printf("Init...%n");
            chamber = new ArrayList<>();
            for (int h = 0; h < height; h++) {
                final ArrayList<Cell> row = new ArrayList<>(width);
                for (int w = 0; w < width; w++) {
                    row.add(Cell.empty(w, h));
                }
                chamber.add(row);
            }

            active = true;
            current = spawnRock();
            //System.out.println("Complete.");
        }

        private Rock spawnRock() {

            final Rock rock = Rock.builder()
                    .type(rockTypeIterator.next())
                    .falling(true)
                    .build();
            //System.out.println("Spawning rock: " + rock.getType().name());

            // append to cells.
            int[][] shape = rock.getType().getShape();

            // grow chamber.
            long n = ROCK_SPAWN_DISTANCE_Y + highestRock + shape.length - 1;
            //System.out.println("Growing chamber to accommodate (" + (chamber.size() - 1) + " --> " + n + ")");
            if (chamber.size() - 1 < n) {
                for (int m = chamber.size(); m <= n; m++) {
                    ArrayList<Cell> row = new ArrayList<>();
                    for (int w = 0; w < width; w++) {
                        row.add(Cell.empty(w, m));
                    }
                    chamber.add(row);//new ArrayList<>());
                }
            }

            Set<Point> locations = new HashSet<>();
            //for (int y = 0, z = n; y < shape.length; y++, z--){
            for (int y = 0, z = (int) n; y < shape.length; y++, z--){
                for (int x = 0; x < shape[y].length; x++) {

                    if (shape[y][x] == 0) {
                        continue;
                    }

                    Cell cell = chamber.get(z).get(ROCK_SPAWN_DISTANCE_X + x);
                    cell.setRock(rock);
                    locations.add(cell.getPoint());
                }
            }

            rock.setLocated(locations);

//            rock.getLocated()
//                    .stream()
//                    .sorted()
//                    .forEach(l -> System.out.println("Setting " + l + " to " + rock.getType().name()));

            return rock;
        }

        public long getRockTowerHeight() {
            return highestRock;
        }

        private boolean containsRock(final Point point) {
            return containsRock(point, null);
        }

        private boolean containsRock(final Point point, final Rock ignore) {
            boolean a = chamber.get(point.getY()).get(point.getX()).containsRock(ignore);
            //System.out.println(point + " does " + (a ? "" : "not ") + "contain rock (ignore: " + ignore + "; cell: " + chamber.get(point.getY()).get(point.getX()).getRock() + ")");
            return a;
        }

        private boolean isWithinChamber(final Point point) {
            if (point.getY() < 0) {
                return false;
            }

            boolean a = point.getX() >= 0 && point.getX() < chamber.get(point.getY()).size();
            //System.out.println(point + " is " + (a ? "" : "not ") + "within chamber");
            return a;
        }

        private boolean arePointsFree(final Rock rock, final Set<Point> points) {
            return points.stream().allMatch(point -> isWithinChamber(point) && !containsRock(point, rock));
        }

        private boolean tryMoveRock(final Rock rock, final Jet jet) {

            Function<Point, Point> mutator;
            switch (jet) {
                case LEFT -> mutator = Point::left;
                case DOWN -> mutator = Point::down;
                case RIGHT -> mutator = Point::right;
                default -> throw new IllegalStateException("Unexpected value: " + jet);
            }

            // Can i move each point right one?
            Set<Point> from = rock.getLocated();
            Set<Point> to = from.stream().map(mutator).collect(Collectors.toSet());
            boolean canMove = arePointsFree(rock, to);

            //System.out.println("Current: " + from);
            //System.out.println("To: " + to);

            if (canMove) {
                //System.out.println("Rock " + rock.getType().name() + " is " + (jet.equals(Jet.DOWN) ? "FALLING" : "MOVING " + jet.name()));
                from.forEach(p -> chamber.get(p.getY()).get(p.getX()).empty());//.set(p.getX(), Cell.empty(p.getX(), p.getY())));
                rock.setLocated(to);
                to.forEach(p -> chamber.get(p.getY()).get(p.getX()).setRock(rock));
            } else {
                if (jet.equals(Jet.DOWN)) {
                    //System.out.println("Rock " + current.getType().name() + " CANNOT FALL; Solidifying...");
                    rock.setFalling(false);

                    highestRock = chamber.stream()
                            .flatMap(Collection::stream)
                            .filter(Cell::containsRock)
                            .map(Cell::getRock)
                            .filter(r -> !r.isFalling())
                            .map(Rock::getLocated)
                            .mapToInt(l -> l.stream()
                                    .mapToInt(Point::getY)
                                    .map(h -> h + 1)
                                    .max()
                                    .orElse(Integer.MIN_VALUE))
                            .max()
                            .orElse(0);

                    fallenRocks++;
                    heightCache.add(highestRock);
                    //System.out.println("Tower height is now: " + highestRock + " after " + fallenRocks++ + " solidified rocks.");

                    current = null;
                }
            }

            return true;
        }

        private long[] keys() {

            Map<Integer, Integer> heightMap = chamber.stream()
                    .flatMap(Collection::stream)
                    .filter(Cell::containsRock)
                    .map(Cell::getRock)
                    .filter(r -> !r.isFalling())
                    .map(Rock::getLocated)
                    .flatMap(Set::stream)
                    .collect(Collectors.groupingBy(Point::getX, Collectors.maxBy(Comparator.comparingLong(Point::getY))))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, value -> value.getValue()
                            .map(Point::getY)
                            .orElse(0)));

            return heightMap.values().stream().mapToLong(x -> getRockTowerHeight() - x - 1).toArray();
        }

        public boolean step() {

            boolean shouldCache = false;
            if (current == null) {
                current = spawnRock();
                shouldCache = true;
            }

            // first, consume a jet.
            // if we run out of jets, halt.
            Jet jet = airJetPattern.next();

            if (shouldCache) {
                String cacheKey = String.format("%s.%s.%s",
                        airJetPattern.getIndex(),
                        rockTypeIterator.getIndex(),
                        Arrays.stream(keys()).boxed().map(String::valueOf).collect(Collectors.joining(",")));
//                        jet.getSymbol(),
//                        current.getType().name());
                cache.putIfAbsent(cacheKey, new ArrayList<>());
                cache.get(cacheKey).add(CacheObject.builder()
                        .counter(getFallenRocks())
                        .height(getRockTowerHeight())
                        .build());
                System.out.println("Cached: " + cacheKey + " = " + cache.get(cacheKey));
            }

            // jet
            tryMoveRock(current, jet);

            // fall
            tryMoveRock(current, Jet.DOWN);

            //SATISFYING_DROPPED_ROCKS
            if (fallenRocks == ROCKS_2_DROP) {
                active = false;
            }

            return active;
        }

        public void render() {
            int yw = (int) Math.floor(Math.log10(chamber.size())) + 1;
            System.out.printf("%n");
            for (int i = chamber.size() - 1; i >= 0; i--) {
                for (int j = 0; j < chamber.get(i).size(); j++) {

                    // left wall
                    if (j == 0) {
                        System.out.printf("%" + yw + "d %s", i, WALL);
                    }

                    System.out.print(chamber.get(i).get(j));

                    // right wall
                    if (j == chamber.get(i).size() - 1) {
                        System.out.printf(WALL + "%n");
                    }
                }

                // Floor
                if (i == 0) {
                    System.out.printf(" ".repeat(yw + 1) + CORNER + FLOOR.repeat(width) + CORNER + "%n");
                    System.out.printf(" ".repeat(yw + 2) + IntStream.range(0, chamber.get(i).size()).boxed().map(String::valueOf).collect(Collectors.joining()) + " %n%n");
                }
            }
        }
    }

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle17.txt")) {

            final List<Jet> jets = reader.readLine()
                    .chars()
                    .boxed()
                    .map(Jet::fromSymbol)
                    .collect(Collectors.toList());

            //final Chamber chamber = new Chamber(Iterables.cycle(Rock.ALL).iterator(), Iterables.cycle(jets).iterator());
            final Chamber chamber = new Chamber(countingCyclicIterator(Rock.ALL), countingCyclicIterator(jets));


            while (chamber.step()) {
                //chamber.render();
            }

            //chamber.render();

            // (cycle_start, highest_start), cycle_size, height_per_cycle
            List<CacheObject> cache = chamber.getCache().values().stream().filter(e -> e.size() > 1).skip(1).findFirst().orElseThrow(() -> new IllegalArgumentException("No cycle"));
            //chamber.getHeightCache().forEach(System.out::println);

            // (cycle_start, highest_start), cycle_size, height_per_cycle
            //return cycles[0], cycles[1][0] - cycles[0][0], cycles[1][1] - cycles[0][1]

            long cycleStart = cache.get(0).getCounter();
            long result = chamber.getHeightCache().get((int) cycleStart);

            long cycleSize = cache.get(1).getCounter() - cache.get(0).getCounter();
            long heightPerCycle = cache.get(1).getHeight() - cache.get(0).getHeight();

            long cycleNumber = Math.floorMod(ELEPHANT_SATISFYING_DROPPED_ROCKS - cycleStart, cycleSize);
            long rest = (ELEPHANT_SATISFYING_DROPPED_ROCKS - cycleStart) % cycleSize;

            result += cycleNumber * heightPerCycle + (chamber.getHeightCache().get((int) (cycleStart + rest)) - chamber.getHeightCache().get((int) cycleStart));

            System.out.println("p1: " + chamber.getHeightCache().get(SATISFYING_DROPPED_ROCKS - 1));//chamber.getRockTowerHeight());
            System.out.println("p2: " + result);
        }
    }

    interface CountableIterator<T> extends Iterator<T> {
        int getIndex();
    }

    public static <T extends Object> CountableIterator<T> countingCyclicIterator(final Iterable<T> iterable) {
        return new CountableIterator<T>() {

            int index = -1;
            Iterator<T> iterator = new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new NoSuchElementException();
                }
            };

            public int getIndex() {
                return index;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext() || iterable.iterator().hasNext();
            }

            @Override
            public T next() {
                if (!iterator.hasNext()) {
                    iterator = iterable.iterator();
                    index = -1;
                    if (!iterator.hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                T next = iterator.next();
                index++;
                return next;
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Rock {

        @Getter
        @AllArgsConstructor
        private enum Type {

            H_BAR(new int[][]{
                    { 1, 1, 1, 1 }
            }),
            CROSS(new int[][]{
                    { 0, 1, 0, },
                    { 1, 1, 1, },
                    { 0, 1, 0, }
            }),
            INVERSE_L(new int[][]{
                    { 0, 0, 1, },
                    { 0, 0, 1, },
                    { 1, 1, 1, }
            }),
            V_BAR(new int[][]{
                    { 1, },
                    { 1, },
                    { 1, },
                    { 1, },
            }),
            SQUARE(new int[][]{
                    { 1, 1, },
                    { 1, 1, },
            }),
            NONE(new int[][]{
            });

            private final int[][] shape;
        }

        private final Type type;
        @Setter private boolean falling;
        @Setter private Set<Point> located;

        public static final List<Type> ALL = Arrays.stream(Type.values())
                .filter(x -> !x.equals(Type.NONE))
                .collect(Collectors.toList());
    }

    @Getter
    @AllArgsConstructor
    private enum Jet {

        LEFT('<', -1, 0),
        RIGHT('>', 1, 0),
        DOWN('v', 0, -1);

        private final char symbol;
        private final int deltaX;
        private final int deltaY;

        public static Jet fromSymbol(final int symbol) {
            return fromSymbol((char) symbol);
        }

        public static Jet fromSymbol(final char symbol) {
            return Arrays.stream(values())
                    .filter(jet -> symbol == jet.getSymbol())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown jet direction: " + symbol));
        }
    }



}