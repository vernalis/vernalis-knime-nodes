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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of SmartsViewer.
 * Retrieves a SMARTSViewer visualisation of a columns of SMARTS strings using the service at www.smartsviewer.de
 */
public class SmartsViewerNodeModel extends NodeModel {

	// the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(SmartsViewerNodeModel.class);

    /** the settings key which is used to retrieve and
        store the settings (from the dialog or from a settings file)
       (package visibility to be usable from the dialog). */

	static final String CFG_SMARTS = "SMARTS_Column";
	static final String CFG_VIS_MODUS = "Visualisation_Modus";
	static final String CFG_LEGEND = "Legend_Option";
	//static final String CFG_IMG_FORMAT = "Image_Format";
	static final String CFG_NUM_RETRIES = "No_of_retries";
	static final String CFG_DELAY = "Retry_Delay";
	static final String CFG_IGNORE_ERR = "Ignore_Errors";


    private final SettingsModelString m_SmartsCol =
    		new SettingsModelString(CFG_SMARTS, null);

    private final SettingsModelString m_VisModus =
    		new SettingsModelString(CFG_VIS_MODUS, "1");

    private final SettingsModelString m_Legend =
    		new SettingsModelString(CFG_LEGEND,"both");

    private final SettingsModelIntegerBounded m_maxRetries = 
    		new SettingsModelIntegerBounded(CFG_NUM_RETRIES, 10,0,20);
    
    private final SettingsModelIntegerBounded m_retryDelay = 
    		new SettingsModelIntegerBounded(CFG_DELAY, 1,1,600);
    
    private final SettingsModelBoolean m_ignoreErrors = 
    		new SettingsModelBoolean(CFG_IGNORE_ERR, true);
    
    //private final SettingsModelString m_ImgFmt =
    //		new SettingsModelString (CFG_IMG_FORMAT,"png");
    //For now, we will always use png
    private String m_ImgFmt = "png";
    
    //And some settings relating to failure to connect to the smartsviewer website
    //Max no of times to try again following initial failure
    //private final int m_maxRetries = 10;
    //Delay in seconds between attempts
    //private final int m_retryDelay = 1;
    //Response to server failuree
    //private final Boolean m_ignoreErrors = true;
    
    /**
     *
     * Constructor for the node model.
     */
    protected SmartsViewerNodeModel() {

        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec());
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);
        return new BufferedDataTable[]{out};
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

    	//Check that a column is selected
       	int colIndex = -1;
        if (m_SmartsCol.getStringValue() == null) {
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
            m_SmartsCol.setStringValue(inSpecs[0].getColumnSpec(colIndex)
                    .getName());
            setWarningMessage("Column '" + m_SmartsCol.getStringValue()
                    + "' auto selected");
        } else {
            colIndex =
                    inSpecs[0].findColumnIndex(m_SmartsCol.getStringValue());
            if (colIndex < 0) {
                throw new InvalidSettingsException("No such column: "
                        + m_SmartsCol.getStringValue());
            }

            DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
            if (!colSpec.getType().isCompatible(StringValue.class)) {
                throw new InvalidSettingsException("Column \"" + m_SmartsCol
                        + "\" does not contain string values: "
                        + colSpec.getType().toString());
            }
        }
        if (m_SmartsCol.getStringValue().equals("") ||m_SmartsCol == null){
        	setWarningMessage("SMARTS column name cannot be empty");
        	throw new InvalidSettingsException("SMARTS column name cannot be empty");
        }
        // everything seems to fine
        ColumnRearranger c = createColumnRearranger(inSpecs[0]);
        return new DataTableSpec[]{c.createSpec()};

    }


    private ColumnRearranger createColumnRearranger(final DataTableSpec in){
    	
        ColumnRearranger c = new ColumnRearranger(in);
        
        //The column index of the selected column
        final int colIndex = in.findColumnIndex(m_SmartsCol.getStringValue());
        
        // column spec of the appended column
        DataColumnSpec newColSpec = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,
        "SMARTS Viewer Representation"), PNGImageContent.TYPE).createSpec();

        // utility object that performs the calculation
        SingleCellFactory factory = new SingleCellFactory(newColSpec) {
            @Override
            public DataCell getCell(final DataRow row) {
                DataCell smartscell = row.getCell(colIndex);
                
                //Deal with an empty input or missing cell
                if (smartscell.isMissing() || !(smartscell instanceof StringValue)) {
                    return DataType.getMissingCell();
                }

                //Here we actually do the meat of the work and fetch file
                //Generate the url
            	String url = SmartsviewerHelper.getSMARTSViewerURL(m_ImgFmt, m_VisModus.getStringValue(),
            			m_Legend.getStringValue(),
            			((StringValue)smartscell).getStringValue());
            	
            	int triesLeft = m_maxRetries.getIntValue();
 
            	while (triesLeft >=0){
            		try {
            			return SmartsviewerHelper.toPNGCell(url);
            		} catch (Exception e){
            			logger.warn("Unable to connect to SMARTSviewer server - " +
            			triesLeft + " of " + m_maxRetries.getIntValue() + " remaining...");
            			triesLeft --;
            			pause (m_retryDelay.getIntValue());
            			
            			//TODO: Figure how to implement checkCanceled
            			//exec.checkCanceled();
            		}
            	}
            	//We only get here if we haven't managed to connect to the server
            	if (m_ignoreErrors.getBooleanValue()){
            		logger.warn("Connection to SMARTSviewer server failed - Row ignored");
            		return DataType.getMissingCell();
            	}else {
            		logger.error("Connection to SMARTSviewer server failed - Execution aborted");
            		return null;
            	}
            }
        };
        
        c.append(factory);
        return c;
    }
    
    private static void pause(int seconds){
    	//simple delay function without using threads
        Date start = new Date();
        Date end = new Date();
        while(end.getTime() - start.getTime() < seconds * 1000){
            end = new Date();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

    	//m_ImgFmt.saveSettingsTo(settings);
    	m_VisModus.saveSettingsTo(settings);
    	m_Legend.saveSettingsTo(settings);
    	m_SmartsCol.saveSettingsTo(settings);
    	m_ignoreErrors.saveSettingsTo(settings);
    	m_maxRetries.saveSettingsTo(settings);
    	m_retryDelay.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {

    	//m_ImgFmt.loadSettingsFrom(settings);
    	m_VisModus.loadSettingsFrom(settings);
    	m_Legend.loadSettingsFrom(settings);
    	m_SmartsCol.loadSettingsFrom(settings);
    	m_ignoreErrors.loadSettingsFrom(settings);
    	m_maxRetries.loadSettingsFrom(settings);
    	m_retryDelay.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

    	//m_ImgFmt.validateSettings(settings);
    	m_VisModus.validateSettings(settings);
    	m_Legend.validateSettings(settings);
    	m_SmartsCol.validateSettings(settings);
    	m_ignoreErrors.validateSettings(settings);
    	m_maxRetries.validateSettings(settings);
    	m_retryDelay.validateSettings(settings);
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

