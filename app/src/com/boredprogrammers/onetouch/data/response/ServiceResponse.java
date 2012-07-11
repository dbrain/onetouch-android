package com.boredprogrammers.onetouch.data.response;


public final class ServiceResponse<T extends BaseResponse> {
	public ServiceError error;
	public T result;
}
