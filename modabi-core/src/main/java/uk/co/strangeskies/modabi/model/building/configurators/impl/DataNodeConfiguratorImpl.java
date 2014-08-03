package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.DataNodeWrapper;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public class DataNodeConfiguratorImpl<T>
		extends
		BindingChildNodeConfiguratorImpl<DataNodeConfigurator<T>, DataNode<T>, T, DataNodeChildNode<?>, DataNode<?>>
		implements DataNodeConfigurator<T> {
	protected static class DataNodeImpl<T> extends
			BindingChildNodeImpl<T, DataNode.Effective<T>> implements DataNode<T> {
		private static class Effective<T> extends
				BindingChildNodeImpl.Effective<T, DataNode.Effective<T>> implements
				DataNode.Effective<T> {
			private final DataBindingType.Effective<T> type;
			private final Format format;
			private final Boolean optional;
			private final BufferedDataSource providedBuffer;
			private final ValueResolution resolution;
			private T provided;

			protected Effective(
					OverrideMerge<DataNode<T>, DataNodeConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				type = overrideMerge.getValue(DataNode::type, (n, o) -> {
					DataBindingType<?> type = n;
					do
						if (type == o)
							return true;
					while ((type = type.baseType()) != null);
					return false;
				}).effective();

				optional = overrideMerge.getValue(DataNode::optional);
				format = overrideMerge.getValue(DataNode::format, Objects::equals);

				providedBuffer = overrideMerge.getValue(DataNode::providedValueBuffer);
				resolution = overrideMerge.getValue(DataNode::valueResolution,
						Objects::equals);
				provided = (resolution == ValueResolution.REGISTRATION_TIME) ? overrideMerge
						.configurator().getContext().getDataLoader()
						.loadData(DataNodeImpl.Effective.this, providedBuffer)
						: null;
			}

			@Override
			public final DataBindingType.Effective<T> type() {
				return type;
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
			public BufferedDataSource providedValueBuffer() {
				return providedBuffer;
			}

			@Override
			public T providedValue() {
				return provided;
			}

			@Override
			public ValueResolution valueResolution() {
				return resolution;
			}
		}

		private final Effective<T> effective;

		private final DataBindingType<T> type;
		private final Format format;
		private final Boolean optional;
		private final BufferedDataSource providedBuffer;
		private final ValueResolution resolution;
		private final T provided;

		DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			format = configurator.format;
			type = configurator.type;
			optional = configurator.optional;

			providedBuffer = configurator.providedBufferedValue;
			resolution = configurator.resolution;
			provided = null;

			effective = new Effective<>(overrideMerge(this, configurator));
		}

		@Override
		public final DataBindingType<T> type() {
			return type;
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
		public BufferedDataSource providedValueBuffer() {
			return providedBuffer;
		}

		@Override
		public T providedValue() {
			return provided;
		}

		@Override
		public ValueResolution valueResolution() {
			return resolution;
		}

		@Override
		public DataNodeImpl.Effective<T> effective() {
			return effective;
		}
	}

	public Format format;

	private DataBindingType<T> type;
	private BufferedDataSource providedBufferedValue;
	private ValueResolution resolution;

	private Boolean optional;

	public DataNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super DataNode<T>> parent) {
		super(parent);
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().getDataLoader();
	}

	protected final Class<?> getTypeBindingClass() {
		if (type != null)
			return type.getBindingClass();
		return getBindingClass();
	}

	protected final Class<?> getTypeUnbindingClass() {
		if (type != null)
			return type.getUnbindingClass();
		return getUnbindingClass();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <V extends T> DataNodeConfigurator<V> dataClass(
			Class<V> dataClass) {
		return (DataNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataNodeConfigurator<U> type(
			DataBindingType<U> type) {
		requireConfigurable(this.type);
		this.type = (DataBindingType<T>) type;

		return (DataNodeConfigurator<U>) getThis();
	}

	@Override
	protected Set<DataNode<T>> getOverriddenNodes() {
		Set<DataNode<T>> overriddenNodes = new HashSet<>(super.getOverriddenNodes());

		overriddenNodes.add(new DataNodeWrapper<>(type.effective()));

		return overriddenNodes;
	}

	@Override
	protected void finaliseProperties() {
		if (!isFinalisedProperties()) {
			List<ChildNode<?>> newInheritedChildren = new ArrayList<>();
			getOverriddenNodes().forEach(
					c -> c.children().forEach(n -> newInheritedChildren.add(n)));

			getChildren().inheritChildren(0, newInheritedChildren);
		}

		super.finaliseProperties();
	}

	@Override
	public DataNodeConfigurator<T> provideValue(BufferedDataSource dataSource) {
		requireConfigurable(providedBufferedValue);
		providedBufferedValue = dataSource;

		return getThis();
	}

	@Override
	public DataNodeConfigurator<T> valueResolution(ValueResolution valueResolution) {
		requireConfigurable(this.resolution);
		this.resolution = valueResolution;

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

	@SuppressWarnings("unchecked")
	@Override
	protected final Class<DataNode<T>> getNodeClass() {
		return (Class<DataNode<T>>) (Object) DataNode.class;
	}

	@Override
	protected final DataNode<T> tryCreate() {
		return new DataNodeImpl<>(this);
	}

	@Override
	public final Class<T> getDataClass() {
		if (type != null)
			return type.getDataClass();
		return super.getDataClass();
	}

	@Override
	public final BindingStrategy getBindingStrategy() {
		if (type != null)
			return type.getBindingStrategy();
		return super.getBindingStrategy();
	}

	@Override
	public final Class<?> getBindingClass() {
		if (type != null)
			return type.getBindingClass();
		return super.getBindingClass();
	}

	@Override
	public final UnbindingStrategy getUnbindingStrategy() {
		if (type != null)
			return type.getUnbindingStrategy();
		return super.getUnbindingStrategy();
	}

	@Override
	public final Class<?> getUnbindingClass() {
		if (type != null)
			return type.getUnbindingClass();
		return super.getUnbindingClass();
	}
}
