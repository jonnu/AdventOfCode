package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle14 implements Puzzle {

    private static final int MAXIMUM_STEPS = 40;
    private static final int PART_ONE_STEPS = 10;

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle14.txt")) {

            Map<String, Long> pairs = new HashMap<>();
            final Map<String, String> polymers = new HashMap<>();

            String template = reader.readLine();
            reader.readLine();

            String line = reader.readLine();

            while (line != null) {
                polymers.put(line.substring(0, 2), Character.toString(line.charAt(6)));
                line = reader.readLine();
            }

            for (int cl = 0, cr = 2; cr <= template.length(); cl++, cr++) {
                pairs.merge(template.substring(cl, cr), 1L, Long::sum);
            }

            final Map<String, Long> chars = template.chars()
                    .boxed()
                    .map(Character::toString)
                    .collect(Collectors.toMap(Function.identity(), x-> 1L, Long::sum));

            for (int s = 1; s <= MAXIMUM_STEPS; s++) {

                final Map<String, Long> polys = new HashMap<>();
                for (Map.Entry<String, Long> entry : pairs.entrySet()) {
                    final String polymer = entry.getKey();
                    final long occurrences = entry.getValue();
                    final char leftElement = polymer.charAt(0);
                    final char rightElement = polymer.charAt(1);
                    polys.merge(leftElement + polymers.get(polymer), occurrences, Long::sum);
                    polys.merge(polymers.get(polymer) + rightElement, occurrences, Long::sum);
                    chars.merge(polymers.get(polymer), occurrences, Long::sum);
                }

                pairs = polys;

                if (s == PART_ONE_STEPS) {
                    System.out.printf("Difference in occurrences between most and least common element (%d steps): %d%n",
                            PART_ONE_STEPS, getMinAndMaxOccurrenceDelta(chars));
                }
            }

            System.out.printf("Difference in occurrences between most and least common element (%d steps): %d%n",
                    MAXIMUM_STEPS, getMinAndMaxOccurrenceDelta(chars));
        }
    }

    private static <K> long getMinAndMaxOccurrenceDelta(final Map<K, Long> map) {
        final K minElement = Collections.min(map.entrySet(), Map.Entry.comparingByValue()).getKey();
        final K maxElement = Collections.max(map.entrySet(), Map.Entry.comparingByValue()).getKey();
        return map.get(maxElement) - map.get(minElement);
    }

}
