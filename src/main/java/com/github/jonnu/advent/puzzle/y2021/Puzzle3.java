package com.github.jonnu.advent.puzzle.y2021;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle3 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle3.txt")) {

            String line = reader.readLine();
            int length = line.length();
            int[] bits = new int[length];

            List<Integer> values = new ArrayList<>();

            while (line != null) {
                int value = Integer.parseInt(line, 2);
                for (int i = 0, j = length - 1; i < length; i++, j--) {
                    bits[j] += (value & (1 << i)) != 0 ? 1 : -1;
                }
                values.add(value);
                line = reader.readLine();
            }

            int gamma = 0;
            for (int i = 0, j = bits.length - 1; i < bits.length; i++, j--) {
                gamma = bits[j] >= 0 ? gamma | (1 << i) : gamma & ~(1 << i);
            }

            int epsilon = compliment(gamma);
            System.out.println("Power consumption of the submarine: " + gamma * epsilon);

            List<Integer> o2 = new ArrayList<>(values);
            List<Integer> co2 = new ArrayList<>(values);

            for (int i = 0, j = bits.length - 1; i < length; i++, j--) {
                o2 = reduce(o2, j, true);
                co2 = reduce(co2, j, false);
                if (o2.size() == 1 && co2.size() == 1) {
                    break;
                }
            }

            System.out.println("Life support rating of the submarine: " + o2.get(0) * co2.get(0));
        }
    }

    private static List<Integer> reduce(final List<Integer> integers, final int bit, final boolean largest) {
        if (integers.size() <= 1) {
            return integers;
        }
        final Map<Boolean, List<Integer>> reduction = integers.stream().collect(Collectors.partitioningBy(value -> (value & (1 << bit)) != 0));
        return reduction.get(true).size() >= reduction.get(false).size() ? reduction.get(largest) : reduction.get(!largest);
    }

    public static int compliment(final int value) {
        return switch (value) {
            case 0 -> ~value;
            case 1 -> 0;
            default -> value ^ (int) ((1L << (Integer.SIZE - Integer.numberOfLeadingZeros(value - 1))) - 1);
        };
    }

}
