package com.github.jonnu.advent.puzzle.y2021;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle16 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle16.txt")) {

            String input = "";
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                input = hex2bin(line);
                        //.forEach(System.out::println);
                System.out.println(input);
                //line.chars().mapToObj(c -> Integer.parseInt(Character.toString(c), 16))map(c -> Integer.parseInt(c, 16)) (c -> String.format("%4s", Integer.toBinaryString(c)).replace(' ', '0')).forEach(System.out::println);
                line = reader.readLine();
            }

            final String binary = input;
            int version = bin2int(binary.substring(0, 3));
            int typeId = bin2int(binary.substring(3, 6));

            switch (typeId) {
                // Literals
                case 4:
                    int pointer = 6;
                    boolean lastWord = false;
                    StringBuilder builder = new StringBuilder();

                    while (!lastWord) {
                        lastWord = binary.substring(pointer, pointer + 1).equals("0");
                        System.out.println(lastWord);
                        pointer++;
                        builder.append(binary.substring(pointer, pointer + 4));
                        pointer += 4;
                    }

                    System.out.println(builder + " = " + bin2int(builder.toString()));

                    break;
                    // Operator
                default:
                    pointer = 6;
                    int length = Integer.parseInt(binary.substring(pointer, pointer + 1), 2);
                    pointer++;

                    if (length == 0) {
                        // 15-bit number, total length in bits
                        int bits = bin2int(binary.substring(pointer, pointer + 15));
                        System.out.println("length 0, bits: " + bits);
                        pointer += 15;

                        int read = 0;
                        // refactor this awful stuff.
                        while (read < bits) {

                            lastWord = false;
                            builder = new StringBuilder();

                            // skip version (3 bits) + type (3 bits).
                            // we should probably parse this.
                            pointer += 6;
                            read += 6;

                            while (!lastWord) {
                                lastWord = binary.substring(pointer, pointer + 1).equals("0");
                                //System.out.println(lastWord);
                                pointer++;
                                read++;
                                builder.append(binary.substring(pointer, pointer + 4));
                                //System.out.println("last: " + binary.substring(pointer, pointer + 4) + " (" + bin2int(binary.substring(pointer, pointer + 4)) + ")");
                                pointer += 4;
                                read += 4;
                            }

                            System.out.println(builder + " = " + bin2int(builder.toString()));
                        }

                    } else if (length == 1) {
                        // 11-bit number, number of sub-packets immediately contained
                        int packets = bin2int(binary.substring(pointer, pointer + 11));
                        for (int p = packets; p > 0; p--) {
                            // skip version + whatever
                            pointer += 6;

                        }
                        System.out.println(packets);
                    }
                    break;
            }
            System.out.println("Version: " + version + "; TypeId: " + typeId);

        }
    }

    private static String hex2bin(final String hex) {
        return Arrays.stream(hex.split(""))
                .map(c -> Integer.parseInt(c, 16))
                .map(Integer::toBinaryString)
                .map(c -> String.format("%4s", c).replace(' ', '0'))
                .collect(Collectors.joining());
    }

    private static int bin2int(final String binary) {
        return Integer.parseInt(binary, 2);
    }
}
