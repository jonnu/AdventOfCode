package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle10 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        List<Machine> machines = new ArrayList<>();
        try (BufferedReader reader = resourceReader.read("y2025/puzzle10.txt")) {
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(" ");
                List<Boolean> goal = parts[0].substring(1, parts[0].length() - 1)
                        .chars()
                        .mapToObj(x -> x == 35)
                        .toList();
                int[] requirement = new int[0];
                int[][] buttons = new int[parts.length - 2][];
                for (int i = 1; i < parts.length; i++) {
                    boolean requirements = i == parts.length - 1;
                    int[] values = Arrays.stream(parts[i].substring(1, parts[i].length() - 1)
                            .split(","))
                            .mapToInt(Integer::valueOf)
                            .toArray();
                    if (requirements) {
                        requirement = values;
                        break;
                    }
                    buttons[i - 1] = values;
                }
                machines.add(new Machine(goal, buttons, requirement));
                line = reader.readLine();
            }
        }

        System.out.println(machines.get(0));
        machines.get(0).push(0);
        System.out.println(machines.get(0));
        machines.get(0).push(1);
        System.out.println(machines.get(0));
        machines.get(0).push(2);
        System.out.println(machines.get(0));

//        System.out.println("[Part 1] " + machines);
    }

    @Getter
    @ToString
    static class Machine {
        private int presses = 0;
        private final boolean[] state;
        private final boolean[] goal;
        private final int[][] buttons;
        private final int[] joltage;

        public Machine(final List<Boolean> goal, int[][] buttons, int[] joltage) {
            this.goal = new boolean[goal.size()];
            for (int i = 0; i < goal.size(); i++) {
                this.goal[i] = goal.get(i);
            }
            this.state = new boolean[goal.size()];
            this.buttons = buttons;
            this.joltage = joltage;

            calculate();
        }

        public void push(final int index) {
            int[] button = buttons[index];
            //System.out.println("pressing index: " + index + " (" + Arrays.toString(button) + ")");
            for (int j : button) {
                state[j] = !state[j];
            }
            presses++;
        }

        private void calculate() {
            final int possibilities = (int) Math.pow(2, buttons.length);

            Map<List<Boolean>, Integer> lightPatternToNrPresses;
            Map<List<Integer>, Integer> joltageIncrToNrPresses;
            lightPatternToNrPresses = new HashMap<>();
            joltageIncrToNrPresses = new HashMap<>();

            for (int i = 0; i < possibilities; i++) {
                final List<Boolean> lightPattern = new ArrayList<>();
                final List<Integer> joltageIncreases = new ArrayList<>();
                //IntStream.range(0, joltageRequirements.size()).forEach(j -> lightPattern.add(false));
                //IntStream.range(0, joltage.length).forEach(j -> joltageIncreases.add(0));

                System.out.println(i + ": " + String.format("%" + 10 + "s", Integer.toBinaryString(i)));
                //final String binary = getBinaryRepresentation(i);


                //int value = Integer.parseInt(line, 2);
                //for (int i = 0, j = length - 1; i < length; i++, j--) {
                //    bits[j] += (value & (1 << i)) != 0 ? 1 : -1;
                //}

                int nrButtonsPressed = 0;
                // Press all the buttons
//                for (int buttonNr = 0; buttonNr < binary.length(); buttonNr++) {
//                    if (binary.charAt(buttonNr) == '1') {
//                        // Get the meters which are influenced by this button.
//                        final Boolean[] influence = buttonInfluences[buttonNr];
//
//                        for (int k = 0; k < influence.length; k++) {
//                            lightPattern.set(k, influence[k] && lightPattern.get(k) ? false
//                                    : influence[k] && !lightPattern.get(k) ? true : lightPattern.get(k));
//
//                            joltageIncreases.set(k, influence[k] ? joltageIncreases.get(k) + 1 : joltageIncreases.get(k));
//                        }
//                        nrButtonsPressed++;
//                    }
//                }

                // Add the pattern/joltage increases if the map does not contain the pattern yet
                // or replace it when it has a button press count which is greater than the one
                // we just calculated.
                if (!lightPatternToNrPresses.containsKey(lightPattern)
                        || lightPatternToNrPresses.get(lightPattern) > nrButtonsPressed) {
                    lightPatternToNrPresses.put(lightPattern, nrButtonsPressed);
                }
                if (!joltageIncrToNrPresses.containsKey(joltageIncreases)
                        || joltageIncrToNrPresses.get(joltageIncreases) > nrButtonsPressed) {
                    joltageIncrToNrPresses.put(joltageIncreases, nrButtonsPressed);
                }
            }
        }

        public boolean on() {
            return state == goal;
        }

        public Machine copy() {
            List<Boolean> g = new ArrayList<>();
            for (boolean b : goal) {
                g.add(b);
            }
            return new Machine(g, buttons, joltage);
        }
    }
}
