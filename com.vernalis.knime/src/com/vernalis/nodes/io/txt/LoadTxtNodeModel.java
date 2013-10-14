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
package com.vernalis.nodes.io.txt;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
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
 * This is the model implementation of LoadTxt.
 * Node to Load txt files to a column in the table
 *
 * @author SDR
 */
public class LoadTxtNodeModel extends NodeModel {
	// the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(LoadTxtNodeModel.class);
        
    /** the settings key which is used to retrieve and 
        store the settings (from the dialog or from a settings file)    
       (package visibility to be usable from the dialog). */

	static final String CFG_PATH_COLUMN_NAME = "Path_column_name";
	static final String CFG_FILE_COLUMN_NAME = "File_column_name";
    
    private final SettingsModelString m_PathColumnName = 
    		new SettingsModelString(CFG_PATH_COLUMN_NAME, null);
	
    private final SettingsModelString m_FilecolumnName = 
    		new SettingsModelString(CFG_FILE_COLUMN_NAME, "Text File");


    /**
     * Constructor for the node model.
     */
    protected LoadTxtNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(1, 1);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO do something here
        logger.info("Node Model Stub... this is not yet implemented !");

        
        // the data table spec of the single output table, 
        // the table will have three columns:
        ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec());
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);
        return new BufferedDataTable[]{out};
    }
    
    private ColumnRearranger createColumnRearranger(DataTableSpec in) {
        ColumnRearranger c = new ColumnRearranger(in);
        //The column index of the selected column
        final int colIndex = in.findColumnIndex(m_PathColumnName.getStringValue());
        // column spec of the appended column
        DataColumnSpec newColSpec = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in, 
        m_FilecolumnName.getStringValue()), StringCell.TYPE).createSpec();
                
        // utility object that performs the calculation
        SingleCellFactory factory = new SingleCellFactory(newColSpec) {
            public DataCell getCell(DataRow row) {
                DataCell pathcell = row.getCell(colIndex);
                
                if (pathcell.isMissing() || !(pathcell instanceof StringValue)) {
                    return DataType.getMissingCell();
                }
                //Here we actually do the meat of the work and fetch file

            	String urlToRetrieve =((StringValue)pathcell).getStringValue();
            	//Now, if it is a Location, convert to a URL
            	urlToRetrieve = FileHelpers.forceURL(urlToRetrieve);
            	
            	//Only try to load the files - do not check type!
            	String r = FileHelpers.readURLToString(urlToRetrieve);
            	if (!(r==null || "".equals(r))){
            		return new StringCell(r);
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
        // TODO Code executed on reset.
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message

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

        // TODO save user settings to the config object.
        
        m_FilecolumnName.saveSettingsTo(settings);
        m_PathColumnName.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO load (valid) settings from the config object.
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
            
        // TODO check if the settings could be applied to our model
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
        
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
        // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).

    }



}

