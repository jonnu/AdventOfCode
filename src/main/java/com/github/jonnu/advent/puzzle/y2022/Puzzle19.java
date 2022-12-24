package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.text.html.Option;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle19 implements Puzzle {

    private final ResourceReader resourceReader;
    private static final Pattern BLUEPRINT_EACH = Pattern.compile("Blueprint (\\d+):\s*");
    private static final Pattern BLUEPRINT_COST = Pattern.compile("Each (?<robotType>ore|obsidian|clay|geode) robot costs (?<oreCost>\\d+) ore( and (?<secondaryCost>\\d+) (?<secondaryType>clay|obsidian))?\\.", Pattern.MULTILINE | Pattern.UNIX_LINES);

    private enum Type {
        ORE,
        CLAY,
        OBSIDIAN,
        GEODE;

        public static Type fromName(final String name) {
            return Arrays.stream(values()).filter(x -> name.equalsIgnoreCase(x.name())).findFirst().orElseThrow(() -> new IllegalArgumentException("unknown type: " + name));
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class Cost {
        Type type;
        int value;
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class Robot {
        Type type;
        Map<Type, Cost> cost;
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class BluePrint {
        int id;
        @Singular Map<Type, Robot> robots;
    }

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle19.txt")) {

            List<BluePrint> blueprints = new ArrayList<>();
            String line = reader.readLine();
            while (line != null) {
                String[] args = line.split(":");
                Matcher matcher = BLUEPRINT_COST.matcher(args[1]);
                if (matcher.find()) {
                    Map<Type, Robot> robots = new HashMap<>();
                    do {
                        Map<Type, Cost> costs = new HashMap<>();
                        costs.put(Type.ORE, Cost.builder()
                                .type(Type.ORE)
                                .value(Integer.parseInt(matcher.group("oreCost")))
                                .build());
                        Optional.ofNullable(matcher.group("secondaryType"))
                                .map(type -> Cost.builder()
                                        .type(Type.fromName(type))
                                        .value(Integer.parseInt(matcher.group("secondaryCost")))
                                        .build())
                                .ifPresent(cost -> costs.put(cost.getType(), cost));

                        Robot robot = Robot.builder()
                                .type(Type.fromName(matcher.group("robotType")))
                                .cost(costs)
                                .build();
                        robots.put(robot.getType(), robot);
                    } while (matcher.find(matcher.start() + 1));

                    blueprints.add(BluePrint.builder()
                            .id(Integer.parseInt(args[0].split(" ")[1]))
                            .robots(robots)
                            .build());
                }

                line = reader.readLine();
            }

            blueprints.forEach(System.out::println);

            simulate(blueprints.get(0));
        }
    }

    private int simulate(final BluePrint blueprint) {

        Map<Type, Integer> robots = new HashMap<>();
        Queue<Type> buildQueue = new ArrayDeque<>();
        // always start with 1 ore robot.
        robots.put(Type.ORE, 1);
        Map<Type, Integer> resources = new HashMap<>();

        List<Type> buildPriority = List.of(Type.GEODE, Type.OBSIDIAN, Type.CLAY, Type.ORE);

        int minutes = 24;
        for (int m = 1; m <= minutes; m++) {
            System.out.printf("== Minute %d ==%n", m);

            // spend.
            for (int i = 0; i < buildPriority.size(); i++) {
                // can I afford an X?
                boolean canAffordToBuild = blueprint.getRobots().get(buildPriority.get(i)).getCost().entrySet().stream().allMatch(pair -> pair.getValue().getValue() <= resources.getOrDefault(pair.getKey(), 0));
                //System.out.println("I can" + ((canAffordToBuild ? " " : "not ") + "afford to build miner of type: " + buildPriority.get(i)));

                if (canAffordToBuild) {
                    System.out.printf("Spend %s to start building a %s-collecting robot.%n", 999, buildPriority.get(i).name().toLowerCase(
                            Locale.ROOT));

                    // spend
                    blueprint.getRobots().get(buildPriority.get(i)).getCost().forEach((key, value) -> resources.compute(key, (k, v) -> v -= value.getValue()));
                    System.out.println(resources);
                    // enqueue
                    buildQueue.add(buildPriority.get(i));
                }
            }

            // mine
            robots.forEach((type, count) -> {
                int collected = count;
                String material = type.name().toLowerCase(Locale.ROOT);
                resources.compute(type, (t, v) -> Optional.ofNullable(v).map(v1 -> v1 + 1).orElse(collected));

                System.out.printf("%d %s-collecting robot collects %d %s; you now have %d %s%n",
                        count, material, collected, material, resources.getOrDefault(type, 0), material);
            });

            // build.
            if (!buildQueue.isEmpty()) {
                Type toBuild = buildQueue.poll();
                robots.compute(toBuild, (t, c) -> Optional.ofNullable(c).map(v1 -> v1 + 1).orElse(1));
                System.out.printf("The new %s-collecting robot is ready; you now have %d of them.%n", toBuild.name().toLowerCase(Locale.ROOT), robots.get(toBuild));
            }

            System.out.printf("%n");
        }

        return resources.getOrDefault(Type.GEODE, 0);
    }

}