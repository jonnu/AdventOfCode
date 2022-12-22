package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle20 implements Puzzle {

    private static final int MIXING_ROUNDS = 10;
    private static final long DECRYPTION_KEY = 811_589_153L;
    private static final int[] COORDINATE_INDEXES = new int[] { 1_000, 2_000, 3_000 };

    private static final boolean DEBUG_OUTPUT = false;

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle20.txt")) {

            final NodeRing<Long> ring = new NodeRing<>();
            ring.insert(Long.parseLong(reader.readLine()) * DECRYPTION_KEY);
            String line = reader.readLine();

            while (line != null) {
                ring.insert(Long.parseLong(line) * DECRYPTION_KEY);
                line = reader.readLine();
            }

            // Enqueue all nodes to process movement.
            final Queue<Node<Long>> queue = new ArrayDeque<>();
            for (int i = 0; i < MIXING_ROUNDS; i++) {
                for (Node<Long> n : ring) {
                    queue.add(n);
                }
            }

            if (DEBUG_OUTPUT) {
                System.out.println("Initial arrangement:");
                System.out.printf("%s%n%n", ring);
            }

            int processed = 0;
            while (!queue.isEmpty()) {

                Node<Long> next = queue.poll();
                ring.move(next, next.getValue(), DEBUG_OUTPUT);

                processed++;
                if (DEBUG_OUTPUT && processed % ring.size == 0) {
                    System.out.printf("After %d round%s of mixing:%n%s%n%n", processed / ring.size, processed / ring.size == 1 ? "" : "s", ring);
                }
            }

            System.out.println("Sum of grove co-ordinates: " + ring.select(COORDINATE_INDEXES).stream().reduce(0L, Long::sum));
        }
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    private static class Node<T> {
        private final T value;
        private Node<T> next = null;
        private Node<T> prev = null;
    }

    @NoArgsConstructor
    private static class NodeRing<T> implements Iterable<Node<T>> {

        Node<T> head = null;
        Node<T> tail = null;
        int size = 0;

        public void insert(final T value) {
            insert(new Node<>(value));
        }

        public void insert(final Node<T> node) {

            if (tail == null) {
                head = node;
                tail = node;
                size++;
                return;
            }

            insert(node, tail);
        }

        public void insert(final Node<T> node, final Node<T> after) {

            if (after.equals(tail)) {
                tail.setNext(node);
                node.setPrev(tail);
                tail = node;
                size++;
                return;
            }

            // n-1, n, n+1. Link n+1 nodes.
            node.setNext(after.getNext());
            after.getNext().setPrev(node);

            // n-1, n, n+1. Link n-1 nodes.
            after.setNext(node);
            node.setPrev(after);

            size++;
        }

        public void remove(final Node<T> node) {

            Optional.ofNullable(node.getPrev()).ifPresent(left -> left.setNext(node.getNext()));
            Optional.ofNullable(node.getNext()).ifPresent(right -> right.setPrev(node.getPrev()));

            if (node.equals(head)) {
                head = node.getNext();
            }

            if (node.equals(tail)) {
                tail = node.getPrev();
            }

            node.setPrev(null);
            node.setNext(null);
            size--;
        }

        public void move(final Node<T> node, long positions, boolean debug) {

            // Nothing to do if the weight of the node is zero.
            if (positions == 0) {
                if (debug) {
                    System.out.printf("0 does not move:%n%s%n%n", this);
                }
                return;
            }

            Node<T> shift = node;
            int modulo = size - 1;
            long moves = positions < 0 ? (positions % modulo) + modulo : positions % modulo;

            for (int i = 0; i < moves; i++) {

                shift = shift.getNext();
                shift = shift == null ? head : shift;

                // when moving, do not count ourselves.
                if (shift.equals(node)) {
                    moves++;
                }
            }

            Node<T> left = shift;
            Node<T> right = shift.equals(tail) ? head : shift.getNext();

            if (debug) {
                System.out.printf("%s moves between %s and %s:%n", node.getValue(), left.getValue(), right.getValue());
            }

            // moving between ourselves means there is no work to do.
            if (node.equals(left)) {
                return;
            }

            remove(node);
            insert(node, left);

            if (debug) {
                System.out.printf("%s%n%n", this);
            }
        }

        public List<T> select(final int[] indexes) {

            Node<T> start = head;
            final ArrayList<T> nodes = new ArrayList<>();
            while (!start.getValue().equals(0L)) {
                start = start.getNext();
            }

            for (int index = 0; index < indexes.length; index++) {

                Node<T> node = start;
                for (int i = 0; i < indexes[index]; i++) {
                    node = Optional.ofNullable(node.getNext()).orElse(head);
                }

                nodes.add(node.getValue());
            }

            return nodes;
        }

        @Override
        public String toString() {
            Node<T> node = head;
            StringJoiner joiner = new StringJoiner(", ");
            while (node != null && node.getNext() != head) {
                joiner.add(node.getValue().toString());
                node = node.getNext();
            }
            return joiner.toString();
        }

        @Override
        public @NonNull Iterator<Node<T>> iterator() {
            return new Iterator<>() {

                Node<T> node = head;

                @Override
                public boolean hasNext() {
                    return node != null && node.getNext() != head;
                }

                @Override
                public Node<T> next() {
                    Node<T> n = node;
                    node = node.getNext();
                    return n;
                }
            };
        }
    }

}