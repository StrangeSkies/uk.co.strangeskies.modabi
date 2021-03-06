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

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.SchemaBuilder;

/**
 * Class for bootstrapping core schemata, i.e. an implementation of
 * {@link BaseSchema} and {@link MetaSchema}.
 * 
 * @author Elias N Vasylenko
 */
public class CoreSchemata {
	final BaseSchema baseSchema;
	final MetaSchema metaSchema;

	public CoreSchemata(SchemaBuilder schemaBuilder) {
		BootstrapDataLoader loader = new BootstrapDataLoader();
		baseSchema = new BaseSchemaImpl(schemaBuilder, loader);
		metaSchema = new MetaSchemaImpl(schemaBuilder, loader, baseSchema);
		loader.setComplete(baseSchema, metaSchema);
	}

	public BaseSchema baseSchema() {
		return baseSchema;
	}

	public MetaSchema metaSchema() {
		return metaSchema;
	}
}
