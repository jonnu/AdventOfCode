package com.github.jonnu.advent.puzzle.y2024;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle2 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2024/puzzle2.txt")) {

            final List<List<Integer>> reports = new ArrayList<>();
            String line = reader.readLine();
            while (line != null) {

                reports.add(Arrays.stream(line.split("\\s+"))
                        .map(Integer::parseInt)
                        .toList());

                line = reader.readLine();
            }

            final long safeReports = reports.stream()
                    .filter(Puzzle2::isReportSafe)
                    .count();

            long dampenedSafeReports = reports.stream()
                    .filter(report -> isReportSafe(report) || isReportSafeWithProblemDampener(report))
                    .count();

            System.out.println("Safe reports: " + safeReports);
            System.out.println("Safe reports with problem dampener: " + dampenedSafeReports);
        }
    }

    private static boolean isReportSafe(final List<Integer> report) {
        ReportDirection lastState = null;
        for (int i = 0, j = 1; j < report.size(); i++, j++) {

            final ReportDirection thisState = report.get(i) < report.get(j) ? ReportDirection.INCREASING : ReportDirection.DECREASING;
            final int difference = Math.abs(report.get(i) - report.get(j));
            boolean stateChangeViolation = lastState != null && !thisState.equals(lastState);
            boolean differenceViolation = difference <= 0 || difference > 3;

            if (stateChangeViolation || differenceViolation) {
                return false;
            }

            lastState = thisState;
        }

        return true;
    }

    private static boolean isReportSafeWithProblemDampener(final List<Integer> report) {
        return IntStream.range(0, report.size())
                .mapToObj(i -> IntStream.range(0, report.size())
                        .filter(index -> index != i)
                        .mapToObj(report::get)
                        .toList())
                .map(ArrayList::new)
                .anyMatch(Puzzle2::isReportSafe);
    }

    private enum ReportDirection {
        INCREASING,
        DECREASING
    }
}
