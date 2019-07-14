package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.variant.VcfConstants;

import java.util.function.Function;

/**
 * A property that contains a double value.
 */
class DoubleProperty extends ObjectProperty<Double> {

	private static final Function<String, Double> function = raw -> {
		if (raw.equals(VcfConstants.EMPTY_VALUE)) return null;
		return Double.valueOf(raw);
	};

	DoubleProperty(Double value) {
		super(value);
	}

	DoubleProperty(String raw) {
		super(raw, function);
	}

}
