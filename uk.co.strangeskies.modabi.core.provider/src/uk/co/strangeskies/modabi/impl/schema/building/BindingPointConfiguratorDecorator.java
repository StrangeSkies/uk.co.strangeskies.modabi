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
package uk.co.strangeskies.modabi.impl.schema.building;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.BindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.token.TypeToken;

public abstract class BindingPointConfiguratorDecorator<T, S extends BindingPointConfigurator<T, S>>
		implements BindingPointConfigurator<T, S> {
	private S component;

	public BindingPointConfiguratorDecorator(S component) {
		this.component = component;
	}

	protected void setComponent(S component) {
		this.component = component;
	}

	public S getComponent() {
		return component;
	}

	@Override
	public S name(QualifiedName name) {
		component = component.name(name);
		return getThis();
	}

	@Override
	public S name(String name) {
		component = component.name(name);
		return getThis();
	}

	@Override
	public QualifiedName getName() {
		return component.getName();
	}

	@Override
	public S export(boolean export) {
		component = component.export(export);
		return getThis();
	}

	@Override
	public Boolean getExport() {
		return component.getExport();
	}

	@Override
	public S concrete(boolean concrete) {
		component = component.concrete(concrete);
		return getThis();
	}

	@Override
	public Boolean getConcrete() {
		return component.getConcrete();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> BindingPointConfigurator<V, ?> dataType(TypeToken<? super V> dataType) {
		component = (S) component.dataType(dataType);
		return (BindingPointConfigurator<V, ?>) getThis();
	}

	@Override
	public TypeToken<T> getDataType() {
		return component.getDataType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public BindingPointConfigurator<?, ?> baseModel(Collection<? extends Model<?>> baseModel) {
		component = (S) component.baseModel(baseModel);
		return getThis();
	}

	@Override
	public List<Model<?>> getBaseModel() {
		return component.getBaseModel();
	}

	@Override
	public SchemaNode getNode() {
		return component.getNode();
	}

	@Override
	public SchemaNodeConfigurator node() {
		return component.node();
	}

	@Override
	public S node(Function<SchemaNodeConfigurator, SchemaNodeConfigurator> configuration) {
		component = component.node(configuration);
		return getThis();
	}

	@Override
	public String toString() {
		return getComponent().toString();
	}
}
