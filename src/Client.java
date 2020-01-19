package src;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
/**
 * @author Felix Dumbeck
 * @version Alpha
 */
public class Client {
    private List<Address> clusterNodes;
    private List<Address> interNodes;
    private int currentCluster;
    private int totalLength;
    private int port;
    private Address clientAddress;
    private Datapackage recievedDatapackage;
    private String user;
    private String passwordHash;
    private List<StorageData> sd;

    public Client(){

    }

    public Client(List<Address> clusterNodes, List<Address> interNodes, int currentCluster, int totalLength, int port,Address clientAddress) {
        this.clusterNodes = clusterNodes;
        this.interNodes = interNodes;
        this.currentCluster = currentCluster;
        this.totalLength = totalLength;
        this.port = port;
        this.clientAddress = clientAddress;
    }

    public void setPassword(String password) throws NoSuchAlgorithmException {
        this.passwordHash = new Hash().hash(password);
    }

    public Datapackage sendDatapackage(String ip, int port, Datapackage payload, boolean expectingAnswer) throws ClassNotFoundException, UnknownHostException, IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port));
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        oos.writeObject(payload);
        oos.flush();
        if (!expectingAnswer) {
            oos.close();
            socket.close();
            return null;
        }
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        Object answer = ois.readObject();

        ois.close();
        oos.close();
        socket.close();
        if (answer instanceof Datapackage) {
            return (Datapackage) answer;
        }
        return null;
    }

    public Datapackage sendDatapackage(Socket socket, Datapackage payload, boolean expectingAnswer)throws IOException, ClassNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        oos.writeObject(payload);
        oos.flush();
        if (!expectingAnswer) {
            oos.close();
            socket.close();
            return null;
        }
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        Object answer = ois.readObject();

        ois.close();
        oos.close();
        socket.close();
        if (answer instanceof Datapackage) {
            return (Datapackage) answer;
        }
        return null;
    }

    public Datapackage request(Datapackage datapackage)throws ClassNotFoundException, UnknownHostException, IOException {
        System.out.println("request: " + datapackage.getName());
        int cluster = datapackage.getCluster();
        if (cluster == this.currentCluster) {
            for (Address add : this.clusterNodes) {
                if (serverIsAvailable(add.getIp(), add.getPort()))
                    return sendDatapackage(add.getIp(), add.getPort(), datapackage, true);
            }
            for (Address add : this.interNodes) {
                if (cluster == add.getCluster()) {
                    if (serverIsAvailable(add.getIp(), add.getPort()))
                        return sendDatapackage(add.getIp(), add.getPort(), datapackage, true);
                }
            }
            return null;
        }
        sendSoftDatapackage(datapackage);
        ServerSocket serverSocket = new ServerSocket(this.port);
        Thread listeningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted() && serverSocket != null) {
                    try {
                        // waiting for client
                        final Socket tempSocket = serverSocket.accept();
                        // retrieving sent data
                        ObjectInputStream ois = new ObjectInputStream(
                                new BufferedInputStream(tempSocket.getInputStream()));

                        if (ois.readObject() instanceof Datapackage) {
                            recievedDatapackage = (Datapackage) ois.readObject();
                        }
                        serverSocket.close();
                        ois.close();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        listeningThread.start();
        return this.recievedDatapackage;
    }

    public void storeFile(String fileLocation, String fileName)throws InvalidKeyException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
        int location = (int) (Math.random()*totalLength);
        byte[] byteFile = Files.readAllBytes(Paths.get(fileLocation + fileName));
        sendSoftDatapackage(new Datapackage(5, nameHash(fileName), new EncryptedObject(byteFile, this.passwordHash), generateOwner(fileName), null, location));

        List<byte[]> storageDatas = new ArrayList<byte[]>();
        storageDatas.add(new EncryptedObject(new StorageData(nameHash(fileName), contentHash(byteFile), location), this.passwordHash).getEncryptedObject());

        for(Address add : this.clusterNodes){
            sendDatapackage(add.getIp(), add.getPort(), new Datapackage(8, nameHash("fileSystem"), storageDatas, generateOwner("fileSystem"), this.currentCluster), false);
        }
    }

    public void getFileSystem() throws ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, IOException,InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        List<byte[]> answer;
        for(Address add : this.clusterNodes){
            if(serverIsAvailable(add.getIp(), add.getPort())){
                answer = (List<byte[]>) sendDatapackage(add.getIp(), add.getPort(), new Datapackage(6, nameHash("fileSystem"), null, null, this.currentCluster), true).getPayload();
                if(answer != null){
                    this.sd = new ArrayList<>();
                    for(byte[] eo : answer){
                        StorageData storageData = (StorageData) new EncryptedObject().decrypt(eo, this.passwordHash);
                        this.sd.add(storageData);
                    }
                    return;
                }
            }
        }
    }

    public boolean pullFile(String fileLocation, String fileName, int cluster, String validationHash)throws ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Datapackage answer = request(new Datapackage(6, nameHash(fileName), null, fileName, clientAddress, cluster));
        //decrypting
        System.out.println("pulling: " + answer.getPayload().getClass());
        byte[] decryptedFile = (byte[]) ((EncryptedObject) ((Datapackage) answer.getPayload()).getPayload())
                .decrypt(passwordHash);
        //validation
        if(!contentHash(decryptedFile).equals(validationHash))    return false;
        //storing
        InputStream is = new ByteArrayInputStream(decryptedFile);
        FileOutputStream fos = new FileOutputStream(fileLocation + fileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0)  fos.write(buffer, 0, length);
        is.close();
        fos.close();
        return true;
    }

    public boolean pullFile(String fileLocation, String fileName) throws NoSuchAlgorithmException, ClassNotFoundException, UnknownHostException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        getFileSystem();
        for(StorageData s : this.sd){
            if(s.getFileHash().equals(nameHash(fileName))){
                System.out.println("name: " + nameHash(fileName) + "  clu: " + s.getStorageCluster() + "  vali: " + s.getValidationHash());
                return pullFile(fileLocation, fileName, s.getStorageCluster(), s.getValidationHash());
            }
        }
        return false;
    }

    public void sendSoftDatapackage(Datapackage datapackage) throws ClassNotFoundException, UnknownHostException, IOException {
        int cluster = datapackage.getCluster();
        int distance = Math.abs(cluster - this.currentCluster);
        Address targetNode = this.clusterNodes.get(0);
        for (int i = 0; i < this.interNodes.size(); i++) {
            if ((Math.abs(this.interNodes.get(i).getCluster() - cluster)) < distance) {
                distance = (this.interNodes.get(i).getCluster() - cluster);
                targetNode = this.interNodes.get(i);
            }
        }

        if (serverIsAvailable(targetNode.getIp(), targetNode.getPort())) {
            sendDatapackage(targetNode.getIp(), targetNode.getPort(), datapackage, false);
            return;
        }
        for (Address add : this.clusterNodes) {
            if (serverIsAvailable(add.getIp(), add.getPort())) {
                sendDatapackage(add.getIp(), add.getPort(), datapackage, false);
                return;
            }
        }
        for (Address add : this.interNodes) {
            if (serverIsAvailable(add.getIp(), add.getPort())) {
                sendDatapackage(add.getIp(), add.getPort(), datapackage, false);
                return;
            }
        }
    }

    public void setUser(String user){
        this.user = user;
    }

    public void setPasswordHash(String password) throws NoSuchAlgorithmException {
        this.passwordHash = new Hash().hash(password);
    }

    private String nameHash(String fileName) throws NoSuchAlgorithmException {
        return new Hash().hash(fileName + this.user + this.passwordHash);
    }
    
    private String generateOwner(String fileName) throws NoSuchAlgorithmException {
        return new Hash().hash(fileName + new Hash().hash(this.user) + this.passwordHash);
    }

    private String contentHash(byte [] arr) throws NoSuchAlgorithmException {
        return new Hash().hash(arr);
    }

    public boolean serverIsAvailable(String ip, int port){
        try {
            Socket testSocket = new Socket();
            testSocket.connect(new InetSocketAddress(ip, port));
            if (!testSocket.isConnected()) {
                testSocket.close();
                return false;
            }
            testSocket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}