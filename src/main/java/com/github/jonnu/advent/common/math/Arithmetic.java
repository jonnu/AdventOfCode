package com.github.jonnu.advent.common.math;

public class Arithmetic {

    // greatest common divisor
    public static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    // least common multiple
    public static long lcm(long a, long b) {
        return a * (b / gcd(a, b));
    }

    // lcm amongst a set
    public static long lcm(final long[] input) {
        long result = input[0];
        for(int i = 1; i < input.length; i++) {
            result = lcm(result, input[i]);
        }
        return result;
    }
}
