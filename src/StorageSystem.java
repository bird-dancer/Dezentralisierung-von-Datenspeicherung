package src;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Felix Dumbeck
 * @version Alpha
 */

public class StorageSystem implements Serializable {
    private static final long serialVersionUID = -3577119964736812050L;
    private File storageFile; // stores the hashtable inside of a file in the file system
    private HashMap<String, Object> hm; // creates an key-value-pair the key is a String, whilst the value can be any
                                        // serializable object

    /**
     * 
     * @param location where the <code>storageFile</code> should be stored
     * @param fileName name of the <code>storageFile</code> it should include the
     *                 filename extesion
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public StorageSystem(String location, String fileName)
            throws FileNotFoundException, ClassNotFoundException, IOException {
        this.storageFile = new File(location + fileName);
        if (new File(location + fileName).exists()) {
            load(location, fileName);
            return;
        }
        this.hm = new HashMap<>();
        this.storageFile = new File(location + fileName);
    }

    /**
     * stores an object belonging to a key inside of the hashtable
     * 
     * @param key
     * @param value
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void put(String key, Object value) throws FileNotFoundException, IOException {
        this.hm.put(key, value);
    }

    /**
     * retrieves an object belonging to the key from the hashtable
     * 
     * @param key
     * @return the stored object belonging to the <code>key</code>
     */
    public Object get(String key) {
        return this.hm.get(key);
    }

    /**
     * writes the hashtable to the storageFile
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void store() throws FileNotFoundException, IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.storageFile));
        oos.writeObject(hm);
        oos.flush();
        oos.close();
    }

    /**
     * saves the hashtable from a file inside of this.hashtable
     * 
     * @param location
     * @param fileName
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void load(String location, String fileName)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(location + fileName)));
        this.hm = (HashMap<String, Object>) ois.readObject();
        ois.close();
    }

    /**
     * removes one item from the storage hash map
     * 
     * @param key key of the value to be deleted
     */
    public void remove(String key) {
        this.hm.remove(key);
    }
}