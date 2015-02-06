package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.reflection.TypeLiteral;

public interface AbstractModelConfigurator<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractComplexNode<T, ?, ?>, T>
		extends BindingNodeConfigurator<S, N, T>, SchemaNodeConfigurator<S, N> {
	default <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return this.<V> baseModel(Arrays.asList(baseModel));
	}

	<V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			List<? extends Model<? super V>> baseModel);

	@Override
	default <V extends T> AbstractModelConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		return (AbstractModelConfigurator<?, ?, V>) BindingNodeConfigurator.super
				.dataClass(dataClass);
	}

	@Override
	<V extends T> AbstractModelConfigurator<?, ?, V> dataType(
			TypeLiteral<V> dataClass);
}