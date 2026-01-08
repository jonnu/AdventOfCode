package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.common.geometry.shape.Polygon;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle9 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        List<Point> redTiles = new ArrayList<>();

        try (BufferedReader reader = resourceReader.read("y2025/puzzle9.txt")) {
            String line = reader.readLine();
            while (line != null) {
                int[] xy = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
                redTiles.add(new Point(xy[0], xy[1]));
                line = reader.readLine();
            }
        }

        Queue<Quad> queue = new PriorityQueue<>(Comparator.comparingLong(Quad::area).reversed());
        for (Point left : redTiles) {
            for (Point right : redTiles) {
                if (right.equals(left)) {
                    continue;
                }
                queue.add(Quad.fromCorners(left, right));
            }
        }
        Quad largest = queue.poll();
        draw(largest, redTiles, new Point(13, 8));

        System.out.println("[Part 1] Largest rectangle area between two red tiles: " + largest.area());

        // Part 2
        Polygon polygon = new Polygon(List.copyOf(redTiles));
        System.out.println("[Part 2] Poly: " + polygon.area());
        draw(polygon, new Point(13, 8));
    }

    private static void draw(final Polygon polygon, final Point bounds) {
        List<Point> outline = polygon.outline();
        for (int y = 0; y <= bounds.getY(); y++) {
            for (int x = 0; x <= bounds.getX(); x++) {
                Point p = new Point(x, y);

                if (polygon.getVertices().contains(p)) {
                    System.out.print("#");
                    continue;
                }
                if (outline.contains(p)) {
                    System.out.print("X");
                    continue;
                }
                if (polygon.contains(p)) {
                    System.out.print("x");
                    continue;
                }
                System.out.print(".");
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }

    private static void draw(final Quad quad, final Collection<Point> redTiles, final Point bounds) {
        for (int y = 0; y <= bounds.getY(); y++) {
            for (int x = 0; x <= bounds.getX(); x++) {
                Point p = new Point(x, y);
                if (quad.contains(p)) {
                    System.out.print("O");
                    continue;
                }
                if (redTiles.contains(p)) {
                    System.out.print("#");
                    continue;
                }
                System.out.print(".");
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }

    record Quad(Point x, Point y) {

        long area() {
            return (long) Math.abs((y.getX() - x.getX()) + 1) * (Math.abs(y.getY() - x.getY() + 1));
        }

        boolean contains(final Point point) {
            return point.getX() >= x.getX() && point.getX() <= y.getX() &&
                    point.getY() >= x.getY() && point.getY() <= y.getY();
        }

        static Quad fromCorners(Point x, Point y) {
            final Point tl = new Point(Math.min(x.getX(), y.getX()), Math.min(x.getY(), y.getY()));
            final Point br = new Point(Math.max(x.getX(), y.getX()), Math.max(x.getY(), y.getY()));
            return new Quad(tl, br);
        }
    }
}
