package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.common.geometry.shape.Polygon;
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

            final List<Point> vertices = new ArrayList<>();
            final Queue<DigPlanInstruction> instructions = new ArrayDeque<>();

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

            while (!instructions.isEmpty()) {

                vertices.add(current);

                DigPlanInstruction instruction = instructions.poll();
                instruction = instruction.convert(); // remove this for part 1.

                final boolean isHorizontal = instruction.getDirection().isHorizontal();
                final int multiplier = instruction.getDirection().equals(Direction.NORTH) || instruction.getDirection().equals(Direction.WEST) ? -1 : 1;
                final int magnitude =  multiplier * instruction.getMetres();

                current = new Point(current.getX() + (isHorizontal ? magnitude : 0), current.getY() + (isHorizontal ? 0 : magnitude));
            }

            // @TODO - count the corners, possibly? using adj.
            // It's off by 1 + perimeter / 2 because the integer representation of this area
            // is too small by half a unit in every direction. Adding perimeter / 2 units gets
            // you closer, but there are actually four "corners" still unaccounted for, hence the +1.
            Polygon polygon = new Polygon(vertices);
            System.out.printf("Cubic meters of lava in the lagoon: %.0f%n", polygon.area() + polygon.perimeter() / 2 + 1);
            System.out.println();
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class DigPlanInstruction {

        Direction direction;
        int metres;
        String colour;

        public DigPlanInstruction convert() {
            int index = Integer.parseInt(getColour().substring(getColour().length() - 1));
            return DigPlanInstruction.builder()
                    .direction(Direction.cardinal().get((index + 1) % 4))
                    .metres(Integer.parseInt(getColour().substring(1, 6), 16))
                    .colour(getColour())
                    .build();
        }
    }

}
