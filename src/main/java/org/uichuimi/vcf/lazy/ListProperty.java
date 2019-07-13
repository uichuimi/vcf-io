package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.variant.VariantSet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListProperty<T> extends LazyProperty<List<T>> {

	private final static String ARRAY_SEPARATOR = ",";

	private Function<String, T> parser;

	public ListProperty(List<T> value) {
		super(value);
	}
	/**
	 *
	 * @param raw the raw string
	 * @param parser a parser that will be applied to each element
	 */
	public ListProperty(String raw, Function<String, T> parser) {
		super(raw);
		this.parser = parser;
	}

	@Override
	protected List<T> parse(String raw) {
		if (raw.equals(VariantSet.EMPTY_VALUE)) return new ArrayList<>();
		final String[] data = raw.split(ARRAY_SEPARATOR);
		final List<T> ts = new ArrayList<>(data.length);
		for (String datum : data) ts.add(parser.apply(datum));
		return ts;
	}
}
