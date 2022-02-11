package com.herblinker.android.libraries.base.printer;

class PrinterRequest {
    enum Job{
        CONNECT,
        SET_LANGUAGE,
        SET_SIZE,
        SET_SPEED,
        SET_DENSITY,
        SET_SENSOR,
        ADD_TEXT,
        ADD_BARCODE_1D,
        ADD_BARCODE_2D,
        PRINT,
        DISCONNECT;
    }
    Job job;
    public PrinterRequest(int ints, int strings, int booleans){
        if(ints>0)
            this.ints = new int[ints];
        if(strings>0)
            this.strings = new String[strings];
        if(booleans>0)
            this.booleans = new boolean[booleans];
    }
    LabelPrinter.Connection connection;
    int[] ints;
    String[] strings;
    boolean[] booleans;
}
