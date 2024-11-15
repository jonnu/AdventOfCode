package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle7 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle7.txt")) {

            boolean useJokerLogic = true;

            PriorityQueue<Hand> hands = new PriorityQueue<>(new HandComparator(useJokerLogic));
            String line = reader.readLine();
            while (line != null) {
                hands.add(Hand.parse(line, useJokerLogic));
                line = reader.readLine();
            }

            int rank = 1;
            long winnings = 0;
            while (!hands.isEmpty()) {
                final Hand hand = hands.poll();
                winnings += rank * hand.getBid();
                log.debug("{} {}", rank, hand);
                rank++;
            }

            System.out.println("\nTotal winnings: " + winnings);
        }
    }

    @Setter
    @AllArgsConstructor
    static class HandComparator implements Comparator<Hand> {

        private final boolean useJokerLogic;

        private static final List<BiPredicate<Hand, Boolean>> ORDERED_HAND_PREDICATES = ImmutableList.of(
                isSameOfAKind(5),   // Five of a Kind
                isSameOfAKind(4),   // Four of a Kind
                isFullHouse(),              // Full House
                isSameOfAKind(3),   // Three of a Kind
                isTwoPair(),                // Two Pair
                isSameOfAKind(2)    // Two of a Kind
        );

        @Override
        public int compare(final Hand left, final Hand right) {

            for (BiPredicate<Hand, Boolean> handPredicate : ORDERED_HAND_PREDICATES) {

                final boolean l = handPredicate.test(left, useJokerLogic);
                final boolean r = handPredicate.test(right, useJokerLogic);

                if (l && r) {
                    return compareCards(left, right);
                }

                if (!l && !r) {
                    continue;
                }

                return l ? 1 : -1;
            }

            return compareCards(left, right);
        }

        public int compareCards(final Hand left, final Hand right) {

            for (int i = 0; i < 5; i++) {
                int weight = left.getCard(i).compareTo(right.getCard(i));
                if (weight != 0) {
                    return weight > 0 ? 1 : -1;
                }
            }

            return 0;
        }
    }

    @Value
    @Builder
    static class Hand {

        long bid;
        @Singular List<Card> cards;

        public Card getCard(final int i) {
            return cards.get(i);
        }

        public Map<Card, Long> grouped() {
            return cards.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        }

        public Map.Entry<Card, Long> most(final Predicate<Map.Entry<Card, Long>> filter) {
            return Collections.max(grouped().entrySet()
                    .stream()
                    .filter(entry -> !Card.JOKER.equals(entry.getKey()))
                    .filter(filter)
                    .collect(Collectors.toSet()), Map.Entry.<Card, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey()));
        }

        @Override
        public String toString() {
            return cards.stream()
                    .map(Card::getSymbol)
                    .collect(Collectors.joining());
        }

        public static Hand parse(final String input, final boolean useJokerLogic) {
            return Hand.builder()
                    .cards(input.substring(0, 5)
                            .chars()
                            .mapToObj(card -> Card.parse(card, useJokerLogic))
                            .collect(Collectors.toList()))
                    .bid(Long.parseLong(input.substring(6)))
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    enum Card implements Comparable<Card> {

        JOKER("J"),
        TWO("2"),
        THREE("3"),
        FOUR("4"),
        FIVE("5"),
        SIX("6"),
        SEVEN("7"),
        EIGHT("8"),
        NINE("9"),
        TEN("T"),
        JACK("J"),
        QUEEN("Q"),
        KING("K"),
        ACE("A")
        ;

        private final String symbol;

        public static Card parse(final int input, boolean part2) {
            return parse(String.valueOf((char) input), part2);
        }

        public static Card parse(final String input, boolean part2) {
            return Arrays.stream(values())
                    .filter(card -> !card.equals(part2 ? Card.JACK : Card.JOKER))
                    .filter(card -> input.equalsIgnoreCase(card.getSymbol()))
                    .findFirst()
                    .orElseThrow();
        }
    }

    private static BiPredicate<Hand, Boolean> isSameOfAKind(long number) {
        return (hand, useJokerLogic) -> {
            // @TODO - should probably perform the grouping with joker awareness. otherwise we will need to keep mutating a copy.
            final Map<Card, Long> cardCounts = hand.grouped();
            final Map<Card, Long> copyCounts = new HashMap<>(cardCounts);
            final long jokerCount = !useJokerLogic ? 0 : Optional.ofNullable(cardCounts.get(Card.JOKER)).orElse(0L);
            copyCounts.remove(Card.JOKER);
            return jokerCount == number || copyCounts.containsValue(number - jokerCount);
        };
    }

    private static BiPredicate<Hand, Boolean> isFullHouse() {
        return (hand, useJokerLogic) -> {
            final Map<Card, Long> cardCounts = hand.grouped();
            final Map<Card, Long> copyCounts = new HashMap<>(cardCounts);
            final long jokerCount = !useJokerLogic ? 0 : Optional.ofNullable(cardCounts.get(Card.JOKER)).orElse(0L);
            copyCounts.compute(hand.most(entry -> entry.getValue() + jokerCount <= 3L).getKey(),
                    (card, count) -> Optional.ofNullable(count).orElse(0L) + jokerCount);
            copyCounts.remove(Card.JOKER);
            return copyCounts.containsValue(2L) && copyCounts.containsValue(3L);
        };
    }

    private static BiPredicate<Hand, Boolean> isTwoPair() {
        return (hand, useJokerLogic) -> {
            final Map<Card, Long> cardCounts = hand.grouped();
            final long jokerCount = !useJokerLogic ? 0 : Optional.ofNullable(cardCounts.get(Card.JOKER)).orElse(0L);
            return jokerCount == 1 && cardCounts.containsValue(2L) || hand.grouped().values().stream().filter(count -> count == 2).count() == 2;
        };
    }

}
