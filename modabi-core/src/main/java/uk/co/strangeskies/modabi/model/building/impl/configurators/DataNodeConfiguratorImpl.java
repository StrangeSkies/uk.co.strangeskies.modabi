package uk.co.strangeskies.modabi.model.building.impl.configurators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Value.ValueResolution;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public class DataNodeConfiguratorImpl<T>
		extends
		BindingChildNodeConfiguratorImpl<DataNodeConfigurator<T>, DataNode<T>, T, DataNodeChildNode, DataNode<?>>
		implements DataNodeConfigurator<T> {
	protected static class DataNodeImpl<T> extends BindingChildNodeImpl<T>
			implements DataNode<T> {
		private final DataBindingType<T> type;
		private final Value<T> value;
		private final Format format;
		private final Boolean optional;

		DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			format = configurator.format;
			type = configurator.type;
			optional = configurator.optional;

			value = new Value<T>() {
				private final BufferedDataSource providedBuffer = configurator.providedBufferedValue;
				private final ValueResolution resolution = configurator.resolution;

				@Override
				public BufferedDataSource providedBuffer() {
					return providedBuffer;
				}

				@Override
				public T provided() {
					return null;
				}

				@Override
				public ValueResolution resolution() {
					return resolution;
				}
			};

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
				List<ChildNode> effectiveChildren, Class<?> outputTargetClass,
				DataLoader loader) {
			super(overrideWithType(node), overriddenNodes, effectiveChildren,
					outputTargetClass);

			OverrideMerge<DataNode<T>> overrideMerge = new OverrideMerge<>(node,
					overriddenNodes);

			type = overrideMerge.getValue(n -> n.type(), (n, o) -> {
				DataBindingType<?> type = n;
				do
					if (type == o)
						return true;
				while ((type = type.baseType()) != null);
				return false;
			});
			optional = overrideMerge.getValue(n -> n.optional());
			format = overrideMerge.getValue(n -> n.format(), (n, o) -> n == o);

			value = new Value<T>() {
				private final BufferedDataSource providedBuffer = overrideMerge
						.getValue(n -> n.value().providedBuffer());
				private final ValueResolution resolution = overrideMerge.getValue(
						n -> n.value().resolution(), (n, o) -> n == o);
				private final T provided = (resolution == ValueResolution.REGISTRATION_TIME) ? loader
						.loadData(DataNodeImpl.this, providedBuffer) : null;

				@Override
				public BufferedDataSource providedBuffer() {
					return providedBuffer;
				}

				@Override
				public T provided() {
					return provided;
				}

				@Override
				public ValueResolution resolution() {
					return resolution;
				}
			};
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataNode))
				return false;

			DataNode<?> other = (DataNode<?>) obj;

			return super.equals(obj)
					&& Objects.equals(type, other.type())
					&& Objects.equals(value.provided(), other.value().provided())
					&& Objects.equals(value.providedBuffer(), other.value()
							.providedBuffer())
					&& Objects.equals(value.resolution(), other.value().resolution())
					&& Objects.equals(format, other.format())
					&& Objects.equals(optional, other.optional());
		}

		private static <T> DataNode<T> overrideWithType(DataNode<T> node) {
			node = DataNode.wrapType(node);
			return node;
		}

		@Override
		public final DataBindingType<T> type() {
			return type;
		}

		@Override
		public Value<T> value() {
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
		dataClass(type.getDataClass());
		this.type = (DataBindingType<T>) type;

		inheritChildren(type.effectiveType().getChildren());

		return (DataNodeConfigurator<U>) getThis();
	}

	@Override
	protected void finaliseProperties() {
		if (!isFinalisedProperties()) {
			List<ChildNode> newInheritedChildren = new ArrayList<>();
			getOverriddenNodes().forEach(
					c -> c.getChildren().forEach(n -> newInheritedChildren.add(n)));

			inheritChildren(0, newInheritedChildren);
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

	@Override
	protected final DataNode<T> getEffective(DataNode<T> node) {
		return new DataNodeImpl<>(node, getOverriddenNodes(),
				getEffectiveChildren(),
				getContext().getCurrentChildOutputTargetClass(), getDataLoader());
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

	@Override
	public ChildBuilder<DataNodeChildNode, DataNode<?>> addChild() {
		SchemaNodeConfigurationContext<ChildNode> context = new SchemaNodeConfigurationContext<ChildNode>() {
			@Override
			public DataLoader getDataLoader() {
				return DataNodeConfiguratorImpl.this.getDataLoader();
			}

			@Override
			public <U extends ChildNode> Set<U> overrideChild(String id,
					Class<U> nodeClass) {
				return DataNodeConfiguratorImpl.this.overrideChild(id, nodeClass);
			}

			@Override
			public Class<?> getCurrentChildOutputTargetClass() {
				return DataNodeConfiguratorImpl.this.getCurrentChildOutputTargetClass();
			}

			@Override
			public Class<?> getCurrentChildInputTargetClass() {
				return DataNodeConfiguratorImpl.this.getCurrentChildInputTargetClass();
			}

			@Override
			public void addChild(ChildNode result, ChildNode effective) {
				DataNodeConfiguratorImpl.this.addChild(result, effective);
			}
		};

		return new ChildBuilder<DataNodeChildNode, DataNode<?>>() {
			@Override
			public SequenceNodeConfigurator<DataNodeChildNode, DataNode<?>> sequence() {
				return new SequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public InputSequenceNodeConfigurator<DataNode<?>> inputSequence() {
				return new InputSequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public DataNodeConfigurator<Object> data() {
				return new DataNodeConfiguratorImpl<>(context);
			}

			@Override
			public ChoiceNodeConfigurator<DataNodeChildNode, DataNode<?>> choice() {
				return new ChoiceNodeConfiguratorImpl<>(context);
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				throw new UnsupportedOperationException();
			}
		};
	}
}