/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.mapping.
 *
 * uk.co.strangeskies.modabi.mapping is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.mapping is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.mapping.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.mapping;

import java.util.List;

import uk.co.strangeskies.modabi.schema.ComplexNode;

/*
 * Ways of doing this:
 * 
 * 1) Unbind to structured data, map structured data to other structured data, rebind
 * 
 * 2) Unbind to objects at specified nodes, rebind objects to certain nodes of another schema.
 */
public interface Mapping<F, T> {
	ComplexNode<F> fromModel();

	ComplexNode<T> toModel();

	T map(F from);

	List<NodeMapping<?, ?>> children();
}
