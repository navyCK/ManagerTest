package com.herblinker.android.libraries.base.device;

import java.util.ArrayList;

public interface NFCJobs {
    public void nfcOff();
    public void nfcTurningOff();
    public void nfcOn();
    public void nfcTurningOn();
    public void nfcStanby(byte[] uid, ArrayList<byte[]> messages);
}
