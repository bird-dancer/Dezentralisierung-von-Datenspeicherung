package src;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Felix Dumbeck
 * @version Alpha
 */
public class Hash {
    /**
     * 
     * @param bytes
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String hash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        bytes = md.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public String hash(Object object) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(baos);
        oo.writeObject(object);
        return hash(baos.toByteArray());
    }

    /**
     * hashes a String using SHA3<br>
     * inspired by: <a href=
     * "https://www.baeldung.com/sha-256-hashing-java#message-digest">https://www.baeldung.com/sha-256-hashing-java#message-digest</a>
     * 
     * @param originalString
     * @return hashed String in a hexadecimal-format
     * @throws NoSuchAlgorithmException
     * 
     */
    public String hash(String originalString) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return bytesToHex(md.digest(originalString.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 
     * @param bytes
     * @return a number with base 16 as a String
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}