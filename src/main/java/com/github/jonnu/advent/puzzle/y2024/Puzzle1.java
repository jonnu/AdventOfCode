package com.github.jonnu.advent.puzzle.y2024;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle1 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2024/puzzle1.txt")) {

            final Queue<Integer> left = new PriorityQueue<>();
            final Queue<Integer> right = new PriorityQueue<>();

            String line = reader.readLine();
            while (line != null) {

                int[] values = Arrays.stream(line.split("\\s+"))
                        .mapToInt(Integer::parseInt)
                        .toArray();
                left.add(values[0]);
                right.add(values[1]);

                line = reader.readLine();
            }

            final List<Integer> left2 = new ArrayList<>(left);
            final Map<Integer, Long> right2 = right.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            // Part 1.
            int distance = 0;
            while (!left.isEmpty() && !right.isEmpty()) {
                int l = left.poll();
                int r = right.poll();
                distance += Math.abs(l - r);
            }

            System.out.println("Total distance: " + distance);

            // Part 2.
            AtomicLong similarity = new AtomicLong();
            left2.forEach(i -> similarity.addAndGet(right2.getOrDefault(i, 0L) * i));

            System.out.println("Similarity score: " + similarity);
        }
    }
}
