package com.herblinker.android.libraries.base.device;

import com.herblinker.android.libraries.base.exception.NFCWrongDataException;

import java.util.ArrayList;

public abstract class OnNFCWriteListener {
    private static final byte[] NONE = new byte[]{0};
    public static byte[] serialize(ArrayList<byte[]> messages) throws NFCWrongDataException {
        if(messages==null)
            return NONE;
        if(messages.size()==0)
            return NONE;
        long length = 0;
        byte[] message;
        for(int i=0;i<messages.size();++i){
            message = messages.get(i);
            if(message==null)
                throw new NFCWrongDataException();
            length++;
            length+=message.length;
        }
        byte[] result = new byte[(int)length];
        int offset=0;
        byte subLength;
        for(int i=0;i<messages.size();++i){
            message = messages.get(i);
            subLength=(byte)message.length;
            result[offset++]=subLength;
            System.arraycopy(message, 0, result, offset, subLength);
            offset+=subLength;
        }
        if(result.length>NFCWrongDataException.MAX_LENGTH)
            throw new NFCWrongDataException();
        return result;
    }
    public abstract void onNFCWriteListener();
    public abstract ArrayList<byte[]> getData();
    public abstract void onFail();
}
