package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.github.jonnu.advent.puzzle.y2023.Puzzle8;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle11 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        Map<String, Node> nodes = new HashMap<>();
        try (BufferedReader reader = resourceReader.read("y2025/puzzle11.txt")) {
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(":");
                Node node = new Node(parts[0]);
                node.setConnections(Arrays.stream(parts[1].trim().split(" "))
                        .map(Node::new)
                        .collect(Collectors.toSet()));
                nodes.put(node.getName(), node);
                line = reader.readLine();
            }

            System.out.println(nodes);
        }
        nodes.put("out", new Node("out"));

        // let's walk.
        Queue<Node> queue = new ArrayDeque<>(Set.of(nodes.get("you")));
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            System.out.println("Reading: " + node);
            nodes.get(node.getName()).getConnections()
                    .stream()
                    .map(x -> nodes.get(x.getName()))
                            .forEach(queue::add);

            //queue.addAll(nodes.get(node.getName()).getConnections());
        }

        //System.out.println("[Part 1] " + you);
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
