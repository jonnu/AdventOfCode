package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.function.BiConsumer;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle2 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle2.txt")) {

            Submarine submarine = new Submarine();
            String line = reader.readLine();
            while (line != null) {
                String[] args = line.split(" ");

                Instruction instruction = Instruction.fromString(args[0]);
                int amplitude = Integer.parseInt(args[1]);
                submarine.move(instruction, amplitude);

                line = reader.readLine();
            }

            System.out.println("horizontal position x depth (part 1): " + submarine.getPositionOne().multiply());
            System.out.println("horizontal position x depth (part 2): " + submarine.getPositionTwo().multiply());
        }
    }

    @Getter
    @ToString
    @NoArgsConstructor
    private static class Position {

        private int x;
        private int y;

        public void move(final int deltaX, final int deltaY) {
            x += deltaX;
            y += deltaY;
        }

        public int multiply() {
            return x * y;
        }
    }

    @Getter
    @ToString
    @NoArgsConstructor
    private static class Submarine {

        private int aim;
        private final Position positionOne = new Position();
        private final Position positionTwo = new Position();

        public void move(final Instruction instruction, final int amplitude) {
            instruction.getModeOne().accept(this, amplitude);
            instruction.getModeTwo().accept(this, amplitude);
        }

        public void aim(final int aimDelta) {
            aim += aimDelta;
        }

    }

    @Getter
    @AllArgsConstructor
    private enum Instruction {

        FORWARD((sub, amp) -> sub.getPositionOne().move(amp, 0),
                (sub, amp) -> sub.getPositionTwo().move(amp, amp * sub.getAim())),
        DOWN((sub, amp) -> sub.getPositionOne().move(0, amp),
                (sub, amp) -> sub.aim(amp)),
        UP((sub, amp) -> sub.getPositionOne().move(0, -amp),
                (sub, amp) -> sub.aim(-amp));

        private BiConsumer<Submarine, Integer> modeOne;
        private BiConsumer<Submarine, Integer> modeTwo;

        public static Instruction fromString(final String instruction) {
            return Arrays.stream(values())
                    .filter(ins -> ins.name().equalsIgnoreCase(instruction))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown instruction: " + instruction));
        }
    }

}
