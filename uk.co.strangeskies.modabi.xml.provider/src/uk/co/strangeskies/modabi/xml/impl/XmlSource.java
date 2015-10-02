/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.xml.provider.
 *
 * uk.co.strangeskies.modabi.xml.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.xml.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.xml.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.xml.impl;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.IOException;
import uk.co.strangeskies.modabi.io.structured.BufferableStructuredDataSourceImpl;
import uk.co.strangeskies.modabi.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSourceWrapper;
import uk.co.strangeskies.modabi.io.structured.StructuredDataState;

public class XmlSource implements StructuredDataSource {
	private final XMLStreamReader in;
	private final Deque<Integer> currentLocation;

	private QualifiedName nextChild;
	private final List<String> comments;
	private final Map<QualifiedName, DataSource> properties;
	private String content;

	private XmlSource(XMLStreamReader in) {
		this.in = in;
		currentLocation = new ArrayDeque<>();

		comments = new ArrayList<>();
		properties = new HashMap<>();

		pumpEvents();
	}

	public static StructuredDataSourceWrapper from(InputStream in) {
		return from(createXMLStreamReader(in));
	}

	public static StructuredDataSourceWrapper from(XMLStreamReader in) {
		return new BufferableStructuredDataSourceImpl(new XmlSource(in));
	}

	private static XMLStreamReader createXMLStreamReader(InputStream in) {
		try {
			return XMLInputFactory.newInstance().createXMLStreamReader(in);
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new SchemaException(e);
		}
	}

	@Override
	public Namespace getDefaultNamespaceHint() {
		return Namespace.parseHttpString(in.getNamespaceURI());
	}

	@Override
	public Set<Namespace> getNamespaceHints() {
		Set<Namespace> namespaces = new HashSet<>();
		for (int i = 0; i < in.getNamespaceCount(); i++)
			namespaces.add(Namespace.parseHttpString(in.getNamespaceURI(i)));
		return namespaces;
	}

	@Override
	public QualifiedName startNextChild() {
		if (nextChild == null)
			throw new IOException();

		currentLocation.push(0);

		return pumpEvents();
	}

	@Override
	public QualifiedName peekNextChild() {
		return nextChild;
	}

	@Override
	public boolean hasNextChild() {
		return nextChild != null;
	}

	private QualifiedName pumpEvents() {
		if (nextChild != null)
			fillProperties();

		QualifiedName thisChild = nextChild;
		nextChild = null;

		comments.clear();
		content = null;

		boolean done = false;
		do {
			int code;
			try {
				code = in.next();
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}

			switch (code) {
			case XMLStreamReader.START_ELEMENT:
				QName name = in.getName();
				QualifiedName qualifiedName = new QualifiedName(name.getLocalPart(),
						Namespace.parseHttpString(name.getNamespaceURI()));

				nextChild = qualifiedName;

				done = true;

				break;
			case XMLStreamReader.END_DOCUMENT:
			case XMLStreamReader.END_ELEMENT:
				done = true;
				break;
			case XMLStreamReader.COMMENT:
				comments.add(in.getText());
				break;
			case XMLStreamReader.CHARACTERS:
				content = in.getText();
				break;
			}
		} while (!done);

		return thisChild;
	}

	private void fillProperties() {
		properties.clear();

		for (int i = 0; i < in.getAttributeCount(); i++) {
			String namespaceString = in.getAttributeNamespace(i);
			if (namespaceString == null)
				namespaceString = in.getNamespaceContext().getNamespaceURI("");
			Namespace namespace = Namespace.parseHttpString(namespaceString);

			QualifiedName propertyName = new QualifiedName(
					in.getAttributeLocalName(i), namespace);

			properties.put(propertyName,
					DataSource.parseString(in.getAttributeValue(i), this::parseName));
		}
	}

	private QualifiedName parseName(String name) {
		String[] splitName = name.split(":", 2);

		String prefix;
		if (splitName.length == 2)
			prefix = splitName[0];
		else
			prefix = "";

		return new QualifiedName(splitName[splitName.length - 1], Namespace
				.parseHttpString(in.getNamespaceContext().getNamespaceURI(prefix)));
	}

	@Override
	public Set<QualifiedName> getProperties() {
		return new HashSet<>(properties.keySet());
	}

	@Override
	public DataSource readProperty(QualifiedName name) {
		return properties.get(name);
	}

	@Override
	public DataSource readContent() {
		return DataSource.parseString(content, this::parseName);
	}

	@Override
	public void endChild() {
		if (nextChild != null)
			while (pumpEvents() != null)
				;
		currentLocation.pop();
		currentLocation.push(currentLocation.pop() + 1);

		pumpEvents();
	}

	@Override
	public List<Integer> index() {
		return new ArrayList<>(currentLocation);
	}

	@Override
	public List<String> getComments() {
		return comments;
	}

	@Override
	public StructuredDataState currentState() {
		throw new AssertionError();
	}

	@Override
	public StructuredDataSource split() {
		throw new AssertionError();
	}

	@Override
	public BufferedStructuredDataSource buffer() {
		throw new AssertionError();
	}
}