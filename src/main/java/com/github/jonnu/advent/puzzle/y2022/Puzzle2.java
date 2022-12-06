package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle2 implements Puzzle {

    private final ResourceReader resourceReader;

    @Getter
    @AllArgsConstructor
    enum Shape {

        ROCK(1),
        PAPER(2),
        SCISSORS(3);

        int score;

        private static final HashMap<String, Shape> SHAPE_MAPPING = new HashMap<>() {{
            put("A", ROCK);
            put("X", ROCK);
            put("B", PAPER);
            put("Y", PAPER);
            put("C", SCISSORS);
            put("Z", SCISSORS);
        }};

        public static Shape fromCharacter(final String character) {
            return SHAPE_MAPPING.get(character);
        }
    }

    @Getter
    @AllArgsConstructor
    enum Outcome {

        WIN(6),
        DRAW(3),
        LOSE(0);

        int score;

        private static final HashMap<String, Outcome> OUTCOME_MAPPING = new HashMap<>() {{
            put("X", LOSE);
            put("Y", DRAW);
            put("Z", WIN);
        }};

        public static Outcome fromCharacter(final String character) {
            return OUTCOME_MAPPING.get(character);
        }
    }

    private static final Map<String, Integer> MEMOIZE = new HashMap<>();
    private static final Map<String, Integer> MEMOIZE2 = new HashMap<>();

    private static final BiMap<Shape, Shape> BEATS = ImmutableBiMap.<Shape, Shape>builder()
            .put(Shape.ROCK, Shape.SCISSORS)
            .put(Shape.PAPER, Shape.ROCK)
            .put(Shape.SCISSORS, Shape.PAPER)
            .build();

    private int computeOutcomePair(final String left, final String right) {
        final Shape opponent = Shape.fromCharacter(left);
        final Outcome outcome = Outcome.fromCharacter(right);

        return MEMOIZE2.computeIfAbsent(left + right, s -> {
            Shape you;
            switch (outcome) {
                case WIN -> you = BEATS.inverse().get(opponent);
                case LOSE -> you = BEATS.get(opponent);
                case DRAW -> you = opponent;
                default -> throw new RuntimeException("!");
            }
            return you.getScore() + outcome.getScore();
        });
    }

    private int computeScorePair(String left, String right) {
        Shape opponent = Shape.fromCharacter(left);
        Shape you = Shape.fromCharacter(right);

        return MEMOIZE.computeIfAbsent(left + right, s -> {
            int score = you.getScore();

            // draw
            if (opponent == you) {
                score += 3;
                return score;
            }

            score += BEATS.get(you).equals(opponent) ? 6 : 0;
            return score;
        });
    }

    @Override
    @SneakyThrows
    public void solve() {

        BufferedReader reader = resourceReader.read("y2022/puzzle2.txt");
        String line = reader.readLine();

        int score = 0;
        int score2 = 0;
        while (line != null) {
            String[] pieces = line.split("\\s+");
            score += computeScorePair(pieces[0], pieces[1]);
            score2 += computeOutcomePair(pieces[0], pieces[1]);
            line = reader.readLine();
        }
        reader.close();

        System.out.println("Total score: " + score);
        System.out.println("Strategic score: " + score2);
    }

}
