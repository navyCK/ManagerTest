package com.mediksystem.managertest.util;

public enum ByteTransfer {
    BIG_ENDIAN(){
        public byte[] shortToBytes(short v) {
            byte[] bytes = new byte[Short.BYTES];
            bytes[1] = (byte)(v&0xFF);
            v>>=8;
            bytes[0] = (byte)(v&0xFF);
            return bytes;
        }

        public byte[] integerToBytes(int v) {
            byte[] bytes = new byte[Integer.BYTES];
            bytes[3] = (byte)(v&0xFF);
            v>>=8;
            bytes[2] = (byte)(v&0xFF);
            v>>=8;
            bytes[1] = (byte)(v&0xFF);
            v>>=8;
            bytes[0] = (byte)(v&0xFF);
            return bytes;
        }

        public byte[] longToBytes(long v) {
            byte[] bytes = new byte[Long.BYTES];
            bytes[7] = (byte)(v&0xFFL);
            v>>=8;
            bytes[6] = (byte)(v&0xFFL);
            v>>=8;
            bytes[5] = (byte)(v&0xFFL);
            v>>=8;
            bytes[4] = (byte)(v&0xFFL);
            v>>=8;
            bytes[3] = (byte)(v&0xFFL);
            v>>=8;
            bytes[2] = (byte)(v&0xFFL);
            v>>=8;
            bytes[1] = (byte)(v&0xFFL);
            v>>=8;
            bytes[0] = (byte)(v&0xFFL);
            return bytes;
        }

        public byte[] intArrayToByteArray(int[] array){
            if(array==null)
                return null;
            byte[] result = new byte[array.length*4];
            int v=0;
            int offset;
            for(int i=0;i<array.length;++i){
                v = array[i];
                offset = 4*i;
                result[offset+3] = (byte)(v&0xFF);
                v>>=8;
                result[offset+2] = (byte)(v&0xFF);
                v>>=8;
                result[offset+1] = (byte)(v&0xFF);
                v>>=8;
                result[offset] = (byte)(v&0xFF);
            }
            return result;
        }
    },
    LITTLE_ENDIAN(){
        public byte[] shortToBytes(short v) {
            byte[] bytes = new byte[Short.BYTES];
            bytes[0] = (byte)(v&0xFF);
            v>>=8;
            bytes[1] = (byte)(v&0xFF);
            return bytes;
        }

        public byte[] integerToBytes(int v) {
            byte[] bytes = new byte[Integer.BYTES];
            bytes[0] = (byte)(v&0xFF);
            v>>=8;
            bytes[1] = (byte)(v&0xFF);
            v>>=8;
            bytes[2] = (byte)(v&0xFF);
            v>>=8;
            bytes[3] = (byte)(v&0xFF);
            return bytes;
        }

        public byte[] longToBytes(long v) {
            byte[] bytes = new byte[Long.BYTES];
            bytes[0] = (byte)(v&0xFFL);
            v>>=8;
            bytes[1] = (byte)(v&0xFFL);
            v>>=8;
            bytes[2] = (byte)(v&0xFFL);
            v>>=8;
            bytes[3] = (byte)(v&0xFFL);
            v>>=8;
            bytes[4] = (byte)(v&0xFFL);
            v>>=8;
            bytes[5] = (byte)(v&0xFFL);
            v>>=8;
            bytes[6] = (byte)(v&0xFFL);
            v>>=8;
            bytes[7] = (byte)(v&0xFFL);
            return bytes;
        }

        public byte[] intArrayToByteArray(int[] array){
            if(array==null)
                return null;
            byte[] result = new byte[array.length*4];
            int v=0;
            int offset;
            for(int i=0;i<array.length;++i){
                v = array[i];
                offset = 4*i;
                result[offset] = (byte)(v&0xFF);
                v>>=8;
                result[offset+1] = (byte)(v&0xFF);
                v>>=8;
                result[offset+2] = (byte)(v&0xFF);
                v>>=8;
                result[offset+3] = (byte)(v&0xFF);
            }
            return result;
        }
    };
    public abstract byte[] shortToBytes(short v);
    public abstract byte[] integerToBytes(int v);
    public abstract byte[] longToBytes(long v);

    public byte[] floatToBytes(float v) {
        int floatValue =  Float.floatToIntBits(v);
        return integerToBytes(floatValue);
    }

    public byte[] doubleToBytes(double v) {
        long doubleValue =  Double.doubleToLongBits(v);
        return longToBytes(doubleValue);
    }
    public abstract byte[] intArrayToByteArray(int[] array);
}

