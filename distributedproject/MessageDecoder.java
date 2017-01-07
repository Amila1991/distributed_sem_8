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
    
    public MessageDecoder(ControlPanel mainWindow)
    {
        this.mainWindow = mainWindow;
    }
    
    public void DecodeMessage(String msg){
        if(msg.contains("REGOK"))
        {
            DecodeMsg_Reg(msg);
        }
    }
    
    private void DecodeMsg_Reg(String msg)
    {
        String[] arr = msg.split(" ");
        int numNeighbours = Integer.parseInt(arr[2]);
        if(numNeighbours>0 && numNeighbours < 3)
        {
            for (int i = 0; i < numNeighbours; i++) {
                Object[] newRecord = {arr[3+2*i],arr[4+2*i]};
                mainWindow.model.addRow(newRecord);
            }
        }
    }

 
}
