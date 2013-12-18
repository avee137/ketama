package com.wayfair.common.hash.ketama;

public interface HashFunction {
    /**
     * Calculate a hash value given the input.
     *
     * @param input the input value used to calculate the hash
     *
     * @return a hash value given the input
     *
     * @throws java.lang.NullPointerException if input is null
     */
    long calculateHash(String input);
}
