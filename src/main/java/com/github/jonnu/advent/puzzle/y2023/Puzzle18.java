package com.github.jonnu.advent.puzzle.y2023;

import static com.google.common.math.IntMath.gcd;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle18 implements Puzzle {

    private final ResourceReader resourceReader;

    private static final Pattern PATTERN = Pattern.compile("^(?<direction>[LDUR])\\s(?<metres>\\d+)\\s\\((?<colour>#[0-9a-f]{6})\\)$");

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle18.txt")) {

            //Set<Point> digSite = new HashSet<>();
            List<Point> digVertex = new ArrayList<>();

            Queue<DigPlanInstruction> instructions = new ArrayDeque<>();

            String line = reader.readLine();
            while (line != null) {

                Matcher matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Cannot parse line: " + line);
                }

                instructions.add(DigPlanInstruction.builder()
                        .direction(Direction.fromString(matcher.group("direction")))
                        .metres(Integer.parseInt(matcher.group("metres")))
                        .colour(matcher.group("colour"))
                        .build());

                line = reader.readLine();
            }

            Point current = new Point(0, 0);

            DigPlanInstruction prev = null;
            boolean lastInside;
            boolean inside = false;
            double adj = 0d;
            while (!instructions.isEmpty()) {

                digVertex.add(current);
                DigPlanInstruction instruction = instructions.poll();

                instruction = convert(instruction.getColour());
                System.out.println("- " + instruction.getColour() + " = " + instruction.getDirection().getGlyph() + " " + instruction.getMetres());

                if (prev != null) {
                    inside = prev.getDirection().rotate(Direction.Rotation.CLOCKWISE).equals(instruction.getDirection());
                }

                final boolean horizontal = instruction.getDirection().isHorizontal();
                final int multiplier = instruction.getDirection().equals(Direction.NORTH) || instruction.getDirection().equals(Direction.WEST) ? -1 : 1;
                final int coordinate = horizontal ? current.getX() : current.getY();
                final Point point = current;

                List<Point> digs = IntStream.iterate(coordinate, c -> c + multiplier).limit(1 + instruction.getMetres())
                        .mapToObj(i -> new Point(horizontal ? i : point.getX(), horizontal ? point.getY() : i))
                        .collect(Collectors.toList());
                //too low: 2147483647

                //digSite.addAll(digs);
                current = digs.get(digs.size() - 1);
                prev = instruction;
                lastInside = inside;
                adj = (lastInside ? .5 : -.5) + (inside ? .5 : -.5);
            }

            digVertex.forEach(System.out::println);

            //draw(digSite.stream().collect(Collectors.toMap(p -> p, l -> "#")), digVertex);

            // 50465
            System.out.println("Cubic metres of lava held: " + (int) shoelace2(digVertex, adj));

            System.out.println(calculatePolygonArea(digVertex));
            System.out.println(perimeter(digVertex));

        }
    }
    public static double perimeter(List<Point> vertices) {
        if (vertices == null || vertices.size() < 2) {
            throw new IllegalArgumentException("Invalid number of vertices for a polygon");
        }

        double perimeter = 0;
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % n);

            double dx = p2.getX() - p1.getX();
            double dy = p2.getY() - p1.getY();

            perimeter += Math.sqrt(dx * dx + dy * dy);
        }

        return perimeter;
    }

    private static DigPlanInstruction convert(String hexadecimal) {

        int i = Integer.parseInt(hexadecimal.substring(hexadecimal.length() - 1));

        Direction x = switch (i) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            case 3 -> Direction.NORTH;
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
        //1072888051
        //952408144115

        return DigPlanInstruction.builder()
                .direction(x)
                .metres(Integer.parseInt(hexadecimal.substring(1, 6), 16))
                .colour(hexadecimal)
                .build();
    }

    public static double calculatePolygonArea4(List<Point> vertices, double cornerAdj) {
        if (vertices == null || vertices.size() < 3) {
            throw new IllegalArgumentException("Invalid number of vertices for a polygon");
        }

        int i = countLatticePointsInside(vertices);
        int b = countLatticePointsOnBoundary(vertices);
        //int c = countExteriorCorners(vertices);
        // Apply Pick's theorem
        double area = i + (b / 2.0) - 1 + cornerAdj;

        //System.out.println("Number of Exterior Corners: " + c);

        return Math.abs(area);
    }

    private static int countLatticePointsInside(List<Point> vertices) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Point vertex : vertices) {
            int x = vertex.getX();
            int y = vertex.getY();

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        int count = 0;
        for (int x = minX + 1; x < maxX; x++) {
            for (int y = minY + 1; y < maxY; y++) {
                if (isPointInsidePolygon(vertices, x, y)) {
                    count++;
                }
            }
        }

        return count;
    }

    private static int countLatticePointsOnBoundary(List<Point> vertices) {
        int count = 0;
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % n);

            int dx = Math.abs(p1.getX() - p2.getX());
            int dy = Math.abs(p1.getY() - p2.getY());

            int gcd = gcd(dx, dy);

            count += gcd;
        }

        return count;
    }

    private static boolean isPointInsidePolygon(List<Point> vertices, int x, int y) {
        int count = 0;
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % n);

            if ((p1.getY() <= y && y < p2.getY() || p2.getY() <= y && y < p1.getY()) &&
                    x < (p2.getX() - p1.getX()) * (y - p1.getY()) / (p2.getY() - p1.getY()) + p1.getX()) {
                count++;
            }
        }

        return count % 2 == 1;
    }

    public static double calculatePolygonArea(List<Point> vertices) {
        if (vertices == null || vertices.size() < 3) {
            throw new IllegalArgumentException("Invalid number of vertices for a polygon");
        }

        double area = 0;
        int n = vertices.size();

        for (int i = 1; i < n - 1; i++) {
            int x1 = vertices.get(0).getX();
            int y1 = vertices.get(0).getY();

            int x2 = vertices.get(i).getX();
            int y2 = vertices.get(i).getY();

            int x3 = vertices.get(i + 1).getX();
            int y3 = vertices.get(i + 1).getY();

            area += 0.5 * Math.abs(x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2));
        }

        return area;
    }
    private static int countExteriorCorners(List<Point> vertices) {
        int corners = 0;
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % n);
            Point p3 = vertices.get((i + 2) % n);

            if (isExteriorCorner(p1, p2, p3)) {
                corners++;
            }
        }

        return corners;
    }

    private static boolean isExteriorCorner(Point p1, Point p2, Point p3) {
        int x1 = p1.getX(), y1 = p1.getY();
        int x2 = p2.getX(), y2 = p2.getY();
        int x3 = p3.getX(), y3 = p3.getY();

        int crossProduct = (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);

        // Check if the cross product is negative (indicating an exterior corner)
        return crossProduct < 0;
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class DigPlanInstruction {
        Direction direction;
        int metres;
        String colour;
    }

    public static double shoelace2(List<Point> vertices, double adj) {
        if (vertices == null || vertices.size() < 3) {
            throw new IllegalArgumentException("Invalid number of vertices for a polygon");
        }

        double area = 0;
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            int x1 = vertices.get(i).getX();
            int y1 = vertices.get(i).getY();

            int x2 = vertices.get((i + 1) % n).getX();
            int y2 = vertices.get((i + 1) % n).getY();

            area += (x1 * y2 - x2 * y1);
        }

        // Take the absolute value of the area
        area = Math.abs(area);

        // Calculate the area covered by the edges
        for (int i = 0; i < n; i++) {
            int x = vertices.get(i).getX();
            int y = vertices.get(i).getY();

            int nextX = vertices.get((i + 1) % n).getX();
            int nextY = vertices.get((i + 1) % n).getY();

            // Add the length of each edge (distance between consecutive vertices)
            area += Math.sqrt(Math.pow(nextX - x, 2) + Math.pow(nextY - y, 2));
        }

        // The area should be non-negative
        return Math.abs(area / 2) + adj;
    }

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    private static <T> void draw(final Map<Point, T> data, final Collection<Point> highlight) {
        final IntSummaryStatistics xStats = data.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = data.keySet().stream().mapToInt(Point::getY).summaryStatistics();
        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                System.out.printf("%s%s%s", highlight.contains(p) ? ANSI_RED : "", data.containsKey(p) ? data.get(p) : ".", highlight.contains(p) ? ANSI_RESET : "");
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }
}
