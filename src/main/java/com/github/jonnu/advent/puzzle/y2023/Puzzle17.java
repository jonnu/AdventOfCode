package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.github.jonnu.advent.puzzle.y2021.Puzzle15;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle17 implements Puzzle {

    private final ResourceReader resourceReader;

    private final Map<Point, Integer> blocks = new HashMap<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle17.txt")) {

            int row = 0;
            String line = reader.readLine();
            int gridWidth = line.length();
            while (line != null) {
                final String p = line;
                final int y = row;
                blocks.putAll(IntStream.range(0, gridWidth)
                        .boxed()
                        .collect(Collectors.toMap(
                                x -> new Point(x, y),
                                x -> Integer.parseInt(String.valueOf(p.charAt(x)))))
                );
                line = reader.readLine();
                row++;
            }

            final Point origin = new Point(0, 0);
            final Point destination = new Point(gridWidth -1 , row - 1);

            System.out.println("Heat loss: " + solve(origin, destination));
        }
    }

    private int solve(final Point origin, final Point destination) {

        int heatLoss = 0;
        Queue<Path> queue = new PriorityQueue<>(Comparator.comparing(Path::getHeatLoss));
        final Map<Point, Integer> costs = new HashMap<>();
        final Map<Point, Path> costs2 = new HashMap<>();
        //Set<Point> visited = new HashSet<>();

        //RollingStack<Direction> lookBack = new RollingStack<>(3);
        //lookBack.push(Direction.EAST);

        //System.out.println("Lookback: " + lookBack);
        //Direction currentDirection = lookBack.lastElement();

        Predicate<Point> isInBounds = p -> p.getX() >= 0 && p.getX() <= blocks.keySet().stream().mapToInt(Point::getX).max().orElse(0)
                && p.getY() >= 0 && p.getY() <= blocks.keySet().stream().mapToInt(Point::getY).max().orElse(0);

        PriorityQueue<Path> p = new PriorityQueue<>(Comparator.comparing(Path::getHeatLoss));

        queue.add(new Path(0, origin, new RollingStack<>(3), new ArrayList<>(List.of(origin))));
        while (!queue.isEmpty()) {

            Path currentPath = queue.poll();
            Point currentPosition = currentPath.getCurrentPosition();

            if (currentPosition.equals(destination)) {
                currentPath.getVisited().add(currentPosition);
                p.add(currentPath);
                continue;
            }

            //int blockHeatLoss = blocks.get(currentPosition);

            // work out possible directions.
            Set<Direction> possibleDirections = Direction.cardinal()
                    .stream()
                    // rule: cannot go back on itself
                    .filter(d -> currentPath.getLookbackDirections().isEmpty() || !d.equals(currentPath.getLookbackDirections().peek().opposite()))
                    // rule: cannot go the same way three times.
                    .filter(d -> !(currentPath.getLookbackDirections().isFull() && currentPath.getLookbackDirections().isHomogeneous() && currentPath.getLookbackDirections().contains(d)))
                    // rule: cannot go out of bounds.
                    .filter(d -> isInBounds.test(currentPath.getCurrentPosition().move(d)))
                    .collect(Collectors.toSet());

            System.out.println("+ Path: " + currentPath);
            System.out.println(" ? Possible: " + possibleDirections + " (Last: " + (currentPath.getLookbackDirections().isEmpty() ? "NONE" : currentPath.getLookbackDirections().peek()) + ")");

            possibleDirections.forEach(direction -> {

                Point nextPosition = currentPath.getCurrentPosition().move(direction);

                int loss = currentPath.getHeatLoss() + blocks.get(nextPosition);
                //System.out.println("Moving from " + currentPath.getCurrentPosition() + " to " + nextPosition + " (" + direction + ") = " + loss);
                //if (!costs.containsKey(nextPosition) || loss < costs.get(nextPosition)) {
                if (!costs2.containsKey(nextPosition) || loss <= costs2.get(nextPosition).getHeatLoss()) {

                    costs.put(nextPosition, loss);

                    RollingStack<Direction> nextLookback = new RollingStack<>(currentPath.getLookbackDirections());
                    nextLookback.push(direction);

                    List<Point> nextVisited = new ArrayList<>(currentPath.getVisited());
                    nextVisited.add(nextPosition);

                    if (destination.equals(nextPosition)) {
                        System.out.println("XPath:" + currentPath);
                    }

                    Path x = new Path(
                            currentPath.getHeatLoss() + blocks.get(nextPosition),
                            nextPosition,
                            nextLookback,
                            nextVisited
                    );
                    System.out.println(" >> Pre Path: " + currentPath);
                    System.out.println(" >> New Path: " + x);
                    costs2.put(nextPosition, x);
                    queue.add(x);
                    //frontier.add(new Puzzle15.Path(cost, point));
                    //movement.put(point, path.getPoint());
                }
//
//
//                RollingStack<Direction> nextLookback = currentPath.getLookbackDirections();
//                nextLookback.add(direction);
//                Set<Point> nextVisited = currentPath.getVisited();
//                nextVisited.add(nextPosition);
//                //nextVisited.add(currentPosition);
//                queue.add(new Path(
//                        currentPath.getHeatLoss() + blocks.get(nextPosition),
//                        nextPosition,
//                        nextLookback,
//                        nextVisited
//                ));
            });

            //draw(blocks, currentPath.getVisited());
        }

        System.out.println("QUEUE: " + p);
        Path winner = p.poll();
        //System.out.println(costs);
        //System.out.println(destination + ": " + costs.get(destination));

        Point interest = new Point(2, 1);
        System.out.println(interest + ":" + blocks.get(interest) + ": " + costs2.get(interest));

        draw(blocks, winner.getVisited().stream().collect(Collectors.toSet()));
        //costs2.get(destination).getVisited().forEach(System.out::println);
        return winner.getHeatLoss();
    }

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    private static void draw(final Map<Point, Integer> grid, final Set<Point> highlight) {
        final IntSummaryStatistics xStats = grid.keySet().stream().mapToInt(Point::getX).summaryStatistics();
        final IntSummaryStatistics yStats = grid.keySet().stream().mapToInt(Point::getY).summaryStatistics();
        System.out.printf("%n");
        for (int y = yStats.getMin(); y <= yStats.getMax(); y++) {
            for (int x = xStats.getMin(); x <= xStats.getMax(); x++) {
                Point p = new Point(x, y);
                System.out.print(highlight.contains(p) ? ANSI_RED + grid.get(p) + ANSI_RESET : grid.get(p));
            }
            System.out.printf("%n");
        }
        System.out.printf("%n");
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @RequiredArgsConstructor
    private static class Path {
        final int heatLoss;
        final Point currentPosition;
        final RollingStack<Direction> lookbackDirections;
        @Setter List<Point> visited;
    }

    private static class RollingStack<T> extends Stack<T> {

        private final int size;

        RollingStack(final int size) {
            this.size = size;
        }

        RollingStack(final RollingStack<T> source) {
            this.size = source.size;
            source.forEach(this::push);
        }

        public T push(final T item) {
            while (size() >= size) {
                remove(0);
            }
            return super.push(item);
        }

        public boolean isFull() {
            return size() == size;
        }

        public boolean isHomogeneous() {
            return Sets.newHashSet(Iterators.forEnumeration(super.elements())).size() <= 1;
        }
    }
}
