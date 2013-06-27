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
package com.vernalis.nodes.io.listdirs2;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.core.runtime.URIUtil;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of ListDirs.
 */
public class ListDirs2NodeModel extends NodeModel {
	
	// the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(ListDirs2NodeModel.class);
        
    /** the settings key which is used to retrieve and 
        store the settings (from the dialog or from a settings file)    
       (package visibility to be usable from the dialog). */

	static final String CFG_PATH = "Path_name";
	static final String CFG_SUB_DIRS = "Include_Subfolders";

    
    private final SettingsModelString m_Path = 
    		new SettingsModelString(CFG_PATH, null);
    
    private final SettingsModelBoolean m_subDirs = 
    		new SettingsModelBoolean(CFG_SUB_DIRS, false);
    
    private BufferedDataContainer m_dc;
    
    private static final DataTableSpec spec = new DataTableSpec(createDataColumnSpec());
    
    private int m_analysed_files;
    
    
    private int m_currentRowID;
    
    
    /**
     * Constructor for the node model.
     */
    protected ListDirs2NodeModel() {

        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

    	//List the folders from the dialogue
    	String[] folders = m_Path.getStringValue().split(";");

    	//Now create a data container for the new output table
    	
    	m_dc = exec.createDataContainer(spec);
    	
    	m_currentRowID = 0;
    	m_analysed_files = 0;
    	
    	for (String folder : folders){
    		folder = folder.trim();
    		File location = new File(folder);
    		if (!location.isDirectory()) {
    			//Handle URLs instead of paths
                try {
                    if (folder.startsWith("file:")) {
                        folder = folder.substring(5);
                    }
                    location = new File((new URI(folder)).getPath());
                } catch (Exception e) {
                    throw new InvalidSettingsException("\"" + folder
                            + "\" does not exist or is not a directory");
                }
                if (!location.isDirectory()) {
                    throw new InvalidSettingsException("\"" + folder
                            + "\" does not exist or is not a directory");
                }
            }
    		addLocation(location,exec);

    	}
    	m_dc.close();
    	
    	
        return new BufferedDataTable[] {m_dc.getTable()};

    }
    
    private void addLocation (final File location, final ExecutionContext exec)
    throws CanceledExecutionException{
    	
    	//List the folders - recursively if we are doing subfolders too
    	m_analysed_files++;
    	exec.setProgress(m_analysed_files + " file(s) and folder(s) analysed..." + m_currentRowID +" added to output");
    	exec.checkCanceled();
    	
    	if (location.isDirectory()){
    		File[] listFiles = location.listFiles();
    		if (listFiles != null){
    			for (File loc:listFiles){
    				if (loc.isDirectory()){
    					//We need to add a directory whenever it is found
    					try{
    						DataCell[] row = new DataCell[2];
    						row[0] = new StringCell(loc.getAbsolutePath());
    						row[1] = new StringCell(loc.getAbsoluteFile().toURI().toURL().toString());
    						m_dc.addRowToTable(new DefaultRow("Row " + m_currentRowID, row));
    						m_currentRowID++;
    						
    					} catch (MalformedURLException e){
    						logger.error("Unable to create URL to folder", e);
    					}
    				
    					//Now deal with subfolders
    					if (m_subDirs.getBooleanValue()){
    						//Recursively call to add them
    						addLocation(loc,exec);
    						
    					}
    				}
    			}
    		}
    	}
    }

    private static DataColumnSpec[] createDataColumnSpec() {
    	//Create the output table spec
        DataColumnSpec[] dcs = new DataColumnSpec[2];
        dcs[0] =
            new DataColumnSpecCreator("Location", StringCell.TYPE).createSpec();
        dcs[1] = new DataColumnSpecCreator("URL", StringCell.TYPE).createSpec();
        return dcs;
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

    	//Check there is something in the path box
    	if (m_Path == null){
    		throw new InvalidSettingsException("No folder selected");
    	}

        return new DataTableSpec[]{spec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

    	m_Path.saveSettingsTo(settings);
    	m_subDirs.saveSettingsTo(settings);
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {

    	m_Path.loadSettingsFrom(settings);
    	m_subDirs.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

    	m_Path.validateSettings(settings);
    	m_subDirs.validateSettings(settings);
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

