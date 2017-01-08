/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributedproject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lahiru
 */
public class Client extends RequestHandler {

    private String serverIp = "";
    private String ClientIp = "";
    private int ClientPort;
    private int serverPort;
    private String userName = "";

    public MessageDecoder msgDecoder;
    private final CommunicationProtocol protocol;
    private final RoutingTable routingTable;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClientIp() {
        return ClientIp;
    }

    public void setClientIp(String ClientIp) {
        this.ClientIp = ClientIp;
    }

    public int getClientPort() {
        return ClientPort;
    }

    public void setClientPort(int ClientPort) {
        this.ClientPort = ClientPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public Client(ControlPanel mainWindow) {
        super(mainWindow);
        msgDecoder = new MessageDecoder(mainWindow);
        protocol = CommunicationProtocol.getInstance();
        routingTable = RoutingTable.getInstance();
    }

    //this is the genuine method
    public void SendRegisterPacket() throws Exception {
        String tempMessage = protocol.register(ClientIp, ClientPort, userName);
        SendMessage(tempMessage, serverIp, serverPort);
    }

    // this is temporary method used to test the system
    // using this method call we can avoid the need of multiple PCs
    public void SendRegisterPacket(String Ip, int port, String userName) throws Exception {
        String tempMessage = protocol.register(Ip, port, userName);
        SendMessage(tempMessage, serverIp, serverPort);
    }

    public void SendJoinPacket(String NodeIp, int nodePort) throws Exception {
        String tempMessage = protocol.join(NodeIp, nodePort);
        SendMessage(tempMessage, serverIp, serverPort);

    }

    public void searchFile(String fileName) throws Exception {
        String tempMessage = protocol.searchFile(this.ClientIp, this.ClientPort, DistributedConstants.defaultHops, fileName);
        Iterator<String> iterator = routingTable.getNeighbouringTable().keySet().iterator();
        String tempKey;

        while (iterator.hasNext()) {
            tempKey = iterator.next();
            if (routingTable.getNeighbouringTable().get(tempKey).equals(DistributedConstants.connected)) {
                String[] temp = tempKey.split(":");
                SendMessage(tempMessage, temp[0], Integer.parseInt(temp[1]));
            }
        }

    }

    public void RunMessageGateway() {
        Thread T = new Thread() {
            public void run() {
                try {
                    whileRunning();
                } catch (Exception ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        T.start();
    }

    public void whileRunning() throws Exception {
        DatagramPacket incomingPacket;
        while (true) {
            incomingPacket = receiveMessage();
            byte[] data = incomingPacket.getData();
            String s = new String(data, 0, incomingPacket.getLength());
            //echo the details of incoming data - client ip : client port - client message
            mainWindow.displayMessage('\n' + "IN - " + s);
            msgDecoder.DecodeMessage(s, incomingPacket.getAddress().toString(), incomingPacket.getPort());
        }
    }

}
