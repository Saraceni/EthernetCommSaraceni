/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.saraceni.ethernetcomm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author Saraceni
 * 
 */
public class EthernetServer implements ServerCallbacks {
    
    private int port;
    private ServerSocket serverSocket;
    private HashMap<String, PrintWriter> clientsMap;
    private ProtocolGenerator protocol;
    
    public static void main(String[] args)
    {
        EthernetServer ether = new EthernetServer(5050);
        ether.conectar();
        while(true)
        {
        }
    }
    
    public EthernetServer(int port)
    {
        this.port = port;
        protocol = new ProtocolGenerator();
        clientsMap = new HashMap<String, PrintWriter>();
    }
    
    public boolean conectar()
    {
        System.out.println("Vai tentar conectar Server");
        if(serverSocket != null && !serverSocket.isBound())
        {
            System.out.println("O Server já está conectado!");
            return false;
        }
        try
        {
            serverSocket = new ServerSocket(port);
            new ConnectionThread().start();
            System.out.println("Server Conectou!");
            return true;
        }
        catch(Exception exc)
        {
            System.out.println("Erro ao tentar conectar Server!");
            System.out.println(exc.toString());
            serverSocket = null;
            return false;
        }
    }
    
    public boolean desconectar()
    {
        if(serverSocket == null)
            return true;
        try
        {
            disconnectClients();
            serverSocket.close();
            return true;
        }
        catch(Exception exc)
        {
            System.out.println("Erro ao fechar serverSocket");
            System.out.println(exc.toString());
            return false;
        }
        
    }
    
    public String[] getClientsIPs()
    {
        synchronized(clientsMap)
        {
            return clientsMap.keySet().toArray(new String[clientsMap.size()]);
        }
    }
    
    private void sendMessage(String clientIP, String message)
    {
        PrintWriter writer;
        synchronized(clientsMap)
        {
            writer = clientsMap.get(clientIP);
        }
        if(writer != null)
        {
            System.out.println("Obteve o Writer do IP " + clientIP);
            writer.println(message);
            writer.flush();
        }
    }
    
    private void disconnectClients()
    {
        synchronized(clientsMap)
        {
            try
            {
                PrintWriter[] printWriters;
                printWriters = clientsMap.values().toArray(new PrintWriter[clientsMap.size()]);
                for(PrintWriter writer : printWriters)
                {
                    clientsMap.remove(writer);
                    writer.close();
                }
             }
             catch(Exception exc)
             {
                System.out.println("Exception in disconnectClients");
                System.out.println(exc.toString());
             }
        }
    }

    @Override
    public void handleMessage(String message) { 
        synchronized(this)
        {
            System.out.println(message);
        }
    }
    
    @Override
    public void handleMessage(String clientIP, String message)
    {
        synchronized(this)
        {
            System.out.println(clientIP + ": " + message);
            String sendIP = protocol.findIP(message);
            if(sendIP != null)
            {
                System.out.println("Encontrou IP para enviar msg: " + sendIP);
                int endOfIP = protocol.findEndOfMatch(clientIP);
                message = message.substring(endOfIP);
                sendMessage(sendIP, message);
            }
        }
    }
    
    @Override
    public void addClient(String clientIP, PrintWriter printWriter)
    {
        synchronized(clientsMap)
        {
            clientsMap.put(clientIP, printWriter);
            System.out.println(clientIP + " conectou");
        }
    }
    
    @Override
    public void removeClient(String clientIP)
    {
        synchronized(clientsMap)
        {
            clientsMap.remove(clientIP);
            System.out.println("Removed " + clientIP);
        }
    }
    
    private class ConnectionThread extends Thread
    {
        Socket clientSocket;
        
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    clientSocket = serverSocket.accept();
                    new ClientHandlerThread(clientSocket, EthernetServer.this).start();
                    
                }
                catch(Exception exc)
                {
                    System.out.println("Exception na ConnectionThread");
                    System.out.println(exc.toString());
                }
            }
        }
    }
    
    private class ClientHandlerThread extends Thread
    {
        private ServerCallbacks serverCallbacks;
        private BufferedReader bufferedReader;
        private Socket clientSocket;
        private String clientIP;
        public volatile boolean isWorking = true;
        
        public ClientHandlerThread(Socket clientSocket, ServerCallbacks serverCallbacks)
        {
            this.serverCallbacks = serverCallbacks;
            this.clientSocket = clientSocket;
            this.clientIP = protocol.findIP(clientSocket.getInetAddress().toString());
            if(clientIP == null)
                clientIP = clientSocket.getInetAddress().toString();
            try
            {
                InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
                this.bufferedReader = new BufferedReader(isr);
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                serverCallbacks.addClient(clientIP, writer);
            }
            catch(Exception exc)
            {
                System.out.println("Exception no construtor da ClientHandlerThread");
                System.out.println(exc.toString());
            }
        }
        
        public void run()
        {
            String message;
            System.out.println(clientIP + "conectou.");
            while(isWorking)
            {
                try
                {
                    while((message = bufferedReader.readLine()) != null)
                    {
                        System.out.println("ClientThread " + clientIP + " vai enviar msg");
                        serverCallbacks.handleMessage(clientIP, message);
                    }
                    clientSocket.close();
                }
                catch(Exception exc)
                {
                    System.out.println("Exception na ClientThread " + clientIP);
                    System.out.println(exc.toString());
                    try
                    {
                        clientSocket.close();
                    }
                    catch(Exception IOE)
                    {
                        System.out.println("Exception ao fechar clientSocket");
                        System.out.println(IOE.toString());
                    }
                }
                finally
                {
                    serverCallbacks.removeClient(clientIP);
                    System.out.println(clientIP + " desconectou");
                    isWorking = false;
                    break;
                }
            }
        }
    }
}
