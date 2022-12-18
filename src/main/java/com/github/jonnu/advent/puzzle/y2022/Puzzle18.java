package com.github.jonnu.advent.puzzle.y2022;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle18 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle18.txt")) {

            final VoxelGrid grid = new VoxelGrid();

            String line = reader.readLine();
            while (line != null) {
                grid.addVoxel(Voxel.build(Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray()));
                line = reader.readLine();
            }

            long scannedSurfaceArea = grid.getVoxels().stream()
                    .mapToLong(voxel -> 6 - voxel.neighbours().stream()
                            .filter(grid::containsVoxel)
                            .count())
                    .sum();

            final AtomicInteger exteriorSurfaceArea = new AtomicInteger();
            final Queue<Voxel> queue = new ArrayDeque<>(Set.of(grid.getMinimum()));
            final Set<Voxel> visited = new HashSet<>();

            // bfs
            while (!queue.isEmpty()) {
                final Voxel next = queue.poll();
                if (!visited.contains(next)) {
                    next.neighbours().stream().filter(grid::withinGrid).forEach(voxel -> {
                        if (grid.containsVoxel(voxel)) {
                            exteriorSurfaceArea.getAndIncrement();
                        } else {
                            queue.add(voxel);
                        }
                        visited.add(next);
                    });
                }
            }

            System.out.println("Surface area of scanned lava droplet: " + scannedSurfaceArea);
            System.out.println("Exterior surface area of scanned lava droplet: " + exteriorSurfaceArea);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class VoxelGrid {

        private Set<Voxel> voxels = new HashSet<>();
        private Voxel minimum = Voxel.build(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        private Voxel maximum = Voxel.build(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);;

        public void addVoxel(final Voxel voxel) {

            voxels.add(voxel);

            if (voxel.getX() - 1  < minimum.getX()) {
                minimum.setX(voxel.getX() - 1);
            }

            if (voxel.getY() - 1  < minimum.getY()) {
                minimum.setY(voxel.getY() - 1);
            }

            if (voxel.getZ() - 1  < minimum.getZ()) {
                minimum.setZ(voxel.getZ() - 1);
            }

            if (voxel.getX() + 1 > maximum.getX()) {
                maximum.setX(voxel.getX() + 1);
            }

            if (voxel.getY() + 1 > maximum.getY()) {
                maximum.setY(voxel.getY() + 1);
            }

            if (voxel.getZ() + 1 > maximum.getZ()) {
                maximum.setZ(voxel.getZ() + 1);
            }
        }

        public boolean containsVoxel(final Voxel voxel) {
            return voxels.contains(voxel);
        }

        public boolean withinGrid(final Voxel voxel) {
            return voxel.getX() >= minimum.getX() && voxel.getX() <= maximum.getX() &&
                    voxel.getY() >= minimum.getY() && voxel.getY() <= maximum.getY() &&
                    voxel.getZ() >= minimum.getZ() && voxel.getZ() <= maximum.getZ();
        }
    }

    @Data
    @AllArgsConstructor
    private static class Voxel {

        int x;
        int y;
        int z;

        public Set<Voxel> neighbours() {
            return ImmutableSet.of(
                    new Voxel(x + 1, y, z),
                    new Voxel(x - 1, y, z),
                    new Voxel(x, y + 1, z),
                    new Voxel(x, y - 1, z),
                    new Voxel(x, y, z + 1),
                    new Voxel(x, y, z - 1)
            );
        }

        public static Voxel build(final int[] coordinates) {
            return build(coordinates[0], coordinates[1], coordinates[2]);
        }

        public static Voxel build(final int x, final int y, final int z) {
            return new Voxel(x, y, z);
        }

        public String toString() {
            return String.format("(%d, %d, %d)", x, y, z);
        }
    }

}
