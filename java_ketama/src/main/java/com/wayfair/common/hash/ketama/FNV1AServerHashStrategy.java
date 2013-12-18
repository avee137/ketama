package com.wayfair.common.hash.ketama;

import com.google.common.base.Preconditions;

import java.util.TreeMap;

/**
 * A ServerHashStrategy implementation that uses the previous hash value as a starting point with each iteration.
 */
public class FNV1AServerHashStrategy implements ServerHashStrategy {

    private FNV1A32HashFunction hashAlgorithm = new FNV1A32HashFunction();

    @Override
    public void addServer(KetamaServer server, TreeMap<Long, KetamaServer> continuum, int pointsPerServer) {
        Preconditions.checkNotNull(server);
        Preconditions.checkNotNull(continuum);
        Preconditions.checkArgument(pointsPerServer > 0, "pointsPerServer must be greater than 0");

        long hashValue = hashAlgorithm.calculateHash(server.getName());
        continuum.put(hashValue, server);

        for (int i = 0 ; i < (pointsPerServer-1) ; i++) {
            hashValue = hashAlgorithm.calculateHash(server.getName(), hashValue);
            continuum.put(hashValue, server);
        }
    }

    @Override
    public void removeServer(KetamaServer server, TreeMap<Long, KetamaServer> continuum, int pointsPerServer) {
        Preconditions.checkNotNull(server);
        Preconditions.checkNotNull(continuum);
        Preconditions.checkArgument(pointsPerServer > 0, "pointsPerServer must be greater than 0");

        long hashValue = hashAlgorithm.calculateHash(server.getName());
        continuum.remove(hashValue);

        for (int i = 0 ; i < (pointsPerServer-1) ; i++) {
            hashValue = hashAlgorithm.calculateHash(server.getName(), hashValue);
            continuum.remove(hashValue);
        }
    }
}
