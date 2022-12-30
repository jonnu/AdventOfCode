package com.github.jonnu.advent.puzzle.y2021;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle7 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2021/puzzle7.txt")) {

            final List<Long> crabs = Arrays.stream(reader.readLine().split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            final LongSummaryStatistics statistics = crabs.stream().mapToLong(l -> l).summaryStatistics();
            final CrabResult minimalConstant = minimalFuelCost(statistics, position -> calculateConstantFuelCost(position, crabs));
            final CrabResult minimalProgressive = minimalFuelCost(statistics, position -> calculateProgressiveFuelCost(position, crabs));

            System.out.printf("[   Constant] Minimum fuel spent to get to position %3d: %d%n", minimalConstant.getPosition(), minimalConstant.getFuelCost());
            System.out.printf("[Progressive] Minimum fuel spent to get to position %3d: %d%n", minimalProgressive.getPosition(), minimalProgressive.getFuelCost());
        }
    }

    private static CrabResult minimalFuelCost(final LongSummaryStatistics statistics, final LongFunction<CrabResult> fuelCostFunction) {
        return LongStream.rangeClosed(statistics.getMin(), statistics.getMax())
                .parallel()
                .mapToObj(fuelCostFunction)
                .min(Comparator.comparing(CrabResult::getFuelCost))
                .orElseThrow(() -> new IllegalStateException("Unable to find cheapest Crab position."));
    }

    private static CrabResult calculateFuelCost(final long crab, final List<Long> crabs, final ToLongFunction<Long> fuelFunction) {
        return new CrabResult(crab, crabs.stream().mapToLong(fuelFunction).sum());
    }

    private static CrabResult calculateConstantFuelCost(final long crab, final List<Long> crabs) {
        return calculateFuelCost(crab, crabs, n -> Math.abs(crab - n));
    }

    private static CrabResult calculateProgressiveFuelCost(final long crab, final List<Long> crabs) {
        return calculateFuelCost(crab, crabs, n -> LongStream.rangeClosed(1, Math.abs(crab - n)).sum());
    }

    @Value
    @ToString
    @AllArgsConstructor
    private static class CrabResult {
        long position;
        long fuelCost;
    }

}
