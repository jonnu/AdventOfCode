package com.github.jonnu.advent.puzzle.y2022;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.primitives.Ints;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle10 implements Puzzle {

    private static final String LIT = "▒";
    private static final String DARK = " ";
    private static final String NEWLINE = "\n";
    private static final int CRT_WIDTH = 40;

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle10.txt")) {

            final HashMap<Integer, Integer> registerChanges = new HashMap<>();

            int registerX = 1;
            int currentCycle = 0;
            int upperBoundCycle = 0;
            int[] signalStrengths = new int[6];
            int signalStrengthIndex = 0;
            final StringBuilder crt = new StringBuilder();
            final int[] observedCycles  = new int[] { 20, 60, 100, 140, 180, 220 };

            String line = reader.readLine();
            while (line != null) {

                String[] args = line.split(" ");
                switch (args[0]) {
                    case "noop" -> upperBoundCycle += 1;
                    case "addx" -> {
                        upperBoundCycle += 2;
                        registerChanges.put(currentCycle + 2, Integer.parseInt(args[1]));
                    }
                }

                for ( ; currentCycle < upperBoundCycle; currentCycle++) {

                    if (Ints.contains(observedCycles, currentCycle)) {
                        signalStrengths[signalStrengthIndex] = registerX * currentCycle;
                        signalStrengthIndex++;
                    }

                    if (registerChanges.containsKey(currentCycle)) {
                        registerX += registerChanges.get(currentCycle);
                    }

                    if (currentCycle % CRT_WIDTH == 0) {
                        crt.append(NEWLINE);
                    }
                    int rowPosition = currentCycle % CRT_WIDTH;
                    crt.append(registerX >= rowPosition - 1 && registerX <= rowPosition + 1 ? LIT : DARK);
                }

                line = reader.readLine();
            }

            System.out.println("");
            System.out.println("End Cycle: " + currentCycle);
            System.out.println("Register X: " + registerX);
            System.out.println("End Signal Strength: " + (registerX * currentCycle));
            System.out.println("");
            System.out.println("All Strengths: " + Arrays.stream(signalStrengths).mapToObj(String::valueOf).collect(Collectors.joining(", ")));
            System.out.println("Total Strength: " + Arrays.stream(signalStrengths).reduce(0, Integer::sum));
            System.out.println("");
            System.out.println("CRT Output:");
            System.out.println(crt);
        }
    }

}

