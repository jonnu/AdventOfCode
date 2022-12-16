package com.github.jonnu.advent.puzzle.y2022;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle16 implements Puzzle {

    private static final String STARTING_VALVE_NAME = "AA";
    private static final int MINUTES_TIL_VOLCANO_ERUPT = 30;
    private static final int MINUTES_TO_TEACH_ELEPHANT = 4;
    private static final Pattern PATTERN = Pattern.compile("^.*?\\s(?<name>[A-Z]{2}).*?rate=(?<rate>\\d+);.*valves?\\s(?<tunnels>[A-Z,\\s]+)$");

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle16.txt")) {
            String line = reader.readLine();

            int valveCount = 0;
            int nonZeroFlowValves = 0;
            final ArrayList<Valve> valves = new ArrayList<>();
            final Map<String, Integer> valveIndex = new HashMap<>();

            while (line != null) {

                final Matcher matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Unable to match input: " + line);
                }

                final Valve valve = Valve.builder()
                        .name(matcher.group("name"))
                        .flow(Integer.parseInt(matcher.group("rate")))
                        .tunnels(matcher.group("tunnels").split(",\\s"))
                        .build();

                // only need to mask non-zero flow valves.
                if (valve.getFlow() > 0) {
                    valve.setMask(1 << (nonZeroFlowValves++));
                }

                valves.add(valve);
                valveIndex.put(valve.getName(), valveCount++);

                line = reader.readLine();
            }

            // set array of edges using index
            valves.forEach(valve -> valve.setEdges(Arrays.stream(valve.getTunnels())
                    .mapToInt(valveIndex::get)
                    .toArray()));

            // part 1.
            int[][] state = initialiseBlankState(valves.size(), nonZeroFlowValves);
            state = openValves(state, valves, valveIndex.get(STARTING_VALVE_NAME), 0);
            System.out.println("Max pressure released: " + max(state));

            // part 2.
            final Participant[] participants = Participant.values();
            state = initialiseBlankState(valves.size(), nonZeroFlowValves);

            for (Participant participant : participants) {

                state = openValves(state, valves, valveIndex.get(STARTING_VALVE_NAME), MINUTES_TO_TEACH_ELEPHANT);

                if (participant.equals(Participant.ME)) {
                    int startIndex = valveIndex.get(STARTING_VALVE_NAME);
                    int[][] newState = initialiseBlankState(valves.size(), nonZeroFlowValves);
                    for (int v = 0; v < state.length; v++) {
                        for (int b = 0; b < state[v].length; b++) {
                            newState[startIndex][b] = Math.max(newState[startIndex][b], state[v][b]);
                        }
                    }

                    state = newState;
                }
            }

            System.out.println("Max pressure released with an elephant helping: " + max(state));
        }
    }

    private static int[][] openValves(int[][] state, final List<Valve> valves, final int start, final int paused) {

        state[start][0] = 0;
        for (int m = paused; m < MINUTES_TIL_VOLCANO_ERUPT; m++) {

            int[][] newState = initialiseBlankState(valves.size(), valves.stream().mapToInt(Valve::getFlow).filter(flow -> flow > 0).count());
            for (int v = 0; v < state.length; v++) {

                int flow = valves.get(v).getFlow() * ((MINUTES_TIL_VOLCANO_ERUPT - 1) - m);
                for (int b = 0; b < state[v].length; b++) {

                    if (state[v][b] >= 0) {
                        // flow & valve is currently off. turn valve on and add potential flow to state.
                        if (flow > 0 && (valves.get(v).getMask() & b) == 0) {
                            int mask = valves.get(v).getMask() | b;
                            newState[v][mask] = Math.max(newState[v][mask], state[v][b] + flow);
                        }

                        // update connected valves to this one with state.
                        for (int e : valves.get(v).getEdges()) {
                            newState[e][b] = Math.max(newState[e][b], state[v][b]);
                        }
                    }
                }
            }

            state = newState;
        }

        return state;
    }

    private static int[][] initialiseBlankState(int size, long valves) {
        int[][] states = new int[size][1 << valves];
        for (int[] state : states) {
            Arrays.fill(state, -1);
        }
        return states;
    }

    private static int max(int[][] array) {
        int result = Integer.MIN_VALUE;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                result = Math.max(result, array[i][j]);
            }
        }
        return result;
    }

    private enum Participant {
        ME,
        ELEPHANT
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class Valve {
        private final String name;
        private final String[] tunnels;
        private final int flow;
        private int[] edges;
        private int mask;
    }

}

