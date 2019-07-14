package org.uichuimi.vcf.lazy;

import java.util.function.Function;

/**
 * A lazy property that will be parsed based on a {@link Function}.
 *
 * @param <T>
 * 		type of the property
 */
public class ObjectProperty<T> extends LazyProperty<T> {

	private Function<String, T> function;

	/**
	 * Creates an already parsed object. Use this initializer is value is already known.
	 *
	 * @param value
	 * 		a non null value
	 */
	ObjectProperty(T value) {
		super(value);
	}

	/**
	 * Creates an object in its raw format and the function to be invoked to convert it to its real
	 * form.
	 *
	 * @param raw
	 * 		the raw representation of the value
	 * @param function
	 * 		the converter from String to T
	 */
	public ObjectProperty(String raw, Function<String, T> function) {
		super(raw);
		this.function = function;
	}

	@Override
	protected T parse(String raw) {
		return function.apply(raw);
	}
}
