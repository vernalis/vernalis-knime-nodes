package com.vernalis.knime.flowvars;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.knime.core.node.Node;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;

/**
 * A class for adding variables to the variable stack during node configure and
 * execution. This is analogous to the
 * {@link org.knime.core.data.container.ColumnRearranger} class for tables,
 * however there are fewer methods as flow variables can neither be dropped from
 * the table nor have an insertion order, and replacement is simply a matter of
 * adding a new variable with the same name as an existing one
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 11-Jun-2024
 */
public class FlowVariableRearranger {

    private static final NodeLogger logger =
            NodeLogger.getLogger(FlowVariableRearranger.class);
    private final List<FlowVariableFactory> facts =
            new CopyOnWriteArrayList<>();

    /**
     * Constructor
     */
    public FlowVariableRearranger() {

    }

    /**
     * Method to add a {@link FlowVariableFactory}. Multiple factories will be
     * called in the order they are added here when generating output. In the
     * event that a factory is added which generates a variable with the same
     * name as a previously added factory, then a warning will be sent to the
     * {@link NodeLogger}
     * 
     * @param factory
     *            the non-{@code null} factory
     */
    public void add(FlowVariableFactory factory) {

        Objects.requireNonNull(factory);
        for (FlowVariableSpec fvSpec : factory.getVariableSpecs()) {
            if (facts.stream()
                    .flatMap(fact -> Arrays.stream(fact.getVariableSpecs()))
                    .anyMatch(fvs -> fvs.getName().equals(fvSpec.getName()))) {
                logger.warnWithFormat(
                        "Existing factory already contains spec for variable"
                                + " with same name as one in this factory (%s) - "
                                + "likely this is an implementation error",
                        fvSpec);
            }
        }
        facts.add(factory);

    }

    /**
     * Method to add default values for all the variables to the node stack.
     * Existing variables on the stack will have their values replaced if they
     * are replaced by one of the {@link FlowVariableFactory}s.
     * Could be considered analgous to
     * {@link org.knime.core.data.container.ColumnRearranger#createSpec()}
     * 
     * @param model
     *            the non-{@code null} {@link NodeModel} instance
     * @implNote
     *               This is a convenience method which delegates to
     *               {@link #setDefaults(NodeModel, FlowVariableStackSpec)},
     *               with {@code null} for the second parameter
     */
    public void setDefaults(NodeModel model) {

        setDefaults(model, null);
    }

    /**
     * Method to add default values for all the variables to the node stack.
     * Could be considered analgous to
     * {@link org.knime.core.data.container.ColumnRearranger#createSpec()}
     * 
     * @param model
     *            the non-{@code null} {@link NodeModel} instance
     * @param inSpec
     *            the incoming {@link FlowVariableStackSpec} - maybe
     *            {@code null}. If a spec is supplied, then any variables
     *            present in it will be skipped at this point when adding
     *            defaults to the stack
     */
    public void setDefaults(NodeModel model, FlowVariableStackSpec inSpec) {

        Objects.requireNonNull(model, "A node model instance must be supplied");

        Map<String, FlowVariable> vars = new LinkedHashMap<>();
        for (FlowVariableFactory fact : facts) {
            FlowVariableSpec[] specs = fact.getVariableSpecs();
            for (FlowVariableSpec spec : specs) {
                if (inSpec != null && inSpec.containsVariable(spec.getName())) {
                    logger.warn("Incoming spec has variable with name '"
                            + spec.getName()
                            + "' - leaving existing value during configure");
                    continue;
                }
                // Create a variable with the default value for the type
                FlowVariable newVar =
                        new FlowVariable(spec.getName(), spec.getType());
                if (vars.put(newVar.getName(), newVar) != null) {
                    logger.codingWithFormat(
                            "Duplicate variables generated with name '%s' - likely an implementation error!",
                            newVar.getName());
                }
            }
        }
        putNewVariablesOnStack(model, vars.values());

    }

    /**
     * Method to add the computed variables to the node stack.
     * Could be considered analgous to
     * {@link org.knime.core.node.ExecutionContext#createColumnRearrangeTable(org.knime.core.node.BufferedDataTable, org.knime.core.data.container.ColumnRearranger, org.knime.core.node.ExecutionMonitor)}
     * 
     * @implNote delegates to {@link #run(NodeModel, FlowVariableStackSpec)}
     *               using
     *               {@link FlowVariableStackSpec#createFromNodeModel(NodeModel)}
     *               to supply the second argument
     * @param model
     *            the non-{@code null} {@link NodeModel} instance
     * @throws IllegalStateException
     *             if any of the registered factories return the wrong number of
     *             variables for their specs, or variables with the wrong name,
     *             or of a non-convertible type to their specs
     */
    public void run(NodeModel model) throws IllegalStateException {

        run(model, FlowVariableStackSpec.createFromNodeModel(model));
    }

    /**
     * Method to add the computed variables to the node stack.
     * Could be considered analgous to
     * {@link org.knime.core.node.ExecutionContext#createColumnRearrangeTable(org.knime.core.node.BufferedDataTable, org.knime.core.data.container.ColumnRearranger, org.knime.core.node.ExecutionMonitor)}
     * 
     * @param model
     *            the non-{@code null} {@link NodeModel} instance
     * @param inSpec
     *            the incoming flow variable stack
     * @throws IllegalStateException
     *             if any of the registered factories return the wrong number of
     *             variables for their specs, or variables with the wrong name,
     *             or of a non-convertible type to their specs
     */
    public void run(NodeModel model, FlowVariableStackSpec inSpec)
            throws IllegalStateException {

        Objects.requireNonNull(model, "A node model instance must be supplied");

        Map<String, FlowVariable> vars = new LinkedHashMap<>();
        for (FlowVariableFactory fact : facts) {
            FlowVariableSpec[] specs = fact.getVariableSpecs();
            FlowVariable[] newVars =
                    fact.getVariables(inSpec.getAllVariables());
            if (specs.length != newVars.length) {
                throw new IllegalStateException(
                        "Flow Variable Factory returned incorrected number of variables - exptected "
                                + specs.length + ", got " + newVars.length);
            }
            for (int i = 0; i < specs.length; i++) {
                FlowVariable newVar = newVars[i];
                FlowVariableSpec expectedSpec = specs[i];
                newVar = validateVariableAgainstSpec(newVar, expectedSpec);
                if (vars.put(newVar.getName(), newVar) != null) {
                    logger.codingWithFormat(
                            "Duplicate variables generated with name '%s' - likely an implementation error!",
                            newVar.getName());
                }
            }
        }
        putNewVariablesOnStack(model, vars.values());

        // And perform any finishing for the individual factories
        facts.stream().forEach(fact -> fact.afterProcessing());

    }

    private void putNewVariablesOnStack(NodeModel model,
            Iterable<FlowVariable> vars) {

        // Finish by pushing the default values onto the stack
        vars.forEach(fVar -> Node.invokePushFlowVariable(model, fVar));
    }

    /**
     * Method to ensure the variable has the expected name and type. If the type
     * is wrong, but convertible it returns the converted value, otherwise it
     * returns the supplied value unchanged
     * 
     * @throws IllegalStateException
     *             if the variable has the wrong name or the type is not
     *             convertible
     */
    private final FlowVariable validateVariableAgainstSpec(FlowVariable newVar,
            FlowVariableSpec expectedSpec) throws IllegalStateException {

        if (!newVar.getName().equals(expectedSpec.getName())) {
            throw new IllegalStateException(String.format(
                    "Expected variable with name '%s', but got '%s'",
                    expectedSpec.getName(), newVar.getName()));

        }
        if (!newVar.getVariableType().getIdentifier()
                .equals(expectedSpec.getType().getIdentifier())) {
            if (newVar.getVariableType()
                    .isConvertible(expectedSpec.getType())) {
                logger.warn(
                        "Different variable type returned from spec, but is adapatable");
                newVar = convertVariable(newVar, expectedSpec);
            } else {
                throw new IllegalStateException(String.format(
                        "Expected variable of type '%s', but got '%s' for variable with name '%s'",
                        expectedSpec.getType().getIdentifier(),
                        newVar.getVariableType().getIdentifier(),
                        newVar.getName()));
            }
        }
        return newVar;
    }

    private final <T> FlowVariable convertVariable(final FlowVariable fVar,
            FlowVariableSpec spec) {

        if (!fVar.getVariableType().isConvertible(spec.getType())) {
            throw new IllegalArgumentException(
                    String.format("Cannot convert types: %s to %s",
                            fVar.getVariableType().getIdentifier(),
                            spec.getType().getIdentifier()));
        }
        @SuppressWarnings("unchecked")
        VariableType<T> newType = (VariableType<T>) spec.getType();
        @SuppressWarnings("unchecked")
        T value = (T) fVar.getValue(spec.getType());
        return new FlowVariable(fVar.getName(), newType, value);

    }
}
