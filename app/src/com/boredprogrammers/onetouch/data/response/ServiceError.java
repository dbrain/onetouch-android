package com.boredprogrammers.onetouch.data.response;

public class ServiceError {
	public int errorCode;
	public int responseCode;
	public String errorMessage;
	public Exception exception;
	
	public ServiceError(int responseCode, String errorMessage) {
		this.responseCode = responseCode;
		this.errorMessage = errorMessage;
	}
	
	public ServiceError(Exception e) {
		this.exception = e;
	}
}
