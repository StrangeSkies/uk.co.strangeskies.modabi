package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.reflection.TypeLiteral;

public interface ComplexNodeConfigurator<T> extends
		AbstractModelConfigurator<ComplexNodeConfigurator<T>, ComplexNode<T>, T>,
		BindingChildNodeConfigurator<ComplexNodeConfigurator<T>, ComplexNode<T>, T> {
	@Override
	default <V extends T> ComplexNodeConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return baseModel(Arrays.asList(baseModel));
	}

	@Override
	<V extends T> ComplexNodeConfigurator<V> baseModel(
			List<? extends Model<? super V>> baseModel);

	@SuppressWarnings("unchecked")
	@Override
	default <V extends T> ComplexNodeConfigurator<V> dataClass(Class<V> dataClass) {
		return (ComplexNodeConfigurator<V>) AbstractModelConfigurator.super
				.dataClass(dataClass);
	}

	@Override
	<V extends T> ComplexNodeConfigurator<V> dataType(TypeLiteral<V> dataClass);

	ComplexNodeConfigurator<T> inline(boolean inline);
}