package org.uichuimi.vcf.variant;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Types of INFO and FORMAT columns.
 */
public abstract class VcfType<T> {

	public static final String FLOAT = "Float";
	public static final String INTEGER = "Integer";
	public static final String FLAG = "Flag";
	public static final String STRING = "String";
	public static final String CHARACTER = "Character";
	private final Function<String, T> valueExtractor;

	VcfType(Function<String, T> valueExtractor) {
		this.valueExtractor = s -> s.equals(VcfConstants.EMPTY_VALUE) ? null : valueExtractor.apply(s);
	}

	public Function<String, T> getValueExtractor() {
		return valueExtractor;
	}

	public static VcfType getInstance(String type) {
		switch (type) {
			case FLOAT:
				return VcfFloat.getInstance();
			case INTEGER:
				return VcfInteger.getInstance();
			case FLAG:
				return VcfFlag.getInstance();
			case STRING:
				return VcfString.getInstance();
			case CHARACTER:
				return VcfCharacter.getInstance();
		}
		throw new IllegalArgumentException("Type not supported " + type);
	}

	public List<T> newList() {
		return new ArrayList<>();
	}
}
