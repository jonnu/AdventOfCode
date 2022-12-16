package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle15 implements Puzzle {

    private static final Pattern PATTERN = Pattern.compile("^.*?x=(?<sx>-?\\d+),\\sy=(?<sy>-?\\d+):.*?x=(?<bx>-?\\d+),\\sy=(?<by>-?\\d+)$");
    private static final BiFunction<Coordinate, Coordinate, Long> MANHATTAN_DISTANCE = (current, next) ->
            Math.abs(current.getX() - next.getX()) + Math.abs(current.getY() - next.getY());

    private final Terrain terrain = new Terrain();
    private final ResourceReader resourceReader;

    @Value
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Point {
        long x;
        long y;

        @Override
        public String toString() {
            return String.format("(%d, %d)", x, y);
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class Line {

        Point a;
        Point b;

        public boolean intersects(final Line other) {
            return intersects(this, other);
        }

        public Point intersection(final Line other) {
            return intersection(this, other);
        }

        // statics
        public static boolean boundsIntersects(final Line a, final Line b) {
            final Point atl = new Point(Math.min(a.getA().getX(), a.getB().getX()), Math.min(a.getA().getY(), a.getB().getY()));
            final Point abr = new Point(Math.max(a.getA().getX(), a.getB().getX()), Math.max(a.getA().getY(), a.getB().getY()));
            final Point btl = new Point(Math.min(b.getA().getX(), b.getB().getX()), Math.min(b.getA().getY(), b.getB().getY()));
            final Point bbr = new Point(Math.max(b.getA().getX(), a.getB().getX()), Math.max(b.getA().getY(), b.getB().getY()));

            return atl.getX() <= bbr.getX() &&
                    abr.getX() >= btl.getX() &&
                    atl.getY() <= bbr.getY() &&
                    abr.getY() >= btl.getY();
        }

        public static boolean intersects(final Line a, final Line b) {
            if (!boundsIntersects(a, b)) {
                return false;
            }

            final Point deltaX = new Point(a.getA().getX() - a.getB().getX(), b.getA().getX() - b.getB().getX());
            final Point deltaY = new Point(a.getA().getY() - a.getB().getY(), b.getA().getY() - b.getB().getY());
            return crossProduct(deltaX, deltaY) != 0;
        }

        public static Point intersection(final Line a, final Line b) {
            long a1 = a.getB().getY() - a.getA().getY();
            long b1 = a.getA().getX() - a.getB().getX();
            long c1 = a1 * (a.getA().getX()) + b1 * (a.getA().getY());

            long a2 = b.getB().getY() - b.getA().getY();
            long b2 = b.getA().getX() - b.getB().getX();
            long c2 = a2 * (b.getA().getX()) + b2 * (b.getA().getY());

            final long determinant = crossProduct(new Point(a1, a2), new Point(b1, b2));
            if (determinant == 0) {
                throw new RuntimeException("Lines do not intersect");
            }

            return new Point((b2 * c1 - b1 * c2) / determinant, (a1 * c2 - a2 * c1) / determinant);
        }

        private static long crossProduct(final Point a, final Point b) {
            return a.getX() * b.getY() - a.getY() * b.getX();
        }
    }


    @Override
    @SneakyThrows
    public void solve() {

        List<Sensor> sensors = new ArrayList<>();
        try (BufferedReader reader = resourceReader.read("y2022/puzzle15.txt")) {
            String line = reader.readLine();
            while (line != null) {
                final Matcher matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Unable to match input: " + line);
                }

                sensors.add(Sensor.createFromMatch(matcher));
                //terrain.addSensor(Sensor.createFromMatch(matcher));
                line = reader.readLine();
            }
        }

        //Line a = new Line(new Point(-10, 0), new Point(10, 0));
        //Line b = new Line(new Point(5, 10), new Point(5, -10));
        //System.out.println("Intersects: " + Line.intersects(a, b));
        //System.out.println("Intersection: " + Line.intersection(a, b));

        Set<Long> imposs = new HashSet<>();
        //List<Sensor> sensors1 = List.of(Sensor.builder().build());
        int level = 2_000_000;
        //int level = 10;
        for (Sensor sensor : sensors) {

            if (!(sensor.getPosition().getY() - sensor.getPosition().getDistance() <= level &&
                    level <= sensor.getPosition().getY() + sensor.getPosition().getDistance())) {
                System.out.println("Skipping sensor at " + sensor.getPosition() + " (distance: " + sensor.getPosition().getDistance() + "): Too far away from " + level);
                continue;
            }

            long[] intersects = sensor.onLev(level);
            System.out.println("Sensor at " + sensor.getPosition() + " (distance: " + sensor.getPosition().getDistance() + "): Intersects at: " + intersects[0] + ", " + intersects[1]);
            imposs.addAll(LongStream.rangeClosed(intersects[0], intersects[1]).boxed().collect(Collectors.toSet()));
        }

        Set<Long> beacons = sensors.stream().map(Sensor::getClosestBeacon).map(TerrainObject::getPosition).filter(position -> position.getY() == level).mapToLong(Coordinate::getX).boxed().collect(Collectors.toSet());
        imposs.removeAll(beacons);
        //System.out.println(imposs.stream().sorted().collect(Collectors.toList()));

//        sensors.forEach(s -> {
//
//            if (Math.abs(row - s.getPosition().getY()) <= s.getPosition().getDistance()) {
//                int[] b = new int[] {
//                        s.getPosition().getX() - (s.getPosition().getDistance() - Math.abs(row - s.getPosition().getY())),
//                        s.getPosition().getX() + (s.getPosition().getDistance() - Math.abs(row - s.getPosition().getY())),
//                };
//                e.add(b);
//            }
//
//        });
//
//
//        int total = e.stream().map(a -> a[1] - a[0] + 1).reduce(0, Integer::sum);
//        total -= sensors.stream().map(x -> x.getClosestBeacon().getPosition().getY()).filter(i -> i == row).count();
//
//        System.out.println("Total: " + total);

        //sensors.forEach(System.out::println);

        //terrain.floodFill();
        //terrain.render();

        //long beaconlessPositions = terrain.getEmptyCellsInRow(10);
        //long beaconlessPositions = terrain.getEmptyCellsInRow(2_000_000);
        long beaconlessPositions = imposs.size();
        System.out.println("Positions that cannot contain a beacon: " + beaconlessPositions);
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Terrain {

        private static final String EMPTY = "#";
        private static final String UNKNOWN = ".";

        private final Set<Sensor> sensorLocations = new HashSet<>();
        private final TreeMap<Long, TreeMap<Long, String>> terrain = new TreeMap<>();

        // profiler.
        private long ay;// = Integer.MAX_VALUE;
        private long by;// = Integer.MIN_VALUE;
        private long ax;// = Integer.MAX_VALUE;
        private long bx;// = Integer.MIN_VALUE;

        public void render() {

            for (long y = ay; y <= by; y++) {
                for (long x = ax; x <= bx; x++) {
                    System.out.printf("%s", getTerrainAt(x, y));
                }
                System.out.printf("%n");
            }

            System.out.printf("%n");
        }

        public void floodFill(final Coordinate start) {
            Deque<Coordinate> queue = new ArrayDeque<>(List.of(start));
            Set<Coordinate> processed = new HashSet<>();

            while (!queue.isEmpty()) {
                Coordinate current = queue.poll();
                String t = getTerrainAt(current.getX(), current.getY());
                if (t.equals(UNKNOWN)) {
                    addTerrainObject(current.getX(), current.getY(), EMPTY);
                }
                processed.add(current);

                if (current.getDistance() > 0) {
                    current.getAdjacentCoordinates()
                            .stream()
                            .filter(neighbour -> !processed.contains(neighbour))
                            .filter(neighbour -> isWithinTerrainBoundary(neighbour.getX(), neighbour.getY()))
                            //.peek(x -> System.out.println("current: " + x + "; distance: " + x.getDistance()))
                            .forEach(queue::add);
                }
            }
        }

        public void floodFill() {
            Deque<Sensor> queue = new ArrayDeque<>(sensorLocations);//.stream().limit(2).collect(Collectors.toList()));
            while (!queue.isEmpty()) {
                Sensor current = queue.poll();
                floodFill(current.getPosition());
            }
        }

        public void addSensor(final Sensor sensor) {
            sensorLocations.add(sensor);
            addTerrainObject(sensor.getPosition().getX(), sensor.getPosition().getY(), sensor);
            addTerrainObject(sensor.getClosestBeacon().getPosition().getX(), sensor.getClosestBeacon().getPosition().getY(), sensor.getClosestBeacon());

            ay = terrain.firstKey();
            by = terrain.lastKey();
            ax = terrain.values().stream().mapToLong(TreeMap::firstKey).min().orElse(0);
            bx = terrain.values().stream().mapToLong(TreeMap::lastKey).max().orElse(0);

            //System.out.println("New dims: (" + ax + ", " + ay + ") to (" + bx + ", " + by + ")");
        }

        public long getEmptyCellsInRow(final long row) {
            System.out.println(terrain.getOrDefault(row, new TreeMap<>())
                    .values());
            return terrain.getOrDefault(row, new TreeMap<>())
                    .values()
                    .stream()
                    .filter(cell -> cell.equals(EMPTY))
                    .count();
        }

        private void addTerrainObject(final long x, final long y, final String object) {
            terrain.computeIfAbsent(y, l -> new TreeMap<>());
            terrain.get(y).put(x, object);
        }

        private void addTerrainObject(final long x, final long y, final TerrainObject object) {
            addTerrainObject(x, y, object.getRepresentation());
        }

        private String getTerrainAt(final long x, final long y) {
            return terrain.getOrDefault(y, new TreeMap<>()).getOrDefault(x, UNKNOWN);
        }

        private boolean isWithinTerrainBoundary(final long x, final long y) {
            return x >= ax && x <= bx && y >= ay && y <= by;
        }
    }

    @Getter
    private static abstract class TerrainObject {
        Coordinate position;
        abstract public String getRepresentation();
    }

    @Value
    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class Sensor extends TerrainObject {

        private static final String BEACON_X = "bx";
        private static final String BEACON_Y = "by";
        private static final String SENSOR_X = "sx";
        private static final String SENSOR_Y = "sy";

        Coordinate position;
        TerrainObject closestBeacon;

        public long[] onLev(final long level) {

            System.out.println("my position: " + position + " distance: " + position.getDistance());

            Point pointy = new Point(position.getX(), level < position.getY() ? position.getY() - position.getDistance() : position.getY() + position.getDistance());
            Point left = new Point(position.getX() - position.getDistance(), position.getY());
            Point right = new Point(position.getX() + position.getDistance(), position.getY());

            System.out.println("Left: " + left + "; Right: " + right + "; Point: " + pointy);

            Line leftLine = new Line(left, pointy);
            Line rightLine = new Line(pointy, right);//, pointy);
            Line levelLine = new Line(new Point(position.getX() - position.getDistance(), level), new Point(position.getX() + position.getDistance(), level));

            Point leftIntersection = Line.intersection(leftLine, levelLine);
            Point rightIntersection = Line.intersection(rightLine, levelLine);

            System.out.println("Left intersection: " + leftIntersection);
            System.out.println("Right intersection: " + rightIntersection);

//            System.out.println("Point: " + pointy);
//            System.out.println("Left: " + left);
//            System.out.println("Right: " + right);
//            System.out.println("Left Inter: " + leftIntersection);
//            System.out.println("Right Inter: " + rightIntersection);

            return new long[] { leftIntersection.getX(), rightIntersection.getX() };
        }

        public static Sensor createFromMatch(final Matcher matcher) {

//            final Coordinate sensorCoordinate = Coordinate.builder()
//                    .x(Integer.parseInt(matcher.group(SENSOR_X)))
//                    .y(Integer.parseInt(matcher.group(SENSOR_Y)))
//                    .build();
//            final Coordinate beaconCoordinate = Coordinate.builder()
//                    .x(Integer.parseInt(matcher.group(BEACON_X)))
//                    .y(Integer.parseInt(matcher.group(BEACON_Y)))
//                    .build();
            final Coordinate sensorCoordinate = new Coordinate(
                    Long.parseLong(matcher.group(SENSOR_X)),
                    Long.parseLong(matcher.group(SENSOR_Y))
            );

            final Coordinate beaconCoordinate = new Coordinate(
                    Long.parseLong(matcher.group(BEACON_X)),
                    Long.parseLong(matcher.group(BEACON_Y))
            );

            sensorCoordinate.setDistance(MANHATTAN_DISTANCE.apply(sensorCoordinate, beaconCoordinate));

            return Sensor.builder()
                    .position(sensorCoordinate)
                    .closestBeacon(Beacon.builder()
                            .position(beaconCoordinate)
                            .build())
                    .build();
        }

        @Override
        public String getRepresentation() {
            return "S";
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class Beacon extends TerrainObject {

        Coordinate position;

        @Override
        public String getRepresentation() {
            return "B";
        }
    }

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor
    @RequiredArgsConstructor
    private static class Coordinate {

//        private final int x;
//        private final int y;
        private final long x;
        private final long y;
        @EqualsAndHashCode.Exclude private long distance;
//        @EqualsAndHashCode.Exclude private Coordinate[] adjacent;

//        Coordinate(int x, int y) {
//            this(x, y, 0, false);
//        }
//
//        Coordinate(int x, int y, int distance) {
//            this(x, y, distance, true);
//        }

//        public Coordinate(int x, int y, int distance) {
//            this.x = x;
//            this.y = y;
//            this.distance = distance;
//            this.adjacent = !precompute || distance == 0 ? new Coordinate[] {} : new Coordinate[] {
//                new Coordinate(x + 1, y, distance - 1, false),
//                new Coordinate(x - 1, y, distance - 1, false),
//                new Coordinate(x, y + 1, distance - 1, false),
//                new Coordinate(x, y - 1, distance - 1, false),
//            };
//        }

//        public void setDistance(int distance) {
//            this.distance = distance;
//            if (distance > 0) {
//                this.adjacent = new Coordinate[] {
//                        new Coordinate(x + 1, y, distance, true),
//                        new Coordinate(x - 1, y, distance, true),
//                        new Coordinate(x, y + 1, distance, true),
//                        new Coordinate(x, y - 1, distance, true),
//                };
//            }
//        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }

        public Set<Coordinate> getAdjacentCoordinates() {
            return Set.of(
                    new Coordinate(x + 1, y, distance - 1),
                    new Coordinate(x - 1, y, distance - 1),
                    new Coordinate(x, y + 1, distance - 1),
                    new Coordinate(x, y - 1, distance - 1));
            /*ImmutableSet.<Coordinate>builder()
                    .add(Coordinate.builder().distance(distance - 1).x(getX() + 1).y(getY()).build())
                    .add(Coordinate.builder().distance(distance - 1).x(getX() - 1).y(getY()).build())
                    .add(Coordinate.builder().distance(distance - 1).x(getX()).y(getY() + 1).build())
                    .add(Coordinate.builder().distance(distance - 1).x(getX()).y(getY() - 1).build())
                    .build();*/
        }
    }

}
