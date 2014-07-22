package uk.co.strangeskies.modabi.model.building.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.collection.HashSetMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.SetMultiMap;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.impl.ChoiceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.configurators.impl.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.configurators.impl.ElementNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.configurators.impl.InputSequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.configurators.impl.SequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class Children<C extends ChildNode, B extends BindingChildNode<?>> {
	private boolean blocked;

	private final List<ChildNode> children;
	private final List<ChildNode> effectiveChildren;
	private final SetMultiMap<String, ChildNode> namedInheritedChildren;
	private final List<ChildNode> inheritedChildren;

	public Children() {
		children = new ArrayList<>();
		effectiveChildren = new ArrayList<>();
		inheritedChildren = new ArrayList<>();
		namedInheritedChildren = new HashSetMultiHashMap<>();
	}

	public List<ChildNode> getChildren() {
		return children;
	}

	public List<ChildNode> getEffectiveChildren() {
		List<ChildNode> effectiveChildren = new ArrayList<>();
		effectiveChildren.addAll(inheritedChildren);
		effectiveChildren.addAll(this.effectiveChildren);
		return effectiveChildren;
	}

	public SetMultiMap<String, ChildNode> getNamedInheritedChildren() {
		return namedInheritedChildren;
	}

	public void assertUnblocked() {
		if (blocked)
			throw new SchemaException("Blocked from adding children");
	}

	public void addChild(ChildNode result, ChildNode effective) {
		blocked = false;
		children.add(result);
		effectiveChildren.add(effective);
		if (result.getId() != null) {
			Set<ChildNode> removed = namedInheritedChildren.remove(result.getId());
			if (removed != null)
				inheritedChildren.removeAll(removed);
		}
	}

	public void inheritChildren(List<? extends ChildNode> nodes) {
		inheritChildren(inheritedChildren.size(), nodes);
	}

	public void inheritChildren(int index, List<? extends ChildNode> nodes) {
		inheritNamedChildren(nodes);
		inheritedChildren.addAll(index, nodes);
	}

	public void inheritNamedChildren(List<? extends ChildNode> nodes) {
		nodes.stream().filter(c -> c.getId() != null)
				.forEach(c -> namedInheritedChildren.add(c.getId(), c));
	}

	@SuppressWarnings("unchecked")
	public <U extends ChildNode> Set<U> overrideChild(String id,
			Class<U> nodeClass) {
		Set<ChildNode> overriddenNodes = namedInheritedChildren.get(id);

		if (overriddenNodes != null) {
			if (overriddenNodes.stream().anyMatch(
					n -> !nodeClass.isAssignableFrom(n.getClass())))
				throw new SchemaException(
						"Cannot override with node of a different class");
		} else
			overriddenNodes = new HashSet<>();

		return (Set<U>) Collections.unmodifiableSet(overriddenNodes);
	}

	public ChildBuilder<C, B> addChild(DataLoader loader, Class<?> inputTarget,
			Class<?> outputtarget) {
		assertUnblocked();
		blocked = true;

		SchemaNodeConfigurationContext<ChildNode> context = new SchemaNodeConfigurationContext<ChildNode>() {
			@Override
			public DataLoader getDataLoader() {
				return loader;
			}

			@Override
			public <U extends ChildNode> Set<U> overrideChild(String id,
					Class<U> nodeClass) {
				return Children.this.overrideChild(id, nodeClass);
			}

			@Override
			public Class<?> getCurrentChildOutputTargetClass() {
				return outputtarget;
			}

			@Override
			public Class<?> getCurrentChildInputTargetClass() {
				return inputTarget;
			}

			@Override
			public void addChild(ChildNode result, ChildNode effective) {
				Children.this.addChild(result, effective);
			}
		};

		return new ChildBuilder<C, B>() {
			@Override
			public InputSequenceNodeConfigurator<B> inputSequence() {
				return new InputSequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public DataNodeConfigurator<Object> data() {
				return new DataNodeConfiguratorImpl<Object>(context);
			}

			@Override
			public ChoiceNodeConfigurator<C, B> choice() {
				return new ChoiceNodeConfiguratorImpl<>(context);
			}

			@Override
			public SequenceNodeConfigurator<C, B> sequence() {
				return new SequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				return new ElementNodeConfiguratorImpl<>(context);
			}
		};
	}
}
