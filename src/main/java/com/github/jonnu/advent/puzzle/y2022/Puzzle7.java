package com.github.jonnu.advent.puzzle.y2022;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle7 implements Puzzle {

    private static final int FILESYSTEM_SIZE = 70_000_000;
    private static final int COMMS_UPDATE_SIZE = 30_000_000;
    private static final int ACCUMULATOR_THRESHOLD = 100_000;

    private static final AtomicInteger ACCUMULATOR = new AtomicInteger(0);
    private static final PriorityQueue<File> DIRECTORY_SIZE_HEAP = new PriorityQueue<>(Comparator.comparingInt(File::getSize));

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        File root = File.createRoot();
        File current = root;
        try (BufferedReader reader = resourceReader.read("y2022/puzzle7.txt")) {
            String line = reader.readLine();
            while (line != null) {

                String[] raw = line.split(" ");
                switch (raw[0]) {
                    case "$" -> {
                        final String command = raw[1];
                        final String parameter = raw.length > 2 ? raw[2] : "";

                        if ("cd".equals(command)) {
                            switch (parameter) {
                                case "/" -> current = root;
                                case ".." -> current = current.getParent();
                                default -> current = current.getChildren()
                                        .stream()
                                        .filter(f -> f.getName().equals(parameter))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalArgumentException("no such child: " + parameter));
                            }
                        }
                    }
                    case "dir" -> {
                        File directory = File.builder()
                                .parent(current)
                                .type(FileType.DIRECTORY)
                                .name(raw[1])
                                .size(0)
                                .build();

                        current.addChild(directory);
                    }
                    default -> {
                        File file = File.builder()
                                .type(FileType.FILE)
                                .name(raw[1])
                                .size(Integer.parseInt(raw[0]))
                                .parent(current)
                                .children(null)
                                .build();

                        current.addChild(file);
                    }
                }

                line = reader.readLine();
            }

            printDebug(root);

            System.out.println("Accumulated size of sub-100K size directories: " + ACCUMULATOR.get());

            Optional.ofNullable(getSmallestCandidateDirectory(Math.abs((FILESYSTEM_SIZE - root.getSize()) - COMMS_UPDATE_SIZE)))
                    .ifPresent(file -> System.out.println("Directory to delete for update: " + file.getName() + " (size: " + file.getSize() + ")"));
        }
    }

    @Nullable
    private static File getSmallestCandidateDirectory(final int sizeThreshold) {
        File file = DIRECTORY_SIZE_HEAP.poll();
        while (file != null) {
            if (file.getSize() > sizeThreshold) {
                return file;
            }
            file = DIRECTORY_SIZE_HEAP.poll();
        }
        return null;
    }

    private static void printDebug(File file) {
        printDebug(file, 0);
    }

    private static void printDebug(File file, int depth) {

        for (int i = 0; i < depth; i++) {
            System.out.print("  ");
        }

        System.out.printf("- %s %s%n", file.getName(), file.getDescription());
        file.getChildren().forEach(child -> {
            if (child.getType().equals(FileType.DIRECTORY)) {
                printDebug(child, depth + 1);
                if (child.getSize() <= ACCUMULATOR_THRESHOLD) {
                    ACCUMULATOR.getAndAdd(child.getSize());
                }

                DIRECTORY_SIZE_HEAP.add(child);
            } else if (child.getType().equals(FileType.FILE)) {
                for (int i = 0; i <= depth; i++) {
                    System.out.print("  ");
                }
                System.out.printf("- %s %s%n", child.getName(), child.getDescription());
            }
        });
    }

    @Getter
    @AllArgsConstructor
    private enum FileType {
        FILE("file"),
        DIRECTORY("dir");

        final String description;
    }

    @Data
    @Builder
    @ToString(exclude = "parent")
    private static class File {
        private FileType type;
        private File parent;
        @Builder.Default private List<File> children = new ArrayList<>();
        private String name;
        private int size;

        public void addChild(File child) {
            children.add(child);
        }

        public int getSize() {

            if (getType().equals(FileType.FILE)) {
                return size;
            }

            return getChildren()
                    .stream()
                    .map(File::getSize)
                    .reduce(0, Integer::sum);
        }

        public String getDescription() {
            StringBuilder b = new StringBuilder("(");
            b.append(type.getDescription());
            if (type.equals(FileType.FILE)) {
                b.append(", size=");
                b.append(getSize());
            } else {
                b.append(", cum=");
                b.append(getSize());
            }
            b.append(")");
            return b.toString();
        }

        public static File createRoot() {
            return File.builder()
                    .type(FileType.DIRECTORY)
                    .name("/")
                    .size(0)
                    .parent(null)
                    .children(new ArrayList<>())
                    .build();
        }

    }
}
