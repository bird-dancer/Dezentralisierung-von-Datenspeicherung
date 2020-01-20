package src;

import java.io.Serializable;

/**
 * @author Felix Dumbeck
 * @version Alpha
 */
public class Address implements Serializable {
    private static final long serialVersionUID = 8737221118248928508L;
    protected int id;
    protected int cluster;
    protected String ip;
    protected int port;

    /**
     * 
     * @param id
     * @param ip
     * @param port
     */
    public Address(int id, String ip, int port, int cluster) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.cluster = cluster;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getCluster() {
        return this.cluster;
    }

}
