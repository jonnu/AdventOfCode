package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle5 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2025/puzzle5.txt")) {

            InputMode inputMode = InputMode.FRESH_RANGE;
            List<Pair<BigInteger>> ranges = new ArrayList<>();
            List<BigInteger> ingredients = new ArrayList<>();

            String line = reader.readLine();
            while (line != null) {
                if (line.isEmpty()) {
                    inputMode = InputMode.INGREDIENT;
                    line = reader.readLine();
                    continue;
                }
                switch (inputMode) {
                    case FRESH_RANGE -> ranges.add(new Pair<>(new BigInteger(line.split("-")[0]), new BigInteger(line.split("-")[1])));
                    case INGREDIENT -> ingredients.add(new BigInteger(line));
                }
                line = reader.readLine();
            }

            int fresh = 0;
            for (BigInteger ingredient : ingredients) {
                for (Pair<BigInteger> range : ranges) {
                    if (range.inRange(ingredient)) {
                        fresh++;
                        break;
                    }
                }
            }

            List<Pair<BigInteger>> simplified = simplify(ranges);
            BigInteger count = BigInteger.ZERO;
            for (Pair<BigInteger> range : simplified) {
                // the +1 is necessary to make the range inclusive.
                count = count.add(range.right().subtract(range.left())).add(BigInteger.ONE);
            }

            System.out.println("[Part 1] Fresh ingredients available: " + fresh);
            System.out.println("[Part 2] Number of possible fresh ingredients: " + count);
        }
    }

    enum InputMode {
        FRESH_RANGE,
        INGREDIENT
    }

    /**
     * given ranges that may overlap, 'simplify' them by merging those that do into a single range.
     */
    private static <T extends Comparable<T>> List<Pair<T>> simplify(final List<Pair<T>> ranges) {
        ranges.sort(Comparator.comparing(Pair::left));
        List<Pair<T>> pairs = new ArrayList<>(ranges);
        for (int i = 0; i < pairs.size(); i++) {
            for (int j = 0; j < pairs.size(); j++) {
                if (pairs.get(i) == pairs.get(j)) {
                    continue;
                }
                if (pairs.get(i).intersect(pairs.get(j))) {
                    pairs.set(i, pairs.get(i).combine(pairs.get(j)));
                    pairs.remove(j);
                }
            }
        }

        // Keep simplifying until nothing move can be removed.
        return ranges.size() == pairs.size() ? pairs : simplify(pairs);
    }

    record Pair<T extends Comparable<T>>(T left, T right) {

        boolean inRange(final T input) {
            return input.compareTo(left) >= 0 && input.compareTo(right) <= 0;
        }

        boolean intersect(final Pair<T> input) {
            boolean leftIntersect = input.left().compareTo(left) >= 0 && input.left().compareTo(right) <= 0;
            boolean rightIntersect = input.right().compareTo(right) <= 0 && input.right().compareTo(left) >= 0;
            return leftIntersect || rightIntersect;
        }

        Pair<T> combine(final Pair<T> input) {
            if (!intersect(input)) {
                return this;
            }
            T newLeft = input.left().compareTo(left) <= 0 ? input.left() : left;
            T newRight = input.right().compareTo(right) >= 0 ? input.right : right;
            return new Pair<>(newLeft, newRight);
        }
    }
}
