/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.benchmarks.
 *
 * uk.co.strangeskies.modabi.benchmarks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.benchmarks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.benchmarks.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.benchmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PersonsType {
	private final List<PersonType> person;

	public PersonsType() {
		person = new ArrayList<>();
	}

	public PersonsType(Collection<? extends PersonType> person) {
		this.person = new ArrayList<>(person);
	}

	public List<PersonType> getPerson() {
		return person;
	}
}
