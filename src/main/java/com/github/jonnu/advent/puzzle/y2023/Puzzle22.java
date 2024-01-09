package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle22 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle22.txt")) {

            List<Brick> bricks = new ArrayList<>();

            String line = reader.readLine();
            int i = 0;
            while (line != null) {

                List<Point3D> points = Arrays.stream(line.split("~"))
                        .map(piece -> Point3D.fromArray(Arrays.stream(piece.split(","))
                                .mapToInt(Integer::parseInt)
                                .toArray()))
                        .collect(Collectors.toList());

                bricks.add(Brick.builder()
                        .name(Character.toString((int) 'A' + i))
                        .a(points.get(0))
                        .b(points.get(1))
                        .build());

                line = reader.readLine();
                i++;
            }
            Map<Brick, Set<Point3D>> grid = bricks.stream().collect(Collectors.toMap(Function.identity(), Brick::positions));

            System.out.println("Before Falling");
            draw(grid, Axis.X);
            draw(grid, Axis.Y);

            // start with lowest z.
            List<Brick> orderedBricks = bricks.stream()
                    .sorted(Comparator.comparing(Brick::base))
                    .collect(Collectors.toList());

            // move.
            // stupid bug #1: i wasn't using the ordered collection properly to evaluate dropping bricks.
            // you have to start bottom-to-top otherwise bricks will "fall through each other".
            Set<Point3D> collisions = new HashSet<>();
            for (int j = 0; j < orderedBricks.size(); j++) {
                System.out.println("Dropping " + orderedBricks.get(j).getName());
                collisions.addAll(orderedBricks.get(j).drop(collisions));
            }

            //bricks.forEach(brick -> System.out.printf("Brick %s supports %s brick(s)%n.", brick.getName(), brick.supports(bricks).size()));

            grid = bricks.stream().collect(Collectors.toMap(Function.identity(), Brick::positions));
            System.out.println("After Falling");
            draw(grid, Axis.X);
            draw(grid, Axis.Y);

            HashMap<Brick, Set<Brick>> supported = new HashMap<>();
            bricks.forEach(brick -> brick.supports(bricks).forEach(br -> {
                supported.putIfAbsent(br, new HashSet<>());
                supported.get(br).add(brick);
            }));

            Set<Brick> safeToRemove = new HashSet<>();
            for (Brick brick : bricks) {

                System.out.println("Evaluating " + brick.getName() + " [supports: " + brick.supports(bricks).stream().map(Brick::getName).collect(Collectors.joining(", "))+ "]");

                // If this brick supports no others, nothing will drop, therefore it is
                // safe to disintegrate.
                if (brick.supports(bricks).isEmpty()) {
                    System.out.println(" - " + brick.getName() + " supports no other bricks. safe to remove.");
                    safeToRemove.add(brick);
                    continue;
                }

                boolean allSupportedBricksSupportedElsewhere = true;
                for (Brick b : brick.supports(bricks)) {
                    // if it is supported by more than just this brick, it is safe to remove.
                    // If brick 'b' (that is supported by the parent 'brick') is NOT supported by anything else,
                    // then it is NOT safe to remove.
                    // @TODO this could probably just be !set.of(brick).equals(supported) [i.e. its not JUST this brick supported]
                    // @TODO this might be quicker than doing set diff. check docs for equality vs diff.
                    Set<Brick> difference = Sets.difference(supported.getOrDefault(b, Set.of()), Set.of(brick));
                    allSupportedBricksSupportedElsewhere &= !difference.isEmpty();

                    //System.out.println(" - " + b.getName() + " is also supported by: " + difference.stream().map(Brick::getName).collect(Collectors.joining(", ")));
                }

                // stupid bug #2 - i wasn't considering ALL bricks that another supports before removing it.
                // as soon as one brick didn't need the support, i thought it was safe to remove. no, it has to
                // be ALL of them. hence the bitwise operator on 116 now, which has to be AND not OR.
                if (allSupportedBricksSupportedElsewhere) {
                    System.out.println("  - supported bricks all supported elsewhere. therefore, " + brick.getName() + " is safe to remove.");
                    safeToRemove.add(brick);
                }
            }

            //supported.forEach((brick, supports) -> System.out.println(brick.getName() + " supported by: " + supports.stream().map(Brick::getName).collect(Collectors.joining(", "))));
            //bricks.forEach(b -> System.out.println(b.getName() + " supports " + b.supports(bricks).stream().map(Brick::getName).collect(Collectors.joining(", "))));

            //safeToRemove.forEach(a -> System.out.println(a.getName() + " is safe to remove"));
            //639 too high
            System.out.println(safeToRemove.size() + " bricks can safely be disintegrated.");

            // part2, start with smallest Z.
            // for each brick (node), each edge leads to another node that represents "this would fall if the root was deleted".
        }
    }

    private void draw(final Map<Brick, Set<Point3D>> bricks, final Axis axis) {

        final LongSummaryStatistics uStats = bricks.values().stream().flatMap(Collection::stream).flatMapToLong(point -> LongStream.of(axis.getDimAccessor().apply(point))).summaryStatistics();
        final LongSummaryStatistics zStats = bricks.values().stream().flatMap(Collection::stream).flatMapToLong(point -> LongStream.of(point.getZ())).summaryStatistics();

        // print u/z first
        System.out.printf("%n");
        long uMidPoint = (long) Math.ceil((double) uStats.getMax() / 2);
        long zMidPoint = (long) Math.ceil((double) zStats.getMax() / 2);

        String label = LongStream.rangeClosed(0, uStats.getMax())
                .mapToObj(Long::toString)
                .collect(Collectors.joining());

        System.out.printf("%s%s%n%s%n", " ".repeat((int) uMidPoint), axis, label);

        for (long z = zStats.getMax(); z > 0; z--) {
            for (long u = uStats.getMin(); u <= uStats.getMax(); u++) {

                final long uPos = u;
                final long zPos = z;

                List<Brick> b = bricks.entrySet()
                        .stream()
                        .filter(p -> p.getValue().stream().anyMatch(pos -> axis.getDimPredicate().test(pos, uPos) && pos.getZ() == zPos))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                String c = b.isEmpty() ? "." : b.size() == 1 ? b.get(0).getName() : "?";
                System.out.print(c);
            }

            System.out.printf(" %d %s%n", z, z == zMidPoint ? "z" : "");
        }

        System.out.printf("%s 0%n", "-".repeat((int) uStats.getMax() + 1));
    }

    @Getter
    @AllArgsConstructor
    private enum Axis {

        X(Point3D::getX, (p, x) -> p.getX() == x),
        Y(Point3D::getY, (p, y) -> p.getY() == y),
        Z(Point3D::getZ, (p, z) -> p.getZ() == z)
        ;

        private final Function<Point3D, Long> dimAccessor;
        private final BiPredicate<Point3D, Long> dimPredicate;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class Point3D {

        long x;
        long y;
        long z;

        public static Point3D fromArray(final int[] array) {
            return new Point3D(array[0], array[1], array[2]);
        }

    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class Brick {

        private final String name;

        // a brick stretches between these two points in 3d space.
        private Point3D a;
        private Point3D b;

        public Set<Point3D> drop(final Set<Point3D> collisions) {
            // find the minimum z+1 for each brick position in collisions that match x/y.

            //System.out.println("Collision check: " + collisions + " vs " + positions());

            long lowestZ = positions().stream()
                    .flatMap(p -> collisions.stream().filter(q -> q.getX() == p.getX() && q.getY() == p.getY()))
                    .flatMapToLong(p -> LongStream.of(p.getZ()))
                    .max()
                    .orElse(0L) + 1;
//            System.out.println("dropping " + getName() + " lowest Z would be: " + lowestZ);
//            System.out.println("old a: " + a);
//            System.out.println("old b: " + b);
            // mutate.
            long bz = lowestZ + (b.getZ() - a.getZ());
            a = Point3D.builder()
                    .x(a.getX())
                    .y(a.getY())
                    .z(lowestZ)
                    .build();
            b = Point3D.builder()
                    .x(b.getX())
                    .y(b.getY())
                    .z(bz)
                    .build();
//            System.out.println("new a: " + a);
//            System.out.println("new b: " + b);
            return positions();
        }

        public Set<Brick> supports(final Collection<Brick> bricks) {
            Set<Brick> candidates = new HashSet<>(bricks);

            // a brick is "supported" by another brick if 3D points on
            // its bottom Z 'zone' (with -1 applied) intersect with this
            // brick.

            for (Brick candidate : bricks) {
                if (candidate.equals(this)) {
                    candidates.remove(this);
                    continue;
                }
                boolean supports = candidate.positions()
                        .stream()
                        .filter(point -> point.getZ() == candidate.base())
                        // move DOWN 1.
                        .map(point -> Point3D.builder()
                                .x(point.getX())
                                .y(point.getY())
                                .z(point.getZ() - 1)
                                .build())
                        .anyMatch(p -> positions().contains(p));
                if (!supports) {
                    candidates.remove(candidate);
                }
            }

            //System.out.printf("> %s supports %d bricks (%s).%n", getName(), candidates.size(), candidates.stream().map(Brick::getName).collect(Collectors.joining(", ")));

            return candidates;
        }

        public long base() {
            return positions()
                    .stream()
                    .mapToLong(Point3D::getZ)
                    .min()
                    .orElse(0L);
        }

        // return set of positions this brick is covering.
        public Set<Point3D> positions() {

            // first, figure out which dim is changing.
            boolean isX = a.getX() != b.getX();
            boolean isY = a.getY() != b.getY();
            boolean isZ = a.getZ() != b.getZ();

            long start = Math.min(isX ? a.getX() : isY ? a.getY() : a.getZ(), isX ? b.getX() : isY ? b.getY() : b.getZ());
            long end = Math.max(isX ? a.getX() : isY ? a.getY() : a.getZ(), isX ? b.getX() : isY ? b.getY() : b.getZ());

            return LongStream.rangeClosed(start, end)
                    .mapToObj(q -> Point3D.builder()
                            .x(isX ? q : a.getX())
                            .y(isY ? q : a.getY())
                            .z(isZ ? q : a.getZ())
                            .build())
                    .collect(Collectors.toSet());
        }
    }
}
