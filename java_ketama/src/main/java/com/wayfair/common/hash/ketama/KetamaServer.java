package com.wayfair.common.hash.ketama;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A Ketama server, with a hostname and port.
 */
public class KetamaServer {

    private String hostname;
    private int port;
    private String name;

    /**
     * Instantiate a new KetamaServer.
     *
     * @param name the logical name of the server
     * @param hostname the hostname of the server
     * @param port the port of the server
     *
     * @throws java.lang.IllegalArgumentException if name is blank
     * @throws java.lang.IllegalArgumentException if hostname is blank
     */
    public KetamaServer(String name, String hostname, int port) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "name cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(hostname), "hostname cannot be blank");

        this.name = name;
        this.hostname = hostname;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof KetamaServer) {
            KetamaServer server = (KetamaServer) object;

            return new EqualsBuilder()
                    .append(server.getName(), getName())
                    .append(server.getHostname(), getHostname())
                    .append(server.getPort(), getPort())
                    .build();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .append(getHostname())
                .append(getPort())
                .build();
    }

    @Override
    public String toString() {
        return getName() + ": " + getHostname() + ":" + getPort();
    }
}
