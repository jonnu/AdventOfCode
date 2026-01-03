package com.github.jonnu.advent.puzzle.y2025;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle8 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {

        List<Point3D> junctions = new ArrayList<>();
        try (BufferedReader reader = resourceReader.read("y2025/puzzle8.txt")) {
            String line = reader.readLine();
            while (line != null) {
                int[] points = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
                junctions.add(new Point3D(points[0], points[1], points[2]));
                line = reader.readLine();
            }
        }

        Queue<Connection> connections = new PriorityQueue<>(Comparator.comparingDouble(Connection::distance));
        connections.addAll(junctions.stream()
                .map(junction -> Set.copyOf(junctions)
                        .stream()
                        .filter(point3d -> !point3d.equals(junction))
                        .map(point3d -> Connection.between(junction, point3d))
                        .collect(Collectors.toSet())
                )
                .flatMap(Set::stream)
                .collect(Collectors.toSet()));

        Map<Point3D, Integer> memo = new HashMap<>();
        Map<Integer, List<Point3D>> circuits = new HashMap<>();
        for (int i = 1; i <= junctions.size(); i++) {
            memo.put(junctions.get(i - 1), i);
            circuits.put(i, new ArrayList<>(Set.of(junctions.get(i - 1))));
        }

        // https://en.wikipedia.org/wiki/Disjoint-set_data_structure
        for (int i = 0; i < 10; i++) {
            Connection connection = connections.poll();
            if (connection == null) {
                break;
            }

            //System.out.println(connection);

            // remove right connection and push
            int left = memo.getOrDefault(connection.left(), -1);
            int right = memo.getOrDefault(connection.right(), -1);

            System.out.println("L: " + left + "; R: " + right);
            if (left != right) {
                List<Point3D> rightPoints = circuits.getOrDefault(right, List.of());
                circuits.getOrDefault(left, new ArrayList<>()).addAll(rightPoints);
                //circuits.get(left).add(connection.right());
                memo.put(connection.right(), left);
                //rightPoints.forEach(p -> );
                circuits.remove(right);
            }

            //circuits.entrySet().stream().sorted(Comparator.comparingInt(a -> a.getValue().size()))
            //        .forEach(a -> System.out.println(a.getKey() + ": " + a.getValue()));
            //System.out.println();
        }

        int size = circuits.values()
                .stream()
                .sorted((a, b) -> Integer.compare(b.size(), a.size()))
                .limit(3)
                .peek(System.out::println)
                .mapToInt(List::size)
                .reduce(Math::multiplyExact)
                .orElse(0);

        System.out.println("[Part 1] Size: " + size);


//        for (int i = 0; i < 10; i++) {
//            Connection connection = connections.poll();
//
//            // is this a new circuit?
//            if (!memo.containsKey(connection.left()) && !memo.containsKey(connection.right())) {
//                circuits.add(connection.getJunctions());
//                int j = circuits.size() - 1;
//                memo.put(connection.left(), j);
//                memo.put(connection.right(), j);
//                continue;
//            }
//
//            int j = memo.getOrDefault(connection.left(), -1);
//            int q = memo.getOrDefault(connection.right(), -1);
//            int z = j == -1 ? q : j;
//            circuits.get(z).addAll(connection.getJunctions());
//            memo.put(connection.left(), z);
//            memo.put(connection.right(), z);
//        }
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    class Point3D {

        final int x;
        final int y;
        final int z;

        public double euclideanDistance(final Point3D point) {
            return Math.sqrt(
                    Math.pow(getX() - point.getX(), 2) +
                    Math.pow(getY() - point.getY(), 2) +
                    Math.pow(getZ() - point.getZ(), 2)
            );
        }
    }

    record Connection(@NonNull Point3D left, @NonNull Point3D right, double distance) {

        public static Connection between(final Point3D a, final Point3D b) {
            return new Connection(a, b, a.euclideanDistance(b));
        }

        public HashSet<Point3D> getJunctions() {
            return new HashSet<>(Set.of(left(), right()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != this.getClass()) {
                return false;
            }
            Connection other = (Connection) obj;
            return Set.of(left, right).equals(Set.of(other.left(), other.right())) && distance == other.distance();
        }

        @Override
        public int hashCode() {
            int total = 31;

            total = total * 31 + (Math.min(left.hashCode(), right.hashCode()));
            total = total * 31 + (Math.max(right.hashCode(), left.hashCode()));
            total = total * 31 + (int) distance;

            return total;
        }
    }
}
