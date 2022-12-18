package com.github.jonnu.advent.puzzle.y2022;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
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
    private static final BiFunction<Point, Point, Long> POINT_MANHATTAN_DISTANCE = (current, next) ->
            Math.abs(current.getX() - next.getX()) + Math.abs(current.getY() - next.getY());

    private final Terrain terrain = new Terrain();
    private final ResourceReader resourceReader;

    @Value
    @Builder
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Point {
        @Builder.Default long x = 0;
        @Builder.Default long y = 0;

        @Override
        public String toString() {
            return String.format("(x: %d, y: %d)", x, y);
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
            return determinant(deltaX, deltaY) != 0;
        }

        public static Point intersection3(final Line a, final Line b) {

            long p0_x = a.getA().getX();
            long p0_y = a.getA().getY();
//            long p1_x = a.getB().getX();
//            long p1_y = a.getB().getY();
            long p2_x = b.getA().getX();
            long p2_y = b.getA().getY();

            long s1_x = a.getB().getX() - a.getA().getX();
            long s2_x = b.getB().getX() - b.getA().getX();
            long s1_y = a.getB().getY() - a.getA().getY();
            long s2_y = b.getB().getY() - b.getA().getY();

            long s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
            long t = ( s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

            if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
                return new Point(p0_x + (t * s1_x), p0_y + (t * s1_y));
            }

            throw new RuntimeException("No intersection");
        }

        public static Point intersection(final Line a, final Line b) {
            long a1 = a.getB().getY() - a.getA().getY();
            long b1 = a.getA().getX() - a.getB().getX();
            long c1 = a1 * (a.getA().getX()) + b1 * (a.getA().getY());

            long a2 = b.getB().getY() - b.getA().getY();
            long b2 = b.getA().getX() - b.getB().getX();
            long c2 = a2 * (b.getA().getX()) + b2 * (b.getA().getY());

            final long determinant = determinant(new Point(a1, a2), new Point(b1, b2));
            System.out.println("Using determinant: " + determinant);
            if (determinant == 0) {
                throw new RuntimeException("Lines do not intersect");
            }

            return new Point((b2 * c1 - b1 * c2) / determinant, (a1 * c2 - a2 * c1) / determinant);
        }

        public static Point intersection2(final Line a, final Line b) {

            Point l = new Point(a.getA().getX() - a.getB().getX(), a.getA().getY() - a.getB().getY());
            Point r = new Point(b.getA().getX() - b.getB().getX(), b.getA().getY() - b.getB().getY());

            long d0 = determinant(l, r);
            if (d0 == 0) {
                throw new RuntimeException("not intersecting");
            }

            long d1 = determinant(a.getA(), a.getB());
            long d2 = determinant(b.getA(), b.getB());
            long x = determinant(new Point(d1, a.getA().getX() - a.getB().getX()),
                                 new Point(d2, b.getA().getX() - b.getB().getX())) / d0;
            long y = determinant(new Point(d1, a.getA().getY() - a.getB().getY()),
                                 new Point(d2, b.getA().getY() - b.getB().getY())) / d0;

            return new Point(x, y);
        }

        private static long determinant(final Point a, final Point b) {
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

        //List<Sensor> sensors1 = List.of(Sensor.builder().build());

        //int level = 10;
//        for (Sensor sensor : sensors) {
//
//            if (!(sensor.getPoint().getY() - sensor.getDistance() <= level &&
//                    level <= sensor.getPoint().getY() + sensor.getDistance())) {
//                //System.out.println("Skipping sensor at " + sensor.getPosition() + " (distance: " + sensor.getPosition().getDistance() + "): Too far away from " + level);
//                continue;
//            }
//
//            long[] intersects = sensor.onLev(level);
//            //System.out.println("Sensor at " + sensor.getPosition() + " (distance: " + sensor.getPosition().getDistance() + "): Intersects at: " + intersects[0] + ", " + intersects[1]);
//            imposs.addAll(LongStream.rangeClosed(intersects[0], intersects[1]).boxed().collect(Collectors.toSet()));
//        }

//        my position: (1003362, 1946094) manhattan distance: 1586508
//        Left Point: (-583146, 1946094); Right Point: (2589870, 1946094); Apex Point: (1003362, 3532602)
//        Left Line: Puzzle15.Line(a=(-583146, 1946094), b=(1003362, 3532602))
//        Right Line: Puzzle15.Line(a=(1003362, 3532602), b=(2589870, 1946094))
//        Level Line: Puzzle15.Line(a=(-583147, 2000000), b=(2589871, 2000000))
//        Left intersection: (-529240, -1664417)
//        Right intersection: (-1128453, -1664417)

        int level = 2_000_000;
        Set<Integer> allXs = sensors.stream()
                .map(x -> x.atRow(level))
                .map(Collection::stream)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(Collectors.toSet());

        Set<Integer> beacons = sensors.stream()
                .map(Sensor::getBeacon)
                .filter(position -> position.getY() == level)
                .map(Point::getX)
                .mapToInt(Long::intValue)
                .boxed()
                .collect(Collectors.toSet());

        allXs.removeAll(beacons);
        System.out.println("Positions at y: " + level + " that cannot contain a beacon: " + allXs.size());
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

        Point point;
        Point beacon;
        long distance;

        public Set<Integer> atRow(int row) {
            long reach = distance - Math.abs(row - point.getY());
            if (reach <= 0) {
                return Collections.emptySet();
            }
            long len = 2 *  (reach + 1) - 1;
            return IntStream.range((int) (point.getX() - reach), (int) (point.getX() - reach + len)).boxed().collect(Collectors.toSet());
        }

        public long[] onLev(final long level) {

            //long distance = position.getDistance();
            System.out.println("my position: " + point + " manhattan distance: " + distance);

            // make a triangle based on this point.

            Point apex = new Point(point.getX(), level < point.getY() ? point.getY() - distance : point.getY() + distance);
            Point left = new Point(point.getX() - distance, point.getY());
            Point right = new Point(point.getX() + distance, point.getY());

//            Point pointy = new Point(position.getX(), level < position.getY() ? position.getY() - position.getDistance() : position.getY() + position.getDistance());
//            Point left = new Point(position.getX() - position.getDistance(), position.getY());
//            Point right = new Point(position.getX() + position.getDistance(), position.getY());


            assert left.getX() <= apex.getX() && apex.getX() <= right.getX();

            Line leftLine = new Line(left, apex);
            Line rightLine = new Line(apex, right);
            Line levelLine = new Line(new Point(left.getX() - 1, level), new Point(right.getX() + 1, level));

            System.out.println("Left : " + left);
            System.out.println("Right: " + right);
            System.out.println("Apex : " + apex);
            System.out.printf("%n");

            System.out.println("Left Line: " + leftLine);
            System.out.println("Right Line: " + rightLine);
            System.out.println("Level Line: " + levelLine);

            Point leftIntersection = Line.intersection(leftLine, levelLine);
            Point rightIntersection = Line.intersection(rightLine, levelLine);
            Point leftIntersection2 = Line.intersection2(leftLine, levelLine);
            Point rightIntersection2 = Line.intersection2(rightLine, levelLine);
            Point leftIntersection3 = Line.intersection3(leftLine, levelLine);
            Point rightIntersection3 = Line.intersection3(rightLine, levelLine);

            System.out.println("Left intersection: " + leftIntersection + " - " + leftIntersection2 + " - " + leftIntersection3);
            System.out.println("Right intersection: " + rightIntersection + " - " + rightIntersection2 + " - " + rightIntersection3);
            System.out.println("");

//            System.out.println("Point: " + pointy);
//            System.out.println("Left: " + left);
//            System.out.println("Right: " + right);
//            System.out.println("Left Inter: " + leftIntersection);
//            System.out.println("Right Inter: " + rightIntersection);

            return new long[] { leftIntersection2.getX(), rightIntersection2.getX() };
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

            Point sp = Point.builder()
                    .x(Integer.parseInt(matcher.group(SENSOR_X)))
                    .y(Integer.parseInt(matcher.group(SENSOR_Y)))
                    .build();
            Point bp = Point.builder()
                    .x(Integer.parseInt(matcher.group(BEACON_X)))
                    .y(Integer.parseInt(matcher.group(BEACON_Y)))
                    .build();

            return Sensor.builder()
                    .position(sensorCoordinate)
                    .closestBeacon(Beacon.builder()
                            .position(beaconCoordinate)
                            .build())
                    .point(sp)
                    .beacon(bp)
                    .distance(POINT_MANHATTAN_DISTANCE.apply(sp, bp))
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
