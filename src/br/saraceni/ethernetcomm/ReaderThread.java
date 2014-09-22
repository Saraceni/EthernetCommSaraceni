/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.saraceni.ethernetcomm;

import br.saraceni.ethernetcomm.EthernetClient.ClientInterface;
import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Saraceni
 */
public class ReaderThread extends Thread {
    
    public volatile boolean isWorking = true;
    private BufferedReader bufferedReader;
    private ClientInterface clientInterface;
    
    public ReaderThread(BufferedReader bufferedReader, ClientInterface clientInterface)
    {
        this.bufferedReader = bufferedReader;
        this.clientInterface = clientInterface;
    }
    
    /**
     *
     */
    @Override
    public void run()
    {
        while(isWorking)
        {
            try
            {
                String message;
                while((message = bufferedReader.readLine()) != null)
                {
                    clientInterface.onHandleMessage(message);
                }
            }
            catch(IOException IOE)
            {
                isWorking = false;
            }
        }
    }
    
}
