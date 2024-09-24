package com.github.jonnu.advent.common.graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AStar {

    public <T> SearchResult<T> shortestPath(final T start, final T end) {
        /*
        fun <K> findShortestPath(
        start: K,
        end: K,
        neighbours: NeighbourFunction<K>,
        cost: CostFunction<K> = { _, _ -> 1 },
        heuristic: HeuristicFunction<K> = { 0 }
        ): GraphSearchResult<K> = findShortestPathByPredicate(start, { it == end }, neighbours, cost, heuristic)
         */
        return shortestPath(start, end, null, (x, y) -> 1, x -> 0);
    }

    public <T> SearchResult<T> shortestPath(final T start, final T end, final Neighbours<T> neighbourFunction, final Cost<T> costFunction, final Heuristic<T> heuristicFunction) {

        /*
        fun <K> findShortestPath(
        start: K,
        end: K,
        neighbours: NeighbourFunction<K>,
        cost: CostFunction<K> = { _, _ -> 1 },
        heuristic: HeuristicFunction<K> = { 0 }
        ): GraphSearchResult<K> = findShortestPathByPredicate(start, { it == end }, neighbours, cost, heuristic)
         */
        return null;
    }

    // function to get neighbours of T.
    @FunctionalInterface
    public interface Neighbours<T> {
        List<T> neighbours(final T node);
    }

    // cost of going from T1->T2.
    @FunctionalInterface
    public interface Cost<T> {
        int cost(final T from, final T to);
    }

    // weighting of T.
    @FunctionalInterface
    public interface Heuristic<T> {
        int heuristic(final T node);
    }
}
