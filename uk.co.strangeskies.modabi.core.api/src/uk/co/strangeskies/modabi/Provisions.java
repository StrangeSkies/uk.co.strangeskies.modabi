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
package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;
import uk.co.strangeskies.utilities.Scoped;
import uk.co.strangeskies.utilities.collection.ObservableSet;

public interface Provisions extends ObservableSet<Provisions, Provider>, Scoped<Provisions> {
	<T> TypedObject<T> provide(TypeToken<T> type, ProcessingContext state);

	default <T> TypedObject<T> provide(Class<T> clazz, ProcessingContext state) {
		return provide(TypeToken.forClass(clazz), state);
	}

	boolean isProvided(TypeToken<?> type, ProcessingContext state);

	default boolean isProvided(Class<?> clazz, ProcessingContext state) {
		return isProvided(TypeToken.forClass(clazz), state);
	}
}
