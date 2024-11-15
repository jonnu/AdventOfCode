package com.github.jonnu.advent.puzzle.y2023;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle15 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle15.txt")) {

            final String line = reader.readLine();
            final long output = Arrays.stream(line.split(","))
                    .map(String::chars)
                    .mapToLong(Puzzle15::hashAlgorithm)
                    .sum();

            System.out.println("Hash of initialization sequence: " + output);

            final List<Instruction> instructions = Arrays.stream(line.split(","))
                    .map(Puzzle15::parseInstruction)
                    .toList();

            final List<Box> boxes = IntStream.range(0, 256)
                    .mapToObj(i -> Box.builder().id(i).build())
                    .toList();

            instructions.forEach(instruction -> {
                int boxId = hashAlgorithm(instruction.getLens().getLabel().chars());
                switch (instruction.getOperation()) {
                    case UPSERT -> boxes.get(boxId).addLens(instruction.getLens());
                    case REMOVE -> boxes.get(boxId).removeLens(instruction.getLens());
                }
            });

            long focusingPower = boxes.stream().mapToLong(Box::getFocusingPower).sum();
            System.out.println("Focusing power of the lens configuration: " + focusingPower);
        }
    }

    private static int hashAlgorithm(final IntStream input) {
        return input.reduce(0, (l, r) -> (l + r) * 17 % 256);
    }

    private static Instruction parseInstruction(final String input) {
        final String[] parts = input.split("[=-]");
        return Instruction.builder()
                .lens(Lens.builder()
                        .label(parts[0])
                        .focalLength(parts.length > 1 ? Long.parseLong(parts[1]) : -1)
                        .build())
                .operation(Operation.parse(input))
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    private static class Box {

        private final int id;
        @Builder.Default List<Lens> lenses = new ArrayList<>();

        public void addLens(final Lens lens) {
            final int index = lenses.indexOf(lens);
            if (index < 0) {
                lenses.add(lens);
            } else {
                lenses.set(index, lens);
            }
        }

        public void removeLens(final Lens lens) {
            final int index = lenses.indexOf(lens);
            if (index >= 0) {
                lenses.remove(index);
            }
        }

        public long getFocusingPower() {

            long output = 0;
            if (isEmpty()) {
                return output;
            }

            final int boxValue = getId() + 1;
            final Queue<Lens> queue = new ArrayDeque<>(getLenses());
            int position = 1;
            while (!queue.isEmpty()) {
                Lens lens = queue.poll();
                output += boxValue * position * lens.getFocalLength();
                position++;
            }

            return output;
        }

        public boolean isEmpty() {
            return lenses.isEmpty();
        }

        @Override
        public String toString() {
            return lenses.stream()
                    .map(Lens::toString)
                    .collect(Collectors.joining(" "));
        }
    }

    @Value
    @Builder
    @EqualsAndHashCode
    private static class Lens {

        String label;
        @EqualsAndHashCode.Exclude long focalLength;

        @Override
        public String toString() {
            return "[" + getLabel() + (getFocalLength() >= 0 ? " " + getFocalLength() : "") + "]";
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class Instruction {
        Operation operation;
        Lens lens;
    }

    @Getter
    @AllArgsConstructor
    private enum Operation {

        UPSERT("="),
        REMOVE("-");

        private final String symbol;

        public static Operation parse(final String input) {
            return Arrays.stream(values())
                    .filter(x -> input.contains(x.getSymbol()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cannot parse input: " + input));
        }
    }
}
