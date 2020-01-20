package src;

import java.io.Serializable;

/**
 * @author Felix Dumbeck
 * @version Alpha
 */
public class StorageData implements Serializable {
    private static final long serialVersionUID = -2609242046305226745L;

    private String fileHash;
    private String validationHash;
    private int storageCluster;

    public StorageData(String fileHash, String validationHash, int storageCluster) {
        this.fileHash = fileHash;
        this.validationHash = validationHash;
        this.storageCluster = storageCluster;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getValidationHash() {
        return validationHash;
    }

    public void setValidationHash(String validationHash) {
        this.validationHash = validationHash;
    }

    public int getStorageCluster() {
        return storageCluster;
    }

    public void setStorageCluster(int storageCluster) {
        this.storageCluster = storageCluster;
    }

}