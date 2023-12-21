package com.github.jonnu.advent.puzzle.y2023;

import com.github.jonnu.advent.common.ResourceReader;
import com.github.jonnu.advent.common.geometry.Point;
import com.github.jonnu.advent.puzzle.Puzzle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.ToLongBiFunction;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class Puzzle21 implements Puzzle {

    private final ResourceReader resourceReader;

    @Override
    @SneakyThrows
    public void solve() {
        try (BufferedReader reader = resourceReader.read("y2023/puzzle21.txt")) {

            final Set<Point> rocks = new HashSet<>();
            Optional<Point> start = Optional.empty();

            int b = 0;
            String line = reader.readLine();
            final int gardenWidth = line.length();

            while (line != null) {
                for (int a = 0; a < line.length(); a++) {
                    switch (line.charAt(a)) {
                        case 'S' -> start = Optional.of(new Point(a, b));
                        case '#' -> rocks.add(new Point(a, b));
                        case '.' -> {}
                        default  -> throw new IllegalArgumentException("Unknown character: " + line.charAt(a));
                    }
                }
                line = reader.readLine();
                b++;
            }

            final int gardenHeight = b;

            // Walk the garden.
            final Map<Integer, List<Point>> visited = new HashMap<>();
            start.ifPresent(s -> visited.put(0, List.of(s)));

            long[] a = new long[3];
            long[] c = new long[3];
            int found = 0;

            BigInteger pbi;// = BigInteger.ZERO;//new BigInteger();
            final int stepMaximum = 26501365;
            long mod = stepMaximum % (gardenHeight / 2);

            for (int stepCount = 0; stepCount < stepMaximum; stepCount++) {
                final int nextStep = stepCount + 1;
                visited.get(stepCount)
                        .forEach(current -> current.cardinalNeighbours()
                            .stream()
                            .filter(p -> !rocks.contains(p))
                            .filter(p -> !visited.getOrDefault(nextStep, Collections.emptyList()).contains(p))
                            .filter(p -> p.getX() >= 0 && p.getX() < gardenWidth && p.getY() >= 0 && p.getY() < gardenHeight)
                            .forEach(point -> visited.computeIfAbsent(nextStep, k -> new ArrayList<>()).add(point)));


                if (stepCount == mod) {
                    System.out.println("found2 a: " + visited.get(stepCount).size());
                    c[found] = visited.get(stepCount).size();
                    found++;
                } else if (stepCount == mod + gardenHeight) {
                    System.out.println("found2 b: " + visited.get(stepCount).size());
                    c[found] = visited.get(stepCount).size();
                    found++;
                } else if (stepCount == (mod + (gardenHeight * 2))) {
                    System.out.println("found2 c: " + visited.get(stepCount).size());
                    c[found] = visited.get(stepCount).size();
                    found++;
                }

                if (found == 3) {
                    break;
                }

                // 26501365 % 131

                if (stepCount % gardenHeight == stepMaximum % gardenHeight) {
                    a[found] = visited.get(stepCount).size();
                    c[found] = visited.get(stepCount).size();
                    found++;
                    if (found == 3) {
                        break;
                    }
                }

            }


//            BigInteger f = new BigInteger("3778");
//            BigInteger g = new BigInteger("7521").subtract(f);
//            BigInteger h = new BigInteger("7467").subtract(g);
//            BigInteger x = new BigInteger("202300");
//
//            BigInteger q = x.multiply(x.subtract(BigInteger.ONE).divide(BigInteger.TWO));
//            BigInteger w = h.subtract(g);
//            BigInteger r = f.add(g).multiply(x).add(q).multiply(w);
//
//            System.out.println(r.toString());
//            double a = v[0];
//            double b = v[1] - v[0];
//            double c = v[2] - v[1];
//
//              a = 3778
//              b = 3743
//              c = -54
//              x = 26501365
//            return 199316766165 + (351161160180930) * (-3743);
//
//            System.out.println(Arrays.stream(a).boxed().collect(Collectors.toList()));
//            System.out.println(Arrays.stream(c).boxed().collect(Collectors.toList()));
//            long part2a = f(26501365 / gardenHeight, a);
//            double part2 = f3((double) 26501365 / gardenHeight, c);
//            System.out.println(part2);
            double part2 = f4(c);
            System.out.println(part2);
        }


    }
//    func f(x int64, a [3]int64) int64 {
//        b0 := a[0]
//        b1 := a[1]-a[0]
//        b2 := a[2]-a[1]
//        return b0 + b1*x + (x*(x-1)/2)*(b2-b1)
//    }

    private static long f(long x, long[] a) {
        long b0 = Math.abs(a[0]);
        long b1 = Math.abs(a[1] - a[0]);
        long b2 = Math.abs(a[2] - a[1]);
        return b0 + b1 * x + (x * (x - 1) / 2) * (b2 - b1);
    }

    private static long f2(long x, long[] v) {
        long a = v[0] / 2 - v[1] + v[2] / 2;
        long b = -3 * (v[0] / 2) + 2 * v[1] - v[2] / 2;
        long c = v[0];
        return (a * x * x) + (b * x) + c;
    }

    private static double f3(double x, double[] v) {
        double a = v[0];
        double b = v[1] - v[0];
        double c = v[2] - v[1];

        return a + b * x + (x * (x - 1) / 2) * (c - b);
    }

    private static long f4(long[] v) {
        long a = v[0];
        long b = v[1] - v[0];
        long c = v[2] - v[1];
        long d = c - b;

        long A = (long) Math.floor(d / 2);
        long B = b - 3 * A;
        long C = a - B - A;

        LongFunction<Long> f = n -> (long) (A * Math.pow(n, 2) + B * n + C);

        return f.apply((long) Math.ceil((double) 26501365 / 131));
    }

//    long long p1{params[0]};
//    long long p2{params[1] - params[0]};
//    long long p3{params[2] - params[1]};
//    long long ip{steps / static_cast<long long>(map.size())};
//
//    // solve the quadratic
//    long long totalSteps{p1 + p2 * ip + (ip * (ip - 1) / 2) * (p3 - p2)};

//    func simplifiedLagrange(values []int) int {
//        a := values[0]/2 - values[1] + values[2]/2
//        b := -3*(values[0]/2) + 2*values[1] - values[2]/2
//        c := values[0]
//        x := 26501365 / 131
//        return a*x*x + b*x + c
//    }

}
