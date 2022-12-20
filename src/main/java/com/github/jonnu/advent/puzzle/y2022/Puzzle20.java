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

        public List<T> select(int[] indexes) {

            //System.out.println(this);

            // start from 0.
            ArrayList<T> nodes = new ArrayList<>();
            Node<T> node = head;
            while (!node.getValue().equals(0)) {
                node = node.getNext();
            }

            System.out.println("Walked to node:" + node);

            for (int a = 0; a < indexes.length; a++) {
                int mod = indexes[a];// % size;
                for (int i = 0; i < mod; i++) {
                    node = node.getNext();
                    if (node == null) {
                        node = head;
                    }
                    //System.out.printf("[%4d] %s", i+1, node.getValue());// i + 1 + "; " + node.getValue());
                }

                System.out.println("Index " + indexes[a] + " = " + node.getValue());
                nodes.add(node.getValue());

                // move back to 0 node (there must be a better way to do this).
                while (!node.getValue().equals(0)) {
                    node = node.getNext();
                    if (node == null) {
                        node = head;
                    }
                }
            }

            return nodes;
        }

        public void move(final Node<T> node, int positions) {
            // walk to node.
            Node<T> destination = head;
            while (destination != node) {
                //System.out.println("Walking... " + destination.getValue());
                destination = destination.getNext();
            }
            //System.out.println("Arrived... " + destination);

            // walk to move pos.
            Function<Node<T>, Node<T>> moveMethod = positions < 0 ? Node::getPrev : Node::getNext;
            Node<T> from = destination;
            for (int i = 0; i < Math.abs(positions); i++) {
                destination = moveMethod.apply(destination);
                if (destination == null) {
                    destination = positions < 0 ? tail : head;
                }
                //System.out.println(i+1 + "; " + destination.getValue());
            }


            // move
            Node<T> fromCopy = from.clone();
            if (from.equals(head)) {
                head = from.getNext();//destination;//fromCopy.getPrev();//destination;
                head.setPrev(null);
            }

            if (positions > 0) {
                //System.out.println(from.getValue() + " moves between " + destination.getValue() + " and " + destination.getNext().getValue());//Moving from: " + from + " to destination: " + destination);

                if (fromCopy.getPrev() != null) {
                    from.getPrev().setNext(from.getNext());
                }

                from.getNext().setPrev(from.getPrev());

                from.setNext(destination.getNext());        //           B -> C
                destination.getNext().setPrev(from);        //           B <- C
                from.setPrev(destination);                  //      A <- B
                destination.setNext(from);                  //      A -> B
            }

            //3 moves between 0 and 4:
            //1, 2, -2, -3, 0, 3, 4
            //1 .>2 [H], 2 1<>-2, -2 2<>-3, -3 -2<>0, 0 -3<>3, 3 0<>4, 4 3<. [T]

            if (positions < 0) {

                Node<T> betweenLeft = destination.equals(head) ? tail : destination.getPrev();
                Node<T> betweenRight = destination;
                //System.out.println("<-- Moving: " + from.getValue() + " between: " + betweenLeft.getValue() + " and " + betweenRight.getValue());

                if (!from.equals(tail)) {
                    from.getNext().setPrev(from.getPrev());
                }

                if (from.getPrev() != null) {
                    from.getPrev().setNext(from.getNext());
                }
                //from.getNext().setPrev(from.getPrev());

                betweenLeft.setNext(from);                  // A --> B
                //from.setPrev(destination.getPrev());        // B <-- A
                from.setPrev(betweenLeft);
                from.setNext(destination);                  // B --> C
                destination.setPrev(from);                  // C <-- B

                if (destination.equals(head)) {
                    tail = from;
                    betweenRight.setPrev(null);
                    from.setNext(null);
                }

            }

            if (positions == 0) {
                //System.out.println("0 does not move");
            }
        }

//        private Node<T> previous(final Node<T> node) {
//            return Optional.ofNullable(node.getPrev()).orElse(tail);
//        }
//
//        private Node<T> next(final Node<T> node) {
//            return Optional.ofNullable(node.getNext()).orElse(head);
//        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(", ");
            Node<T> node = head;
            while (node != null && node.getNext() != head) {
                String status = "";
                if (node.equals(head) && node.equals(tail)) {
                    status = "ht";
                } else if (node.equals(head)) {
                    status = "h";
                } else if (node.equals(tail)) {
                    status = "t";
                }
                //String link = String.format(" %s%s", node.getPrev() != null ? node.getPrev().getValue() + "<" : ".", node.getNext() != null ? ">" + node.getNext().getValue() : ".");
                String link = String.format(" %s%s", node.getPrev() != null ? "<" : ".", node.getNext() != null ? ">" : ".");
//                joiner.add(node.getValue().toString() + link + status);
                joiner.add(node.getValue().toString() + status + link);
                node = node.getNext();
            }

            return String.format("Ring(%d): [%2s]", size, joiner);
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
    private static class Node<T> implements Cloneable {
        private final T value;
        @Setter private int index;
        @Setter private Node<T> next = null;
        @Setter private Node<T> prev = null;

//        public Node<T> getPrev() {
//            return Optional.ofNullable(prev).orElseThrow(() -> new RuntimeException("No previous for node: " + this));
//        }
//
//        public Node<T> getNext() {
//            return Optional.ofNullable(next).orElseThrow(() -> new RuntimeException("No next for node: " + this));
//        }

//        boolean tail = false;
//        boolean head = false;

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

//        public void splice(final Node<T> insert) {
//            // make this link to prev.
//            this.setIndex(index - 1);
//            insert.setIndex(insert.getIndex() + 1);
//            Node<T> oldNext = next;
//            insert.setNext(new Node<>(oldNext.getValue(), oldNext.getIndex(), this));
//            setNext(insert);
//            //insert.setPrev(this);
//        }

        @Override
        public String toString() {
            return "Node (Val: " + value + "; Next: " +
                    (next == null ? "false" : "true (" + next.getValue() + ")") + "; Prev: " +
                    (prev == null ? "false" : "true (" + prev.getValue() + ")") + ")";

//            return "Node(Index: " + index + "; Value: " + value.toString() + "; Next: " +
//                    (next == null ? "false" : "true (" + next.getIndex() + ")") + "; Prev: " +
//                    (prev == null ? "false" : "true (" + prev.getIndex() + ")") + ")";
        }

        @Override
        public Node<T> clone() {
            try {
                Node<T> clone = (Node<T>) super.clone();
                clone.setPrev(getPrev());
                clone.setNext(getNext());
                // TODO: copy mutable state here, so the clone can't change the internals of the original
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

    }

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2022/puzzle20.txt")) {

            NodeRing<Integer> ring = new NodeRing<>(new Node<>(Integer.parseInt(reader.readLine())));
            String line = reader.readLine();

            while (line != null) {
                Node<Integer> node = new Node<>(Integer.parseInt(line));
                ring.add(node);
                line = reader.readLine();
            }

            // done.

            System.out.println("Initial arrangement:");
            System.out.printf("[ST] %s%n%n", ring);
            Queue<Node<Integer>> queue = new ArrayDeque<>();
            for (Node<Integer> n : ring) {
                queue.add(n);
            }

            while (!queue.isEmpty()) {
                Node<Integer> next = queue.poll();
                ring.move(next, next.getValue());
                System.out.printf("[%2d] %s%n%n", next.getValue(), ring);
            }

            System.out.println("Sum of grove co-ordinates: " + ring.select(COORDINATE_INDEXES).stream().reduce(0, Integer::sum));
        }
    }

}