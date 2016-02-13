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

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.processing.BindingContextImpl;
import uk.co.strangeskies.modabi.impl.processing.BindingNodeBinder;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;

public class BindingFutureImpl<T> implements BindingFuture<T> {
	private interface TryGet<T> {
		T tryGet() throws InterruptedException, ExecutionException, TimeoutException;
	}

	public static class BindingSource<T> {
		private final Model<T> model;
		private final StructuredDataSource data;

		public BindingSource(Model<T> model, StructuredDataSource data) {
			this.model = model;
			this.data = data;
		}

		public Model.Effective<T> getModel() {
			return model.effective();
		}

		public StructuredDataSource getData() {
			return data;
		}
	}

	private final SchemaManagerImpl manager;
	private final FutureTask<BindingSource<T>> sourceFuture;
	private final FutureTask<T> dataFuture;

	public BindingFutureImpl(SchemaManagerImpl manager, Supplier<BindingSource<T>> modelSupplier,
			ClassLoader classLoader) {
		this.manager = manager;

		sourceFuture = new FutureTask<>(modelSupplier::get);
		new Thread(() -> sourceFuture.run()).start();

		ClassLoader classLoaderFinal = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();

		dataFuture = new FutureTask<>(() -> {
			Thread.currentThread().setContextClassLoader(classLoaderFinal);

			return bind(sourceFuture.get());
		});
		new Thread(() -> dataFuture.run()).start();

		manager.addBindingFuture(this);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return sourceFuture.cancel(mayInterruptIfRunning) | dataFuture.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return dataFuture.isCancelled();
	}

	@Override
	public boolean isDone() {
		return dataFuture.isDone();
	}

	@Override
	public Binding<T> get() {
		return tryGet(() -> sourceFuture.get().getModel(), dataFuture::get);
	}

	@Override
	public Binding<T> get(long timeout, TimeUnit unit) {
		return tryGet(() -> sourceFuture.get(timeout, unit).getModel(), () -> dataFuture.get(timeout, unit));
	}

	private Binding<T> tryGet(TryGet<Model<T>> getModel, TryGet<T> getData) {
		String input = "TEMP"; // TODO

		String modelString = "";
		try {
			Model<T> model = getModel.tryGet();

			modelString = " with model '" + model.getName() + "'";

			System.out.println("TESTESTE");
			System.out.println("TESTESTE");
			T data = getData.tryGet();
			System.out.println("TESTESTEROOOOOOOOOO");

			return new Binding<T>() {
				@Override
				public Model<T> getModel() {
					return model;
				}

				@Override
				public T getData() {
					return data;
				}
			};
		} catch (InterruptedException e) {
			throw new SchemaException("Unexpected interrupt during binding of '" + input + "'" + modelString, e);
		} catch (ExecutionException e) {
			throw new SchemaException("Exception during binding of '" + input + "'" + modelString, e);
		} catch (TimeoutException e) {
			throw new SchemaException("Timed out waiting for binding of '" + input + "'" + modelString, e);
		}
	}

	@Override
	public Future<Model<T>> getModelFuture() {
		return new FutureTask<>(() -> sourceFuture.get().getModel());
	}

	@Override
	public Set<BindingFuture<?>> getBlockingBindings() {
		// TODO Auto-generated method stub
		return null;
	}

	private T bind(BindingSource<T> source) {
		Model.Effective<T> model = source.getModel();
		StructuredDataSource input = source.getData();
		BindingContextImpl context = manager.getBindingContext().withInput(input);

		QualifiedName inputRoot = input.startNextChild();
		if (!inputRoot.equals(model.getName()))
			throw new BindingException("Model '" + model.getName() + "' does not match root input node '" + inputRoot + "'",
					context);

		try {
			return new BindingNodeBinder(context).bind(model);
		} catch (SchemaException e) {
			throw e;
		} catch (Exception e) {
			throw new BindingException("Unexpected problem during binding", context, e);
		}
	}
}
