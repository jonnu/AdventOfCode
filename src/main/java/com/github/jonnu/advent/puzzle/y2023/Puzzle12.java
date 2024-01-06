package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle12 implements Puzzle {

    private static final Pattern PATTERN = Pattern.compile("^(?<template>\\S+)\\s(?<requirement>[0-9,]+)$");

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    private static final Function<String, String> PART2_MAPPING = a -> IntStream.range(0, 5)
            .mapToObj(i -> a)
            .collect(Collectors.joining("?"))
            // simplify: ....#.... === .#.
            // simplify: #.......# === #.#
            .replaceAll("[.,]+", ".");

    //private static final Map<String, Integer> CACHE = new HashMap<>();

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle12.txt")) {

            List<ConditionRecord> records = new ArrayList<>();
            String line = reader.readLine();
            while (line != null) {

                Matcher matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Cannot parse: " + line);
                }

                String template = PART2_MAPPING.apply(matcher.group("template"));

                int[] requirements = Collections.nCopies(5, Arrays.stream(matcher.group("requirement").split(","))
                                .map(Integer::parseInt)
                                .collect(Collectors.toList()))
                        .stream()
                        .flatMap(Collection::stream)
                        .mapToInt(x -> x)
                        .toArray();

                records.add(ConditionRecord.builder()
                        .template(template)
                        .requirements(requirements)
                        .arrangements(yuno(template, requirements))
                        .build());

                line = reader.readLine();
            }

            records.forEach(System.out::println);
            System.out.println("Sum: " + records.stream().mapToInt(ConditionRecord::getArrangements).sum());
        }
    }

    private static int yuno(String template, int[] requirements) {
        return yuno(template, requirements, 0, 0, new HashMap<>());
    }

    @Value
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    private static class Pair<L, R> {

        L left;
        R right;

        public static <L, R> Pair<L, R> of(L left, R right) {
            return new Pair<>(left, right);
        }
    }

    private static final Map<Pair<String, Integer>, Integer> CACHE = new HashMap<>();

    private static int yuno(String template, int[] requirements, int atChar, int atRequirement, Map<Pair<String, Integer>, Integer> cache) {

        Pair<String, Integer> key = Pair.of(template.substring(atChar), atRequirement);
        if (CACHE.containsKey(key)) {
            System.out.println(key);
            return cache.get(key);
            //System.out.println("CACHE HIT" + key + " + " + cache.get(key));
            //System.exit(1);
        }
//        if (CACHE.containsKey(template)) {
//            return CACHE.get(template);
//        }

        // check where we are at in template
        boolean atTemplateEnd = atChar == template.length();
        boolean atRequirementEnd = atRequirement == requirements.length;
        StringBuilder sb = new StringBuilder(template);

        int minimum = Arrays.stream(requirements, atRequirement, requirements.length).sum();
        if (minimum > template.length() - atChar) {
            //System.out.println("NOT ENOUGH CHARS TO WIN");
            //cache.put(key, 0);//CACHE.put(template, 0);
            return 0;
        }

//        System.out.println(template + " [" + IntStream.range(0, requirements.length).mapToObj(i -> (i == atRequirement ? ANSI_RED : "") + requirements[i] + (i == atRequirement ? ANSI_RESET : "")).collect(Collectors.joining(", ")) + " min: " + minimum + "] templateEnd: " + atTemplateEnd + "; requirementEnd: " + atRequirementEnd);
//        System.out.println(" ".repeat(atChar) + "^");
//        System.out.println();

        if (atTemplateEnd || atRequirementEnd) {
            if (atTemplateEnd) {
                CACHE.put(Pair.of(template, atRequirement), atRequirementEnd ? 1 : 0);
                cache.put(key, atRequirementEnd ? 1 : 0);
                return atRequirementEnd ? 1 : 0;
            } else {

                if (template.substring(atChar).contains("#")) {
                    // reject
                    //CACHE.put(template, 0);
                    //cache.put(key, 0);
                    return 0;
                }

                sb.replace(atChar, sb.length(), ".".repeat(sb.length() - atChar));
                int res = yuno(sb.toString(), requirements, sb.length(), atRequirement, cache);
                //CACHE.put(template, res);
                //cache.put(key, res);
                return res;
            }

        }

        switch (template.charAt(atChar)) {
            case '?' -> {

                int aa = 0;
                // we can only check damaged if the previous spring wasn't damaged.
                // or we're at the start of the string. they have to have a space.
                boolean canCheckDamaged = atChar == 0 || template.charAt(atChar - 1) != '#';
                if (canCheckDamaged) {
                    sb.setCharAt(atChar, '#');
                    String a = sb.toString();
                    aa = yuno(a, requirements, atChar, atRequirement, cache);
                    //CACHE.put(a, aa);
                    //cache.put(key, aa);
                }

                sb.setCharAt(atChar, '.');
                String b = sb.toString();
                int ab = yuno(b, requirements, atChar, atRequirement, cache);
                //CACHE.put(b, ab);
                //cache.put(key, ab);
                return aa + ab;
            }
            case '#' -> {
                // find number of concurrent #
                int lookingFor = requirements[atRequirement];
                //System.out.println("Found spring. Need: " + lookingFor + " in a row.");

                int x = atChar;
                while (x < template.length() && template.charAt(x) == '#') {
                    x++;
                }

                int concurrent = x - atChar;
                if (lookingFor == concurrent) {
                    // satisfied the requirement. go again.
                    int res = yuno(template, requirements, atChar + concurrent, atRequirement + 1, cache);
                    //CACHE.put(template, res);
                    //cache.put(key, res);
                    return res;
                } else if (lookingFor < concurrent) {
                    // this is an insta-fail.
                    //System.out.println("we needed " + lookingFor + " but found " + concurrent);
                    //System.out.println("Reject!");
                    //CACHE.put(template, 0);
                    //cache.put(key, 0);
                    return 0;
                } else {

                    // next char is NOT a dmg spring.
                    //atChar += concurrent - 1;

                    if (template.charAt(atChar + concurrent) == '?') {
                        sb.setCharAt(atChar + concurrent, '#');
                        int res = yuno(sb.toString(), requirements, atChar, atRequirement, cache);
                        //CACHE.put(sb.toString(), res);
                        //cache.put(key, res);
                        return res;
                    } else {
                        //System.out.println("i cant get that shit in here y'all (atChar: " + atChar + "; concurrent: " + concurrent + ")");
                        //System.out.println(template);
                        //System.out.println(" ".repeat(atChar) + "^");
                        //CACHE.put(template, 0);
                        //cache.put(key, 0);
                        return 0;
                    }
                }
            }
            case '.' -> {
                int res = yuno(template, requirements, ++atChar, atRequirement, cache);
                //CACHE.put(template, res);
                //cache.put(key, res);
                return res;
            }
            default -> throw new IllegalStateException("Unknown Character");
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class ConditionRecord {
        String template;
        int[] requirements;
        int arrangements;
    }
}
