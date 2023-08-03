package com.github.jonnu.advent.puzzle.y2021;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle16 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle16.txt")) {

            final String binary = hex2bin(reader.readLine());
            final Packet packet = parse(binary);

            System.out.println("sum of versions: " + versionSum(packet));
            System.out.println("outermost value: " + packet.getValue());
        }
    }

    private static Packet parse(final String binary) {

        int version = parseInt(binary, 0, 3);
        final Operator operator = Operator.resolve(parseInt(binary, 3, 3));

        if (operator.equals(Operator.LITERAL)) {
            Literal literal = literal(binary.substring(6));
            return Packet.builder()
                    .binary(binary)
                    .version(version)
                    .typeId(operator.value())
                    .value(literal.getValue())
                    .length(6 + literal.getLength())
                    .subPackets(Collections.emptyList())
                    .build();
        }

        int read = 0;
        int opType = parseInt(binary, 6, 1);
        int offset = opType == 0 ? 22 : 18;
        List<Packet> children = new ArrayList<>();

        if (opType == 0) {

            int bits = parseInt(binary, 7, 15);

            while (read < bits) {
                Packet child = parse(binary.substring(offset + read));
                children.add(child);
                read += child.getLength();
            }

        } else {

            int packetsRequired = parseInt(binary, 7, 11);
            int packetsProcessed = 0;

            while (packetsProcessed < packetsRequired) {
                Packet child = parse(binary.substring(offset + read));
                children.add(child);
                read += child.getLength();
                packetsProcessed++;
            }
        }

        long value = 0;
        switch (operator) {
            case SUM -> value = children.stream().mapToLong(Packet::getValue).sum();
            case PRODUCT -> value = children.stream().mapToLong(Packet::getValue).reduce(1, (a, b) -> a * b);
            case MIN -> value = children.stream().mapToLong(Packet::getValue).summaryStatistics().getMin();
            case MAX -> value = children.stream().mapToLong(Packet::getValue).summaryStatistics().getMax();
            case GREATER_THAN -> value = children.get(0).getValue() > children.get(1).getValue() ? 1 : 0;
            case LESS_THAN -> value = children.get(0).getValue() < children.get(1).getValue() ? 1 : 0;
            case EQUAL_TO -> value = children.get(0).getValue() == children.get(1).getValue() ? 1 : 0;
        }

        return Packet.builder()
                .binary(binary)
                .version(version)
                .value(value)
                .typeId(operator.value())
                .length(offset + read)
                .subPackets(children)
                .build();
    }

    private static int parseInt(final String binary, final int start, final int length) {
        return Integer.parseInt(binary.substring(start, start + length), 2);
    }

    private static String hex2bin(final String hex) {
        return Arrays.stream(hex.split(""))
                .map(c -> Integer.parseInt(c, 16))
                .map(Integer::toBinaryString)
                .map(c -> String.format("%4s", c).replace(' ', '0'))
                .collect(Collectors.joining());
    }

    private static Literal literal(final String binary) {

        int pointer = 0;
        boolean isLastWord = false;
        final StringBuilder builder = new StringBuilder();

        while (!isLastWord) {
            isLastWord = binary.charAt(pointer) == '0';
            builder.append(binary, pointer + 1, pointer + 5);
            pointer += 5;
        }

        return new Literal(Long.parseLong(builder.toString(), 2), pointer);
    }

    private static int versionSum(final Packet packet) {
        return versionSum(packet, 0);
    }

    private static int versionSum(final Packet packet, int sum) {

        sum += packet.getVersion();
        for (Packet cp : packet.getSubPackets()) {
            sum = versionSum(cp, sum);
        }

        return sum;
    }

    @AllArgsConstructor
    @Builder
    @Value
    public static class Packet {
        String binary;
        int version;
        int typeId;
        long value;
        int length;
        List<Packet> subPackets;
    }

    @AllArgsConstructor
    @Value
    public static class Literal {
        long value;
        int length;
    }

    private enum Operator {
        SUM,
        PRODUCT,
        MIN,
        MAX,
        LITERAL,
        GREATER_THAN,
        LESS_THAN,
        EQUAL_TO;

        public int value() {
            return ordinal();
        }

        public static Operator resolve(final int ordinal) {
            return Arrays.stream(Operator.values())
                    .skip(ordinal)
                    .findFirst()
                    .orElse(null);
        }
    }
}

