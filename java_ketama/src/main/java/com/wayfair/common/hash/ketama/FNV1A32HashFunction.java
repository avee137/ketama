package com.wayfair.common.hash.ketama;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

/**
 * This FNV implementation was adapted from Jake Douglas's fnv-java project: https://github.com/jakedouglas/fnv-java
 */
public class FNV1A32HashFunction implements HashFunction {

    private static final long INIT32  = 2166136261L; // 0x811c9dc5
    private static final BigInteger PRIME32 = new BigInteger("01000193", 16);
    private static final BigInteger MOD32   = new BigInteger("2").pow(32);

    @Override
    public long calculateHash(String input) {
        return calculateHash(input, INIT32);
    }

    /**
     * Calculate a hash value given an input and initialValue. The input String will be encoded as a UTF-8 byte array before hashing.
     *
     * @param input
     * @param initialValue
     *
     * @return hash value given input and initialValue
     *
     * @throws  java.lang.NullPointerException if input is null
     */
    public long calculateHash(String input, long initialValue) {
        Preconditions.checkNotNull(input);

        byte[] bytes;
        try {
            bytes = input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }

        BigInteger hash = BigInteger.valueOf(initialValue);
        for (byte b : bytes) {
            hash = hash.xor(BigInteger.valueOf((int) b & 0xff));
            hash = hash.multiply(PRIME32).mod(MOD32);
        }

        return hash.longValue();
    }
}
