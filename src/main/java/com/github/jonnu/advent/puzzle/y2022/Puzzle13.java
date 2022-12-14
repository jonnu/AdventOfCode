package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle13 implements Puzzle {

    private static final List<PacketElement> DECODER_PACKETS = ImmutableList.of(parse("[[2]]"), parse("[[6]]"));
    private static final Comparator<PacketElement> COMPARATOR = new PacketComparator();

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle13.txt")) {

            ArrayList<Integer> orderedPacketPairs = new ArrayList<>();
            PriorityQueue<PacketElement> packets = new PriorityQueue<>(COMPARATOR);
            packets.addAll(DECODER_PACKETS);

            int pair = 1;
            String line = "";
            while (line != null) {

                PacketElement left = parse(reader.readLine());
                PacketElement right = parse(reader.readLine());
                packets.add(left);
                packets.add(right);

                if (COMPARATOR.compare(left, right) < 0) {
                    orderedPacketPairs.add(pair);
                }

                // Skip blank lines
                line = reader.readLine();
                pair++;
            }

            int heapIndex = 1;
            ArrayList<Integer> decoderIndicies = new ArrayList<>(DECODER_PACKETS.size());
            while (decoderIndicies.size() < 2) {
                PacketElement packet = packets.poll();
                if (DECODER_PACKETS.contains(packet)) {
                    decoderIndicies.add(heapIndex);
                }

                heapIndex++;
            }

            System.out.println("Sum of ordered packet pairs: " + orderedPacketPairs.stream().reduce(0, Integer::sum));
            System.out.println("Product of decoder packet indicies: " + decoderIndicies.stream().reduce(1, Math::multiplyExact));
        }
    }

    private static PacketElement parse(final String input) {

        final Deque<PacketElement> stack = new ArrayDeque<>();
        final char[] characters = new char[] { ',', '[', ']' };

        for (int i = 0, j = input.length() - 1; i < j; i++) {
            switch (input.charAt(i)) {
                case '[' -> {
                    PacketList subElement = new PacketList();
                    Optional.ofNullable(stack.peek()).ifPresent(element -> element.append(subElement));
                    stack.push(subElement);
                }
                case ']' -> stack.pop();
                case ',' -> {}
                default -> {

                    int next = Integer.MAX_VALUE;
                    for (char chr : characters) {
                        int index = input.indexOf(chr, i + 1);
                        next = index >= 0 ? Math.min(next, index) : next;
                    }

                    final PacketInt packet = new PacketInt(Integer.parseInt(input.substring(i, next)));
                    Optional.ofNullable(stack.peek()).ifPresent(element -> element.append(packet));
                    i = next - 1;
                }
            }
        }

        return stack.pop();
    }

    private static class PacketComparator implements Comparator<PacketElement> {
        @Override
        public int compare(final PacketElement left, final PacketElement right) {

            if (left instanceof PacketInt && right instanceof PacketInt) {
                return Integer.compare(((PacketInt) left).getValue(), ((PacketInt) right).getValue());
            }

            final Iterator<PacketElement> li = left.toList().iterator();
            final Iterator<PacketElement> ri = right.toList().iterator();

            for (;;) {
                boolean hasLeft = li.hasNext(), hasRight = ri.hasNext();
                if (!hasLeft && !hasRight) {
                    return 0;
                } else if (!hasLeft) {
                    return -1;
                } else if (!hasRight) {
                    return 1;
                } else {
                    int compared = compare(li.next(), ri.next());
                    if (compared != 0) {
                        return compared;
                    }
                }
            }
        }
    }

    interface PacketElement {
        PacketElement append(PacketElement item);
        PacketList toList();
    }

    @Getter
    @AllArgsConstructor
    private static class PacketInt implements PacketElement {

        int value;

        @Override
        public PacketList toList() {
            return new PacketList(List.of(this));
        }

        @Override
        public PacketElement append(final PacketElement item) {
            return new PacketList(List.of(this, item));
        }

        @Override
        public String toString() {
            return String.valueOf(getValue());
        }
    }

    @Getter
    @AllArgsConstructor
    private static class PacketList implements PacketElement, Iterable<PacketElement> {

        private final List<PacketElement> value;

        PacketList() {
            this.value = new ArrayList<>();
        }

        @Override
        public PacketList toList() {
            return this;
        }

        @Override
        public PacketElement append(final PacketElement item) {
            value.add(item);
            return this;
        }

        @Override
        public @NonNull Iterator<PacketElement> iterator() {
            return getValue().iterator();
        }

        @Override
        public String toString() {
            return getValue().toString();
        }
    }

}
