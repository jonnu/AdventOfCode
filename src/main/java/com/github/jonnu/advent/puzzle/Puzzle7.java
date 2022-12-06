package com.github.jonnu.advent.puzzle;

import java.io.BufferedReader;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle7 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("puzzle7.txt")) {

        }
    }

}
