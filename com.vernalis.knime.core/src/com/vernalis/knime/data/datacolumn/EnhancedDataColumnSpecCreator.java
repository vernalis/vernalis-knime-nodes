/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.knime.data.datacolumn;

import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.property.ColorHandler;
import org.knime.core.data.property.ShapeHandler;
import org.knime.core.data.property.SizeHandler;
import org.knime.core.data.property.filter.FilterHandler;

/**
 * A simple wrapper for the {@link DataColumnSpecCreator} class which wraps all
 * that classes methods in a Builder pattern to allow method call
 * daisy-chaining, e.g.
 * 
 * <pre>
 * DataColumnSpec colSpec = new EnhancedDataColumnSpecCreator("New Column", StringCell.TYPE)
 * 		.setProperties(colProps).setSizeHandler(szeHdlr).createSpec();
 * </pre>
 * 
 * @author s.roughley
 *
 */
public class EnhancedDataColumnSpecCreator {

	private final DataColumnSpecCreator colSpecFact;

	/**
	 * Initializes the creator with the given column name and type. The
	 * <code>DataColumnProperties</code> are left empty and color, size, and
	 * shape handler are set to <code>null</code>.
	 *
	 * @param name
	 *            the column name
	 * @param type
	 *            the column type
	 * @throws NullPointerException
	 *             if either the column name or type is <code>null</code>
	 */
	public EnhancedDataColumnSpecCreator(final String name, final DataType type) {
		colSpecFact = new DataColumnSpecCreator(name, type);
	}

	/**
	 * Initializes the creator with a given {@link DataColumnSpec}.
	 *
	 * @param cspec
	 *            other spec
	 */
	public EnhancedDataColumnSpecCreator(final DataColumnSpec cspec) {
		colSpecFact = new DataColumnSpecCreator(cspec);
	}

	/**
	 * Merges the existing {@link DataColumnSpec} with a second
	 * {@link DataColumnSpec}. If they have equal structure, the domain
	 * information and properties from both DataColumnSpecs is merged, Color,
	 * Shape and Size-Handlers are compared (must be equal).
	 *
	 * @param cspec2
	 *            the second {@link DataColumnSpec}.
	 *
	 * @see DataTableSpec#mergeDataTableSpecs(DataTableSpec...)
	 * @throws IllegalArgumentException
	 *             if the structure (type and name) does not match, if the
	 *             domain cannot be merged, if the Color-, Shape- or
	 *             SizeHandlers are different or the sub element names are not
	 *             equal.
	 */
	public EnhancedDataColumnSpecCreator merge(DataColumnSpec colSpec) {
		colSpecFact.merge(colSpec);
		return this;
	}

	/**
	 * Creates and returns a new <code>DataColumnSpec</code> using the internal
	 * properties of this creator.
	 *
	 * @return newly created <code>DataColumnSpec</code>
	 */
	public DataColumnSpec createSpec() {
		return colSpecFact.createSpec();
	}

	/**
	 * Set (new) column name. If the column name is empty or consists only of
	 * whitespaces, a warning is logged and an artificial name is created.
	 *
	 * @param name
	 *            the (new) column name
	 * @throws NullPointerException
	 *             if the column name is <code>null</code>
	 */
	public EnhancedDataColumnSpecCreator setName(String name) {
		colSpecFact.setName(name);
		return this;
	}

	/**
	 * Set names of elements when this column contains a vector type. The
	 * default value is an empty array as per
	 * {@link DataColumnSpec#getElementNames()}. If this method is call with
	 * argument <code>null</code>, an empty array will be passed on to the
	 * {@link DataColumnSpec} constructor.
	 * 
	 * @param elNames
	 *            The elements names/identifiers to set.
	 * @throws NullPointerException
	 *             If the argument contains <code>null</code> elements.
	 * @see DataColumnSpec#getElementNames()
	 */
	public EnhancedDataColumnSpecCreator setElementNames(String[] elNames) {
		colSpecFact.setElementNames(elNames);
		return this;
	}

	/**
	 * Set (new) column type.
	 *
	 * @param type
	 *            the (new) column type
	 * @throws NullPointerException
	 *             if the column type is <code>null</code>
	 */
	public EnhancedDataColumnSpecCreator setType(DataType type) {
		colSpecFact.setType(type);
		return this;
	}

	/**
	 * Set (new) domain. If a <code>null</code> domain is set, an empty domain
	 * will be created.
	 *
	 * @param domain
	 *            the (new) domain, if <code>null</code> an empty default domain
	 *            will be created
	 */
	public EnhancedDataColumnSpecCreator setDomain(DataColumnDomain domain) {
		colSpecFact.setDomain(domain);
		return this;
	}

	/**
	 * Set (new) column properties. If a <code>null</code> properties object is
	 * passed, a new empty property object will be created.
	 *
	 * @param props
	 *            the (new) properties, if <code>null</code> an empty props
	 *            object is created
	 */
	public EnhancedDataColumnSpecCreator setProperties(DataColumnProperties props) {
		colSpecFact.setProperties(props);
		return this;
	}

	/**
	 * Set (new) <code>SizeHandler</code> which can be <code>null</code>.
	 *
	 * @param sizeHdl
	 *            the (new) <code>SizeHandler</code> or <code>null</code>
	 */
	public EnhancedDataColumnSpecCreator setSizeHandler(SizeHandler sizeHdl) {
		colSpecFact.setSizeHandler(sizeHdl);
		return this;
	}

	/**
	 * Set (new) <code>ShapeHandler</code> which can be <code>null</code>.
	 *
	 * @param shapeHdl
	 *            the (new) <code>ShapeHandler</code> or <code>null</code>
	 */
	public EnhancedDataColumnSpecCreator setShapeHandler(ShapeHandler shapeHdl) {
		colSpecFact.setShapeHandler(shapeHdl);
		return this;
	}

	/**
	 * Set (new) <code>FilterHandler</code> which can be <code>null</code>.
	 *
	 * @param filterHdl
	 *            the (new) <code>FilterHandler</code> or <code>null</code>
	 * @since 3.3
	 */
	public EnhancedDataColumnSpecCreator setFilterHandler(FilterHandler filterHdl) {
		colSpecFact.setFilterHandler(filterHdl);
		return this;
	}

	/**
	 * Set (new) <code>ColorHandler</code> which can be <code>null</code>.
	 *
	 * @param colorHdl
	 *            the (new) <code>ColorHandler</code> or <code>null</code>
	 */
	public EnhancedDataColumnSpecCreator setColorHandler(ColorHandler colorHdl) {
		colSpecFact.setColorHandler(colorHdl);
		return this;
	}

	/**
	 * Removes all handlers from this creator which are then set to
	 * <code>null</code> for the next call of <code>#createSpec()</code>.
	 */
	public EnhancedDataColumnSpecCreator removeAllHandlers() {
		colSpecFact.removeAllHandlers();
		return this;
	}

}
