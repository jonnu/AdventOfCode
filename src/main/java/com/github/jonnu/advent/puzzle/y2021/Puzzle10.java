package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle10 implements Puzzle {

    private final ResourceReader resourceReader;

    private static final List<String> OPENERS = ImmutableList.of("[", "(", "{", "<");
    private static final List<String> CLOSERS = ImmutableList.of("]", ")", "}", ">");

    private static final Map<String, Integer> SCORE_SYNTAX_ERROR = ImmutableMap.<String, Integer>builder()
            .put(")", 3)
            .put("]", 57)
            .put("}", 1197)
            .put(">", 25137)
            .build();
    private static final Map<String, Integer> SCORE_AUTOCOMPLETE = ImmutableMap.<String, Integer>builder()
            .put(")", 1)
            .put("]", 2)
            .put("}", 3)
            .put(">", 4)
            .build();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle10.txt")) {

            String line = reader.readLine();
            List<Result> results = new ArrayList<>();
            while (line != null) {
                results.add(parse(line));
                line = reader.readLine();
            }

            final long syntaxErrorScore = results.stream()
                    .filter(result -> result.getState().equals(Result.State.INVALID))
                    .mapToLong(Result::getSyntaxErrorScore)
                    .sum();

            System.out.println("Total syntax error score: " + syntaxErrorScore);

            final long middleAutocompleteScore = results.stream()
                    .filter(result -> result.getState().equals(Result.State.INCOMPLETE))
                    .map(Result::getAutocompleteScore)
                    .sorted()
                    .collect(Collectors.collectingAndThen(Collectors.toList(), scores -> scores.get(scores.size() / 2)));

            System.out.println("Middle autocomplete score: " + middleAutocompleteScore);
        }
    }

    private Result parse(final String line) {
        final Stack<String> stack = new Stack<>();
        stack.push(Character.toString(line.charAt(0)));

        for (int i = 1; i < line.length(); i++) {

            final String current = Character.toString(line.charAt(i));

            if (OPENERS.contains(current)) {
                stack.push(current);
                continue;
            }

            final String opener = stack.pop();
            if (!OPENERS.get(CLOSERS.indexOf(current)).equals(opener)) {
                return Result.createSyntaxError(CLOSERS.get(OPENERS.indexOf(opener)), current);
            }
        }

        if (stack.empty()) {
            return Result.createValidResult();
        }

        List<String> autocomplete = new ArrayList<>();
        while (!stack.empty()) {
            autocomplete.add(CLOSERS.get(OPENERS.indexOf(stack.pop())));
        }

        return Result.createIncompleteResult(autocomplete);
    }

    @Value
    @AllArgsConstructor
    private static class Result {

        private enum State {
            INCOMPLETE,
            VALID,
            INVALID
        }

        String firstIllegalCharacter;
        String expectedCharacter;
        State state;
        List<String> autocompleteSequence;

        public static Result createSyntaxError(final String expected, final String actual) {
            return new Result(actual, expected, State.INVALID, Collections.emptyList());
        }

        public static Result createValidResult() {
            return new Result(null, null, State.VALID, Collections.emptyList());
        }

        public static Result createIncompleteResult(final List<String> autocomplete) {
            return new Result(null, null, State.INCOMPLETE, autocomplete);
        }

        public long getSyntaxErrorScore() {
            return SCORE_SYNTAX_ERROR.getOrDefault(firstIllegalCharacter, 0);
        }

        public long getAutocompleteScore() {
            long score = 0;

            for (String s : autocompleteSequence) {
                score *= 5;
                score += SCORE_AUTOCOMPLETE.getOrDefault(s, 0);
            }

            return score;
        }
    }
}
