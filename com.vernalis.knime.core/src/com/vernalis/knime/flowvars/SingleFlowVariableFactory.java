package com.vernalis.knime.flowvars;

import java.util.Map;

import org.knime.core.node.workflow.FlowVariable;

/**
 * A {@link FlowVariableFactory} when only a single variable is created.
 * Implmentations need to implement the abstract {@link #getVariable(Map)}
 * method, and optionally the {@link #afterProcessing()} method
 * 
 * @see FlowVariableFactory
 * @author S.Roughley knime@vernalis.com
 * @since 11-Jun-2024
 */
public abstract class SingleFlowVariableFactory
        extends AbstractFlowVariableFactory {

    /**
     * Constructor
     * 
     * @param newFlowVariableSpec
     *            the spec of the new variable
     */
    public SingleFlowVariableFactory(FlowVariableSpec newFlowVariableSpec) {

        super(newFlowVariableSpec);
    }

    @Override
    public FlowVariable[] getVariables(Map<String, FlowVariable> variables) {

        return new FlowVariable[] { getVariable(variables) };
    }

    /**
     * @param variables
     *            the existing variables
     * @return the new variable to add to the output. NB Must be of the same
     *             name and type as specified by the spec in the constructor
     */
    public abstract FlowVariable getVariable(
            Map<String, FlowVariable> variables);

}
