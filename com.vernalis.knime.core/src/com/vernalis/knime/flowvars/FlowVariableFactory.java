package com.vernalis.knime.flowvars;

import java.util.Map;

import org.knime.core.node.workflow.FlowVariable;

/**
 * An interface analagous do {@link org.knime.core.data.container.CellFactory}
 * for adding new variables to the stack via a {@link FlowVariableRearranger}
 * 
 * @see FlowVariableRearranger
 * @see AbstractFlowVariableFactory
 * @see SingleFlowVariableFactory
 * @author S.Roughley knime@vernalis.com
 * @since 11-Jun-2024
 */
public interface FlowVariableFactory {

    /**
     * The method which should return the variables to add to the stack at node
     * execution time.
     * 
     * @param variables
     *            the incoming variables available for the calculation of new
     *            values
     * @return the new Variables to add to the stack. NB The returned variables
     *             must be the same number, names
     *             and types as specified by {@link #getVariableSpecs()}
     */
    FlowVariable[] getVariables(Map<String, FlowVariable> variables);

    /**
     * @return the specs of the new variables
     */
    FlowVariableSpec[] getVariableSpecs();

    /**
     * Actions to perform when processing is complete, e.g. clean up resources
     * or setting node warning messages
     */
    default void afterProcessing() {

        // Do nothing
    }
}
