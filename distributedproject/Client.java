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

/**
 *
 * @author lahiru
 */
public class Client {

    private String serverIp = "";
    private String ClientIp = "";
    private int ClientPort;
    private int serverPort;
    private String userName = "";
    public ControlPanel mainWindow;
    public DatagramSocket socket;
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
        this.mainWindow = mainWindow;
        msgDecoder = new MessageDecoder(mainWindow);
        protocol = CommunicationProtocol.getInstance();
        routingTable = RoutingTable.getInstance();
    }

    public void SetupSocket() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException soe) {
            soe.printStackTrace();
        }
    }

    public void SendMessage(String message, String Dest_Ip, int Dest_port) throws Exception {

        mainWindow.displayMessage('\n' + "OUT - " + message);
        byte[] buf = message.getBytes();
        try {
            InetAddress address = InetAddress.getByName(Dest_Ip);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Dest_port);
            socket.send(packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    public void RunMessageGateway() {
        Thread T = new Thread() {
            public void run() {
                whileRunning();
            }
        };
        T.start();
    }

    public void whileRunning() {
        while (true) {
            byte[] buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(incoming);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] data = incoming.getData();
            String s = new String(data, 0, incoming.getLength());
            //echo the details of incoming data - client ip : client port - client message
            mainWindow.displayMessage('\n' + "IN - " + s);
            msgDecoder.DecodeMessage(s, incoming.getAddress().toString(), incoming.getPort());
        }
    }
}
