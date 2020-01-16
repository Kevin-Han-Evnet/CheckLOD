package com.netmania.checklod.general.hash;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class Base64EncUtil
{
    
    public static byte[] encode(byte[] input)
    {
        return Base64.encode(input, Base64.DEFAULT);
    }
    
    public static String encode(byte[] input, String defaultCharset)
    {
        String output = null;
        try {
            defaultCharset = (null == defaultCharset || defaultCharset.isEmpty())?"UTF-8": defaultCharset;
            output = new String(Base64.encode(input, Base64.DEFAULT), defaultCharset);
        } catch (UnsupportedEncodingException e) {
            Log.e("", "", e);
            output = new String("");
        }
        return output;
    }
    
    public static byte[] encode(String input)
    {
        byte[] byteInput = null;
        byte[] output = null;
        try {
            byteInput = input.getBytes("UTF-8");
            output = Base64.encode(byteInput, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            output = new byte[0];
            Log.e("", "", e);
        }
        return output;
    }
    
    public static String encode(String input, String defaultCharset)
    {
        byte[] byteInput = null;
        String output = null;
        try {
            defaultCharset = (null == defaultCharset || defaultCharset.isEmpty())?"UTF-8": defaultCharset;
            byteInput = input.getBytes("UTF-8");
            output = new String(Base64.encode(byteInput, Base64.DEFAULT), defaultCharset);
        } catch (UnsupportedEncodingException e) {
            output = new String("");
            Log.e("", "", e);
        }
        return output;
    }
    
    public static String decode (String content) {
    	return new String(Base64.decode(content, 0));
    }
}
