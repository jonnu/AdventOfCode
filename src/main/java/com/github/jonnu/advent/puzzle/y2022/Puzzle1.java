package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle1 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        BufferedReader reader = resourceReader.read("y2022/puzzle1.txt");
        String line = reader.readLine();

        PriorityQueue<Elf> elves = new PriorityQueue<>(Comparator.comparingInt(Elf::getCarriedCalories).reversed());
        Set<Integer> calories = new HashSet<>();

        while (line != null) {

            if (line.length() == 0) {
                elves.add(Elf.builder()
                        .foodItems(calories)
                        .build());
                calories = new HashSet<>();
                line = reader.readLine();
                continue;
            }

            calories.add(Integer.valueOf(line));
            line = reader.readLine();
        }

        reader.close();

        Elf mostCalorificElf = elves.poll();
        System.out.println("Elf carrying the most Calories: " + mostCalorificElf.getCarriedCalories());
        System.out.println("Top 3 elves by calories summed: " + (mostCalorificElf.getCarriedCalories() + elves.poll().getCarriedCalories() + elves.poll().getCarriedCalories()));
    }

    @Value
    @Builder
    private static class Elf {
        Set<Integer> foodItems;
        public int getCarriedCalories() {
            return foodItems.stream().reduce(0, Integer::sum);
        }
    }
}
