/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.saraceni.ethernetcomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Saraceni
 */
public class EthernetClient {
    
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private InetAddress ip_address;
    private int port;
    private ClientInterface clientInterface;
    private ReaderThread readerThread;
    
    public EthernetClient(InetAddress ip_address, int port, ClientInterface clientInterface)
    {
        this.ip_address = ip_address;
        this.port = port;
        this.clientInterface = clientInterface;
    }
    
    public boolean connect(){
	if(socket != null && socket.isConnected()){
		return false;
	}
	try{
		socket = new Socket(ip_address, port);
		InputStreamReader ir = new InputStreamReader(socket.getInputStream());
		bufferedReader = new BufferedReader(ir);
		printWriter = new PrintWriter(socket.getOutputStream());
		readerThread = new ReaderThread(bufferedReader, clientInterface);
		readerThread.start();
		return true;
	}
	catch(UnknownHostException UHE){
           UHE.printStackTrace();
           return false;
       }
       catch(SecurityException SE){
           SE.printStackTrace();
           return false;
       }
       catch(NumberFormatException NFE){
           NFE.printStackTrace();
           return false;
       }
       catch(IOException IOE){
           IOE.printStackTrace();
           return false;
       }
       catch(Exception exc){
            exc.printStackTrace();
            return false;
       }
    }
    
    public boolean sendMessage(String message)
    {
        if(socket != null && socket.isConnected())
        {
            try
            {
                printWriter.println(message);
                printWriter.flush();
                return true;
            }
            catch(Exception exc)
            {
                exc.printStackTrace();
                return false;
            }
        }
        else
        {
            return false;
        }
            
    }
    
    public boolean disconnect()
    {
        if(socket == null)
            return true;
        try
        {
            readerThread.isWorking = false;
            bufferedReader.close();
            printWriter.close();
            socket.close();
            socket = null;
            readerThread = null;
            return true;
        }
        catch(Exception exc)
        {
            exc.printStackTrace();
            return false;
        }
    }
    
    public interface ClientInterface
    {
        public void onHandleMessage(String message);
    }
    
}
