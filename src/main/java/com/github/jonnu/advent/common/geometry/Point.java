package com.github.jonnu.advent.common.geometry;

import java.util.Map;
import java.util.Set;
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

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
