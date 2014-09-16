package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class BindingNodeUnbinder {
	private final UnbindingContext context;

	public BindingNodeUnbinder(UnbindingContext context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> void unbind(BindingNode.Effective<U, ?, ?> node, U data) {
		UnbindingContext context = this.context;

		context.output().nextChild(node.getName());

		Function<Object, Object> supplier = Function.identity();
		if (node.getUnbindingStrategy() != null) {
			switch (node.getUnbindingStrategy()) {
			case SIMPLE:
				break;
			case PASS_TO_PROVIDED:
				supplier = u -> {
					Object o = context.provide(node.getUnbindingClass());
					invokeMethod(node.getUnbindingMethod(), o,
							prepareUnbingingParameterList(node, u));
					return o;
				};
				break;
			case ACCEPT_PROVIDED:
				supplier = u -> {
					Object o = context.provide(node.getUnbindingClass());
					invokeMethod(node.getUnbindingMethod(), u,
							prepareUnbingingParameterList(node, o));
					return o;
				};
				break;
			case CONSTRUCTOR:
				supplier = u -> {
					Constructor<?> c = null;
					try {
						c = node.getUnbindingClass().getConstructor(u.getClass());
						return c.newInstance(prepareUnbingingParameterList(node, u));
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						throw new SchemaException("Cannot invoke constructor " + c + " on "
								+ node.getUnbindingClass(), e);
					}
				};
				break;
			case STATIC_FACTORY:
				supplier = u -> invokeMethod(node.getUnbindingMethod(), null,
						prepareUnbingingParameterList(node, u));

				break;
			case PROVIDED_FACTORY:
				supplier = u -> invokeMethod(node.getUnbindingMethod(),
						context.provide(node.getUnbindingFactoryClass()),
						prepareUnbingingParameterList(node, u));

				break;
			}
		}

		context.withUnbindingSource(supplier.apply(data));
		for (ChildNode.Effective<?, ?> child : node.children())
			child.process(getProcessingContext());

		context.output().endChild();
	}

	private SchemaProcessingContext getProcessingContext() {
		return new SchemaProcessingContext() {
			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				nodeStack.push(node);
				for (U child : getData(node))
					unbindElement(node, child);
				nodeStack.pop();
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				unbindNode(node);
			}

			@Override
			public void accept(InputSequenceNode.Effective node) {
				nodeStack.push(node);
				processChildren(node);
				nodeStack.pop();
			}

			@Override
			public void accept(SequenceNode.Effective node) {
				nodeStack.push(node);
				processChildren(node);
				nodeStack.pop();
			}

			@Override
			public void accept(ChoiceNode.Effective node) {
				nodeStack.push(node);
				nodeStack.pop();
			}
		};
	}

	private Object[] prepareUnbingingParameterList(
			BindingNode.Effective<?, ?, ?> node, Object data) {
		List<Object> parameters = new ArrayList<>();

		boolean addedData = false;
		if (node.getProvidedUnbindingMethodParameters() != null)
			for (DataNode.Effective<?> parameter : node
					.getProvidedUnbindingMethodParameters()) {
				if (parameter != null)
					parameters.add(parameter.providedValues() == null ? null : parameter
							.providedValues().get(0));
				else {
					parameters.add(data);
					addedData = true;
				}
			}
		if (!addedData)
			parameters.add(0, data);

		return parameters.toArray();
	}

	private Object invokeMethod(Method method, Object receiver,
			Object... parameters) {
		try {
			return method.invoke(receiver, parameters);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SecurityException | NullPointerException e) {
			throw new SchemaException("Cannot invoke method '"
					+ method
					+ "' on '"
					+ receiver
					+ "' with arguments '["
					+ Arrays.asList(parameters).stream().map(Objects::toString)
							.collect(Collectors.joining(", ")) + "]' at node '"
					+ getNodeStackString() + "'.", e);
		}
	}

	@SuppressWarnings("unchecked")
	public <U> List<U> getData(BindingChildNode.Effective<U, ?, ?> node) {
		List<U> itemList;

		Object parent = bindingStack.peek();

		if (node.getDataClass() == null)
			throw new SchemaException("Cannot unbind node '" + node.getName()
					+ "' with no data class.");

		if (node.isOutMethodIterable() != null && node.isOutMethodIterable()) {
			Iterable<U> iterable = null;
			if (node.getOutMethodName() != null
					&& node.getOutMethodName().equals("this"))
				iterable = (Iterable<U>) parent;
			else
				iterable = (Iterable<U>) invokeMethod(node.getOutMethod(), parent);

			itemList = StreamSupport.stream(iterable.spliterator(), false)
					.filter(Objects::nonNull).collect(Collectors.toList());
			U failedCast = itemList
					.stream()
					.filter(
							o -> !ClassUtils.isAssignable(o.getClass(), node.getDataClass()))
					.findAny().orElse(null);
			if (failedCast != null)
				throw new ClassCastException("Cannot cast " + failedCast.getClass()
						+ " to " + node.getDataClass());
		} else {
			U item;
			if (node.getOutMethodName() != null
					&& node.getOutMethodName().equals("this"))
				item = (U) parent;
			else
				item = (U) invokeMethod(node.getOutMethod(), parent);

			if (item == null)
				itemList = null;
			else {
				if (!ClassUtils.isAssignable(item.getClass(), node.getDataClass()))
					throw new ClassCastException("Cannot cast " + item.getClass()
							+ " to " + node.getDataClass());
				itemList = Arrays.asList(item);
			}
		}

		if (itemList != null && node.occurances() != null
				&& !node.occurances().contains(itemList.size()))
			throw new SchemaException("Output list '" + itemList
					+ "' must contain a number of items within range '"
					+ Range.compose(node.occurances()) + "' to be unbound by node '"
					+ node + "'.");

		return itemList;
	}
}
