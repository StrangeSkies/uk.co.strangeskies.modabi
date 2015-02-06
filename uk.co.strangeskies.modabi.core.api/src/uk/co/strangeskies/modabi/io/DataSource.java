package uk.co.strangeskies.modabi.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.utilities.Copyable;

public interface DataSource extends Copyable<DataSource> {
	DataStreamState currentState();

	DataSource reset();

	int index();

	default <T> T get(DataType<T> type) {
		return get().data(type);
	}

	DataItem<?> get();

	<T extends DataTarget> T pipe(T target, int items);

	default <T extends DataTarget> T pipeNext(T target) {
		return pipe(target, 1);
	}

	int size();

	default <T extends DataTarget> T pipe(T target) {
		return pipe(target, size());
	}

	static DataSource parseString(String string,
			Function<String, QualifiedName> qualifiedNameParser) {
		List<DataItem<?>> dataItemList = new ArrayList<>();

		String[] strings = string.split(",");
		for (int i = 0; i < strings.length; i++) {
			String item = strings[i];
			if (item.charAt(item.length() - 1) == '\\')
				item += strings[i++];

			dataItemList.add(DataItem.forString(item, qualifiedNameParser));
		}

		return forDataItems(dataItemList);
	}

	static DataSource forDataItems(List<DataItem<?>> dataItemList) {
		return new DataSourceDecorator(new RepeatingDataSource(dataItemList, 0,
				dataItemList.size()));
	}

	static <T> DataSource repeating(DataType<T> type, T data, int times) {
		return repeating(DataItem.forDataOfType(type, data), times);
	}

	static <T> DataSource repeating(DataItem<?> item, int times) {
		return new DataSourceDecorator(new RepeatingDataSource(item, times));
	}

	static <T> DataSource single(DataType<T> type, T data) {
		return single(DataItem.forDataOfType(type, data));
	}

	static DataSource single(DataItem<?> item) {
		return new DataSourceDecorator(new RepeatingDataSource(item, 1));
	}

	class RepeatingDataSource implements DataSource {
		private final List<DataItem<?>> list;
		private int index;
		private final int size;

		private RepeatingDataSource(DataItem<?> item, int size) {
			this(item, 0, size);
		}

		private RepeatingDataSource(DataItem<?> item, int index, int size) {
			this(Arrays.asList(item), index, size);
		}

		private RepeatingDataSource(List<DataItem<?>> list, int index, int size) {
			this.list = list;
			this.index = index;
			this.size = size;
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof DataSource))
				return false;

			DataSource thatDataSource = ((DataSource) that).copy().reset();
			DataItem<?> item;
			while ((item = thatDataSource.get()) != null)
				if (!item.equals(this.list))
					return false;

			return thatDataSource.index() == index && thatDataSource.size() == size;
		}

		@Override
		public int hashCode() {
			return list.hashCode() ^ index ^ size;
		}

		@Override
		public int index() {
			return index;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public DataItem<?> get() {
			return list.get(index++ % list.size());
		}

		@Override
		public <U extends DataTarget> U pipe(U target, int items) {
			if (items + index() >= size)
				throw new ArrayIndexOutOfBoundsException(size);

			if (items < 0)
				throw new ArrayIndexOutOfBoundsException(items);

			for (int i = 0; i < items; i++)
				target.put(list.get((index + items) % list.size()));

			index += items;

			return target;
		}

		@Override
		public DataStreamState currentState() {
			return null;
		}

		@Override
		public DataSource reset() {
			index = 0;
			return this;
		}

		@Override
		public DataSource copy() {
			return new RepeatingDataSource(list, index, size);
		}
	}
}