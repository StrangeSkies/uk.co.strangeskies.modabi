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
package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.management.SchemaProcessingContext;

public interface InputSequenceNode extends
		InputNode<InputSequenceNode, InputSequenceNode.Effective>,
		DataNodeChildNode<InputSequenceNode, InputSequenceNode.Effective> {
	interface Effective extends InputSequenceNode,
			InputNode.Effective<InputSequenceNode, Effective> {
		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}
	}
}
