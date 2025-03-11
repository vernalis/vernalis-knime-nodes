package com.vernalis.knime.flowvars;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeModel;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.NodeContext;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableTypeRegistry;
import org.knime.core.util.UniqueNameGenerator;

/**
 * A simple object to mimic a 'spec' of the currently available flow variables.
 * Broadly analogous to {@link org.knime.core.data.DataTableSpec}.
 * As {@link org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec}
 * does not allow access to the available flow variables we use this object
 * based on a {@link Supplier}
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class FlowVariableStackSpec implements Iterable<FlowVariableSpec> {

    private final Supplier<Map<String, FlowVariable>> availableFlowVariablesSupplier;
    private final NodeDialogPane pane;

    /**
     * Constructor from a supplier
     * 
     * @param availableFlowVariablesSupplier
     *            supplier returning the current
     *            available flow variables
     */
    public FlowVariableStackSpec(
            Supplier<Map<String, FlowVariable>> availableFlowVariablesSupplier) {

        this(availableFlowVariablesSupplier, null);
    }

    private FlowVariableStackSpec(
            Supplier<Map<String, FlowVariable>> availableFlowVariablesSupplier,
            NodeDialogPane dialog) {

        this.availableFlowVariablesSupplier = availableFlowVariablesSupplier;
        this.pane = dialog;
    }

    /**
     * Factory method in which the supplier is based on the {@link NodeContext}
     * 
     * @return an instance
     * @throws NullPointerException
     *             if there is no node context
     */
    public static FlowVariableStackSpec createFromNodeContext()
            throws NullPointerException {

        return new FlowVariableStackSpec(
                () -> NodeContext.getContext().getNodeContainer()
                        .getFlowObjectStack().getAllAvailableFlowVariables());
    }


    /**
     * Factory method to create an instance from a {@link NodeModel}. NB this
     * will include any 'output' variables already added to the stack, e.g.
     * during a configure step adding defaults
     * 
     * @param model
     *            the node model instance
     * @return an instance with all available variable
     *             types included
     * @throws NullPointerException
     *             is the model is {@code null}
     * @implNote calls {@link #createFromNodeModel(NodeModel, boolean)} with
     *               second parameter {@code false}
     * @see #createFromNodeModel(NodeModel, boolean)
     */
    public static FlowVariableStackSpec createFromNodeModel(NodeModel model)
            throws NullPointerException {

        return createFromNodeModel(model, false);
    }

    /**
     * Factory method to create an instance from a {@link NodeModel}
     * 
     * @param model
     *            the node model instance
     * @param inputVariablesOnly
     *            only include input variables
     * @return an instance with all available variable
     *             types included
     * @throws NullPointerException
     *             is the model is {@code null}
     * @since 12-Jun-2024
     */
    public static FlowVariableStackSpec createFromNodeModel(NodeModel model,
            boolean inputVariablesOnly) throws NullPointerException {

        Objects.requireNonNull(model);
        return new FlowVariableStackSpec(() -> inputVariablesOnly
                ? model.getAvailableInputFlowVariables(
                        VariableTypeRegistry.getInstance().getAllTypes())
                : model.getAvailableFlowVariables(
                        VariableTypeRegistry.getInstance().getAllTypes()));
    }

    /**
     * Factory method to create an instance from a {@link NodeDialogPane}
     * 
     * @param dialog
     *            the node dialog instance
     * @return an instance with all available variable
     *             types included
     * @throws NullPointerException
     *             is the dialog is {@code null}
     */
    public static FlowVariableStackSpec createFromNodeDialog(
            NodeDialogPane dialog) {

        return new FlowVariableStackSpec(
                () -> dialog.getAvailableFlowVariables(
                        VariableTypeRegistry.getInstance().getAllTypes()),
                dialog);
    }

    @Override
    public Iterator<FlowVariableSpec> iterator() {

        return availableFlowVariablesSupplier.get().values().stream()
                .map(FlowVariableSpec::new).toList().iterator();
    }

    /**
     * @param name
     *            the variable name
     * @return the {@link FlowVariableSpec} of an available variable, or
     *             null of
     *             there is no variable with the requested name
     */
    public FlowVariableSpec getVariableSpec(String name) {

        final FlowVariable fVar =
                availableFlowVariablesSupplier.get().get(name);
        return fVar == null ? FlowVariableSpec.createInvalidSpec(name)
                : new FlowVariableSpec(fVar);
    }

    /**
     * Method to check whether a variable with the given name exists in the
     * stack spec
     * 
     * @param name
     *            the name to check
     * @return {@code true} if there is a variable with the name
     */
    public boolean containsVariable(String name) {

        return availableFlowVariablesSupplier.get().containsKey(name);
    }

    /**
     * @return a Map of the variable names and variables available at the time
     *             of the call
     */
    public Map<String, FlowVariable> getAllVariables() {

        return availableFlowVariablesSupplier.get();
    }

    /**
     * @param types
     *            the allowed variable types
     * @return the available flow variables of the requested types
     */
    public Map<String, FlowVariable> getFlowVariables(VariableType<?>[] types) {

        List<VariableType<?>> l = Arrays.asList(types);
        return getAllVariables().entrySet().stream()
                .filter(v -> l.contains(v.getValue().getVariableType()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                        (v1, v2) -> v1, LinkedHashMap::new));
    }

    /**
     * Create model and register a new variable for a specific settings entry
     * for a hierarchical settings object.
     * This can serve two purposes:
     * 1) replace the actual value in the settings object by the value of
     * the variable
     * 2) and/or put the current value of the settings object into the
     * specified variable.
     * <em>NB</em> This method delegates to the {@link NodeDialogPane} if
     * present otherwise is not available
     * 
     * @param keys
     *            hierarchy of keys of
     *            corresponding settings object
     * @param type
     *            of variable/settings object
     * @return new FlowVariableModel which is
     *             already registered
     * @throws UnsupportedOperationException
     *             if the object was not created
     *             from a {@link NodeDialogPane}
     */
    public FlowVariableModel createFlowVariableModel(final String[] keys,
            final VariableType<?> type) throws UnsupportedOperationException {

        if (pane == null) {
            throw new UnsupportedOperationException();
        }
        return pane.createFlowVariableModel(keys, type);
    }

    /**
     * Create model and register a new variable for a specific settings entry
     * (in a non-hierarchical settings object).
     * This can serve two purposes:
     * 1) replace the actual value in the settings object by the value of
     * the variable
     * 2) and/or put the current value of the settings object into the
     * specified variable.
     * <em>NB</em> This method delegates to the {@link NodeDialogPane} if
     * present otherwise is not available
     * 
     * @param key
     *            of corresponding settings
     *            object
     * @param type
     *            of variable/settings object
     * @return new FlowVariableModel which is
     *             already registered
     * @throws UnsupportedOperationException
     *             if the object was not created
     *             from a {@link NodeDialogPane}
     */
    public FlowVariableModel createFlowVariableModel(final String key,
            final VariableType<?> type) throws UnsupportedOperationException {

        return createFlowVariableModel(new String[] { key }, type);
    }

    /**
     * @return a {@link UniqueNameGenerator} based on the current state of the
     *             stack to allow new unique names to be generated. NB
     *             subsequent calls to this method will return new generators
     *             based on the current state of the flow variable stack, not
     *             accounting for any suggestions made by previously returned
     *             objects
     * @since 11-Jun-2024
     */
    public UniqueNameGenerator getUniqueNameGenerator() {

        return new UniqueNameGenerator(getAllVariables().keySet());
    }

}
