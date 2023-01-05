package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Direction;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle8 implements Puzzle {

    private static final Map<Direction, GridTraversal> TRAVERSAL = ImmutableMap.<Direction, GridTraversal>builder()
            .put(Direction.NORTH, new GridTraversal(y -> 0, true))
            .put(Direction.SOUTH, new GridTraversal(y -> y.length, false))
            .put(Direction.WEST, new GridTraversal(x -> 0, true))
            .put(Direction.EAST, new GridTraversal(x -> x.length, false))
            .build();

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle8.txt")) {

            String line = reader.readLine();
            int c = 0;
            int[][] trees = new int[line.length()][line.length()];
            while (line != null) {
                for (int r = 0; r < line.length(); r++) {
                    trees[c][r] = Integer.parseInt(String.valueOf(line.charAt(r)));
                }
                c++;
                line = reader.readLine();
            }

            int visibleTrees = 0;
            int bestScenicScore = 0;
            for (int i = 0; i < trees.length; i++) {
                for (int j = 0; j < trees[i].length; j++) {
                    if (isVisible(trees, j, i)) {
                        visibleTrees++;
                    }
                    int scenicScore = getScenicScore(trees, j, i);
                    if (scenicScore > bestScenicScore) {
                        bestScenicScore = scenicScore;
                    }
                }
            }

            System.out.println("Total visible trees: " + visibleTrees);
            System.out.println("Best treehouse scenic score: " + bestScenicScore);
        }
    }

    private static boolean isVisible(int[][] trees, int x, int y) {
        return Direction.cardinal().stream().anyMatch(direction -> isVisible(trees, x, y, direction));
    }

    private static int getScenicScore(int[][] trees, int x, int y) {
        return Direction.cardinal().stream()
                .map(direction -> getScenicScore(trees, x, y, direction))
                .reduce(1, Math::multiplyExact);
    }

    private static int getScenicScore(int[][] trees, int x, int y, Direction direction) {
        return walkMatrixAndCallback(trees, x, y, direction, Puzzle8::getTreeScenicScore);
    }

    private static boolean isVisible(int[][] trees, int x, int y, Direction direction) {
        return walkMatrixAndCallback(trees, x, y, direction, Puzzle8::isTreeViewBlocked);
    }

    private static <T> T walkMatrixAndCallback(final int[][] trees, final int x, final int y, final Direction direction, final Function<int[], T> callback) {

        final GridTraversal traversal = TRAVERSAL.get(direction);
        final IntUnaryOperator mappingOperator = direction.isVertical() ? i -> trees[i][x] : i -> trees[y][i];
        final int end = traversal.findBoundary(trees);
        final int start = direction.isVertical() ? y : x;

        IntStream stream = IntStream.range(Math.min(start, end), Math.max(start, end));
        if (traversal.shouldReverse()) {
            stream = stream.map(i -> Math.max(start, end) - i - 1);
        }

        int[] ints = (traversal.shouldReverse() ? Stream.concat(Stream.of(start), stream.boxed()) : stream.boxed())
                .map(mappingOperator::applyAsInt)
                .mapToInt(Integer::intValue)
                .toArray();

        return callback.apply(ints);
    }

    private static boolean isTreeViewBlocked(final int[] path) {

        if (path.length == 1) {
            return true;
        }

        int treeHeight = path[0];
        for (int i = 1; i < path.length; i++) {
            if (path[i] >= treeHeight) {
                return false;
            }
        }

        return true;
    }

    private static int getTreeScenicScore(final int[] path) {

        int score = 0;
        if (path.length == 1) {
            return score;
        }

        int treeHeight = path[0];
        for (int i = 1; i < path.length; i++) {
            score++;
            if (path[i] >= treeHeight) {
                return score;
            }
        }

        return score;
    }

    @AllArgsConstructor
    private static class GridTraversal {

        private final ToIntFunction<int[][]> findBound;
        private final boolean reverse;

        public int findBoundary(final int[][] input) {
            return findBound.applyAsInt(input);
        }

        public boolean shouldReverse() {
            return reverse;
        }
    }

}
