package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle7 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        int split = 0;
        Point start = new Point(0, 0);

        final Set<Point> splitters = new HashSet<>();
        final Set<Beam> beams = new HashSet<>();

        int y = 0;
        int maxX;

        try (BufferedReader reader = resourceReader.read("y2025/puzzle7.txt")) {
            String line = reader.readLine();
            maxX = line.length();
            while (line != null) {
                for (int x = 0; x < line.length(); x++) {
                    switch (line.charAt(x)) {
                        case 'S' -> start = new Point(x, y);
                        case '^' -> splitters.add(new Point(x, y));
                    }
                }
                y++;
                line = reader.readLine();
            }
        }

        final Point bounds = new Point(maxX, y);
        final Queue<Beam> queue = new ArrayDeque<>();
        queue.add(new Beam(start));

        while (!queue.isEmpty()) {
            Beam beam = queue.poll();
            Point next = beam.next();

            // boundary check
            if (next.getX() < 0 || next.getX() > bounds.getX() || next.getY() < 0 || next.getY() > bounds.getY()) {
                beams.add(beam);
                continue;
            }

            if (splitters.contains(next)) {
                beams.add(beam);

                // try split beam into two
                boolean hasSplit = false;
                Point east = next.move(Direction.EAST);
                Point west = next.move(Direction.WEST);

                if (beams.stream().noneMatch(b -> b.hasPoint(west))) {
                    Beam westBeam = new Beam(next.move(Direction.WEST));
                    queue.add(westBeam);
                    beams.add(westBeam);
                    hasSplit = true;
                }

                if (beams.stream().noneMatch(b -> b.hasPoint(east))) {
                    Beam eastBeam = new Beam(next.move(Direction.EAST));
                    queue.add(eastBeam);
                    beams.add(eastBeam);
                    hasSplit = true;
                }

                if (hasSplit) {
                    split++;
                }

                continue;
            }

            beam.grow();
            // needs to be re-evaluated.
            queue.add(beam);
        }

        draw(bounds, start, splitters, beams);
        System.out.println("[Part 1] Tachyon beam is split " + split + " time(s).");
        // part2 dfs.
    }

    private static void draw(final Point bounds, final Point start, Set<Point> splitters, Set<Beam> beams) {
        Set<Point> beam = beams.stream().map(Beam::getPoints).flatMap(List::stream).collect(Collectors.toSet());
        for (int y = 0; y < bounds.getY(); y++) {
            for (int x = 0; x < bounds.getX(); x++) {
                Point point = new Point(x, y);
                if (point.equals(start)) {
                    System.out.print("S");
                    continue;
                }
                if (beam.contains(point)) {
                    System.out.print("|");
                    continue;
                }
                if (splitters.contains(point)) {
                    System.out.print("^");
                    continue;
                }
                System.out.print(".");
            }
            System.out.printf("%n");
        }
    }

    @Data
    static class Beam {

        private List<Point> points = new ArrayList<>();

        public Beam(final Point start) {
            points.add(start);
        }

        public Point position() {
            return points.getLast();
        }

        public boolean hasPoint(final Point point) {
            return points.contains(point);
        }

        public Point next() {
            return position().move(Direction.SOUTH);
        }

        public void grow() {
            points.add(next());
        }
    }
}
