/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.modabi.schema.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class SequenceNodeConfiguratorImpl extends ChildNodeConfiguratorImpl<SequenceNodeConfigurator, SequenceNode>
		implements SequenceNodeConfigurator {
	public SequenceNodeConfiguratorImpl(SchemaNodeConfigurationContext parent) {
		super(parent);
	}

	public SequenceNodeConfiguratorImpl(SequenceNodeConfiguratorImpl copy) {
		super(copy);
	}

	@Override
	public SequenceNodeConfigurator copy() {
		return new SequenceNodeConfiguratorImpl(this);
	}

	@Override
	public SequenceNode createImpl() {
		return new SequenceNodeImpl(this);
	}

	@Override
	public ChildrenConfigurator createChildrenConfigurator() {
		TypeToken<?> inputTarget = getContext().inputTargetType();
		TypeToken<?> outputSource = getContext().outputSourceType();

		return new SequentialChildrenConfigurator(new SchemaNodeConfigurationContext() {
			@Override
			public void addChildConfigurator(ChildNodeConfigurator<?, ?> configurator) {
				SequenceNodeConfiguratorImpl.this.addChildConfigurator(configurator);
			}

			@Override
			public SchemaNode<?> parent() {
				return getResult();
			}

			@Override
			public BoundSet boundSet() {
				return getContext().boundSet();
			}

			@Override
			public DataLoader dataLoader() {
				return getDataLoader();
			}

			@Override
			public Imports imports() {
				return getImports();
			}

			@Override
			public boolean isAbstract() {
				return isChildContextAbstract();
			}

			@Override
			public boolean isInputExpected() {
				/*
				 * We shouldn't bind input if our parent doesn't expect it, or if our
				 * parent expects a constructor or static method
				 */
				return getContext().isInputExpected() && !getContext().isConstructorExpected()
						&& !getContext().isStaticMethodExpected();
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
			public TypeToken<?> inputTargetType() {
				return inputTarget;
			}

			@Override
			public TypeToken<?> outputSourceType() {
				return outputSource;
			}

			@Override
			public List<? extends SchemaNode<?>> overriddenAndBaseNodes() {
				return getOverriddenAndBaseNodes();
			}
		});
	}

	@Override
	protected boolean isChildContextAbstract() {
		return getContext().isAbstract() || super.isChildContextAbstract();
	}
}
