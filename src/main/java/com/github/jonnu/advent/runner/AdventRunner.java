package com.github.jonnu.advent.runner;

import com.github.jonnu.advent.inject.AdventModule;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.github.jonnu.advent.puzzle.y2024.*;
import com.google.common.base.Stopwatch;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class AdventRunner {
    public static void main(final String[] args) {
        Injector injector = Guice.createInjector(new AdventModule());
        Puzzle puzzle = injector.getInstance(Puzzle3.class);
        solve(puzzle);
    }

    private static void solve(final Puzzle puzzle) {
        System.out.printf("Puzzle: %s%n%n", puzzle.getClass().getCanonicalName());
        Stopwatch watch = Stopwatch.createStarted();
        puzzle.solve();
        System.out.printf("%nTime taken: %s%n", watch.stop());
    }
}
