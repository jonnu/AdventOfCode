package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle11 implements Puzzle {

    private final ResourceReader resourceReader;

    private static final Map<String, Node> NODEMAP = new HashMap<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2025/puzzle11.txt")) {
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(":");
                Node node = new Node(parts[0]);
                node.setConnections(Arrays.stream(parts[1].trim().split(" "))
                        .map(Node::new)
                        .collect(Collectors.toSet()));
                NODEMAP.put(node.getName(), node);
                line = reader.readLine();
            }
        }
        NODEMAP.put("out", new Node("out"));

        // let's walk.
        Path you = new Path();
        you.add(NODEMAP.get("you"));

        Path svr = new Path();
        svr.add(NODEMAP.get("svr"));

        Set<Path> part1 = getValidPaths(you, path -> true);
        Set<Path> part2 = getValidPaths(svr, path -> new HashSet<>(path.getNodes()).containsAll(Set.of(NODEMAP.get("fft"), NODEMAP.get("dac"))));

        System.out.println("[Part 1] Paths from 'you' to 'out': " + part1.size());
        System.out.println("[Part 2] Paths from 'srv' to 'out' (via fft and dac): " + part2.size());
    }

    //gonna need backtracking.
    private Set<Path> getValidPaths(final Path start, Predicate<Path> predicate) {
        final Set<Path> result = new HashSet<>();
        final Set<Path> seen = new HashSet<>();
        final Queue<Path> queue = new ArrayDeque<>(Set.of(start));
        while (!queue.isEmpty()) {
            Path path = queue.poll();
            //System.out.println("Eval: " + path);
            if (seen.contains(path)) {
                continue;
            }
            if (path.tail().getName().equals("out") && predicate.test(path)) {
                result.add(path);
                continue;
            }
            queue.addAll(path.possibilities());
            seen.add(path);
        }
        return result;
    }

    @Getter
    private static class Path {

        List<Node> nodes = new ArrayList<>();

        public void add(final Node node) {
            nodes.add(NODEMAP.get(node.getName()));
        }

        public Node tail() {
            return nodes.getLast();
        }

        // from the given path, return a list of outcomes.
        public Set<Path> possibilities() {
            return tail().getConnections()
                    .stream()
                    .map(this::copyWith)
                    .collect(Collectors.toSet());
        }

        public Path copyWith(final Node node) {
            Path path = new Path();
            getNodes().forEach(path::add);
            path.add(node);
            return path;
        }

        @Override
        public String toString() {
            return nodes.stream().map(Node::getName).collect(Collectors.joining("->"));
        }
    }

    @Data
    @RequiredArgsConstructor
    private static class Node {

        private final String name;
        private Set<Node> connections = new HashSet<>();

        @Override
        public String toString() {
            return getName() +
                    " [" + connections.stream().map(Node::getName).collect(Collectors.joining(", ")) + "]";
        }
    }
}
