package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode.Effective;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.reference.DereferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportDereferenceTarget;

public class SchemaUnbinder {
	private final UnbindingContext context;

	public SchemaUnbinder(SchemaManager manager) {
		Bindings bindings = new Bindings();

		ImportDereferenceTarget importTarget = new ImportDereferenceTarget() {
			@Override
			public <U> DataSource dereferenceImport(Model<U> model,
					QualifiedName idDomain, U object) {
				DataNode.Effective<?> node = (DataNode.Effective<?>) model
						.effective()
						.children()
						.stream()
						.filter(
								c -> c.getName().equals(idDomain)
										&& c instanceof DataNode.Effective<?>)
						.findAny()
						.orElseThrow(
								() -> new SchemaException("Can't fine child '" + idDomain
										+ "' to target for model '" + model + "'."));

				BufferingDataTarget target = new BufferingDataTarget();

				new DataNodeUnbinder(new UnbindingContext() {
					@Override
					public Object unbindingSource() {
						return object;
					}

					@Override
					public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public <V> V provide(Class<V> clazz) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public StructuredDataTarget output() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public <T> List<Model<? extends T>> getMatchingModels(
							Effective<T> element, Class<?> dataClass) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Bindings bindings() {
						// TODO Auto-generated method stub
						return null;
					}
				}).unbind(node);

				return target.buffer();
			}
		};

		DereferenceTarget dereferenceTarget = new DereferenceTarget() {
			@Override
			public <U> DataSource dereference(Model<U> model, QualifiedName idDomain,
					U object) {
				if (!bindings.get(model).contains(object))
					throw new SchemaException("Cannot find any instance '" + object
							+ "' bound to model '" + model.getName() + "'.");

				return importTarget.dereferenceImport(model, idDomain, object);
			}
		};

		context = new UnbindingContext() {
			@Override
			public Object unbindingSource() {
				return null;
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
				return Collections.emptyList();
			}

			@SuppressWarnings("unchecked")
			@Override
			public <U> U provide(Class<U> clazz) {
				if (clazz.equals(DereferenceTarget.class))
					return (U) dereferenceTarget;
				if (clazz.equals(ImportDereferenceTarget.class))
					return (U) importTarget;

				return manager.provide(clazz);
			}

			@Override
			public StructuredDataTarget output() {
				return null;
			}

			@Override
			public Bindings bindings() {
				return bindings;
			}

			@Override
			public <T> List<Model<? extends T>> getMatchingModels(
					Effective<T> element, Class<?> dataClass) {
				return manager.registeredModels().getMatchingModels(element, dataClass);
			}
		};
	}

	public <T> void unbind(Model.Effective<T> model, StructuredDataTarget output,
			T data) {
		UnbindingContext context = this.context.withOutput(output);

		output.registerDefaultNamespaceHint(model.getName().getNamespace());

		try {
			new BindingNodeUnbinder(context).unbind(model, data);
		} catch (SchemaException e) {
			throw e;
		} catch (Exception e) {
			throw context.exception("Unexpected problem during uninding.", e);
		}
	}
}
