package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Rotation;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle1 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2025/puzzle1.txt")) {

            int count = 0;
            int position = 50;

            String line = reader.readLine();
            while (line != null) {

                final Rotation rotation = Rotation.fromString(line.substring(0, 1));
                final int amount = Integer.parseInt(line.substring(1));

                int direction = switch (rotation) {
                    case Rotation.CLOCKWISE -> 1;
                    case Rotation.ANTICLOCKWISE -> -1;
                };

                for (int i = 0; i < amount; i++) {
                    position = (position + direction) % 100;
                    if (position == 0) {
                        count++;
                    }
                }

                line = reader.readLine();
            }

            System.out.printf("%nTimes hit zero: %d;%n", count);
        }
    }
}
