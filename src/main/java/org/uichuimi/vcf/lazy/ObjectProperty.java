package org.uichuimi.vcf.lazy;

import java.util.function.Function;

public class ObjectProperty<T> extends LazyProperty<T> {

	private Function<String, T> function;

	public ObjectProperty(T value) {
		super(value);
	}

	public ObjectProperty(String raw, Function<String, T> function) {
		super(raw);
		this.function = function;
	}

	@Override
	protected T parse(String raw) {
		return function.apply(raw);
	}
}
