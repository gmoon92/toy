package com.gmoon.payment.appstore.exception;

import java.io.Serial;

import com.apple.itunes.storekit.client.APIException;
import com.gmoon.payment.global.exception.BaseApiException;
import com.gmoon.payment.global.exception.ErrorCode;
import com.gmoon.payment.global.utils.NumberUtils;

import lombok.Getter;

@Getter
public class AppStoreApiException extends AppStoreException implements BaseApiException {

	@Serial
	private static final long serialVersionUID = 6463035718906290310L;

	private String apiReturnCode;
	private String apiDetailCode;
	private String apiMessage;

	public AppStoreApiException(APIException cause) {
		super(cause, cause.getMessage());

		this.apiReturnCode = NumberUtils.toString(cause.getHttpStatusCode());
		this.apiDetailCode = NumberUtils.toString(cause.getRawApiError());
		this.apiMessage = cause.getMessage();
	}

	@Override
	public String getDetailCode() {
		return ErrorCode.APP_STORE_API_REQUEST_ERROR;
	}
}
