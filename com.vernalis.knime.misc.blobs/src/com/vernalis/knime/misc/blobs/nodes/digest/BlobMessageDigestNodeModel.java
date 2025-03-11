/*******************************************************************************
 * Copyright (c) 2025, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.misc.blobs.nodes.digest;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.digest.BlobMessageDigestNodeDialog.DIGEST_ALGORITHMS;
import static com.vernalis.knime.misc.blobs.nodes.digest.BlobMessageDigestNodeDialog.createAlgorithmNameModel;
import static com.vernalis.knime.misc.blobs.nodes.digest.BlobMessageDigestNodeDialog.createColNameModel;
import static com.vernalis.knime.misc.blobs.nodes.digest.BlobMessageDigestNodeDialog.createConvertToUpperCaseModel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

/**
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since 1.38.0
 */
public class BlobMessageDigestNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelString colNameMdl =
			registerSettingsModel(createColNameModel());
	private final SettingsModelString algorithmMdl =
			registerSettingsModel(createAlgorithmNameModel());
	private final SettingsModelBoolean toUppercaseMdl =
			registerSettingsModel(createConvertToUpperCaseModel());

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		String algo = algorithmMdl.getStringValue();
		if (!DIGEST_ALGORITHMS.contains(algo)) {
			throw new InvalidSettingsException(
					"Algorithm name '" + algo + "' not available");
		}
		int colIdx = getValidatedColumnSelectionModelColumnIndex(colNameMdl,
				BLOB_COLUMN_FILTER, spec, getLogger());

		DataColumnSpec newColSpec = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(spec,
						String.format("%s %s Digest",
								colNameMdl.getStringValue(), algo)),
				StringCell.TYPE).createSpec();

		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.append(new SingleCellFactory(newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell blobcell = row.getCell(colIdx);
				if (blobcell.isMissing()) {
					return DataType.getMissingCell();
				}
				BinaryObjectDataValue bdv = (BinaryObjectDataValue) blobcell;
				MessageDigest md;
				try {
					md = MessageDigest.getInstance(algo);
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				}
				byte[] buffer = new byte[4096];
				try (DigestInputStream dis =
						new DigestInputStream(bdv.openInputStream(), md)) {
					while (dis.read(buffer) >= 0)
						; // Empty loop - we just need to read everything
							// through
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				byte[] hash = md.digest();
				HexFormat hexFormat = HexFormat.of();
				if (toUppercaseMdl.getBooleanValue()) {
					hexFormat = hexFormat.withUpperCase();
				}
				return new StringCell(hexFormat.formatHex(hash));

			}
		});
		return rearranger;
	}

}
