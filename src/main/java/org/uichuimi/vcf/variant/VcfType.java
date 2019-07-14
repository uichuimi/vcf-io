package org.uichuimi.vcf.variant;

import java.util.function.Function;

/**
 * Types of INFO and FORMAT columns.
 */
public enum VcfType {
	Float(raw -> {
		final float f = java.lang.Float.parseFloat(raw);
		return java.lang.Float.isFinite(f) ? f : null;
	}),
	Integer(java.lang.Integer::valueOf),
	Flag(s -> true),
	String(s -> s),
	Character(s-> s);

	private final Function<String, ?> valueExtractor;

	VcfType(Function<String, ?> valueExtractor) {
		this.valueExtractor = s -> s.equals(VcfConstants.EMPTY_VALUE) ? null : valueExtractor.apply(s);
	}

	public Function<String, ?> getValueExtractor() {
		return valueExtractor;
	}
}
