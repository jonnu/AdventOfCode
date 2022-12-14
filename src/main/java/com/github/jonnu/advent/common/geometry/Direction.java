package com.github.jonnu.advent.common.geometry;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

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

    public static Set<Direction> cardinal() {
        return Set.of(NORTH, EAST, SOUTH, WEST);
    }

    public static Set<Direction> intercardinal() {
        return Set.of(NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST);
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
