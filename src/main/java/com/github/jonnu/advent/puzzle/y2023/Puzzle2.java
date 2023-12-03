package com.github.jonnu.advent.puzzle.y2023;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle2 implements Puzzle {

    private static final Pattern GAME_PATTERN = Pattern.compile("^Game (?<gameId>\\d+):\\s(?<gameData>.*)$", Pattern.MULTILINE);
    private static final Pattern ROUND_PATTERN = Pattern.compile("(\\d+)\\s(red|green|blue)");

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle2.txt")) {

            final Set<Combination> constraints = ImmutableSet.of(
                    Combination.builder().count(12).color(Color.RED).build(),
                    Combination.builder().count(14).color(Color.BLUE).build(),
                    Combination.builder().count(13).color(Color.GREEN).build()
            );

            int gameIdSum = 0;
            long gameSumOfPowers = 0;

            String line = reader.readLine();
            while (line != null) {

                Matcher matcher = GAME_PATTERN.matcher(line);
                if (matcher.matches()) {
                    int gameId = Integer.parseInt(matcher.group("gameId"));

                    Game game = Game.builder()
                            .id(gameId)
                            .rounds(Arrays.stream(matcher.group("gameData").split(";"))
                                    .map(Puzzle2::toRound)
                                    .collect(Collectors.toSet()))
                            .build();

                    if (game.possibleWith(constraints)) {
                        gameIdSum += game.getId();
                    }

                    gameSumOfPowers += game.fewestCubesPower();
                }

                line = reader.readLine();
            }

            System.out.println("Sum of possible game identifiers: " + gameIdSum);
            System.out.println("Sum of power of game sets: " + gameSumOfPowers);
        }
    }

    private static Round toRound(final String input) {

        final Matcher matcher = ROUND_PATTERN.matcher(input.trim());
        final HashSet<Combination> outcomes = new HashSet<>();

        while (matcher.find()) {
            outcomes.add(Combination.builder()
                    .count(Integer.parseInt(matcher.group(1)))
                    .color(Enum.valueOf(Color.class, matcher.group(2).toUpperCase()))
                    .build());
        }

        return Round.builder()
                .outcomes(outcomes)
                .build();
    }

    @Value
    @Builder
    private static class Game {
        int id;
        Set<Round> rounds;

        public boolean possibleWith(final Set<Combination> constraints) {
            return rounds.stream().allMatch(round -> round.possibleWith(constraints));
        }

        public long fewestCubesPower() {
            return rounds.stream()
                    .map(Round::getOutcomes)
                    .flatMap(Set::stream)
                    .collect(Collectors.toMap(Combination::getColor, Combination::getCount, BinaryOperator.maxBy(Integer::compareTo)))
                    // multiply maximums.
                    .values()
                    .stream()
                    .mapToLong(Long::valueOf)
                    .reduce(1, Math::multiplyExact);
        }
    }

    @Value
    @Builder
    private static class Round {
        Set<Combination> outcomes;

        public boolean possibleWith(final Set<Combination> constraints) {
            return constraints.stream().map(constraint -> outcomes.stream()
                    .filter(outcome -> outcome.getColor().equals(constraint.getColor()))
                    .allMatch(outcome -> outcome.getCount() <= constraint.getCount()))
                    .reduce(Boolean::logicalAnd)
                    .orElse(false);
        }
    }

    @Value
    @Builder
    private static class Combination {
        int count;
        Color color;
    }

    enum Color {
        RED,
        GREEN,
        BLUE
    }
}
