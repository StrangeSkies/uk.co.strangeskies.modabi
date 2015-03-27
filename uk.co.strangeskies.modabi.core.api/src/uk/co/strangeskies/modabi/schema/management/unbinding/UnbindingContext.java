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
package uk.co.strangeskies.modabi.schema.management.unbinding;

import java.util.List;

import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.management.Provisions;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public interface UnbindingContext extends UnbindingState {
	Provisions provisions();

	<T> List<Model.Effective<T>> getMatchingModels(TypeLiteral<T> dataClass);

	<T> ComputingMap<Model.Effective<? extends T>, ComplexNode.Effective<? extends T>> getComplexNodeOverrides(
			ComplexNode.Effective<T> element);

	<T> ComputingMap<DataBindingType.Effective<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrides(
			DataNode.Effective<T> node);

	StructuredDataTarget output();
}
