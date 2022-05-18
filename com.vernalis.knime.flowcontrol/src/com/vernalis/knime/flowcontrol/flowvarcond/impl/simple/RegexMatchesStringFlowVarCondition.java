/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.flowvarcond.impl.simple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.swing.JTextField;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableType.StringType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperCheckbox;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperStringEntry;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * A {@link FlowVarCondition} implementation to perform Regular Expression
 * matches on string variables
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class RegexMatchesStringFlowVarCondition
		implements FlowVarCondition<String> {

	private static final Map<String, Integer> FLAGS = new LinkedHashMap<>();
	static {
		FLAGS.put("Case Insensitive", Pattern.CASE_INSENSITIVE);
		FLAGS.put("Allow Comments", Pattern.COMMENTS);
		FLAGS.put("Multiline", Pattern.MULTILINE);
		FLAGS.put("UNIX lines", Pattern.UNIX_LINES);
		FLAGS.put("Literal", Pattern.LITERAL);
		FLAGS.put("Dot All", Pattern.DOTALL);
		FLAGS.put("Unicode Case", Pattern.UNICODE_CASE);
		FLAGS.put("Canonical Equivalence", Pattern.CANON_EQ);
		FLAGS.put("Unicode Character Class", Pattern.UNICODE_CHARACTER_CLASS);
	}

	@Override
	public String getID() {
		return "Regex Match";
	}

	@Override
	public String getDisplayName() {
		return "Matches Regex";
	}

	@Override
	public VariableType<String> getApplicableVariableType() {
		return StringType.INSTANCE;
	}

	@Override
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model) {
		checkType(variable);
		List<ComponentWrapper<?, ?, ?>> comps = model.getComponents();
		String regex = ((ComponentWrapperStringEntry) comps.get(0)).getValue();
		int flags = comps.stream()
				.filter(c -> c instanceof ComponentWrapperCheckbox)
				.map(c -> (ComponentWrapperCheckbox) c)
				.filter(c -> c.getValue()).map(c -> c.getID())
				.filter(c -> FLAGS.containsKey(c)).mapToInt(c -> FLAGS.get(c))
				.reduce(0, (i, j) -> i | j);
		Pattern patt = Pattern.compile(regex, flags);
		Predicate<String> pred = patt.asPredicate();
		if (model.isInverted()) {
			pred = pred.negate();
		}
		return pred.test(variable.getValue(getApplicableVariableType()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition#
	 * getReferenceComponents()
	 */
	@Override
	public List<ComponentWrapper<?, ?, ?>> getReferenceComponents() {
		List<ComponentWrapper<?, ?, ?>> retVal = new ArrayList<>();
		retVal.add(new ComponentWrapperStringEntry(new JTextField(),
				"Reference Value"));
		for (Entry<String, Integer> cb : FLAGS.entrySet()) {
			retVal.add(new ComponentWrapperCheckbox(cb.getKey(), true));
		}
		return retVal;
	}

	@Override
	public String getDescription() {
		return "Test whether a string variable matches a given regular expression";
	}

}
