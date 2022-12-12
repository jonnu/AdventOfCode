package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle11 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle11.txt")) {
            String line = reader.readLine();

            long commonDenominator = 1;
            List<Monkey> monkeys = new ArrayList<>();
            Monkey.MonkeyBuilder monkeyBuilder = Monkey.builder();

            while (line != null) {

                final String[] args = parseLineIntoArgs(line);

                switch (args[0]) {
                    case "Monkey" -> monkeyBuilder = Monkey.builder().id(Integer.parseInt(args[1]));
                    case "Starting" -> monkeyBuilder.items(Arrays.stream(args).skip(2).map(Long::parseLong).collect(Collectors.toCollection(LinkedList::new)));
                    case "Operation" -> {

                        ArithmeticOperator operator = ArithmeticOperator.fromSymbol(args[4]);
                        Long value = Optional.of(args[5])
                                .filter(input -> !input.equals("old"))
                                .map(Long::parseLong)
                                .orElse(null);

                        LongUnaryOperator worryOperation = worry -> {
                            long delta = value == null ? worry : value;
                            switch (operator) {
                                case ADDITION -> { return worry + delta; }
                                case SUBTRACTION -> { return worry - delta; }
                                case MULTIPLICATION -> { return worry * delta; }
                                default -> throw new IllegalStateException("Unexpected operator: " + operator);
                            }
                        };

                        monkeyBuilder.inspection(worryOperation);
                    }
                    case "Test" -> {
                        int divisor = Integer.parseInt(args[args.length - 1]);
                        commonDenominator *= divisor;
                        int trueMonkey = parseLineIntoArgsAndGetLast(reader.readLine());
                        int falseMonkey = parseLineIntoArgsAndGetLast(reader.readLine());
                        monkeyBuilder.throwToMonkey(w -> w % divisor == 0 ? trueMonkey : falseMonkey);
                        monkeys.add(monkeyBuilder.build());
                    }
                    case "Skip" -> {}
                    default -> throw new IllegalArgumentException("Unhandled line: " + line);
                }

                line = reader.readLine();
            }

            final int rounds = 10000;
            final long denom = commonDenominator;
            final MonkeyBusiness monkeyBusiness = MonkeyBusiness.builder()
                    .monkeys(monkeys)
                    .numberOfRounds(rounds)
                    .postInspectionOperation(rounds >= 10000 ? w -> w % denom : w -> Math.floorDiv(w, 3))
                    .build();

            monkeyBusiness.simulateKeepAway();
            System.out.println("Monkey business after " + monkeyBusiness.getNumberOfRounds() + ": " + monkeyBusiness.getLevel());
        }
    }

    @Builder
    @AllArgsConstructor
    private static class MonkeyBusiness {

        List<Monkey> monkeys;
        @Getter int numberOfRounds;
        LongUnaryOperator postInspectionOperation;

        public void simulateKeepAway() {
            for (int r = 1; r <= numberOfRounds; r++) {
                for (Monkey currentMonkey : monkeys) {
                    while (!currentMonkey.getItems().isEmpty()) {
                        long worry = currentMonkey.getItems().pollFirst();
                        worry = currentMonkey.getInspection().applyAsLong(worry);
                        currentMonkey.incrementInspections();
                        worry = postInspectionOperation.applyAsLong(worry);
                        monkeys.get(currentMonkey.getThrowToMonkey().apply(worry)).getItems().addLast(worry);
                    }
                }
            }
        }

        public long getLevel() {
            return monkeys.stream()
                    .sorted(Comparator.comparingInt(Monkey::getInspections).reversed())
                    .limit(2)
                    .peek(System.out::println)
                    .mapToLong(Monkey::getInspections)
                    .reduce(1, Math::multiplyExact);
        }
    }

    private static String[] parseLineIntoArgs(final String raw) {
        return Optional.of(raw)
                .filter(input -> !input.isEmpty())
                .map(String::trim)
                .map(input -> input.replaceAll("[,:]", ""))
                .map(input -> input.split(" "))
                .orElseGet(() -> new String[] { "Skip" });
    }

    private static int parseLineIntoArgsAndGetLast(final String raw) {
        final String[] args = parseLineIntoArgs(raw);
        return Integer.parseInt(args[args.length - 1]);
    }

    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    private static class Monkey {
        private final int id;
        private int inspections;
        private final Deque<Long> items;
        private final LongUnaryOperator inspection;
        private final Function<Long, Integer> throwToMonkey;

        public void incrementInspections() {
            inspections++;
        }
    }

    @Getter
    @AllArgsConstructor
    private enum ArithmeticOperator {

        ADDITION("+"),
        SUBTRACTION("-"),
        MULTIPLICATION("*");

        String symbol;

        public static ArithmeticOperator fromSymbol(final String symbol) {
            return Arrays.stream(values())
                    .filter(operator -> operator.getSymbol().equals(symbol))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown symbol: " + symbol));
        }
    }
}
