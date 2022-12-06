package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.base.Ascii;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle3 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        BufferedReader reader = resourceReader.read("y2022/puzzle3.txt");
        String line = reader.readLine();

        int count = 1;
        int sumOfGroupPriorities = 0;
        Map<Character, Integer> groupRucksack = new HashMap<>();

        List<Character> common = new ArrayList<>();
        while (line != null) {

            Set<Character> allSack = new HashSet<>();
            Set<Character> leftSack = new HashSet<>();
            Set<Character> commonSack = new HashSet<>();
            for (int i = 0; i < line.length(); i++) {

                boolean left = i < line.length() / 2;
                char item = line.charAt(i);
                allSack.add(item);

                if (left) {
                    leftSack.add(item);
                } else {
                    if (leftSack.contains(item)) {
                        commonSack.add(item);
                    }
                }
            }

            common.addAll(commonSack);

            if (count % 3 == 1) {
                // set groupSack elements to 1.
                groupRucksack = allSack.stream()
                        .collect(Collectors.toMap(Function.identity(), (c) -> 1));
            } else {
                // increment allSack elements by 1.
                groupRucksack = groupRucksack.entrySet()
                        .stream().filter(e -> allSack.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() + 1));
            }

            if (count % 3 == 0) {
                // groupSack with size 3 are in all 3 sacks.
                // add their priorities to the running tally.
                int blah = groupRucksack.entrySet()
                        .stream()
                        .filter(r -> r.getValue() == 3)
                        .map(Map.Entry::getKey)
                        .map(Puzzle3::getPriority)
                        .reduce(0, Integer::sum);
                sumOfGroupPriorities += blah;
            }

            line = reader.readLine();
            count++;
        }
        reader.close();

        int sumOfPriorities = common.stream()
                .map(Puzzle3::getPriority)
                .reduce(0, Integer::sum);

        System.out.println("Sum of priorities: " + sumOfPriorities);
        System.out.println("Sum of three-elf group priorities: " + sumOfGroupPriorities);
    }

    private static int getPriority(final char item) {
        return Ascii.isLowerCase(item) ? (int) item - 96 : (int) item - 38;
    }
}
