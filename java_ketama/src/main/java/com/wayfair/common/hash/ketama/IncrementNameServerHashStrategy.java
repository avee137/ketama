package com.wayfair.common.hash.ketama;

import com.google.common.base.Preconditions;

import java.util.TreeMap;

/**
 * A ServerHashStrategy implementation that alters the server string used to calculate the point on the continuum with each iteration.
 */
public class IncrementNameServerHashStrategy implements ServerHashStrategy {

    private HashFunction hashFunction;

    /**
     * Instantiate a new IncrementNameServerHashStrategy using the provided HashFunction.
     *
     * @param hashFunction the HashFunction to use when placing servers on a continuum
     *
     * @throws java.lang.NullPointerException if hashFunction is null
     */
    public IncrementNameServerHashStrategy(HashFunction hashFunction) {
        Preconditions.checkNotNull(hashFunction, "hashFunction cannot be null");
        this.hashFunction = hashFunction;
    }

    @Override
    public void addServer(KetamaServer server, TreeMap<Long, KetamaServer> continuum, int pointsPerServer) {
        Preconditions.checkNotNull(server);
        Preconditions.checkNotNull(continuum);
        Preconditions.checkArgument(pointsPerServer > 0, "pointsPerServer must be greater than 0");

        for (int i = 0 ; i < pointsPerServer ; i++) {
            String serverIteration = server.getName() + "-" + i;
            continuum.put(hashFunction.calculateHash(serverIteration), server);
        }
    }

    @Override
    public void removeServer(KetamaServer server, TreeMap<Long, KetamaServer> continuum, int pointsPerServer) {
        Preconditions.checkNotNull(server);
        Preconditions.checkNotNull(continuum);
        Preconditions.checkArgument(pointsPerServer > 0, "pointsPerServer must be greater than 0");

        for (int i = 0 ; i < pointsPerServer ; i++) {
            String serverIteration = server.getName() + "-" + i;
            continuum.remove(hashFunction.calculateHash(serverIteration));
        }
    }
}
