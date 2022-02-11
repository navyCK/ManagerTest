package com.herblinker.android.libraries.base.device;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.herblinker.android.libraries.base.exception.NFCInvalidDetectingException;
import com.herblinker.android.libraries.base.exception.NFCNotSupportException;
import com.herblinker.android.libraries.base.exception.NFCWrongDataException;
import com.herblinker.libraries.base.data.BytesEncoding;
import com.herblinker.libraries.base.data.DataTransfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AndroidNFCManager implements NFCManager{
    //RESULT_CODE 랑 FLAG 왜 0을 넣었는지 이유 체크 필요
    private static final int RESULT_CODE = 0;
    private static final int FLAG = 0;

    private volatile boolean isDetecting;
    private Activity activity;
    private NFCJobs nfcJobs;

    private BroadcastReceiver broadcastReceiver;

    private PendingIntent tagDetectIntent;
    private IntentFilter[] tagIntentFilter;

    private NfcAdapter nfcAdapter;
    private OnNFCReadListener onNFCReadListener;
    private OnNFCWriteListener onNFCWriteListener;

    private boolean isResume;
    private boolean isRestart;
    AndroidNFCManager(Activity activity, NFCJobs nfcJobs) throws NFCNotSupportException {
        this.activity=activity;
        this.nfcJobs=nfcJobs;
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if(nfcAdapter==null){
            NfcManager nfcManager = (NfcManager)activity.getSystemService(Context.NFC_SERVICE);
            if(nfcManager!=null)
                nfcAdapter=nfcManager.getDefaultAdapter();
        }
        if(nfcAdapter==null)
            throw new NFCNotSupportException();
        final AndroidNFCManager nfcManager = this;
        //NFC 상태 변경 감지 및 처리
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2){
            this.broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if(action!=null)
                        if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                            int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);
                            switch (state) {
                                case NfcAdapter.STATE_OFF:
                                    nfcManager.nfcJobs.nfcOff();
                                    break;
                                case NfcAdapter.STATE_TURNING_OFF:
                                    nfcManager.nfcJobs.nfcTurningOff();
                                    break;
                                case NfcAdapter.STATE_ON:
                                    nfcManager.nfcJobs.nfcOn();
                                    break;
                                case NfcAdapter.STATE_TURNING_ON:
                                    nfcManager.nfcJobs.nfcTurningOn();
                                    break;
                            }
                        }
                }
            };
            activity.registerReceiver(this.broadcastReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        }

        this.tagDetectIntent = PendingIntent.getActivity(activity, RESULT_CODE, new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), FLAG);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        this.tagIntentFilter = new IntentFilter[]{tagDetected};
    }

    @Override
    public NFCState getCurrentNFCState() {
        if(nfcAdapter==null)
            return NFCState.NOT_SUPPORT;
        if(!nfcAdapter.isEnabled())
            return  NFCState.TURNED_OFF;
        if(nfcAdapter.isNdefPushEnabled())
            return  NFCState.READ_WRITE_SUPPORT;
        return NFCState.READ_SUPPORT;
    }

    @Override
    public synchronized void detect(OnNFCReadListener onNFCReadListener, OnNFCWriteListener onNFCWriteListener) throws NFCInvalidDetectingException {
        if(isDetecting)
            throw new NFCInvalidDetectingException();
        this.onNFCReadListener=onNFCReadListener;
        this.onNFCWriteListener=onNFCWriteListener;
        this.isDetecting=true;
        detectResume();
    }

    private static class NfcData{
        byte[] uid;
        boolean readResult;
        boolean writeResult;
        MifareUltralight mifareUltralight;
    }
    @Override
    public void detectResume(){
        if(isResume)
            return;
        boolean isEnabled = false;
        try{
            isEnabled = nfcAdapter.isEnabled();
        } finally {
            if(isEnabled) {
                nfcAdapter.enableForegroundDispatch(activity, tagDetectIntent, tagIntentFilter, null);
                isResume=true;
            }
        }
        if(isRestart) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Bundle opts = new Bundle();
                int flags = NfcAdapter.FLAG_READER_NFC_A;
                nfcAdapter.enableReaderMode(activity, new NfcAdapter.ReaderCallback() {
                    @Override
                    public synchronized void onTagDiscovered(Tag tag) {
                        Log.e("CHECK-NFC", "onTagDiscovered:tag" + tag);
                    }
                }, flags, opts);
                nfcAdapter.disableReaderMode(activity);
            }
            isRestart=false;
        }
        Log.e("CHECK-NFC", "AndroidNFCManager:detectResume();");
    }

    @Override
    public void detectPause(){
        if(isResume) {
            nfcAdapter.disableForegroundDispatch(activity);
            isResume=false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                nfcAdapter.disableReaderMode(activity);
            }
        }
        Log.e("CHECK-NFC", "AndroidNFCManager:detectPause();nfcAdapter.disableForegroundDispatch(activity);");
    }

    @Override
    public boolean isDetecting() {
        return isDetecting;
    }

    @Override
    public void execute() {
        byte[] uid = null;
        boolean readResult = false;
        boolean writeResult = false;
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        Tag tag = null;
        MifareUltralight mifareUltralight = null;
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag==null) {
                //TODO throw new NotFoundException같은거
                return;
            }
            try {
                mifareUltralight = MifareUltralight.get(tag);
                if(mifareUltralight==null)
                    return;
                mifareUltralight.connect();
            } catch (Exception e){
                e.printStackTrace();
                return;
            }
        } else
            return;

        try {
            uid = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            if(uid==null)
                return;
            /**TODO 추후 HCE 수정해야함
             IsoDep isoDep = IsoDep.get(tag);
             if(isoDep!=null){
             try {
             isoDep.connect();
             Log.i("CHECK", "Requesting remote AID: F20AC48B12");
             byte[] data = sum(HCE_REQUEST_HEADER, AID_CHECK);
             Log.e("CHECK", "data: "+BytesEncoding.HEXA.encode(data));
             byte[] result = isoDep.transceive(data);
             Log.e("CHECK", "result: "+BytesEncoding.HEXA.encode(result));
             Log.i("CHECK", "rrr: "+(DataTransfer.bytesToIntegerValue(result)==RESULT_CODE_SUCESS));
             }catch (Exception e){
             Log.i("CHECK", "IOException");
             e.printStackTrace();
             }
             }
             */
            if(isDetecting){
                try{
                    if(mifareUltralight.isConnected()){
                        if(onNFCReadListener==null) {
                            readResult=true;
                        } else {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            for(int i=4; i<40;i+=4)
                                bos.write(mifareUltralight.readPages(i));
                            byte[] data = bos.toByteArray();
                            bos.close();
                            boolean notRead=true;
                            if(data!=null)
                                if(data.length==NFCWrongDataException.READ_LENGTH) {
                                    int size = data[1] - 7;
                                    if (size > 0){
                                        byte[] bytes = new byte[size];
                                        System.arraycopy(data, 9, bytes, 0, size);
                                        try {
                                            ArrayList<byte[]> processedData = OnNFCReadListener.deserialize(bytes);
                                            onNFCReadListener.onNFCReadListener(uid, processedData);
                                            notRead=false;
                                            readResult=true;
                                        }catch (NFCWrongDataException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            if(notRead){
                                onNFCReadListener.onNFCReadListener(uid, null);
                                readResult=true;
                            }
                        }
                        if(onNFCWriteListener==null) {
                            writeResult = true;
                        } else {
                            try {
                                byte[] writeData = OnNFCWriteListener.serialize(onNFCWriteListener.getData());
                                writeResult = false;
                                if(writeData.length<=NFCWrongDataException.MAX_LENGTH){
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
                                    byte[] buffer = new byte[4];
                                    int pos = 0;
                                    for(int offset = 4; offset<40;++offset, pos+=4) {
                                        System.arraycopy(bytes, pos, buffer,0, 4);
                                        mifareUltralight.writePage(offset, buffer);
                                    }
                                    writeResult = true;
                                }
                            } catch (NFCWrongDataException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                } finally {
                    try {
                        mifareUltralight.close();
                        mifareUltralight = null;
                    } catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        //Log.e("CHECK", "readResult: "+readResult);
                        //Log.e("CHECK", "writeResult: "+writeResult);
                        if(readResult){
                            if(writeResult){
                                if(onNFCWriteListener!=null)
                                    onNFCWriteListener.onNFCWriteListener();
                                isDetecting=false;
                            } else {
                                if(onNFCWriteListener!=null)
                                    onNFCWriteListener.onFail();
                            }
                        } else {
                            if(writeResult){
                                if(onNFCReadListener!=null)
                                    onNFCReadListener.onFail();
                                if(onNFCWriteListener!=null)
                                    onNFCWriteListener.onNFCWriteListener();
                            } else {
                                if(onNFCReadListener!=null)
                                    onNFCReadListener.onFail();
                                if(onNFCWriteListener!=null)
                                    onNFCWriteListener.onFail();
                            }
                        }
                    }
                }
            } else {
                ArrayList<byte[]> processedData = null;
                try {
                    if (mifareUltralight.isConnected()) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        for (int i = 4; i < 40; i += 4)
                            bos.write(mifareUltralight.readPages(i));
                        byte[] data = bos.toByteArray();
                        bos.close();
                        if (data != null)
                            if (data.length == NFCWrongDataException.READ_LENGTH) {
                                int size = data[1] - 7;
                                if (size > 0) {
                                    byte[] bytes = new byte[size];
                                    System.arraycopy(data, 9, bytes, 0, size);
                                    try {
                                        processedData = OnNFCReadListener.deserialize(bytes);
                                    } catch (NFCWrongDataException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        mifareUltralight.close();
                        mifareUltralight = null;
                    } catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        nfcJobs.nfcStanby(uid, processedData);
                    }
                }
            }
        } finally {
            try{if(mifareUltralight!=null)mifareUltralight.close();} catch(Exception e){e.printStackTrace();}
        }
    }

    @Override
    public void onRestart() {
        isRestart = true;
    }

    @Override
    public void cancel() {
        this.isDetecting=false;
    }

    @Override
    public void close(){
        activity.unregisterReceiver(this.broadcastReceiver);
    }
    public static byte[] sum(byte[] a, byte[] b) {
        if(a==null) {
            if(b==null)
                return null;
            return b;
        }
        if(b==null)
            return a;
        byte[] bytes = new byte[a.length + b.length];
        System.arraycopy(a, 0, bytes, 0, a.length);
        System.arraycopy(b, 0, bytes, a.length, b.length);
        return bytes;
    }
}
