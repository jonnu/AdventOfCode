package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle22 implements Puzzle {

    private static final Pattern INSTRUCTION_PATTERN = Pattern.compile("(?<move>\\d+)(?<rotation>[RL]?)");

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle22.txt")) {

            String line = reader.readLine();
            final Queue<Instruction> instructions = new ArrayDeque<>();
            final Map<Position, String> positions = new HashMap<>();

            int y = 1;
            while (line != null) {

                for (int x = 1; x <= line.length(); x++) {
                    if (line.charAt(x - 1) == ' ') {
                        continue;
                    }
                    positions.put(new Position(x, y), String.valueOf(line.charAt(x - 1)));
                }
                y++;

                if (line.isEmpty()) {
                    final Matcher instructionMatcher = INSTRUCTION_PATTERN.matcher(reader.readLine());
                    while (instructionMatcher.find()) {
                        instructions.add(new Instruction(
                                Integer.parseInt(instructionMatcher.group("move")),
                                Rotation.build(instructionMatcher.group("rotation")))
                        );
                    }
                }

                line = reader.readLine();
            }

            Direction currentDirection = Direction.RIGHT;
            Position currentPosition = new Position(positions.entrySet().stream()
                    .filter(position -> position.getValue().equals("."))
                    .map(Map.Entry::getKey)
                    .filter(position -> position.getY() == 1)
                    .min(Comparator.comparingInt(Position::getX))
                    .map(Position::getX)
                    .orElse(1), 1);

            final Map<Position, String> moves = new HashMap<>();

            while (!instructions.isEmpty()) {
                Instruction currentInstruction = instructions.poll();
                //System.out.println("Processing: " + currentInstruction);

                //System.out.printf("[%s] Moving %d %s%n", currentPosition, currentInstruction.getMove(), currentDirection);
                for (int m = currentInstruction.getMove(); m > 0; m--) {
                    // check cell.
                    Position nextPosition = currentPosition.move(currentDirection);
                    if (positions.containsKey(nextPosition)) {
                        // we can move
                        if (positions.get(nextPosition).equals(".")) {
                            moves.put(currentPosition, currentDirection.getCharacter());
                            currentPosition = nextPosition;
                            //System.out.printf("[%s] Moved %s (%d left)%n", currentPosition, currentDirection, m);
                            continue;
                        }

                        // we cant move, its a wall.
                        //System.out.printf("[%s] Hit wall moving %s at %s. Halting%n", currentPosition, nextPosition, currentDirection);
                        break;
                    } else {
                        // its empty, we need to loop around.
                        nextPosition = currentDirection.getWrapRuleFunction().apply(currentPosition, positions.keySet().stream());
                        if (positions.get(nextPosition).equals(".")) {
                            moves.put(currentPosition, currentDirection.getCharacter());
                            currentPosition = nextPosition;
                            //System.out.printf("[%s] Moved %s (%d left)%n", currentPosition, currentDirection, m);
                            continue;
                        }

                        // we cant move, its a wall.
                        //System.out.printf("[%s] Hit wall moving %s at %s. Halting%n", currentPosition, nextPosition, currentDirection);
                        break;
                    }
                }

                // Rotate.
                currentDirection = currentDirection.rotate(currentInstruction.getRotation());
                //System.out.printf("[%s] Rotating %s. New direction: %s%n", currentPosition, currentInstruction.getRotation(), currentDirection);
            }

            int rowPart = 1000 * currentPosition.getY();
            int colPart = 4 * currentPosition.getX();
            int dirPart = currentDirection.ordinal();
            int finalPw = rowPart + colPart + dirPart;

            // append other pieces.
            moves.putAll(positions.entrySet()
                    .stream()
                    .filter(x -> !moves.containsKey(x.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            // append final resting point.
            moves.put(currentPosition, currentDirection.getCharacter());
            debug(moves);

            System.out.println("Final Position  : " + currentPosition);
            System.out.println("Final Direction : " + currentDirection + " (" + currentDirection.ordinal() + ")");
            System.out.printf("Final Password  : 1000 * %d + 4 * %d + %d%n", currentPosition.getY(), currentPosition.getX(), currentDirection.ordinal());
            System.out.printf("                : %d + %d + %d%n", rowPart, colPart, dirPart);
            System.out.printf("                : %d%n", finalPw);
        }
    }

    private static void debug(final Map<Position, String> moves) {

        final IntSummaryStatistics xStat = moves.keySet().stream().mapToInt(Position::getX).summaryStatistics();
        final IntSummaryStatistics yStat = moves.keySet().stream().mapToInt(Position::getY).summaryStatistics();

        for (int y = yStat.getMin(); y < yStat.getMax(); y++) {
            for (int x = xStat.getMin(); x < xStat.getMax(); x++) {
                System.out.printf("%s", moves.getOrDefault(new Position(x, y), " "));
            }
            System.out.printf("%n");
        }

        System.out.printf("%n");
    }

    @Value
    @Builder
    @AllArgsConstructor
    public static class Position {

        int x;
        int y;

        public Position move(final Direction direction) {
            return Position.builder().x(x + direction.getDelta()[0]).y(y + direction.getDelta()[1]).build();
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    @Value
    @ToString
    @AllArgsConstructor
    private static class Instruction {
        int move;
        Rotation rotation;
    }

    @Getter
    @AllArgsConstructor
    private enum Rotation {

        CLOCKWISE("R", 1),
        COUNTERCLOCKWISE("L", -1);

        private final String character;
        private final int adjustment;

        public static Rotation build(final String character) {
            return Arrays.stream(values())
                    .filter(rotation -> rotation.getCharacter().equals(character))
                    .findFirst()
                    .orElse(null);
        }
    }

    @Getter
    @AllArgsConstructor
    private enum Direction {

        // Order is important (since we use .ordinal()).
        RIGHT("→", new int[] { 1, 0 }, (current, stream) -> new Puzzle22.Position(stream.filter(p -> p.getY() == current.getY()).mapToInt(Position::getX).min().orElse(0), current.getY())),
        DOWN("↓", new int[] { 0, 1 }, (current, stream) -> new Puzzle22.Position(current.getX(), stream.filter(p -> p.getX() == current.getX()).mapToInt(Position::getY).min().orElse(0))),
        LEFT("←", new int[] { -1, 0 }, (current, stream) -> new Puzzle22.Position(stream.filter(p -> p.getY() == current.getY()).mapToInt(Position::getX).max().orElse(0), current.getY())),
        UP("↑", new int[] { 0, -1 }, (current, stream) -> new Puzzle22.Position(current.getX(), stream.filter(p -> p.getX() == current.getX()).mapToInt(Position::getY).max().orElse(0)));

        private String character;
        private final int[] delta;
        private BiFunction<Puzzle22.Position, Stream<Puzzle22.Position>, Puzzle22.Position> wrapRuleFunction;

        public Direction rotate(final Rotation rotation) {
            if (rotation == null) {
                return this;
            }
            int ordinal = (ordinal() + rotation.getAdjustment()) % values().length;
            return Direction.values()[ordinal < 0 ? values().length - 1 : ordinal];
        }
    }
}