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
package uk.co.strangeskies.modabi.impl.processing;

import java.util.Arrays;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputNode.InputMemberType;
import uk.co.strangeskies.reflection.ExecutableMember;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public abstract class InputNodeBinder<T extends InputNode<?>> extends ChildNodeBinder<T> {
	public InputNodeBinder(ProcessingContext context, T node) {
		super(context, node);
	}

	@SuppressWarnings("unchecked")
	protected Object invokeInMethod(Object... parameters) {
		TypedObject<?> target = getContext().getBindingObject();

		if (getNode().inputMemberType() != InputMemberType.NONE) {
			TypedObject<?> result;

			try {
				TypeToken<?> postInputType = getNode().postInputType();
				if (postInputType == null) {
					if (getNode().chainedInput()) {
						postInputType = getNode().inputExecutable().withTargetType(target.getType()).getReturnType();
					} else {
						postInputType = target.getType();
					}
				}

				result = TypedObject.castInto(postInputType,
						((ExecutableMember<Object, ?>) getNode().inputExecutable()).invoke(target.getObject(), parameters));
			} catch (Exception e) {
				throw new ProcessingException(t -> t.cannotInvoke(getNode().inputExecutable().getMember(),
						getContext().getBindingObject().getType(), getNode(), Arrays.asList(parameters)), getContext(), e);
			}

			if (getNode().chainedInput()) {
				setContext(getContext().withReplacementBindingObject(result));
				target = result;
			}
		}

		return target;
	}
}
