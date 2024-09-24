package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;

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
                        .position(new Point3d(matcher.group("px"), matcher.group("py"), matcher.group("pz")))
                        .vector(new Vector3d(matcher.group("vx"), matcher.group("vy"), matcher.group("vz")))
                        .build());

                line = reader.readLine();
            }

            // int time = 0;
            // int testXmin = 7;
            // int testYmin = 7;
            // int testXmax = 27;
            // int testYmax = 27;

            // long min = 200_000_000_000_000L;
            // long max = 400_000_000_000_000L;

//            List<Line3d> lines = hailstones.stream()
//                    .map(h -> new Line3d(h.getPosition(), h.getVector()))
//                    .collect(Collectors.toList());
//
//            List<Pair<Line3d>> linePairs = lines.stream()
//                    .flatMap(left -> lines.stream()
//                            .filter(right -> !left.equals(right))
//                            .map(right -> Pair.of(left, right))
//                            .distinct())
//                    .distinct()
//                    .collect(Collectors.toList());

            List<Pair<Hailstone>> hailPairs = hailstones.stream()
                    .flatMap(left -> hailstones.stream()
                            .filter(right -> !left.equals(right))
                            .map(right -> Pair.of(left, right))
                            .distinct())
                    .distinct()
                    .collect(Collectors.toList());

            //Pair<Point3d> testBounds = Pair.of(
            //        new Point3d(7, 7, 7),
            //        new Point3d(27, 27, 27)
            //);

            hailPairs.forEach(pair -> {

                Hailstone a = pair.getLeft();
                Hailstone b = pair.getRight();

                System.out.printf("Hailstone A: %d, %d, %d @ %d, %d, %d%n", a.getPosition().getX(), a.getPosition().getY(), a.getPosition().getZ(), a.getVector().getX(), a.getVector().getY(), a.getVector().getZ());
                System.out.printf("Hailstone B: %d, %d, %d @ %d, %d, %d%n", b.getPosition().getX(), b.getPosition().getY(), b.getPosition().getZ(), b.getVector().getX(), b.getVector().getY(), b.getVector().getZ());

                a.intersection(b);

                Line2d al = a.move2d(50);
                Line2d bl = b.move2d(50);

                System.out.println("Hailstones' paths ...: " + al.intersection(bl));
                System.out.println();
            });

            // convert hailstones into two lines.
            // and now pair them.
            //System.out.println(linePairs.size() + ": " + linePairs);

        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    public static class Point2d<T> {
        T x;
        T y;
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
    public static class Vector2d {

        private final long x;
        private final long y;

        public <T> Vector2d(final T x, final T y) {
            this.x = Integer.parseInt(x.toString());
            this.y = Integer.parseInt(y.toString());
        }

        @Override
        public String toString() {
            return String.format("[%d, %d]", x, y);
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

    @Value
    @AllArgsConstructor
    public static class Line2d {

        // line between two points in 2d space.
        Point a;
        Point b;

        public Optional<Point2d<Double>> intersection(final Line2d other) {

            // double a1 = l1.e.y - l1.s.y
            double a1 = getB().getY() - getA().getY();

            // double b1 = l1.s.x - l1.e.x
            double b1 = getA().getX() - getB().getX();

            // double c1 = a1 * l1.s.x + b1 * l1.s.y
            double c1 = a1 * getA().getX() + b1 * getA().getY();

            // double a2 = l2.e.y - l2.s.y
            double a2 = other.getB().getY() - other.getA().getY();

            // double b2 = l2.s.x - l2.e.x
            double b2 = other.getA().getX() - other.getB().getX();

            // double c2 = a2 * l2.s.x + b2 * l2.s.y
            double c2 = a2 * other.getA().getX() + b2 * other.getA().getY();

            // double delta = a1 * b2 - a2 * b1
            double delta = a1 * b2 - a2 * b1;

            // (b.y-a.y) / (b.x-a.x)
            double s1 = (double) (getB().getY() - getA().getY()) / (getB().getX() - getA().getX());
            double s2 = (double) (other.getB().getY() - other.getA().getY()) / (getB().getX() - getA().getX());

            if (s1 == s2) {
                return Optional.empty();
            }

            return Optional.of(new Point2d<>(
                    (b2 * c1 - b1 * c2) / delta,
                    (a1 * c2 - a2 * c1) / delta
            ));
        }
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
        private final Vector3d vector;

        public Line2d move2d() {
            return move2d(1);
        }

        public Line2d move2d(final long magnitude) {
            Point start = new Point((int) position.getX(), (int) position.getY());
            Point end = new Point(start.getX() + (int)(magnitude * vector.getX()), start.getY() + (int)(magnitude * vector.getY()));
            return new Line2d(start, end);
        }

        public Line3d move() {
            return move(1);
        }

        public Line3d move(long magnitude) {
            // @TODO really should make Mutable point classes.
            position = new Point3d(
                    position.getX() + vector.getX(),
                    position.getY() + vector.getY(),
                    position.getZ() + vector.getZ()
            );
            return null;
        }

        public Optional<Point2d<Double>> intersection(final Hailstone other) {
            return Optional.empty();
        }
    }

}
