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
package com.vernalis.nodes.pdb.props;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.helpers.PDBHelperFunctions;


/**
 * This is the model implementation of PdbProperties.
 * Node to extract properties from a PDB cell
 */
public class PdbPropertiesNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(PdbPropertiesNodeModel.class);

	/** the settings key which is used to retrieve and
        store the settings (from the dialog or from a settings file)
       (package visibility to be usable from the dialog). */

	static final String CFG_PDB_COLUMN_NAME = "PDB_column_name";
	static final String CFG_PDB_ID = "PDB_ID";
	static final String CFG_TITLE = "Title";
	static final String CFG_EXP_METHOD = "Experimental_Method";
	static final String CFG_MDLCNT = "Number of Models";
	static final String CFG_RESOLUTION = "Resolution";
	static final String CFG_R = "R";
	static final String CFG_R_FREE = "R_Free";
	static final String CFG_SPACE_GROUP = "Space_Group";
	static final String CFG_REMARK_1 = "Remark_1";
	static final String CFG_REMARK_2 = "Remark_2";
	static final String CFG_REMARK_3 = "Remark_3";

	private final SettingsModelString m_PDBcolumnName =
			new SettingsModelString(CFG_PDB_COLUMN_NAME, null);

	private final SettingsModelBoolean m_PDBID =
			new SettingsModelBoolean(CFG_PDB_ID, true);

	private final SettingsModelBoolean m_ExpMet =
			new SettingsModelBoolean(CFG_EXP_METHOD, true);

	private final SettingsModelBoolean m_Title =
			new SettingsModelBoolean(CFG_TITLE, true);

	private final SettingsModelBoolean m_ModelCount =
			new SettingsModelBoolean (CFG_MDLCNT,true);

	private final SettingsModelBoolean m_Resolution =
			new SettingsModelBoolean(CFG_RESOLUTION, true);

	private final SettingsModelBoolean m_R =
			new SettingsModelBoolean(CFG_R, true);

	private final SettingsModelBoolean m_RFree =
			new SettingsModelBoolean(CFG_R_FREE, true);

	private final SettingsModelBoolean m_SpaceGroup =
			new SettingsModelBoolean(CFG_SPACE_GROUP, true);

	private final SettingsModelBoolean m_Remark1 =
			new SettingsModelBoolean(CFG_REMARK_1, true);

	private final SettingsModelBoolean m_Remark2 =
			new SettingsModelBoolean(CFG_REMARK_2, true);

	private final SettingsModelBoolean m_Remark3 =
			new SettingsModelBoolean(CFG_REMARK_3, true);

	/**
     * Constructor for the node model.
     */
    protected PdbPropertiesNodeModel() {

        super(1, 1);
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        // the data table spec of the single output table,
        ColumnRearranger c = createRearranger(inData[0].getDataTableSpec());
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);
        return new BufferedDataTable[]{out};
    }

    private ColumnRearranger createRearranger(final DataTableSpec in) {

    	//The column index of the selected column
        final int colIndexPDB = in.findColumnIndex(m_PDBcolumnName.getStringValue());

        //Count the new columns to add
        int j=0;
        j += (m_PDBID.getBooleanValue()) ? 1 : 0;
        j += (m_R.getBooleanValue()) ? 1 : 0;
        j += (m_Remark1.getBooleanValue()) ? 1 : 0;
        j += (m_Remark2.getBooleanValue()) ? 1 : 0;
        j += (m_Remark3.getBooleanValue()) ? 1 : 0;
        j += (m_Resolution.getBooleanValue()) ? 1 : 0;
        j += (m_RFree.getBooleanValue()) ? 1 : 0;
        j += (m_SpaceGroup.getBooleanValue()) ? 1 : 0;
        j += (m_Title.getBooleanValue()) ? 1 : 0;
        j += (m_ExpMet.getBooleanValue()) ? 1 : 0;
        j+= (m_ModelCount.getBooleanValue()) ? 1:0;

        final int NewCols = j;

        // column spec of the appended columns
        DataColumnSpec[] newColSpec = new DataColumnSpec[NewCols];
        int i = 0;
        if (m_PDBID.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"PDB ID"),StringCell.TYPE).createSpec();
        	i ++;
        }
        if (m_Title.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"TITLE"),StringCell.TYPE).createSpec();
        	i ++;
        }
        if (m_ExpMet.getBooleanValue()){
        	//TODO - Ideally return this as an array!
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"Experimental Method"),StringCell.TYPE).createSpec();
        	i ++;
        }
        if (m_Resolution.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"Resolution"),DoubleCell.TYPE).createSpec();
        	i ++;
        }
        if (m_ModelCount.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"Number of Models"),IntCell.TYPE).createSpec();
        	i ++;
        }
        if (m_R.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"R"),DoubleCell.TYPE).createSpec();
        	i ++;
        }
        if (m_RFree.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"R Free"),DoubleCell.TYPE).createSpec();
        	i ++;
        }
        if (m_SpaceGroup.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"Space Group"),StringCell.TYPE).createSpec();
        	i ++;
        }
        if (m_Remark1.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"Remark 1"),StringCell.TYPE).createSpec();
        	i ++;
        }
        if (m_Remark2.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"Remark 2"),StringCell.TYPE).createSpec();
        	i ++;
        }
        if (m_Remark3.getBooleanValue()){
        	newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in,"Remark 3"),StringCell.TYPE).createSpec();
        }

        ColumnRearranger rearranger = new ColumnRearranger (in);
        rearranger.append(new AbstractCellFactory(newColSpec){
        	@Override
            public DataCell[] getCells(final DataRow row) {
        		DataCell[] result = new DataCell[NewCols];
        		Arrays.fill(result, DataType.getMissingCell());
        		DataCell c = row.getCell(colIndexPDB);
        		if (c.isMissing()) {
        			return result;
        		}
        		String pdbtext = ((StringValue)c).getStringValue();
        		int i=0;
                if (m_PDBID.getBooleanValue()){
                	result[i] = new StringCell(PDBHelperFunctions.getPDBID(pdbtext));
                	i ++;
                }
                if (m_Title.getBooleanValue()){
                	result[i] = new StringCell(PDBHelperFunctions.getMultiLineText(pdbtext, "TITLE ", false));
                	i ++;
                }
                if(m_ExpMet.getBooleanValue()){
                	result[i] = new StringCell(PDBHelperFunctions.getExpMethod(pdbtext));
                	i ++;
                }
                if (m_Resolution.getBooleanValue()){
                	if (!(PDBHelperFunctions.getResolution(pdbtext)==null)){
                		result[i] = new DoubleCell(PDBHelperFunctions.getResolution(pdbtext));
                	}
                	i ++;
                }
                if (m_ModelCount.getBooleanValue()){
                	if (!(PDBHelperFunctions.getNumModels(pdbtext)==null)){
                		result[i] = new IntCell(PDBHelperFunctions.getNumModels(pdbtext));
                	}
                	i++;
                }
                if (m_R.getBooleanValue()){
                	if (!(PDBHelperFunctions.getR(pdbtext)==null)){
                		result[i] = new DoubleCell(PDBHelperFunctions.getR(pdbtext));
                	}
                	i ++;
                }
                if (m_RFree.getBooleanValue()){
                	if (!(PDBHelperFunctions.getRFree(pdbtext)==null)){
                		result[i] = new DoubleCell(PDBHelperFunctions.getRFree(pdbtext));
                	}
                	i ++;
                }
                if (m_SpaceGroup.getBooleanValue()){
                	if (!(PDBHelperFunctions.getSpaceGroup(pdbtext)==null)){
                		result[i] = new StringCell(PDBHelperFunctions.getSpaceGroup(pdbtext));
                	}
                	i ++;
                }
                if (m_Remark1.getBooleanValue()){
                	if (!(PDBHelperFunctions.getMultiLineText(pdbtext, "REMARK   1", false)==null)){
                		result[i] = new StringCell(PDBHelperFunctions.getMultiLineText(pdbtext, "REMARK   1", false));
                	}
                	i ++;
                }
                if (m_Remark2.getBooleanValue()){
                	if (!(PDBHelperFunctions.getMultiLineText(pdbtext, "REMARK   2", false)==null)){
                		result[i] = new StringCell(PDBHelperFunctions.getMultiLineText(pdbtext, "REMARK   2", false));
                	}
                	i ++;
                }
				if (m_Remark3.getBooleanValue()
						&& !(PDBHelperFunctions.getMultiLineText(pdbtext,
								"REMARK   3", false) == null)) {
					result[i] = new StringCell(PDBHelperFunctions
							.getMultiLineText(pdbtext, "REMARK   3", false));
				}
                return result;
        	}

        });
        return rearranger;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        // check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message

    	// Check the selection for the pdb column
    	int colIndex = -1;
        if (m_PDBcolumnName.getStringValue() == null) {
            int i = 0;
            for (DataColumnSpec cs : inSpecs[0]) {
                if (cs.getType().isCompatible(StringValue.class)) {
                    if (colIndex != -1) {
                    	setWarningMessage("No PDB cell column selected");
                        throw new InvalidSettingsException(
                                "No PDB cell column selected.");
                    }
                    colIndex = i;
                }
                i++;
            }

            if (colIndex == -1) {
            	setWarningMessage("No PDB cell column selected");
                throw new InvalidSettingsException("No PDB cell column selected.");
            }
            m_PDBcolumnName.setStringValue(inSpecs[0].getColumnSpec(colIndex)
                    .getName());
            setWarningMessage("Column '" + m_PDBcolumnName.getStringValue()
                    + "' auto selected for PDB column");
        } else {
            colIndex =
                    inSpecs[0].findColumnIndex(m_PDBcolumnName.getStringValue());
            if (colIndex < 0) {
            	setWarningMessage("No such column: "
                        + m_PDBcolumnName.getStringValue());
                throw new InvalidSettingsException("No such column: "
                        + m_PDBcolumnName.getStringValue());
            }

            DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
            if (!colSpec.getType().isCompatible(StringValue.class)) {
            	setWarningMessage("Column \"" + m_PDBcolumnName
                        + "\" does not contain string values");
                throw new InvalidSettingsException("Column \"" + m_PDBcolumnName
                        + "\" does not contain string values: "
                        + colSpec.getType().toString());
            }
        }
        //Finally we need to find at least one property otherwise we are wasting a node!
        if(!(m_PDBID.getBooleanValue() || m_R.getBooleanValue() || m_Remark1.getBooleanValue() ||
        		m_Remark2.getBooleanValue() || m_Remark3.getBooleanValue() || m_Resolution.getBooleanValue() ||
        		m_RFree.getBooleanValue() || m_SpaceGroup.getBooleanValue() || m_Title.getBooleanValue() ||
        		m_ExpMet.getBooleanValue())) {
        	setWarningMessage("At least one property must be selected");
        	throw new InvalidSettingsException("No properties selected");
        }

        // everything seems to fine
        ColumnRearranger c = createRearranger(inSpecs[0]);
        return new DataTableSpec[]{c.createSpec()};
    }




    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        m_PDBcolumnName.saveSettingsTo(settings);
        m_PDBID.saveSettingsTo(settings);
        m_R.saveSettingsTo(settings);
        m_Remark1.saveSettingsTo(settings);
        m_Remark2.saveSettingsTo(settings);
        m_Remark3.saveSettingsTo(settings);
        m_Resolution.saveSettingsTo(settings);
        m_RFree.saveSettingsTo(settings);
        m_SpaceGroup.saveSettingsTo(settings);
        m_Title.saveSettingsTo(settings);
        m_ExpMet.saveSettingsTo(settings);
        m_ModelCount.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        m_PDBcolumnName.loadSettingsFrom(settings);
        m_PDBID.loadSettingsFrom(settings);
        m_R.loadSettingsFrom(settings);
        m_Remark1.loadSettingsFrom(settings);
        m_Remark2.loadSettingsFrom(settings);
        m_Remark3.loadSettingsFrom(settings);
        m_Resolution.loadSettingsFrom(settings);
        m_RFree.loadSettingsFrom(settings);
        m_SpaceGroup.loadSettingsFrom(settings);
        m_Title.loadSettingsFrom(settings);
        m_ExpMet.loadSettingsFrom(settings);
        m_ModelCount.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        m_PDBcolumnName.validateSettings(settings);
        m_PDBID.validateSettings(settings);
        m_R.validateSettings(settings);
        m_Remark1.validateSettings(settings);
        m_Remark2.validateSettings(settings);
        m_Remark3.validateSettings(settings);
        m_Resolution.validateSettings(settings);
        m_RFree.validateSettings(settings);
        m_SpaceGroup.validateSettings(settings);
        m_Title.validateSettings(settings);
        m_ExpMet.validateSettings(settings);
        m_ModelCount.validateSettings(settings);

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

	/**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Code executed on reset.
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }

}

