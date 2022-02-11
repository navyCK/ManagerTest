package com.herblinker.android.libraries.base.data;

public class JsonDataWrongFormatException extends JsonDataException{
	//private static final long serialVersionUID = 8682637069363985373L;
	private static final long serialVersionUID = 2682637069363985373L;
	private String message;
	public JsonDataWrongFormatException() {
		
	}
	
	public JsonDataWrongFormatException(Exception e) {
		//addSuppressed(e);
	}
	
	public JsonDataWrongFormatException(String message) {
		this.message=message;
	}
	
	public JsonDataWrongFormatException(Exception e, String message) {
		//addSuppressed(e);
		this.message=message;
	}
	
	@Override
	public String getMessage() {
		return makeMessage(super.getMessage(), this.getExplanation(), message);
	}
	@Override
	public String getExplanation() {
		return "잘못된 Json 포멧 예외.";
	}
}
