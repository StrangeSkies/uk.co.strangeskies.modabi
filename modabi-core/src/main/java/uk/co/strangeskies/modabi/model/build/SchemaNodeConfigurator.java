package uk.co.strangeskies.modabi.model.build;

import uk.co.strangeskies.gears.utilities.factory.Factory;
import uk.co.strangeskies.modabi.model.SchemaNode;

public interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode>
		extends Factory<N> {
	public S id(String name);
}
