package com.vernalis.knime.flowvars;

import java.awt.Color;
import java.util.Objects;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;

import org.knime.core.node.util.SharedIcons;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;

/**
 * An object describing an available {@link FlowVariable}. The name and type are
 * stored, along with optionally the value. Broadly equivalent to
 * {@link org.knime.core.data.DataColumnSpec}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class FlowVariableSpec {

	private final String name;
	private final VariableType<?> type;
	private final String valueString;

	private FlowVariableSpec(String name, VariableType<?> type,
			String valueString) {
		this.name = Objects.requireNonNull(name);
		this.type = type;
		this.valueString = valueString;
	}

	/**
	 * No-value constructor
	 * 
	 * @param name
	 *            the variable name
	 * @param type
	 *            the variable type
	 */
	public FlowVariableSpec(String name, VariableType<?> type) {
		this(name, type, null);
	}

	/**
	 * Constructor from variable, in which case, the value will also be stored
	 * 
	 * @param fVar
	 *            the variable
	 */
	public FlowVariableSpec(FlowVariable fVar) {
		this(fVar.getName(), fVar.getVariableType(), fVar.getValueAsString());
	}

	/**
	 * Method to create an invalid variable spec, which will have neither value
	 * nor type, only a name
	 * 
	 * @param name
	 *            the name
	 * @return and invalid variable
	 */
	public static FlowVariableSpec createInvalidSpec(String name) {
		return new FlowVariableSpec(name, null, null);
	}

	/**
	 * @return whether the variable is valid (i.e. it has a type)
	 */
	public boolean isValid() {
		return type != null;
	}

	/**
	 * @return whether the variable is a constant (i.e. its name starts with
	 *         '{@code knime.}')
	 */
	public boolean isConstant() {
		return name.startsWith("knime.");
	}

	/**
	 * @return the string representation of the stored value or an empty
	 *         optional if no value is stored
	 */
	public Optional<String> getValueString() {
		return Optional.ofNullable(valueString);
	}

	/**
	 * @return whether there is a value stored
	 */
	public boolean hasValue() {
		return valueString != null;
	}

	/**
	 * @return the icon for the variable type
	 */
	public Icon getIcon() {
		return isValid() ? type.getIcon() : SharedIcons.FLOWVAR_DEFAULT.get();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the variable type ({@code null} if the variable is invalid)
	 */
	public VariableType<?> getType() {
		return type;
	}

	/**
	 * Method to render the variable in a JLabel - used for List and Table Cell
	 * renderers
	 * 
	 * @param label
	 *            the component to render to
	 */
    public void render(JLabel label) {

		label.setText(getName());
		label.setIcon(getIcon());
		if (isValid()) {
			StringBuilder sb = new StringBuilder(getName());
			if (hasValue()) {
				sb.append(" (");
				if (isConstant()) {
					sb.append("constant");
				} else {
					sb.append("current value");
				}
				sb.append(": ").append(getValueString().orElse("")).append(')');
			}
			label.setToolTipText(sb.toString());
		} else {
			label.setToolTipText(null);
			label.setBorder(BorderFactory.createLineBorder(Color.RED));
		}

	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof FlowVariableSpec fvs) {
			if (!fvs.name.equals(this.name)) {
				return false;
			}
			if (this.type == null) {
				return fvs.type == null;
			}
			return Objects.equals(this.type.getIdentifier(),
					fvs.type.getIdentifier());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return isValid() ? Objects.hash(name, type.getIdentifier())
				: name.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb =
				new StringBuilder(getClass().getSimpleName()).append('[');
		if (isValid()) {
		sb.append(getType().getIdentifier());
		} else {
			sb.append("Invalid Type");
		}
		if (isValid() && hasValue()) {
			sb.append(" (");
			if (isConstant()) {
				sb.append("constant");
			} else {
				sb.append("current value");
			}
			sb.append(": ").append(getValueString().orElse("")).append(')');
		}
		sb.append(']');
		return sb.toString();
	}
}
