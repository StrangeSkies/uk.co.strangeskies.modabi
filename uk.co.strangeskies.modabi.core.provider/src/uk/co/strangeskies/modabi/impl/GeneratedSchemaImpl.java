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
package uk.co.strangeskies.modabi.impl;

import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.modabi.DataTypes;
import uk.co.strangeskies.modabi.GeneratedSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.impl.processing.DataNodeBinder;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class GeneratedSchemaImpl implements GeneratedSchema {
	private final SchemaManagerImpl manager;

	private final QualifiedName name;

	private final Schemata dependencies;

	private final DataTypes types;
	private final Models models;

	private Imports imports;

	public GeneratedSchemaImpl(SchemaManagerImpl schemaManager, QualifiedName name,
			Collection<? extends Schema> dependencies) {
		this.manager = schemaManager;

		this.name = name;

		this.dependencies = new Schemata();
		this.dependencies.addAll(dependencies);
		this.dependencies.add(schemaManager.getBaseSchema());
		this.dependencies.add(schemaManager.getMetaSchema());

		types = new DataTypes();
		models = new Models();

		imports = Imports.empty();
	}

	@Override
	public DataTypes getDataTypes() {
		return types;
	}

	@Override
	public Models getModels() {
		return models;
	}

	@Override
	public QualifiedName getQualifiedName() {
		return name;
	}

	@Override
	public Schemata getDependencies() {
		return dependencies;
	}

	@Override
	public Imports getImports() {
		return imports;
	}

	@Override
	public void addImports(Imports imports) {
		this.imports = this.imports.withImports(imports.getImportedClasses())
				.withPackageImports(imports.getImportedPackages());
	}

	@Override
	public <T> Model<T> generateModel(TypeToken<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> DataType<T> generateDataType(TypeToken<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> DataType<T> buildDataType(Function<DataTypeConfigurator<Object>, DataTypeConfigurator<T>> build) {
		ProcessingContextImpl context = manager.getProcessingContext();

		DataType<T> type = build.apply(manager.getDataTypeBuilder().configure(DataNodeBinder.dataLoader(context))).create();

		manager.registerDataType(type);

		return type;
	}

	@Override
	public <T> Model<T> buildModel(Function<ModelConfigurator<Object>, ModelConfigurator<T>> build) {
		ProcessingContextImpl context = manager.getProcessingContext();

		Model<T> model = build.apply(manager.getModelBuilder().configure(DataNodeBinder.dataLoader(context))).create();

		manager.registerModel(model);

		return model;
	}
}
