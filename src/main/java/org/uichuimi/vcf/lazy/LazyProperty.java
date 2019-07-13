package org.uichuimi.vcf.lazy;

public abstract class LazyProperty<T> {

	private String raw;
	private T value;

	public LazyProperty(String raw) {
		this.raw = raw;
	}

	public LazyProperty(T value) {
		this.value = value;
	}

	public T getValue() {
		if (value == null) {
			value = parse(raw);
			raw = null;
		}
		return value;
	}

	protected abstract T parse(String raw);

	public void setValue(T value) {
		raw = null;
		this.value = value;
	}
}
