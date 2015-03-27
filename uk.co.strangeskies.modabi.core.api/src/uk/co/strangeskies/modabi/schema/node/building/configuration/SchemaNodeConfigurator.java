/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.utilities.factory.Factory;

public interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<?, ?>>
		extends Factory<N> {
	public S name(QualifiedName name);

	public default S name(String name, Namespace namespace) {
		return name(new QualifiedName(name, namespace));
	}

	S isAbstract(boolean isAbstract);

	public ChildBuilder addChild();

	public default SchemaNodeConfigurator<?, N> addChild(
			Function<ChildBuilder, SchemaNodeConfigurator<?, ?>> builder) {
		builder.apply(addChild()).create();

		return this;
	}
}
