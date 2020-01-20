package src;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Felix Dumbeck
 * @version Alpha
 */

public class Datapackage implements Serializable {
    private static final long serialVersionUID = -1922732218549212535L;

    private int id;
    private String name;
    private Object payload;
    private String owner;
    private Address returnAddress;
    private int cluster;
    private ArrayList<Object> shard;

    /**
     * 
     * @param id
     * @param name
     * @param payload
     * @param owner
     */
    public Datapackage(int id, String name, Object payload, String owner, int cluster) {
        this.id = id;
        this.name = name;
        this.payload = payload;
        this.owner = owner;
        this.cluster = cluster;
    }

    public Datapackage(int id, String name, Object payload, String owner, Address returnAddress, int cluster) {
        this(id, name, payload, owner, cluster);
        this.returnAddress = returnAddress;
    }

    /**
     * 
     * @return the Datapackages id if it doesn't hava a id it will return
     *         <code>null</code>
     */
    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Object getPayload() {
        return payload;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Address getRerturnAddress() {
        return this.returnAddress;
    }

    public int getCluster() {
        return this.cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }
}