package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle4 implements Puzzle {

    private static final Pattern PATTERN = Pattern.compile("^Card\\s+(?<cardId>\\d+):\\s+(?<winners>(\\d+\\s+)+)\\|\\s+(?<selected>(\\d+\\s*)+)$", Pattern.MULTILINE);
    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle4.txt")) {

            int points = 0;
            final List<Card> cards = new ArrayList<>();

            String line = reader.readLine();
            while (line != null) {

                Matcher matcher = PATTERN.matcher(line);
                if (matcher.matches()) {

                    Card card = Card.builder()
                            .id(Integer.parseInt(matcher.group("cardId")))
                            .winners(Arrays.stream(matcher.group("winners")
                                    .split("\\s+"))
                                    .map(String::trim)
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toSet()))
                            .selected(Arrays.stream(matcher.group("selected")
                                            .split("\\s+"))
                                    .map(String::trim)
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toSet()))
                            .build();

                    cards.add(card);
                    points += card.getValue();
                }

                line = reader.readLine();
            }

            int processed = 0;
            Queue<Card> queue = new ArrayDeque<>(cards);
            while (!queue.isEmpty()) {
                Card card = queue.poll();
                processed++;
                IntStream.range(card.getId(), card.getId() + card.getWinCount())
                        .mapToObj(cards::get)
                        .forEach(queue::add);
            }

            System.out.println("Total scratchcard points: " + points);
            System.out.println("Total scratchcards processed: " + processed);
        }
    }

    @Value
    @Builder
    private static class Card {

        int id;
        Set<Integer> selected;
        Set<Integer> winners;

        public int getWinCount() {
            return Sets.intersection(selected, winners).size();
        }

        public int getValue() {
            final int matches = getWinCount();
            return matches == 0 ? 0 : (int) Math.pow(2, matches - 1);
        }
    }
}
