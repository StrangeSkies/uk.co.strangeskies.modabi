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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.BindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.token.TypeToken;

public abstract class BindingPointConfiguratorImpl<T, S extends BindingPointConfigurator<T, S>>
		implements BindingPointConfigurator<T, S> {
	private RuntimeException instantiationException;

	private QualifiedName name;
	private Boolean concrete;
	private Boolean export;

	private BindingPoint<T> result;

	public BindingPointConfiguratorImpl() {

	}

	protected BindingPointConfiguratorImpl(BindingPointConfigurator<T, S> copy) {
		name = copy.getName();
		concrete = copy.getConcrete();
	}

	@Override
	public final S name(QualifiedName name) {
		this.name = name;

		return getThis();
	}

	@Override
	public final QualifiedName getName() {
		return name;
	}

	@Override
	public final S concrete(boolean concrete) {
		this.concrete = concrete;

		return getThis();
	}

	@Override
	public final Boolean getConcrete() {
		return concrete;
	}

	@Override
	public final S export(boolean export) {
		this.export = export;

		return getThis();
	}

	@Override
	public Boolean getExport() {
		return export;
	}

	protected boolean isChildContextAbstract() {
		return getConcrete() != null && !getConcrete();
	}

	@Override
	public TypeToken<T> getDataType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V> ChildBindingPointConfigurator<V> dataType(TypeToken<? super V> dataType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingPointConfigurator<?, ?> baseModel(Collection<? extends Model<?>> baseModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Model<?>> getBaseModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaNodeConfigurator node() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaNode getNode() {
		// TODO Auto-generated method stub
		return null;
	}

	protected abstract Set<BindingPoint<T>> getOverriddenBindingPoints();

	public <U> OverrideBuilder<U, BindingPoint<T>> override(
			Function<? super BindingPoint<T>, ? extends U> overriddenValues,
			Function<? super BindingPointConfigurator<T, ?>, ? extends U> overridingValue) {
		return new OverrideBuilder<>(getOverriddenBindingPoints(), overriddenValues, overridingValue.apply(this));
	}
}
