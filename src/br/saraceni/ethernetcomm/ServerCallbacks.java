/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.saraceni.ethernetcomm;

import java.io.PrintWriter;

/**
 *
 * @author Saraceni
 */
public interface ServerCallbacks {
    
    public void handleMessage(String clientIP, String message);
    public void handleMessage(String message);
    public void addClient(String clientIP, PrintWriter clientWriter);
    public void removeClient(String clientIP);
}
