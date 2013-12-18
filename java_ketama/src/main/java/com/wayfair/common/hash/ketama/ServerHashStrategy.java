package com.wayfair.common.hash.ketama;

import java.util.TreeMap;

public interface ServerHashStrategy {
    /**
     * Add a server to a continuum.
     *
     * @param server the server to be added
     * @param continuum the continuum to which the server will be added
     * @param pointsPerServer the number of points on the continuum the server will be associated with; must be greater than 0
     *
     * @throws java.lang.NullPointerException if server is null
     * @throws  java.lang.NullPointerException if continuum is null
     * @throws java.lang.IllegalArgumentException if pointsPerServer is not greater than 0
     */
    void addServer(KetamaServer server, TreeMap<Long, KetamaServer> continuum, int pointsPerServer);

    /**
     * Remove a server from a continuum.
     *
     * @param server the server to be remove
     * @param continuum the continuum to which the server will be removed
     * @param pointsPerServer the number of points on the continuum the server was associated with when added; must be greater than 0
     *
     * @throws java.lang.NullPointerException if server is null
     * @throws  java.lang.NullPointerException if continuum is null
     * @throws java.lang.IllegalArgumentException if pointsPerServer is not greater than 0
     */
    void removeServer(KetamaServer server, TreeMap<Long, KetamaServer> continuum, int pointsPerServer);
}
