package com.github.jonnu.advent.common.geometry.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.y2025.Puzzle9;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Polygon implements Shape2D {

    List<Point> vertices;

    @Override
    public double area() {

        // shoelace.
        long area = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Point current = vertices.get(i);
            Point next = vertices.get((i + 1) % vertices.size());
            area += ((long) current.getX() * next.getY() - (long) next.getX() * current.getY());
        }

        return Math.abs(area) / 2d;
    }

    @Override
    public double perimeter() {
        double distance = 0d;
        for(int i = vertices.size() - 1, j = 0; j < vertices.size(); i = j, j++) {
            distance += vertices.get(i).distance(vertices.get(j));
        }
        return distance;
    }

    public List<Puzzle9.Quad> lines() {
        List<Puzzle9.Quad> quads = new ArrayList<>();
        for (int a = 0, b = 1; a < vertices.size(); a++, b++) {
            Point x = vertices.get(a);
            Point y = vertices.get(b % vertices.size());
            quads.add(new Puzzle9.Quad(x, y));
        }
        return quads;
    }

    public List<Point> outline() {
        List<Point> points = new ArrayList<>();
        for (int a = 0, b = 1; a < vertices.size(); a++, b++) {
            Point x = vertices.get(a);
            Point y = vertices.get(b % vertices.size());

            if (x.getX() != y.getX()) {
                IntStream.range(Math.min(x.getX(), y.getX()), Math.max(x.getX(), y.getX()))
                        .boxed()
                        .map(n -> new Point(n, x.getY()))
                        .forEach(points::add);
            } else {
                IntStream.range(Math.min(x.getY(), y.getY()), Math.max(x.getY(), y.getY()))
                        .boxed()
                        .map(n -> new Point(x.getX(), n))
                        .forEach(points::add);
            }
        }
        return points;
    }

//    public static Polygon fromCorners(final Point x, final Point y) {
//
//        final BiFunction<Point, Point, Integer> manhattan = (a, b) -> Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
//
//        final Point origin = new Point(0, 0);
//        final Point tl = manhattan.apply(origin, x) < manhattan.apply(origin, y) ? x : y;
//        final Point br = tl.equals(x) ? y : x;
//        return new Polygon(List.of(
//            new Point(tl.getX(), tl.getY()),
//            new Point(br.getX(), tl.getY()),
//            new Point(tl.getX(), br.getY()),
//            new Point(br.getX(), br.getY())
//        ));
//    }
}
