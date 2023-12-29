package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle24 implements Puzzle {

    private static final Pattern PATTERN = Pattern.compile("^(?<px>-?\\d+),\\s+(?<py>-?\\d+),\\s+(?<pz>-?\\d+)\\s@\\s+(?<vx>-?\\d+),\\s+(?<vy>-?\\d+),\\s+(?<vz>-?\\d+)$");

    private final ResourceReader resourceReader;

    private final Set<Hailstone> hailstones = new HashSet<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle24.txt")) {

            String line = reader.readLine();
            while (line != null) {
                Matcher matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Unable to parse: " + line);
                }

                hailstones.add(Hailstone.builder()
                        .position(new Point3d(matcher.group("px"), matcher.group("py"), matcher.group("vz")))
                        .vector(new Vector3d(matcher.group("vx"), matcher.group("vy"), matcher.group("vz")))
                        .build());

                line = reader.readLine();
            }


            int time = 0;
            int testXmin = 7;
            int testYmin = 7;
            int testXmax = 27;
            int testYmax = 27;

            long min = 200_000_000_000_000L;
            long max = 400_000_000_000_000L;

            List<Line3d> lines = hailstones.stream()
                    .map(h -> new Line3d(h.getPosition(), h.getVector()))
                    .collect(Collectors.toList());

            List<Pair<Line3d>> linePairs = lines.stream()
                    .flatMap(left -> lines.stream()
                            .filter(right -> !left.equals(right))
                            .map(right -> Pair.of(left, right))
                            .distinct())
                    .distinct()
                    .collect(Collectors.toList());

            linePairs.forEach(pair -> {
                Line3d a = pair.getLeft();
                Line3d b = pair.getRight();

                System.out.printf("Hailstone A: %d, %d, %d @ %d, %d, %d%n", a.getPoint().getX(), a.getPoint().getY(), a.getPoint().getZ(), a.getVector().getX(), a.getVector().getY(), a.getVector().getZ());
                System.out.printf("Hailstone B: %d, %d, %d @ %d, %d, %d%n", b.getPoint().getX(), b.getPoint().getY(), b.getPoint().getZ(), b.getVector().getX(), b.getVector().getY(), b.getVector().getZ());

                
                //System.out.println();
            });

            // convert hailstones into two lines.
            // and now pair them.
            System.out.println(linePairs.size() + ": " + linePairs);

        }
    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class Point3d {

        private final long x;
        private final long y;
        private final long z;

        public <T> Point3d(final T x, final T y, final T z) {
            this.x = Integer.parseInt(x.toString());
            this.y = Integer.parseInt(y.toString());
            this.z = Integer.parseInt(z.toString());
        }

        @Override
        public String toString() {
            return String.format("(%d, %d, %d)", x, y, z);
        }
    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class Vector3d {

        private final long x;
        private final long y;
        private final long z;

        public <T> Vector3d(final T x, final T y, final T z) {
            this.x = Integer.parseInt(x.toString());
            this.y = Integer.parseInt(y.toString());
            this.z = Integer.parseInt(z.toString());
        }

        @Override
        public String toString() {
            return String.format("[%d, %d, %d]", x, y, z);
        }
    }


    public static class Line2d {

    }


    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static class Line3d {
        Point3d point;
        Vector3d vector;
    }


    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static class Pair<T> {

        T left;
        T right;

        public static <T> Pair<T> of(final T left, final T right) {
            return new Pair<>(left, right);
        }

        @Override
        public int hashCode() {
            return 59 + Arrays.deepHashCode(Set.of(getLeft(), getRight()).toArray());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Pair)) {
                return false;
            }
            Pair<T> other = (Pair<T>) obj;
            return Set.of(getLeft(), getRight()).equals(Set.of(other.getLeft(), other.getRight()));
        }
    }

    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static class Hailstone {
        private Point3d position;
        private Vector3d vector;
    }

}
