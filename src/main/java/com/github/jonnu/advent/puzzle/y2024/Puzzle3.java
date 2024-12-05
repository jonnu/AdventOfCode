package com.github.jonnu.advent.puzzle.y2024;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle3 implements Puzzle {

    private static final Pattern MUL_PATTERN = Pattern.compile("mul\\((\\d{1,3}),(\\d{1,3})\\)");
    private static final Pattern DO_DONT_PATTERN = Pattern.compile("don't\\(\\).*?do\\(\\)|don't\\(\\).*?$", Pattern.DOTALL);

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2024/puzzle3.txt")) {

            String line = reader.readLine();
            StringBuilder input = new StringBuilder();
            while (line != null) {
                input.append(line);
                line = reader.readLine();
            }

            long part1 = parse(input.toString());
            long part2 = parse(String.join("", DO_DONT_PATTERN.split(input.toString())));

            System.out.println("Sum of uncorrupted mul instructions: " + part1);
            System.out.println("Sum of uncorrupted and enabled mul instructions: " + part2);
        }
    }

    private static long parse(final String input) {
        long result = 0;
        final Matcher matcher = MUL_PATTERN.matcher(input);
        while (matcher.find()) {
            result += Long.parseLong(matcher.group(1)) * Long.parseLong(matcher.group(2));
        }
        return result;
    }

}
