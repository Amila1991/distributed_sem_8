/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributedproject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author lahiru
 */
public class MessageDecoder extends RequestHandler {

    private String receivedIp;
    private int receivedPort;
    private final RoutingTable table;
    private final CommunicationProtocol protocol;

    public MessageDecoder(ControlPanel mainWindow) {
        super(mainWindow);
        table = RoutingTable.getInstance();
        protocol = CommunicationProtocol.getInstance();
    }

    public void DecodeMessage(String msg, String receivedIp, int receivedPort) throws Exception {
        if (receivedIp != null && receivedPort >= 0) {
            this.receivedIp = receivedIp;
            this.receivedPort = receivedPort;
        }

        if (msg.contains("REGOK")) {
            registerResponse(msg);
        } else if (msg.contains("UNREGOK")) {
            unregisterResponse(msg);
        } else if (msg.contains("JOINOK")) {
            joinResponse(msg);
        } else if (msg.contains("LEAVEOK")) {
            leaveResponse(msg);
        } else if (msg.contains("SEROK")) {
            searchResponse(msg);
        } else if (msg.contains("JOIN")) {
            handleJoinRequest(msg);
        } else if (msg.contains("LEAVE")) {
            handleLeaveRequest(msg);
        } else if (msg.contains("SER")) {
            handleSearchRequest(msg);
        }
    }

    private void registerResponse(String message) {
        String buffer[] = message.split(" ");
        int neighboursCount = Integer.parseInt(buffer[2]);

        if (neighboursCount > 0 && neighboursCount <= DistributedConstants.numberOfneighbours) {
            for (int i = 0; i < neighboursCount; i++) {
                table.addNeighBour(buffer[3 + 2 * i], Integer.parseInt(buffer[4 + 2 * i]));
                updateRoutingTable(table, mainWindow);
            }
        }
    }

    private void unregisterResponse(String message) throws Exception {
        String buffer[] = message.split(" ");
        if (buffer[2] == "9999") {
            /*
                solution has to be a group decision
             */
        } else if (buffer[2].equals("0")) {
            System.out.println("Unregistered");

            Iterator<String> iterator = table.getNeighbouringTable().keySet().iterator();
            String tempKey;
            while (iterator.hasNext()) {
                tempKey = iterator.next();
                if (table.getNeighbouringTable().get(tempKey).equals(DistributedConstants.connected)) {
                    String[] temp = tempKey.split(":");
                    String fileRequestMsg = protocol.leave(RequestHandler.clientIP, RequestHandler.socket.getLocalPort());
                    SendMessage(fileRequestMsg, temp[0], Integer.parseInt(temp[1]));
                }
            }

        }
    }

    private void joinResponse(String message) {
        String receivingEndState = table.getNeighbouringTable().get(this.receivedIp + this.receivedPort);
        String buffer[] = message.split(" ");
        if (receivingEndState != null) {
            if (buffer[2] == "9999") {
                /*
                solution has to be a group decision
                 */
            } else if (buffer[2] == "0") {
                receivingEndState = DistributedConstants.connected;
                table.updateNeighbourState(this.receivedIp, this.receivedPort, receivingEndState);
                this.updateRoutingTable(table, mainWindow);
            }
        }
    }

    private void leaveResponse(String message) {
        String buffer[] = message.split(" ");
        if (buffer[2] == "9999") {
            /*
                solution has to be a group decision
             */
        } else if (buffer[2] == "0") {
            System.out.println("Succesfully Left");
        }
    }
//assume file hosted node directly communicate with requested node

    private void searchResponse(String message) {
        String buffer[] = message.split(" ");
        int fileCount = Integer.parseInt(buffer[2]);
        if (fileCount > 0) {
            String fileHostedNodeIP = buffer[3];
            int fileHostedNodePort = Integer.parseInt(buffer[4]);
            int requiredHops = Integer.parseInt(buffer[5]);

            for (int i = 0; i < fileCount; i++) {
                mainWindow.getDisplaySearchResult().append(buffer[6 + i] + " ==> " + fileHostedNodeIP + ":" + fileHostedNodePort + "\n");
            }
        }
    }

    private void handleJoinRequest(String message) throws Exception {

        String buffer[] = message.split(" ");
        String ipOfRequestedNode = buffer[2];
        String responseMessage;
        int portOfRequestedNode = Integer.parseInt(buffer[3]);

        if (ipOfRequestedNode.equals(this.receivedIp) && this.receivedPort == portOfRequestedNode) {
            this.table.getNeighbouringTable().put(ipOfRequestedNode + ":" + portOfRequestedNode, DistributedConstants.connected);
            updateRoutingTable(table, mainWindow);
            responseMessage = protocol.joinResponse(0);
        } else {
            responseMessage = protocol.joinResponse(9999);
        }

        SendMessage(responseMessage, ipOfRequestedNode, portOfRequestedNode);
    }

    private void handleLeaveRequest(String message) throws Exception {
        String buffer[] = message.split(" ");
        String ipOfRequestedNode = buffer[2];
        String responseMessage;
        int portOfRequestedNode = Integer.parseInt(buffer[3]);

        if (ipOfRequestedNode.equals(this.receivedIp) && this.receivedPort == portOfRequestedNode) {
            this.table.removeNeighbour(ipOfRequestedNode, portOfRequestedNode);
            updateRoutingTable(table, mainWindow);
            responseMessage = protocol.leaveResponse(0);
        } else {
            responseMessage = protocol.leaveResponse(9999);
        }

        SendMessage(responseMessage, ipOfRequestedNode, portOfRequestedNode);
    }

    private void handleSearchRequest(String message) throws Exception {
        String buffer[] = message.split("'");
        String buffer_1[] = message.split(" ");
        String ipOfRequestedNode = buffer_1[2];
        String fileName = buffer[1];
        int fileCount = 0;
        int portOfRequestedNode = Integer.parseInt(buffer_1[3]);
        int hopCount = Integer.parseInt(buffer_1[buffer_1.length - 1]);
        String fileList = "";
        List<String> list;
        hopCount++;
        if (hopCount <= DistributedConstants.defaultHops) {
            String[] keywords = fileName.split("_");
            for (int i = 0; i < keywords.length; i++) {
                list = table.getFileMap().get(keywords[i]);
                for (int j = 0; j < list.size(); j++) {
                    String tempFileName = list.get(i);
                    if (fileList.contains(tempFileName)) {
                        fileList += tempFileName + " ";
                        fileCount++;
                    }
                }
            }
            String fileRequestMsg = protocol.searchFile(ipOfRequestedNode, portOfRequestedNode, hopCount, fileName);
            Iterator<String> iterator = table.getNeighbouringTable().keySet().iterator();
            String tempKey;
            while (iterator.hasNext()) {
                tempKey = iterator.next();
                if (table.getNeighbouringTable().get(tempKey).equals(DistributedConstants.connected)
                        && !tempKey.equals(this.receivedIp + ":" + this.receivedPort)) {
                    String[] temp = tempKey.split(":");
                    SendMessage(fileRequestMsg, temp[0], Integer.parseInt(temp[1]));
                }
            }
        }

        if (fileList.length() > 0) {
            String searchResponse = protocol.searchResponse(fileCount, RequestHandler.clientIP, RequestHandler.socket.getLocalPort(), hopCount, fileList);
            SendMessage(searchResponse, ipOfRequestedNode, portOfRequestedNode);
        }
    }
}
