package com.github.jonnu.advent.common.geometry;

import java.util.Set;
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

    public Point move(final Direction direction) {
        return new Point(getX() + direction.getDelta().getX(), getY() + direction.getDelta().getY());
    }

    public Set<Point> neighbours() {
        return Direction.all()
                .stream()
                .map(this::move)
                .collect(Collectors.toSet());
    }

    public Set<Point> cardinalNeighbours() {
        return Direction.cardinal()
                .stream()
                .map(this::move)
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
