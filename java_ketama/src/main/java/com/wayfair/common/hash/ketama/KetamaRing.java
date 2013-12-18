package com.wayfair.common.hash.ketama;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;

/**
 * A KetamaRing can be used to map keys to a pool of servers. This consistent hashing approach maps each server to 160
 * points on the ring. When a server is added or remove, only a subset of the keys will be mapped to new servers,
 * rather than remapping everything.
 * <p/>
 * This is not a thread-safe implementation.
 */
public class KetamaRing {

    private static final int POINTS_PER_SERVER = 160;

    private ServerHashStrategy serverHashStrategy;
    private HashFunction keyHashFunction;
    private TreeMap<Long, KetamaServer> continuum;
    private Set<KetamaServer> servers;
    private DateTime modifiedDate;

    /**
     * Create a KetamaRing with the specified ServerHashStrategy and Set of servers.
     *
     * @param serverHashStrategy the hashing strategy to use when distributing the servers around the ring, and when
     *                           picking a server given a key
     * @param keyHashFunction    the HashFunction to be used when mapping keys to points on the ring
     * @param servers            a Set of servers to on the ring initially
     *
     * @throws java.lang.NullPointerException if serverHashStrategy is null
     * @throws java.lang.NullPointerException if keyHashFunction is null
     * @throws java.lang.NullPointerException if servers is null
     */
    public KetamaRing(ServerHashStrategy serverHashStrategy, HashFunction keyHashFunction, Set<KetamaServer> servers) {
        Preconditions.checkNotNull(serverHashStrategy, "serverHashStrategy cannot be null");
        Preconditions.checkNotNull(keyHashFunction, "keyHashFunction cannot be null");
        Preconditions.checkNotNull(servers, "servers cannot be null");

        this.serverHashStrategy = serverHashStrategy;
        this.keyHashFunction = keyHashFunction;
        this.servers = new HashSet<KetamaServer>();
        continuum = new TreeMap<Long, KetamaServer>();
        add(servers);
    }

    /**
     * Create an empty KetamaRing with the specified ServerHashStrategy.
     *
     * @param serverHashStrategy the hashing strategy to use when distributing the servers around the ring, and when
     *                           picking a server given a key
     * @param keyHashFunction    the HashFunction to be used when mapping keys to points on the ring
     *
     * @throws java.lang.NullPointerException if serverHashStrategy is null
     * @throws java.lang.NullPointerException if keyHashFunction is null
     */
    public KetamaRing(ServerHashStrategy serverHashStrategy, HashFunction keyHashFunction) {
        this(serverHashStrategy, keyHashFunction, Collections.<KetamaServer>emptySet());
    }

    /**
     * Synchronize the ring with the provided set of servers. This will remove all servers currently in the ring
     * but not in the provided set, and will add all servers in the provided set but not in the ring. Any servers in
     * the provided set and currently in the ring will be untouched.
     *
     * @param newServers the set of servers to be used for synchronization; after synchronization is complete this
     *                   set and the set of servers in the ring will match
     */
    public void synchronizeRing(Set<KetamaServer> newServers) {
        Preconditions.checkNotNull(newServers, "newServers cannot be null");

        // Make a copy of the Set before altering it. Otherwise you run into a ConcurrentModificationException.
        Set<KetamaServer> currentServers = new HashSet<KetamaServer>(getServers());

        // Add new servers
        add(Sets.difference(newServers, currentServers));

        // Remove missing servers
        remove(Sets.difference(currentServers, newServers));
    }

    /**
     * Add a server to the ring at 160 different points
     *
     * @param server the server to add to the ring
     *
     * @throws java.lang.NullPointerException if server is null
     */
    public void add(KetamaServer server) {
        Preconditions.checkNotNull(server, "server cannot be null");

        if (contains(server)) {
            return;
        }

        serverHashStrategy.addServer(server, continuum, POINTS_PER_SERVER);
        servers.add(server);
        modifiedDate = DateTime.now(DateTimeZone.UTC);
    }

    /**
     * Add a set of servers to the ring, each at 160 different points
     *
     * @param servers the set of servers to add to the ring
     *
     * @throws java.lang.NullPointerException if servers is null
     */
    public void add(Set<KetamaServer> servers) {
        Preconditions.checkNotNull(servers, "servers cannot be null");

        for (KetamaServer server : servers) {
            add(server);
        }
    }

    /**
     * Remove a server from the ring
     *
     * @param server the server to remove from the ring
     *
     * @throws java.lang.NullPointerException if server is null
     */
    public void remove(KetamaServer server) {
        Preconditions.checkNotNull(server, "server cannot be null");

        if (!contains(server)) {
            return;
        }

        serverHashStrategy.removeServer(server, continuum, POINTS_PER_SERVER);
        servers.remove(server);
        modifiedDate = DateTime.now(DateTimeZone.UTC);
    }

    /**
     * Remove a set of servers from the ring
     *
     * @param servers the set of servers to remove from the ring
     *
     * @throws java.lang.NullPointerException if servers is null
     */
    public void remove(Set<KetamaServer> servers) {
        Preconditions.checkNotNull(servers, "servers cannot be null");

        for (KetamaServer server : servers) {
            remove(server);
        }
    }

    /**
     * Returns true if the ring contains the specified server.
     *
     * @param server the server whose presence in the ring is to be tested
     *
     * @return true if the rings contains the specified server
     *
     * @throws java.lang.NullPointerException if server is null
     */
    public boolean contains(KetamaServer server) {
        Preconditions.checkNotNull(server, "server cannot be null");
        return servers.contains(server);
    }

    /**
     * Get a server given a key. This picks the server with a point on the ring greater than or equal to the
     * hash of the key. If the hash is greater than all points on the ring, it returns the first point.
     *
     * @param key the key to be hashed to select a server point on the ring
     *
     * @return the server associated with a point on the ring greater than or equal to the hash of the key; the
     * server associated with the the first point on the ring if the hash is greater than all points on the rings
     *
     * @throws java.lang.NullPointerException if key is null
     */
    public KetamaServer get(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        if (size() == 0) {
            throw new NoSuchElementException("The ring is empty!");
        }

        long hashValue = keyHashFunction.calculateHash(key);

        // find the next point on the continuum after hashValue
        Map.Entry<Long, KetamaServer> entry = continuum.ceilingEntry(hashValue);

        // if entry is null then there is no point on the continuum greater than hashValue
        // so wrap to the beginning and use the first point
        if (entry == null) {
            entry = continuum.firstEntry();
        }

        return entry.getValue();
    }

    /**
     * Get the set of servers currently in the ring. The set cannot be modified.
     *
     * @return an unmodifiable set of servers currently in the ring
     */
    public Set<KetamaServer> getServers() {
        return Collections.unmodifiableSet(servers);
    }

    /**
     * Get the number of servers in the ring.
     *
     * @return the number of servers in the ring
     */
    public int size() {
        return getServers().size();
    }

    /**
     * Get the datetime of the last modification of the ring -- addition or removal of servers.
     *
     * @return the last datetime a server was added or removed
     */
    public DateTime getModifiedDate() {
        return modifiedDate;
    }

}
