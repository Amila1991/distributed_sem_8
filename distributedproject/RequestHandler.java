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
import java.util.Map;

/**
 *
 * @author yellowflash
 */
public abstract class RequestHandler {

    public ControlPanel mainWindow;
    public static DatagramSocket socket;

    public void SetupSocket() {
        try {
            RequestHandler.socket = new DatagramSocket();
            this.mainWindow.getTxtClientPort().setText("" + RequestHandler.socket.getPort());
        } catch (SocketException soe) {
            soe.printStackTrace();
        }
    }

    public RequestHandler(ControlPanel mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void SendMessage(String message, String Dest_Ip, int Dest_port) throws Exception {
        mainWindow.displayMessage('\n' + "OUT - " + message);
        byte[] buf = message.getBytes();
        try {
            InetAddress address = InetAddress.getByName(Dest_Ip);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Dest_port);
            RequestHandler.socket.send(packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DatagramPacket receiveMessage() {
        byte[] buffer = new byte[65536];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        try {
            RequestHandler.socket.receive(incoming);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return incoming;
    }

    public void updateRoutingTable(RoutingTable table, ControlPanel mainWindow) {
        Map<String, String> neighbourTable = table.getNeighbouringTable();
        Iterator<String> keySet = neighbourTable.keySet().iterator();

        for (int i = 0; i < mainWindow.getNeighbourTable().getRowCount(); i++) {
            mainWindow.getNeighbourTable().removeRow(i);
        }

        while (keySet.hasNext()) {
            String key = keySet.next();
            Object[] temp = {key, neighbourTable.get(key)};
            mainWindow.getNeighbourTable().addRow(temp);
        }
    }

}
