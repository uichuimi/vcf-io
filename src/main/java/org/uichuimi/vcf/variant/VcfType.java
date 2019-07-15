package org.uichuimi.vcf.variant;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Types of INFO and FORMAT columns.
 */
public abstract class VcfType<T> {

	private final Function<String, T> valueExtractor;

	VcfType(Function<String, T> valueExtractor) {
		this.valueExtractor = s -> s.equals(VcfConstants.EMPTY_VALUE) ? null : valueExtractor.apply(s);
	}

	public Function<String, T> getValueExtractor() {
		return valueExtractor;
	}

	public static VcfType getInstance(String type) {
		switch (type) {
			case "Float":
				return VcfFloat.getInstance();
			case "Integer":
				return VcfInteger.getInstance();
			case "Flag":
				return VcfFlag.getInstance();
			case "String":
				return VcfString.getInstance();
			case "Character":
				return VcfCharacter.getInstance();
		}
		throw new IllegalArgumentException("Type not supported " + type);
	}

	public List<T> newList() {
		return new ArrayList<>();
	}
}
