package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle1 implements Puzzle {

    private static final Map<String, Integer> DIGIT_WORDS = ImmutableMap.<String, Integer>builder()
            .put("one", 1)
            .put("two", 2)
            .put("three", 3)
            .put("four", 4)
            .put("five", 5)
            .put("six", 6)
            .put("seven", 7)
            .put("eight", 8)
            .put("nine", 9)
            .build();

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle1.txt")) {

            int result = 0;
            String line = reader.readLine();
            while (line != null) {
                result += alphanumericCalibrationValue(line);
                line = reader.readLine();
            }

            System.out.println("Sum of calibration values: " + result);
        }
    }

    private static int alphanumericCalibrationValue(final String input) {

        // create dictionary with both string and digit representations of 1-9.
        final Map<String, Integer> compositeDigitMap = new HashMap<>();
        compositeDigitMap.putAll(DIGIT_WORDS);
        compositeDigitMap.putAll(DIGIT_WORDS.entrySet()
                .stream()
                .collect(Collectors.toMap(digit -> String.valueOf(digit.getValue()), Map.Entry::getValue)));

        return (firstOccurrence(input, compositeDigitMap) * 10) + lastOccurrence(input, compositeDigitMap);
    }

    private static int firstOccurrence(final String input, final Map<String, Integer> dictionary) {
        for (int i = 0, j = 1; j <= input.length(); j++) {
            OptionalInt digit = findDigitMatch(input.substring(i, j), dictionary);
            if (digit.isPresent()) {
                return digit.getAsInt();
            }
        }

        throw new IllegalArgumentException("First occurrence not found within " + input);
    }

    private static int lastOccurrence(final String input, final Map<String, Integer> dictionary) {
        for (int i = input.length(), j = input.length() - 1; j >= 0; j--) {
            OptionalInt digit = findDigitMatch(input.substring(j, i), dictionary);
            if (digit.isPresent()) {
                return digit.getAsInt();
            }
        }

        throw new IllegalArgumentException("Last occurrence not found within " + input);
    }

    private static OptionalInt findDigitMatch(final String input, final Map<String, Integer> dictionary) {
        return dictionary.entrySet().stream().filter(key -> input.contains(key.getKey())).mapToInt(Map.Entry::getValue).findFirst();
    }
}
