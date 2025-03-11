package com.vernalis.knime.flowvars;

import java.util.Arrays;
import java.util.Objects;

/**
 * Base abstract {@link FlowVariableFactory} implementation class.
 * Implementations of this class need to implement the missing
 * {@link #getVariables(java.util.Map)} method, and optionally
 * {@link #afterProcessing()}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 11-Jun-2024
 */
public abstract class AbstractFlowVariableFactory
        implements FlowVariableFactory {

    private final FlowVariableSpec[] specs;

    /**
     * Constructor
     * 
     * @param newFlowVariableSpecs
     *            the specs for the new flow variables
     */
    public AbstractFlowVariableFactory(FlowVariableSpec... newFlowVariableSpecs) {

        if (Arrays.asList(Objects.requireNonNull(newFlowVariableSpecs,
                "Specs cannot be null")).contains(null)) {
            throw new NullPointerException("Specs cannot contain null");
        }
        this.specs = newFlowVariableSpecs;
    }

    @Override
    public FlowVariableSpec[] getVariableSpecs() {

        return specs;
    }

}
