package com.github.jonnu.advent.puzzle.y2023;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle20 implements Puzzle {

    private final ResourceReader resourceReader;

    private static final Pattern PATTERN = Pattern.compile("^(?<type>[%&])?(?<name>[a-z]+)\\s->\\s(?<destination>[a-z, ]+)$");

    private final Map<String, Module> modules = new HashMap<>();

    private static final Machine MACHINE = Machine.builder().build();

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle20.txt")) {

            String line = reader.readLine();
            while (line != null) {

                Matcher matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Err: " + line);
                }

                String moduleName = matcher.group("name");
                List<String> destinations = Arrays.stream(matcher.group("destination").split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());

                if (moduleName.equals("broadcaster")) {
                    modules.put(moduleName, BroadcastModule.builder()
                            .name("broadcaster")
                            .destinations(destinations)
                            .build());
                } else {
                    String type = matcher.group("type");
                    switch (type) {
                        case "%" -> modules.put(moduleName, FlipFlopModule.builder().name(moduleName).destinations(destinations).build());
                        case "&" -> modules.put(moduleName, ConjunctionModule.builder().name(moduleName).destinations(destinations).build());
                    }
                }

                line = reader.readLine();
            }

            MACHINE.setModules(modules);
            MACHINE.pressButton();
        }
    }

    @Builder
    @AllArgsConstructor
    private static class Machine {

        private static final String INITIAL_MODULE = "broadcaster";

        EnumMap<Pulse, Integer> pulses;
        @Setter Map<String, Module> modules;

        public void pressButton() {
            System.out.printf("button -%s-> %s%n", Pulse.LOW.name().toLowerCase(), INITIAL_MODULE);
            modules.get(INITIAL_MODULE).process(PulseRequest.builder()
                    .pulse(Pulse.LOW)
                    .origin("button")
                    .destination(INITIAL_MODULE)
                    .build());
        }

        public Module getModule(final String module) {
            return modules.get(module);
        }
    }

    @Getter
    @SuperBuilder
    private static abstract class Module {

        protected String name;
        protected List<String> destinations;
        @Builder.Default protected Queue<PulseRequest> calls = new ArrayDeque<>();

        // pulse type, pulse from.
        // number of pulses sent.
        abstract void process(PulseRequest request);

        void pulse() {
            while (!calls.isEmpty()) {
                PulseRequest request = calls.poll();
                MACHINE.getModule(request.getDestination()).process(request);
            }
        }
    }

    @SuperBuilder
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    private static class FlipFlopModule extends Module {

        private boolean state;

        @Override
        public void process(PulseRequest request) {

            // Ignore high pulses.
            if (Pulse.HIGH.equals(request.getPulse())) {
                return;
            }

            state = !state;
            Pulse outboundPulse = state ? Pulse.HIGH : Pulse.LOW;

            getDestinations().forEach(destination -> {
                PulseRequest outbound = PulseRequest.builder()
                        .origin(getName())
                        .destination(destination)
                        .pulse(outboundPulse)
                        .build();
                System.out.printf("%s -%s-> %s%n", outbound.getOrigin(), outbound.getPulse().name().toLowerCase(), outbound.getDestination());
                getCalls().add(outbound);
            });

            pulse();
        }
    }

    @SuperBuilder
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    private static class BroadcastModule extends Module {

        @Override
        public void process(PulseRequest request) {
            getDestinations().forEach(destination -> {
                PulseRequest outbound = PulseRequest.builder()
                        .origin(getName())
                        .destination(destination)
                        .pulse(request.getPulse())
                        .build();
                System.out.printf("%s -%s-> %s%n", outbound.getOrigin(), outbound.getPulse().name().toLowerCase(), outbound.getDestination());
                getCalls().add(outbound);
            });

            pulse();
        }
    }

    @SuperBuilder
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    private static class ConjunctionModule extends Module {

        @Builder.Default private Map<String, Pulse> lastPulse = new HashMap<>();

        @Override
        public void process(PulseRequest request) {

            lastPulse.put(request.getOrigin(), request.getPulse());

            Pulse outboundPulse = lastPulse.values().stream().allMatch(Pulse.HIGH::equals) ? Pulse.LOW : Pulse.HIGH;

            getDestinations().forEach(destination -> {
                PulseRequest outbound = PulseRequest.builder()
                        .origin(getName())
                        .destination(destination)
                        .pulse(outboundPulse)
                        .build();
                System.out.printf("%s -%s-> %s%n", request.getOrigin(), request.getPulse().name().toLowerCase(), request.getDestination());
                getCalls().add(outbound);
            });

            pulse();
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class PulseRequest {
        String origin;
        String destination;
        Pulse pulse;
    }

    private enum Pulse {
        LOW,
        HIGH;
    }
}
