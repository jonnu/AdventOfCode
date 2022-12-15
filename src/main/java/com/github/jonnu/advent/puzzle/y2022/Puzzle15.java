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
    private static final BiFunction<Coordinate, Coordinate, Integer> MANHATTAN_DISTANCE = (current, next) ->
            Math.abs(current.getX() - next.getX()) + Math.abs(current.getY() - next.getY());

    private final Terrain terrain = new Terrain();
    private final ResourceReader resourceReader;

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

        int row = 2_000_000;
        ArrayList<int[]> e = new ArrayList<>();
        sensors.forEach(s -> {

            if (Math.abs(row - s.getPosition().getY()) <= s.getPosition().getDistance()) {
                int[] b = new int[] {
                        s.getPosition().getX() - (s.getPosition().getDistance() - Math.abs(row - s.getPosition().getY())),
                        s.getPosition().getX() + (s.getPosition().getDistance() - Math.abs(row - s.getPosition().getY())),
                };
                e.add(b);
            }

        });


        int total = e.stream().map(a -> a[1] - a[0] + 1).reduce(0, Integer::sum);
        total -= sensors.stream().map(x -> x.getClosestBeacon().getPosition().getY()).filter(i -> i == row).count();

        System.out.println("Total: " + total);

        //sensors.forEach(System.out::println);

        //terrain.floodFill();
        //terrain.render();

        //long beaconlessPositions = terrain.getEmptyCellsInRow(10);
        //long beaconlessPositions = terrain.getEmptyCellsInRow(2_000_000);
        long beaconlessPositions = 0L;
        System.out.println("Positions that cannot contain a beacon: " + beaconlessPositions);
    }

    private List<int[]> overlaps(int[] segs) {

        for (int i = 0; i < segs.length; i++) {
            //if (segs[0])
        }


    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Terrain {

        private static final String EMPTY = "#";
        private static final String UNKNOWN = ".";

        private final Set<Sensor> sensorLocations = new HashSet<>();
        private final TreeMap<Integer, TreeMap<Integer, String>> terrain = new TreeMap<>();

        // profiler.
        private int ay;// = Integer.MAX_VALUE;
        private int by;// = Integer.MIN_VALUE;
        private int ax;// = Integer.MAX_VALUE;
        private int bx;// = Integer.MIN_VALUE;

        public void render() {

            for (int y = ay; y <= by; y++) {
                for (int x = ax; x <= bx; x++) {
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
            ax = terrain.values().stream().mapToInt(TreeMap::firstKey).min().orElse(0);
            bx = terrain.values().stream().mapToInt(TreeMap::lastKey).max().orElse(0);

            //System.out.println("New dims: (" + ax + ", " + ay + ") to (" + bx + ", " + by + ")");
        }

        public long getEmptyCellsInRow(final int row) {
            System.out.println(terrain.getOrDefault(row, new TreeMap<>())
                    .values());
            return terrain.getOrDefault(row, new TreeMap<>())
                    .values()
                    .stream()
                    .filter(cell -> cell.equals(EMPTY))
                    .count();
        }

        private void addTerrainObject(final int x, final int y, final String object) {
            terrain.computeIfAbsent(y, l -> new TreeMap<>());
            terrain.get(y).put(x, object);
        }

        private void addTerrainObject(final int x, final int y, final TerrainObject object) {
            addTerrainObject(x, y, object.getRepresentation());
        }

        private String getTerrainAt(final int x, final int y) {
            return terrain.getOrDefault(y, new TreeMap<>()).getOrDefault(x, UNKNOWN);
        }

        private boolean isWithinTerrainBoundary(final int x, final int y) {
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
                    Integer.parseInt(matcher.group(SENSOR_X)),
                    Integer.parseInt(matcher.group(SENSOR_Y))
            );

            final Coordinate beaconCoordinate = new Coordinate(
                    Integer.parseInt(matcher.group(BEACON_X)),
                    Integer.parseInt(matcher.group(BEACON_Y))
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

        private final int x;
        private final int y;
        @EqualsAndHashCode.Exclude private int distance;
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
            return "(" + x + "," + y + ")";
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
