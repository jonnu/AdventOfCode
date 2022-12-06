package com.github.jonnu.advent.puzzle;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle5 implements Puzzle {

    private static final String SPACE_PATTERN = " ";
    private static final Pattern CRATE_PATTERN = Pattern.compile("[\\[\\s]([A-Z\\s])[\\s\\]]\\s?");

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        BufferedReader reader = resourceReader.read("puzzle5.txt");
        String line = reader.readLine();

        PuzzleReadState state = PuzzleReadState.BUILD_CRATES;
        final List<LinkedList<String>> crates9000 = new ArrayList<>();
        final List<LinkedList<String>> crates9001 = new ArrayList<>();

        while (line != null) {

            if (line.isEmpty() && state.equals(PuzzleReadState.BUILD_CRATES)) {
                state = PuzzleReadState.MOVE_CRATES;
            }

            switch (state) {
                case BUILD_CRATES -> {

                    final String[] matches = CRATE_PATTERN.matcher(line)
                            .results()
                            .map(r -> r.group(1))
                            .toArray(String[]::new);

                    for (int i = 0; i < matches.length; i++) {

                        if (crates9000.size() < i + 1) {
                            crates9000.add(new LinkedList<>());
                            crates9001.add(new LinkedList<>());
                        }

                        if (Objects.equals(matches[i], SPACE_PATTERN)) {
                            continue;
                        }

                        crates9000.get(i).add(matches[i]);
                        crates9001.get(i).add(matches[i]);
                    }
                }
                case MOVE_CRATES -> {

                    final String[] bits = line.split(SPACE_PATTERN);
                    if (bits.length != 6) {
                        break;
                    }

                    moveCratesUsingMover9000(crates9000, Integer.parseInt(bits[1]), Integer.parseInt(bits[3]), Integer.parseInt(bits[5]));
                    moveCratesUsingMover9001(crates9001, Integer.parseInt(bits[1]), Integer.parseInt(bits[3]), Integer.parseInt(bits[5]));
                }
            }

            line = reader.readLine();
        }

        reader.close();

        System.out.println("Topmost crates after rearranging with CrateMover9000: " + getTopmostCrates(crates9000));
        System.out.println("Topmost crates after rearranging with CrateMover9001: " + getTopmostCrates(crates9001));
    }

    private static void moveCratesUsingMover9000(final List<LinkedList<String>> crates, final int amount, final int moveFrom, final int moveTo) {
        for (int i = 0; i < amount; i++) {
            crates.get(moveTo - 1).push(crates.get(moveFrom - 1).pop());
        }
    }

    private static void moveCratesUsingMover9001(final List<LinkedList<String>> crates, final int amount, final int moveFrom, final int moveTo) {
        final List<String> crateStack = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            crateStack.add(crates.get(moveFrom - 1).pop());
        }
        for (int j = crateStack.size(); j > 0; j--) {
            crates.get(moveTo - 1).push(crateStack.get(j - 1));
        }
    }

    private static String getTopmostCrates(final List<LinkedList<String>> crates) {
        return crates.stream().map(Deque::peek).collect(Collectors.joining());
    }

    private static void printCratesState(final List<LinkedList<String>> crates) {

        final int tallest = crates.stream()
                .max(Comparator.comparingInt(List::size))
                .map(List::size)
                .orElse(0);

        for (int r = tallest; r > 0; r--) {
            for (int c = 0; c < crates.size(); c++) {
                System.out.print(crates.get(c).size() >= r ? "[" + crates.get(c).get(crates.get(c).size() - r) + "]" : "   ");
                if (c != crates.size() - 1) {
                    System.out.print(" ");
                }
            }
            System.out.print("\n");
        }

        System.out.println(IntStream.rangeClosed(1, crates.size())
                .mapToObj(String::valueOf)
                .map(position -> " " + position + "  ")
                .collect(Collectors.joining()));
    }

    private enum PuzzleReadState {
        BUILD_CRATES,
        MOVE_CRATES
    }
}
