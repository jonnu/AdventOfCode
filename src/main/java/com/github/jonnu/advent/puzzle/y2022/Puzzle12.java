package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle12 implements Puzzle {

    private final ResourceReader resourceReader;

    private static Predicate<Node<Character>> nodeFinder(final char toFind) {
        return node -> node.getValue().equals(toFind);
    }

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle12.txt")) {

            String line = reader.readLine();
            List<String> allLines = new ArrayList<>();
            while (line != null) {
                allLines.add(line);
                line = reader.readLine();
            }

            Predicate<Node<Character>> originFinder = nodeFinder('E');
            Predicate<Node<Character>> finishFinder = nodeFinder('S');
            Predicate<Node<Character>> lowElevationFinder = nodeFinder('a');

            // build grid.
            Coordinate<Character> origin = null;
            Coordinate<Character> finish = null;

            Set<Coordinate<Character>> allAs = new HashSet<>();

            final ArrayList<ArrayList<Coordinate<Character>>> grid = new ArrayList<>();
            for (int y = 0; y < allLines.size(); y++) {
                grid.add(new ArrayList<>());
                char[] chars = allLines.get(y).toCharArray();
                for (int x = 0; x < chars.length; x++) {

                    Coordinate<Character> current = Coordinate.<Character>builder().x(x).y(y).value(chars[x]).build();
                    if (origin == null && originFinder.test(current)) {
                        origin = current;
                        current.setValue('a');
                    }

                    if (finish == null && finishFinder.test(current)) {
                        finish = current;
                        current.setValue('z');
                    }

                    if (lowElevationFinder.test(current)) {
                        allAs.add(current);
                    }

                    grid.get(y).add(current);
                }
            }

            BiPredicate<Coordinate<Character>, Coordinate<Character>> uphillTraversal = (from, to) -> to.getValue() <= from.getValue() + 1;
            BiPredicate<Coordinate<Character>, Coordinate<Character>> downhillTraversal = (from, to) -> from.getValue() <= to.getValue() + 1;

            // Part one
            CoordinateStar cs = new CoordinateStar(origin, finish, grid, new ChebyshevDistanceHeuristic<>(), downhillTraversal);
            int smallest = cs.solve();
            System.out.println("Smallest distance between " + origin + " and " + finish + ": " + cs.solve());

            // Part two
            // Ideally we would memoise for all 'a' or backtrack.
            // But I am lazy. So lets bfs each from scratch and compare.
            Coordinate<Character> twoStart = origin;
            int shortestRouteTwo = allAs.stream()
                    .map(a -> {
                        try {
                            return cs.solve(twoStart, a);
                        } catch (TraversalException e) {
                            return Integer.MAX_VALUE;
                        }
                    })
                    .filter(x -> x > 0)
                    .reduce(smallest, Math::min);
            System.out.println("Smallest distance from " + twoStart + " to square with lowest elevation: " + shortestRouteTwo);
        }
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    private static class Node<V>  {
        V value;
    }

    @Value
    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    private static class Coordinate<V> extends Node<V> {

        int x;
        int y;

        @Override
        public String toString() {
            return getValue() + " (" + x + "," + y + ")";
        }
    }

    @Data
    @AllArgsConstructor
    private static class RouteNode<T extends Node<V>, V> implements Comparable<RouteNode<T, V>> {

        private final T current;
        private T previous;

        private double scoreRouted;
        private double scoreEstimate;

        RouteNode(final T current) {
            this(current, null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        @Override
        public int compareTo(final RouteNode<T, V> otherRouteNode) {
            return Double.compare(getScoreEstimate(), otherRouteNode.getScoreEstimate());
        }
    }

    interface Heuristic<T> {
        int apply(T current, T next);
    }

    static class ManhattanDistanceHeuristic<V> implements Heuristic<Coordinate<V>> {
        @Override
        public int apply(final Coordinate<V> current, final Coordinate<V> next) {
            return Math.abs(current.getX() - next.getX()) + Math.abs(current.getY() - next.getY());
        }
    }

    static class ChebyshevDistanceHeuristic<V> implements Heuristic<Coordinate<V>> {
        @Override
        public int apply(final Coordinate<V> current, final Coordinate<V> next) {
            return Math.max(Math.abs(current.getX() - next.getX()), Math.abs(current.getY() - next.getY()));
        }
    }

    @AllArgsConstructor
    private abstract static class Star<N extends Node<V>, V> {

        @NonNull private final N start;
        @NonNull private final N finish;

        // Could abstract this. It's a matrix of Node<V>.
        protected final ArrayList<ArrayList<N>> graph;

        private final Map<N, RouteNode<N, V>> allNodes = new HashMap<>();
        private final Heuristic<N> heuristic;

        // allow this to be injected. This will allow us to increase score between
        // nodes. classic example, moving through 'forest' is double moving through 'plains'.
        private final Heuristic<N> scorer = (current, next) -> 1;

        public int solve() {
            return solve(start, finish);
        }

        public int solve(final N start, final N finish) {

            if (start.equals(finish)) {
                return 0;
            }

            allNodes.clear();
            final PriorityQueue<RouteNode<N, V>> frontier = new PriorityQueue<>();

            final RouteNode<N, V> startNode = new RouteNode<>(start, null, 0, heuristic.apply(start, finish));
            frontier.add(startNode);
            allNodes.put(start, startNode);

            while (!frontier.isEmpty()) {

                final RouteNode<N, V> nextNode = frontier.poll();
                if (nextNode.getCurrent().equals(finish)) {

                    //List<N> path = new ArrayList<>();
                    int steps = 0;
                    RouteNode<N, V> currentNode = nextNode;
                    do {
                        steps++;
                        //path.add(currentNode.getCurrent());
                        currentNode = allNodes.get(currentNode.getPrevious());
                    } while (currentNode != null && !currentNode.getCurrent().equals(start));

                    return steps;//path.size();
                }

                // find neighbours.
                Set<N> neighbours = findNeighbours(nextNode.getCurrent());
                neighbours.forEach(neighbour -> {
                    final RouteNode<N, V> neighbourNode = allNodes.getOrDefault(neighbour, new RouteNode<>(neighbour));
                    allNodes.put(neighbour, neighbourNode);
                    double score = nextNode.getScoreRouted() + scorer.apply(nextNode.getCurrent(), neighbour);
                    if (score < neighbourNode.getScoreRouted()) {
                        neighbourNode.setPrevious(nextNode.getCurrent());
                        neighbourNode.setScoreRouted(score);
                        neighbourNode.setScoreEstimate(score + heuristic.apply(neighbour, finish));
                        frontier.add(neighbourNode);
                    }
                });
            }

            throw new TraversalException("No route found between " + start + " and " + finish);
        }

        protected abstract Set<N> findNeighbours(N node);
    }

    static class CoordinateStar extends Star<Coordinate<Character>, Character> {

        private final BiPredicate<Coordinate<Character>, Coordinate<Character>> traversalCheck;

        public CoordinateStar(Coordinate<Character> start,
                Coordinate<Character> finish,
                ArrayList<ArrayList<Coordinate<Character>>> graph,
                Heuristic<Coordinate<Character>> heuristic,
                BiPredicate<Coordinate<Character>, Coordinate<Character>> traversalCheck) {
            super(start, finish, graph, heuristic);
            this.traversalCheck = traversalCheck;
        }

        @Override
        protected Set<Coordinate<Character>> findNeighbours(final Coordinate<Character> node) {
            return Direction.cardinal()
                    .stream()
                    .map(direction -> {

                        // In-bounds check.
                        int dx = node.getX() + direction.getDelta().getX();
                        int dy = node.getY() + direction.getDelta().getY();
                        if (dy < 0 || dy >= graph.size() || dx < 0 || dx >= graph.get(dy).size()) {
                            return Optional.<Coordinate<Character>>empty();
                        }

                        // Traversable check.
                        final Coordinate<Character> potential = graph.get(dy).get(dx);
                        if (traversalCheck.test(node, potential)) {
                            return Optional.of(potential);
                        }

                        return Optional.<Coordinate<Character>>empty();
                    })
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet());
        }
    }

    static class TraversalException extends RuntimeException {
        TraversalException(final String message) {
            super(message);
        }
    }
}
