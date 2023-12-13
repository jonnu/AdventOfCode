package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle3 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle3.txt")) {

            int lineNum = 0;
            int xLength = 0;
            String line = reader.readLine();
            Set<Component> components = new HashSet<>();
            Map<Point, String> symbols = new HashMap<>();

            while (line != null) {

                char[] chunk = line.toCharArray();
                final int y = lineNum;

                StringBuilder memo = new StringBuilder();;
                for (int x = 0; x < chunk.length; x++) {

                    if (!Character.isDigit(chunk[x])) {
                        if (chunk[x] != '.') {
                            symbols.put(new Point(x, y), Character.toString(chunk[x]));
                        }
                    } else {
                        memo.append(chunk[x]);
                    }

                    if (!memo.isEmpty() && (x + 1 == chunk.length || !Character.isDigit(chunk[x]))) {
                        components.add(Component.builder()
                                .value(Integer.parseInt(memo.toString()))
                                .points(IntStream.range(x - memo.length(), x)
                                        .mapToObj(a -> new Point(a, y))
                                        .collect(Collectors.toSet()))
                                .build());
                        memo = new StringBuilder();
                    }
                }

                line = reader.readLine();
                lineNum++;

                if (xLength == 0) {
                    xLength = chunk.length - 1;
                }
            }

            final int xMax = xLength;
            final int yMax = lineNum - 1;

            Set<Component> engineSchematic = components.stream()
                    .filter(component -> !Sets.intersection(component.getPoints()
                            .stream()
                            .flatMap(point -> point.neighbours()
                                    .stream()
                                    .filter(p -> withinBounds(p, 0, xMax, 0, yMax)))
                            .collect(Collectors.toSet()), symbols.keySet())
                            .isEmpty())
                    .collect(Collectors.toSet());

            Set<Component> nonComponents = Sets.difference(components, engineSchematic);

            final String ANSI_RED = "\u001B[31m";
            final String ANSI_RESET = "\u001B[0m";

            for (int y = 0; y <= yMax; y++) {
                for (int x = 0; x <= xMax; x++) {

                    if (x == 0) {
                        System.out.printf("%n");
                    }

                    Point z = new Point(x, y);
                    if (symbols.containsKey(z)) {
                        System.out.print(symbols.get(z));
                        continue;
                    }

                    Optional<Component> component = engineSchematic.stream().filter(c -> c.getPoints().contains(z)).findFirst();
                    Optional<Component> badComponent = nonComponents.stream().filter(c -> c.getPoints().contains(z)).findFirst();
                    if (component.isPresent()) {
                        System.out.print(component.get().getValue());
                        x = x + String.valueOf(component.get().getValue()).length() - 1;
                    } else if (badComponent.isPresent()) {
                        System.out.print(ANSI_RED + badComponent.get().getValue() + ANSI_RESET);
                        x = x + String.valueOf(badComponent.get().getValue()).length() - 1;
                    } else {
                        System.out.print(".");
                    }
                }
            }

            long engineSchematicSum = engineSchematic.stream()
                    .mapToLong(Component::getValue)
                    .sum();

            long gearRatioSum = symbols.entrySet().stream()
                    .filter(e -> e.getValue().equals("*"))
                    .map(Map.Entry::getKey)
                    .map(gear -> components.stream().filter(component -> component.neighbours().contains(gear)).collect(Collectors.toSet()))
                    .filter(gears -> gears.size() == 2)
                    .mapToLong(s -> s.stream().map(Component::getValue).reduce(1, Math::multiplyExact))
                    .sum();

            System.out.printf("%n%nSum of all of the part numbers in the engine schematic: %d", engineSchematicSum);
            System.out.printf("%n%nSum of all of the gear ratios in the engine schematic: %d", gearRatioSum);
        }
    }

    private static Set<Point> getAdjoining(Point needle, Set<Point> haystack) {
        return haystack.stream()
                .filter(point -> point.neighbours().contains(needle))
                .collect(Collectors.toSet());
    }

    private static boolean withinBounds(final Point point, int minX, int maxX, int minY, int maxY) {
        return point.getX() >= minX && point.getX() <= maxX && point.getY() >= minY && point.getY() <= maxY;
    }

    @Value
    @Builder
    private static class Component {
        Set<Point> points;
        int value;

        public Set<Point> neighbours() {
            return Stream.concat(points.stream(), points.stream().map(Point::neighbours).flatMap(Collection::stream)).collect(Collectors.toSet());
        }
    }
}
