package com.github.jonnu.advent.puzzle.y2023;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle9 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle9.txt")) {

            final List<List<Long>> data = new ArrayList<>();

            String line = reader.readLine();
            while (line != null) {

                data.add(Arrays.stream(line.split("\\s+"))
                        .filter(str -> !str.isBlank())
                        .map(Long::valueOf)
                        .collect(Collectors.toList()));

                line = reader.readLine();
            }

            long i = 0;
            long k = 0;
            for (List<Long> in : data) {
                long d = findNextValue(in);
                long j = findPrevValue(in);
                i += d;
                k += j;
            }

            System.out.println("Sum of extrapolated values: " + i);
            System.out.println("Sum of backwards extrapolation: " + k);
        }
    }

    private static long findPrevValue(final List<Long> input) {
        List<Long> values = LongStream.range(0, input.size() - 1)
                .mapToObj(i -> input.get(1 + (int) i) - input.get((int) i))
                .collect(Collectors.toList());
        if (values.stream().allMatch(d -> d == 0)) {
            return input.getFirst();
        } else {
            return input.getFirst() + (-1 * findPrevValue(values));
        }
    }

    private static long findNextValue(final List<Long> input) {
        List<Long> values = LongStream.range(0, input.size() - 1)
                .mapToObj(i -> input.get(1 + (int) i) - input.get((int) i))
                .collect(Collectors.toList());

        if (values.stream().allMatch(d -> d == 0)) {
            return input.getLast();
        } else {
            return input.getLast() + findNextValue(values);
        }
    }

}
