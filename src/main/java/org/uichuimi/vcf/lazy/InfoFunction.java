package org.uichuimi.vcf.lazy;

import java.util.function.Function;

public class InfoFunction {

	private Function<String, ?> function;
	private final boolean array;

	public InfoFunction(String type, String number) {
		this.function = getFunction(type);
		this.array = !number.equals("0") && !number.equals("1");
	}

	private Function<String, ?> getFunction(String type) {
		switch (type) {
			case "Float":
				return s1 -> {
					final float f = Float.parseFloat(s1);
					if (Float.isFinite(f)) return f;
					else return null;
				};
			case "Integer":
				return Integer::valueOf;
			case "Flag":
				// Normally if this function is called, it is because the flag is present, so it must be true
				return s -> true;
			case "String":
			case "Character":
			default:
				return Function.identity();
		}
	}

	/**
	 * Creates a non initialized property
	 *
	 * @param value
	 * 		raw value of property
	 * @return
	 */
	LazyProperty<?> getProperty(String value) {
		return array ? new ListProperty<>(value, function) : new ObjectProperty<>(value, function);
	}

}
