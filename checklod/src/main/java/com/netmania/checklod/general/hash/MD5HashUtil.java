package com.netmania.checklod.general.hash;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5HashUtil
{
    public static String hash(String input)
    {
        MessageDigest md= null;
        byte[] digested = null;
        String result = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes("utf-8"));
            digested = md.digest();
            result = byteToHex(digested);
        } catch (UnsupportedEncodingException e) {
            

        } catch (NoSuchAlgorithmException e) {
            
        }
        return result;
    }
    
    public static String hash(byte[] input)
    {
        MessageDigest md= null;
        byte[] digested = null;
        String result = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(input);
            digested = md.digest();
            result = byteToHex(digested);
        } catch (NoSuchAlgorithmException e) {
            
        }
        return result;
    }
    
    public static String byteToHex(byte[] input)
    {
        StringBuffer sb = new StringBuffer();
        
        if( null == input || 0 == input.length)
            return new String("");
        
        for( int i = 0 ; i < input.length; i++)
            sb.append(Integer.toString((input[i] & 0xff) + 0x100, 16).substring(1));
        
        return sb.toString(); 
    }
}
