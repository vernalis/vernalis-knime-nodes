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
package com.vernalis.nodes.io.pdb.loadlocal;

import java.io.File;
import java.io.IOException;

import org.knime.bio.types.PdbCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.helpers.FileHelpers;

/**
 * This is the model implementation of LocalPDBLoad.
 * Load a local copy of a PDB files from a column of source filepaths or URLs
 *
 * @author SDR
 */
public class LocalPDBLoadNodeModel2 extends NodeModel {
	// the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(LocalPDBLoadNodeModel2.class);

    /** the settings key which is used to retrieve and
        store the settings (from the dialog or from a settings file)
       (package visibility to be usable from the dialog). */

	static final String CFG_PATH_COLUMN_NAME = "Path_column_name";
	static final String CFG_FILE_COLUMN_NAME = "File_column_name";

    private final SettingsModelString m_PathColumnName =
    		new SettingsModelString(CFG_PATH_COLUMN_NAME, null);

    private final SettingsModelString m_FilecolumnName =
    		new SettingsModelString(CFG_FILE_COLUMN_NAME, "PDB File");


    /**
     * Constructor for the node model.
     */
    protected LocalPDBLoadNodeModel2() {

        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        logger.debug("LocalPBDLoad executing");

        // the data table spec of the single output table,
        ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec());
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);
        return new BufferedDataTable[]{out};
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec in) {
        ColumnRearranger c = new ColumnRearranger(in);

        //The column index of the selected column
        final int colIndex = in.findColumnIndex(m_PathColumnName.getStringValue());

        // column spec of the appended column
        DataColumnSpec newColSpec = new DataColumnSpecCreator(
        		DataTableSpec.getUniqueColumnName(in,
        		m_FilecolumnName.getStringValue()), PdbCell.TYPE).createSpec();

        // utility object that performs the calculation
        SingleCellFactory factory = new SingleCellFactory(newColSpec) {
            @Override
            public DataCell getCell(final DataRow row) {
                DataCell pathcell = row.getCell(colIndex);

                if (pathcell.isMissing() || !(pathcell instanceof StringValue)) {
                    return DataType.getMissingCell();
                }
                //Here we actually do the meat of the work and fetch file

            	String urlToRetrieve =((StringValue)pathcell).getStringValue();
            	//Now, if it is a Location, convert to a URL
            	urlToRetrieve = FileHelpers.forceURL(urlToRetrieve);

            	//Only load up pdb files, but allow them to be g-zipped
            	if (urlToRetrieve.toLowerCase().endsWith(".pdb") ||
            			urlToRetrieve.toLowerCase().endsWith(".pdb.gz")){
            		String r = FileHelpers.readURLToString(urlToRetrieve);
            		if (!(r==null || "".equals(r))){
            			return new PdbCell(r);
            		}
            	}
            	return DataType.getMissingCell();
            }
        };
        c.append(factory);
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {


    	int colIndex = -1;
        if (m_PathColumnName.getStringValue() == null) {
            int i = 0;
            for (DataColumnSpec cs : inSpecs[0]) {
                if (cs.getType().isCompatible(StringValue.class)) {
                    if (colIndex != -1) {
                        throw new InvalidSettingsException(
                                "No column selected.");
                    }
                    colIndex = i;
                }
                i++;
            }

            if (colIndex == -1) {
                throw new InvalidSettingsException("No column selected.");
            }
            m_PathColumnName.setStringValue(inSpecs[0].getColumnSpec(colIndex)
                    .getName());
            setWarningMessage("Column '" + m_PathColumnName.getStringValue()
                    + "' auto selected");
        } else {
            colIndex =
                    inSpecs[0].findColumnIndex(m_PathColumnName.getStringValue());
            if (colIndex < 0) {
                throw new InvalidSettingsException("No such column: "
                        + m_PathColumnName.getStringValue());
            }

            DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
            if (!colSpec.getType().isCompatible(StringValue.class)) {
                throw new InvalidSettingsException("Column \"" + m_PathColumnName
                        + "\" does not contain string values: "
                        + colSpec.getType().toString());
            }
        }
        if (m_PathColumnName.getStringValue().equals("") ||m_PathColumnName == null){
        	setWarningMessage("Path column name cannot be empty");
        	throw new InvalidSettingsException("Path column name cannot be empty");
        }
        // everything seems to fine
        ColumnRearranger c = createColumnRearranger(inSpecs[0]);
        return new DataTableSpec[]{c.createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {


        m_FilecolumnName.saveSettingsTo(settings);
        m_PathColumnName.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        // It can be safely assumed that the settings are valided by the
        // method below.

        m_FilecolumnName.loadSettingsFrom(settings);
        m_PathColumnName.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        // e.g. if the count is in a certain range (which is ensured by the
        // SettingsModel).
        // Do not actually set any values of any member variables.

        m_FilecolumnName.validateSettings(settings);
        m_PathColumnName.validateSettings(settings);

    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

