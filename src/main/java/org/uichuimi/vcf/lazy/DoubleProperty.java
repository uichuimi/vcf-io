package org.uichuimi.vcf.lazy;

import org.uichuimi.vcf.variant.VariantSet;

public class DoubleProperty extends LazyProperty<Double> {

	public DoubleProperty(String raw) {
		super(raw);
	}

	@Override
	protected Double parse(String raw) {
		if (raw.equals(VariantSet.EMPTY_VALUE)) return null;
		return Double.valueOf(raw);
	}
}
