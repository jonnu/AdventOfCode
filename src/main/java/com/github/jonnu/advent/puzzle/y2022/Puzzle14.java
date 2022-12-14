package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle14 implements Puzzle {

    private static final int RENDER_STEP_SLEEP_MS = 0;
    private static final String ROCK_PATH_SEPARATOR = " -> ";
    private static final String COORDINATE_SEPARATOR = ",";
    private static final Coordinate SAND_ORIGIN = new Coordinate(500, 0);

    private final ResourceReader resourceReader;
    private final Cave cave = new Cave(SAND_ORIGIN);

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle14.txt")) {

            String line = reader.readLine();
            while (line != null) {
                cave.addRockPath(Arrays.stream(line.split(ROCK_PATH_SEPARATOR))
                        .map(raw -> raw.split(COORDINATE_SEPARATOR))
                        .map(pair -> new Coordinate(Integer.parseInt(pair[0]), Integer.parseInt(pair[1])))
                        .collect(Collectors.toList()));
                line = reader.readLine();
            }
        }

        while (cave.simulate(true)) {
            if (RENDER_STEP_SLEEP_MS > 0) {
                Thread.sleep(RENDER_STEP_SLEEP_MS);
                System.out.print("\033[H\033[2J");
                System.out.flush();
                cave.render();
            }
        }

        System.out.println("Units of settled sand: " + cave.getSettledSand());
    }

    @Getter
    private static class Cave {

        private final Coordinate sandOrigin;
        private final TreeMap<Integer, TreeMap<Integer, String>> cells;
        private final Deque<Coordinate> fallingSand;

        private final String AIR = ".";
        private final String ROCK = "#";
        private final String SAND_ORIGIN = "+";
        private final String SAND_SETTLED = "o";

        private int settledSand = 0;
        private int depthOfFloor = 0;

        Cave(final Coordinate sandOrigin) {
            this.sandOrigin = sandOrigin;
            this.cells = new TreeMap<>();
            this.fallingSand = new ArrayDeque<>();
            addCell(sandOrigin, SAND_ORIGIN);
        }

        public void addRockPath(final List<Coordinate> path) {
            for (int i = 0, j = 1; j < path.size(); i++, j++) {
                final RockPathDirection direction = path.get(i).getX() == path.get(j).getX() ?
                        RockPathDirection.VERTICAL : RockPathDirection.HORIZONTAL;
                // VERTICAL
                if (direction.equals(RockPathDirection.VERTICAL)) {
                    int from = Math.min(path.get(i).getY(), path.get(j).getY());
                    int to = Math.max(path.get(i).getY(), path.get(j).getY());
                    for (int k = from; k <= to; k++) {
                        addCell(path.get(i).getX(), k, ROCK);
                    }

                    depthOfFloor = Math.max(depthOfFloor, to + 2);
                }
                // HORIZONTAL
                else {
                    int from = Math.min(path.get(i).getX(), path.get(j).getX());
                    int to = Math.max(path.get(i).getX(), path.get(j).getX());
                    for (int k = from; k <= to; k++) {
                        addCell(k, path.get(i).getY(), ROCK);
                    }
                }
            }
        }

        public boolean simulate() {
            return simulate(false);
        }

        public boolean simulate(boolean withCaveFloor) {
            return createSand() && moveSand(withCaveFloor);
        }

        public void render() {

            int ay = getCells().firstKey();
            int by = getCells().lastKey();
            int ax = getCells().values().stream().mapToInt(TreeMap::firstKey).min().orElse(0);
            int bx = getCells().values().stream().mapToInt(TreeMap::lastKey).max().orElse(0);

            for (int y = ay; y <= by; y++) {
                for (int x = ax; x <= bx; x++) {
                    System.out.printf("%s", getCell(x, y));
                }
                System.out.printf("%n");
            }
        }

        private String getCell(final int x, final int y) {
            return cells.getOrDefault(y, new TreeMap<>()).getOrDefault(x, AIR);
        }

        private void addCell(final Coordinate coordinate, final String character) {
            addCell(coordinate.getX(), coordinate.getY(), character);
        }

        private void addCell(final int x, final int y, final String character) {
            cells.computeIfAbsent(y, l -> new TreeMap<>());
            cells.get(y).put(x, character);
        }

        private boolean isCellOccupied(final Coordinate coordinate) {
            return isCellOccupied(coordinate.getX(), coordinate.getY());
        }

        private boolean isCellOccupied(final int x, final int y) {
            return Optional.ofNullable(cells.getOrDefault(y, new TreeMap<>()).getOrDefault(x, null))
                    .map(character -> character.equals(ROCK) || character.equals(SAND_SETTLED))
                    .orElse(false);
        }

        private boolean isCellBeneathCave(final Coordinate coordinate) {
            return coordinate.getY() > getCells().lastKey();
        }

        private boolean isCellHittingCaveFloor(final Coordinate coordinate) {
            return coordinate.below().getY() == depthOfFloor;
        }

        public boolean moveSand(final boolean withCaveFloor) {
            while (!fallingSand.isEmpty()) {
                final Coordinate current = fallingSand.poll();

                // Simulation with floor. Sand is hitting infinite floor. Settle it.
                if (withCaveFloor && isCellHittingCaveFloor(current)) {
                    settleSandInCell(current);
                    continue;
                }

                // Sand is falling forever. Halt simulation.
                if (isCellBeneathCave(current)) {
                    return false;
                }

                if (!isCellOccupied(current.below())) {
                    fallingSand.push(current.below());
                    continue;
                }

                if (!isCellOccupied(current.belowLeft())) {
                    fallingSand.push(current.belowLeft());
                    continue;
                }

                if (!isCellOccupied(current.belowRight())) {
                    fallingSand.push(current.belowRight());
                    continue;
                }

                settleSandInCell(current);
            }

            return true;
        }

        private void settleSandInCell(final Coordinate coordinate) {
            addCell(coordinate, SAND_SETTLED);
            settledSand++;
        }

        public boolean createSand() {
            if (isCellOccupied(sandOrigin)) {
                return false;
            }

            fallingSand.push(sandOrigin);
            return true;
        }
    }

    @Value
    @AllArgsConstructor
    private static class Coordinate {

        int x;
        int y;

        public Coordinate below() {
            return new Coordinate(x, y + 1);
        }

        public Coordinate belowLeft() {
            return new Coordinate(x - 1, y + 1);
        }

        public Coordinate belowRight() {
            return new Coordinate(x + 1, y + 1);
        }
    }

    private enum RockPathDirection {
        VERTICAL,
        HORIZONTAL
    }

}
