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
import java.net.URL;

import org.knime.core.data.DataCell;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.util.FileUtil;

public class SmartsviewerHelper {
	public static String getSMARTSViewerURL (String imageType, String visModus, String Legend, String SMARTS){
		/*
		 * Function to build correct SMARTSviewer url from specified user options
		 * For details see smartsview.zbh.uni-hamburg.de/home/auto_retrieving
		 */
		String url = "http://www.smartsview.de/smartsview/auto/";
		url += imageType +"/";
		url += visModus + "/";
		url += Legend +"/";
		url += SMARTS;
		url = url.replace("#","%23");
		return url;
	}
	
    public static DataCell toPNGCell(final String urlValue) throws IOException {
        URL url = new URL(urlValue);
        InputStream in = FileUtil.openStreamWithTimeout(url);
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
