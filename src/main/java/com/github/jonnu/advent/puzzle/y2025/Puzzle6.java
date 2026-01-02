package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle6 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        List<Operation> operations = new ArrayList<>();;
        List<List<Long>> math = new ArrayList<>();

        try (BufferedReader reader = resourceReader.read("y2025/puzzle6.txt")) {
            String line = reader.readLine();
            while (line != null) {
                if (!Character.isDigit(line.strip().charAt(0))) {
                    operations = Arrays.stream(line.split("\\s+")).map(Operation::fromGlyph).toList();
                    line = reader.readLine();
                    continue;
                }

                long[] chunks = Arrays.stream(line.split("\\s+"))
                        .filter(x -> !x.isBlank())
                        .mapToLong(Long::parseLong)
                        .toArray();
                for (int i = 0; i < chunks.length; i++) {
                    if (math.size() <= i) {
                        math.add(new ArrayList<>());
                    }
                    math.get(i).add(chunks[i]);
                }

                line = reader.readLine();
            }

            List<Long> results = new ArrayList<>();
            for (int i = 0; i < math.size(); i++) {
                long result = operations.get(i).getOperator().apply(math.get(i));
                System.out.printf("%s = %d%n", math.get(i)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(" " + operations.get(i).getCharacter() + " ")),
                result);
                results.add(i, result);
            }

            System.out.println(results.stream().mapToLong(x -> x).sum());
        }
    }

    @Getter
    @AllArgsConstructor
    private enum Operation {
        ADD("+", values -> values.stream().mapToLong(x -> x).sum()),
        MULTIPLY("*", values -> values.stream().mapToLong(x -> x).reduce(1, Math::multiplyExact)),
        ;

        private final String character;
        private final Function<List<Long>, Long> operator;

        public static Operation fromGlyph(final String glyph) {
            return Arrays.stream(values())
                    .filter(operation -> glyph.equals(operation.getCharacter()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No such operation: " + glyph));
        }
    }
}
