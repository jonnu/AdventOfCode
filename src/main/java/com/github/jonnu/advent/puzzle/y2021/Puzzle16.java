package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle16 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle16.txt")) {

            String binary = "";
            String line = reader.readLine();
            while (line != null) {
                binary = hex2bin(line);
                line = reader.readLine();
            }

            Packet packet = parse(binary);
            System.out.println("VersionSum: " + versionSum(packet));
        }
    }


    private static Packet parse(final String binary) {
        int version = parseInt(binary, 0, 3);
        int typeId = parseInt(binary, 3, 3);

        switch (typeId) {
            case 4:
                Literal literal = literal(binary.substring(6));
                //System.out.println(literal);
                return Packet.builder()
                        .binary(binary)
                        .version(version)
                        .typeId(typeId)
                        .value(literal.getValue())
                        .length(6 + literal.getLength())
                        .subPackets(Collections.EMPTY_LIST)
                        .build();
                //break;
            default:
                int lengthTypeId = parseInt(binary, 6, 1);
                if (lengthTypeId == 0) {

                    int bits = parseInt(binary, 7, 15);
                    int read = 0;

                    List<Packet> children = new ArrayList<>();
                    while (read < bits) {
                        //System.out.println("Read: " + read + ", Bits: " + bits + "(" + binary.substring(22 + read, 22 + bits) + ")");
                        Packet child = parse(binary.substring(22 + read));
                        children.add(child);
                        //System.out.println("Child: " + child);
                        read += child.getLength();
                    }

                    //System.out.println("Return from 0");
                    return Packet.builder()
                            .binary(binary)
                            .version(version)
                            .typeId(typeId)
                            //.length(binary.length())
                            .length(22 + read)
                            .subPackets(children)
                            .build();

                } else {
                    int packets = parseInt(binary, 7, 11);
                    int process = 0;
                    int read = 0;

                    List<Packet> children = new ArrayList<>();
                    while (process < packets) {
                        Packet child = parse(binary.substring(18 + read));
                        children.add(child);
                        //System.out.println("Child: " + child);
                        read += child.getLength();
                        process++;
                    }

                    //System.out.println("Return from 1");
                    return Packet.builder()
                            .binary(binary)
                            .version(version)
                            .typeId(typeId)
                            .length(18 + read)
                            //.length(binary.length())
                            .subPackets(children)
                            .build();
                }
                //break;
        }


    }

    private static int parseInt(final String binary, final int start, final int length) {
        //System.out.println(binary + " (" + start + " - " + (start+length) + ") = " + binary.substring(start, start + length) + " = " + Integer.parseInt(binary.substring(start, start + length), 2));
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
        StringBuilder builder = new StringBuilder();

        //101111111000101
        // 0111 1110 0101
        while (!isLastWord) {
            isLastWord = binary.charAt(pointer) == '0';
            builder.append(binary.substring(pointer + 1, pointer + 5));
            //System.out.printf("L: %5s, P: %02d, %s%n", isLastWord, pointer, builder);
            pointer += 5;
        }

        //System.out.println("B: " + binary + " (" + binary.length() + "); P: " + pointer);
        return new Literal(Long.parseLong(builder.toString(), 2), pointer);
    }

    private static int versionSum(final Packet packet) {
        return versionSum(packet, 0);
    }

    private static int versionSum(final Packet packet, int sum) {

        System.out.println(packet);
        sum += packet.getVersion();
        for (Packet cp : packet.getSubPackets()) {
            sum = versionSum(cp, sum);
        }
        return sum;
    }


    @AllArgsConstructor
    @Builder
    @Data
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
}

