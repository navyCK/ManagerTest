package com.herblinker.android.libraries.base.device.obm_a8;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;

import com.herblinker.android.libraries.base.exception.NFCInvalidDetectingException;
import com.herblinker.android.libraries.base.device.NFCManager;
import com.herblinker.android.libraries.base.exception.NFCNotSupportException;
import com.herblinker.android.libraries.base.exception.NFCWrongDataException;
import com.herblinker.android.libraries.base.device.OnNFCReadListener;
import com.herblinker.android.libraries.base.device.OnNFCWriteListener;
import com.herblinker.android.libraries.base.util.NFCHelper;
import com.herblinker.libraries.base.data.BytesEncoding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class ObmA8NFCManager extends Thread implements NFCManager {
    private static final String TAG = "ObmA8NFCManager";

    private static final String DEVICE_PATH = "/proc/devicepower/rfidpower";
    private static final char DEVICE_STATE_ON = '1';
    private static final char DEVICE_STATE_OFF = '2';

    private static final String DEVICE_SERIAL_PORT_PATH = "/dev/ttyMT1";
    private static final int DEVICE_SERIAL_PORT_BAUD_RATE = 115200;
    private static final int DEVICE_SERIAL_PORT_FLAG = 0;


    // Command 정리
    // 55 55 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 03 FD D4 14 01 17 00
    private static final byte[] COMMAND_GET_FIRMWARE = BytesEncoding.HEXA.decode("55550000000000000000000000000000FF03FDD414011700");
    // 00 00 FF 04 FC D4 4A 01 00 E1 00
    private static final byte[] COMMAND_GET_UID = BytesEncoding.HEXA.decode("0000FF04FCD44A0100E100");
    // 00 00 FF 05 FB D4 40 01 30 04 B7 00
    private static final byte[] COMMAND_READ_DATA = BytesEncoding.HEXA.decode("0000FF05FBD440013004B700");
    //0000FF15EBD44001A00400112233445566778899AABBCCDDEEFF4F00
    private static final byte[] COMMAND_WRITE_DATA = BytesEncoding.HEXA.decode("0000FF15EBD44001A000000000000000000000000000000000000000");
    //RESPONSE 정리
    // 00 00 FF 00 FF 00 00 00 FF 02 FE D5 15 16 00
    private static final byte[] RESPONSE_COMMAND_GET_FIRMWARE_ACTIVE = BytesEncoding.HEXA.decode("02FED5151600");
    // 00 FF 00 FF 00 00 00 FF
    private static final byte[] RESPONSE_HEADER_GET_FIRMWARE = BytesEncoding.HEXA.decode("0000FF00FF000000FF");
    private static final byte[] RESPONSE_HEADER_GET_UID = BytesEncoding.HEXA.decode("0FF1D54B0101004400");
    private static final byte[] RESPONSE_HEADER_GET_READ = BytesEncoding.HEXA.decode("0000FF00FF000000FF13EDD54100");
    private static final byte[] RESPONSE_HEADER_GET_WRITE = BytesEncoding.HEXA.decode("0000FF00FF000000FF03FDD54100EA00");

    private static final long REQUEST_TRIAL_INTERVAL_MILLIS = 10;
    private static final int REQUEST_MIN_LENGTH = 9;
    private static final int REQUEST_MAX_TRIAL = 20;

    private static final int REQUEST_WORK = 1;
    private static final int REQUEST_CHECK = 2;
    private static final int REQUEST_GET_UID = 3;
    private static final int REQUEST_READ_WRITE = 4;
    private static final int REQUEST_PAUSE = 5;
    private static final int REQUEST_CLOSE = 6;

    private InputStream is;
    private OutputStream os;

    private volatile boolean isDetecting;
    private OnNFCReadListener onNFCReadListener;
    private OnNFCWriteListener onNFCWriteListener;
    private Lock getter;
    private Condition waitRequest;
    private Condition waitResponse;

    private volatile boolean isClosed;
    private volatile int request = REQUEST_PAUSE;
    private volatile ObmA8NFCResultObject nfcResult;
    private Vibrator vibrator;
    private static class DetectTask extends AsyncTask<Void, Void, ObmA8NFCResultObject>{
        private ObmA8NFCManager manager;
        private DetectTask(ObmA8NFCManager manager){
            this.manager=manager;
        }

        @Override
        protected ObmA8NFCResultObject doInBackground(Void... voids) {
            Log.e(TAG, "doInBackground");
            manager.getter.lock();
            try{
                manager.request=REQUEST_WORK;
                manager.waitRequest.signal();
                manager.waitResponse.awaitUninterruptibly();
                return manager.nfcResult;
            } finally {
                manager.getter.unlock();
            }
        }

        @Override
        protected void onPostExecute(ObmA8NFCResultObject result) {
            Log.e(TAG, "onPostExecute");
            if(result==null)
                result = new ObmA8NFCResultObject(false);
            Log.e(TAG, "NFC_RESULT tagResult: " + result.tagResult);
            Log.e(TAG, "NFC_RESULT readResult: " + result.readResult);
            Log.e(TAG, "NFC_RESULT writeResult: " + result.writeResult);
            manager.vibrator.vibrate(300);
            if(result.uid==null)
                Log.e(TAG, "REQUEST_READ uid: null");
            else
                Log.e(TAG, "REQUEST_READ uid: " + BytesEncoding.HEXA.encode(result.uid));
            if(result.data==null)
                Log.e(TAG, "REQUEST_READ data: null");
            else
                Log.e(TAG, "REQUEST_READ data: " + BytesEncoding.HEXA.encode(result.data));
            if(result.tagResult){
                manager.getter.lock();
                try{
                    if(manager.onNFCReadListener!=null){
                        if(result.readResult){
                            ArrayList<byte[]> processedData = null;
                            if(result.data!=null)
                                try {
                                    processedData = OnNFCReadListener.deserialize(result.data);
                                } catch (NFCWrongDataException e){
                                    e.printStackTrace();
                                }
                            manager.onNFCReadListener.onNFCReadListener(result.uid, processedData);
                        } else {
                            manager.onNFCReadListener.onFail();
                        }
                    }
                    if(manager.onNFCWriteListener!=null){
                        if(result.writeResult){
                            manager.onNFCWriteListener.onNFCWriteListener();
                        } else {
                            manager.onNFCWriteListener.onFail();
                        }
                    }
                } finally {
                    manager.getter.unlock();
                }
            } else {
                if(manager.onNFCReadListener!=null)
                    manager.onNFCReadListener.onFail();
                if(manager.onNFCWriteListener!=null)
                    manager.onNFCWriteListener.onFail();
            }
        }
    }
    public ObmA8NFCManager(Activity activity) throws NFCNotSupportException {
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        String devices[] = serialPortFinder.getAllDevices();
        if(devices==null)
            throw new NFCNotSupportException();
        if(devices.length==0)
            throw new NFCNotSupportException();
        for(String device : devices)
            Log.e(TAG, "/dev/" + device.split(" ")[0]);

        try{
            SerialPort serialPort = new SerialPort(new File(DEVICE_SERIAL_PORT_PATH), DEVICE_SERIAL_PORT_BAUD_RATE, DEVICE_SERIAL_PORT_FLAG);
            is = serialPort.getInputStream();
            os = serialPort.getOutputStream();
        } catch (IOException e){
            e.printStackTrace();
            throw new NFCNotSupportException();
        }
        this.getter=new ReentrantLock(true);
        this.waitRequest=this.getter.newCondition();
        this.waitResponse=this.getter.newCondition();
        vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);

        this.start();
    }

    private byte calcDCS(byte[] data) {
        byte crc = 0;
        for (byte datum:data) {
            crc += datum;
        }
        //Make Not Operator(?)
        crc = (byte) (0x100 - crc);
        return crc;
    }

    @Override
    public NFCState getCurrentNFCState() {
        return NFCState.READ_WRITE_SUPPORT;
    }

    @Override
    public synchronized void detect(OnNFCReadListener onNFCReadListener, OnNFCWriteListener onNFCWriteListener) throws NFCInvalidDetectingException {
        Log.e(TAG, "detect: "+isDetecting);
        if(isDetecting)
            throw new NFCInvalidDetectingException();
        getter.lock();
        try{
            this.onNFCReadListener=onNFCReadListener;
            this.onNFCWriteListener=onNFCWriteListener;
            this.isDetecting=true;
        } finally {
            getter.unlock();
        }
        new DetectTask(this).execute();
    }

    @Override
    public void detectResume() {
        //리스너 방식이 아니라 리스닝 재개할 필요가 없음
    }

    @Override
    public void detectPause() {
        //리스너 방식이 아니라 리스닝 중단할 필요가 없음
    }

    @Override
    public boolean isDetecting() {
        return isDetecting;
    }

    @Override
    public void execute() {
        //Thread polling 유도 방식이라 필요없음
    }

    @Override
    public void cancel() {
        Log.e(TAG, "cancel");
        if(isDetecting){
            getter.lock();
            try{
                this.request=REQUEST_PAUSE;
                this.waitRequest.signal();
            } finally {
                getter.unlock();
            }
        }
    }

    @Override
    public void onRestart() {

    }

    @Override
    public void close() {
        Log.e(TAG, "close");
        if(isDetecting){
            getter.lock();
            try{
                this.request=REQUEST_CLOSE;
                this.waitRequest.signal();
            } finally {
                getter.unlock();
            }
        }
    }

    @Override
    public void run() {
        byte[] uid = null;
        byte[] data = null;
        while(!isClosed){
            getter.lock();
            try{
                if(request==REQUEST_WORK){
                    Log.e(TAG, "REQUEST_WORK");
                    setPowerState(true);
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    requestNFC(COMMAND_GET_FIRMWARE);
                    request=REQUEST_CHECK;
                } else if (request==REQUEST_CHECK){
                    Log.e(TAG, "REQUEST_CHECK");
                    byte[] response = responseNFC();
                    if(response!=null) {
                        response = NFCHelper.cutoffLast(response, RESPONSE_HEADER_GET_FIRMWARE);
                        if(response==null){
                            Log.e(TAG, "REQUEST_CHECK : response==null");
                            nfcResult = new ObmA8NFCResultObject(false, false, false);
                            request = REQUEST_PAUSE;
                        } else {
                            if(Arrays.equals(response, RESPONSE_COMMAND_GET_FIRMWARE_ACTIVE)){
                                requestNFC(COMMAND_GET_UID);
                                request=REQUEST_GET_UID;
                            } else {
                                Log.e(TAG, "REQUEST_CHECK : response==fail");
                                nfcResult = new ObmA8NFCResultObject(false, false, false);
                                request = REQUEST_PAUSE;
                            }
                        }
                    }
                } else if (request==REQUEST_GET_UID) {
                    Log.e(TAG, "REQUEST_GET_UID");
                    byte[] response = responseNFC();
                    if (response == null) {
                        Log.e(TAG, "REQUEST_GET_UID : response==null");
                    } else {
                        Log.e(TAG, "REQUEST_GET_UID Raw Response: "+BytesEncoding.HEXA.encode(response));
                        response = NFCHelper.cutoffFirst(response, RESPONSE_HEADER_GET_UID);
                        if(response==null){
                            nfcResult = new ObmA8NFCResultObject(false, false, false);
                            request = REQUEST_PAUSE;
                        } else {
                            Log.e(TAG, "REQUEST_GET_UID Response: " + BytesEncoding.HEXA.encode(response));
                            if(response.length==0) {
                                response = null;
                            } else if(response[0]==0){
                                response = null;
                            } else if(response.length<response[0]+1){
                                response = null;
                            } else {
                                uid = new byte[response[0]];
                                System.arraycopy(response, 1, uid, 0, response[0]);
                                response = uid;
                            }
                            if(response==null){
                                nfcResult = new ObmA8NFCResultObject(false, false, false);
                                request = REQUEST_PAUSE;
                            } else {
                                Log.e(TAG, "REQUEST_GET_UID Result: " + BytesEncoding.HEXA.encode(response));
                                request=REQUEST_READ_WRITE;
                            }
                        }
                    }
                } else if (request==REQUEST_READ_WRITE){
                    Log.e(TAG, "REQUEST_READ_WRITE");
                    boolean readResult = true;
                    boolean writeResult = true;
                    byte[] response;
                    if(onNFCReadListener!=null){
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[16];
                        try {
                            for (byte page = 4; page < 40; page+=4) {
                                requestNFC(getReadCommand(page));
                                response = responseNFC();
                                if (response != null)
                                    response = NFCHelper.cutoffFirst(response, RESPONSE_HEADER_GET_READ);
                                if(response != null)
                                    if (response.length != 18)
                                        response = null;
                                if (response == null) {
                                    readResult = false;
                                    break;
                                }
                                System.arraycopy(response, 0, buffer, 0, 16);
                                Log.e(TAG, "REQUEST_READ Page("+page+"~"+(page+3)+") Result: " + BytesEncoding.HEXA.encode(buffer));
                                baos.write(buffer);
                            }
                            if (readResult) {
                                readResult=false;
                                data = baos.toByteArray();
                                Log.e(TAG, "REQUEST_READ baos.toByteArray(): " + BytesEncoding.HEXA.encode(data));
                                if(data.length==NFCWrongDataException.READ_LENGTH) {
                                    readResult = true;
                                    int size = data[1] - 7;
                                    if (size > 0 && size<=data.length-9){
                                        byte[] bytes = new byte[size];
                                        System.arraycopy(data, 9, bytes, 0, size);
                                        data = bytes;
                                    } else {
                                        data=null;
                                    }
                                }
                                if(readResult) {
                                    if (data != null)
                                        Log.e(TAG, "REQUEST_READ Result: " + BytesEncoding.HEXA.encode(data));
                                    else
                                        Log.e(TAG, "REQUEST_READ Result: null");
                                } else
                                    Log.e(TAG, "REQUEST_READ Result: fail");
                            }
                            baos.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(onNFCWriteListener!=null){
                        try {
                            byte[] writeData = OnNFCWriteListener.serialize(onNFCWriteListener.getData());
                            writeResult = false;
                            Log.e("check data1: ", BytesEncoding.HEXA.encode(writeData));
                            if(writeData.length<=NFCWrongDataException.MAX_LENGTH){
                                writeResult = true;
                                byte[] bytes = new byte[144];
                                bytes[0]=(byte)0x03;
                                bytes[1]=(byte)(writeData.length+7);
                                bytes[2]=(byte)0xD1;
                                bytes[3]=(byte)0x01;
                                bytes[4]=(byte)(writeData.length+3);
                                bytes[5]=(byte)0x54;
                                bytes[6]=(byte)0x02;
                                bytes[7]=(byte)0x65;
                                bytes[8]=(byte)0x6E;
                                System.arraycopy(writeData, 0, bytes, 9, writeData.length);
                                bytes[writeData.length+10]=(byte)0xFE;
                                Log.e("check data2: ", BytesEncoding.HEXA.encode(bytes));
                                byte[] buffer = new byte[4];
                                int pos = 0;
                                for(byte offset = 4; offset<40;++offset, pos+=4) {
                                    System.arraycopy(bytes, pos, buffer,0, 4);
                                    requestNFC(getWriteCommand(offset, buffer));
                                    response = responseNFC();
                                    if(response!=null)
                                        Log.e(TAG, "REQUEST_WRITE Result: " + BytesEncoding.HEXA.encode(response));
                                    if(response!=null)
                                        if(!Arrays.equals(response, RESPONSE_HEADER_GET_WRITE))
                                            response=null;
                                    if(response==null){
                                        writeResult = false;
                                        break;
                                    }
                                }
                            }
                        } catch (NFCWrongDataException e){
                            e.printStackTrace();
                        }
                    }
                    nfcResult = new ObmA8NFCResultObject(true, readResult, writeResult, uid, data);
                    request=REQUEST_PAUSE;
                } else if(request==REQUEST_PAUSE){
                    Log.e(TAG, "REQUEST_PAUSE");
                    setPowerState(false);
                    if(isDetecting) {
                        isDetecting=false;
                        waitResponse.signal();
                    }
                    try{
                        waitRequest.await();
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                } else if(request==REQUEST_CLOSE){
                    Log.e(TAG, "REQUEST_CLOSE");
                    nfcResult = new ObmA8NFCResultObject(false);
                    if(isDetecting)
                        waitResponse.signal();
                    isClosed=true;
                } else {
                    Log.e(TAG, "REQUEST_ETC");
                }
            } finally {
                getter.unlock();
            }
        }
        Log.e(TAG, "NFC 종료");
        try {
            setPowerState(false);
            is.close();
            os.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private byte[] getReadCommand(byte page){
        byte[] command = new byte[COMMAND_READ_DATA.length];
        System.arraycopy(COMMAND_READ_DATA, 0, command, 0, COMMAND_READ_DATA.length);
        command[9]=page;
        command[10]=(byte)(0xBB-page);
        Log.e(TAG, "REQUEST_READ command: " + BytesEncoding.HEXA.encode(command));
        return command;
    }
    private byte[] getWriteCommand(byte page, byte[] data4size){
        byte[] command = new byte[COMMAND_WRITE_DATA.length];
        System.arraycopy(COMMAND_WRITE_DATA, 0, command, 0, COMMAND_WRITE_DATA.length);

        command[9]=page;
        int size = data4size.length;
        if(size>16)
            size=16;
        System.arraycopy(data4size, 0, command, 10, size);
        byte[] crc = new byte[21];
        System.arraycopy(command, 5, crc, 0, 21);
        command[26]=calcDCS(crc);
        return command;
    }

    private void requestNFC(byte[] command) {
        Log.e(TAG, "requestNFC");
        try {
            //os.write(COMMAND_STOP_ACK);
            //Thread.sleep(200);
            os.write(command);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private byte[] responseNFC(){
        Log.e(TAG, "responseNFC");
        int length = 0;
        int trial=0;
        try{
            while(trial++<REQUEST_MAX_TRIAL) {
                length = is.available();
                if(length<=REQUEST_MIN_LENGTH) {
                    length = 0;
                    if(trial<REQUEST_MAX_TRIAL)
                        try{
                            Thread.sleep(REQUEST_TRIAL_INTERVAL_MILLIS);
                        } catch (InterruptedException e){
                            e.printStackTrace();
                        }
                }
            }
            if(length==0) {
                return null;
            }
            byte[] response = new byte[length];
            int size;
            for(int offset=0; offset<length;offset+=size){
                size = is.read(response, offset, length-offset);
                if(size<0)
                    return null;
            }
            Log.e(TAG, "Raw Response: "+BytesEncoding.HEXA.encode(response));
            return response;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private void setPowerState(boolean onOff) {
        File file = new File(DEVICE_PATH);
        if (!file.exists()) {
            Log.e(TAG, "file /proc/atmlqtouch/sleep/proto does not exists");
            return;
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            if(onOff)
                writer.write(DEVICE_STATE_ON);
            else
                writer.write(DEVICE_STATE_OFF);
        } catch (IOException e) {
            Log.e(TAG, "error writing to setPowerState fs interface");
            e.printStackTrace();
        } finally {
            if(writer!=null)try{writer.close();}catch (Exception e){e.printStackTrace();}
        }
    }
}
