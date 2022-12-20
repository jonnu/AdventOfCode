package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle4 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle4.txt")) {

            Queue<Integer> bingoQueue = Arrays.stream(reader.readLine().split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toCollection(ArrayDeque::new));

            List<BingoBoard> boards = new ArrayList<>();
            String line = reader.readLine();

            int currentRow = 0;
            int[][] numbers = new int[5][5];

            while (line != null) {

                if (line.isEmpty()) {
                    line = reader.readLine();
                    numbers = new int[5][5];
                    currentRow = 0;
                    continue;
                }

                numbers[currentRow] = Arrays.stream(line.trim().split("\\s+"))
                        .mapToInt(Integer::parseInt)
                        .toArray();

                currentRow++;

                if (currentRow == 5) {
                    boards.add(BingoBoard.create(numbers));
                }

                line = reader.readLine();
            }

            // play bingo.
            AtomicBoolean losingBoardFound = new AtomicBoolean(false);
            AtomicBoolean winningBoardFound = new AtomicBoolean(false);
            while (!losingBoardFound.get() && !bingoQueue.isEmpty()) {

                int number = bingoQueue.poll();
                for (BingoBoard board : boards) {

                    board.mark(number);

                    if (board.isWinner()) {
                        if (!winningBoardFound.get()) {
                            System.out.println("Winning board: " + number * board.unmarkedValue());
                            winningBoardFound.set(true);
                        }

                        if (boards.stream().allMatch(BingoBoard::isWinner)) {
                            System.out.println("Losing board: " + number * board.unmarkedValue());
                            losingBoardFound.set(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Builder
    private static class BingoBoard {

        private final List<List<BingoSquare>> board = new ArrayList<>();

        public void addRow(int[] numbers) {
            board.add(Arrays.stream(numbers).mapToObj(BingoSquare::new).collect(Collectors.toList()));
        }

        public boolean mark(final int number) {
            Optional<BingoSquare> bingoSquare = board.stream().flatMap(Collection::stream)
                    .filter(square -> square.getNumber() == number)
                    .findFirst();
            bingoSquare.ifPresent(BingoSquare::mark);
            return bingoSquare.isPresent();
        }

        public boolean isWinner() {
            return hasWinningColumn() || hasWinningRow();
        }

        private boolean hasWinningRow() {
            return board.stream().anyMatch(row -> row.stream().allMatch(BingoSquare::isMarked));
        }

        private boolean hasWinningColumn() {
            return IntStream.range(0, board.get(0).size())
                    .mapToObj(index -> board.stream().map(row -> row.get(index)))
                    .anyMatch(column -> column.allMatch(BingoSquare::isMarked));
        }

        private int unmarkedValue() {
            return board.stream()
                    .flatMap(Collection::stream)
                    .filter(BingoSquare::isUnmarked)
                    .mapToInt(BingoSquare::getNumber)
                    .sum();
        }

        public static BingoBoard create(final int[][] numbers) {
            BingoBoard board = new BingoBoard();
            for (int[] row : numbers) {
                board.addRow(row);
            }
            return board;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int r = 0; r < board.size(); r++) {
                for (int c = 0; c < board.get(r).size(); c++) {
                    BingoSquare square = board.get(r).get(c);
                    builder.append(String.format("%s%2d%s", (square.isMarked() ? "[" : " "), square.getNumber(), (square.isMarked() ? "]" : " ")));
                }
                builder.append("\n");
            }
            return builder.append("\n").toString();
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class BingoSquare {

        private final int number;
        boolean marked = false;

        public void mark() {
            marked = true;
        }

        public boolean isUnmarked() {
            return !marked;
        }
    }
}