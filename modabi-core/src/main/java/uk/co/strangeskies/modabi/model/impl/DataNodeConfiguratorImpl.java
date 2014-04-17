package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public class DataNodeConfiguratorImpl<T> extends
		BindingChildNodeConfiguratorImpl<DataNodeConfigurator<T>, DataNode<T>, T>
		implements DataNodeConfigurator<T> {
	protected static class DataNodeImpl<T> extends BindingChildNodeImpl<T>
			implements DataNode<T> {
		private final DataType<T> type;
		private final BufferedDataSource value;
		private final Format format;
		private final Boolean optional;

		DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			format = configurator.format;
			type = configurator.type;
			optional = configurator.optional;

			value = configurator.value;

			if (super.getDataClass() != null
					&& !super.getDataClass().isAssignableFrom(type.getDataClass()))
				throw new SchemaException();

			if (super.getBindingStrategy() != null
					&& !super.getBindingStrategy().equals(type.getBindingStrategy()))
				throw new SchemaException();

			if (super.getBindingClass() != null
					&& !super.getBindingClass().equals(type.getBindingClass()))
				throw new SchemaException();

			if (super.getUnbindingStrategy() != null
					&& !super.getUnbindingStrategy().equals(type.getUnbindingStrategy()))
				throw new SchemaException();

			if (super.getUnbindingClass() != null
					&& !super.getUnbindingClass().equals(type.getUnbindingClass()))
				throw new SchemaException();

			if (super.getUnbindingMethod() != null
					&& !super.getUnbindingMethod().equals(type.getUnbindingMethod()))
				throw new SchemaException();
		}

		DataNodeImpl(DataNode<T> node, Collection<DataNode<T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> parentClass) {
			super(node, overriddenNodes, effectiveChildren, parentClass);

			type = getValue(node, overriddenNodes, n -> n.type());

			optional = getValue(node, overriddenNodes, n -> n.optional());

			format = getValue(node, overriddenNodes, n -> n.format(),
					(n, o) -> n == o);

			value = getValue(node, overriddenNodes, n -> n.value());

			if (value != null && !getDataClass().isAssignableFrom(value.getClass()))
				throw new SchemaException();
		}

		@Override
		public final DataType<T> type() {
			return type;
		}

		@Override
		public BufferedDataSource value() {
			return value;
		}

		@Override
		public final Format format() {
			return format;
		}

		@Override
		public final Boolean optional() {
			return optional;
		}

		@Override
		public final Class<T> getDataClass() {
			if (type() != null)
				return type().getDataClass();
			return super.getDataClass();
		}

		@Override
		public final BindingStrategy getBindingStrategy() {
			if (type() != null)
				return type().getBindingStrategy();
			return super.getBindingStrategy();
		}

		@Override
		public final Class<?> getBindingClass() {
			if (type() != null)
				return type().getBindingClass();
			return super.getBindingClass();
		}

		@Override
		public final UnbindingStrategy getUnbindingStrategy() {
			if (type() != null)
				return type().getUnbindingStrategy();
			return super.getUnbindingStrategy();
		}

		@Override
		public final Class<?> getUnbindingClass() {
			if (type() != null)
				return type().getUnbindingClass();
			return super.getUnbindingClass();
		}

		@Override
		public final Method getUnbindingMethod() {
			if (type() != null)
				return type().getUnbindingMethod();
			return super.getUnbindingMethod();
		}
	}

	public Format format;

	private DataType<T> type;
	private BufferedDataSource value;

	private Boolean optional;

	public DataNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super DataNode<T>> parent) {
		super(parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <V extends T> DataNodeConfigurator<V> dataClass(
			Class<V> dataClass) {
		return (DataNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataNodeConfigurator<U> type(DataType<U> type) {
		requireConfigurable(this.type);
		dataClass(type.getDataClass());
		this.type = (DataType<T>) type;

		return (DataNodeConfigurator<U>) getThis();
	}

	@Override
	public DataNodeConfigurator<T> value(BufferedDataSource dataSource) {
		requireConfigurable(value);

		return getThis();
	}

	@Override
	public final DataNodeConfigurator<T> optional(boolean optional) {
		requireConfigurable(this.optional);
		this.optional = optional;

		return this;
	}

	@Override
	public final DataNodeConfigurator<T> format(Format format) {
		requireConfigurable(this.format);
		this.format = format;

		return this;
	}

	@Override
	protected final DataNode<T> getEffective(DataNode<T> node) {
		return new DataNodeImpl<>(node, getOverriddenNodes(),
				getEffectiveChildren(), getContext().getCurrentChildOutputTargetClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected final Class<DataNode<T>> getNodeClass() {
		return (Class<DataNode<T>>) (Object) DataNode.class;
	}

	@Override
	protected final DataNode<T> tryCreate() {
		return new DataNodeImpl<>(this);
	}
}
