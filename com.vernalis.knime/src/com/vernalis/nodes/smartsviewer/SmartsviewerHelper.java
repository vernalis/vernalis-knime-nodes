/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.nodes.smartsviewer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.knime.core.data.DataCell;
import org.knime.core.data.image.png.PNGImageContent;

/**
 * Utility Class to assist with retrieving SMARTSViewer renderings from a SMARTS
 * String
 * 
 * @author Stephen Roughley
 * 
 */
public class SmartsviewerHelper {

	/**
	 * The base url for the webservice
	 */
	private static final String SMARTSVIEW_SERVICE_ENTRY =
			"http://smartsview.zbh.uni-hamburg.de/smartsview/auto/";

	/**
	 * Function to generate a URL for the SMARTSViewer query based on the
	 * defined options
	 * 
	 * @param imageType
	 *            The image type (png, pdf or svg)
	 * @param visModus
	 *            The visulaisation mode - see {@link www.smartsview.de} for
	 *            details
	 * @param Legend
	 *            The legend option - see {@link www.smartsview.de} for details
	 * @param SMARTS
	 *            The SMARTS query string
	 * @return A String containing the query url
	 */
	public static String getSMARTSViewerURL(String imageType, String visModus, String Legend,
			String SMARTS) {
		/*
		 * Function to build correct SMARTSviewer url from specified user
		 * options For details see
		 * smartsview.zbh.uni-hamburg.de/home/auto_retrieving
		 */
		StringBuilder sb = new StringBuilder(SMARTSVIEW_SERVICE_ENTRY);
		sb.append(imageType).append("/");
		sb.append(visModus).append("/");
		sb.append(Legend).append("/");
		sb.append(SMARTS);

		// String url = SMARTSVIEW_SERVICE_ENTRY;
		// url += imageType + "/";
		// url += visModus + "/";
		// url += Legend + "/";
		// url += SMARTS;
		// url = url.replace("#", "%23");
		return sb.toString().replace("#", "%23");
	}

	/**
	 * Function to obtain a ImageCell from a SMARTSViewer query url
	 * 
	 * @param urlValue
	 *            The query URL string
	 * @return The DataCell containing the rendered image
	 * @throws IOException
	 */
	public static DataCell toPNGCell(final String urlValue) throws IOException {

		URL url = new URL(urlValue);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException(conn.getResponseMessage());
		}

		// InputStream in = FileUtil.openStreamWithTimeout(url);
		InputStream in = conn.getInputStream();
		try {
			PNGImageContent pngImageContent = new PNGImageContent(in);
			return pngImageContent.toImageCell();
		} finally {
			try {
				in.close();
			} catch (IOException ioe) {
				// ignore
			}
		}
	}
}
