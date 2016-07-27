/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.reflection.TypeToken;

public interface BindingChildNodeConfigurator<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T, N>, T>
		extends BindingNodeConfigurator<S, N, T>, InputNodeConfigurator<S, N> {
	S outMethod(String methodName);

	static String noOutMethod() {
		return InputNodeConfigurator.noInMethod();
	}

	String getOutMethod();

	S outMethodUnchecked(boolean unchecked);

	Boolean getOutMethodUnchecked();

	S outMethodIterable(boolean iterable);

	Boolean getOutMethodIterable();

	S outMethodCast(boolean cast);

	Boolean getOutMethodCast();

	S extensible(boolean extensible);

	Boolean getExtensible();

	S synchronous(boolean synchronous);

	Boolean getSynchronous();

	S nullIfOmitted(boolean nullIfOmitted);

	Boolean getNullIfOmitted();

	TypeToken<T> getExpectedType();
}
