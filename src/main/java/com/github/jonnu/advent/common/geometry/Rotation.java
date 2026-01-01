package com.github.jonnu.advent.common.geometry;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Rotation {

    CLOCKWISE("R"),
    ANTICLOCKWISE("L")
    ;

    private final String glyph;

    public static Rotation fromString(final String string) {
        return Arrays.stream(values())
                .filter(element -> element.getGlyph().equalsIgnoreCase(string))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Rotation: " + string));
    }
}
