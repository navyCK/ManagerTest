package com.herblinker.android.libraries.base.device.obm_a8;

public class ObmA8NFCResultObject {
    protected boolean tagResult;
    protected boolean readResult;
    protected boolean writeResult;
    protected byte[] uid;
    protected byte[] data;
    public ObmA8NFCResultObject(boolean tagResult) {
        this.tagResult=tagResult;
    }

    public ObmA8NFCResultObject(boolean tagResult, boolean readResult, boolean writeResult) {
        this.tagResult=tagResult;
        this.readResult=readResult;
        this.writeResult=writeResult;
    }
    public ObmA8NFCResultObject(boolean tagResult, boolean readResult, boolean writeResult, byte[] uid, byte[] data) {
        this.tagResult=tagResult;
        this.readResult=readResult;
        this.writeResult=writeResult;
        this.uid=uid;
        this.data=data;
    }
}
