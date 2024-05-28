/*******************************************************************************
 * Copyright (C) 2012,2024, Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 ******************************************************************************/
package com.vernalis.pdbconnector.config;

/**
 * Manages the dialog options for PDB Connector KNIME Node.
 *
 * <P>
 * Singleton class to define the query and report options presented in
 * {@link com.vernalis.pdbconnector.PdbConnectorNodeDialog2} and used
 * by {@link com.vernalis.pdbconnector.PdbConnectorNodeModel2}. The
 * configuration is loaded dynamically from an external
 * <code>xml/PdbConnectorConfig.xml/dtd</code> file at run time, to allow for
 * updates to the supported PDB query and report options without the need for
 * code modification.
 *
 * @author dmorley
 * @see com.vernalis.pdbconnector.PdbConnectorNodeDialog2
 * @see com.vernalis.pdbconnector.PdbConnectorNodeModel2
 * @deprecated 07-Jul-2016 SDR - Use {@link PdbConnectorConfig2} in place
 */
@Deprecated
public class PdbConnectorConfig {

    /**
     * @see PdbConnectorConfig2#ERROR_MSG
     */
    public static String ERROR_MSG = PdbConnectorConfig2.ERROR_MSG;
}
