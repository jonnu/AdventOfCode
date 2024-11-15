package com.github.jonnu.advent.common.geometry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Direction {

    NORTH("N", new Point(0, -1)),
    NORTHEAST("NE", new Point(1, -1)),
    EAST("E", new Point(1, 0)),
    SOUTHEAST("SE", new Point(1, 1)),
    SOUTH("S", new Point(0, 1)),
    SOUTHWEST("SW", new Point(-1, 1)),
    WEST("W", new Point(-1, 0)),
    NORTHWEST("NW", new Point(-1, -1));

    @Getter
    @AllArgsConstructor
    private enum Alias {

        UP("U", Direction.NORTH),
        DOWN("D", Direction.SOUTH),
        LEFT("L", Direction.WEST),
        RIGHT("R", Direction.EAST);

        private final String glyph;
        private Direction aliasedTo;

        private static Optional<Direction> fromString(final String string) {
            return Arrays.stream(Alias.values())
                    .filter(aliased -> aliased.getGlyph().equalsIgnoreCase(string))
                    .map(Alias::getAliasedTo)
                    .findFirst();
        }
    }

    private final String glyph;
    private final Point delta;

    public boolean isVertical() {
        return delta.getX() == 0;
    }

    public boolean isHorizontal() {
        return delta.getY() == 0;
    }

    public static Set<Direction> all() {
        return Set.of(values());
    }

    public Direction opposite() {
        return values()[(ordinal() + 4) % values().length];
    }

    public static List<Direction> cardinal() {
        return IntStream.range(0, values().length)
                .filter(ordinal -> ordinal % 2 == 0)
                .mapToObj(ordinal -> values()[ordinal])
                .collect(Collectors.toList());
    }

    public static List<Direction> intercardinal() {
        return IntStream.range(0, values().length)
                .filter(ordinal -> ordinal % 2 == 1)
                .mapToObj(ordinal -> values()[ordinal])
                .collect(Collectors.toList());
    }

    public static Direction fromString(final char character) {
        return fromString(Character.toString(character));
    }

    public static Direction fromString(final String string) {
        return Alias.fromString(string)
                .orElseGet(() -> Arrays.stream(Direction.values())
                        .filter(direction -> direction.getGlyph().equalsIgnoreCase(string))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown Direction: " + string)));
    }
}
