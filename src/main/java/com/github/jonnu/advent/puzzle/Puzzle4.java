package com.github.jonnu.advent.puzzle;

import java.io.BufferedReader;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle4 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        BufferedReader reader = resourceReader.read("puzzle4.txt");
        String line = reader.readLine();

        int fullyOverlappingAssignments = 0;
        int partiallyOverlappingAssignments = 0;
        while (line != null) {
            String[] pieces = line.split("[-,]", 4);

            final Assignment left = Assignment.builder()
                    .start(Integer.parseInt(pieces[0]))
                    .finish(Integer.parseInt(pieces[1]))
                    .build();
            final Assignment right = Assignment.builder()
                    .start(Integer.parseInt(pieces[2]))
                    .finish(Integer.parseInt(pieces[3]))
                    .build();

            if (left.fullyOverlaps(right) || right.fullyOverlaps(left)) {
                fullyOverlappingAssignments++;
            }

            if (left.partiallyOverlaps(right) || right.partiallyOverlaps(left)) {
                partiallyOverlappingAssignments++;
            }

            line = reader.readLine();
        }

        reader.close();

        System.out.println("Fully overlapping assignments: " + fullyOverlappingAssignments);
        System.out.println("Partially overlapping assignments: " + partiallyOverlappingAssignments);
    }

    @Value
    @Builder
    static class Assignment {

        int start;
        int finish;

        public boolean fullyOverlaps(final Assignment assignment) {
            return getStart() <= assignment.getStart() && getFinish() >= assignment.getFinish();
        }

        public boolean partiallyOverlaps(final Assignment assignment) {
            return getFinish() >= assignment.getStart() && getFinish() <= assignment.getFinish();
        }
    }
}
