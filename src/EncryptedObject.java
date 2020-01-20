package src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Felix Dumbeck
 * @version Alpha
 */
public class EncryptedObject implements Serializable {
    private static final long serialVersionUID = -9099278460443798511L;

    private byte[] encryptedObject;

    public EncryptedObject() {

    }

    public EncryptedObject(Object decryptedObject, String password) throws IOException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(baos);
        oo.writeObject(decryptedObject);
        this.encryptedObject = encrypt(baos.toByteArray(), password);
    }

    private byte[] encrypt(byte[] byteArray, String password) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key key = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(byteArray);
    }

    private Key generateKey(String password) {
        String key = password;
        while (key.length() < 32)
            key += key;
        key = key.substring(0, 32);
        return new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), "AES");
    }

    public Object decrypt(String password) throws IOException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException {
        return decrypt(this.encryptedObject, password);
    }

    public Object decrypt(byte[] encryptedObject, String password)
            throws InvalidKeyException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException,
            IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        Key key = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new ObjectInputStream(new ByteArrayInputStream(cipher.doFinal(encryptedObject))).readObject();
    }

    public byte[] getEncryptedObject() {
        return this.encryptedObject;
    }
}