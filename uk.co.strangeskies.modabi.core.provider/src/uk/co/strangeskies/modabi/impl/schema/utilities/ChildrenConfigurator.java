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
package uk.co.strangeskies.modabi.impl.schema.utilities;

import java.util.List;

import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utilities.Factory;

public interface ChildrenConfigurator extends Factory<List<ChildBindingPoint<?>>> {
	public ChildBindingPointConfigurator<?> addChild();

	public TypeToken<?> getPostInputType();
}
