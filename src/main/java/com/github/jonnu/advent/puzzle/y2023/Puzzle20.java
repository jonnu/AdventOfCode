package com.github.jonnu.advent.puzzle.y2023;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.jonnu.advent.common.math.Arithmetic.lcm;

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

            // Part 1
            IntStream.range(0, 1_000).forEach(i -> MACHINE.pressButton());
            System.out.println("Low and High pulses multiplied after 1,000 presses: " + MACHINE.getPulseValue());

            // Part 2
            long output = 0;
            while (output == 0) {
                output = MACHINE.pressButton();
            }

            System.out.println("Lowest number of presses until low-pulse sent to 'rx': " + output);
        }
    }

    @Builder
    @AllArgsConstructor
    private static class Machine {

        private static final String SAND_MODULE = "rx";
        private static final String INITIAL_MODULE = "broadcaster";

        private int cycle;
        Map<String, Module> modules;
        @Builder.Default Module module = BroadcastModule.BUTTON_BROADCAST_MODULE;
        @Builder.Default EnumMap<Pulse, Long> pulses = new EnumMap<>(Pulse.class);
        @Builder.Default Queue<PulseRequest> pulseQueue = new ArrayDeque<>();
        @Builder.Default Map<String, Integer> conjunctionLowPulsed = new HashMap<>();

        public void setModules(final Map<String, Module> modules) {
            // initialise all conjunction modules.
            modules.values()
                    .stream()
                    .map(module -> module.getDestinations()
                            .stream()
                            .collect(Collectors.toMap(Function.identity(), e -> module.getName())))
                    .flatMap(inverted -> inverted.entrySet().stream())
                    .filter(entry -> modules.get(entry.getKey()) instanceof ConjunctionModule)
                    .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
                    .forEach((conjunctor, callers) -> {
                        if (modules.get(conjunctor).getDestinations().size() > 1) {
                            conjunctionLowPulsed.put(conjunctor, 0);
                        }
                        ((ConjunctionModule) modules.get(conjunctor)).initialise(callers);
                    });

            this.modules = modules;
        }

        public long pressButton() {

            cycle++;

            module.process(PulseRequest.builder()
                    .cycle(cycle)
                    .pulse(Pulse.LOW)
                    .origin(module.getName())
                    .destination(INITIAL_MODULE)
                    .build());

            return process();
        }

        public long record(PulseRequest request) {

            pulses.putIfAbsent(request.getPulse(), 0L);
            pulses.computeIfPresent(request.getPulse(), (pulse, value) -> value + 1);

            conjunctionLowPulsed.computeIfPresent(request.getOrigin(), (k, v) -> Pulse.LOW.equals(request.getPulse()) ? request.getCycle() : v);
            if (conjunctionLowPulsed.values().stream().allMatch(x -> x > 0)) {
                return lcm(conjunctionLowPulsed.values().stream().mapToLong(x -> x).toArray());
            }

            return 0;
        }

        public Module getModule(final String module) {
            return modules.getOrDefault(module, SinkModule.builder().name(module).build());
        }

        public long getPulseValue() {
            return pulses.values().stream().reduce(1L, Math::multiplyExact);
        }

        public void enqueue(final PulseRequest request) {
            pulseQueue.add(request);
        }

        public long process() {
            while (!pulseQueue.isEmpty()) {
                PulseRequest request = pulseQueue.poll();
                MACHINE.getModule(request.getDestination()).process(request);
                long output = MACHINE.record(request);
                if (output > 0) {
                    return output;
                }
            }
            return 0;
        }
    }

    @Getter
    @SuperBuilder
    private static abstract class Module {

        protected String name;
        protected List<String> destinations;

        abstract void process(PulseRequest request);
    }

    @SuperBuilder
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    private static class SinkModule extends Module {

        @Override
        public void process(PulseRequest request) {
            // no-op.
        }
    }


    @SuperBuilder
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    private static class BroadcastModule extends Module {

        private static final BroadcastModule BUTTON_BROADCAST_MODULE = BroadcastModule.builder()
                .name("button")
                .destinations(List.of(Machine.INITIAL_MODULE))
                .build();

        @Override
        public void process(PulseRequest request) {
            getDestinations().forEach(destination -> {
                PulseRequest outbound = PulseRequest.builder()
                        .cycle(request.getCycle())
                        .origin(getName())
                        .destination(destination)
                        .pulse(request.getPulse())
                        .build();
                MACHINE.enqueue(outbound);
            });
        }
    }

    @SuperBuilder
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    private static class FlipFlopModule extends Module {

        private boolean state;

        @Override
        public void process(PulseRequest request) {

            if (Pulse.HIGH.equals(request.getPulse())) {
                return;
            }

            state = !state;
            Pulse outboundPulse = state ? Pulse.HIGH : Pulse.LOW;

            getDestinations().forEach(destination -> {
                PulseRequest outbound = PulseRequest.builder()
                        .cycle(request.getCycle())
                        .origin(getName())
                        .destination(destination)
                        .pulse(outboundPulse)
                        .build();
                MACHINE.enqueue(outbound);
            });
        }
    }

    @SuperBuilder
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    private static class ConjunctionModule extends Module {

        @Builder.Default private Map<String, Pulse> lastPulse = new HashMap<>();

        public void initialise(final List<String> callers) {
            callers.forEach(caller -> lastPulse.put(caller, Pulse.LOW));
        }

        @Override
        public void process(PulseRequest request) {

            lastPulse.put(request.getOrigin(), request.getPulse());
            Pulse outboundPulse = lastPulse.values().stream().allMatch(Pulse.HIGH::equals) ? Pulse.LOW : Pulse.HIGH;

            getDestinations().forEach(destination -> {
                PulseRequest outbound = PulseRequest.builder()
                        .cycle(request.getCycle())
                        .origin(getName())
                        .destination(destination)
                        .pulse(outboundPulse)
                        .build();
                MACHINE.enqueue(outbound);
            });
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    private static class PulseRequest {
        int cycle;
        String origin;
        String destination;
        Pulse pulse;
    }

    private enum Pulse {
        LOW,
        HIGH;
    }
}
