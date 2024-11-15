package com.github.jonnu.advent.common.geometry.shape;

import java.util.List;

import com.github.jonnu.advent.common.geometry.Point;
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

}
