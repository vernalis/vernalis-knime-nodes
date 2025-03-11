package com.vernalis.knime.flowvars;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableTypeRegistry;
import org.knime.core.util.Pair;

/**
 * A utility class to help with operations involving flow variables
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 13-Jun-2024
 */
public class FlowVariableUtils {

    private FlowVariableUtils() {

        // Do not instantiate - utility class
        throw new UnsupportedOperationException(
                "Utility class - may not be instantiated!");
    }

    /**
     * Method to get the 'simple' type variable corresponding to an array
     * variable type
     * 
     * @param <T>
     *            the type of value stored
     * @param type
     *            the array variable type
     * @return the corresponding simple variable type, or {@code null} if there
     *             is no
     *             such type
     */
    public static <T> VariableType<T> getSimpleTypeFromArray(
            VariableType<T[]> type) {

        Class<T[]> clazz = type.getSimpleType();
        if (!clazz.isArray()) {
            throw new IllegalArgumentException(
                    "The Supplied type is not an array type");
        }
        Class<?> compClz = clazz.getComponentType();
        @SuppressWarnings("unchecked")
        VariableType<T> retVal = (VariableType<T>) Arrays
                .stream(getAllSingletonTypes())
                .filter(t -> t.getSimpleType().isAssignableFrom(compClz))
                .findFirst().orElse(null);
        return retVal;
    }

    /**
     * Method to get the array type variable corresponding to a 'simple' type
     * 
     * @param <T>
     *            the type of value stored
     * @param type
     *            the simple variable type
     * @return the corresponding array variable type, or {@code null} if there
     *             is no such type
     */
    public static <T> VariableType<T[]> getArrayTypeFromSimple(
            VariableType<T> type) {

        Class<T> clazz = type.getSimpleType();
        if (clazz.isArray()) {
            throw new IllegalArgumentException(
                    "The supplied type may not be an array type");
        }
        @SuppressWarnings("unchecked")
        VariableType<T[]> retVal = (VariableType<T[]>) Arrays
                .stream(getAllArrayTypes()).filter(t -> t.getSimpleType()
                        .getComponentType().isAssignableFrom(clazz))
                .findFirst().orElse(null);
        return retVal;
    }

    /**
     * @return an array containing all the registered variable types which store
     *             array values
     */
    public static VariableType<?>[] getAllArrayTypes() {

        return Arrays.stream(VariableTypeRegistry.getInstance().getAllTypes())
                .filter(t -> t.getSimpleType().isArray())
                .toArray(VariableType<?>[]::new);
    }

    /**
     * @return an array containing all the registered variable types which store
     *             'simple' values (i.e. non-array values)
     */
    public static VariableType<?>[] getAllSingletonTypes() {

        return Arrays.stream(VariableTypeRegistry.getInstance().getAllTypes())
                .filter(t -> !t.getSimpleType().isArray())
                .toArray(VariableType<?>[]::new);
    }

    /**
     * @return a map of all singleton-array variable type pairings
     */
    public static Map<VariableType<?>, VariableType<?>> getSingletonToArrayTypesMap() {

        return Arrays.stream(getAllSingletonTypes())
                .map(t -> new Pair<>(t, getArrayTypeFromSimple(t)))
                .filter(p -> p.getSecond() != null)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    /**
     * @return a map of all array-singleton variable type pairings
     */
    public static Map<VariableType<?>, VariableType<?>> getArrayToSingletonTypesMap() {

        return Arrays.stream(getAllSingletonTypes())
                .map(t -> new Pair<>(t, getArrayTypeFromSimple(t)))
                .filter(p -> p.getSecond() != null)
                .collect(Collectors.toMap(Pair::getSecond, Pair::getFirst));
    }

}
