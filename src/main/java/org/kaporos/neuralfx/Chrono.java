package org.kaporos.neuralfx;

public class Chrono {
    private static long current;
    private static String reason;
    static public void start(String reason) {
        current = System.currentTimeMillis();
        Chrono.reason = reason;
    }
    static public void end() {
        System.out.printf("=== Time taken to do %s: %d ms\n", reason, System.currentTimeMillis() - current);
    }
}
