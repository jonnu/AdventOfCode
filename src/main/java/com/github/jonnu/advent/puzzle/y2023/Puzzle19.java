package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.Accessors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle19 implements Puzzle {

    private static final Pattern PATTERN = Pattern.compile("((?<type>[xmas])(?<comparator>[<>])(?<value>\\d+):)?((?<destination>\\w+)|(?<finaliser>[AR]))$");

    private final ResourceReader resourceReader;

    private final Map<String, Workflow> workflows = new HashMap<>();
    private final List<Part> parts = new ArrayList<>();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle19.txt")) {

            boolean parseWorkflows = true;
            String line = reader.readLine();
            while (line != null) {

                if (line.isBlank()) {
                    parseWorkflows = false;
                    line = reader.readLine();
                    continue;
                }

                if (parseWorkflows) {

                    String name = line.substring(0, line.indexOf('{'));
                    String[] parts = line.substring(line.indexOf('{') + 1, line.length() - 1).split(",");

                    List<WorkflowPredicate> predicates = new ArrayList<>();

                    for (String part : parts) {

                        Matcher matcher = PATTERN.matcher(part);
                        if (!matcher.matches()) {
                            throw new IllegalArgumentException("Invalid argument: " + part);
                        }

                        if (IntStream.rangeClosed(2, 4).allMatch(i -> matcher.group(i) != null)) {

                            WorkflowPredicate predicate = WorkflowValuePredicate.builder()
                                    .type(matcher.group(2))
                                    .operator(WorkflowOperator.parse(matcher.group(3)))
                                    .value(Integer.parseInt(matcher.group(4)))
                                    .destination(matcher.group(5))
                                    .build();

                            predicates.add(predicate);
                            continue;
                        }

                        // pass thru result.
                        predicates.add(x -> WorkflowResult.builder()
                                .resolved(true)
                                .destination(matcher.group(6))
                                .build());
                    }

                    Workflow workflow = Workflow.builder()
                            .name(name)
                            .predicates(predicates)
                            .build();

                    workflows.put(workflow.getName(), workflow);
                } else {

                    Part part = Part.builder()
                            .values(Arrays.stream(line.substring(1, line.length() - 1)
                                    .split(","))
                                    .map(piece -> piece.split("="))
                                    .collect(Collectors.toMap(k -> k[0], v -> Integer.parseInt(v[1]))))
                            .build();

                    parts.add(part);
                }

                line = reader.readLine();
            }

            int total = 0;
            for (Part part: parts) {
                String workflow = "in";
                boolean complete = false;
                System.out.printf("%s: %s", part, workflow);
                while (true) {
                    Workflow wf = workflows.get(workflow);

                    WorkflowResult result = wf.evaluate(part);
                    if (result.resolved()) {
                        if (result.isAccepted() || result.isRejected()) {
                            System.out.printf(" -> %s %n", result.getDestination());
                            complete = result.isAccepted();
                            break;
                        }

                        System.out.printf(" -> %s", result.getDestination());
                        workflow = result.getDestination();
                    }
                }

                if (complete) {
                    total += part.total();
                }
            }

            System.out.println("Total of accepted rating numbers: " + total);
        }
    }

    @Value
    @Builder
    private static class Workflow {

        String name;
        List<WorkflowPredicate> predicates;

        private WorkflowResult evaluate(final Part part) {
            return predicates.stream()
                    .map(predicate -> predicate.evaluate(part))
                    .filter(WorkflowResult::resolved)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unable to evaluate workflow: " + getName()));
        }
    }

    private interface WorkflowPredicate {
        WorkflowResult evaluate(Part part);
    }

    @Value
    @Builder
    private static class WorkflowValuePredicate implements WorkflowPredicate {

        String type;
        WorkflowOperator operator;
        int value;
        String destination;

        public WorkflowResult evaluate(final Part part) {
            boolean result = operator.getPredicate().test(part.get(type), value);
            return WorkflowResult.builder()
                    .resolved(result)
                    .destination(result ? destination : null)
                    .build();
        }
    }

    @Value
    @Builder
    private static class WorkflowResult {

        @Accessors(fluent = true) boolean resolved;
        String destination;

        public boolean isAccepted() {
            return WorkflowTerminator.ACCEPTED.getLetter().equals(destination);
        }

        public boolean isRejected() {
            return WorkflowTerminator.REJECTED.getLetter().equals(destination);
        }
    }

    @Value
    @Builder
    private static class Part {

        Map<String, Integer> values;

        public int get(final String type) {
            return values.get(type);
        }

        public int total() {
            return values.values().stream().mapToInt(x -> x).sum();
        }

        @Override
        public String toString() {
            return String.format("{x=%d,m=%d,a=%d,s=%d}", values.get("x"), values.get("m"), values.get("a"), values.get("s"));
        }
    }

    @Getter
    @AllArgsConstructor
    private enum WorkflowTerminator {

        ACCEPTED("A"),
        REJECTED("R");

        private final String letter;

        public static WorkflowTerminator parse(final String input) {
            return Arrays.stream(values())
                    .filter(terminator -> terminator.getLetter().equals(input))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown terminator: " + input));
        }
    }

    @Getter
    @AllArgsConstructor
    private enum WorkflowOperator {

        LESS_THAN("<", (i, v) -> i < v),
        GREATER_THAN(">", (i, v) -> i > v)
        ;

        private final String operator;
        private final BiPredicate<Integer, Integer> predicate;

        public static WorkflowOperator parse(final String input) {
            return Arrays.stream(values())
                    .filter(operator -> operator.getOperator().equals(input))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown operator: " + input));
        }
    }

}
