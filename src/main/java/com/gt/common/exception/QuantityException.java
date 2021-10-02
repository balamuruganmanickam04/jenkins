package com.gt.common.exception;

public class QuantityException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String errorMessage;

	public QuantityException(String error) {
		super(error);
	}

	public static String getErrorMessage() {
		return errorMessage;
	}

	public static void setErrorMessage(String errorMessage) {
		QuantityException.errorMessage = errorMessage;
	}

}
