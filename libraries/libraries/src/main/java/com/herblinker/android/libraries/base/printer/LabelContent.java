package com.herblinker.android.libraries.base.printer;

public class LabelContent{
    public enum Size{
        W50H30(50, 30),
        W80H30(80, 30),
        W80H60(80, 60);
        public int width;
        public int height;
        public int getWidth(){
            return width;
        }
        public int getHeight(){
            return height;
        }
        Size(int width, int height){
            this.width=width;
            this.height=height;
        }
    }
    public enum Format{
        PATIENT_NAME,
        HERB_GOODS,
        TAKE_INSTRUNCTION,
        HERB_GOODS_EXTENSION;
    }
    private Size size;
    private Format format;
    private int set;
    private int setSize;
    private String[] params;
    public LabelContent(Size size, Format format, int set, int setSize, String... params){
        this.size=size;
        this.format=format;
        this.set=set;
        this.setSize=setSize;
        this.params=params;
    }

    public Size getSize(){
        return size;
    }
    public void setSize(Size size){
        this.size=size;
    }
    public Format getFormat(){
        return format;
    }
    public void setFormat(Format format){
        this.format=format;
    }
    public String[] getParams(){
        return params;
    }
    public int getSet(){
        return set;
    }
    public int getSetSize(){
        return setSize;
    }
}
