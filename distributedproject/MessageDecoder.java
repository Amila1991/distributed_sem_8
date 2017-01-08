/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributedproject;

/**
 *
 * @author lahiru
 */
public class MessageDecoder extends RequestHandler {

    private String receivedIp;
    private int receivedPort;
    private final RoutingTable table;
    private CommunicationProtocol protocol;

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

    private void unregisterResponse(String message) {
        String buffer[] = message.split(" ");
        if (buffer[2] == "0") {
            /*
                solution has to be a group decision
             */
        } else if (buffer[2] == "1") {
            System.out.println("Unregistered");
        }
    }

    private void joinResponse(String message) {
        String receivingEndState = table.getNeighbouringTable().get(this.receivedIp + this.receivedPort);
        String buffer[] = message.split(" ");
        if (receivingEndState != null) {
            if (buffer[2] == "0") {
                /*
                solution has to be a group decision
                 */
            } else if (buffer[2] == "1") {
                receivingEndState = DistributedConstants.connected;
                table.updateNeighbourState(this.receivedIp, this.receivedPort, receivingEndState);
                System.out.println(this.receivedIp + ":" + this.receivedPort + " " + receivingEndState);
            }
        }
    }

    private void leaveResponse(String message) {
        String buffer[] = message.split(" ");
        if (buffer[2] == "0") {
            /*
                solution has to be a group decision
             */
        } else if (buffer[2] == "1") {
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
                System.out.println(buffer[6 + i]);
            }
        }
    }

    private void handleJoinRequest(String message) throws Exception {

        String buffer[] = message.split(" ");
        String ipOfRequestedNode = buffer[2];
        String responseMessage;
        int portOfRequestedNode = Integer.parseInt(buffer[3]);

        if (ipOfRequestedNode.equals(receivedIp) && receivedPort == portOfRequestedNode) {
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

        if (ipOfRequestedNode.equals(receivedIp) && receivedPort == portOfRequestedNode) {
            this.table.removeNeighbour(ipOfRequestedNode, portOfRequestedNode);
            updateRoutingTable(table, mainWindow);
            responseMessage = protocol.leaveResponse(0);
        } else {
            responseMessage = protocol.leaveResponse(9999);
        }

        SendMessage(responseMessage, ipOfRequestedNode, portOfRequestedNode);
    }

    private void handleSearchRequest(String message) {
    }
}
