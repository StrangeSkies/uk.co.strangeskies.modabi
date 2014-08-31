package uk.co.strangeskies.modabi.data.io.structured;

import java.util.Set;

import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface StructuredDataSource {
	public Namespace defaultNamespaceHint();

	public Set<Namespace> namespaceHints();

	public Set<String> comments();

	public QualifiedName nextChild();

	public Set<QualifiedName> properties();

	public TerminatingDataSource propertyData(QualifiedName name);

	public TerminatingDataSource content();

	public void endChild();

	public int depth();

	public int indexAtDepth();

	public default <T extends StructuredDataTarget> T pipeNextChild(T output) {
		if (defaultNamespaceHint() != null)
			output.registerDefaultNamespaceHint(defaultNamespaceHint());

		if (!namespaceHints().isEmpty())
			for (Namespace hint : namespaceHints())
				output.registerNamespaceHint(hint);

		QualifiedName childElement;

		int depth = 0;
		do {
			while ((childElement = nextChild()) != null) {
				output.nextChild(childElement);

				if (defaultNamespaceHint() != null)
					output.registerDefaultNamespaceHint(defaultNamespaceHint());
				for (Namespace hint : namespaceHints())
					output.registerNamespaceHint(hint);

				for (QualifiedName property : properties())
					propertyData(property).pipe(output.property(property)).terminate();

				TerminatingDataSource content = content();
				if (content != null)
					content.pipe(output.content()).terminate();

				depth++;
			}
			output.endChild();
			endChild();

			depth--;
		} while (depth > 0);

		return output;
	}

	public default BufferedStructuredDataSource bufferNextChild() {
		return pipeNextChild(new BufferingStructuredDataTarget()).buffer();
	}
}
