package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.variant.VcfConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListProperty<T> extends LazyProperty<List<T>> {

	private Function<String, T> parser;

	ListProperty(List<T> value) {
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
		if (raw.equals(VcfConstants.EMPTY_VALUE)) return new ArrayList<>();
		final String[] data = raw.split(VcfConstants.ARRAY_DELIMITER);
		final List<T> ts = new ArrayList<>(data.length);
		for (String datum : data) ts.add(parser.apply(datum));
		return ts;
	}
}
