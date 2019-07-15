package org.uichuimi.vcf.variant;

import java.util.function.Function;

/**
 * A property that contains a double value.
 */
public class DoubleProperty extends ObjectProperty<Double> {

	private static final Function<String, Double> function = raw -> {
		if (raw.equals(VcfConstants.EMPTY_VALUE)) return null;
		return Double.valueOf(raw);
	};

	public DoubleProperty(Double value) {
		super(value);
	}

	public DoubleProperty(String raw) {
		super(raw, function);
	}

}
