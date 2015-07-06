/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io.structured;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.IOException;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredData.BufferedStructuredData;

/**
 * It shouldn't matter what order attributes are added to a child, or whether
 * they are added before, after, or between other children. Because of this,
 * {@link BufferingStructuredDataTarget} does not produce a
 * {@link BufferedStructuredDataSource} which tries to match input order.
 * Instead, in an effort to make it easier for consumers to deal with stream
 * order, it adds a guarantee that buffered attributes will appear before any
 * other children types when piped. Similarly, it guarantees that all global
 * namespace hints will be piped before the rest of the document begins, and
 * non-global hints will be piped before any children of the child they occur
 * in.
 *
 * @author Elias N Vasylenko
 *
 */
public class BufferingStructuredDataTarget extends
		StructuredDataTargetImpl<BufferingStructuredDataTarget> {
	private final Deque<BufferingStructuredData> stack = new ArrayDeque<>(
			Arrays.asList(new BufferingStructuredData(null)));

	private Namespace defaultNamespaceHint;
	private final Set<Namespace> namespaceHints = new HashSet<>();

	private final List<String> comments = new ArrayList<>();

	@Override
	public void registerDefaultNamespaceHintImpl(Namespace namespace) {
		if (stack.isEmpty())
			if (defaultNamespaceHint != null)
				throw new IOException(
						"Cannot register multiple default namespace hints at any given location.");
			else
				defaultNamespaceHint = namespace;
		else
			stack.peek().setDefaultNamespaceHint(namespace);
	}

	@Override
	public void registerNamespaceHintImpl(Namespace namespace) {
		if (stack.isEmpty())
			namespaceHints.add(namespace);
		else
			stack.peek().addNamespaceHint(namespace);
	}

	@Override
	public DataTarget writePropertyImpl(QualifiedName name) {
		return stack.peek().addProperty(name);
	}

	@Override
	public DataTarget writeContentImpl() {
		return stack.peek().addContent();
	}

	@Override
	public void nextChildImpl(QualifiedName name) {
		stack.push(new BufferingStructuredData(name));
	}

	@Override
	public void endChildImpl() {
		BufferingStructuredData element = stack.pop();
		stack.peek().addChild(element);
	}

	public BufferedStructuredDataSource buffer(boolean linked, boolean consumable) {
		if (!linked && stack.size() != 1)
			throw new IllegalStateException("Stack depth '" + stack.size()
					+ "' should be 1.");

		return new BufferedStructuredDataSourceImpl(
				stack.getFirst().buffer(linked), defaultNamespaceHint, namespaceHints,
				comments, consumable);
	}

	public BufferedStructuredDataSource buffer() {
		return buffer(false, false);
	}

	@Override
	public void commentImpl(String comment) {
		if (stack.isEmpty())
			comments.add(comment);
		else
			stack.peek().comment(comment);
	}
}

class BufferedStructuredDataSourceImpl extends StructuredDataSourceImpl
		implements BufferedStructuredDataSource {
	private final Namespace defaultNamespaceHint;
	private final Set<Namespace> namespaceHints;

	private final List<String> comments;

	private final BufferedStructuredData root;
	private final Deque<BufferedStructuredData> stack;

	private final boolean consumable;

	public BufferedStructuredDataSourceImpl(BufferedStructuredData root,
			Namespace defaultNamespaceHint, Set<Namespace> namespaceHints,
			List<String> comments, boolean consumable) {
		this(root, new ArrayDeque<>(Arrays.asList(root)), defaultNamespaceHint,
				namespaceHints, comments, consumable);
	}

	public BufferedStructuredDataSourceImpl(BufferedStructuredData root,
			Deque<BufferedStructuredData> stack, Namespace defaultNamespaceHint,
			Set<Namespace> namespaceHints, List<String> comments, boolean consumable) {
		this.root = root;
		this.stack = stack;
		this.defaultNamespaceHint = defaultNamespaceHint;
		this.namespaceHints = namespaceHints;
		this.comments = comments;
		this.consumable = consumable;
	}

	@Override
	public Namespace getDefaultNamespaceHintImpl() {
		if (stack.isEmpty())
			return defaultNamespaceHint;
		else
			return stack.peek().defaultNamespaceHint();
	}

	@Override
	public Set<Namespace> getNamespaceHintsImpl() {
		if (stack.isEmpty())
			return namespaceHints;
		else
			return stack.peek().namespaceHints();
	}

	@Override
	public List<String> getCommentsImpl() {
		if (stack.isEmpty())
			return comments;
		else
			return stack.peek().comments();
	}

	@Override
	public DataSource readPropertyImpl(QualifiedName name) {
		return stack.peek().propertyData(name);
	}

	@Override
	public Set<QualifiedName> getProperties() {
		return stack.peek().properties();
	}

	@Override
	public QualifiedName startNextChildImpl() {
		BufferedStructuredData child = stack.peek().nextChild();

		if (child == null)
			return null;

		stack.push(child);
		return stack.peek().name();
	}

	@Override
	public QualifiedName peekNextChild() {
		return stack.peek() == null ? null : stack.peek().peekNextChild();
	}

	@Override
	public boolean hasNextChild() {
		return stack.peek().hasNextChild();
	}

	@Override
	public void endChildImpl() {
		stack.pop();
	}

	@Override
	public DataSource readContentImpl() {
		DataSource content = stack.peek().content();
		return content == null ? null : content;
	}

	@Override
	public void reset() {
		stack.clear();
		stack.push(root);
		root.reset();
	}

	@Override
	public BufferedStructuredDataSourceImpl split() {
		BufferedStructuredDataSourceImpl copy = new BufferedStructuredDataSourceImpl(
				root, stack, defaultNamespaceHint, namespaceHints, comments, consumable);
		return copy;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof BufferedStructuredDataSource))
			return false;

		BufferedStructuredDataSource thatCopy = (BufferedStructuredDataSource) that;

		if (depth() != thatCopy.depth()
				|| indexAtDepth() != thatCopy.indexAtDepth())
			return false;

		thatCopy = thatCopy.split();
		thatCopy.reset();

		if (!Objects.equals(defaultNamespaceHint,
				thatCopy.getDefaultNamespaceHint()))
			return false;

		if (!Objects.equals(namespaceHints, thatCopy.getNamespaceHints()))
			return false;

		return root.equals(((BufferedStructuredDataSourceImpl) thatCopy
				.pipeNextChild(new BufferingStructuredDataTarget()).buffer()).root);
	}

	@Override
	public int hashCode() {
		return root.hashCode() + depth() + indexAtDepth();
	}

	@Override
	public int depth() {
		return stack.size();
	}

	@Override
	public int indexAtDepth() {
		return stack.peek().childIndex();
	}
}

class BufferingStructuredData {
	private final QualifiedName name;

	private Namespace defaultNamespaceHint;
	private final Set<Namespace> namespaceHints;

	private final Map<QualifiedName, BufferingDataTarget> properties;
	private BufferingDataTarget content;

	private final List<BufferingStructuredData> children;

	private final Set<String> comments;

	private final Set<Consumer<BufferedStructuredData>> childListeners;

	public BufferingStructuredData(QualifiedName name) {
		namespaceHints = new HashSet<>();
		comments = new HashSet<>();

		children = new ArrayList<>();
		properties = new LinkedHashMap<>();
		this.name = name;
		childListeners = new HashSet<>();
	}

	public void comment(String comment) {
		comments.add(comment);
	}

	public void addNamespaceHint(Namespace namespace) {
		namespaceHints.add(namespace);
	}

	public void setDefaultNamespaceHint(Namespace namespace) {
		if (defaultNamespaceHint != null)
			throw new IOException(
					"Cannot register multiple default namespace hints at any given location.");
		defaultNamespaceHint = namespace;
	}

	public BufferingDataTarget addProperty(QualifiedName name) {
		BufferingDataTarget target = new BufferingDataTarget();
		properties.put(name, target);
		return target;
	}

	public void addChild(BufferingStructuredData element) {
		children.add(element);
	}

	public BufferingDataTarget addContent() {
		content = new BufferingDataTarget();
		return content;
	}

	public BufferedStructuredData buffer(boolean linked) {
		return new BufferedStructuredData(this, linked);
	}

	public void addChildListener(Consumer<BufferedStructuredData> childConsumer) {
		childListeners.add(childConsumer);
	}

	public void endChild() {}

	public static class BufferedStructuredData {
		private final QualifiedName name;

		private final Namespace defaultNamespaceHint;
		private final Set<Namespace> namespaceHints;

		private final List<String> comments;

		private final Map<QualifiedName, DataSource> properties;
		private final DataSource content;

		private final List<BufferedStructuredData> children;
		private int childIndex;

		public BufferedStructuredData(BufferingStructuredData from, boolean linked) {
			name = from.name;

			defaultNamespaceHint = from.defaultNamespaceHint;
			namespaceHints = new HashSet<>(from.namespaceHints);

			comments = new ArrayList<>(from.comments);

			children = from.children.stream()
					.map(b -> new BufferedStructuredData(b, linked))
					.collect(Collectors.toList());
			childIndex = 0;
			if (linked)
				from.addChildListener(children::add);

			properties = new LinkedHashMap<>();
			for (Map.Entry<QualifiedName, BufferingDataTarget> property : from.properties
					.entrySet())
				properties.put(property.getKey(), property.getValue().buffer());
			content = from.content == null ? null : from.content.buffer();
		}

		public Set<Namespace> namespaceHints() {
			return namespaceHints;
		}

		public Namespace defaultNamespaceHint() {
			return defaultNamespaceHint;
		}

		public List<String> comments() {
			return comments;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BufferedStructuredData))
				return false;
			BufferedStructuredData that = (BufferedStructuredData) obj;

			return super.equals(obj) && childIndex == that.childIndex
					&& Objects.equals(defaultNamespaceHint, that.defaultNamespaceHint)
					&& Objects.equals(namespaceHints, that.namespaceHints)
					&& Objects.equals(name, that.name)
					&& Objects.equals(properties, that.properties)
					&& Objects.equals(content, that.content)
					&& Objects.equals(children, that.children);
		}

		@Override
		public int hashCode() {
			int hashCode = childIndex;
			if (name != null)
				hashCode += name.hashCode();
			if (properties != null)
				hashCode += properties.hashCode();
			if (content != null)
				hashCode += content.hashCode();
			if (children != null)
				hashCode += children.hashCode();
			return hashCode;
		}

		public BufferedStructuredData nextChild() {
			if (childIndex == children.size())
				return null;
			return children.get(childIndex++);
		}

		public QualifiedName peekNextChild() {
			if (childIndex == children.size())
				return null;
			return children.get(childIndex).name();
		}

		public boolean hasNextChild() {
			return childIndex < children.size();
		}

		public QualifiedName name() {
			return name;
		}

		public Set<QualifiedName> properties() {
			return properties.keySet();
		}

		public DataSource propertyData(QualifiedName name) {
			return properties.get(name);
		}

		public DataSource content() {
			return content;
		}

		public int childIndex() {
			return childIndex;
		}

		public void reset() {
			if (content != null)
				content.reset();

			for (DataSource property : properties.values())
				property.reset();

			childIndex = 0;
			for (BufferedStructuredData child : children)
				child.reset();
		}
	}
}
