package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle14 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle14.txt")) {

            String line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
            }

        }
    }
}
