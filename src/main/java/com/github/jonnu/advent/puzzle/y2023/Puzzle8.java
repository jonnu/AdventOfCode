package com.github.jonnu.advent.puzzle.y2023;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle8 implements Puzzle {

    private static final Pattern PATTERN = Pattern.compile("^(?<root>[A-Z0-9]{3})\\s=\\s\\((?<left>[A-Z0-9]{3}),\\s(?<right>[A-Z0-9]{3})\\)$");


    private final ResourceReader resourceReader;

    private final Map<String, Node> nodes = new HashMap<>();
    private final List<String> directions = new ArrayList<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle8.txt")) {

            directions.addAll(reader.readLine()
                    .chars()
                    .mapToObj(Character::toString)
                    .collect(Collectors.toList()));

            String line = reader.readLine();
            Set<String> starts = new HashSet<>();

            while (line != null) {

                Matcher m = PATTERN.matcher(line);
                if (m.matches()) {

                    // todo: clean this up.
                    Node root = nodes.getOrDefault(m.group("root"), new Node(m.group("root")));
                    Node left = nodes.getOrDefault(m.group("left"), new Node(m.group("left")));
                    Node right = nodes.getOrDefault(m.group("right"), new Node(m.group("right")));
                    root.setLeft(left);
                    root.setRight(right);
                    nodes.put(root.getName(), root);
                    nodes.put(left.getName(), left);
                    nodes.put(right.getName(), right);

                    if (root.getName().endsWith("A")) {
                        starts.add(root.getName());
                    }
                }

                line = reader.readLine();
            }

            long part1 = solve("AAA", d -> d.equals("ZZZ"));

            long[] destinations = starts.stream()
                    .mapToLong(node -> solve(node, d -> d.endsWith("Z")))
                    .toArray();

            System.out.println("Output 1: " + part1);
            System.out.println("Output 2: " + lcm(destinations));
        }
    }

    private long solve(final String start, final Predicate<String> destination) {
        String current = start;
        int step = 0;
        for ( ; ; ) {

            if (destination.test(current)) {
                return step;
            }

            String direction = directions.get(step % directions.size());
            current = "L".equals(direction) ? nodes.get(current).getLeft().getName() : nodes.get(current).getRight().getName();
            step++;
        }
    }

    // greatest common divisor
    private static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    // lowest common multiple
    private static long lcm(long a, long b) {
        return a * (b / gcd(a, b));
    }

    // lcm amongst a set.
    private static long lcm(final long[] input) {
        long result = input[0];
        for(int i = 1; i < input.length; i++) {
            result = lcm(result, input[i]);
        }
        return result;
    }

    @Data
    private static class Node {

        private String name;
        private Node left;
        private Node right;

        Node(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return getName() +
                    " [L: " + Optional.ofNullable(left).map(Node::getName).orElse("???") +
                    ", R: " + Optional.ofNullable(right).map(Node::getName).orElse("???") + "]";
        }
    }
}
