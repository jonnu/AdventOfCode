package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle8 implements Puzzle {

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
        return Arrays.stream(Direction.values())
                .anyMatch(direction -> isVisible(trees, x, y, direction));
    }

    private static int getScenicScore(int[][] trees, int x, int y) {
        return Arrays.stream(Direction.values())
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

        boolean isVertical = direction.equals(Direction.UP) || direction.equals(Direction.DOWN);
        final IntUnaryOperator mappingOperator = isVertical ? i -> trees[i][x] : i -> trees[y][i];
        final int end = direction.getFindLimitFunction().apply(trees);
        final int start = isVertical ? y : x;

        IntStream stream = IntStream.range(Math.min(start, end), Math.max(start, end));
        if (direction.isReverse()) {
            stream = stream.map(i -> Math.max(start, end) - i - 1);
        }

        int[] ints = (direction.isReverse() ? Stream.concat(Stream.of(start), stream.boxed()) : stream.boxed())
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

    @Getter
    @AllArgsConstructor
    private enum Direction {

        UP(1, m -> 0, true),
        DOWN(0, m -> m.length, false),
        LEFT(0, m -> 0, true),
        RIGHT(0, m -> m.length, false);

        private final int delta;
        private final Function<int[][], Integer> findLimitFunction;
        private final boolean reverse;
    }
}
