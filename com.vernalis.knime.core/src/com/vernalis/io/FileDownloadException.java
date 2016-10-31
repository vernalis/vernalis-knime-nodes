package com.vernalis.io;

/**
 * A catch-all class for exceptions relating to downloading files from local or
 * remote servers
 */
public class FileDownloadException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 648012596916286421L;

	public FileDownloadException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileDownloadException(String message) {
		super(message);
	}

	public FileDownloadException(Throwable cause) {
		super(cause);
	}

}
