package com.herblinker.android.libraries.base.net;

import com.herblinker.libraries.base.data.DateType;
import com.herblinker.libraries.base.data.NumberType;
import com.herblinker.libraries.base.data.OutputReader;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;

public class NameValuePair {
    public static final int FILE_TYPE_TEXT = 12;
    public static final int FILE_TYPE_IMAGE = 1;
    public static final int FILE_TYPE_VIDEO = 11;

    public static final int FILE_TYPE_BINARY = 2;
    public static final int FILE_TYPE_OTHER = 3;

    public static final String CONTENT_TYPE = "*/*";

    public static final String CONTENT_TYPE_TEXT = "text/*";
    public static final String CONTENT_TYPE_TEXT_PREFIX = "text/";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    public static final String CONTENT_TYPE_IMAGE = "image/*";
    public static final String CONTENT_TYPE_IMAGE_PREFIX = "image";
    public static final String CONTENT_TYPE_IMAGE_PNG = "image/png";
    public static final String CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";
    public static final String CONTENT_TYPE_IMAGE_GIF = "image/gif";
    public static final String CONTENT_TYPE_IMAGE_BMP = "image/bmp";

    public static final String CONTENT_TYPE_VIDEO = "video/*";
    public static final String CONTENT_TYPE_VIDEO_PREFIX = "video";
    public static final String CONTENT_TYPE_VIDEO_MP4 = "video/mp4";
    public static final String CONTENT_TYPE_VIDEO_WAV = "video/wav";
    public static final String CONTENT_TYPE_VIDEO_MKV = "video/mkv";
    public static final String CONTENT_TYPE_VIDEO_3GPP = "video/3GPP";

    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final String DEFAULT_CONTENT_TYPE_IMAGE_PNG = "image/png";
    public static final String DEFAULT_CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";
    public static final String DEFAULT_CONTENT_TYPE_IMAGE_GIF = "image/gif";

    public String name;
    public String value;

    public boolean isFile;
    public String fileName;
    public String contentType;
    public File file;
    public byte[] bytes;

    public NameValuePair(String name, String value){
        this.name=name;
        this.value=value;
    }

    public NameValuePair(String name, byte value){
        this.name=name;
        this.value=String.valueOf(value);
    }

    public NameValuePair(String name, short value){
        this.name=name;
        this.value=String.valueOf(value);
    }

    public NameValuePair(String name, int value){
        this.name=name;
        this.value=String.valueOf(value);
    }

    public NameValuePair(String name, long value){
        this.name=name;
        this.value=String.valueOf(value);
    }

    public NameValuePair(String name, float value){
        this.name=name;
        this.value=String.valueOf(value);
    }

    public NameValuePair(String name, double value){
        this.name=name;
        this.value=String.valueOf(value);
    }

    public NameValuePair(String name, char value){
        this.name=name;
        this.value=String.valueOf(value);
    }

    public NameValuePair(String name, Date value){
        this.name=name;
        this.value=OutputReader.toString(value, DateType.YEAR_TO_SENCONDS, null);
    }

    public NameValuePair(String name, BigDecimal value){
        this.name=name;
        this.value= OutputReader.toString(value, NumberType.PLAIN, null);
    }

    public NameValuePair(String name, Object value){
        this.name=name;
        this.value=value.toString();
    }

    public NameValuePair(String name, File file) {
        this(name, file==null?"":file.getName(), DEFAULT_CONTENT_TYPE, file);
    }

    public NameValuePair(String name, String fileName, File file) {
        this(name, fileName, DEFAULT_CONTENT_TYPE, file);
    }
    public NameValuePair(String name, String fileName, byte[] bytes){
        this(name, fileName, DEFAULT_CONTENT_TYPE, bytes);
    }

    public NameValuePair(String name, String fileName, String contentType, File file){
        this.isFile = true;
        this.name=name;
        this.fileName=fileName;
        this.contentType=contentType;
        this.file=file;
    }

    public NameValuePair(String name, String fileName, String contentType, byte[] bytes){
        this.isFile = true;
        this.name=name;
        this.fileName=fileName;
        this.contentType=contentType;
        this.bytes=bytes;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NameValuePair)
            return name.equals(((NameValuePair) obj).name);
        return false;
    }

    @Override
    public String toString() {
        return name+" : "+value;
    }
}
