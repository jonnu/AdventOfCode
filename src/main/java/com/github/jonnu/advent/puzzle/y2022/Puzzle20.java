package com.github.jonnu.advent.puzzle.y2022;

import java.io.BufferedReader;
import java.util.Iterator;
import java.util.StringJoiner;
import javax.inject.Inject;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle20 implements Puzzle {

    private static final int[] COORDINATE_INDEXES = new int[] { 1_000, 2_000, 3_000 };

    private final ResourceReader resourceReader;

    @NoArgsConstructor
    private static class NodeRing<T> implements Iterable<Node<T>> {
        // keep track of my length, my head, and my tail.
        Node<T> head = null;
        Node<T> tail = null;
        int size = 0;

        public NodeRing(final Node<T> head) {
            this.head = head;
            this.tail = head;
            this.size = 1;
        }

        public void add(final Node<T> node) {
            node.setPrev(this.tail);
            this.tail.setNext(node);
            this.tail = node;
            this.size += 1;
        }

        public void move(final Node<T> node, final int positions) {
            // walk to node.
            Node<T> position = head;
            while (position != node) {
                System.out.println("Walking... " + position.getValue());
                position = position.getNext();
            }
            System.out.println("Arrived... " + position.getValue());

        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(", ");
            Node<T> node = head;
            while (node != null && node.getNext() != head) {
                joiner.add(node.getValue().toString());
                node = node.getNext();
            }

            return String.format("Ring(%d): [%s]", size, joiner);
        }

        @Override
        public Iterator<Node<T>> iterator() {
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

    @Getter
    private static class Node<T> {
        private final T value;
        @Setter private int index;
        @Setter private Node<T> next = null;
        @Setter private Node<T> prev = null;
        boolean tail = false;

        Node(final T value) {
            this.value = value;
        }

        Node(final T value, final int index) {
            this.value = value;
            this.index = index;
        }

        Node(final T value, final int index, final Node<T> prev) {
            this.value = value;
            this.index = index;
            this.prev = prev;
        }

        public void splice(final Node<T> insert) {
            // make this link to prev.
            this.setIndex(index - 1);
            insert.setIndex(insert.getIndex() + 1);
            Node<T> oldNext = next;
            insert.setNext(new Node<>(oldNext.getValue(), oldNext.getIndex(), this));
            setNext(insert);
            //insert.setPrev(this);
        }

        public void markAsTail() {
            this.tail = true;
        }

        public void clearTail() {
            this.tail = false;
        }

        @Override
        public String toString() {
            return "Node(Index: " + index + "; Value: " + value.toString() + "; Next: " +
                    (next == null ? "false" : "true (" + next.getIndex() + ")") + "; Prev: " +
                    (prev == null ? "false" : "true (" + prev.getIndex() + ")") + ")";
        }
    }

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle20.txt")) {

            //Node<Integer> head = new Node<>(Integer.parseInt(reader.readLine()));
            //Node<Integer> mixed = head;

            NodeRing<Integer> ring = new NodeRing<>(new Node<>(Integer.parseInt(reader.readLine())));
            String line = reader.readLine();

//            Queue<Node<Integer>> moveQueue = new ArrayDeque<>();
//            moveQueue.add(head);

            while (line != null) {
                Node<Integer> node = new Node<>(Integer.parseInt(line));
                ring.add(node);
                line = reader.readLine();
            }

            // done.
            //System.out.println(ring);

            for (Node<Integer> n : ring) {
                // move it
                int v = n.getValue();
                ring.move(n, v);
                System.out.printf("[%2d] %s%n", v, ring);
            }

            //
//            // iterate to make ring.
//            head = mixed;
//            Node<Integer> tail = null;
//            while (head != null) {
//                tail = head;
//                head = head.getNext();
//            }
//
//            // join up into a ring.
//            tail.setNext(mixed);
//            tail.markAsTail();
//            mixed.setPrev(tail);
//
//            // reset.
//            head = mixed;
//            debug(head);
//
//            System.out.println("Q:" + moveQueue);
//            // move.
//            Node<Integer> current = moveQueue.poll();
//            int moveSteps = mixed.getValue() % index;
//            Function<Node<Integer>, Node<Integer>> moveMethod = moveSteps < 0 ? Node::getPrev : Node::getNext;
//            //final int nodeCount = index;
//            //Consumer<Node<Integer>> indexAdjustingMethod = moveSteps < 0 ? n -> n.setIndex(n.getIndex() + 1) : n -> n.setIndex(n.getIndex() - 1);
//            for (int m = Math.abs(moveSteps); m > 0; m--) {
//                head = moveMethod.apply(head);
//            }
//
//            // insert
//            head.splice(current);
//            debug(head);
//
//            // walk to next element.
//
//            head = moveQueue.poll();
//            System.out.println("Q:" + moveQueue);
//            System.out.println("HERE:" + head);
//
//            // move
//            current = head;
//            moveSteps = mixed.getValue() % index;
//            for (int m = Math.abs(moveSteps); m > 0; m--) {
//                head = moveMethod.apply(head);
//            }
//
//            // insert
//            head.splice(current);
//            debug(head);


            //System.out.println("Now at " + head.getValue() + " (index: " + head.getIndex() + ") and want to place " + last);

            //last.getPrev().setNext();
        }
    }

    private void debug(final Node<Integer> node) {
        StringJoiner joiner = new StringJoiner(" -> ");
        Node<Integer> head = node;
        Node<Integer> tail;
        while (head != null) {
            joiner.add(String.format("[%d]: %d", head.getIndex(), head.getValue()));
            tail = head;
            head = head.getNext();
            if (head != null && (head.isTail() || head.getIndex() < tail.getIndex())) {
                head = null;
            }
        }

        System.out.println(joiner);
    }
}