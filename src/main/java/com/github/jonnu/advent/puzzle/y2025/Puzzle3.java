package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle3 implements Puzzle {

    private static final int PART1_JOLT = 2;
    private static final int PART2_JOLT = 12;

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2025/puzzle3.txt")) {

            long answer1 = 0;
            long answer2 = 0;

            String line = reader.readLine();
            while (line != null) {
                answer1 += largestEmbedded(line, PART1_JOLT);
                answer2 += largestEmbedded(line, PART2_JOLT);
                line = reader.readLine();
            }

            System.out.println("Total output joltage (part 1): " + answer1);
            System.out.println("Total output joltage (part 2): " + answer2);
        }
    }

    private static long largestEmbedded(final String string, final int length) {

        int[] values = string.chars().map(x -> x - 48).toArray();

        int index = 0;
        long answer = 0;

        for (long j = 1, pow = (long) Math.pow(10, length - 1); j <= length; j++, pow /= 10) {
            long largest = -1;
            for (int i = index; i < values.length - (length - j); i++) {

                if (values[i] > largest) {
                    largest = values[i];
                    index = i + 1;
                }

                if (largest == 9) {
                    break;
                }
            }

            answer += largest * pow;
        }

        return answer;
    }
}
