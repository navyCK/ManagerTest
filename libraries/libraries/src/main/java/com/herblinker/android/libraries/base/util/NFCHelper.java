package com.herblinker.android.libraries.base.util;

public class NFCHelper {
    public static byte[] cutoffFirst(byte[] source, byte[] start){
        if(source==null)
            return null;
        if(start==null)
            return null;
        int headIndex = 0;
        boolean find = false;
        for(int i=0; i<source.length - start.length; i++) {
            find=true;
            for (int j = 0; j < start.length; ++j) {
                if (source[i + j] != start[j]) {
                    find = false;
                    break;
                }
            }
            if(find){
                headIndex = i+start.length;
                break;
            }
        }
        byte[] result = null;
        if(find){
            result = new byte[source.length - headIndex];
            System.arraycopy(source, headIndex, result, 0, result.length);
        }
        return result;
    }
    public static byte[] cutoffLast(byte[] source, byte[] start){
        if(source==null)
            return null;
        if(start==null)
            return null;
        int headIndex = -1;
        boolean find = false;
        for(int i=0; i<source.length - start.length; i++) {
            find=true;
            for (int j = 0; j < start.length; ++j) {
                if (source[i + j] != start[j]) {
                    find = false;
                    break;
                }
            }
            if(find){
                headIndex = i+start.length;
            }
        }
        byte[] result = null;
        if(headIndex>=0){
            result = new byte[source.length - headIndex];
            System.arraycopy(source, headIndex, result, 0, result.length);
        }
        return result;
    }
}
