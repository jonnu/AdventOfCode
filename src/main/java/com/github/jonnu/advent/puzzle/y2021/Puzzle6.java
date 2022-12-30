package com.github.jonnu.advent.puzzle.y2021;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle6 implements Puzzle {

    private final ResourceReader resourceReader;
    private final List<Integer> interestingDays = ImmutableList.of(80, 256);

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle6.txt")) {

            Map<Integer, Long> school = Arrays.stream(reader.readLine().split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            for (int days = 1, interesting = 0; days <= interestingDays.get(interestingDays.size() - 1); days++) {

                // age all lantern fish
                school = school.entrySet().stream().collect(Collectors.toMap(e -> e.getKey() - 1, Map.Entry::getValue, Long::sum));

                // add new fish for those below zero, and set existing fish to 6
                Long respawn = school.remove(-1);
                if (respawn != null) {
                    school.compute(6, increaseBy(respawn));
                    school.compute(8, increaseBy(respawn));
                }

                if (days == interestingDays.get(interesting)) {
                    long fish = school.values().stream().mapToLong(x -> x).sum();
                    System.out.println("LanternFish within the school (day " + days + "): " + fish);
                    interesting++;
                }
            }
        }
    }

    private static BiFunction<Integer, Long, Long> increaseBy(final long increase) {
        return (key, value) -> (value == null ? 0 : value) + increase;
    }
}
