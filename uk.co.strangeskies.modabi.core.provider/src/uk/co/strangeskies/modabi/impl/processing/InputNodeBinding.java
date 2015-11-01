package uk.co.strangeskies.modabi.impl.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.InputNode;

public abstract class InputNodeBinding<T extends InputNode.Effective<?, ?>>
		extends ChildNodeBinding<T> {
	public InputNodeBinding(BindingContextImpl context, T node) {
		super(context, node);
	}

	protected Object invokeInMethod(Object... parameters) {
		Object target = getContext().bindingTarget();

		Object result;

		if (!"null".equals(getNode().getInMethodName())) {
			try {
				result = ((Method) getNode().getInMethod()).invoke(target, parameters);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new BindingException("Unable to call method '"
						+ getNode().getInMethod() + "' with parameters '"
						+ Arrays.toString(parameters) + "' at node '" + getNode() + "'",
						getContext(), e);
			}

			if (getNode().isInMethodChained()) {
				setContext(getContext().withReplacementBindingTarget(result));
				target = result;
			}
		} else {
			result = null;
		}

		return target;
	}
}