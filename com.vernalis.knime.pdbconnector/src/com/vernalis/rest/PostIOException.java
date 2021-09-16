package com.vernalis.rest;

import java.io.IOException;

/**
 * A simple {@link IOException} implementation which also tracks the HTTP
 * response code
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
@SuppressWarnings("serial")
public class PostIOException extends IOException {

	private final int responseCode;

	/**
	 * Simple constructor with only a response Code
	 * 
	 * @param responseCode
	 *            The response code
	 */
	public PostIOException(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * Full constructor
	 * 
	 * @param responseCode
	 *            The response code
	 * @param message
	 *            The error message
	 * @param cause
	 *            The cause
	 */
	public PostIOException(int responseCode, String message, Throwable cause) {
		super(message, cause);
		this.responseCode = responseCode;
	}

	/**
	 * Constructor for response code and message
	 * 
	 * @param responseCode
	 *            The response code
	 * @param message
	 *            The error message
	 */
	public PostIOException(int responseCode, String message) {
		super(message);
		this.responseCode = responseCode;
	}

	/**
	 * Constructor for response code and cause
	 * 
	 * @param responseCode
	 *            The response code
	 * @param cause
	 *            The cause
	 */
	public PostIOException(int responseCode, Throwable cause) {
		super(cause);
		this.responseCode = responseCode;
	}

	/**
	 * @return the response code
	 */
	public int getResponseCode() {
		return responseCode;
	}

}
