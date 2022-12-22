package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.function.Function;
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

    private static final int[] COORDINATE_INDEXES = new int[] { 1_000, 2_000, 3_000 };
    private static final boolean DEBUG_OUTPUT = false;

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle20.txt")) {

            final NodeRing<Integer> ring = new NodeRing<>();
            ring.insert(Integer.parseInt(reader.readLine()));
            String line = reader.readLine();

            while (line != null) {
                Node<Integer> node = new Node<>(Integer.parseInt(line));
                ring.insert(node);
                line = reader.readLine();
            }

            // Enqueue all nodes to process movement.
            final Queue<Node<Integer>> queue = new ArrayDeque<>();
            for (Node<Integer> n : ring) {
                queue.add(n);
            }

            if (DEBUG_OUTPUT) {
                System.out.println("Initial arrangement:");
                System.out.printf("%s%n%n", ring);
            }

            while (!queue.isEmpty()) {
                Node<Integer> next = queue.poll();
                ring.move(next, next.getValue(), DEBUG_OUTPUT);
            }

            System.out.println("Sum of grove co-ordinates: " + ring.select(COORDINATE_INDEXES).stream().reduce(0, Integer::sum));
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

        public void move(final Node<T> node, int positions) {
            move(node, positions, false);
        }

        public void move(final Node<T> node, int positions, boolean debug) {

            // Nothing to do if the weight of the node is zero.
            if (positions == 0) {
                if (debug) {
                    System.out.printf("0 does not move:%n%s%n%n", this);
                }
                return;
            }

            Node<T> shift = node;
            int moves = Math.abs(positions);
            final Function<Node<T>, Node<T>> movement = positions < 0 ? Node::getPrev : Node::getNext;
            final Function<Node<T>, Node<T>> loopback = n -> n == null ? (positions < 0 ? tail : head) : n;
            for (int i = 0; i < moves; i++) {

                shift = movement.apply(shift);
                shift = loopback.apply(shift);

                // when moving, do not count ourselves.
                if (shift.equals(node)) {
                    moves++;
                }
            }

            Node<T> left = positions > 0 ? shift : (shift.equals(head) ? tail : shift.getPrev());
            Node<T> right = positions > 0 ? (shift.equals(tail) ? head : shift.getNext()) : shift;

            if (debug) {
                System.out.printf("%s moves between %s and %s:%n", node.getValue(), left.getValue(), right.getValue());
            }

            // moving between ourselves means there is no work to do.
            if (node.equals(left) || node.equals(right)) {
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
            while (!start.getValue().equals(0)) {
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