package com.herblinker.android.libraries.base.printer;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;


import java.io.Closeable;

public interface LabelPrinter extends Closeable {
    enum Manufacturer{
        TSC;
    }

    enum TSC{
        MX200;
    }
    
    enum Rotation{
        DEGREE_0(0),
        DEGREE_90(90),
        DEGREE_180(180),
        DEGREE_270(270);
        int degree;
        Rotation(int degree){
            this.degree=degree;
        }
    }
    enum Alignment{
        DEFAULT(0),
        LEFT(1),
        CENTER(2),
        RIGHT(3);
        int alignment;
        Alignment(int alignment){
            this.alignment=alignment;
        }
    }
    enum Connection{
        SERIAL,
        ETHERNET,
        USB;
    }

    public boolean connectBlutooth(String address);
    public void connectEthernet(String ip, int port, long timeoutMillis, PrinterCallback callback);
    public boolean connectUSB(UsbManager usbManager, UsbDevice usbDevice);
    public void print(LabelContent labelContent, PrinterCallback callback) throws PrinterNotSupportException;
    public boolean isSupport(LabelContent labelContent);
    public void close(PrinterCallback callback);
}
