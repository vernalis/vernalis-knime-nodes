package com.vernalis.pdbconnector2.query.text.dialog;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

/**
 * An {@link InvalidSettingsException} subclass to be used when the calling
 * method needs to know the {@link NodeSettingsRO} object causing the exception
 * to be thrown
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.30.3
 */
@SuppressWarnings("serial")
public class KnowsROSettingsInvalidSettingsException
		extends InvalidSettingsException {

	private final NodeSettingsRO brokenSettings;

	/**
	 * Full constructor
	 * 
	 * @param msg
	 *            The error message
	 * @param cause
	 *            The causing exception
	 * @param brokenSettings
	 *            The broken settings object
	 */
	public KnowsROSettingsInvalidSettingsException(String msg, Throwable cause,
			NodeSettingsRO brokenSettings) {
		super(msg, cause);
		this.brokenSettings = brokenSettings;
	}

	/**
	 * No cause Constructor
	 * 
	 * @param msg
	 *            The error message
	 * @param brokenSettings
	 *            The broken settings object
	 */
	public KnowsROSettingsInvalidSettingsException(String msg,
			NodeSettingsRO brokenSettings) {
		super(msg);
		this.brokenSettings = brokenSettings;
	}

	/**
	 * No description constructor
	 * 
	 * @param cause
	 *            The causing exception
	 * @param brokenSettings
	 *            The broken settings object
	 */
	public KnowsROSettingsInvalidSettingsException(Throwable cause,
			NodeSettingsRO brokenSettings) {
		super(cause);
		this.brokenSettings = brokenSettings;
	}

	/**
	 * @return The causing NodeSettingsRO object
	 */
	public NodeSettingsRO getBrokenSettings() {
		return brokenSettings;
	}

}
