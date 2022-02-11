package com.herblinker.android.libraries.base.data;

import com.herblinker.libraries.base.data.DataException;

public class JsonDataException extends DataException{
	//private static final long serialVersionUID = -5154225871171354088L;
	private static final long serialVersionUID = -3154225871171354088L;
	private String message;
	public JsonDataException() {
		
	}

	public JsonDataException(Exception e) {
		//addSuppressed(e);
	}

	public JsonDataException(String message) {
		this.message=message;
	}
	
	public JsonDataException(Exception e, String message) {
		//addSuppressed(e);
		this.message=message;
	}
	
	@Override
	public String getMessage() {
		return makeMessage(super.getMessage(), this.getExplanation(), message);
	}
	@Override
	public String getExplanation() {
		return "Json 데이터 예외.";
	}
}
