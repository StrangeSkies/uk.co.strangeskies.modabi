package uk.co.strangeskies.modabi.schema.processing.reference;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface ReferenceSource {
	<T> T reference(Model<T> model, QualifiedName idDomain, BufferedDataSource id);
}
