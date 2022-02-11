package com.herblinker.android.libraries.base.device;

import com.herblinker.android.libraries.base.exception.NFCWrongDataException;

import java.util.ArrayList;

public abstract class OnNFCReadListener {
    public static ArrayList<byte[]> deserialize(byte[] data) throws NFCWrongDataException {
        ArrayList<byte[]> result = new ArrayList<>();
        if(data==null)
            return result;
        byte[] message;
        int length=0;
        int subLength;
        for(int i=0;i<data.length;){
            subLength = data[i++];
            message = new byte[subLength];
            System.arraycopy(data, i, message, 0, subLength);
            i+=subLength;
            length+=1+subLength;
            result.add(message);
        }
        if(length>NFCWrongDataException.MAX_LENGTH)
            throw new NFCWrongDataException();
        return result;
    }
    public abstract void onNFCReadListener(byte[] uid, ArrayList<byte[]> messages);
    public abstract void onFail();
}
