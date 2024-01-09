package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;
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

            //System.out.println("Heat loss: " + solve(origin, destination));

            System.out.println("Ultra Heat loss: " + solve(origin, destination, 10, 4));
        }
    }

    private int solve(final Point origin, final Point destination) {
        return solve(origin, destination, 3, 1);
    }

    private int solve(final Point origin, final Point destination, final int stackSize, final int minimumBeforeTurn) {

        // should we use a heuristic here as well?
        //Queue<Path> queue = new PriorityQueue<>(Comparator.comparing(Path::getHeatLoss));
        final BiFunction<Point, Point, Integer> heuristic = (a, b) -> Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
        Queue<Path> queue = new PriorityQueue<>(Comparator.comparingInt(path -> heuristic.apply(path.getCurrentPosition(), destination)));

        final int maxX = blocks.keySet().stream().mapToInt(Point::getX).max().orElse(0);
        final int maxY = blocks.keySet().stream().mapToInt(Point::getY).max().orElse(0);
        Predicate<Point> isInBounds = p -> p.getX() >= 0 && p.getX() <= maxX && p.getY() >= 0 && p.getY() <= maxY;

        PriorityQueue<Path> p = new PriorityQueue<>(Comparator.comparing(Path::getHeatLoss));

        final Map<String, Path> costs = new HashMap<>();

        queue.add(new Path(0, origin, new RollingStack<>(stackSize), new ArrayList<>(List.of(origin))));
        int steps = 0;
        int minLoss = Integer.MAX_VALUE;
        while (!queue.isEmpty()) {

            Path currentPath = queue.poll();
            Point currentPosition = currentPath.getCurrentPosition();

            // needs to have gone at least minimumBeforeTurn to complete as well.
            if (currentPosition.equals(destination)) {
                // only add it to the winners pool if we have homogenous n.
                if (currentPath.getLookbackDirections().isHomogeneous(minimumBeforeTurn)) {
                    currentPath.getVisited().add(currentPosition);
                    p.add(currentPath);
                    if (currentPath.getHeatLoss() < minLoss) {
                        //System.err.println("I found a path! " + currentPath.getHeatLoss());
                        minLoss = currentPath.getHeatLoss();
                        //1000 too high
                        // 820 too low.
                    }
                }
                continue;
            }

            //int blockHeatLoss = blocks.get(currentPosition);

            // work out possible directions.
            Set<Direction> possibleDirections = Direction.cardinal()
                    .stream()
                    // rule: you must move in the same direction of minimumBeforeTurn before you are allowed to change direction at all.
                    // so if number of moves in stack is below the minimum, or the last n moves are not homogeneous, then you MUST continue in the same direction [so filter down to just this direction].
                    //.filter(d -> (currentPath.getLookbackDirections().size() < minimumBeforeTurn || !currentPath.getLookbackDirections().isHomogeneous(minimumBeforeTurn)) && (currentPath.getLookbackDirections().isEmpty() || currentPath.getLookbackDirections().peek().equals(d)))
                    .filter(d -> currentPath.getLookbackDirections().isEmpty() || (currentPath.getLookbackDirections().peek().equals(d) || currentPath.getLookbackDirections().isHomogeneous(minimumBeforeTurn)))
                    // rule: cannot go back on itself
                    .filter(d -> currentPath.getLookbackDirections().isEmpty() || !d.equals(currentPath.getLookbackDirections().peek().opposite()))
                    // rule: cannot go the same way n times [3 for part 1, and 10 for part 2].
                    .filter(d -> !(currentPath.getLookbackDirections().isFull() && currentPath.getLookbackDirections().isHomogeneous() && currentPath.getLookbackDirections().contains(d)))
                    // rule: cannot go out of bounds.
                    .filter(d -> isInBounds.test(currentPath.getCurrentPosition().move(d)))
                    .collect(Collectors.toSet());

            //System.out.println("+ Path: " + currentPath);
            //System.out.println(" ? Possible: " + possibleDirections + " (Last: " + (currentPath.getLookbackDirections().isEmpty() ? "NONE" : currentPath.getLookbackDirections().peek()) + "); Homogenous(" + minimumBeforeTurn + "): " + currentPath.getLookbackDirections().isHomogeneous(minimumBeforeTurn));

            /*
            generic pathfind: bfs.

            It takes a generic T start point, a end condition function itself taking another T as its parameter and returning a boolean, a neighbor finding fuction which takes a T and returns a list of them, a cost function which takes the current T and the next T and returns an int, and finally a heuristic function which takes a T and returns an int (For today, it was just returning 0 and there we no use for any kind of heuristics as far as I'm aware.
            I also have a Seen object generalized by T which contains the current cost when seen, and the previous T which made me get to it ; and a Scored object, also generalized by T, which contains the current point in the path, the "score" of this current path, and its heuristic if any.
            The function itself returns a result object which is generalized by the same generic T, and contains the start, the eventual end, and a Map of <T, Seen<T>. When you get the value for the end point, it should be the score, in this case the heat loss.
            ... This comment feels like infodump. The source is here if you want to take a look:
             */
            possibleDirections.forEach(direction -> {

                Point nextPosition = currentPath.getCurrentPosition().move(direction);
                // todo: store this in the path.
                String nextKey = currentPath.getLookbackDirections().stream().skip(1).map(Enum::name).collect(Collectors.joining())
                        + direction.name()
                        + nextPosition;
                //String.format("%s%s%s", currentPath.getLookbackDirections().stream().skip(1).map(x -> x.name().substring(0, 1)).collect(Collectors.joining()), direction.name().substring(0, 1), nextPosition) ;

                int loss = currentPath.getHeatLoss() + blocks.get(nextPosition);
                //System.out.println("Moving from " + currentPath.getCurrentPosition() + " to " + nextPosition + " (" + direction + ") = " + loss);
                //if (!costs.containsKey(nextPosition) || loss < costs.get(nextPosition)) {
                if (!costs.containsKey(nextKey) || loss < costs.get(nextKey).getHeatLoss()) {

                    //costs.put(nextPosition, loss);

                    RollingStack<Direction> nextLookback = new RollingStack<>(currentPath.getLookbackDirections());
                    nextLookback.push(direction);

                    List<Point> nextVisited = new ArrayList<>(currentPath.getVisited());
                    nextVisited.add(nextPosition);

                    //if (destination.equals(nextPosition)) {
                        //System.out.println("XPath:" + currentPath);
                    //}

                    Path x = new Path(
                            currentPath.getHeatLoss() + blocks.get(nextPosition),
                            nextPosition,
                            nextLookback,
                            nextVisited
                    );
                    //System.out.println(" >> Pre Path: " + currentPath);
                    //System.out.println(" >> New Path: " + x);
                    costs.put(nextKey, x);
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

            steps++;
            //draw(blocks, currentPath.getVisited());
        }

        System.out.println("QUEUE: " + p);
        Path winner = p.poll();
        //System.out.println(costs);
        //System.out.println(destination + ": " + costs.get(destination));

        Point interest = new Point(2, 1);
        System.out.println(interest + ":" + blocks.get(interest) + ": " + costs.get(interest));

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
            return isHomogeneous(size());
        }

        public boolean isHomogeneous(final int limit) {

            final int len = size();
            if (len <= 1 || limit <= 1) {
                return true;
            }

            T last = get(len - 1);
            for (int i = len - 2, j = limit - 1; i >= 0 && j > 0; i--, j--) {
                T current = get(i);
                //System.out.println("Len: " + len + "; J: " + j + " I: " + i + " [comparing " + current + " to " + last + "]: " + current.equals(last));
                if (!current.equals(last)) {
                    return false;
                }
                last = current;
            }

            return true;
        }
    }
}
