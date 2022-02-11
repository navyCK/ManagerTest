package com.herblinker.android.libraries.base.printer;

public class PrinterResponse {
    public enum Result{
        SUCCESS,
        FAIL,
        CONNECTION_PROBLEM,
        SERVICE_END;
    }
    public Result result;

    PrinterResponse() {
        this.result=Result.SUCCESS;
    }

    PrinterResponse(Result resultCode) {
        this.result = result;
    }
}