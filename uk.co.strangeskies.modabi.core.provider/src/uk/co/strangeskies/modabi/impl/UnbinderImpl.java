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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.Unbinder;
import uk.co.strangeskies.modabi.impl.processing.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.classpath.ContextClassLoaderRunner;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class UnbinderImpl<T> implements Unbinder<T> {
	private final SchemaManagerImpl manager;
	private final T data;

	private final Function<ProcessingContext, List<Model.Effective<T>>> unbindingFunction;

	private final Set<Provider> providers;
	private ClassLoader classLoader;

	public UnbinderImpl(SchemaManagerImpl manager, T data,
			Function<ProcessingContext, List<Model.Effective<T>>> unbindingFunction) {
		this.manager = manager;
		this.data = data;
		this.unbindingFunction = unbindingFunction;

		providers = new HashSet<>();
	}

	@Override
	public BindingFuture<T> to(File output) {
		return to(ManifestUtilities.getResourceExtension(output.getName()), () -> new FileOutputStream(output));
	}

	@Override
	public BindingFuture<T> to(String extension, ThrowingSupplier<OutputStream, ?> output) {
		try (OutputStream stream = output.get()) {
			to(manager.registeredFormats().get(extension).saveData(stream));
		} catch (Exception e) {
			throw new SchemaException("Could not unbind to output with unloader registered for " + extension, e);
		}

		return null;
	}

	@Override
	public BindingFuture<T> to(URL output) {
		String extension = ManifestUtilities.getResourceExtension(output.getQuery());

		try {
			if (extension != null) {
				return to(extension, output.openConnection()::getOutputStream);
			} else {
				throw new SchemaException("No output format registered to handle " + output.getPath());
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <U extends StructuredDataTarget> U to(U output) {
		ProcessingContextImpl context = manager.getProcessingContext().withOutput(output);
		context = context.withNestedProvisionScope();
		context.provisions().addAll(providers);

		List<? extends Model.Effective<? extends T>> models = unbindingFunction.apply(context);

		if (models.isEmpty()) {
			throw new ProcessingException("Cannot find any model to unbind '" + data + "'", context);
		}

		ProcessingContextImpl finalContext = context;
		context.attemptUnbindingUntilSuccessful(models, (c, m) -> {
			unbindImpl(c, m, output);
		}, e -> new ProcessingException("Cannot unbind data '" + data + "' with models '" + models + "'", finalContext, e));

		return output;
	}

	@SuppressWarnings("unchecked")
	private <U extends T> void unbindImpl(ProcessingContext context, Model.Effective<U> model,
			StructuredDataTarget output) {
		output.registerDefaultNamespaceHint(model.getName().getNamespace());

		try {
			context.output().get().addChild(model.getName());

			ClassLoader classLoader = this.classLoader != null ? this.classLoader
					: Thread.currentThread().getContextClassLoader();

			new ContextClassLoaderRunner(classLoader).run(() -> new BindingNodeUnbinder(context).unbind(model, (U) data));

			context.output().get().endChild();
		} catch (ProcessingException e) {
			throw e;
		} catch (Exception e) {
			throw new ProcessingException("Unexpected problem during uninding of '" + data + "' according to '" + model + "'",
					context, e);
		}
	}

	@Override
	public Unbinder<T> withClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}

	@Override
	public Unbinder<T> withProvider(Provider provider) {
		providers.add(provider);
		return this;
	}

	@Override
	public Unbinder<T> withErrorHandler(Consumer<Exception> errorHandler) {
		// TODO Auto-generated method stub
		return null;
	}
}
