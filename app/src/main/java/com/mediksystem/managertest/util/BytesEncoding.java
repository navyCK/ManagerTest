package com.mediksystem.managertest.util;

import android.os.Build;

import java.util.Base64;

import androidx.annotation.RequiresApi;

public enum BytesEncoding {
    BASIC{
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public String encode(byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public byte[] decode(String string) throws IllegalArgumentException {
            return Base64.getDecoder().decode(string);
        }
    }, URL{
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public String encode(byte[] bytes) {
            return Base64.getUrlEncoder().encodeToString(bytes);
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public byte[] decode(String string) throws IllegalArgumentException {
            return Base64.getUrlDecoder().decode(string);
        }
    }, MIME{
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public String encode(byte[] bytes) {
            return Base64.getMimeEncoder().encodeToString(bytes);
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public byte[] decode(String string) throws IllegalArgumentException {
            return Base64.getMimeDecoder().decode(string);
        }
    }, HEXA{
        @Override
        public String encode(byte[] bytes) {
            StringBuilder stringBuilder = new StringBuilder();
            for(byte b :bytes)
                stringBuilder.append(Constants.BYTE_TO_HEXA_STRING_UPPER[b&0xFF]);
            return stringBuilder.toString();
        }
        @Override
        public byte[] decode(String string) throws IllegalArgumentException {
            char[] chars = string.toCharArray();
            byte[] bytes = new byte[chars.length/2];
            int value;
            char front, rear;
            for(int i=0;i<chars.length;) {
                front = chars[i++];
                rear = chars[i++];
                if(front<='9') {
                    if(front<'0')
                        throw new IllegalArgumentException(string);
                    value = front-'0';
                } else {
                    if(front>'F')
                        throw new IllegalArgumentException(string);
                    value = front-'A'+10;
                }
                value<<=4;
                if(rear<='9')
                    value += rear-'0';
                else
                    value += rear-'A'+10;
                bytes[i/2-1]=(byte)(value);
            }
            return bytes;
        }
    };

    public String encode(byte[] bytes) {
        return null;
    }
    public byte[] decode(String string) throws IllegalArgumentException {
        return null;
    }
}

