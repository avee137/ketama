package com.wayfair.common.hash.ketama;

/**
 * Uses String's native hashCode() method for a hash value.
 */
public class NativeHashFunction implements HashFunction {
    @Override
    public long calculateHash(String input) {
        return input.hashCode();
    }
}
