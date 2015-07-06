/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.HidingChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.TypeToken;

public class ChoiceNodeConfiguratorImpl extends
		ChildNodeConfiguratorImpl<ChoiceNodeConfigurator, ChoiceNode> implements
		ChoiceNodeConfigurator {
	private Boolean mandatory;

	public ChoiceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ChildNode<?, ?>> parent) {
		super(parent);
	}

	@Override
	public ChoiceNode tryCreate() {
		return new ChoiceNodeImpl(this);
	}

	@Override
	public ChildrenConfigurator createChildrenConfigurator() {
		TypeToken<?> inputTarget = getContext().inputTargetType(getName());
		TypeToken<?> outputSource = getContext().outputSourceType();

		return new HidingChildrenConfigurator(
				new SchemaNodeConfigurationContext<ChildNode<?, ?>>() {
					@Override
					public BoundSet boundSet() {
						return getContext().boundSet();
					}

					@Override
					public DataLoader dataLoader() {
						return getDataLoader();
					}

					@Override
					public boolean isAbstract() {
						return isChildContextAbstract();
					}

					@Override
					public boolean isInputExpected() {
						return true;
					}

					@Override
					public boolean isInputDataOnly() {
						return getContext().isInputDataOnly();
					}

					@Override
					public boolean isConstructorExpected() {
						return false;
					}

					@Override
					public boolean isStaticMethodExpected() {
						return false;
					}

					@Override
					public Namespace namespace() {
						return getNamespace();
					}

					@Override
					public TypeToken<?> inputTargetType(QualifiedName node) {
						return inputTarget;
					}

					@Override
					public TypeToken<?> outputSourceType() {
						return outputSource;
					}

					@Override
					public void addChild(ChildNode<?, ?> result) {}

					@Override
					public <U extends ChildNode<?, ?>> List<U> overrideChild(
							QualifiedName id, TypeToken<U> nodeClass) {
						return null;
					}

					@Override
					public List<? extends SchemaNode<?, ?>> overriddenNodes() {
						return getOverriddenNodes();
					}
				});
	}

	@Override
	public ChoiceNodeConfigurator mandatory(boolean mandatory) {
		this.mandatory = mandatory;

		return this;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	@Override
	protected TypeToken<ChoiceNode> getNodeClass() {
		return TypeToken.over(ChoiceNode.class);
	}
}