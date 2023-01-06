package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle12 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        final Map<String, Cave> caves = new HashMap<>();

        try (BufferedReader reader = resourceReader.read("y2021/puzzle12.txt")) {
            String line = reader.readLine();
            while (line != null) {
                List<Cave> joinedCaves = Arrays.stream(line.split("-"))
                        .map(name -> caves.computeIfAbsent(name, Cave::new))
                        .collect(Collectors.toList());
                joinedCaves.get(0).addPath(joinedCaves.get(1));
                line = reader.readLine();
            }
        }

        final Set<Path> complete = new HashSet<>();
        final Queue<Path> incomplete = new ArrayDeque<>();

        // Begin from the 'starting' cave.
        incomplete.add(new Path(caves.values()
                .stream()
                .filter(Cave::isStartCave)
                .collect(Collectors.toList())));

        while (!incomplete.isEmpty()) {
            Path path = incomplete.poll();
            path.getCurrentCave().getAdjoiningCaves().stream().filter(cave -> !path.disallowedCaves().contains(cave)).forEach(cave -> {
                Path possiblePath = path.createPossiblePath(cave);
                if (cave.isEndCave()) {
                    complete.add(possiblePath);
                    return;
                }
                incomplete.add(possiblePath);
            });
        }

        System.out.println("Number of unique pathways through the cave system: " + complete.size());
    }


    @Getter
    private static class Path {

        private final List<Cave> path;
        private final Map<String, Long> timesVisited;

        Path(final List<Cave> path) {
            this.path = path;
            timesVisited = path.stream()
                    .filter(cave -> !cave.isBigCave())
                    .collect(Collectors.groupingBy(Cave::getName, Collectors.counting()));
        }

        public Set<Cave> disallowedCaves() {
            return Stream.concat(Stream.of(getStartCave()), path.stream()
                    .filter(cave -> !cave.isBigCave())
                    .filter(this::caveIsVisitable))
                    .collect(Collectors.toSet());
        }

        public Cave getCurrentCave() {
            return path.get(path.size() - 1);
        }

        public Path createPossiblePath(final Cave cave) {
            return new Path(Stream.concat(path.stream(), Stream.of(cave)).collect(Collectors.toList()));
        }

        @Override
        public String toString() {
            return path.stream().map(Cave::getName).collect(Collectors.joining(","));
        }

        private Cave getStartCave() {
            return path.get(0);
        }

        private boolean caveIsVisitable(final Cave cave) {
            return timesVisited.getOrDefault(cave.getName(), 0L) >= (timesVisited.containsValue(2L) ? 1 : 2);
        }
    }

    @Getter
    @ToString
    @AllArgsConstructor
    private static class Cave {

        private static final String START = "start";
        private static final String FINISH = "end";

        private final String name;
        private final Set<Cave> adjoiningCaves = new HashSet<>();

        public void addPath(final Cave cave) {
            getAdjoiningCaves().add(cave);
            cave.getAdjoiningCaves().add(this);
        }

        public boolean isBigCave() {
            return name.chars().allMatch(Character::isUpperCase);
        }

        public boolean isStartCave() {
            return getName().equals(START);
        }

        public boolean isEndCave() {
            return getName().equals(FINISH);
        }
    }
}
