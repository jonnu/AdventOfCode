package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
<<<<<<< Updated upstream
=======
import java.util.Comparator;
>>>>>>> Stashed changes
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
<<<<<<< Updated upstream
=======
import java.util.function.BiPredicate;
>>>>>>> Stashed changes
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
<<<<<<< Updated upstream
=======
import com.google.common.collect.ImmutableList;
>>>>>>> Stashed changes
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
<<<<<<< Updated upstream
=======
import lombok.Setter;
>>>>>>> Stashed changes
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle7 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle7.txt")) {

<<<<<<< Updated upstream
            PriorityQueue<Hand> hands = new PriorityQueue<>(Hand::compareTo);
            String line = reader.readLine();
            while (line != null) {
                hands.add(Hand.parse(line));
=======
            boolean useJokerLogic = true;
            PriorityQueue<Hand> hands = new PriorityQueue<>(Hand::compareTo);
            String line = reader.readLine();
            while (line != null) {
                hands.add(Hand.parse(line, useJokerLogic));
>>>>>>> Stashed changes
                line = reader.readLine();
            }

            int rank = 1;
            long winnings = 0;
            while (!hands.isEmpty()) {
                final Hand hand = hands.poll();
                winnings += rank * hand.getBid();
                System.out.println(rank + " " + hand);
                rank++;
            }

            System.out.println("\nTotal winnings: " + winnings);
        }
    }

<<<<<<< Updated upstream
=======
    @Setter
    public static class HandComparator implements Comparator<Hand> {

        boolean useJokerLogic;

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

            for (Predicate<Hand> handPredicate : ORDERED_HAND_PREDICATES) {

                boolean l = handPredicate.test(left);
                boolean r = handPredicate.test(right);

                //System.out.println(i + " L (" + this + "): " + l + "; R (" + other + "): " + r);

                if (l && r) {
                    return compareCards(other);
                }

                if (!l && !r) {
                    continue;
                }

                return l ? 1 : -1;
            }

            return compareCards(other);



        }
    }
>>>>>>> Stashed changes

    @Value
    @Builder
    private static class Hand implements Comparable<Hand> {

        long bid;
        @Singular List<Card> cards;

        public Map<Card, Long> grouped() {
            return cards.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        }

<<<<<<< Updated upstream
        public Map.Entry<Card, Long> most() {
            return most(x -> true);
        }

        public Map.Entry<Card, Long> most(Predicate<Map.Entry<Card, Long>> filter) {
=======
        public Map.Entry<Card, Long> most(final Predicate<Map.Entry<Card, Long>> filter) {
>>>>>>> Stashed changes
            return Collections.max(grouped().entrySet()
                    .stream()
                    .filter(entry -> !Card.JOKER.equals(entry.getKey()))
                    .filter(filter)
                    .collect(Collectors.toSet()), Map.Entry.<Card, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey()));
        }

        public int compareCards(final Hand other) {

            for (int i = 0; i < 5; i++) {
                int weight = cards.get(i).compareTo(other.cards.get(i));
                if (weight != 0) {
                    return weight > 0 ? 1 : -1;
                }
            }

            return 0;
        }

        @Override
        public int compareTo(@NonNull final Hand other) {

            // Part one predicates.
<<<<<<< Updated upstream
            final List<Predicate<Hand>> p = new ArrayList<>();
            p.add(ha -> ha.grouped().containsValue(5L));
            p.add(ha -> ha.grouped().containsValue(4L));
            p.add(ha -> ha.grouped().containsValue(3L) && ha.grouped().containsValue(2L));
            p.add(ha -> ha.grouped().containsValue(3L));
            p.add(ha -> ha.grouped().values().stream().filter(x -> x == 2).count() == 2);
            p.add(ha -> ha.grouped().containsValue(2L));

            List<HandPredicate> pred = new ArrayList<>();
=======
//            final List<Predicate<Hand>> p = new ArrayList<>();
//            p.add(ha -> ha.grouped().containsValue(5L));
//            p.add(ha -> ha.grouped().containsValue(4L));
//            p.add(ha -> ha.grouped().containsValue(3L) && ha.grouped().containsValue(2L));
//            p.add(ha -> ha.grouped().containsValue(3L));
//            p.add(ha -> ha.grouped().values().stream().filter(x -> x == 2).count() == 2);
//            p.add(ha -> ha.grouped().containsValue(2L));

            //List<NamedPredicate<Hand>> pred = new ArrayList<>();
//            List<NamedPredicate<Hand>> predicates = ImmutableList.of(
//                    handPredicate("5 of a Kind", hand -> hand.grouped().containsValue(5L)),
//                    handPredicate("4 of a Kind", hand -> hand.grouped().containsValue(4L)),
//                    handPredicate("Full House", hand -> hand.grouped().containsValue(3L) && hand.grouped().containsValue(2L)),
//                    handPredicate("3 of a Kind", hand -> hand.grouped().containsValue(3L)),
//                    handPredicate("Two Pair", hand -> hand.grouped().values().stream().filter(count -> count == 2).count() == 2),
//                    handPredicate("Pair", hand -> hand.grouped().containsValue(2L))
//            );
>>>>>>> Stashed changes

            // this needs some serious clean-up!
            // idea of the most.predicate is to stop 2+(2xJOKER) making a 4 of a kind
            // instead of a FH. 4OAK is covered elsewhere.
            // K2JJ2 - in isolation actually this doesn't matter. discrete predicates, it does (in terms of correctness).
            // just because KKKK2 > KKK22, but this predicate doesn't know that.
            Predicate<Hand> h2_fh = hand -> {
                Map<Card, Long> grouped = hand.grouped();
                Map<Card, Long> cop = new HashMap<>(grouped);
                long jokers = Optional.ofNullable(cop.remove(Card.JOKER)).orElse(0L);
                cop.compute(hand.most(x -> x.getValue() + jokers <= 3L).getKey(), (c, v) -> Optional.ofNullable(v).orElse(0L) + jokers);
                return cop.containsValue(2L) && cop.containsValue(3L);
            };

//            assert h2_fh.test(Hand.parse("22233 10"));
//            assert h2_fh.test(Hand.parse("22J33 10"));
//            assert h2_fh.test(Hand.parse("22JJ3 10"));
//            assert h2_fh.test(Hand.parse("JJTT9 10"));
//            assert h2_fh.test(Hand.parse("KJ33K 10"));
//            assert !h2_fh.test(Hand.parse("2AJJ3 10"));
//            assert !h2_fh.test(Hand.parse("KJ23K 10"));

            Function<Integer, Predicate<Hand>> h2_n = (i) -> hand -> {
                Map<Card, Long> grouped = hand.grouped();
                long jokers = Optional.ofNullable(grouped.remove(Card.JOKER)).orElse(0L);
                return grouped.containsValue(i - jokers) || jokers == i;
            };

            //2-pair AJKJ
            // this will never outrank a 3oak.
            Predicate<Hand> h2_2oak = hand -> {
                Map<Card, Long> grouped = hand.grouped();
                long jokers = Optional.ofNullable(grouped.remove(Card.JOKER)).orElse(0L);
                return (jokers == 1 && grouped.containsValue(2L) || hand.grouped().values().stream().filter(x -> x == 2).count() == 2);
            };


<<<<<<< Updated upstream
            // part 2.
            p.clear();
            p.add(h2_n.apply(5));
            p.add(h2_n.apply(4));
            p.add(h2_fh);
            p.add(h2_n.apply(3));
            p.add(h2_2oak);
            p.add(h2_n.apply(2));

            //System.out.println("Lg: " + grouped() + "; Rg: " + other.grouped());
            for (Predicate<Hand> handPredicate : p) {
=======
            boolean useJokerLogic = true;
            List<NamedPredicate<Hand>> predicates = ImmutableList.of(
                sameOfAKind(5, useJokerLogic, "Five of a Kind"),
                sameOfAKind(4, useJokerLogic, "Four of a Kind"),
                fullHouse(useJokerLogic),
                sameOfAKind(3, useJokerLogic, "Three of a Kind"),
                twoPair(useJokerLogic),
                sameOfAKind(2, useJokerLogic, "Pair")
            );

//            assert sameOfAKind(4, true, "4OAK").test(Hand.parse("KTJJT 10", true));

            // part 2.
//            p.clear();
//            p.add(h2_n.apply(5));
//            p.add(h2_n.apply(4));
//            p.add(h2_fh);
//            p.add(h2_n.apply(3));
//            p.add(h2_2oak);
//            p.add(h2_n.apply(2));

            //System.out.println("Lg: " + grouped() + "; Rg: " + other.grouped());
            for (Predicate<Hand> handPredicate : predicates) {
>>>>>>> Stashed changes

                boolean l = handPredicate.test(this);
                boolean r = handPredicate.test(other);

                //System.out.println(i + " L (" + this + "): " + l + "; R (" + other + "): " + r);

                if (l && r) {
                    return compareCards(other);
                }

                if (!l && !r) {
                    continue;
                }

                return l ? 1 : -1;
            }

            return compareCards(other);
        }

        @Override
        public String toString() {
            return cards.stream()
                    .map(Card::getSymbol)
                    .collect(Collectors.joining());
        }

<<<<<<< Updated upstream
        public static Hand parse(final String input) {
            return Hand.builder()
                    .cards(input.substring(0, 5)
                            .chars()
                            .mapToObj(card -> Card.parse(card, true))
=======
        public static Hand parse(final String input, final boolean useJokerLogic) {
            return Hand.builder()
                    .cards(input.substring(0, 5)
                            .chars()
                            .mapToObj(card -> Card.parse(card, useJokerLogic))
>>>>>>> Stashed changes
                            .collect(Collectors.toList()))
                    .bid(Long.parseLong(input.substring(6)))
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    private enum Card implements Comparable<Card> {

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

<<<<<<< Updated upstream
    @Value
    @Builder
    private static class HandPredicate {
        String name;
        Predicate<Hand> predicate;
    }
=======
    private static BiPredicate<Hand, Boolean> isSameOfAKind(long number) {
        return (hand, useJokerLogic) -> {
            final Map<Card, Long> cardCounts = hand.grouped();
            final long jokerCount = !useJokerLogic ? 0 : Optional.ofNullable(cardCounts.get(Card.JOKER)).orElse(0L);
            return jokerCount == number || cardCounts.containsValue(number - jokerCount);
        };
    }

    private static BiPredicate<Hand, Boolean> isFullHouse() {
        return (hand, useJokerLogic) -> {
            final Map<Card, Long> cardCounts = hand.grouped();
            final Map<Card, Long> copyCounts = new HashMap<>(cardCounts);
            final long jokerCount = !useJokerLogic ? 0 : Optional.ofNullable(cardCounts.get(Card.JOKER)).orElse(0L);
            copyCounts.compute(hand.most(entry -> entry.getValue() + jokerCount <= 3L).getKey(),
                    (card, count) -> Optional.ofNullable(count).orElse(0L) + jokerCount);
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

>>>>>>> Stashed changes
}
