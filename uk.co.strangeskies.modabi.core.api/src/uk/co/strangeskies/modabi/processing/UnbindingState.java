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
package uk.co.strangeskies.modabi.processing;

import java.util.List;

import uk.co.strangeskies.modabi.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaNode;

public interface UnbindingState {
	List<SchemaNode.Effective<?, ?>> unbindingNodeStack();

	default SchemaNode.Effective<?, ?> unbindingNode() {
		return unbindingNode(0);
	}

	default SchemaNode.Effective<?, ?> unbindingNode(int parent) {
		return unbindingNodeStack().get(unbindingNodeStack().size() - (1 + parent));
	}

	List<Object> unbindingSourceStack();

	default Object unbindingSource() {
		return unbindingSource(0);
	}

	default Object unbindingSource(int parent) {
		return unbindingSourceStack().get(
				unbindingSourceStack().size() - (1 + parent));
	}

	Bindings bindings();
}