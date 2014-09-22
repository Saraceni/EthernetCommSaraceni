/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.saraceni.ethernetcomm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rafaelgontijo
 */
public class ProtocolGenerator {
    
     public static String IPADDRESS_PATTERN = 
        "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    
    
    public String findIP(String str)
    {
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find())
        {
            return matcher.group();
        }
        else
            return null;
    }
    
    public int findEndOfMatch(String str)
    {
       Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find())
        {
            return matcher.regionEnd();
        } 
        else return 0;
    }
    
}
