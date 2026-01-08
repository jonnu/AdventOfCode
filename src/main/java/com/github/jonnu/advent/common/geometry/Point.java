package com.github.jonnu.advent.common.geometry;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Point {

    private final int x;
    private final int y;

    public double distance(final Point other) {
        return Math.sqrt(Math.pow(getX() - other.getX(), 2) + Math.pow(getY() - other.getY(), 2));
    }

    public Point move(final Direction direction) {
        return new Point(getX() + direction.getDelta().getX(), getY() + direction.getDelta().getY());
    }

    public Map<Direction, Point> neighbours() {
        return Direction.all()
                .stream()
                .collect(Collectors.toMap(Function.identity(), this::move));
    }

    public Map<Direction, Point> cardinalNeighbours() {
        return Direction.cardinal()
                .stream()
                .collect(Collectors.toMap(Function.identity(), this::move));
    }

    public boolean liesBetween(final Point x, final Point y) {
        int dxc = getX() - x.getX();
        int dyc = getY() - x.getY();
        int dxl = y.getX() - x.getX();
        int dyl = y.getY() - x.getY();
        int c = dxc * dyl - dyc * dxl;
        return c == 0;
//        dxc = currPoint.x - point1.x;
//        dyc = currPoint.y - point1.y;
//
//        dxl = point2.x - point1.x;
//        dyl = point2.y - point1.y;
//
//        cross = dxc * dyl - dyc * dxl;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
