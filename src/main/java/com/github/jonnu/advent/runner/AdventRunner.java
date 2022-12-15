package com.github.jonnu.advent.runner;

import com.github.jonnu.advent.inject.AdventModule;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.github.jonnu.advent.puzzle.y2022.Puzzle15;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class AdventRunner {
    public static void main(final String[] args) {
        Injector injector = Guice.createInjector(new AdventModule());
        Puzzle puzzle = injector.getInstance(Puzzle15.class);
        long start = System.currentTimeMillis();
        puzzle.solve();
        System.out.printf("%n[Time taken: %dms]%n", System.currentTimeMillis() - start);
    }
}
