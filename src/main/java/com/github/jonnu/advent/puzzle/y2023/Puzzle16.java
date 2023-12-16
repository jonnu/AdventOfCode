package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle16 implements Puzzle {

    private final ResourceReader resourceReader;

    private final Map<Point, BeamMutator> grid = new HashMap<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle16.txt")) {

            int row = 0;
            String line = reader.readLine();
            while (line != null) {

                final int y = row;
                for (int x = 0; x < line.length(); x++) {
                    if (line.charAt(x) == '.') {
                        continue;
                    }
                    grid.put(new Point(x, y), BeamMutator.parse(line.charAt(x)));
                }

                line = reader.readLine();
                row++;
            }

            System.out.println("Energised: " + solve(Beam.at(-1, 0, Direction.EAST)));

            final int maxX = grid.keySet().stream().mapToInt(Point::getX).summaryStatistics().getMax();
            final int maxY = grid.keySet().stream().mapToInt(Point::getY).summaryStatistics().getMax();


            // east beams.
            Set<Beam> e = IntStream.range(0, maxY).mapToObj(y -> Beam.at(-1, y, Direction.EAST)).collect(Collectors.toSet());
            // west beams.
            Set<Beam> w = IntStream.range(0, maxY).mapToObj(y -> Beam.at(maxX + 1, y, Direction.WEST)).collect(Collectors.toSet());
            // south beams.
            Set<Beam> s = IntStream.range(0, maxX).mapToObj(x -> Beam.at(x, -1, Direction.SOUTH)).collect(Collectors.toSet());
            // north beams
            Set<Beam> n = IntStream.range(0, maxX).mapToObj(x -> Beam.at(x, maxY + 1, Direction.NORTH)).collect(Collectors.toSet());

            System.out.println("Max energised: " + Stream.of(e, w, s, n)
                    .flatMap(Collection::stream)
                    .mapToInt(this::solve)
                    .max()
                    .orElseThrow(() -> new IllegalStateException("No max")));
        }
    }

    private int solve(final Beam initial) {

        final Map<Point, Set<Beam>> energised = new HashMap<>();
        final Queue<Beam> beams = new ArrayDeque<>();
        beams.add(initial);

        final int maxX = grid.keySet().stream().mapToInt(Point::getX).summaryStatistics().getMax();
        final int maxY = grid.keySet().stream().mapToInt(Point::getY).summaryStatistics().getMax();

        while (!beams.isEmpty()) {

            final Beam beam = beams.poll();
            final Point position = beam.getPosition();

            if (grid.containsKey(position)) {
                switch (grid.get(position)) {
                    case MIRROR_BW -> {
                        switch (beam.getDirection()) {
                            case EAST:
                            case WEST:
                                beam.setDirection(beam.getDirection().rotate(Direction.Rotation.CLOCKWISE));
                                break;
                            case NORTH:
                            case SOUTH:
                                beam.setDirection(beam.getDirection().rotate(Direction.Rotation.ANTICLOCKWISE));
                                break;
                        }
                    }
                    case MIRROR_FW -> {
                        switch (beam.getDirection()) {
                            case NORTH:
                            case SOUTH:
                                beam.setDirection(beam.getDirection().rotate(Direction.Rotation.CLOCKWISE));
                                break;
                            case EAST:
                            case WEST:
                                beam.setDirection(beam.getDirection().rotate(Direction.Rotation.ANTICLOCKWISE));
                                break;
                        }
                    }
                    case SPLITTER_VERTICAL -> {
                        if (beam.getDirection().equals(Direction.EAST) || beam.getDirection().equals(Direction.WEST)) {
                            beam.setDirection(Direction.SOUTH);
                            beams.add(beam.split(Direction.NORTH));
                        }
                    }
                    case SPLITTER_HORIZONTAL -> {
                        if (beam.getDirection().equals(Direction.NORTH) || beam.getDirection().equals(Direction.SOUTH)) {
                            beam.setDirection(Direction.WEST);
                            beams.add(beam.split(Direction.EAST));
                        }
                    }
                }
            }

            beam.setPosition(beam.getPosition().move(beam.getDirection()));
            energised.putIfAbsent(beam.getPosition(), new HashSet<>());

            boolean requeue = beam.getPosition().getX() >= 0 && beam.getPosition().getX() <= maxX
                    && beam.getPosition().getY() >= 0 && beam.getPosition().getY() <= maxY
                    && !energised.get(beam.getPosition()).contains(beam);

            if (requeue) {
                energised.get(beam.getPosition()).add(beam);
                beams.add(beam);
            }
        }

        return (int) energised.entrySet().stream().filter(x -> !x.getValue().isEmpty()).count();
    }

    private static void drawEnergised(final Map<Point, BeamMutator> points, final Set<Point> energised) {
        final IntSummaryStatistics xStats = points.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = points.keySet().stream().mapToInt(Point::getY).summaryStatistics();
        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                String block = energised.contains(p) ? "#" : ".";
                System.out.printf("%s", block);
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static void draw(final Map<Point, BeamMutator> points, final Map<Point, List<Beam>> beams) {
        final IntSummaryStatistics xStats = points.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = points.keySet().stream().mapToInt(Point::getY).summaryStatistics();
        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                String block = beams.containsKey(p) ? (beams.get(p).size() > 1 ? String.valueOf(beams.get(p).size()) : ANSI_RED + beams.get(p).get(0).getDirection().getGlyph() + ANSI_RESET) : (points.containsKey(p) ? points.get(p).getSymbol() : ".");
                System.out.printf("%s", block);
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    private static class Beam {

        @EqualsAndHashCode.Include UUID uuid;
        @EqualsAndHashCode.Include Direction direction;
        Point position;

        public Beam() {
            uuid = UUID.randomUUID();
            direction = Direction.EAST;
            position = new Point(-1, 0);
        }

        public Beam split(final Direction direction) {
            return new Beam(uuid, direction, position);
        }

        @Override
        public String toString() {
            return uuid.toString();
        }

        public static Beam at(final int x, final int y, final Direction direction) {
            return new Beam(UUID.randomUUID(), direction, new Point(x, y));
        }
    }

    @Getter
    @AllArgsConstructor
    private enum BeamMutator {

        MIRROR_BW("\\"),
        MIRROR_FW("/"),
        SPLITTER_HORIZONTAL("-"),
        SPLITTER_VERTICAL("|")
        ;

        private final String symbol;

        public static BeamMutator parse(final char input) {
            return parse(String.valueOf(input));
        }

        public static BeamMutator parse(final String input) {
            return Arrays.stream(values())
                    .filter(mutator -> mutator.getSymbol().equals(input))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown symbol: " + input));
        }
    }
}
