package com.herblinker.android.libraries.base.printer;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;


import com.herblinker.libraries.base.concurrent.RequestCallbackManager;
import com.herblinker.libraries.base.data.Word;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;

public class PrinterTSCMX200 extends RequestCallbackManager<PrinterRequest, PrinterResponse> implements LabelPrinter {
    private static int getLineHeight(int fontSize){
        return (int)(fontSize*2.8);
    }
    private static int getNextLineHeight(int fontSize){
        return (int)(fontSize*3.2);
    }
    private static class LabelWriter{
        private PrinterTSCMX200 printer;
        private int widthMm;
        private int heightMm;
        private int width;
        private int height;
        private int paddingLeft;
        private int paddingRight;
        private int paddingTop;
        private int paddingBottom;

        private int x;
        private int y;
        private LabelWriter(PrinterTSCMX200 printer, int widthMm, int heightMm, int horizontalPadding, int verticalPadding){
            this.printer=printer;
            this.widthMm=widthMm;
            this.heightMm=heightMm;
            this.width=widthMm*8;
            this.height=widthMm*8;
            this.paddingLeft=horizontalPadding;
            this.paddingRight=horizontalPadding;
            this.paddingTop=verticalPadding;
            this.paddingBottom=verticalPadding;
            this.x=paddingLeft;
            this.y=paddingTop;
            printer.setLanguage("UTF-8", null);
            printer.setSize(widthMm, heightMm, null);
            printer.setSpeed(4, null);
            printer.setDensity(4, null);
            printer.setSensor(true, 3, 0, null);
        }

        private void move(int x, int y){
            this.x=x;
            if(this.x<paddingLeft)
                this.x=paddingLeft;
            if(this.x>=width-paddingRight)
                this.x=width-paddingRight;
            this.y=y;
            if(this.y<paddingTop)
                this.y=paddingTop;
            if(this.y>=height-paddingBottom)
                this.y=height-paddingBottom;
        }
        private void moveX(int x){
            this.x=x;
            if(this.x<paddingLeft)
                this.x=paddingLeft;
            if(this.x>=width-paddingRight)
                this.x=width-paddingRight;
        }
        private void moveY(int y){
            this.y=y;
            if(this.y<paddingTop)
                this.y=paddingTop;
            if(this.y>=height-paddingBottom)
                this.y=height-paddingBottom;
        }
        private void moveXAdd(int x){
            this.x+=x;
            if(this.x<paddingLeft)
                this.x=paddingLeft;
            if(this.x>=width-paddingRight)
                this.x=width-paddingRight;
        }
        private void moveYAdd(int y){
            this.y+=y;
            if(this.y<paddingTop)
                this.y=paddingTop;
            if(this.y>=height-paddingBottom)
                this.y=height-paddingBottom;
        }
        private void nextLine(double heightRatio){
            moveYAdd((int)(this.height*heightRatio));
            moveX(0);
        }
        private void nextLine(int fontSize){
            moveYAdd(getNextLineHeight(fontSize));
            moveX(0);
        }
        private void writeBarcodeCenter(String barcode, double widthRatio, double heightRatio){
            int width = (int)((this.width-this.paddingLeft-this.paddingRight)*widthRatio);
            int height = (int)(this.height*heightRatio);
            writeBarcodeCenter(barcode, width, height);
        }
        private void writeBarcodeCenter(String barcode, int width, int height){
            Log.e("CHECK", "x: "+(x+(this.width-paddingRight-x)/2));
            Log.e("CHECK", "y: "+y);
            Log.e("CHECK", "height: "+height);
            printer.addBarcode1D(x+(this.width-paddingRight-x)/2, y, "128", height, Alignment.CENTER, Rotation.DEGREE_0, 3,3, Alignment.CENTER, barcode, null);
        }
        private void writeLine(String text, int fontSize, int maxLine){
            if(maxLine<=0)
                return;
            LinkedList<Word> words = Word.slice(text);
            int line = 0;
            int sizeByFont=0;
            String font;
            Word word;
            while(words.size()>0){
                if(y+getNextLineHeight(fontSize)>height-paddingBottom)
                    break;
                word=words.poll();
                switch (word.type) {
                    case SYMBOL:
                        sizeByFont=(int)(fontSize*2);
                        font="K.TTF";
                        break;
                    case NUMBER:
                        sizeByFont=(int)(fontSize*2);
                        font="K.TTF";
                        break;
                    case ENGLISH:
                        sizeByFont=(int)(fontSize*2);
                        font="K.TTF";
                        break;
                    case KOREAN:
                        sizeByFont=(int)(fontSize*3);
                        font="K.TTF";
                        break;
                    case CHINESE:
                    case CJK_CHINESE:
                    case JAPANESE:
                    case ETC_BIG:
                        sizeByFont=(int)(fontSize*3);
                        font="TC.TTF";
                        break;
                    case ETC_SMALL:
                    default:
                        sizeByFont=(int)(fontSize*1);
                        font="K.TTF";
                        break;
                }
                if(x + sizeByFont*word.number > width - paddingRight){
                    int count = 0;
                    while(x + sizeByFont*count <= width - paddingRight)
                        count++;
                    if(count==0){
                        words.push(word);
                    } else {
                        LinkedList<String> subChars = new LinkedList<>();
                        for(int i=count;i<word.number;++i)
                            subChars.add(word.chars.get(i));
                        words.push(new Word(word.type, word.number-count, subChars));
                        StringBuilder subBuilder = new StringBuilder();
                        for(int i=0;i<count;++i)
                            subBuilder.append(word.chars.get(i));
                        printer.addText(x, y, font, Rotation.DEGREE_0, fontSize, fontSize, Alignment.LEFT, subBuilder.toString(), null);
                    }
                    moveX(0);
                    y+=getNextLineHeight(sizeByFont);
                    if(++line>=maxLine)
                        return;
                } else {
                    printer.addText(x, y, font, Rotation.DEGREE_0, fontSize, fontSize, Alignment.LEFT, word.word, null);
                    this.x+=sizeByFont*word.number;
                }
            }
        }
        private void writeLineCenter(String text, int fontSize, int maxLine){
            if(maxLine<=0)
                return;
            LinkedList<LinkedList<Word>> lines = new LinkedList<>();
            LinkedList<Word> words = Word.slice(text);
            LinkedList<Word> line = new LinkedList<>();
            int currentX=x;
            int currentY=y;
            int sizeByFont=0;
            String font;
            Word word;
            while(words.size()>0){
                if(currentY+getNextLineHeight(fontSize)>height-paddingBottom)
                    break;
                word=words.poll();
                switch (word.type) {
                    case SYMBOL:
                        sizeByFont=(int)(fontSize*2);
                        break;
                    case NUMBER:
                        sizeByFont=(int)(fontSize*2);
                        break;
                    case ENGLISH:
                        sizeByFont=(int)(fontSize*2);
                        break;
                    case KOREAN:
                        sizeByFont=(int)(fontSize*3);
                        break;
                    case CHINESE:
                    case CJK_CHINESE:
                    case JAPANESE:
                    case ETC_BIG:
                        sizeByFont=(int)(fontSize*3);
                        break;
                    case ETC_SMALL:
                    default:
                        sizeByFont=(int)(fontSize*1);
                        break;
                }
                if(currentX + sizeByFont*word.number > width - paddingRight){
                    int count = 0;
                    while(currentX + sizeByFont*count <= width - paddingRight)
                        count++;
                    if(count==0){
                        words.push(word);
                    } else {
                        LinkedList<String> subChars = new LinkedList<>();
                        for(int i=count;i<word.number;++i)
                            subChars.add(word.chars.get(i));
                        words.push(new Word(word.type, word.number-count, subChars));subChars = new LinkedList<>();
                        for(int i=0;i<count;++i)
                            subChars.add(word.chars.get(i));
                        line.add(new Word(word.type, count, subChars));
                    }
                    currentX=paddingLeft;
                    currentY+=getNextLineHeight(sizeByFont);
                    lines.add(line);
                    if(lines.size()>=maxLine)
                        break;
                    line = new LinkedList<>();
                } else {
                    line.add(word);
                    currentX+=sizeByFont*word.number;
                }
            }
            if(line.size()>0)
                lines.add(line);
            int sumX;
            for(int i=0;i<lines.size();++i){
                line=lines.get(i);
                sumX=0;
                for(Word w :line) {
                    switch (w.type) {
                        case SYMBOL:
                            sizeByFont=(int)(fontSize*2);
                            break;
                        case NUMBER:
                            sizeByFont=(int)(fontSize*2);
                            break;
                        case ENGLISH:
                            sizeByFont=(int)(fontSize*2);
                            break;
                        case KOREAN:
                            sizeByFont=(int)(fontSize*3);
                            break;
                        case CHINESE:
                        case CJK_CHINESE:
                        case JAPANESE:
                        case ETC_BIG:
                            sizeByFont=(int)(fontSize*3);
                            break;
                        case ETC_SMALL:
                        default:
                            sizeByFont=(int)(fontSize*1);
                            break;
                    }
                    sumX+=sizeByFont*w.number;
                }
                x=x+(width-paddingRight-x-sumX)/2;
                while(line.size()>0){
                    word=line.poll();
                    switch (word.type) {
                        case SYMBOL:
                            sizeByFont=(int)(fontSize*2);
                            font="K.TTF";
                            break;
                        case NUMBER:
                            sizeByFont=(int)(fontSize*2);
                            font="K.TTF";
                            break;
                        case ENGLISH:
                            sizeByFont=(int)(fontSize*2);
                            font="K.TTF";
                            break;
                        case KOREAN:
                            sizeByFont=(int)(fontSize*3);
                            font="K.TTF";
                            break;
                        case CHINESE:
                        case CJK_CHINESE:
                        case JAPANESE:
                        case ETC_BIG:
                            sizeByFont=(int)(fontSize*3);
                            font="TC.TTF";
                            break;
                        case ETC_SMALL:
                        default:
                            sizeByFont=(int)(fontSize*1);
                            font="K.TTF";
                            break;
                    }
                    printer.addText(x, y, font, Rotation.DEGREE_0, fontSize, fontSize, Alignment.LEFT, word.word, null);
                    this.x+=sizeByFont*word.number;
                }
            }
        }
    }
    private static final long DEFAULT_POLLING_INTERVAL=500L;

    private byte[] COMMAND_END_SYMBOL = "\r\n".getBytes();

    private Handler handler;

    private LabelPrinter.Connection connection;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    public PrinterTSCMX200(Handler handler) {
        this(handler, DEFAULT_POLLING_INTERVAL);
    }
    public PrinterTSCMX200(Handler handler, long pollingInterval) {
        super(pollingInterval);
        this.handler=handler;
    }

    @Override
    public PrinterResponse job(PrinterRequest request) {
        PrinterResponse response = new PrinterResponse();
        //Log.e("CHECK", "JOB: "+request.job);
        try {
            switch (request.job){
                case CONNECT:
                    connection=request.connection;
                    clearConnection();
                    switch (connection){
                        case ETHERNET:
                                try{
                                    socket = new Socket();
                                    socket.connect(new InetSocketAddress(request.strings[0], request.ints[0]), request.ints[1]);
                                    is = socket.getInputStream();
                                    os = socket.getOutputStream();
                                }catch (IOException e){
                                    e.printStackTrace();
                                    response.result=PrinterResponse.Result.CONNECTION_PROBLEM;
                                }
                            break;
                        default:
                    }
                    command("CLS");
                    break;
                case SET_LANGUAGE:
                    command("CODEPAGE "+request.strings[0]);
                    break;
                case SET_SIZE:
                    command("SIZE "+request.ints[0]+" mm, "+request.ints[1]+" mm");
                    break;
                case SET_SPEED:
                    command("SPEED "+request.ints[0]);
                    break;
                case SET_DENSITY:
                    command("DENSITY "+request.ints[0]);
                    break;
                case SET_SENSOR:
                    if(request.booleans[0])
                        command("BLINE "+request.ints[0]+" mm, "+request.ints[1]+" mm");
                    else
                        command("GAP "+request.ints[0]+" mm, "+request.ints[1]+" mm");
                    break;
                case ADD_TEXT:
                    command("TEXT "+request.ints[0]+","+request.ints[1]+",\""+request.strings[0]+"\","+request.ints[2]+","+request.ints[3]+","+request.ints[4]+","+request.ints[5]+",\""+request.strings[1]+"\"");
                    break;
                case ADD_BARCODE_1D:
                    command("BARCODE "+request.ints[0]+","+request.ints[1]+",\""+request.strings[0]+"\","+request.ints[2]+","+request.ints[3]+","+request.ints[4]+","+request.ints[5]+","+request.ints[6]+","+request.ints[7]+",\""+request.strings[1]+"\"");
                    break;
                case ADD_BARCODE_2D:
                    break;
                case PRINT:
                    command("PRINT "+request.ints[0]+","+request.ints[1]);
                    break;
                case DISCONNECT:
                clearConnection();
                    break;
                default:
                    return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("CHECK", "Job Die");
            response.result=PrinterResponse.Result.CONNECTION_PROBLEM;
        }
        return response;
    }

    public boolean connectBlutooth(String address){
        return false;
    }
    public void connectEthernet(String ip, int port, long timeoutMillis, final PrinterCallback callback){
        Log.e("CHECK", "connectEthernet");
        PrinterRequest request = new PrinterRequest(2,1,0);
        request.job=PrinterRequest.Job.CONNECT;
        request.connection=Connection.ETHERNET;
        request.strings[0]=ip;
        request.ints[0]=port;
        request.ints[1]=(int)timeoutMillis;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    Log.e("CHECK", "connectEthernet callback");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            Log.e("CHECK", "connectEthernet Exception");
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }
    public boolean connectUSB(UsbManager usbManager, UsbDevice usbDevice){
        return false;
    }

    @Override
    public void close() {
        close(null);
    }

    @Override
    public void close(final PrinterCallback callback) {
        PrinterRequest request = new PrinterRequest(0,0,0);
        request.job=PrinterRequest.Job.DISCONNECT;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PrinterTSCMX200.super.close();
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }

    private void clearConnection(){
        switch (connection){
            case SERIAL:
                break;
            case ETHERNET:
                if(os!=null)
                    try{os.close();}catch (IOException e){e.printStackTrace();}
                if(is!=null)
                    try{is.close();}catch (IOException e){e.printStackTrace();}
                if(socket!=null)
                    try{socket.close();}catch (IOException e){e.printStackTrace();}
                break;
            case USB:
                break;
            default:
        }
    }

    public void setLanguage(String language, final PrinterCallback callback){
        PrinterRequest request = new PrinterRequest(0,1,0);
        request.job=PrinterRequest.Job.SET_LANGUAGE;
        request.strings[0]=language;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }

    public void setSize(int width, int height, final PrinterCallback callback) {
        PrinterRequest request = new PrinterRequest(2,0,0);
        request.job=PrinterRequest.Job.SET_SIZE;
        request.ints[0]=width;
        request.ints[1]=height;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }

    public void setSpeed(int speed, final PrinterCallback callback){
        PrinterRequest request = new PrinterRequest(1,0,0);
        request.job=PrinterRequest.Job.SET_SPEED;
        request.ints[0]=speed;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }

    public void setDensity(int density, final PrinterCallback callback){
        PrinterRequest request = new PrinterRequest(1,0,0);
        request.job=PrinterRequest.Job.SET_DENSITY;
        request.ints[0]=density;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }

    public void setSensor(boolean use, int distance, int offset, final PrinterCallback callback) {
        PrinterRequest request = new PrinterRequest(2,0,1);
        request.job=PrinterRequest.Job.SET_SENSOR;
        request.booleans[0]=use;
        request.ints[0]=distance;
        request.ints[1]=offset;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }
    //도트 위치(203DPI기준) 1mm = 8dot, TTF 글씨크기 0.353 mm = 1 point, 1m = 2.8346 point
    public void addText(int x, int y, String fontName, Rotation rotation, int xMultiplication, int yMultiplication, Alignment alignment, String content, final PrinterCallback callback) {
        PrinterRequest request = new PrinterRequest(6,2,0);
        request.job=PrinterRequest.Job.ADD_TEXT;
        request.ints[0]=x;
        request.ints[1]=y;
        request.strings[0]=fontName;
        request.ints[2]=rotation.degree;
        request.ints[3]=xMultiplication;
        request.ints[4]=yMultiplication;
        request.ints[5]=alignment.alignment;
        request.strings[1]=content;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }

    public void addBarcode1D(int x, int y, String barcodeType, int height, Alignment numberAlignment, Rotation rotation, int narrowWith, int wideWidth, Alignment barcodeAlignment, String content, final PrinterCallback callback) {
        PrinterRequest request = new PrinterRequest(8,2,0);
        request.job=PrinterRequest.Job.ADD_BARCODE_1D;
        request.ints[0]=x;
        request.ints[1]=y;
        request.strings[0]=barcodeType;
        request.ints[2]=height;
        request.ints[3]=numberAlignment.alignment;
        request.ints[4]=rotation.degree;
        request.ints[5]=narrowWith;
        request.ints[6]=wideWidth;
        request.ints[7]=barcodeAlignment.alignment;
        request.strings[1]=content;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }

    public void print(int set, int setSize, final PrinterCallback callback) {
        PrinterRequest request = new PrinterRequest(2,0,0);
        request.job=PrinterRequest.Job.PRINT;
        request.ints[0]=set;
        request.ints[1]=setSize;
        try {
            requestForce(request, new Callback<PrinterResponse>() {
                @Override
                public void callback(final PrinterResponse response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.callback(response);
                        }
                    });
                }
            });
        } catch (Exception e){
            if(callback!=null)
                callback.callback(new PrinterResponse(PrinterResponse.Result.SERVICE_END));
        }
    }

    @Override
    public boolean isSupport(LabelContent labelContent) {
        switch (labelContent.getSize()){
            case W50H30:
                switch (labelContent.getFormat()){
                    case HERB_GOODS:
                        return true;
                    case TAKE_INSTRUNCTION:
                    default:
                        return false;
                }
            case W80H60:
                switch (labelContent.getFormat()){
                    case TAKE_INSTRUNCTION:
                    case HERB_GOODS_EXTENSION:
                        return true;
                    case HERB_GOODS:
                    default:
                        return false;
                }
            case W80H30:
                switch (labelContent.getFormat()){
                    case PATIENT_NAME:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    private void command(String command) throws IOException{
        command(command.getBytes());
    }
    private void command(byte[] bytes) throws IOException{
        os.write(bytes);
        os.write(COMMAND_END_SYMBOL);
        os.flush();
    }

    @Override
    public void print(LabelContent labelContent, PrinterCallback callback) throws PrinterNotSupportException{
        String params[]=labelContent.getParams();
        switch (labelContent.getSize()){
            case W50H30:
                switch (labelContent.getFormat()){
                    case HERB_GOODS:
                        if(params.length!=3)
                            throw new PrinterNotSupportException();
                        //50 mm X 30mm    =   400 dot X 240 dot       1.9685 inch X 1.1811 inch    141.7323 Mul  X 85 Mul
                        setLanguage("UTF-8", null);
                        setSize(50, 30, null);
                        setSpeed(4, null);
                        setDensity(4, null);
                        setSensor(true, 3, 0, null);
                        LinkedList<Word> words = Word.slice(params[0]);
                        int letterWeight = 36;
                        int totalLength = 0;
                        for(Word word: words)
                            totalLength += word.number*letterWeight;
                        int start=16;
                        if(totalLength<368)
                            start=(400-totalLength)/2;
                        int length;
                        String font;
                        for(Word word: words) {
                            switch (word.type) {
                                case SYMBOL:
                                case NUMBER:
                                case ENGLISH:
                                case KOREAN:
                                    font="K.TTF";
                                    break;
                                case CHINESE:
                                case CJK_CHINESE:
                                case JAPANESE:
                                case ETC_BIG:
                                    font="TC.TTF";
                                    break;
                                case ETC_SMALL:
                                default:
                                    font="K.TTF";
                            }
                            length = word.number*letterWeight;
                            if(start+length>368){
                                int count = (368 - start)/letterWeight;
                                StringBuilder subBuilder = new StringBuilder();
                                for(int i=0;i<count;++i)
                                    subBuilder.append(word.chars.get(i));
                                subBuilder.append("...");
                                addText(start, 8, font, Rotation.DEGREE_0, 13, 13, Alignment.LEFT, subBuilder.toString(), null);
                                break;
                            } else {
                                addText(start+length/2, 8, font, Rotation.DEGREE_0, 13, 13, Alignment.CENTER, word.word, null);
                            }
                            start+=length;
                        }
                        addBarcode1D(200, 56, "128", 96, Alignment.CENTER, Rotation.DEGREE_0, 3,3, Alignment.CENTER, params[1], null);

                        addText(384, 184, "K.TTF", Rotation.DEGREE_0, 10, 10, Alignment.RIGHT, params[2], null);

                        print(labelContent.getSet(), labelContent.getSetSize(), callback);
                        break;
                    case TAKE_INSTRUNCTION:
                        throw new PrinterNotSupportException();
                    default:
                        throw new PrinterNotSupportException();
                }
                break;
            case W80H60:
                switch (labelContent.getFormat()){
                    case HERB_GOODS:
                        throw new PrinterNotSupportException();
                    case TAKE_INSTRUNCTION:
                        if(params.length!=7)
                            throw new PrinterNotSupportException();
                        //80 mm X 60mm    =   640 dot X 480 dot
                        setLanguage("UTF-8", null);
                        setSize(80, 60, null);
                        setSpeed(4, null);
                        setDensity(4, null);
                        setSensor(true, 3, 0, null);
                        int letterWeight = 33;

                        LinkedList<Word> words;
                        String[] copiedParam = Arrays.<String>copyOf(params, params.length);
                        int yPosition = 8;
                        int start;
                        int length;
                        String font;
                        int line = 0;
                        for(int p=0;p<copiedParam.length;++p){
                            line++;
                            start=16;
                            words = Word.slice(copiedParam[p]);
                            for(Word word: words) {
                                length = word.number*letterWeight;
                                if(start+length>608){
                                    int count = (608 - start)/letterWeight;
                                    StringBuilder leftBuilder = new StringBuilder();
                                    int index = 0;
                                    for(String ch : word.chars)
                                        if(++index>count)
                                            leftBuilder.append(ch);
                                    copiedParam[p--]=leftBuilder.toString();
                                    break;
                                }
                                start+=length;
                            }
                            yPosition += letterWeight+12;
                        }
                        if(line>10)
                            line=10;
                        yPosition = 48*(10-line)/2;
                        line=0;
                        for(int p=0;p<params.length;++p){
                            if(++line>=10)
                                break;
                            start=16;
                            words = Word.slice(params[p]);
                            for(Word word: words) {
                                switch (word.type) {
                                    case SYMBOL:
                                    case NUMBER:
                                    case ENGLISH:
                                    case KOREAN:
                                        font="K.TTF";
                                        break;
                                    case CHINESE:
                                    case CJK_CHINESE:
                                    case JAPANESE:
                                    case ETC_BIG:
                                        font="TC.TTF";
                                        break;
                                    case ETC_SMALL:
                                    default:
                                        font="K.TTF";
                                }
                                length = word.number*letterWeight;
                                if(start+length>608){
                                    int count = (608 - start)/letterWeight;
                                    StringBuilder subBuilder = new StringBuilder();
                                    StringBuilder leftBuilder = new StringBuilder();
                                    int index = 0;
                                    for(String ch : word.chars)
                                        if(++index>count)
                                            leftBuilder.append(ch);
                                        else
                                            subBuilder.append(ch);
                                    addText(start+length/2, yPosition, font, Rotation.DEGREE_0, 13, 13, Alignment.CENTER, subBuilder.toString(), null);
                                    Log.e("CHECK", "start: "+line);
                                    params[p--]=leftBuilder.toString();
                                    break;
                                } else {
                                    addText(start+length/2, yPosition, font, Rotation.DEGREE_0, 13, 13, Alignment.CENTER, word.word, null);
                                }
                                start+=length;
                            }
                            yPosition += letterWeight+12;
                        }

                        print(labelContent.getSet(), labelContent.getSetSize(), callback);


                        break;
                    case HERB_GOODS_EXTENSION:
                        if(params.length!=8)
                            throw new PrinterNotSupportException();
                        LabelWriter labelWriter = new LabelWriter(this, LabelContent.Size.W80H60.width, LabelContent.Size.W80H60.height, 30, 20);
                        int fontSize=20;
                        labelWriter.writeLineCenter(params[0], fontSize, 1);
                        labelWriter.nextLine(fontSize);

                        labelWriter.writeBarcodeCenter(params[1], 0.8 , 0.1);
                        labelWriter.nextLine(0.1);

                        fontSize=10;
                        labelWriter.nextLine(fontSize);
                        labelWriter.writeLine(params[2], fontSize, 1);
                        labelWriter.nextLine(fontSize);
                        labelWriter.writeLine(params[3], fontSize, 1);
                        labelWriter.nextLine(fontSize);
                        labelWriter.writeLine(params[4], fontSize, 1);
                        labelWriter.nextLine(fontSize);
                        labelWriter.writeLine(params[5], fontSize, 1);
                        labelWriter.nextLine(fontSize);
                        labelWriter.writeLineCenter(params[6], fontSize, 1);
                        labelWriter.nextLine(fontSize);
                        labelWriter.writeBarcodeCenter(params[7], 0.8 , 0.1);
                        print(labelContent.getSet(), labelContent.getSetSize(), callback);

                        break;
                    default:
                        throw new PrinterNotSupportException();
                }
                break;
            default:
                throw new PrinterNotSupportException();
        }

    }
}
