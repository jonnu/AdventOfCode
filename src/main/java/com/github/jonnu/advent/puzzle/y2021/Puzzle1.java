package com.github.jonnu.advent.puzzle.y2021;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Deque;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle1 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle1.txt")) {

            int i = 0;
            int depth;
            int increasedDepth = 0;
            int increasedDeque = 0;

            Deque<Integer> readings = new ArrayDeque<>(3);
            int sumOfPreviousReadings = -1;

            String line = reader.readLine();
            while (line != null) {

                if (readings.size() == 3) {
                    readings.removeLast();
                }

                depth = Integer.parseInt(line);
                if (!readings.isEmpty() && depth > readings.peek()) {
                    increasedDepth++;
                }

                readings.push(depth);

                int currentSum = readings.stream().reduce(0, Integer::sum);
                if (i >= 3 && currentSum > sumOfPreviousReadings) {
                    increasedDeque++;
                }

                sumOfPreviousReadings = currentSum;
                line = reader.readLine();
                i++;
            }

            System.out.println("Previous measurement depth increases: " + increasedDepth);
            System.out.println("Sliding window 3-depth sum depth increases: " + increasedDeque);
        }
    }

}
