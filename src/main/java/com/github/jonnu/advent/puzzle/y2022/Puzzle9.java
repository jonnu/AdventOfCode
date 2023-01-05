package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle9 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle9.txt")) {

            Point[] knots = IntStream.range(0, 10)
                    .mapToObj(knot -> new Point(0, 0))
                    .toArray(Point[]::new);

            Set<String> tailVisited = new HashSet<>();
            Set<String> knotVisited = new HashSet<>();
            markVisitedPoint(knots[0], tailVisited);
            markVisitedPoint(knots[0], knotVisited);

            String line = reader.readLine();
            while (line != null) {

                String[] chunks = line.split(" ");
                Direction direction = Direction.fromString(chunks[0]);
                int distance = Integer.parseInt(chunks[1]);

                for (int i = 0; i < distance; i++) {

                    knots[0].move(direction);
                    for (int j = 1; j < knots.length; j++) {
                        knots[j].follow(knots[j - 1]);
                    }

                    markVisitedPoint(knots[1], tailVisited);
                    markVisitedPoint(knots[knots.length - 1], knotVisited);
                }

                line = reader.readLine();
            }

            System.out.println("Number of tail-visited positions: " + tailVisited.size());
            System.out.println("Number of knot-visited positions: " + knotVisited.size());
        }
    }

    private void markVisitedPoint(final Point point, final Set<String> visited) {
        visited.add(point.toString());
    }

    @Getter
    @AllArgsConstructor
    private static class Point {

        int x;
        int y;

        public void move(final Direction direction) {
            x += direction.getDelta().getX();
            y += direction.getDelta().getY();
        }

        public void follow(final Point point) {
            int delta = Math.max(Math.abs(getX() - point.getX()), Math.abs(getY() - point.getY()));
            if (delta > 1) {
                int deltaX = point.getX() - getX();
                int deltaY = point.getY() - getY();
                x += Math.abs(deltaX) == 2 ? deltaX / 2 : deltaX;
                y += Math.abs(deltaY) == 2 ? deltaY / 2 : deltaY;
            }
        }

        public String toString() {
            return "(" + getX() + "," + getY() + ")";
        }
    }

}

