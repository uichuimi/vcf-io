package org.uichuimi.vcf.lazy;

/**
 * Stores a value as a raw string until the getter is called for the first time. In that moment, the
 * string is parsed to an object of type <strong>T</strong> and nullified. <em>value</em> is
 * unmodifiable: it is set only once and cannot be replaced or modified.
 *
 * @param <T>
 * 		type of the object stored
 */
public abstract class LazyProperty<T> {

	private String raw;
	private T value;

	/**
	 * Creates a lazy property with a raw string
	 *
	 * @param raw
	 * 		raw value of the property
	 */
	LazyProperty(String raw) {
		this.raw = raw;
	}

	/**
	 * Creates a property with the value already initialized.
	 *
	 * @param value
	 * 		the actual value of the property
	 * @throws NullPointerException
	 * 		if value is null
	 */
	LazyProperty(T value) {
		if (value == null)
			throw new NullPointerException("value cannot be null");
		this.value = value;
	}

	/**
	 * Gets the value of the property as an already converted object
	 *
	 * @return the value of the property
	 */
	T getValue() {
		if (value == null) {
			value = parse(raw);
			raw = null;
		}
		return value;
	}

	/**
	 * Implementations of {@link LazyProperty} should specify how the raw string is converted to the
	 * actual value.
	 *
	 * @param raw
	 * 		string representation
	 * @return the actual value
	 */
	protected abstract T parse(String raw);
}
