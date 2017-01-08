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
public class MessageDecoder {

    ControlPanel mainWindow;
    private String receivedIp;
    private int receivedPort;
    private final RoutingTable table;

    public MessageDecoder(ControlPanel mainWindow) {
        this.mainWindow = mainWindow;
        table = RoutingTable.getInstance();
    }

    public void DecodeMessage(String msg, String receivedIp, int receivedPort) {
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

//    private void DecodeMsg_Reg(String msg) {
//        String[] arr = msg.split(" ");
//        int numNeighbours = Integer.parseInt(arr[2]);
//        if (numNeighbours > 0 && numNeighbours < 3) {
//            for (int i = 0; i < numNeighbours; i++) {
//                Object[] newRecord = {arr[3 + 2 * i], arr[4 + 2 * i]};
//                mainWindow.model.addRow(newRecord);
//            }
//        }
//    }
    private void registerResponse(String message) {
        String buffer[] = message.split(" ");
        int neighboursCount = Integer.parseInt(buffer[2]);

        if (neighboursCount > 0 && neighboursCount <= DistributedConstants.numberOfneighbours) {
            for (int i = 0; i < neighboursCount; i++) {
                table.addNeighBour(buffer[3 + 2 * i], Integer.parseInt(buffer[4 + 2 * i]));
                Object[] newRecord = {buffer[3 + 2 * i] + ":" + buffer[4 + 2 * i], DistributedConstants.notConnected};
                mainWindow.model.addRow(newRecord);
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

    private void handleJoinRequest(String message) {
    }

    private void handleLeaveRequest(String message) {
    }

    private void handleSearchRequest(String message) {
    }
}
