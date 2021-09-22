package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.util.Random;

public class RandomGen {
    public static final Random RANDOM = new Random(System.currentTimeMillis());

    /**
     * Returns a pseudorandom, uniformly distributed long value between 1 (inclusive) and the specified value (inclusive),
     * drawn from this random number generator's sequence.
     *
     * @param bound
     * @return
     */
    public static long nextLong(long bound) {
        return 1 + (long) (RANDOM.nextDouble() * bound);
    }
}
