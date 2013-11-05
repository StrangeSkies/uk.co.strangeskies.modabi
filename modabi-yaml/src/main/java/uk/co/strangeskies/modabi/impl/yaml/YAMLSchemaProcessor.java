package uk.co.strangeskies.modabi.impl.yaml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.schema.BindingSchema;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessor;

public class YAMLSchemaProcessor implements SchemaProcessor {
	@Override
	public String getFormatName() {
		return "YAML";
	}

	@Override
	public List<String> getFileExtentions() {
		return Arrays.asList("yaml");
	}

	@Override
	public <T> T processInput(BindingSchema<T> schema, InputStream input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void processOutput(T data, BindingSchema<T> schema,
			OutputStream output) {
		// TODO Auto-generated method stub

	}
}
